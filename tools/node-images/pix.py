"""Tiny procedural pixel-art kit matching the node-image style:
256x192 native, 32-colour palette, ordered dithering, 4x nearest upscale."""
import math
import random

import numpy as np
from PIL import Image, ImageDraw

W, H = 256, 192


def c(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def dark(a, f=0.7):
    return tuple(int(v * f) for v in a)


BAYER = (np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16.0) - 0.47


class Scene:
    def __init__(self, seed):
        self.img = Image.new('RGB', (W, H))
        self.d = ImageDraw.Draw(self.img)
        self.r = random.Random(seed)
        self.mask = np.zeros((H, W), bool)

    # ---------- primitives ----------
    def vgrad(self, y0, y1, c0, c1, x0=0, x1=W):
        for y in range(y0, y1):
            t = (y - y0) / max(1, y1 - y0 - 1)
            self.d.line([(x0, y), (x1 - 1, y)], fill=mix(c0, c1, t))

    def poly(self, pts, col):
        self.d.polygon([(int(x), int(y)) for x, y in pts], fill=col)

    def rect(self, x0, y0, x1, y1, col):
        self.d.rectangle([int(min(x0, x1)), int(min(y0, y1)), int(max(x0, x1)), int(max(y0, y1))], fill=col)

    def ell(self, x0, y0, x1, y1, col):
        self.d.ellipse([int(min(x0, x1)), int(min(y0, y1)), int(max(x0, x1)), int(max(y0, y1))], fill=col)

    def line(self, pts, col, w=1):
        self.d.line([(int(x), int(y)) for x, y in pts], fill=col, width=w)

    def px(self, x, y, col):
        if 0 <= x < W and 0 <= y < H:
            self.img.putpixel((int(x), int(y)), col)

    def _arr(self):
        return np.asarray(self.img).astype(np.float32)

    def _set(self, a):
        self.img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8))
        self.d = ImageDraw.Draw(self.img)

    def glow(self, cx, cy, r, col, k=1.0, rx=None):
        a = self._arr()
        yy, xx = np.mgrid[0:H, 0:W]
        rx = rx or r
        dd = np.sqrt(((xx - cx) / rx) ** 2 + ((yy - cy) / r) ** 2)
        f = np.clip(1 - dd, 0, 1) ** 2 * k
        a += f[..., None] * np.array(col, np.float32)[None, None, :]
        self._set(a)

    def shade(self, k, y0=0, y1=H):
        """Multiply a band by k (darkening/vignette)."""
        a = self._arr()
        a[y0:y1] *= k
        self._set(a)

    def vignette(self, k=0.55):
        a = self._arr()
        yy, xx = np.mgrid[0:H, 0:W]
        dd = np.sqrt(((xx - W / 2) / (W / 2)) ** 2 + ((yy - H / 2) / (H / 2)) ** 2)
        f = 1 - np.clip(dd - 0.55, 0, 1) * k
        self._set(a * f[..., None])

    def tint(self, col, k):
        a = self._arr()
        a = a * (1 - k) + np.array(col, np.float32) * k
        self._set(a)

    def speckle(self, y0, y1, cols, n, x0=0, x1=W):
        for _ in range(n):
            self.px(self.r.randint(x0, x1 - 1), self.r.randint(y0, y1 - 1), self.r.choice(cols))

    def tufts(self, y0, y1, cols, n, x0=0, x1=W):
        for _ in range(n):
            x, y = self.r.randint(x0, x1 - 1), self.r.randint(y0, y1 - 1)
            col = self.r.choice(cols)
            self.px(x, y, col)
            self.px(x - 1, y - 1, col)
            self.px(x + 1, y - 1, col)

    def stars(self, y1, n, col=(230, 230, 250)):
        for _ in range(n):
            x, y = self.r.randint(0, W - 1), self.r.randint(0, y1)
            self.px(x, y, col)
            if self.r.random() < 0.12:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    self.px(x + dx, y + dy, dark(col, 0.75))

    def snowfall(self, n, col=(235, 240, 250), y1=H):
        for _ in range(n):
            self.px(self.r.randint(0, W - 1), self.r.randint(0, y1 - 1), col)

    # ---------- landscape ----------
    def ridge(self, base, amp, col, freq=0.04, phase=0.0, y_bottom=H, jag=0.0):
        pts = [(0, y_bottom)]
        for x in range(0, W + 4, 4):
            y = base - amp * (0.6 * math.sin(x * freq + phase) + 0.4 * math.sin(x * freq * 2.3 + phase * 1.7))
            y += self.r.uniform(-jag, jag)
            pts.append((x, y))
        pts.append((W, y_bottom))
        self.poly(pts, col)

    def mountain(self, px_, py, base, half, col, snow=None, snow_frac=0.3, shade=0.72):
        rr = self.r
        left = [(px_ - half, base)]
        steps = 7
        for i in range(1, steps):
            t = i / steps
            left.append((px_ - half * (1 - t) + rr.uniform(-3, 3), base - (base - py) * t + rr.uniform(-3, 3)))
        right = []
        for i in range(steps - 1, 0, -1):
            t = i / steps
            right.append((px_ + half * (1 - t) + rr.uniform(-3, 3), base - (base - py) * t + rr.uniform(-3, 3)))
        self.poly(left + [(px_, py)] + right + [(px_ + half, base)], col)
        self.poly([(px_, py)] + right + [(px_ + half, base), (px_ + half * 0.15, base)], dark(col, shade))
        if snow:
            sy = py + (base - py) * snow_frac
            sh = half * snow_frac
            pts = [(px_, py), (px_ + sh, sy)]
            for i in range(5, 0, -1):
                x = px_ - sh + 2 * sh * i / 6
                pts.append((x, sy + rr.uniform(-1, 5)))
            pts.append((px_ - sh, sy))
            self.poly(pts, snow)
            self.poly([(px_, py), (px_ + sh, sy), (px_ + sh * 0.3, sy + 3)], dark(snow, 0.82))

    def cloud(self, x, y, w, col):
        for i in range(5):
            cx = x + w * i / 4
            r = w / 5 * (0.8 + 0.4 * math.sin(i * 1.7))
            self.ell(cx - r, y - r * 0.6, cx + r, y + r * 0.5, col)
        self.rect(x - w / 6, y, x + w + w / 6, y + w / 10, col)

    def bare_tree(self, x, y, h, col, w=2):
        self.line([(x, y), (x, y - h)], col, w)

        def br(x0, y0, ang, ln, depth):
            if depth == 0 or ln < 2:
                return
            x1 = x0 + math.cos(ang) * ln
            y1 = y0 - math.sin(ang) * ln
            self.line([(x0, y0), (x1, y1)], col, 1)
            br(x1, y1, ang + self.r.uniform(0.2, 0.6), ln * 0.7, depth - 1)
            br(x1, y1, ang - self.r.uniform(0.2, 0.6), ln * 0.7, depth - 1)

        for k in range(3):
            yy = y - h * (0.5 + 0.2 * k)
            br(x, yy, math.pi / 2 + self.r.uniform(-0.9, 0.9), h * 0.35, 3)

    def willow(self, x, y, h, col):
        self.line([(x, y), (x + 1, y - h)], col, 2)
        for i in range(14):
            sx = x + self.r.uniform(-h * 0.45, h * 0.45)
            sy = y - h + self.r.uniform(-3, 4)
            self.line([(x, y - h), (sx, sy - 2)], col)
            self.line([(sx, sy - 2), (sx + self.r.uniform(-2, 2), sy + h * self.r.uniform(0.3, 0.7))], col)

    def leafy_tree(self, x, y, h, trunk, leaf, light=None, r=None):
        r = r or h * 0.35
        self.rect(x - 1, y - h * 0.45, x + 1, y, trunk)
        for i in range(7):
            cx = x + self.r.uniform(-r * 0.7, r * 0.7)
            cy = y - h * 0.6 + self.r.uniform(-r * 0.6, r * 0.5)
            rr = r * self.r.uniform(0.5, 0.8)
            self.ell(cx - rr, cy - rr, cx + rr, cy + rr, leaf)
        if light:
            for i in range(3):
                cx = x - r * 0.3 + self.r.uniform(-r * 0.3, r * 0.2)
                cy = y - h * 0.7 + self.r.uniform(-r * 0.3, r * 0.2)
                rr = r * 0.35
                self.ell(cx - rr, cy - rr, cx + rr, cy + rr, light)

    def holly(self, x, y, h, leaf, berries=(170, 40, 40), snow=None):
        self.rect(x - 1, y - h * 0.3, x + 1, y, (60, 45, 35))
        for i in range(6):
            t = i / 6
            w = h * 0.35 * (1 - t * 0.7)
            cy = y - h * 0.25 - h * 0.7 * t
            self.ell(x - w, cy - h * 0.14, x + w, cy + h * 0.1, leaf if i % 2 == 0 else dark(leaf, 0.85))
            if snow and i > 1:
                self.line([(x - w * 0.6, cy - h * 0.12), (x + w * 0.3, cy - h * 0.13)], snow)
        for _ in range(5):
            self.px(x + self.r.uniform(-h * 0.25, h * 0.25), y - h * self.r.uniform(0.3, 0.8), berries)

    def pine(self, x, y, h, col):
        self.rect(x - 1, y - h * 0.2, x, y, dark(col, 0.6))
        for i in range(4):
            t = i / 4
            w = h * 0.3 * (1 - t * 0.6)
            yb = y - h * 0.15 - h * 0.22 * i
            self.poly([(x - w, yb), (x + w, yb), (x, yb - h * 0.35)], col if i % 2 else dark(col, 0.85))

    def forest_wall(self, y, h, col, density=26):
        for i in range(density):
            x = self.r.randint(-5, W + 5)
            hh = h * self.r.uniform(0.7, 1.1)
            self.rect(x - 2, y - hh, x + 2, y, dark(col, 0.8))
            self.ell(x - 9, y - hh - 8, x + 9, y - hh + 10, col)
        self.rect(0, y - h * 0.6, W, y, dark(col, 0.85))

    def bush(self, x, y, w, col, light=None):
        for i in range(4):
            cx = x + self.r.uniform(-w / 2, w / 2)
            rr = w * self.r.uniform(0.25, 0.4)
            self.ell(cx - rr, y - rr * 1.6, cx + rr, y, col)
        if light:
            self.ell(x - w * 0.3, y - w * 0.45, x, y - w * 0.25, light)

    def brambles(self, x, y, w, col, thorn):
        for _ in range(int(w * 1.4)):
            x0 = x + self.r.uniform(-w / 2, w / 2)
            y0 = y - self.r.uniform(0, w * 0.4)
            pts = [(x0, y0)]
            for _ in range(3):
                pts.append((pts[-1][0] + self.r.uniform(-5, 5), pts[-1][1] + self.r.uniform(-4, 2)))
            self.line(pts, col)
            self.px(pts[-1][0], pts[-1][1], thorn)

    def rock(self, x, y, w, h, col):
        self.poly([(x - w / 2, y), (x - w / 2 + 2, y - h * 0.7), (x - w / 6, y - h), (x + w / 3, y - h * 0.9),
                   (x + w / 2, y - h * 0.3), (x + w / 2, y)], col)
        self.poly([(x + w / 3, y - h * 0.9), (x + w / 2, y - h * 0.3), (x + w / 2, y), (x + w / 8, y)], dark(col, 0.75))

    def bricks(self, x0, y0, x1, y1, col, mortar, bw=10, bh=5):
        self.rect(x0, y0, x1, y1, col)
        row = 0
        y = y0
        while y < y1:
            self.line([(x0, y), (x1, y)], mortar)
            off = (bw // 2) * (row % 2)
            x = x0 + off
            while x < x1:
                self.line([(x, y), (x, min(y + bh, y1))], mortar)
                x += bw
            y += bh
            row += 1

    # ---------- sprites (outlined layers) ----------
    def begin(self):
        self._base = self.img
        self.img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.img)

    def end(self, outline=(22, 18, 22)):
        spr = self.img
        a = np.asarray(spr)[..., 3] > 0
        g = a.copy()
        g[1:] |= a[:-1]
        g[:-1] |= a[1:]
        g[:, 1:] |= a[:, :-1]
        g[:, :-1] |= a[:, 1:]
        base = np.asarray(self._base).copy()
        if outline is not None:
            base[g & ~a] = outline
        sp = np.asarray(spr)
        base[a] = sp[a][:, :3]
        self.mask |= g
        self.img = Image.fromarray(base)
        self.d = ImageDraw.Draw(self.img)

    # ---------- characters ----------
    def figure(self, *a, outline=(22, 18, 22), **kw):
        self.begin()
        r = self._figure(*a, **kw)
        self.end(outline)
        return r

    def _figure(self, x, y, h=28, f=1, cloak=(110, 70, 55), hood=None, skin=(218, 160, 120), hair=None,
               legs=(55, 45, 50), weapon=None, wcol=(200, 200, 205), pose='stand', beard=None, eyepatch=False,
               belt=None, arm_up=False, wide=1.0):
        u = h / 28
        hx, hy = x + f * 0.5 * u, y - h + 4 * u
        # legs
        if pose == 'run':
            self.line([(x - u, y - 7 * u), (x - f * 5 * u, y)], legs, max(1, int(2.5 * u)))
            self.line([(x + u, y - 7 * u), (x + f * 5 * u, y - 1)], legs, max(1, int(2.5 * u)))
        elif pose == 'leap':
            self.line([(x, y - 9 * u), (x - f * 7 * u, y - 4 * u)], legs, max(1, int(2.5 * u)))
            self.line([(x, y - 9 * u), (x + f * 6 * u, y - 2 * u)], legs, max(1, int(2.5 * u)))
        elif pose == 'crouch':
            self.ell(x - 7 * u, y - 6 * u, x + 7 * u, y, legs)
        elif pose == 'sit':
            self.rect(x - 6 * u, y - 4 * u, x + 6 * u, y, legs)
        else:
            self.rect(x - 3 * u, y - 7 * u, x - 0.8 * u, y, legs)
            self.rect(x + 0.8 * u, y - 7 * u, x + 3 * u, y, legs)
            self.rect(x - 3.5 * u, y - 1.5 * u, x - 0.5 * u, y, dark(legs, 0.6))
            self.rect(x + 0.5 * u, y - 1.5 * u, x + 3.5 * u, y, dark(legs, 0.6))
        top = y - h + 8 * u
        hem = y - 6 * u if pose not in ('crouch', 'sit') else y - 2 * u
        sw, hw = 5 * u * wide, 7.5 * u * wide
        self.poly([(x - sw, top), (x + sw, top), (x + hw, hem), (x - hw, hem)], cloak)
        self.poly([(x - f * sw, top), (x - f * 1 * u, top), (x - f * 2 * u, hem), (x - f * hw, hem)], dark(cloak, 0.78))
        if belt:
            self.rect(x - sw - u, top + 6 * u, x + sw + u, top + 7 * u, belt)
        # back arm
        hand = (x + f * 8 * u, top + (-1 * u if arm_up else 5 * u))
        self.line([(x + f * 2 * u, top + u), hand], dark(cloak, 0.85), max(1, int(2.2 * u)))
        # head
        if hood:
            self.ell(hx - 5 * u, hy - 5.5 * u, hx + 5 * u, hy + 4.5 * u, hood)
            self.ell(hx - 3 * u + f * 1.6 * u, hy - 3 * u, hx + 3 * u + f * 1.6 * u, hy + 3.5 * u, skin)
        else:
            self.ell(hx - 3.8 * u, hy - 4 * u, hx + 3.8 * u, hy + 3.6 * u, skin)
            if hair:
                self.ell(hx - 4 * u, hy - 4.5 * u, hx + 4 * u, hy - 0.5 * u, hair)
                self.rect(hx - f * 4 * u, hy - 2 * u, hx - f * 1 * u, hy + 3 * u, hair)
        ex = hx + f * 2.5 * u
        self.px(ex, hy - 0.5 * u, (25, 20, 25))
        if eyepatch:
            self.rect(ex - u, hy - 1.5 * u, ex + u, hy + 0.5 * u, (20, 15, 15))
            self.line([(hx - 4 * u, hy - 3 * u), (ex, hy - u)], (20, 15, 15))
        if beard:
            self.poly([(hx - 2 * u, hy + u), (hx + f * 3.5 * u, hy + u), (hx + f * 1.5 * u, hy + 7 * u), (hx - 1 * u, hy + 5 * u)], beard)
        # weapon
        if weapon == 'sword':
            tip = (hand[0] + f * 9 * u, hand[1] - (12 * u if arm_up else 6 * u))
            self.line([hand, tip], wcol, max(1, int(1.2 * u)))
            self.line([(hand[0] - 1.5 * u, hand[1] + f * 1.5 * u - u), (hand[0] + 1.5 * u, hand[1] - f * 1.5 * u - u)], (120, 100, 60))
        elif weapon == 'hilt':
            self.line([(x - f * 2 * u, top + 9 * u), (x - f * 9 * u, top + 15 * u)], (70, 60, 55), max(1, int(1.4 * u)))
            self.line([(x - f * 1 * u, top + 6 * u), (x - f * 3 * u, top + 11 * u)], (150, 130, 80), max(1, int(1.2 * u)))
        elif weapon == 'axe':
            tip = (hand[0] + f * 3 * u, hand[1] - 12 * u)
            self.line([hand, tip], (95, 70, 45), max(1, int(1.5 * u)))
            self.poly([tip, (tip[0] + f * 5 * u, tip[1] - 2 * u), (tip[0] + f * 6 * u, tip[1] + 4 * u), (tip[0], tip[1] + 3 * u)], wcol)
        elif weapon == 'spear':
            a = (hand[0] - f * 6 * u, hand[1] + 8 * u)
            tip = (hand[0] + f * 10 * u, hand[1] - 14 * u)
            self.line([a, tip], (110, 85, 55), max(1, int(1.2 * u)))
            self.poly([tip, (tip[0] + f * 3 * u, tip[1] - 4 * u), (tip[0] + f * 0.5 * u, tip[1] - 1 * u)], wcol)
        elif weapon == 'torch':
            tip = (hand[0] + f * 1 * u, hand[1] - 7 * u)
            self.line([hand, tip], (100, 70, 40), max(1, int(1.4 * u)))
            self.ell(tip[0] - 2 * u, tip[1] - 4 * u, tip[0] + 2 * u, tip[1] + u, (255, 170, 60))
            self.ell(tip[0] - u, tip[1] - 2.5 * u, tip[0] + u, tip[1], (255, 235, 150))
        return hand

    def hero(self, x, y, h=40, f=1, weapon='sword', **kw):
        """The recurring young traveler: grey-green cloak, rust-red hood."""
        kw.setdefault('cloak', (92, 98, 80))
        kw.setdefault('hood', (168, 58, 42))
        kw.setdefault('legs', (70, 55, 45))
        return self.figure(x, y, h, f, weapon=weapon, **kw)

    def wolf(self, *a, outline=(22, 18, 22), **kw):
        self.begin()
        r = self._wolf(*a, **kw)
        self.end(outline)
        return r

    def _wolf(self, x, y, f=1, s=1.0, col=(120, 120, 125), leap=False):
        u = s
        dy = -10 * u if leap else 0
        body = [(x - 11 * u, y - 10 * u + dy), (x + 8 * u, y - 12 * u + dy), (x + 10 * u, y - 5 * u + dy), (x - 10 * u, y - 4 * u + dy)]
        if f < 0:
            body = [(2 * x - px_, py) for px_, py in body]
        self.poly(body, col)
        hx = x + f * 11 * u
        hy = y - 12 * u + dy
        self.poly([(hx - f * 3 * u, hy - 3 * u), (hx + f * 4 * u, hy - 1 * u), (hx + f * 8 * u, hy + 2 * u), (hx + f * 3 * u, hy + 4 * u), (hx - f * 3 * u, hy + 4 * u)], col)
        self.poly([(hx - f * 2 * u, hy - 3 * u), (hx - f * 1 * u, hy - 7 * u), (hx + f * 1 * u, hy - 2 * u)], dark(col, 0.8))
        self.px(hx + f * 3 * u, hy, (230, 200, 60))
        self.line([(hx + f * 4 * u, hy + 3 * u), (hx + f * 7 * u, hy + 2.5 * u)], (240, 235, 225))
        lw = max(1, int(2 * u))
        if leap:
            self.line([(x + f * 7 * u, y - 6 * u + dy), (x + f * 13 * u, y - 2 * u + dy)], dark(col, 0.85), lw)
            self.line([(x - f * 8 * u, y - 5 * u + dy), (x - f * 15 * u, y + 2 * u + dy)], dark(col, 0.85), lw)
        else:
            for lx in (-8, -5, 5, 8):
                self.line([(x + f * lx * u, y - 6 * u), (x + f * lx * u, y)], dark(col, 0.85), lw)
        self.line([(x - f * 10 * u, y - 9 * u + dy), (x - f * 16 * u, y - 4 * u + dy)], col, max(1, int(3 * u)))
        self.line([(x - f * 9 * u, y - 4 * u + dy), (x + f * 9 * u, y - 5 * u + dy)], dark(col, 0.75))

    def crow(self, *a, outline=(22, 18, 22), **kw):
        self.begin()
        r = self._crow(*a, **kw)
        self.end(outline)
        return r

    def _crow(self, x, y, s=1.0, col=(20, 18, 28), dive=False):
        u = s
        if dive:
            self.poly([(x, y + 6 * u), (x - 9 * u, y - 6 * u), (x - 2 * u, y - 2 * u), (x, y - 8 * u), (x + 2 * u, y - 2 * u), (x + 9 * u, y - 6 * u)], col)
            self.px(x, y + 5 * u, (90, 90, 100))
        else:
            self.poly([(x - 8 * u, y - 2 * u), (x - 2 * u, y), (x, y + 2 * u), (x + 2 * u, y), (x + 8 * u, y - 2 * u), (x + 3 * u, y + 2 * u), (x - 3 * u, y + 2 * u)], col)

    def fire(self, x, y, s=1.0, glow=True):
        u = s
        if glow:
            self.glow(x, y - 3 * u, 40 * u, (255, 140, 50), 0.55)
        self.line([(x - 5 * u, y), (x + 5 * u, y - 2 * u)], (80, 50, 30), max(1, int(2 * u)))
        self.line([(x - 5 * u, y - 2 * u), (x + 5 * u, y)], (70, 45, 30), max(1, int(2 * u)))
        self.poly([(x - 4 * u, y - u), (x - 1 * u, y - 11 * u), (x + 1 * u, y - 6 * u), (x + 3 * u, y - 9 * u), (x + 4 * u, y - u)], (240, 110, 40))
        self.poly([(x - 2 * u, y - u), (x, y - 7 * u), (x + 2 * u, y - u)], (255, 220, 120))


