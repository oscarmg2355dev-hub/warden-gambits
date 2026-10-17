from pix import Scene, c, H, W
from scenes import scene, DUNLEND

CARADHRAS = c('9a4a3e')
SNOW = c('e6e8ee')
SNOWG = c('c4c8d4')


def winter_sky(s, y1=100, top='6f7590', bot='c9c4c8'):
    s.vgrad(0, y1, c(top), c(bot))


def caradhras(s, x, py, base, half, clouds=True):
    s.mountain(x, py, base, half, CARADHRAS, snow=SNOW, snow_frac=0.35)
    if clouds:
        s.cloud(x - half * 0.5, py + 10, half * 0.9, c('9a9cac'))


@scene('eregion_4_5')
def eregion_4_5(s: Scene):
    winter_sky(s, 92, '7c8aa0', 'c8c6c0')
    s.ridge(92, 6, c('6f7a6a'), 0.03)
    s.vgrad(96, H, c('6a6450'), c('4a4436'))
    s.speckle(96, H, [c('5a5442'), c('7a7258'), SNOWG], 400)
    for x, y, h in ((20, 150, 70), (60, 120, 50), (205, 125, 55), (245, 155, 75), (130, 104, 34)):
        s.holly(x, y, h, c('2e4a34'), snow=SNOWG)
    # abandoned camp: blankets, pouch with silver
    s.poly([(110, 160), (160, 150), (176, 170), (122, 180)], c('6a4a58'))
    s.line([(118, 165), (168, 158)], c('4f3644'))
    s.poly([(170, 140), (200, 136), (206, 150), (176, 152)], c('5a6a7a'))
    s.ell(150, 168, 164, 180, c('7a5a38'))
    for p in ((166, 178), (169, 180), (163, 182), (171, 177), (167, 182)):
        s.px(*p, c('e8e8f0'))
        s.px(p[0] + 1, p[1], c('a8a8b8'))
    s.line([(124, 176), (132, 168)], c('3a2a20'), 2)
    s.line([(126, 168), (134, 176)], c('3a2a20'), 2)
    s.hero(84, 184, 46, 1, weapon=None, arm_up=True)
    # parchment map with a white open hand
    s.begin()
    s.poly([(92, 136), (116, 133), (118, 152), (94, 155)], c('d8c49a'))
    s.line([(98, 140), (110, 139)], c('8a6a48'))
    s.line([(97, 144), (106, 146)], c('8a6a48'))
    s.ell(103, 146, 109, 152, c('f6f6f6'))
    for dx in (0, 2, 4, 6):
        s.line([(103 + dx, 147), (102 + dx * 1.2, 143)], c('f6f6f6'))
    s.end()
    s.vignette(0.5)


@scene('eregion_4_6')
def eregion_4_6(s: Scene):
    winter_sky(s, 92, '6a7088', 'c4c2c8')
    s.mountain(60, 58, 92, 50, c('7a7484'), snow=SNOW, snow_frac=0.4)
    s.mountain(220, 50, 92, 50, c('8a7a80'), snow=SNOW, snow_frac=0.4)
    caradhras(s, 150, 30, 92, 70)
    s.vgrad(92, H, c('a8aab4'), c('7a7c86'))
    # frozen stream
    s.poly([(0, 150), (120, 138), (256, 128), (256, 140), (120, 152), (0, 166)], c('9ab4c8'))
    s.line([(0, 158), (256, 134)], c('c8dae6'))
    # muddy bank with tracks leading to the mountains
    s.poly([(30, H), (220, H), (150, 94), (140, 94)], c('6a5a4a'))
    s.speckle(96, H, [c('5a4a3c'), c('7a6a58')], 200, 40, 220)
    for i in range(14):
        t = i / 14
        y = H - 8 - t * 92
        x = 125 + (1 - t) * 10 + (3 if i % 2 else -3) * (1 - t)
        sz = max(1, int(4 * (1 - t)))
        s.ell(x - sz, y - sz * 0.6, x + sz, y + sz * 0.6, c('3a2e26'))
        if sz > 1:
            for k in (-1, 0, 1):
                s.px(x + k * sz, y - sz, c('3a2e26'))
        bx = x + 14 * (1 - t) + 4
        s.rect(bx, y - sz * 0.5, bx + sz * 1.4, y + sz * 1.2, c('2e2620'))
    s.hero(80, 180, 44, 1, weapon=None, pose='crouch')
    s.snowfall(60)
    s.vignette(0.5)


