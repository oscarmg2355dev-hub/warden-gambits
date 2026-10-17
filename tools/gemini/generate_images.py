#!/usr/bin/env python3
"""Generate node images and fighter portraits with the Gemini API and convert them for the app.

Reads the prompt table in docs/content/prompts-imagenes-pixelart.md
(rows: | <key>.webp | <title> | <prompt> |), calls Gemini for each prompt
that has no image yet in docs/content/gemini/, and saves the result as
<key>.png. The `convert` command turns every image found in
docs/content/gemini/ (png, jpg, jfif, webp) into a 1024x768 WebP in
app/src/main/res/drawable-nodpi/ (center crop to 4:3).

API key: environment variable GEMINI_API_KEY (never stored in the repo).

Usage:
  python tools/gemini/generate_images.py generate [--zone bree] [--only bree_1_0,bree_2_0]
                                                  [--limit 5] [--force] [--dry-run]
                                                  [--model MODEL] [--delay 6]
  python tools/gemini/generate_images.py convert [--only ...] [--zone ...]
  python tools/gemini/generate_images.py all ...   (generate, then convert)

Portraits: add `--set portraits` to any command. It reads
docs/content/prompts-retratos-pixelart.md (rows: | portrait_<key>.webp | ... |),
asks Gemini for 1:1 images, keeps them in docs/content/gemini/portraits/ and
`convert` turns them into 256x256 WebP (center crop to 1:1, nearest-neighbour
resize to keep the pixels crisp) named portrait_<key>.webp.
"""
from __future__ import annotations

import argparse
import base64
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PROMPTS = ROOT / "docs" / "content" / "prompts-imagenes-pixelart.md"
OUT_DIR = ROOT / "docs" / "content" / "gemini"
APP_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
DEFAULT_MODEL = "gemini-2.5-flash-image"
API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"
IMAGE_EXTS = (".png", ".jpg", ".jpeg", ".jfif", ".webp")
APP_SIZE = (1024, 768)
NODE_TOTAL = 309
PORTRAIT_PREFIX = "portrait_"
# Node keys (bree_1_0) and portrait keys (portrait_boss_bree) are both accepted.
ROW = re.compile(r"^\|\s*((?:portrait_)?[a-z0-9_]+)\.webp\s*\|\s*(.*?)\s*\|\s*(.+?)\s*\|\s*$")


class ImageSet:
    """One family of images: its prompt table, where originals go and how they are converted."""

    def __init__(self, name: str, prompts: Path, out_dir: Path, aspect: str, size: tuple[int, int],
                 resample: str, quality: int):
        self.name, self.prompts, self.out_dir, self.aspect = name, prompts, out_dir, aspect
        self.size, self.resample, self.quality = size, resample, quality

    def owns(self, app_file: Path) -> bool:
        """Whether a WebP in drawable-nodpi belongs to this set (portraits are prefixed)."""
        return app_file.name.startswith(PORTRAIT_PREFIX) == (self.name == "portraits")


SETS = {
    "nodes": ImageSet("nodes", PROMPTS, OUT_DIR, "4:3", APP_SIZE, "LANCZOS", 85),
    "portraits": ImageSet("portraits", ROOT / "docs" / "content" / "prompts-retratos-pixelart.md",
                          OUT_DIR / "portraits", "1:1", (256, 256), "NEAREST", 90),
}


def load_prompts(image_set: ImageSet = SETS["nodes"]) -> list[tuple[str, str, str]]:
    rows = []
    for line in image_set.prompts.read_text(encoding="utf-8").splitlines():
        m = ROW.match(line)
        if m:
            rows.append((m.group(1), m.group(2), m.group(3)))
    return rows


def select(rows, only: str | None, zone: str | None):
    keys = {k.strip() for k in only.split(",")} if only else None
    return [r for r in rows
            if (keys is None or r[0] in keys)
            and (zone is None or r[0].split("_")[0] == zone)]


def existing_image(key: str, out_dir: Path = OUT_DIR) -> Path | None:
    for ext in IMAGE_EXTS:
        p = out_dir / f"{key}{ext}"
        if p.exists():
            return p
    return None


def call_gemini(prompt: str, model: str, api_key: str, retries: int = 5, aspect: str = "4:3") -> bytes:
    body = json.dumps({
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "responseModalities": ["IMAGE"],
            "imageConfig": {"aspectRatio": aspect},
        },
    }).encode("utf-8")
    req = urllib.request.Request(
        API_URL.format(model=model), data=body, method="POST",
        headers={"Content-Type": "application/json", "x-goog-api-key": api_key},
    )
    wait = 5.0
    for attempt in range(1, retries + 1):
        try:
            with urllib.request.urlopen(req, timeout=180) as resp:
                data = json.loads(resp.read().decode("utf-8"))
            for cand in data.get("candidates", []):
                for part in cand.get("content", {}).get("parts", []):
                    inline = part.get("inlineData") or part.get("inline_data")
                    if inline and inline.get("data"):
                        return base64.b64decode(inline["data"])
            reason = data.get("promptFeedback") or data.get("candidates", [{}])[0].get("finishReason")
            raise RuntimeError(f"no image in response ({reason})")
        except urllib.error.HTTPError as e:
            detail = e.read().decode("utf-8", "replace")[:300]
            if e.code in (429, 500, 502, 503, 504) and attempt < retries:
                print(f"    HTTP {e.code}, reintento en {wait:.0f}s…", flush=True)
                time.sleep(wait)
                wait *= 2
                continue
            raise RuntimeError(f"HTTP {e.code}: {detail}") from e
        except urllib.error.URLError as e:
            if attempt < retries:
                print(f"    red: {e.reason}, reintento en {wait:.0f}s…", flush=True)
                time.sleep(wait)
                wait *= 2
                continue
            raise
    raise RuntimeError("sin respuesta tras los reintentos")


