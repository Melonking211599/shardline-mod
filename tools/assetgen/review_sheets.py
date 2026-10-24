"""Pruefbogen fuer die Texturen (nur Kontrolle, schreibt nichts in die Mod).

Aufruf:
  python tools/assetgen/review_sheets.py blocks <aus.png> <block/name> ... [--before ORDNER] [--scale 4]
      Jeder Block 3x3 gekachelt (Naehte, Wiederholungen). Mit --before steht links der alte Stand.
      Mit --glow wird <name>_glow.png (erster Frame) voll leuchtend aufgelegt.
  python tools/assetgen/review_sheets.py items <aus.png> <item/name> ... [--before ORDNER] [--scale 6]
      Items auf Inventar-Slot (Grau wie Vanilla) gross und in 1x-Groesse (Silhouette-Test).
  python tools/assetgen/review_sheets.py atlas <aus.png> <entity/name> ... [--before ORDNER] [--scale 3] [--glow]
      Texturatlas (Glowmaske als zweites Bild auf Schwarz mit --glow).
Pfade sind relativ zu assets/shardline/textures/. --before zeigt auf eine Kopie von textures/ (alter Stand).
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw

from common import TEX

BG = (44, 46, 58)
SLOT = (139, 139, 139)
SLOT_DARK = (55, 55, 55)
SLOT_LIGHT = (255, 255, 255)
FG = (230, 230, 240)


def load(root: Path, name: str) -> Image.Image | None:
    path = root / f"{name}.png"
    if not path.exists():
        return None
    img = Image.open(path).convert("RGBA")
    if img.height > img.width and img.height % img.width == 0:      # animierter Streifen: erster Frame
        img = img.crop((0, 0, img.width, img.width))
    return img


def checker(w: int, h: int, cell: int = 8) -> Image.Image:
    out = Image.new("RGB", (w, h), (74, 78, 94))
    d = ImageDraw.Draw(out)
    for y in range(0, h, cell):
        for x in range(0, w, cell):
            if (x // cell + y // cell) % 2:
                d.rectangle([x, y, x + cell - 1, y + cell - 1], fill=(66, 70, 86))
    return out


def tiled(img: Image.Image, scale: int) -> Image.Image:
    w, h = img.size
    big = Image.new("RGBA", (w * 3, h * 3))
    for j in range(3):
        for i in range(3):
            big.paste(img, (i * w, j * h))
    big = big.resize((w * 3 * scale, h * 3 * scale), Image.NEAREST)
    bg = checker(big.width, big.height, scale * 4)
    bg.paste(big, (0, 0), big)
    return bg


def slot(img: Image.Image, scale: int, pad: int = 2) -> Image.Image:
    w, h = img.size
    size = (w + pad * 2) * scale
    out = Image.new("RGB", (size, size), SLOT)
    d = ImageDraw.Draw(out)
    d.rectangle([0, 0, size - 1, scale - 1], fill=SLOT_DARK)
    d.rectangle([0, 0, scale - 1, size - 1], fill=SLOT_DARK)
    d.rectangle([0, size - scale, size - 1, size - 1], fill=SLOT_LIGHT)
    d.rectangle([size - scale, 0, size - 1, size - 1], fill=SLOT_LIGHT)
    big = img.resize((w * scale, h * scale), Image.NEAREST)
    out.paste(big, (pad * scale, pad * scale), big)
    return out


def small(img: Image.Image) -> Image.Image:
    """1x-Ansicht auf Slot (so klein sieht man es im Spiel), danach 2x fuer die Lesbarkeit im Bogen."""
    w, h = img.size
    out = Image.new("RGB", (w + 4, h + 4), SLOT)
    out.paste(img, (2, 2), img)
    return out.resize(((w + 4) * 2, (h + 4) * 2), Image.NEAREST)


def atlas(img: Image.Image, scale: int, dark: bool) -> Image.Image:
    w, h = img.size
    big = img.resize((w * scale, h * scale), Image.NEAREST)
    bg = Image.new("RGB", big.size, (0, 0, 0)) if dark else checker(big.width, big.height, scale * 4)
    bg.paste(big, (0, 0), big)
    return bg


def arrange(cells: list[tuple[str, list[Image.Image]]], out: str, cols: int):
    """cells: (Name, [Bilder nebeneinander]). Bilder einer Zelle werden mit Abstand nebeneinander gesetzt."""
    gap = 10
    widths = [sum(i.width for i in imgs) + gap * (len(imgs) - 1) for _, imgs in cells]
    heights = [max(i.height for i in imgs) for _, imgs in cells]
    cw, ch = max(widths) + 16, max(heights) + 30
    rows = (len(cells) + cols - 1) // cols
    sheet = Image.new("RGB", (cw * min(cols, len(cells)), ch * rows), BG)
    d = ImageDraw.Draw(sheet)
    for n, (name, imgs) in enumerate(cells):
        x, y = (n % cols) * cw + 8, (n // cols) * ch + 22
        d.text((x, y - 16), name, fill=FG)
        for img in imgs:
            sheet.paste(img, (x, y))
            x += img.width + gap
    sheet.save(out)
    print("gespeichert:", out, sheet.size)


def option(args: list[str], flag: str, default=None):
    return args[args.index(flag) + 1] if flag in args else default


def main():
    mode, out = sys.argv[1], sys.argv[2]
    args = sys.argv[3:]
    scale = int(option(args, "--scale", {"blocks": 4, "items": 6, "atlas": 3}[mode]))
    before = Path(option(args, "--before")) if "--before" in args else None
    glow = "--glow" in args
    skip = {option(args, "--scale"), option(args, "--before"), option(args, "--cols")}
    names = [a for a in args if not a.startswith("--") and a not in skip]
    cols = int(option(args, "--cols", {"blocks": 3 if before is None else 2, "items": 4, "atlas": 2}[mode]))
    cells = []
    for name in names:
        roots = ([before] if before else []) + [TEX]
        imgs = []
        for root in roots:
            img = load(root, name)
            if img is None:
                continue
            if mode == "blocks":
                if glow:
                    g = load(root, name + "_glow")
                    if g is not None:
                        img = Image.alpha_composite(img, g)
                imgs.append(tiled(img, scale))
            elif mode == "items":
                imgs += [slot(img, scale), small(img)]
            else:
                imgs.append(atlas(img, scale, False))
                if glow:
                    g = load(root, name + "_glowmask")
                    if g is not None:
                        imgs.append(atlas(g, scale, True))
        if imgs:
            cells.append((name + ("   (vorher | nachher)" if before else ""), imgs))
    arrange(cells, out, cols)


if __name__ == "__main__":
    main()
