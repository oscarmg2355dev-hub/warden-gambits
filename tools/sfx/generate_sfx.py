#!/usr/bin/env python3
"""Synthesizes the game's original sound effects and combat ambience.

Standard library only (wave, math, struct, random with a fixed seed), so the output is reproducible.
Writes 16-bit PCM mono WAV files into app/src/main/res/raw/ (22050 Hz, except the key sounds at 44100 Hz, which
need the extra treble for the blade's shimmer):

  sfx_action.wav        ~0.15 s  metallic swoosh (swept filtered noise + short clang/click)
  sfx_defeat_enemy.wav  ~0.5 s   low thump + descending bell
  sfx_victory.wav       ~1.5 s   major arpeggio fanfare on a soft square wave
  sfx_defeat.wav        ~1.5 s   three descending minor notes, low and sad
  amb_combat.wav        12 s     seamless loop: war drum, filtered wind, low drone
  sfx_key_sword.wav     ~0.34 s  key 1: drawn-sword "shing" (rising noise scrape + shimmering inharmonic ring)
  sfx_key_shield.wav    ~0.28 s  key 2: dry hit on a wooden shield with a metal boss (thump + plank clonk + ring)
  sfx_key_punch.wav     ~0.26 s  key 3: heavy punch (low falling thump + flesh thud + short crack + low rumble)

Usage: python tools/sfx/generate_sfx.py
"""

import math
import os
import random
import struct
import wave

SR = 22050
SEED = 1337
OUT_DIR = os.path.normpath(
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "app", "src", "main", "res", "raw")
)


# ----------------------------------------------------------------------------------------------- helpers

def silence(seconds):
    return [0.0] * int(round(seconds * SR))


def mix_into(dst, src, start_s=0.0, gain=1.0):
    """Adds src into dst starting at start_s (dst grows if needed)."""
    start = int(round(start_s * SR))
    end = start + len(src)
    if end > len(dst):
        dst.extend([0.0] * (end - len(dst)))
    for i, v in enumerate(src):
        dst[start + i] += v * gain
    return dst


def adsr(n, attack, decay, sustain, release):
    """ADSR envelope of n samples; times in seconds, release at the end of the buffer."""
    a = max(1, int(attack * SR))
    d = max(1, int(decay * SR))
    r = max(1, int(release * SR))
    env = []
    for i in range(n):
        if i < a:
            e = i / a
        elif i < a + d:
            e = 1.0 - (1.0 - sustain) * ((i - a) / d)
        else:
            e = sustain
        tail = n - i
        if tail < r:
            e *= tail / r
        env.append(e)
    return env


def exp_decay(n, tau, attack=0.003):
    a = max(1, int(attack * SR))
    return [(i / a if i < a else 1.0) * math.exp(-(i / SR) / tau) for i in range(n)]


def soft_square(freq, seconds, brightness=1.4, vibrato_hz=0.0, vibrato_depth=0.0):
    """Band-limited square (odd harmonics) with a steeper roll-off than 1/k, so it sounds soft."""
    n = int(seconds * SR)
    out = [0.0] * n
    phase = 0.0
    max_k = int((SR / 2 * 0.9) / freq)
    harmonics = [(k, 1.0 / (k ** brightness)) for k in range(1, max_k + 1, 2)][:12]
    for i in range(n):
        f = freq * (1.0 + vibrato_depth * math.sin(2 * math.pi * vibrato_hz * i / SR))
        phase += 2 * math.pi * f / SR
        out[i] = sum(a * math.sin(k * phase) for k, a in harmonics)
    return out


def one_pole_lowpass(xs, cutoff):
    alpha = 1.0 - math.exp(-2 * math.pi * cutoff / SR)
    y = 0.0
    out = []
    for x in xs:
        y += alpha * (x - y)
        out.append(y)
    return out


def reverb(xs, wet=0.25, tail_s=0.6):
    """Small Schroeder reverb (4 combs + 2 allpasses); appends a tail."""
    src = xs + [0.0] * int(tail_s * SR)
    combs = [(0.0297, 0.77), (0.0371, 0.75), (0.0411, 0.73), (0.0437, 0.71)]
    acc = [0.0] * len(src)
    for delay_s, fb in combs:
        d = int(delay_s * SR)
        buf = [0.0] * len(src)
        for i, x in enumerate(src):
            buf[i] = x + (fb * buf[i - d] if i >= d else 0.0)
            acc[i] += buf[i] * 0.25
    for delay_s, g in [(0.005, 0.7), (0.0017, 0.7)]:
        d = int(delay_s * SR)
        out = [0.0] * len(acc)
        for i, x in enumerate(acc):
            delayed = out[i - d] if i >= d else 0.0
            xd = acc[i - d] if i >= d else 0.0
            out[i] = -g * x + xd + g * delayed
        acc = out
    return [s * (1 - wet) + r * wet for s, r in zip(src, acc)]


