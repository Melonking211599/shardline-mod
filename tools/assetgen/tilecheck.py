"""Kachelpruefung (nur Kontrolle, schreibt nichts in die Mod).

Aufruf:
  python tools/assetgen/tilecheck.py rank [--top 30]            Kacheln mit der staerksten inneren Wiederholung
  python tools/assetgen/tilecheck.py field <aus.png> <block/a> <block/b> ... [--cols 8 --rows 5 --scale 3]
      Zufallsfeld wie im Spiel: pro Zelle eine der angegebenen Varianten (fester Seed), zeigt Raster und Wiederholung.
Wiederholungsmass: Anteil gleicher Pixel bei Verschiebung um mindestens 4 px (kachelnd), abzueglich Zufallsuebereinstimmung.
"""
from __future__ import annotations

import random
import sys
from pathlib import Path

from PIL import Image

from common import TEX


def load(name: str) -> Image.Image:
    return Image.open(TEX / f"{name}.png").convert("RGBA")


def repetition(im: Image.Image) -> tuple[float, tuple[int, int]]:
    w, h = im.size
    if h != w:                                   # animierte Streifen: erstes Bild
        im = im.crop((0, 0, w, w))
        h = w
    px = list(im.getdata())
    cols = {}
    idx = [cols.setdefault(p, len(cols)) for p in px]
    n = len(px)
    chance = sum((idx.count(i) / n) ** 2 for i in range(len(cols)))
    best, at = 0.0, (0, 0)
    for dy in range(h):
        for dx in range(w):
            if min(dx, w - dx) + min(dy, h - dy) < 4 or (min(dx, w - dx) < 2 and min(dy, h - dy) < 2):
                continue
            if min(dx, w - dx) + min(dy, h - dy) < 4:
                continue
            same = sum(1 for y in range(h) for x in range(w) if idx[y * w + x] == idx[((y + dy) % h) * w + (x + dx) % w])
            v = same / n - chance
            if v > best:
                best, at = v, (dx, dy)
    return best, at


def rank(top: int):
    rows = []
    for p in sorted((TEX / "block").glob("*.png")):
        if p.stem.endswith("_glow"):
            continue
        im = Image.open(p).convert("RGBA")
        if im.size[0] != 16:
            continue
        r, at = repetition(im)
        rows.append((r, p.stem, at))
    rows.sort(reverse=True)
    for r, name, at in rows[:top]:
        print(f"{r * 100:5.1f} %  {name:34s} Verschiebung {at}")
    vals = sorted(r for r, _, _ in rows)
    print(f"Median {vals[len(vals) // 2] * 100:.1f} %, {sum(1 for v in vals if v > 0.25)} ueber 25 %, {sum(1 for v in vals if v > 0.40)} ueber 40 % (von {len(vals)})")


def field(out: str, names: list[str], cols: int = 8, rows: int = 5, scale: int = 3, seed: int = 7):
    tiles = [load(n) for n in names]
    tiles = [t.crop((0, 0, 16, 16)) for t in tiles]
    rng = random.Random(seed)
    img = Image.new("RGBA", (cols * 16, rows * 16))
    for j in range(rows):
        for i in range(cols):
            img.alpha_composite(rng.choice(tiles), (i * 16, j * 16))
    img.resize((cols * 16 * scale, rows * 16 * scale), Image.NEAREST).convert("RGB").save(out)
    print("gespeichert:", out)


if __name__ == "__main__":
    a = sys.argv[1:]
    if a[0] == "rank":
        rank(int(a[a.index("--top") + 1]) if "--top" in a else 30)
    elif a[0] == "field":
        def opt(k, d):
            return int(a[a.index(k) + 1]) if k in a else d
        names = [x for x in a[2:] if not x.startswith("--") and not x.isdigit()]
        field(a[1], names, opt("--cols", 8), opt("--rows", 5), opt("--scale", 3))
