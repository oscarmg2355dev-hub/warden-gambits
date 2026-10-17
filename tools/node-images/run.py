import os
import sys
import zlib

from PIL import Image

from pix import Scene, finish
from scenes import SCENES
import scenes_eregion, scenes_moria  # noqa: F401  (register scenes)

out = sys.argv[1]
only = sys.argv[2:]
os.makedirs(out, exist_ok=True)
done = []
for key, fn in SCENES.items():
    if only and key not in only:
        continue
    s = Scene(zlib.crc32(key.encode()))
    fn(s)
    done.append((key, finish(s, os.path.join(out, key + '.webp'))))

# contact sheet
cols = 2
rows = (len(done) + cols - 1) // cols
sheet = Image.new('RGB', (cols * 512, rows * 384))
for i, (k, im) in enumerate(done):
    sheet.paste(im.resize((512, 384), Image.NEAREST), ((i % cols) * 512, (i // cols) * 384))
sheet.save(os.path.join(out, '_sheet.png'))
print('\n'.join(k for k, _ in done))