def fade_out(xs, seconds):
    n = min(len(xs), int(seconds * SR))
    for i in range(n):
        xs[len(xs) - n + i] *= 1.0 - i / n
    return xs


def remove_dc(xs):
    mean = sum(xs) / len(xs)
    return [x - mean for x in xs]


def normalize(xs, peak_db):
    peak = max(abs(x) for x in xs) or 1.0
    target = 10 ** (peak_db / 20.0)
    return [x * target / peak for x in xs]


def write_wav(name, xs, rate=None):
    rate = rate or SR
    path = os.path.join(OUT_DIR, name)
    frames = bytearray()
    for x in xs:
        v = max(-1.0, min(1.0, x))
        frames += struct.pack("<h", int(round(v * 32767)))
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(bytes(frames))
    peak = max(abs(x) for x in xs)
    print(f"{name}: {len(xs) / rate:.3f} s, {rate} Hz, peak {20 * math.log10(peak):.1f} dBFS, {os.path.getsize(path)} bytes")


def note(midi):
    return 440.0 * 2 ** ((midi - 69) / 12.0)


# ----------------------------------------------------------------------------------------------- sounds

def sfx_action(rng):
    """Blade swoosh: noise through a resonant band-pass sweeping down, plus a short metallic clang/click."""
    n = int(0.15 * SR)
    out = [0.0] * n
    low = band = 0.0
    damping = 0.6
    env = adsr(n, 0.012, 0.06, 0.45, 0.07)
    for i in range(n):
        t = i / n
        fc = 3400.0 * (1 - t) + 500.0 * t
        f = 2 * math.sin(math.pi * fc / SR)
        x = rng.uniform(-1, 1)
        high = x - low - damping * band
        band += f * high
        low += f * band
        out[i] = band * env[i] * 0.8
    # Metallic ring: inharmonic partials with a fast decay (the blade "sings" for a moment).
    ring = [0.0] * n
    for freq, amp in [(1870.0, 0.35), (2960.0, 0.22), (4130.0, 0.14)]:
        for i in range(n):
            ring[i] += amp * math.sin(2 * math.pi * freq * i / SR) * math.exp(-(i / SR) / 0.035)
    mix_into(out, ring, 0.004, 0.6)
    # Short click at the attack.
    click = [rng.uniform(-1, 1) * (1 - i / 40) for i in range(40)]
    mix_into(out, one_pole_lowpass(click, 5000), 0.0, 0.5)
    out = out[:n]
    return normalize(fade_out(remove_dc(out), 0.01), -3.0)


def thump(seconds=0.35, f_start=120.0, f_end=42.0, tau=0.12, rng=None, noise=0.25):
    n = int(seconds * SR)
    out = [0.0] * n
    phase = 0.0
    for i in range(n):
        t = i / SR
        f = f_end + (f_start - f_end) * math.exp(-t / 0.04)
        phase += 2 * math.pi * f / SR
        out[i] = math.sin(phase) * math.exp(-t / tau)
    if rng is not None and noise > 0:
        burst = [rng.uniform(-1, 1) * math.exp(-(i / SR) / 0.015) for i in range(int(0.05 * SR))]
        mix_into(out, one_pole_lowpass(burst, 900), 0.0, noise)
    a = int(0.002 * SR)
    for i in range(a):
        out[i] *= i / a
    return out


def bell(f_start, f_end, seconds, tau=0.25):
    """Additive bell (inharmonic partials) gliding from f_start to f_end."""
    n = int(seconds * SR)
    partials = [(1.0, 1.0), (2.0, 0.5), (2.76, 0.35), (5.4, 0.18), (8.93, 0.08)]
    phases = [0.0] * len(partials)
    out = [0.0] * n
    for i in range(n):
        t = i / SR
        base = f_start * (f_end / f_start) ** (i / n)
        s = 0.0
        for j, (ratio, amp) in enumerate(partials):
            phases[j] += 2 * math.pi * base * ratio / SR
            # Higher partials die faster, like a real bell.
            s += amp * math.sin(phases[j]) * math.exp(-t / (tau / (1 + 0.6 * j)))
        out[i] = s * min(1.0, i / (0.002 * SR))
    return out


