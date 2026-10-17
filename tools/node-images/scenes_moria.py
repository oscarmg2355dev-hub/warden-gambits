import math

from pix import Scene, c, dark, H, W
from scenes import scene, ORC, GOBLIN

STONE = c('3a3c48')
MORTAR = c('2a2c36')
TORCH = (255, 150, 60)


def hall(s, floor_y=130, wall='3a3c48', floor='2e2c30'):
    s.bricks(0, 0, W, floor_y, c(wall), MORTAR, 18, 9)
    s.vgrad(floor_y, H, c(floor), dark(c(floor), 0.6))
    for i in range(7):
        y = floor_y + (H - floor_y) * (i / 7) ** 1.6
        s.line([(0, y), (W, y)], dark(c(floor), 0.75))
    for i in range(-6, 7):
        s.line([(128 + i * 12, floor_y), (128 + i * 60, H)], dark(c(floor), 0.75))


def pillar(s, x, y0, y1, w, col):
    s.rect(x - w / 2, y0, x + w / 2, y1, col)
    s.rect(x - w / 2, y0, x - w / 2 + w / 4, y1, dark(col, 1.25) if max(col) < 200 else col)
    s.rect(x - w / 2 - 2, y1 - 4, x + w / 2 + 2, y1, dark(col, 0.8))


@scene('moria_5_0')
def moria_5_0(s: Scene):
    hall(s, 120, '34363f', '2c2a2e')
    s.shade(0.7)
    # high opening and shaft of grey daylight
    s.rect(112, 0, 144, 10, c('c8ccd4'))
    s.poly([(112, 10), (144, 10), (176, 170), (88, 170)], c('6a6c74'))
    s.glow(132, 150, 50, (130, 130, 140), 0.7, rx=60)
    for _ in range(60):
        s.px(s.r.randint(100, 160), s.r.randint(10, 160), c('b0b4bc'))
    # pale runed slab
    s.poly([(104, 156), (156, 156), (166, 170), (94, 170)], c('d4d2cc'))
    s.rect(94, 170, 166, 176, c('a8a6a0'))
    for x in range(108, 154, 8):
        s.line([(x, 160), (x + 3, 164), (x, 167)], c('7a7874'))
    # bones, axes, chests, slashed book
    for x, y in ((40, 172), (70, 182), (190, 178), (214, 168), (60, 160), (230, 184)):
        s.line([(x, y), (x + 8, y - 2)], c('c8c0aa'), 2)
        s.ell(x + 7, y - 4, x + 11, y, c('c8c0aa'))
    s.ell(196, 160, 204, 167, c('c8c0aa'))
    s.px(198, 163, c('1a1a1a'))
    s.px(201, 163, c('1a1a1a'))
    s.line([(30, 150), (46, 166)], c('5a4030'), 2)
    s.poly([(30, 150), (24, 148), (24, 156), (30, 156)], c('6a6a72'))
    s.rect(212, 140, 238, 156, c('5a4028'))
    s.poly([(212, 140), (238, 140), (234, 132), (216, 132)], c('4a3420'))
    s.line([(214, 148), (236, 146)], c('8a8070'))
    s.poly([(170, 182), (186, 178), (188, 186), (172, 190)], c('7a5a40'))
    s.line([(176, 180), (184, 188)], c('d8d0b8'))
    s.hero(70, 172, 42, 1, weapon=None)
    s.vignette(0.65)


@scene('moria_5_1')
def moria_5_1(s: Scene):
    hall(s, 120)
    s.shade(0.6)
    # distant galleries with flaring torches
    for x, y in ((30, 40), (80, 30), (180, 34), (226, 46), (130, 22)):
        s.rect(x - 8, y - 4, x + 8, y + 14, c('121218'))
        s.glow(x, y + 4, 14, TORCH, 0.9)
        s.px(x, y + 4, c('ffe0a0'))
    # drum beats: shock rings
    for r in (30, 44, 58):
        s.d.arc([128 - r * 2, 60 - r, 128 + r * 2, 60 + r], 200, 340, fill=c('4a4c5a'))
    s.figure(196, 182, 52, -1, weapon='sword', wcol=c('8a8c92'), arm_up=True, wide=1.3, **ORC)
    s.d.ellipse([170, 148, 177, 154], fill=c('1a0a0a'))
    s.figure(150, 184, 30, -1, weapon='spear', wcol=c('8a8c92'), **GOBLIN)
    s.glow(80, 142, 46, TORCH, 0.6)
    s.hero(70, 184, 44, 1, weapon='torch', pose='run')
    s.begin()
    s.line([(54, 158), (40, 148)], c('c8c8d0'), 1)
    s.end()
    s.vignette(0.6)


