#!/usr/bin/env python3
"""Generate the node narration locally with Kokoro-82M (Apache-2.0, pip kokoro), as an alternative to
recording it in Google AI Studio. ~1x real time on CPU (i5-9400F).

Output: build/narration-tts/<es|en>/<key>.wav (to listen first), or with --into-narration
        docs/content/narration/<es|en>/<key>.wav, ready for convert.py. Keys that already have a recorded
        narration (docs/content/narration) or an output WAV are skipped, so a run can be stopped and resumed
        and never overwrites a Gemini recording.

Usage (from the repo root; venv setup in requirements-tts.txt):
  tools/narration/.venv-kokoro/Scripts/python tools/narration/tts_generate.py --lang es --zone bree
"""
from __future__ import annotations

import argparse
import os
import re
import sys
import time
from concurrent.futures import ProcessPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_prompts  # noqa: E402
import convert  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
STAGING = ROOT / "build" / "narration-tts"
LANGS = ("es", "en")
TITLE_PAUSE = 0.8
PARAGRAPH_PAUSE = 0.5

SENTENCE_PAUSE = 0.25

KOKORO_VOICE = {"es": "ef_dora", "en": "af_heart"}


@dataclass(frozen=True)
class Job:
    lang: str
    key: str
    text: str
    dst: Path


def plan(nodes: dict[str, dict[str, str]], out_root: Path, recorded_root: Path, langs=LANGS,
         zones=None, keys=None, limit: int | None = None) -> list[Job]:
    """The keys still to synthesize, in route order: no output WAV yet and no recorded narration."""
    jobs = []
    for lang in langs:
        for key, text in nodes.get(lang, {}).items():
            if zones and build_prompts.zone_of(key) not in zones:
                continue
            if keys and key not in keys:
                continue
            dst = out_root / lang / f"{key}.wav"
            if dst.exists() or convert.find_audio(key, recorded_root / lang) is not None:
                continue
            jobs.append(Job(lang, key, text, dst))
    return jobs[:limit] if limit else jobs


def paragraphs(text: str) -> list[str]:
    """Title first, then the body paragraphs; each one is synthesized apart and joined with a pause."""
    return [p.strip() for p in text.split("\n") if p.strip()]


def sentences(paragraph: str) -> list[str]:
    """Kokoro's espeak path (Spanish) truncates long inputs, so it gets one sentence at a time."""
    return [s for s in re.split(r"(?<=[.!?…»\"])\s+(?=[¿¡«\"A-ZÁÉÍÓÚÑ])", paragraph.strip()) if s]


def parse_voice(spec: str) -> list[tuple[str, float]]:
    """"ef_dora" or a blend "ef_dora:0.6,if_sara:0.4" -> [(voice, weight)], weights normalized to 1."""
    parts = []
    for item in spec.split(","):
        name, _, weight = item.strip().partition(":")
        parts.append((name.strip(), float(weight) if weight else 1.0))
    total = sum(w for _, w in parts)
    return [(n, w / total) for n, w in parts]


def kokoro_lang(lang: str, voice: str) -> str:
    """Kokoro pipeline code: Spanish "e"; English follows the voice prefix, American "a" (af_/am_) or
    British "b" (bf_/bm_)."""
    if lang == "es":
        return "e"
    return "b" if parse_voice(voice)[0][0].startswith("b") else "a"


def build_voice(pipe, voice: str, prosody: str | None = None):
    """Style vector with the timbre of the `voice` blend and the intonation of the `prosody` blend."""
    import torch

    def blend(spec):
        return sum(pipe.load_single_voice(n) * w for n, w in parse_voice(spec))
    timbre = blend(voice)
    melody = blend(prosody) if prosody else timbre
    return torch.cat([timbre[..., :128], melody[..., 128:]], dim=-1)


class KokoroEngine:
    """Imported lazily: kokoro and torch live in .venv-kokoro, not in the system Python.

    A Kokoro voice is a style vector: its first 128 values drive the decoder (timbre) and the last 128
    the prosody predictor (intonation, rhythm). `voice` blends the timbre and `prosody` the intonation,
    so a voice can keep its timbre and borrow another one's melody."""
    rate = 24000

    def __init__(self, voice: str | None, speed: float, prosody: str | None = None,
                 pitch: float = 0.0, pauses: float = 1.0):
        self.voice, self.prosody, self.speed, self.pitch, self.pauses = voice, prosody, speed, pitch, pauses
        self.pipes = {}

    def _pipe(self, lang: str):
        if lang not in self.pipes:
            from kokoro import KPipeline
            voice = self.voice or KOKORO_VOICE[lang]
            pipe = KPipeline(lang_code=kokoro_lang(lang, voice), repo_id="hexgrad/Kokoro-82M")
            self.pipes[lang] = (pipe, build_voice(pipe, voice, self.prosody))
        return self.pipes[lang]

    def synth(self, text: str, lang: str):
        import numpy as np
        pipe, voice = self._pipe(lang)
        parts = []
        for s in sentences(text):
            if parts:
                parts.append(np.zeros(int(self.rate * SENTENCE_PAUSE * self.pauses), "float32"))
            parts += [a.numpy() for _, _, a in pipe(s, voice=voice, speed=self.speed, split_pattern=None)]
        return np.concatenate(parts).astype("float32")


