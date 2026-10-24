"""Texturen der Story-Erweiterung 2 (Bloecke, Items, GUI, Effekt, Partikel). Aufruf: python tools/assetgen/story2_assets.py

Paletten aus common.py: Virus = Schwarzviolett/Magenta (VIRUS, VGLOW), Reinheit = Elfenbein/Gold/Himmelblau
(IVORY, GOLD, SKYB), Splitterweite = Violett/Cyan (VIOLET, SHARD, CYAN). Licht kommt von oben links.
Leuchtschichten (<name>_glow.png) sind durchsichtig bis auf Adern, Runen und Kristalle.
"""
from __future__ import annotations

from common import CLEAR, CYAN, GOLD, IVORY, SKYB, VGLOW, VIRUS, Tex, hx, save_png
from pixel import clusters, ramps, stamp

# ------------------------------------------------------------------ Hilfen


def blank(w=16, h=16) -> Tex:
    return Tex(w, h)


def hline(t: Tex, x0, x1, y, c):
    for x in range(x0, x1 + 1):
        t.set(x, y, c)


def vline(t: Tex, x, y0, y1, c):
    for y in range(y0, y1 + 1):
        t.set(x, y, c)


def paste(dst: Tex, src: Tex, ox=0, oy=0, rows=None):
    for y in range(src.h):
        if rows is not None and not (rows[0] <= y + oy <= rows[1]):
            continue
        for x in range(src.w):
            c = src.get(x, y)
            if c[3]:
                dst.set(ox + x, oy + y, c)


def put(t: Tex, rows: str, pal: dict, ox=0, oy=0):
    """ASCII-Stempel ohne Wrap-around ('.' = unveraendert)."""
    for j, row in enumerate(rows.strip("\n").split("\n")):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(ox + i, oy + j, pal[ch])
    return t


# ====================================================================== F2 Entscheidungsaltar
# Die Seiten der drei Modellteile liegen auf festen Zeilen: Schale y 3-5, Saeule y 6-12, Sockel y 13-15.
ALTAR_RUNE_GOLD = """
.g.
ggg
.g.
.g.
g.g
"""
ALTAR_VEIN = """
..m
.m.
mm.
.m.
m..
"""


def decision_altar_side() -> Tex:
    t = clusters(2101, IVORY, base=2, lights=4, darks=4, longest=3)
    # Schale: Goldrand mit dunkler Fuge
    for x in range(16):
        t.set(x, 3, GOLD[4] if x % 5 else GOLD[3])
        t.set(x, 4, GOLD[3])
        t.set(x, 5, GOLD[1])
    # Saeule: links Elfenbein (Siegel), rechts von Virus durchzogen (Herrschaft)
    for y in range(6, 13):
        t.set(4, y, IVORY[4])
        t.set(11, y, IVORY[0])
        for x in range(9, 12):
            if (x * 3 + y) % 5 == 0:
                t.set(x, y, VIRUS[2])
    put(t, ALTAR_RUNE_GOLD, {"g": GOLD[1]}, 5, 7)
    put(t, ALTAR_VEIN, {"m": VIRUS[3]}, 8, 7)
    # Sockel: Fase und Goldeinlage
    for x in range(16):
        t.set(x, 13, IVORY[4])
        t.set(x, 14, GOLD[2] if 2 <= x <= 13 else IVORY[2])
        t.set(x, 15, IVORY[0])
    return t


def decision_altar_side_glow() -> Tex:
    t = blank()
    put(t, ALTAR_RUNE_GOLD, {"g": GOLD[4]}, 5, 7)
    put(t, ALTAR_VEIN, {"m": VGLOW[2]}, 8, 7)
    for x in (2, 7, 12):
        t.set(x, 3, GOLD[4])                     # Funken am Schalenrand
    for x in range(3, 13, 3):
        t.set(x, 14, GOLD[4] if x < 8 else VGLOW[3])
    return t


def decision_altar_top() -> Tex:
    t = clusters(2103, IVORY, base=2, lights=3, darks=3, longest=2)
    for i in range(1, 15):                       # Sockelkante
        t.set(i, 1, IVORY[4])
        t.set(1, i, IVORY[4])
        t.set(i, 14, IVORY[1])
        t.set(14, i, IVORY[1])
    for y in range(3, 13):                       # Schale: dunkles Becken mit Goldring
        for x in range(3, 13):
            ring = max(abs(x * 2 - 15), abs(y * 2 - 15))
            if ring >= 9:
                t.set(x, y, GOLD[3] if (x + y) % 2 else GOLD[2])
            else:
                t.set(x, y, VIRUS[1] if (x + y) % 3 else VIRUS[2])
    return t


def decision_altar_top_glow() -> Tex:
    """Kristall und Ring in der Schale: links golden, rechts magenta - die beiden Wege."""
    t = blank()
    for y in range(16):
        for x in range(16):
            gold = x < 8
            if 6 <= x <= 9 and 6 <= y <= 9:
                edge = x in (6, 9) or y in (6, 9)
                t.set(x, y, (GOLD[3] if edge else GOLD[4]) if gold else (VGLOW[2] if edge else VGLOW[4]))
            elif (x in (4, 11) and 4 <= y <= 11) or (y in (4, 11) and 4 <= x <= 11):
                if (x + y) % 2 == 0:
                    t.set(x, y, GOLD[4] if gold else VGLOW[3])
    return t


def decision_altar_bottom() -> Tex:
    return clusters(2105, IVORY, base=1, lights=3, darks=4, longest=3)


BLOCKS = {
    "block/decision_altar_side": decision_altar_side,
    "block/decision_altar_side_glow": decision_altar_side_glow,
    "block/decision_altar_top": decision_altar_top,
    "block/decision_altar_top_glow": decision_altar_top_glow,
    "block/decision_altar_bottom": decision_altar_bottom,
}
ITEMS = {}
OTHER = {}


def main():
    for name, fn in {**BLOCKS, **ITEMS, **OTHER}.items():
        save_png(fn(), name)
    print(f"story2_assets: {len(BLOCKS)} Block-, {len(ITEMS)} Item- und {len(OTHER)} weitere Texturen")


if __name__ == "__main__":
    main()