@scene('moria_5_2')
def moria_5_2(s: Scene):
    hall(s, 110)
    s.shade(0.55)
    # column of orcs and goblins with torches
    for i in range(9):
        x = -10 + i * 30
        h = 26 + (i % 3) * 3
        kw = ORC if i % 2 else GOBLIN
        s.figure(x + 60, 132 + (i % 2) * 2, h, 1, weapon='torch' if i % 3 == 0 else 'spear', wcol=c('7a7c84'), pose='run', **kw)
    for i in range(9):
        if i % 3 == 0:
            s.glow(-10 + i * 30 + 68, 104, 26, TORCH, 0.7)
    # a goblin sniffing at the door
    s.figure(150, 178, 40, -1, **GOBLIN)
    # door crack framing: hero hides in darkness behind
    s.poly([(0, 0), (112, 0), (104, 192), (0, 192)], c('100c0e'))
    s.poly([(256, 0), (176, 0), (184, 192), (256, 192)], c('100c0e'))
    s.line([(112, 0), (104, 192)], c('3a2c24'), 2)
    s.line([(176, 0), (184, 192)], c('3a2c24'), 2)
    s.hero(50, 190, 54, 1, weapon=None, outline=(40, 34, 40))
    s.shade(0.45, 0, H) if False else None
    s.vignette(0.5)


@scene('moria_5_3')
def moria_5_3(s: Scene):
    s.rect(0, 0, W, H, c('16161c'))
    # long straight corridor in perspective
    vx, vy = 128, 86
    s.poly([(0, 0), (W, 0), (vx + 14, vy - 10), (vx - 14, vy - 10)], c('22222a'))
    s.poly([(0, H), (W, H), (vx + 14, vy + 10), (vx - 14, vy + 10)], c('2e2c30'))
    s.poly([(0, 0), (vx - 14, vy - 10), (vx - 14, vy + 10), (0, H)], c('2a2a34'))
    s.poly([(W, 0), (vx + 14, vy - 10), (vx + 14, vy + 10), (W, H)], c('26262e'))
    s.rect(vx - 14, vy - 10, vx + 14, vy + 10, c('08080a'))
    # statues of dwarf kings in niches
    for side in (-1, 1):
        for k, t in enumerate((0.12, 0.42, 0.64, 0.8)):
            x = vx + side * (128 - 114 * t) * 0.9
            yb = H - (H - vy - 10) * t
            h = 80 * (1 - t) + 8
            if side == 1 and k == 0:
                continue
            s.rect(x - h * 0.28, yb - h * 1.05, x + h * 0.28, yb, c('1a1a20'))
            s.figure(x, yb, h, -side, beard=c('2c2a30'), cloak=c('26242c'), legs=c('1e1c22'),
                     skin=c('2c2a30'), hair=c('26242c'), weapon='axe', wcol=c('2c2a30'), wide=1.2, outline=(14, 14, 18))
    # the troll stepping out, shaking off dust
    s.glow(206, 110, 60, (60, 70, 50), 0.5)
    s.figure(206, 188, 92, -1, wide=1.6, cloak=c('4a5040'), legs=c('3a4032'), skin=c('6e7a5a'), outline=(10, 10, 12))
    s.px(193, 114, c('f0d040'))
    s.px(197, 114, c('f0d040'))
    s.begin()
    s.line([(170, 140), (150, 118)], c('4a3a2a'), 4)
    s.ell(142, 108, 156, 122, c('4a3a2a'))
    s.end()
    for _ in range(60):
        s.px(170 + s.r.randint(0, 70), 90 + s.r.randint(0, 100), c('7a7870'))
    s.glow(92, 150, 60, TORCH, 0.4)
    s.hero(84, 186, 44, 1, weapon='torch')
    s.vignette(0.6)


@scene('moria_6_0')
def moria_6_0(s: Scene):
    s.rect(0, 0, W, H, c('121218'))
    # enormous high-roofed hall: rows of pillars
    for i, x in enumerate((10, 50, 90, 166, 206, 246)):
        pillar(s, x, 0, 128, 14 if i in (0, 5) else 10, c('2a2a34'))
    s.vgrad(128, H, c('26242a'), c('18161a'))
    # distant archway beyond a dark drop
    s.poly([(118, 128), (118, 92), (128, 82), (138, 92), (138, 128)], c('2a2a34'))
    s.rect(122, 94, 134, 128, c('050506'))
    # glowing red fissure across the floor
    pts = [(0, 160)]
    for x in range(0, W + 8, 8):
        pts.append((x, 158 - x * 0.08 + s.r.uniform(-3, 3)))
    pts2 = [(x, y + 4 + s.r.uniform(0, 3)) for x, y in reversed(pts[1:])]
    s.poly(pts[1:] + pts2, c('e05a20'))
    for x, y in pts[1::2]:
        s.glow(x, y + 2, 16, (200, 60, 20), 0.4)
    s.line(pts[1:], c('ffd070'))
    s.glow(150, 144, 40, TORCH, 0.5)
    s.hero(150, 182, 44, -1, weapon='torch', pose='run')
    s.vignette(0.6)


