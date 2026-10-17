"""Builds the adaptive launcher icon (mipmap-*/ic_launcher_{background,foreground}.png) from
the original Flutter-app artwork.

Usage: python tools/app-icon/make_adaptive_icon.py ../warden2/assets/icon/warden_icon.png app/src/main/res preview.png
"""
import sys
from PIL import Image, ImageFilter, ImageDraw
src, res, preview = sys.argv[1], sys.argv[2], sys.argv[3]
im = Image.open(src).convert("RGBA")
# Inner square without the white rounded corners of the original artwork.
inner = im.crop((22, 22, im.width - 22, im.height - 22)).convert("RGB")
W = 432  # 108 dp at xxxhdpi
# Background: warm dark radial gradient matching the artwork's backdrop.
bg = Image.new("RGB", (W, W))
px = bg.load()
c0, c1 = (46, 28, 16), (10, 7, 6)
for y in range(W):
    for x in range(W):
        t = min(1.0, (((x - W / 2) ** 2 + (y - W / 2) ** 2) ** 0.5) / (W * 0.5))
        px[x, y] = tuple(int(a + (b - a) * t) for a, b in zip(c0, c1))
# Foreground: the shield and sword scaled into the 66 dp safe zone, feathered edges.
fs = 262
fg_img = inner.resize((fs, fs), Image.LANCZOS).convert("RGBA")
mask = Image.new("L", (fs, fs), 0)
ImageDraw.Draw(mask).ellipse((4, 4, fs - 4, fs - 4), fill=255)
mask = mask.filter(ImageFilter.GaussianBlur(10))
fg_img.putalpha(mask)
fg = Image.new("RGBA", (W, W), (0, 0, 0, 0))
fg.paste(fg_img, ((W - fs) // 2, (W - fs) // 2 + 4), fg_img)
for d, s in {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}.items():
    bg.resize((s, s), Image.LANCZOS).save(f"{res}/mipmap-{d}/ic_launcher_background.png", optimize=True)
    fg.resize((s, s), Image.LANCZOS).save(f"{res}/mipmap-{d}/ic_launcher_foreground.png", optimize=True)
# Preview: circle and squircle masks as a launcher would apply them.
comp = bg.convert("RGBA"); comp.alpha_composite(fg)
out = Image.new("RGBA", (W * 2 + 40, W), (255, 255, 255, 255))
for i, r in enumerate((W // 2, W // 4)):
    m = Image.new("L", (W, W), 0)
    ImageDraw.Draw(m).rounded_rectangle((0, 0, W - 1, W - 1), radius=r, fill=255)
    # Launchers show the central 72 of 108 dp.
    c = comp.crop((W // 6, W // 6, W - W // 6, W - W // 6)).resize((W, W))
    out.paste(c, (i * (W + 40), 0), m)
out.save(preview)