def sfx_defeat_enemy(rng):
    out = silence(0.5)
    mix_into(out, thump(0.4, 130.0, 40.0, 0.11, rng, 0.35), 0.0, 1.0)
    mix_into(out, bell(880.0, 587.0, 0.44, tau=0.18), 0.05, 0.45)
    out = reverb(out, wet=0.2, tail_s=0.0)[: int(0.5 * SR)]
    return normalize(fade_out(remove_dc(out), 0.06), -3.0)


def sfx_victory(rng):
    """C major arpeggio up to a held chord, over a timpani hit."""
    out = silence(1.5)
    arp = [60, 64, 67, 72]
    for k, m in enumerate(arp):
        tone = soft_square(note(m), 0.2)
        env = adsr(len(tone), 0.01, 0.08, 0.6, 0.06)
        mix_into(out, [s * e for s, e in zip(tone, env)], 0.12 * k, 0.45)
    chord_start = 0.48
    chord_len = 1.5 - chord_start - 0.02
    for m, g in [(72, 0.4), (76, 0.32), (79, 0.3), (60, 0.25)]:
        tone = soft_square(note(m), chord_len, brightness=1.6, vibrato_hz=5.0, vibrato_depth=0.004)
        env = adsr(len(tone), 0.02, 0.25, 0.55, 0.45)
        mix_into(out, [s * e for s, e in zip(tone, env)], chord_start, g)
    mix_into(out, thump(0.5, 110.0, 55.0, 0.18, rng, 0.2), 0.0, 0.8)
    mix_into(out, thump(0.6, 100.0, 49.0, 0.22, rng, 0.2), chord_start, 0.9)
    out = one_pole_lowpass(out, 4500)
    out = reverb(out, wet=0.3, tail_s=0.0)[: int(1.5 * SR)]
    return normalize(fade_out(remove_dc(out), 0.2), -3.0)


def sfx_defeat(rng):
    """Three descending C-minor notes (G3, Eb3, C3), low and mournful, with a sub octave."""
    out = silence(1.5)
    notes = [(55, 0.0, 0.5), (51, 0.42, 0.5), (48, 0.84, 0.64)]
    for m, start, length in notes:
        f = note(m)
        a = soft_square(f, length, brightness=1.9, vibrato_hz=4.5, vibrato_depth=0.003)
        b = soft_square(f * 1.004, length, brightness=1.9)  # slight detune: a sadder, chorused tone
        sub = [math.sin(2 * math.pi * (f / 2) * i / SR) for i in range(len(a))]
        env = adsr(len(a), 0.04, 0.15, 0.7, 0.25)
        tone = [(x + y) * 0.5 * e + s * 0.35 * e for x, y, s, e in zip(a, b, sub, env)]
        mix_into(out, tone, start, 0.6)
    mix_into(out, thump(0.6, 80.0, 38.0, 0.25, rng, 0.15), 0.84, 0.6)
    out = one_pole_lowpass(out, 1800)
    out = reverb(out, wet=0.35, tail_s=0.0)[: int(1.5 * SR)]
    return normalize(fade_out(remove_dc(out), 0.3), -3.0)


def amb_combat(rng):
    """12 s seamless loop. The wind (noise) is rendered with an extra tail that is crossfaded onto its start
    with equal-power curves; the drum hits are mixed circularly (a hit's decay past 12 s wraps to the start)
    and the drone only uses frequencies that are periodic in 12 s. The end therefore flows continuously into
    the beginning: no click and no audible fade."""
    loop_s = 12.0
    extra_s = 1.5
    n_loop = int(loop_s * SR)
    n = n_loop + int(extra_s * SR)

    # Wind: noise through a resonant low-pass whose cutoff breathes slowly (periods divide 12 s).
    wind = [0.0] * n
    low = band = 0.0
    for i in range(n):
        t = i / SR
        fc = 380.0 + 260.0 * (0.5 + 0.5 * math.sin(2 * math.pi * t / 6.0)) + 90.0 * math.sin(2 * math.pi * t / 4.0)
        f = 2 * math.sin(math.pi * fc / SR)
        high = rng.uniform(-1, 1) - low - 0.35 * band
        band += f * high
        low += f * band
        gust = 0.6 + 0.4 * math.sin(2 * math.pi * t / 12.0 + 1.0)
        wind[i] = band * 0.09 * gust
    x = int(extra_s * SR)
    out = wind[:n_loop]
    for i in range(x):
        w = i / x
        out[i] = wind[i] * math.sin(w * math.pi / 2) + wind[n_loop + i] * math.cos(w * math.pi / 2)

    # War drum at 80 BPM (0.75 s per beat, 16 beats = 12 s): BOOM . . boom-boom BOOM . .
    beat = 0.75
    pattern = [(0.0, 1.0), (1.5, 0.55), (1.75, 0.45), (2.0, 0.85)]  # positions in beats within a 4-beat bar
    for bar in range(4):
        for pos, vel in pattern:
            start = int(round((bar * 4 + pos) * beat * SR))
            for j, v in enumerate(thump(0.7, 95.0, 48.0, 0.2, rng, 0.3)):
                out[(start + j) % n_loop] += v * vel

    # Drone: A1 + E2 + A2 (multiples of 1/12 Hz, so they are periodic in 12 s), slow swell.
    for i in range(n_loop):
        t = i / SR
        swell = 0.75 + 0.25 * math.sin(2 * math.pi * t / 6.0)
        out[i] += swell * (0.22 * math.sin(2 * math.pi * 55.0 * t)
                           + 0.12 * math.sin(2 * math.pi * 82.5 * t)
                           + 0.08 * math.sin(2 * math.pi * 110.0 * t))

    return normalize(remove_dc(out), -12.0)