@scene('eregion_4_7')
def eregion_4_7(s: Scene):
    s.vgrad(0, 120, c('0e1428'), c('2a3050'))
    s.stars(110, 110)
    s.vgrad(120, H, c('8890a8'), c('5a6078'))
    s.speckle(120, H, [c('a0a8c0'), c('6a7088')], 300)
    # small elvish stone shelter
    s.poly([(40, 150), (40, 70), (128, 40), (216, 70), (216, 150)], c('6a6a78'))
    s.bricks(46, 72, 210, 150, c('7a7a88'), c('5a5a68'), 14, 7)
    s.poly([(30, 74), (128, 36), (226, 74), (226, 80), (128, 44), (30, 80)], c('4a4a58'))
    s.poly([(30, 74), (128, 36), (226, 74), (128, 40)], SNOW)
    # doorway with carved stars above, fire inside
    s.poly([(100, 152), (100, 96), (128, 80), (156, 96), (156, 152)], c('1a1416'))
    s.glow(128, 140, 46, (255, 130, 50), 0.6, rx=30)
    for x, y in ((112, 74), (128, 68), (144, 74)):
        s.line([(x - 3, y), (x + 3, y)], c('c8d0e8'))
        s.line([(x, y - 3), (x, y + 3)], c('c8d0e8'))
    # faded elvish letters
    s.line([(60, 100), (70, 104), (64, 112)], c('9a9ab0'))
    s.line([(186, 100), (196, 98), (190, 110)], c('9a9ab0'))
    s.fire(140, 150, 0.8, glow=False)
    s.hero(116, 152, 30, 1, weapon=None, pose='sit')
    s.snowfall(120)
    s.vignette(0.5)


@scene('eregion_5_0')
def eregion_5_0(s: Scene):
    winter_sky(s, 112, '5e6a88', 'c2bcc0')
    s.mountain(80, 76, 112, 60, c('7a7484'), snow=SNOW, snow_frac=0.35)
    caradhras(s, 190, 36, 112, 70)
    s.cloud(200, 55, 50, c('9a9cac'))
    s.vgrad(112, H, c('7a8468'), c('6e7a5e'))
    s.ridge(150, 30, c('6e7a5e'), 0.012, 1.2)
    s.vgrad(150, H, c('6e7a5e'), c('4a5440'))
    s.tufts(120, H, [c('5a6a48'), c('8a9070'), SNOWG], 260)
    # ancient moss-covered waystone
    s.poly([(96, 168), (98, 96), (104, 88), (122, 88), (128, 96), (130, 168)], c('8a8a84'))
    s.poly([(122, 88), (128, 96), (130, 168), (122, 168)], c('6a6a66'))
    for _ in range(40):
        s.px(s.r.randint(98, 128), s.r.randint(120, 166), c('4a6a3a'))
    s.poly([(102, 104), (116, 104), (116, 100), (124, 107), (116, 114), (116, 110), (102, 110)], c('4a4a46'))
    for y in (122, 132, 142):
        s.line([(104, y), (110, y - 4), (116, y), (122, y - 4)], c('4a4a46'))
    s.hero(70, 172, 44, 1, weapon=None, arm_up=True)
    s.vignette(0.5)


