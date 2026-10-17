import math

from pix import Scene, c, dark, mix, W, H

SCENES = {}


def scene(key):
    def deco(fn):
        SCENES[key] = fn
        return fn
    return deco


BLOOD = (120, 25, 25)
BANDIT = dict(cloak=(85, 70, 60), legs=(50, 42, 38), skin=(205, 160, 125), hair=(55, 40, 30))
DUNLEND = dict(cloak=(95, 80, 62), legs=(55, 45, 40), skin=(200, 155, 120), hair=(40, 30, 25), beard=(40, 30, 25), belt=(60, 40, 30))
ORC = dict(cloak=(60, 55, 50), legs=(40, 36, 34), skin=(110, 125, 85), hair=(30, 28, 25))
GOBLIN = dict(cloak=(70, 60, 50), legs=(45, 40, 36), skin=(140, 140, 90))


# ======================= BREE =======================
@scene('bree_6_0')
def bree_6_0(s: Scene):
    s.vgrad(0, 95, c('8c96a3'), c('c7c9c4'))
    s.cloud(20, 25, 60, c('a8adb3'))
    s.cloud(150, 18, 80, c('9da3aa'))
    s.cloud(110, 45, 50, c('b4b8bb'))
    s.forest_wall(100, 22, c('2f3d2e'), 30)
    s.ridge(100, 2, c('6f6a4a'), y_bottom=H)
    s.vgrad(100, H, c('6f6a4a'), c('4f4a34'))
    # road
    s.poly([(120, 100), (140, 100), (250, H), (60, H)], c('8a7a5a'))
    s.speckle(100, H, [c('7a6a4a'), c('9a8a68')], 300, 60, 250)
    s.tufts(102, H, [c('5d6a3a'), c('7a6a3a'), c('6b4f5a')], 220)
    for x in (20, 45, 225, 245):
        s.bush(x, 140 + (x % 17), 16, c('4d5a33'), c('66713f'))
    # overturned cart
    cx, cy = 170, 150
    s.poly([(cx - 26, cy), (cx - 22, cy - 18), (cx + 22, cy - 22), (cx + 26, cy - 4)], c('6e4a2e'))
    s.poly([(cx - 26, cy), (cx + 26, cy - 4), (cx + 24, cy + 1), (cx - 24, cy + 4)], c('4f3420'))
    for i in range(5):
        x = cx - 20 + i * 10
        s.line([(x, cy - 18 + i * -1), (x + 2, cy)], c('573a24'))
    s.ell(cx + 14, cy - 34, cx + 34, cy - 14, c('3e2c1e'))
    s.ell(cx + 17, cy - 31, cx + 31, cy - 17, c('7a6a5a'))
    for a in range(0, 360, 45):
        r = math.radians(a)
        s.line([(cx + 24, cy - 24), (cx + 24 + 7 * math.cos(r), cy - 24 + 7 * math.sin(r))], c('3e2c1e'))
    s.line([(cx - 26, cy), (cx - 46, cy + 10)], c('4f3420'), 2)
    # seat with dried blood
    s.rect(cx - 8, cy - 26, cx + 6, cy - 21, c('5a3c26'))
    s.rect(cx - 5, cy - 24, cx + 1, cy - 22, BLOOD)
    s.px(cx - 3, cy - 20, BLOOD)
    s.px(cx - 2, cy - 18, BLOOD)
    for _ in range(6):
        s.px(cx - 10 + s.r.randint(0, 20), cy + 4 + s.r.randint(0, 4), (110, 30, 25))
    # scattered sacks
    s.ell(cx - 44, cy + 6, cx - 32, cy + 14, c('a08a64'))
    s.ell(cx + 30, cy + 2, cx + 40, cy + 10, c('947e58'))
    s.hero(78, 178, 46, 1, weapon='hilt')
    s.shade(0.92)
    s.vignette(0.5)