# ----------------------------------------------------------------------------------------------- key sounds
# Rendered at KEY_SR: render_at switches the module-level SR (read by every helper) for the duration of a call.

KEY_SR = 44100


def fade_in(xs, seconds):
    n = min(len(xs), int(seconds * SR))
    for i in range(n):
        xs[i] *= i / n
    return xs


def render_at(rate, fn, *args):
    global SR
    saved = SR
    SR = rate
    try:
        return fn(*args)
    finally:
        SR = saved


def one_pole_highpass(xs, cutoff):
    return [x - low for x, low in zip(xs, one_pole_lowpass(xs, cutoff))]


def bandpass(xs, freq, q):
    """RBJ biquad band-pass (0 dB peak gain) centred on freq with quality q."""
    w0 = 2 * math.pi * freq / SR
    alpha = math.sin(w0) / (2 * q)
    a0 = 1 + alpha
    b0, b2 = alpha / a0, -alpha / a0
    a1, a2 = -2 * math.cos(w0) / a0, (1 - alpha) / a0
    x1 = x2 = y1 = y2 = 0.0
    out = []
    for x in xs:
        y = b0 * x + b2 * x2 - a1 * y1 - a2 * y2
        x2, x1 = x1, x
        y2, y1 = y1, y
        out.append(y)
    return out


def mode(freq, tau, seconds, phase=0.0):
    """One damped sinusoidal mode: a resonance of a struck object."""
    return [math.sin(2 * math.pi * freq * i / SR + phase) * math.exp(-(i / SR) / tau)
            for i in range(int(seconds * SR))]


def sfx_key_sword(rng):
    """Key 1, drawn sword: a short scrape whose pitch RISES (noise through a resonant band-pass sweeping
    2.2 -> 7.5 kHz, the blade sliding out of the scabbard; sfx_action sweeps down instead), then the blade rings:
    pairs of slightly detuned inharmonic partials that beat against each other (the shimmer of the "shing"),
    the high ones dying first. High-passed so it stays bright and light, with a touch of room."""
    total = 0.34
    n = int(total * SR)
    out = [0.0] * n

    scrape_n = int(0.075 * SR)
    low = band = 0.0
    for i in range(scrape_n):
        t = i / scrape_n
        fc = 2200.0 * (7500.0 / 2200.0) ** t
        f = 2 * math.sin(math.pi * fc / SR)
        high = rng.uniform(-1, 1) - low - 0.35 * band
        band += f * high
        low += f * band
        out[i] = band * min(1.0, i / (0.006 * SR)) * (1.0 - 0.6 * t) * 0.45

    ring = [0.0] * n
    base = 2350.0
    for ratio, amp, tau in [(1.0, 0.55, 0.16), (1.53, 0.42, 0.13), (2.27, 0.32, 0.10),
                            (2.94, 0.22, 0.075), (3.71, 0.14, 0.055)]:
        for detune in (1.0, 1.0045):
            partial = mode(base * ratio * detune, tau, total, rng.uniform(0, 2 * math.pi))
            for i, v in enumerate(partial):
                ring[i] += v * amp * 0.5
    swell = int(0.012 * SR)
    for i in range(swell):
        ring[i] *= i / swell
    mix_into(out, ring, 0.05, 0.5)
    out = out[:n]

    tick = [rng.uniform(-1, 1) * math.exp(-(i / SR) / 0.002) for i in range(int(0.01 * SR))]
    mix_into(out, one_pole_highpass(tick, 3000), 0.0, 0.35)
    fade_in(out, 0.001)

    out = one_pole_highpass(out, 1200)
    out = reverb(out, wet=0.14, tail_s=0.0)[:n]
    return normalize(fade_out(remove_dc(out), 0.04), -3.0)


