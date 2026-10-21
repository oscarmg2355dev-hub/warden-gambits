"""Build the Google Play feature graphic (1024x500 PNG) in docs/store/feature-graphic.png.

Background: the ending node image (the Warden before Mount Doom), darkened towards the left where the title sits;
title in Cinzel, subtitle in Almendra and the app icon, all from the game's own assets.

    python tools/store/make_feature_graphic.py
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "docs" / "content" / "gemini" / "orodruin_final.webp"
ICON = ROOT.parent / "warden2" / "assets" / "icon" / "warden_icon.png"
FONTS = ROOT / "app" / "src" / "main" / "res" / "font"
OUT = ROOT / "docs" / "store" / "feature-graphic.png"

W, H = 1024, 500
GOLD = (201, 164, 76)
TEXT = (237, 227, 207)


def background() -> Image.Image:
    im = Image.open(SRC).convert("RGB")
    scale = W / im.width
    im = im.resize((W, round(im.height * scale)), Image.NEAREST)
    top = min(max(0, 150), im.height - H)
    im = im.crop((0, top, W, top + H))
    # Darken the left half (where the text goes) with a horizontal gradient.
    shade = Image.new("L", (W, H))
    px = shade.load()
    for x in range(W):
        a = int(215 * max(0.0, 1 - x / (W * 0.62)))
        for y in range(H):
            px[x, y] = a
    dark = Image.new("RGB", (W, H), (12, 9, 6))
    return Image.composite(dark, im, shade)


def text_with_shadow(img, xy, text, font, fill):
    shadow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).text((xy[0] + 3, xy[1] + 3), text, font=font, fill=(0, 0, 0, 230))
    img.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(3)))
    ImageDraw.Draw(img).text(xy, text, font=font, fill=fill)


def main():
    img = background().convert("RGBA")
    title = ImageFont.truetype(str(FONTS / "cinzel.ttf"), 56)
    try:
        title.set_variation_by_axes([800])
    except (OSError, AttributeError):
        pass
    sub = ImageFont.truetype(str(FONTS / "almendra_regular.ttf"), 32)
    small = ImageFont.truetype(str(FONTS / "almendra_italic.ttf"), 26)
    x = 52
    if ICON.exists():
        icon = Image.open(ICON).convert("RGBA")
        # The source icon has white corners: crop a little and round them off.
        inset = round(icon.width * 0.06)
        icon = icon.crop((inset, inset, icon.width - inset, icon.height - inset)).resize((104, 104), Image.LANCZOS)
        mask = Image.new("L", icon.size, 0)
        ImageDraw.Draw(mask).rounded_rectangle((0, 0, *icon.size), radius=22, fill=255)
        icon.putalpha(mask)
        img.alpha_composite(icon, (x, 100))
    text_with_shadow(img, (x, 226), "WARDEN-GAMBITS", title, GOLD)
    text_with_shadow(img, (x + 4, 312), "Combate por combos en la Tierra Media", sub, TEXT)
    text_with_shadow(img, (x + 4, 360), "De Bree al Monte del Destino", small, (214, 196, 160))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.convert("RGB").save(OUT, "PNG", optimize=True)
    print(f"{OUT.relative_to(ROOT)}: {img.size[0]}x{img.size[1]}, {OUT.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