@scene('eregion_5_1')
def eregion_5_1(s: Scene):
    s.vgrad(0, H, c('a8acb8'), c('d8dae2'))
    # rock walls of the ravine
    s.poly([(0, 0), (70, 0), (90, 60), (78, 120), (96, 192), (0, 192)], c('5a5662'))
    s.poly([(256, 0), (180, 0), (166, 70), (180, 130), (160, 192), (256, 192)], c('4e4a56'))
    s.poly([(70, 0), (90, 60), (78, 120), (70, 120), (80, 60)], c('7a7684'))
    for x0, x1 in ((0, 80), (176, 256)):
        for _ in range(20):
            x, y = s.r.randint(x0, x1), s.r.randint(0, 190)
            s.line([(x, y), (x + s.r.randint(-6, 6), y + s.r.randint(3, 10))], c('3a3644'))
    s.vgrad(150, H, c('e6e8ee'), c('b8bcc8'), 86, 172)
    s.snowfall(80)
    s.figure(112, 124, 30, 1, weapon='spear', wcol=c('a0a0a8'), arm_up=True, **DUNLEND)
    s.wolf(184, 184, -1, 1.6, c('6e6a66'))
    s.hero(124, 186, 46, -1, weapon='sword')
    s.vignette(0.55)


@scene('eregion_5_2')
def eregion_5_2(s: Scene):
    s.vgrad(0, 120, c('70747e'), c('a4a6ae'))
    s.cloud(0, 30, 90, c('64686f'))
    s.cloud(150, 20, 110, c('5c6068'))
    s.ridge(122, 2, c('9a9a92'), 0.02)
    s.vgrad(124, H, c('8a8a80'), c('62625a'))
    s.tufts(124, H, [c('7a7a6e'), c('a0a094'), c('5a5a50')], 300)
    for x, y in ((40, 30), (70, 22), (170, 34), (200, 18), (110, 14)):
        s.crow(x, y, 0.5, outline=None)
    s.hero(128, 180, 46, 1, weapon='sword', arm_up=True)
    s.crow(176, 106, 1.8, dive=True)
    s.crow(70, 128, 1.6, dive=True)
    s.vignette(0.55)


@scene('eregion_5_3')
def eregion_5_3(s: Scene):
    winter_sky(s, 90, '6a6c84', 'b4b0b8')
    s.mountain(160, 50, 90, 50, c('7a7484'), snow=SNOW, snow_frac=0.4)
    s.mountain(210, 34, 90, 60, CARADHRAS, snow=SNOW, snow_frac=0.4)
    s.ridge(90, 3, c('5a6050'))
    s.vgrad(90, 130, c('5a6050'), c('4a5040'))
    # narrow dark fast river
    s.poly([(0, 118), (256, 100), (256, 124), (0, 150)], c('1e2a36'))
    for _ in range(30):
        x = s.r.randint(0, 240)
        y = int(120 - x * 0.07 + s.r.randint(0, 20))
        s.line([(x, y), (x + 6, y - 1)], c('5a7080'))
    s.poly([(0, 150), (256, 124), (256, H), (0, H)], c('a89a7c'))
    s.speckle(130, H, [c('8a7c60'), c('c0b494')], 300)
    for x in (30, 70, 200):
        s.willow(x, 120 - x * 0.07, 40, c('3a3630'))
    # many heavy boot prints heading east
    for i in range(12):
        x = 30 + i * 18
        y = 178 - i * 3.2 + (3 if i % 2 else 0)
        s.ell(x, y, x + 5, y + 2, c('6a5c44'))
        s.ell(x + 7, y - 4, x + 12, y - 2, c('6a5c44'))
    s.hero(150, 172, 42, 1, weapon=None)
    s.vignette(0.5)


@scene('eregion_6_0')
def eregion_6_0(s: Scene):
    s.vgrad(0, 140, c('3c3e5c'), c('c08a7c'))
    caradhras(s, 128, 24, 104, 80, clouds=False)
    s.ridge(110, 30, c('4a4a48'), 0.025, 2.0)
    # large campfire glowing below in the valley
    s.glow(128, 112, 40, (255, 120, 40), 0.8, rx=60)
    s.ell(124, 108, 132, 114, c('ffb050'))
    # windswept saddle between hills
    s.poly([(0, 110), (60, 128), (128, 140), (200, 126), (256, 104), (256, H), (0, H)], c('6a6458'))
    s.vgrad(140, H, c('6a6458'), c('4a4438'))
    s.tufts(130, H, [c('8a8270'), c('5a5446'), SNOWG], 240)
    s.figure(206, 168, 40, -1, arm_up=True, **DUNLEND)
    s.line([(188, 150), (172, 168)], c('3a2a20'))
    s.wolf(160, 176, -1, 1.4, c('6e6a66'), leap=True)
    s.wolf(176, 190, -1, 1.5, c('7a756e'))
    s.hero(54, 184, 44, 1, weapon='sword', arm_up=True)
    s.vignette(0.5)