def sfx_key_shield(rng):
    """Key 2, shield block: a dry low thump (sine 210 -> 105 Hz, very short, high enough for a phone speaker), the clonk of a wooden plank (three
    damped modes plus a band-passed noise impact) and a quieter ring of the metal boss, slightly saturated so the
    hit feels solid. A tight room, no long tail."""
    total = 0.28
    n = int(total * SR)
    out = [0.0] * n

    mix_into(out, thump(0.2, 210.0, 105.0, 0.05, rng, 0.0), 0.0, 0.7)
    for freq, amp, tau in [(310.0, 0.7, 0.045), (540.0, 0.55, 0.035), (930.0, 0.35, 0.025)]:
        mix_into(out, mode(freq, tau, 0.2), 0.0, amp)
    impact = [rng.uniform(-1, 1) * math.exp(-(i / SR) / 0.006) for i in range(int(0.04 * SR))]
    mix_into(out, bandpass(impact, 1400.0, 0.9), 0.0, 1.4)
    for freq, amp, tau in [(1730.0, 0.22, 0.07), (2610.0, 0.14, 0.05), (3890.0, 0.08, 0.035)]:
        mix_into(out, mode(freq, tau, 0.25), 0.003, amp)

    out = [math.tanh(1.6 * x) for x in fade_in(out[:n], 0.001)]
    out = reverb(out, wet=0.1, tail_s=0.0)[:n]
    return normalize(fade_out(remove_dc(out), 0.05), -3.0)


def sfx_key_punch(rng):
    """Key 3, heavy punch: a grave, dry body blow. The body is a low sine whose pitch falls fast 140 -> 88 Hz
    (short decay, like a fist sinking into a torso), with a "leather/flesh" mid thud under it (a damped 240 Hz mode
    plus a noise burst band-passed at 450 Hz), a crunchy crack of a few ms at the attack (noise band-passed around
    3 kHz, tau 1.5 ms) and a brief low rumble tail (noise low-passed at 160 Hz). Saturated with tanh so the low end
    gains harmonics a phone speaker can play; almost no room. No ring and no scrape: it reads as flesh, not as the
    metal of key 1 or the wood of key 2."""
    total = 0.26
    n = int(total * SR)
    out = [0.0] * n

    mix_into(out, thump(0.24, 140.0, 88.0, 0.05, rng, 0.0), 0.0, 0.5)
    mix_into(out, mode(240.0, 0.028, 0.15), 0.0, 0.6)
    thud = [rng.uniform(-1, 1) * math.exp(-(i / SR) / 0.012) for i in range(int(0.06 * SR))]
    mix_into(out, bandpass(thud, 450.0, 1.1), 0.0, 4.0)
    crack = [rng.uniform(-1, 1) * math.exp(-(i / SR) / 0.0015) for i in range(int(0.008 * SR))]
    mix_into(out, one_pole_highpass(bandpass(crack, 3000.0, 0.8), 1500), 0.0, 2.4)
    rumble = [rng.uniform(-1, 1) * min(1.0, i / (0.01 * SR)) * math.exp(-(i / SR) / 0.05)
              for i in range(int(0.22 * SR))]
    mix_into(out, one_pole_lowpass(one_pole_lowpass(rumble, 160), 160), 0.015, 0.9)

    out = [math.tanh(2.4 * x) for x in fade_in(out[:n], 0.0008)]
    out = reverb(out, wet=0.06, tail_s=0.0)[:n]
    return normalize(fade_out(remove_dc(out), 0.05), -3.0)


def main():
    rng = random.Random(SEED)
    os.makedirs(OUT_DIR, exist_ok=True)
    write_wav("sfx_action.wav", sfx_action(rng))
    write_wav("sfx_defeat_enemy.wav", sfx_defeat_enemy(rng))
    write_wav("sfx_victory.wav", sfx_victory(rng))
    write_wav("sfx_defeat.wav", sfx_defeat(rng))
    amb = amb_combat(rng)
    write_wav("amb_combat.wav", amb)
    jump = abs(amb[-1] - amb[0])
    print(f"amb_combat loop seam: |last - first| = {jump:.5f}")
    # Own seeded generators, so adding key sounds never changes the files above.
    keys = [("sfx_key_sword.wav", sfx_key_sword), ("sfx_key_shield.wav", sfx_key_shield),
            ("sfx_key_punch.wav", sfx_key_punch)]
    for k, (name, fn) in enumerate(keys):
        write_wav(name, render_at(KEY_SR, fn, random.Random(SEED + 1 + k)), KEY_SR)


if __name__ == "__main__":
    main()