def shift_pitch(audio, rate: int, semitones: float):
    """Pitch shift keeping tempo and formants (ffmpeg rubberband), so a lower voice does not sound slowed."""
    import subprocess
    import numpy as np
    cmd = [convert.ffmpeg_exe(), "-v", "error", "-f", "f32le", "-ar", str(rate), "-ac", "1", "-i", "pipe:0",
           "-af", f"rubberband=pitch={2 ** (semitones / 12):.5f}:formant=preserved:pitchq=quality",
           "-f", "f32le", "pipe:1"]
    r = subprocess.run(cmd, input=audio.astype("float32").tobytes(), capture_output=True)
    if r.returncode != 0:
        raise RuntimeError(f"ffmpeg rubberband failed: {r.stderr.decode(errors='replace')[-300:]}")
    return np.frombuffer(r.stdout, dtype="float32")


def render(engine, job: Job) -> float:
    """Synthesize one node to job.dst (written atomically). Returns the audio length in seconds."""
    import numpy as np
    import soundfile as sf
    pauses = getattr(engine, "pauses", 1.0)
    parts = []
    for i, p in enumerate(paragraphs(job.text)):
        if parts:
            pause = TITLE_PAUSE if i == 1 else PARAGRAPH_PAUSE
            parts.append(np.zeros(int(engine.rate * pause * pauses), "float32"))
        parts.append(engine.synth(p, job.lang))
    audio = np.concatenate(parts)
    if getattr(engine, "pitch", 0.0):
        audio = shift_pitch(audio, engine.rate, engine.pitch)
    job.dst.parent.mkdir(parents=True, exist_ok=True)
    tmp = job.dst.with_name(job.dst.stem + ".part.wav")
    sf.write(tmp, audio, engine.rate, subtype="PCM_16")
    os.replace(tmp, job.dst)
    return len(audio) / engine.rate


# --- worker pool --------------------------------------------------------------------------------------

_engine = None


def _init_worker(args, threads: int) -> None:
    global _engine
    import torch
    torch.set_num_threads(threads)
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    _engine = KokoroEngine(args.voice, args.speed, args.prosody, args.pitch, args.pauses)


def _run(job: Job) -> tuple[Job, float, float]:
    t = time.time()
    return job, render(_engine, job), time.time() - t


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    p = argparse.ArgumentParser(description="Genera la narración de los nodos con Kokoro (TTS local).")
    p.add_argument("--lang", choices=LANGS, help="solo un idioma (por defecto, los dos)")
    p.add_argument("--zone", action="append", help="solo esta zona (repetible)")
    p.add_argument("--key", action="append", help="solo esta clave (repetible)")
    p.add_argument("--limit", type=int, help="como mucho N nodos")
    p.add_argument("--workers", type=int, default=1, help="procesos en paralelo (cada uno carga el modelo)")
    p.add_argument("--threads", type=int, help="hilos de torch por proceso (por defecto, núcleos / workers)")
    p.add_argument("--speed", type=float, default=0.9, help="velocidad de habla (1.0 = normal)")
    p.add_argument("--voice", help="voz de Kokoro o mezcla «ef_dora:0.6,if_sara:0.4» (por defecto es ef_dora,"
                                   " en af_heart)")
    p.add_argument("--prosody", help="voz o mezcla de la que tomar solo la entonación (por defecto, la de --voice)")
    p.add_argument("--pitch", type=float, default=0.0, help="semitonos (negativo = más grave), sin cambiar el ritmo")
    p.add_argument("--pauses", type=float, default=1.0, help="multiplica las pausas entre título, párrafos y frases")
    p.add_argument("--into-narration", action="store_true",
                   help="escribir en docs/content/narration/<idioma>/ (para convert.py) en vez de build/")
    p.add_argument("--dry-run", action="store_true", help="solo listar lo que se generaría")
    args = p.parse_args()

    out_root = convert.SRC if args.into_narration else STAGING
    jobs = plan(build_prompts.load_nodes(), out_root, convert.SRC, (args.lang,) if args.lang else LANGS,
                set(args.zone or ()), set(args.key or ()), args.limit)
    print(f"{len(jobs)} nodos por generar -> {out_root}")
    if args.dry_run or not jobs:
        for j in jobs:
            print(f"  {j.lang}/{j.key}")
        return 0

    workers = max(1, min(args.workers, len(jobs)))
    threads = args.threads or max(1, (os.cpu_count() or 1) // workers)
    start, total_audio, failures = time.time(), 0.0, []
    with ProcessPoolExecutor(workers, initializer=_init_worker, initargs=(args, threads)) as pool:
        futures = {pool.submit(_run, j): j for j in jobs}
        for n, f in enumerate(as_completed(futures), 1):
            job = futures[f]
            try:
                _, secs, took = f.result()
            except Exception as e:  # one bad node must not stop the batch
                failures.append(f"{job.lang}/{job.key}: {e}")
                print(f"  [{n}/{len(jobs)}] ERROR {job.lang}/{job.key}: {e}", flush=True)
                continue
            total_audio += secs
            elapsed = time.time() - start
            eta = elapsed / n * (len(jobs) - n)
            print(f"  [{n}/{len(jobs)}] {job.lang}/{job.key}: {secs:.0f} s de audio en {took:.0f} s"
                  f" · quedan ~{eta / 60:.0f} min", flush=True)
    elapsed = time.time() - start
    print(f"Hecho: {len(jobs) - len(failures)} audios, {total_audio / 60:.1f} min de voz en {elapsed / 60:.1f} min"
          f" ({elapsed / max(total_audio, 1):.2f}× tiempo real con {workers}×{threads} hilos).")
    if not args.into_narration:
        print("Escúchalos y, si valen, repite con --into-narration (o cópialos a docs/content/narration/<idioma>/)"
              " y lanza python tools/narration/convert.py.")
    if failures:
        print(f"Fallaron {len(failures)}:")
        for f in failures:
            print(f"  - {f}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