@scene('bree_6_1')
def bree_6_1(s: Scene):
    s.vgrad(0, 90, c('7d8fa0'), c('c9c2a8'))
    s.cloud(40, 20, 70, c('a3acb4'))
    s.forest_wall(92, 18, c('34402f'), 26)
    s.vgrad(92, H, c('6e6648'), c('4a4430'))
    # clearing
    s.ell(50, 120, 210, 185, c('7d7350'))
    s.speckle(95, H, [c('5c5638'), c('8a7f58')], 350)
    s.tufts(95, H, [c('7b5a6e'), c('6d4a5f'), c('5f6b3a')], 260)  # heather
    s.brambles(30, 130, 50, c('3e3226'), c('6a2a2a'))
    s.brambles(230, 125, 50, c('3e3226'), c('6a2a2a'))
    s.brambles(200, 190, 60, c('3a2e22'), c('6a2a2a'))
    s.brambles(40, 190, 60, c('3a2e22'), c('6a2a2a'))
    # lookout on a rock whistling
    s.rock(205, 118, 34, 16, c('6e6a62'))
    s.figure(205, 104, 30, -1, arm_up=True, **BANDIT)
    s.line([(199, 87), (201, 88)], c('f0f0f0'))
    s.line([(196, 85), (194, 82)], c('f0f0f0'))
    s.line([(193, 89), (190, 89)], c('f0f0f0'))
    # wolf leaping from thorns
    s.wolf(150, 172, -1, 1.7, c('77736c'), leap=True)
    s.hero(80, 182, 46, 1, weapon='sword', arm_up=True)
    s.poly([(80, 166), (76, 172), (82, 170)], c('5a5f4c'))  # torn cloak
    s.vignette(0.5)


@scene('bree_7_0')
def bree_7_0(s: Scene):
    s.vgrad(0, 60, c('3d3a5c'), c('a2607a'))
    s.vgrad(60, 96, c('a2607a'), c('e09a6a'))
    s.forest_wall(100, 30, c('1f2622'), 34)
    s.vgrad(100, H, c('4a3e34'), c('2c2520'))
    s.poly([(110, 100), (146, 100), (240, H), (20, H)], c('5e4c3a'))
    s.speckle(100, H, [c('3b3028'), c('6a5644')], 300)
    # fallen tree across road
    s.poly([(0, 126), (246, 114), (248, 130), (0, 144)], c('4a3424'))
    s.poly([(0, 126), (246, 114), (247, 119), (0, 131)], c('6a4c34'))
    s.ell(238, 112, 256, 132, c('5a4030'))
    s.ell(241, 115, 253, 129, c('8a6a48'))
    for x in (40, 90, 170, 215):
        s.line([(x, 126), (x - 6, 112)], c('3a2a1c'), 2)
    s.glow(128, 80, 120, (60, 20, 30), 0.25)
    # henchman behind
    s.figure(212, 156, 36, -1, weapon='sword', **BANDIT)
    # leader, broad, one eye, notched axe
    s.figure(165, 182, 58, -1, weapon='axe', wcol=c('6a625a'), eyepatch=True, wide=1.35,
             cloak=c('5e4a3a'), legs=c('3a302a'), skin=c('c99a78'), hair=c('2a2220'), beard=c('2a2220'), belt=c('3a2a20'))
    s.hero(62, 182, 44, 1, weapon='sword')
    s.vignette(0.6)


# ======================= CHET =======================
@scene('chet_4_4')
def chet_4_4(s: Scene):
    s.vgrad(0, 70, c('6f8a8a'), c('a9b7a6'))
    s.forest_wall(78, 40, c('243328'), 34)
    s.vgrad(78, 112, c('3c4a32'), c('35422c'))
    # stream
    s.vgrad(112, 150, c('3d5a68'), c('2a4250'))
    for _ in range(40):
        x, y = s.r.randint(0, W - 8), s.r.randint(113, 148)
        s.line([(x, y), (x + s.r.randint(3, 8), y)], c('7fa0aa'))
    s.vgrad(150, H, c('3a442c'), c('2a321f'))
    s.tufts(78, 112, [c('4f6034'), c('5d6e3a')], 120)
    s.tufts(150, H, [c('4f6034'), c('5d6e3a')], 160)
    # stepping stones
    for x, y in ((40, 146), (78, 136), (118, 128), (160, 122), (198, 116)):
        s.ell(x - 9, y - 4, x + 9, y + 4, c('5e605c'))
        s.ell(x - 9, y - 4, x + 7, y + 1, c('7c7e78'))
    # carved post on far bank with arrow east
    s.rect(220, 70, 225, 112, c('6a5a44'))
    s.rect(218, 68, 227, 71, c('5a4a38'))
    s.line([(221, 78), (224, 78)], c('3a3026'))
    s.line([(221, 84), (223, 88)], c('3a3026'))
    s.poly([(212, 92), (232, 92), (232, 89), (238, 95), (232, 101), (232, 98), (212, 98)], c('7e6c52'))
    s.line([(216, 95), (230, 95)], c('3a3026'))
    s.ell(84, 128, 112, 134, c('22323c'))
    s.hero(98, 104, 40, 1, weapon=None, pose='leap', arm_up=True)
    s.vignette(0.5)