def finish(scene, path, colors=32, amp=30):
    a = np.asarray(scene.img).astype(np.float32)
    lum = a.mean(axis=2, keepdims=True)
    a = (lum + (a - lum) * 1.18 - 128) * 1.08 + 128
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8))
    spr = a[scene.mask]
    src = img
    if len(spr):
        rep = np.tile(spr, (max(1, (W * H // 3) // len(spr)), 1))
        n = (len(rep) + W - 1) // W
        rep = np.resize(rep, (n * W, 3)).reshape(n, W, 3)
        key = np.array([(168, 58, 42), (218, 160, 120), (92, 98, 80)], np.float32)
        key = (key.mean(axis=1, keepdims=True) + (key - key.mean(axis=1, keepdims=True)) * 1.18 - 128) * 1.08 + 128
        sw = np.repeat(key, W * 12, axis=0).reshape(-1, W, 3)
        src = Image.fromarray(np.clip(np.concatenate([a, rep, sw]), 0, 255).astype(np.uint8))
    pal = src.quantize(colors, method=Image.Quantize.MEDIANCUT)
    yy, xx = np.mgrid[0:H, 0:W]
    a = a + BAYER[yy % 4, xx % 4][..., None] * amp
    noisy = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8))
    q = noisy.quantize(palette=pal, dither=Image.Dither.NONE).convert('RGB')
    big = q.resize((W * 4, H * 4), Image.NEAREST)
    big.save(path, 'WEBP', lossless=True, method=6)
    return big
