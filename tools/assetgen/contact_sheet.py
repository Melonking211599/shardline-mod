"""Kontaktbogen: Texturen vergroessert nebeneinander, mit Dateinamen. Leuchtmasken werden auf Schwarz gezeigt.

Aufruf: python tools/assetgen/contact_sheet.py <ausgabe.png> <relativer/pfad.png> [...] [--scale 6] [--cols 4]
Pfade sind relativ zu assets/shardline/textures/. Nur zur Kontrolle, schreibt nichts in die Mod.
"""
from __future__ import annotations

import sys

from PIL import Image, ImageDraw

from common import TEX

BG = (58, 62, 78)
CHECK = ((74, 78, 94), (66, 70, 86))


def tile(img: Image.Image, scale: int, dark: bool) -> Image.Image:
    w, h = img.size
    out = Image.new("RGB", (w * scale, h * scale), (0, 0, 0))
    if not dark:
        d = ImageDraw.Draw(out)
        for y in range(h):
            for x in range(w):
                d.rectangle([x * scale, y * scale, (x + 1) * scale - 1, (y + 1) * scale - 1], fill=CHECK[(x + y) % 2])
    big = img.resize((w * scale, h * scale), Image.NEAREST)
    out.paste(big, (0, 0), big)
    return out


def main():
    args = sys.argv[1:]
    out = args[0]
    scale = int(args[args.index("--scale") + 1]) if "--scale" in args else 6
    cols = int(args[args.index("--cols") + 1]) if "--cols" in args else 4
    names = [a for i, a in enumerate(args[1:], 1) if not a.startswith("--") and args[i - 1] not in ("--scale", "--cols")]
    tiles = []
    for name in names:
        img = Image.open(TEX / name).convert("RGBA")
        tiles.append((name, tile(img, scale, "glow" in name)))
    cw = max(t.width for _, t in tiles) + 16
    ch = max(t.height for _, t in tiles) + 30
    rows = (len(tiles) + cols - 1) // cols
    sheet = Image.new("RGB", (cw * min(cols, len(tiles)), ch * rows), BG)
    d = ImageDraw.Draw(sheet)
    for i, (name, t) in enumerate(tiles):
        x, y = (i % cols) * cw + 8, (i // cols) * ch + 22
        sheet.paste(t, (x, y))
        d.text((x, y - 16), name, fill=(230, 230, 240))
    sheet.save(out)
    print("gespeichert:", out)


if __name__ == "__main__":
    main()
