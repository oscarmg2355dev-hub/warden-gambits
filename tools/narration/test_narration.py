"""Tests for the narration tools. Run: python -m unittest tools/narration/test_narration.py"""
import math
import re
import struct
import subprocess
import sys
import tempfile
import unittest
import wave
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import build_prompts  # noqa: E402
import convert  # noqa: E402
import tts_generate  # noqa: E402


def write_wav(path: Path, rate: int = 44100) -> None:
    """1 s silence, 2 s of 440 Hz tone, 1 s silence (16-bit mono)."""
    frames = bytearray()
    for i in range(rate * 4):
        t = i / rate
        v = int(12000 * math.sin(2 * math.pi * 440 * t)) if 1.0 <= t < 3.0 else 0
        frames += struct.pack("<h", v)
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(bytes(frames))


def probe(path: Path) -> tuple[float, str]:
    out = subprocess.run([convert.ffmpeg_exe(), "-i", str(path)], capture_output=True, text=True).stderr
    m = re.search(r"Duration: (\d+):(\d+):([\d.]+)", out)
    dur = int(m.group(1)) * 3600 + int(m.group(2)) * 60 + float(m.group(3))
    return dur, out


class PromptsTest(unittest.TestCase):
    def test_prompts_cover_every_node(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "prompts.md"
            build_prompts.build(out)
            md = out.read_text(encoding="utf-8")
        nodes = build_prompts.load_nodes()
        for lang in ("es", "en"):
            self.assertEqual(507, len(nodes[lang]))
            self.assertIn("orodruin_final", nodes[lang])
            section = md.split(f"<!-- lang:{lang} -->")[1].split("<!-- end -->")[0]
            entries = build_prompts.parse_entries(section)
            self.assertEqual(507, len(entries))
            self.assertEqual(list(nodes[lang]), list(entries))
            for key, text in nodes[lang].items():
                self.assertEqual(text, entries[key], key)


class ConvertTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        t = Path(self.tmp.name)
        self.src, self.out, self.pending = t / "src", t / "out", t / "pending.md"
        (self.src / "es").mkdir(parents=True)
        self.keys = {"es": {"bree_1_0": "x"}}

    def tearDown(self):
        self.tmp.cleanup()

    def run_convert(self, **kw):
        return convert.convert_all(self.src, self.out, self.pending, self.keys, **kw)

    def test_convert_wav_to_m4a(self):
        write_wav(self.src / "es" / "bree_1_0.wav")
        done, skipped = self.run_convert()
        self.assertEqual((1, 0), (done, skipped))
        dst = self.out / "es" / "bree_1_0.m4a"
        self.assertTrue(dst.exists())
        dur, info = probe(dst)
        self.assertAlmostEqual(2.3, dur, delta=0.2)
        self.assertIn("24000 Hz, mono", info)
        self.assertNotIn("`bree_1_0`", self.pending.read_text(encoding="utf-8"))

    def test_convert_skips_unchanged(self):
        write_wav(self.src / "es" / "bree_1_0.wav")
        self.run_convert()
        self.assertEqual((0, 1), self.run_convert())

    def test_bitrate_change_reconverts(self):
        write_wav(self.src / "es" / "bree_1_0.wav")
        self.run_convert(bitrate=32)
        self.assertEqual((0, 1), self.run_convert(bitrate=32))
        self.assertEqual((1, 0), self.run_convert(bitrate=24))
        self.assertEqual((0, 1), self.run_convert(bitrate=24))

    def test_failure_does_not_abort_batch(self):
        self.keys = {"es": {"a_bad": "x", "b_good": "y"}}
        (self.src / "es" / "a_bad.wav").write_bytes(b"not audio")
        write_wav(self.src / "es" / "b_good.wav")
        failures = []
        done, _ = self.run_convert(failures=failures)
        self.assertEqual(1, done)
        self.assertEqual(1, len(failures))
        self.assertTrue((self.out / "es" / "b_good.m4a").exists())
        self.assertFalse((self.out / "es" / "a_bad.m4a").exists())
        self.assertIn("Grabados pero sin convertir: 1", self.pending.read_text(encoding="utf-8"))

    def test_pending_lists_missing(self):
        self.run_convert()
        self.assertIn("`bree_1_0`", self.pending.read_text(encoding="utf-8"))

    def test_duplicate_suffix_preference(self):
        d = self.src / "es"
        (d / "x (1).wav").write_bytes(b"1")
        self.assertEqual(d / "x (1).wav", convert.find_audio("x", d))
        (d / "x.wav").write_bytes(b"2")
        self.assertEqual(d / "x.wav", convert.find_audio("x", d))
        self.assertIsNone(convert.find_audio("y", d))


class TtsPlanTest(unittest.TestCase):
    NODES = {"es": {"bree_1_0": "Uno.\nCuerpo.", "bree_2_0": "Dos.\nB.", "chet_1_0": "Tres.\nC."},
             "en": {"bree_1_0": "One.\nBody."}}

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        t = Path(self.tmp.name)
        self.out, self.rec = t / "out", t / "rec"

    def tearDown(self):
        self.tmp.cleanup()

    def keys(self, **kw):
        return [(j.lang, j.key) for j in tts_generate.plan(self.NODES, self.out, self.rec, **kw)]

    def test_plans_every_node_in_order(self):
        self.assertEqual([("es", "bree_1_0"), ("es", "bree_2_0"), ("es", "chet_1_0"), ("en", "bree_1_0")],
                         self.keys())
        job = tts_generate.plan(self.NODES, self.out, self.rec)[0]
        self.assertEqual(self.out / "es" / "bree_1_0.wav", job.dst)
        self.assertEqual("Uno.\nCuerpo.", job.text)

    def test_filters(self):
        self.assertEqual([("es", "chet_1_0")], self.keys(langs=("es",), zones={"chet"}))
        self.assertEqual([("es", "bree_2_0")], self.keys(langs=("es",), keys={"bree_2_0"}))
        self.assertEqual(2, len(self.keys(limit=2)))

    def test_skips_recorded_and_generated(self):
        (self.rec / "es").mkdir(parents=True)
        (self.rec / "es" / "bree_1_0 (1).mp3").write_bytes(b"x")
        (self.out / "en").mkdir(parents=True)
        (self.out / "en" / "bree_1_0.wav").write_bytes(b"x")
        self.assertEqual([("es", "bree_2_0"), ("es", "chet_1_0")], self.keys())

    def test_paragraphs(self):
        self.assertEqual(["T.", "a b", "c"], tts_generate.paragraphs("T.\n a b \n\nc\n"))

    def test_sentences(self):
        self.assertEqual(["Uno.", "¿Dos?", "Tres… cuatro"], tts_generate.sentences(" Uno. ¿Dos?  Tres… cuatro"))

    def test_parse_voice(self):
        self.assertEqual([("ef_dora", 1.0)], tts_generate.parse_voice("ef_dora"))
        self.assertEqual([("ef_dora", 0.75), ("if_sara", 0.25)], tts_generate.parse_voice("ef_dora:3, if_sara:1"))

    def test_kokoro_lang(self):
        self.assertEqual("e", tts_generate.kokoro_lang("es", "ef_dora"))
        self.assertEqual("a", tts_generate.kokoro_lang("en", tts_generate.KOKORO_VOICE["en"]))
        self.assertEqual("b", tts_generate.kokoro_lang("en", "bf_emma:0.5,af_heart:0.5"))

    def test_shift_pitch_keeps_length_and_lowers_tone(self):
        try:
            import numpy as np
            import imageio_ffmpeg  # noqa: F401
        except ImportError:
            self.skipTest("needs numpy and imageio-ffmpeg")
        tone = np.sin(2 * np.pi * 220 * np.arange(24000) / 24000).astype("float32")
        low = tts_generate.shift_pitch(tone, 24000, -2)
        self.assertEqual(len(tone), len(low))
        peak = np.argmax(np.abs(np.fft.rfft(low[2000:22000]))) * 24000 / 20000
        self.assertAlmostEqual(220 * 2 ** (-2 / 12), peak, delta=5)

    def test_render_joins_paragraphs_with_pauses(self):
        try:
            import numpy as np
            import soundfile as sf
        except ImportError:
            self.skipTest("needs numpy and soundfile")

        class Fake:
            rate = 1000

            def synth(self, text, lang):
                return np.ones(100, "float32")

        job = tts_generate.Job("es", "bree_1_0", "T.\nA.\nB.", self.out / "es" / "bree_1_0.wav")
        secs = tts_generate.render(Fake(), job)
        expected = 0.3 + tts_generate.TITLE_PAUSE + tts_generate.PARAGRAPH_PAUSE
        self.assertAlmostEqual(expected, secs, places=3)
        self.assertAlmostEqual(expected, sf.info(job.dst).duration, places=3)
        self.assertEqual([job.dst], list((self.out / "es").iterdir()))


if __name__ == "__main__":
    unittest.main()
