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


# ====================================================================== F3 Hueter-Ruinen (Elfenbein, Gold, Himmelblau)
MOSS = [hx(c) for c in ("#1f5a5c", "#2c7f7a", "#46a99a")]
RUNE = """
................
................
.....gggggg.....
....g......g....
...g...ss...g...
..g...s..s...g..
..g..s....s..g..
..g..s.ss.s..g..
..g..s.ss.s..g..
..g..s....s..g..
..g...s..s...g..
...g...ss...g...
....g......g....
.....gggggg.....
................
................
"""


def keeper_ruin_bricks() -> Tex:
    """Helle Hueterziegel (versetzt, 8x4), dunkle Fugen, hier und da Goldeinlage, Moos und Risse."""
    t = Tex(16, 16)
    for y in range(16):
        row = y // 4
        for x in range(16):
            bx = (x + (4 if row % 2 else 0)) % 8
            by = y % 4
            if by == 3 or bx == 7:
                t.set(x, y, IVORY[0])
                continue
            tone = 2.0
            if by == 0 or bx == 0:
                tone = 3.0
            if by == 2 or bx == 6:
                tone = 1.6
            t.set(x, y, IVORY[int(round(tone))])
    base = clusters(3101, IVORY, base=2, lights=4, darks=4, longest=2)
    for (x, y) in ((2, 1), (11, 5), (5, 9), (13, 13), (9, 2)):
        t.set(x, y, base.get(x, y))
    for (x, y) in ((7, 3), (3, 11), (15, 7)):
        t.set(x, y, GOLD[2])                     # Goldeinlage in den Fugen
    for (x, y) in ((12, 10), (13, 10), (13, 9), (14, 8)):
        t.set(x, y, IVORY[0])                    # Riss
    for (x, y, c) in ((0, 14, 1), (1, 14, 2), (1, 13, 0), (2, 14, 1), (9, 15, 1), (10, 14, 2)):
        t.set(x, y, MOSS[c])                     # Moos am Fuss
    return t


def keeper_ruin_pillar() -> Tex:
    """Kannelierte Saeule mit goldenem Kapitell und Fuss."""
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            f = x % 4
            c = IVORY[3] if f == 0 else IVORY[2] if f in (1, 2) else IVORY[1]
            if y in (0, 15):
                c = GOLD[3] if y == 0 else GOLD[1]
            elif y in (1, 14):
                c = GOLD[2]
            elif y in (2, 13):
                c = IVORY[4] if y == 2 else IVORY[0]
            t.set(x, y, c)
    for (x, y) in ((6, 7), (6, 8), (10, 4), (2, 10)):
        t.set(x, y, IVORY[0])                    # Abplatzer
    return t


def keeper_ruin_pillar_top() -> Tex:
    t = clusters(3103, IVORY, base=2, lights=3, darks=3, longest=2)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 6.5 <= d < 7.6:
                t.set(x, y, GOLD[2] if (x + y) % 2 else GOLD[3])
            elif d < 2.2:
                t.set(x, y, GOLD[3] if x + y < 15 else GOLD[2])
            elif 2.2 <= d < 3.0:
                t.set(x, y, IVORY[0])
    return t


def keeper_rune_slab() -> Tex:
    t = clusters(3105, IVORY, base=2, lights=4, darks=3, longest=2)
    for i in range(16):
        t.set(i, 0, IVORY[4])
        t.set(0, i, IVORY[4])
        t.set(i, 15, IVORY[1])
        t.set(15, i, IVORY[1])
    put(t, RUNE, {"g": GOLD[1], "s": IVORY[0]})  # eingemeisselte Rune
    return t


def keeper_rune_slab_glow() -> Tex:
    t = blank()
    put(t, RUNE, {"g": SKYB[3], "s": GOLD[4]})
    return t


def keeper_rune_slab_side() -> Tex:
    """Zwei uebereinanderliegende Stufen (die Seite der unteren Stufe nutzt die untere Haelfte)."""
    t = Tex(16, 16)
    for half in (0, 8):
        for y in range(half, half + 8):
            for x in range(16):
                ly = y - half
                c = IVORY[4] if ly == 0 else IVORY[0] if ly == 7 else IVORY[2]
                if ly == 4 and 1 <= x <= 14:
                    c = GOLD[2] if x % 3 else GOLD[3]
                elif ly in (1, 6) and x % 5 == 2:
                    c = IVORY[3] if ly == 1 else IVORY[1]
                t.set(x, y, c)
    return t


# ====================================================================== F4 Immunitaet (Effekt-Icon 18x18)
IMMUNITY_ICON = """
..................
...oooooooooooo...
..oGGGGGGGGGGGGo..
..oGWWWWWWWWWWgo..
..oGWWWWbbWWWWgo..
..oGWWWbBBbWWWgo..
..oGWWbBSSBbWWgo..
..oGWWbBSBBbWWgo..
..oGWWbBBBBbWWgo..
..oGWWWbBBbWWWgo..
...oGWWWbbWWWgo...
...oGWWWWWWWWgo...
....oGWWWWWWgo....
.....oGWWWWgo.....
......oGWWgo......
.......oggo.......
........oo........
..................
"""


def immunity_icon() -> Tex:
    t = Tex(18, 18)
    pal = {"o": hx("#2a1d10"), "G": GOLD[3], "g": GOLD[1], "b": SKYB[0], "B": SKYB[2], "S": SKYB[4]}
    for y, row in enumerate(IMMUNITY_ICON.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch == "W":
                t.set(x, y, IVORY[4] if x < 9 else IVORY[3] if y < 12 else IVORY[2])
            elif ch != ".":
                t.set(x, y, pal[ch])
    return t


OTHER = {
    "mob_effect/immunity": immunity_icon,
}

BLOCKS = {
    "block/keeper_ruin_bricks": keeper_ruin_bricks,
    "block/keeper_ruin_pillar": keeper_ruin_pillar,
    "block/keeper_ruin_pillar_top": keeper_ruin_pillar_top,
    "block/keeper_rune_slab": keeper_rune_slab,
    "block/keeper_rune_slab_glow": keeper_rune_slab_glow,
    "block/keeper_rune_slab_side": keeper_rune_slab_side,
    "block/decision_altar_side": decision_altar_side,
    "block/decision_altar_side_glow": decision_altar_side_glow,
    "block/decision_altar_top": decision_altar_top,
    "block/decision_altar_top_glow": decision_altar_top_glow,
    "block/decision_altar_bottom": decision_altar_bottom,
}
ITEMS = {}


def main():
    for name, fn in {**BLOCKS, **ITEMS, **OTHER}.items():
        save_png(fn(), name)
    print(f"story2_assets: {len(BLOCKS)} Block-, {len(ITEMS)} Item- und {len(OTHER)} weitere Texturen")


if __name__ == "__main__":
    main()
