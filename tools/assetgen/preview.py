"""Hilfsskript: baut ein vergroessertes Kontaktblatt aus allen PNGs eines Ordners (nur zum Ansehen)."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw
from common import TEX

def sheet(folder, out, scale=6, cols=8, pad=6, bg=(52, 46, 64, 255), only=None):
    files = sorted((TEX / folder).glob("*.png"))
    if only:
        files = [f for f in files if only(f.name)]
    ims = [(f.name, Image.open(f).convert("RGBA")) for f in files]
    cell_w = max(min(i.width, 32) for _, i in ims) * scale + pad
    cell_h = max(min(i.height, 32) for _, i in ims) * scale + pad + 10
    rows = (len(ims) + cols - 1) // cols
    sh = Image.new("RGBA", (cols * cell_w, rows * cell_h), bg)
    d = ImageDraw.Draw(sh)
    for k, (n, im) in enumerate(ims):
        im2 = im.crop((0, 0, im.width, min(im.height, 32)))
        im2 = im2.resize((im2.width * scale, im2.height * scale), Image.NEAREST)
        x, y = (k % cols) * cell_w + pad // 2, (k // cols) * cell_h + pad // 2
        sh.alpha_composite(im2, (x, y))
        d.text((x, y + im2.height + 1), n[:-4][:22], fill=(220, 220, 240, 255))
    sh.convert("RGB").save(out)

if __name__ == "__main__":
    sheet(sys.argv[1], sys.argv[2], scale=int(sys.argv[3]) if len(sys.argv) > 3 else 6, cols=int(sys.argv[4]) if len(sys.argv) > 4 else 8)