@scene('eregion_6_1')
def eregion_6_1(s: Scene):
    s.vgrad(0, 50, c('2e2e4c'), c('8a5a6a'))
    s.vgrad(50, 108, c('8a5a6a'), c('d89a7a'))
    caradhras(s, 140, 14, 104, 92, clouds=False)
    # great ruins at its foot, firelight between the walls
    for x, h in ((96, 22), (112, 30), (130, 18), (150, 26), (170, 20)):
        s.rect(x, 104 - h, x + 9, 104, c('5a4e52'))
        s.rect(x, 104 - h, x + 3, 104, c('7a6a6a'))
    s.glow(136, 98, 30, (255, 130, 50), 0.8, rx=50)
    s.vgrad(104, H, c('c8c4cc'), c('8a8698'))
    s.speckle(104, H, [c('a8a4b4'), c('e0dce4')], 300)
    # old elvish road climbing toward the mountain
    s.poly([(110, H), (180, H), (142, 104), (134, 104)], c('8a7c7a'))
    for i in range(8):
        y = 110 + i * 11
        w = 4 + i * 4
        s.line([(138 - w, y), (138 + w, y)], c('6a5e60'))
    for x, y, h in ((30, 170, 60), (70, 140, 40), (210, 160, 56), (238, 130, 40), (90, 118, 22)):
        s.holly(x, y, h, c('2a3e30'), snow=SNOW)
    s.hero(140, 176, 32, 1, weapon='sword')
    s.snowfall(50)
    s.vignette(0.55)


@scene('eregion_7_0')
def eregion_7_0(s: Scene):
    s.vgrad(0, 70, c('1e1a2c'), c('4a2e3a'))
    s.mountain(150, -10, 80, 120, c('6a3430'), snow=c('b0a0a8'), snow_frac=0.5)
    # firelit ruins of an elvish hall
    s.rect(0, 70, W, H, c('3a2e2e'))
    for x in (10, 70, 186, 236):
        s.rect(x, 30, x + 14, 150, c('6a5a58'))
        s.rect(x, 30, x + 4, 150, c('8a7a74'))
    s.poly([(24, 40), (44, 24), (70, 40), (70, 46), (44, 30), (24, 46)], c('6a5a58'))
    s.poly([(200, 40), (214, 30), (236, 40), (236, 46), (214, 36), (200, 46)], c('6a5a58'))
    s.vgrad(140, H, c('5a4038'), c('3a2a26'))
    s.glow(128, 150, 110, (255, 120, 40), 0.55, rx=140)
    s.fire(40, 150, 1.2, glow=False)
    s.fire(220, 148, 1.0, glow=False)
    s.figure(214, 158, 30, -1, weapon='sword', **DUNLEND)
    s.wolf(186, 166, -1, 1.2, c('5e5a56'))
    s.figure(160, 186, 64, -1, weapon='axe', wcol=c('7a7c84'), arm_up=True, wide=1.4,
             cloak=c('5a4232'), legs=c('2e2620'), skin=c('c08a68'), hair=c('141012'), beard=c('141012'), belt=c('3a2418'))
    # the second axe, held low
    s.begin()
    s.line([(146, 160), (136, 146)], c('5a3e2a'), 2)
    s.poly([(136, 146), (128, 142), (127, 152), (136, 152)], c('7a7c84'))
    s.end()
    s.hero(60, 186, 42, 1, weapon='sword')
    s.vignette(0.55)
