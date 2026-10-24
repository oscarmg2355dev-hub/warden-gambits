#!/usr/bin/env python3
"""Convert the narration audios the user recorded into the app's assets.

Input:  docs/content/narration/<es|en>/<key>.<wav|mp3|m4a|ogg|flac>  (a " (1)" suffix is accepted;
        the version without suffix wins)
Output: app/src/main/assets/narration/<es|en>/<key>.m4a  (AAC-LC mono 24 kHz, 32 kbps or 24 kbps,
        edge silences trimmed leaving ~150 ms, loudnorm ~ -18 LUFS)

Usage: python tools/narration/convert.py [--bitrate 32|24] [--force] [--lang es|en]
A key is reconverted when its output is missing, the chosen source file changed (other file or newer
mtime) or the bitrate differs from the last conversion (local state in docs/content/narration/.converted.json,
ignored by git); --force reconverts everything. A failing file does not stop the batch: failures are listed
at the end and the exit code is 1.
Also rewrites docs/content/narracion-pendientes.md (keys without audio).
Needs imageio-ffmpeg (pip install --user imageio-ffmpeg).
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_prompts  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "docs" / "content" / "narration"
OUT = ROOT / "app" / "src" / "main" / "assets" / "narration"
MANIFEST_NAME = ".converted.json"
PENDING = ROOT / "docs" / "content" / "narracion-pendientes.md"
AAB = ROOT / "app" / "build" / "outputs" / "bundle" / "release" / "app-release.aab"
EXTS = (".wav", ".mp3", ".m4a", ".ogg", ".flac")
LANGS = ("es", "en")
MB = 1024 * 1024
EDGE = "silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.15"
FILTER = f"{EDGE},areverse,{EDGE},areverse,loudnorm=I=-18:TP=-1.5:LRA=11"


def ffmpeg_exe() -> str:
    import imageio_ffmpeg
    return imageio_ffmpeg.get_ffmpeg_exe()


def find_audio(key: str, folder: Path) -> Path | None:
    """The audio of a key: `<key>.<ext>` first, then `<key> (1).<ext>` (browser duplicate suffix)."""
    for name in (key, f"{key} (1)"):
        for ext in EXTS:
            p = folder / f"{name}{ext}"
            if p.exists():
                return p
    return None


def encode(src: Path, dst: Path, bitrate: int) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    cmd = [ffmpeg_exe(), "-y", "-v", "error", "-i", str(src), "-af", FILTER,
           "-ac", "1", "-ar", "24000", "-c:a", "aac", "-b:a", f"{bitrate}k", "-movflags", "+faststart", str(dst)]
    r = subprocess.run(cmd, capture_output=True, text=True)
    if r.returncode != 0:
        dst.unlink(missing_ok=True)
        raise RuntimeError(f"ffmpeg failed for {src.name}: {r.stderr.strip()[-300:]}")


def dir_size(path: Path) -> int:
    return sum(f.stat().st_size for f in path.rglob("*.m4a")) if path.exists() else 0


def write_pending(src_root: Path, out_root: Path, pending: Path, keys: dict) -> None:
    nl = "\n"
    sections = []
    for lang in LANGS:
        if lang not in keys:
            continue
        missing = [k for k in keys[lang] if not (out_root / lang / f"{k}.m4a").exists()]
        raw = [k for k in missing if find_audio(k, src_root / lang) is None]
        lines = [f"## {lang} ({len(missing)} de {len(keys[lang])} sin audio en la app)", "",
                 f"Sin grabar: {len(raw)}. Grabados pero sin convertir: {len(missing) - len(raw)}.", ""]
        by_zone: dict[str, list[str]] = {}
        for k in raw:
            by_zone.setdefault(build_prompts.zone_of(k), []).append(k)
        lines += [f"- **{z}** ({len(ks)}): " + ", ".join(f"`{k}`" for k in ks) for z, ks in by_zone.items()]
        sections.append(nl.join(lines) + nl)
    size = sum(dir_size(out_root / lang) for lang in LANGS) / MB
    header = (
        f"# Narración pendiente{nl}{nl}"
        f"Audios que faltan por idioma; narración en la app: {size:.1f} MB. Los textos están en "
        f"`narration-prompts.md`. Guarda cada audio como `<clave>.wav` en `docs/content/narration/<es|en>/` y "
        f"lanza `python tools/narration/convert.py`; la clave desaparecerá de esta lista. Fichero generado.{nl}"
    )
    pending.write_text(header + nl + nl.join(sections), encoding="utf-8", newline="\n")


def load_manifest(path: Path) -> dict:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return {}


def convert_all(src_root: Path, out_root: Path, pending: Path, keys: dict,
                bitrate: int = 32, force: bool = False, langs=LANGS,
                manifest: Path | None = None, failures: list | None = None) -> tuple[int, int]:
    """Convert every key that has audio and is missing or stale (other source, newer source or other bitrate).
    Returns (converted, up to date); failed files are appended to `failures` and the batch goes on."""
    manifest = manifest or src_root / MANIFEST_NAME
    state = load_manifest(manifest)
    done = skipped = 0
    for lang in langs:
        for key in keys.get(lang, {}):
            src = find_audio(key, src_root / lang)
            if src is None:
                continue
            dst = out_root / lang / f"{key}.m4a"
            entry = {"source": src.name, "mtime": src.stat().st_mtime, "bitrate": bitrate}
            prev = state.get(f"{lang}/{key}")
            fresh = (dst.exists() and prev is not None and prev["source"] == src.name
                     and prev["bitrate"] == bitrate and src.stat().st_mtime <= prev["mtime"])
            if fresh and not force:
                skipped += 1
                continue
            try:
                encode(src, dst, bitrate)
            except RuntimeError as e:
                state.pop(f"{lang}/{key}", None)
                if failures is not None:
                    failures.append(str(e))
                print(f"  ERROR {e}")
                continue
            state[f"{lang}/{key}"] = entry
            done += 1
            print(f"  {src.name} -> {lang}/{dst.name}")
    if state or manifest.exists():
        manifest.parent.mkdir(parents=True, exist_ok=True)
        manifest.write_text(json.dumps(state, indent=1, sort_keys=True), encoding="utf-8", newline="\n")
    write_pending(src_root, out_root, pending, keys)
    return done, skipped


def bundle_estimate(out_root: Path) -> str:
    if not AAB.exists():
        return "sin app-release.aab"
    base = AAB.stat().st_mtime
    delta = sum(f.stat().st_size for f in out_root.rglob("*.m4a") if f.stat().st_mtime > base)
    return f"~{(AAB.stat().st_size + delta) / MB:.0f} MB"


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    p = argparse.ArgumentParser(description="Convierte la narración grabada a AAC para la app.")
    p.add_argument("--bitrate", type=int, choices=(32, 24), default=32, help="kbps (32 por defecto)")
    p.add_argument("--force", action="store_true", help="reconvertir aunque estén al día")
    p.add_argument("--lang", choices=LANGS, help="solo un idioma")
    p.add_argument("--src", type=Path, default=SRC,
                   help="carpeta con <es|en>/<clave>.wav (por defecto docs/content/narration; la de"
                        " tts_generate.py es build/narration-tts)")
    args = p.parse_args()
    keys = build_prompts.load_nodes()
    failures: list[str] = []
    done, skipped = convert_all(args.src, OUT, PENDING, keys, args.bitrate, args.force,
                                (args.lang,) if args.lang else LANGS, failures=failures)
    counts = {lang: sum(1 for k in keys[lang] if not (OUT / lang / f"{k}.m4a").exists()) for lang in LANGS}
    size = sum(dir_size(OUT / lang) for lang in LANGS) / MB
    estimate = bundle_estimate(OUT)
    print(f"Convertidos {done}, al día {skipped}; narración en la app: {size:.1f} MB; "
          f"bundle estimado: {estimate}")
    m = re.match(r"~(\d+) MB", estimate)
    if m and int(m.group(1)) >= 190:
        print("Aviso: el bundle se acerca al límite de 200 MB de Play: prueba --bitrate 24; "
              "si no basta, Play Asset Delivery.")
    print(f"Pendientes: es {counts['es']}, en {counts['en']} -> {PENDING.relative_to(ROOT)}")
    if failures:
        print(f"Fallaron {len(failures)} archivos:")
        for f in failures:
            print(f"  - {f}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