@scene('moria_6_1')
def moria_6_1(s: Scene):
    s.rect(0, 0, W, H, c('060608'))
    # immense black void with a slender stone bridge
    s.glow(210, 192, 90, (160, 40, 10), 0.8, rx=110)
    s.poly([(150, 103), (256, 96), (256, 100), (150, 107)], c('6a5a56'))
    s.line([(150, 103), (256, 96)], c('9a8478'))
    s.line([(150, 107), (256, 100)], c('241c1c'))
    # narrow ramp passage walls with red cracks and smoke
    s.poly([(0, 0), (120, 0), (130, 50), (128, 130), (150, 104), (150, 192), (0, 192)], c('2e2a2c'))
    s.poly([(120, 0), (130, 50), (128, 130), (122, 130), (120, 50)], c('3e3836'))
    for _ in range(9):
        x, y = s.r.randint(10, 110), s.r.randint(10, 170)
        pts = [(x, y)]
        for _ in range(4):
            pts.append((pts[-1][0] + s.r.randint(-4, 4), pts[-1][1] + s.r.randint(2, 7)))
        s.line(pts, c('e04a1a'))
        s.glow(x, y + 10, 10, (200, 50, 10), 0.4)
    s.poly([(0, 192), (150, 192), (150, 104), (128, 130)], c('3a3432'))
    for _ in range(5):
        cx, cy = s.r.randint(20, 200), s.r.randint(10, 80)
        s.glow(cx, cy, 30, (60, 50, 50), 0.4, rx=50)
    s.glow(136, 112, 30, TORCH, 0.5)
    s.hero(130, 140, 34, 1, weapon='torch')
    s.vignette(0.6)


@scene('moria_7_0')
def moria_7_0(s: Scene):
    s.rect(0, 0, W, H, c('0a0608'))
    # fiery abyss below
    s.vgrad(120, H, c('3a0e08'), c('c04018'))
    s.glow(128, 192, 120, (255, 90, 20), 0.7, rx=160)
    s.glow(196, 120, 110, (170, 50, 15), 0.7, rx=90)
    # immense shadow with a heart of flame, rising and turning away
    s.poly([(150, H), (138, 110), (150, 60), (170, 30), (196, 22), (220, 40), (240, 90), (250, 140), (246, H)], c('140a0c'))
    s.poly([(150, 60), (110, 20), (80, 10), (130, 50)], c('1a0e10'))
    s.poly([(220, 40), (250, 0), (256, 30), (240, 70)], c('1a0e10'))
    s.poly([(180, 24), (176, 8), (188, 20)], c('140a0c'))
    s.poly([(204, 22), (210, 6), (214, 24)], c('140a0c'))
    s.glow(196, 96, 30, (255, 120, 30), 1.0)
    s.line([(150, 60), (170, 30), (196, 22), (220, 40), (240, 90), (250, 140)], c('d0602a'))
    s.line([(150, 60), (110, 20), (80, 10)], c('a04420'))
    s.ell(190, 90, 202, 104, c('ffb050'))
    # stairs and grey daylight on the left
    s.rect(0, 40, 30, 110, c('8a8e96'))
    s.glow(14, 70, 40, (150, 150, 160), 0.6)
    for i in range(6):
        s.rect(0, 110 - i * 10, 44 - i * 4, 114 - i * 10, c('3a3638'))
    # narrow railless bridge
    s.poly([(30, 112), (256, 120), (256, 125), (30, 117)], c('4a3a36'))
    s.line([(30, 112), (256, 120)], c('8a6a5a'))
    s.hero(92, 114, 26, -1, weapon='sword')
    s.vignette(0.55)


@scene('lothlorien_final')
def lothlorien_final(s: Scene):
    s.vgrad(0, 120, c('2a3060'), c('d8a070'))
    s.stars(50, 40)
    # green hill crowned with immensely tall golden trees
    s.vgrad(120, H, c('3e5a30'), c('2a3e22'))
    s.ell(10, 96, 246, 190, c('4a6a3a'))
    s.ell(40, 100, 160, 150, c('5a7a44'))
    s.vgrad(150, H, c('3e5a30'), c('2a3e22'))
    # wall and moat
    s.poly([(0, 150), (256, 146), (256, 158), (0, 162)], c('6a7488'))
    s.line([(0, 154), (256, 150)], c('a8b4c8'))
    s.rect(0, 138, W, 146, c('5a5a50'))
    s.line([(0, 138), (W, 138)], c('8a8a78'))
    s.glow(128, 20, 30, (255, 255, 240), 0.9)
    for x, h in ((70, 120), (100, 140), (128, 150), (156, 140), (186, 118), (44, 96), (212, 96)):
        base = 143 - 46 * math.sqrt(max(0, 1 - ((x - 128) / 118) ** 2))
        s.rect(x - 2, base - h * 0.55, x + 2, base, c('a8acb4'))
        s.rect(x - 2, base - h * 0.55, x - 1, base, c('d8dce4'))
        s.leafy_tree(x, base - h * 0.45, h * 0.6, c('a8acb4'), c('c89a30'), c('f0c850'), r=h * 0.18)
    for _ in range(120):
        x = s.r.randint(30, 226)
        y = s.r.randint(20, 120)
        s.px(x, y, s.r.choice([c('f4f4ff'), c('a8f0a0'), c('ffe080')]))
    s.glow(128, 70, 100, (255, 220, 140), 0.25, rx=110)
    s.hero(128, 184, 44, -1, weapon=None)
    s.vignette(0.45)