def generate(args) -> int:
    image_set = SETS[args.set]
    rows = select(load_prompts(image_set), args.only, args.zone)
    todo = [r for r in rows if args.force or existing_image(r[0], image_set.out_dir) is None]
    if args.limit:
        todo = todo[: args.limit]
    print(f"{len(rows)} prompts seleccionados, {len(todo)} por generar (modelo {args.model}).")
    if args.dry_run:
        for key, title, _ in todo:
            print(f"  [dry-run] {key}  {title}")
        return 0
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("Falta la variable de entorno GEMINI_API_KEY.", file=sys.stderr)
        return 2
    image_set.out_dir.mkdir(parents=True, exist_ok=True)
    failures = []
    for i, (key, title, prompt) in enumerate(todo, 1):
        print(f"[{i}/{len(todo)}] {key}  {title}", flush=True)
        try:
            png = call_gemini(prompt, args.model, api_key, aspect=image_set.aspect)
            (image_set.out_dir / f"{key}.png").write_bytes(png)
        except Exception as e:  # keep going with the rest
            print(f"    ERROR: {e}", flush=True)
            failures.append(key)
        if i < len(todo):
            time.sleep(args.delay)
    print(f"Hecho: {len(todo) - len(failures)} generadas, {len(failures)} fallidas.")
    if failures:
        print("Fallidas: " + ",".join(failures))
    return 1 if failures else 0


def to_app_webp(src: Path, dst: Path, image_set: ImageSet = SETS["nodes"]) -> None:
    from PIL import Image  # Pillow
    with Image.open(src) as im:
        im = im.convert("RGB")
        w, h = im.size
        target = image_set.size[0] / image_set.size[1]
        if w / h > target:  # too wide: crop sides
            nw = round(h * target)
            im = im.crop(((w - nw) // 2, 0, (w - nw) // 2 + nw, h))
        elif w / h < target:  # too tall: crop top/bottom
            nh = round(w / target)
            im = im.crop((0, (h - nh) // 2, w, (h - nh) // 2 + nh))
        im = im.resize(image_set.size, getattr(Image, image_set.resample))
        im.save(dst, "WEBP", quality=image_set.quality, method=6)


def convert(args) -> int:
    image_set = SETS[args.set]
    rows = load_prompts(image_set)
    keys = {r[0] for r in select(rows, args.only, args.zone)}
    APP_DIR.mkdir(parents=True, exist_ok=True)
    done = skipped = 0
    for key in sorted(keys):
        src = existing_image(key, image_set.out_dir)
        if src is None:
            continue
        dst = APP_DIR / f"{key}.webp"
        if dst.exists() and dst.stat().st_mtime >= src.stat().st_mtime and not args.force:
            skipped += 1
            continue
        to_app_webp(src, dst, image_set)
        done += 1
        print(f"  {src.name} -> {dst.relative_to(ROOT)}")
    in_app = sum(1 for f in APP_DIR.glob("*.webp") if image_set.owns(f))
    total = NODE_TOTAL if image_set.name == "nodes" else len(rows)
    print(f"Convertidas {done}, ya al día {skipped}. Imágenes en la app: {in_app}/{total}.")
    return 0


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    p = argparse.ArgumentParser(description="Genera imágenes de nodos y retratos con Gemini y las prepara para la app.")
    p.add_argument("command", choices=["generate", "convert", "all"])
    p.add_argument("--set", choices=sorted(SETS), default="nodes",
                   help="nodes: imágenes de nodo 4:3 (por defecto); portraits: retratos de combate 1:1")
    p.add_argument("--zone", help="solo una zona (p. ej. bree)")
    p.add_argument("--only", help="claves separadas por comas (p. ej. bree_1_0,bree_2_0)")
    p.add_argument("--limit", type=int, default=0, help="máximo de imágenes a generar")
    p.add_argument("--force", action="store_true", help="regenerar/reconvertir aunque ya existan")
    p.add_argument("--dry-run", action="store_true", help="solo listar lo que se generaría")
    p.add_argument("--model", default=DEFAULT_MODEL)
    p.add_argument("--delay", type=float, default=6.0, help="segundos entre llamadas")
    args = p.parse_args()
    rc = 0
    if args.command in ("generate", "all"):
        rc = generate(args)
    if args.command in ("convert", "all") and not args.dry_run:
        rc = max(rc, convert(args))
    return rc


if __name__ == "__main__":
    sys.exit(main())
