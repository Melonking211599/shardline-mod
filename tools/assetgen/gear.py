"""Ruestungs-, Werkzeug- und Waffen-Icons (16x16) im Netherite-Stil. Aufruf: python tools/assetgen/gear.py

Alles von Hand als Pixelkarte gezeichnet, feste Paletten, kein Zufall. Licht oben links.
Zeichen: '.' frei, '#' Kontur, '1'-'5' Stahl dunkel -> hell (5 = Lichtkante), 'a' 'b' 'c' 'w' Kristall bzw. Kern
dunkel -> hell, 'h' 'j' 'k' Griffwicklung dunkel -> hell, 'r' Niete.
Ruestungsicons sind als linke Haelfte gezeichnet; die rechte Haelfte entsteht gespiegelt und eine Stufe dunkler.
"""
from __future__ import annotations

from common import Tex, hx, save_png
from pixel import art

# ------------------------------------------------------------------ Paletten
# Shard: dunkler Violettstahl mit Kristallinlays
SHARD_P = {
    "#": hx("#0f0a1b"), "1": hx("#1e1533"), "2": hx("#30224f"), "3": hx("#45337a"), "4": hx("#5f4a9c"), "5": hx("#8e78cc"),
    "a": hx("#5a2fb0"), "b": hx("#9152ec"), "c": hx("#cf8bff"), "w": hx("#f6e2ff"),
    "h": hx("#3a2a2e"), "j": hx("#5a4248"), "k": hx("#7d6168"), "r": hx("#b4a3e2"),
}
# Colossus: schwerer Steinstahl (kuehles Schiefer) mit cyanfarbenem Kern
COLOSSUS_P = {
    "#": hx("#090b11"), "1": hx("#1a1f2b"), "2": hx("#2a3342"), "3": hx("#43516a"), "4": hx("#61728d"), "5": hx("#8fa1ba"),
    "a": hx("#0b5a68"), "b": hx("#1ea4b6"), "c": hx("#6fe6f4"), "w": hx("#e0ffff"),
    "h": hx("#2e2724"), "j": hx("#4d403a"), "k": hx("#6e5d53"), "r": hx("#a5b3c4"),
}

_DARKER = {"5": "4", "4": "3", "3": "2", "2": "1", "w": "c", "c": "b", "b": "a", "k": "j", "j": "h", "r": "4"}


def mirror(half: str) -> str:
    """Linke Haelfte (8 Spalten) -> volles Bild; rechte Seite gespiegelt und eine Stufe dunkler."""
    rows = half.strip("\n").split("\n")
    out = []
    for row in rows:
        if len(row) != 8:
            raise ValueError(f"Halbzeile hat {len(row)} statt 8 Zeichen: {row!r}")
        out.append(row + "".join(_DARKER.get(c, c) for c in reversed(row)))
    if len(out) != 16:
        raise ValueError(f"{len(out)} Zeilen statt 16")
    return "\n".join(out)


def icon(rows: str, pal: dict, name: str) -> Tex:
    return art(rows, pal, name)


def spade(pal, tip, base, half=2.6, core=None, neck=0.7, cap=0.22):
    """Schaufelblatt als Traene zwischen Stielende `base` und Spitze `tip`: schmaler Hals (`neck`), breit zum Ende
    (`half`), abgerundete Kappe auf den letzten `cap` der Laenge.

    Licht kommt von oben links: die linke obere Kante ist hell, die rechte untere dunkel, in der Mitte laeuft eine
    dunklere Rippe. `core` = (x, y, Zeichen) setzt einen Kristall oder Kern auf das Blatt.
    """
    t = Tex(16, 16)
    ax, ay = tip[0] - base[0], tip[1] - base[1]
    ln = (ax * ax + ay * ay) ** 0.5
    ux, uy = ax / ln, ay / ln
    px, py = -uy, ux                                  # Senkrechte, zeigt nach rechts unten
    mask = {}
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - base[0], y + 0.5 - base[1]
            u, v = dx * ux + dy * uy, dx * px + dy * py
            s = u / ln
            if s < 0 or s > 1:
                continue
            w = neck + (half - neck) * min(1.0, s / 0.65) ** 1.4
            if s > 1 - cap:
                w *= (1 - ((s - (1 - cap)) / cap) ** 2.2) ** 0.5
            if abs(v) <= w:
                mask[(x, y)] = (v, w)
    for (x, y), (v, w) in mask.items():
        r = v / max(w, 0.1)
        tone = "5" if r < -0.55 else "4" if r < -0.1 else "3" if r < 0.5 else "2"
        if abs(r) < 0.12:
            tone = "3"
        if (x - 1, y) not in mask or (x, y - 1) not in mask:
            tone = "5" if r < 0.2 else tone
        t.set(x, y, pal[tone])
    if core:
        t.set(core[0], core[1], pal[core[2]])
    return t


def shovel(pal, core=None, rivet=None, tip=(13.4, 2.6), base=(8.8, 7.2), half=3.0, neck=0.9, cap=0.3):
    t = spade(pal, tip=tip, base=base, half=half, core=core, neck=neck, cap=cap)
    t.line(1, 14, 8, 7, pal["k"])
    t.line(2, 14, 9, 7, pal["j"])
    t.line(0, 15, 1, 14, pal["h"])
    t.line(1, 15, 2, 14, pal["h"])
    if rivet:
        t.set(8, 8, pal[rivet])
        t.set(9, 8, pal["3"])
    t.outline(pal["#"])
    return t


# ================================================================== Shard-Ruestung
SHARD_HELMET = mirror("""
........
.......w
......#c
....###b
...#5554
..#55544
..#54433
..#43#11
..#43#11
..#32#1#
..#2###.
...#....
........
........
........
........
""")

SHARD_CHEST = mirror("""
........
........
.#####..
.#5554#.
.#4443##
.#3332#w
.#1111#c
.###541c
...#431b
...#431b
...#331a
...#r222
...#####
....#32#
.....###
........
""")

SHARD_LEGS = mirror("""
........
........
...#####
...#5r55
...#####
...#4444
...#c433
...#43#.
...#54#.
...#c3#.
...#32#.
...#43#.
...#32#.
...####.
........
........
""")

SHARD_BOOTS = mirror("""
........
........
........
..####..
..#5c4#.
..#54b#.
..#443#.
..#443#.
..#432#.
.#4432#.
#54432#.
#543221.
#######.
........
........
........
""")

# ================================================================== Colossus-Ruestung
COLOSSUS_HELMET = mirror("""
........
........
....####
...#5555
..#55544
..#544cc
..#4#111
..#4#111
..#43#1#
..#32###
..###...
........
........
........
........
........
""")

COLOSSUS_CHEST = mirror("""
........
#####...
#55554#.
#44443##
#2222#33
.#4433#a
.#43#3#b
..#.43#c
..#.4##w
...#4#cc
...#3#bb
...#r222
...#####
...#2112
....####
........
""")

COLOSSUS_LEGS = mirror("""
........
........
..######
..#5r555
..######
..#44333
..#4333#
..#433#.
..#rr3#.
..#433#.
..#433#.
..#332#.
..#222#.
..#####.
........
........
""")

COLOSSUS_BOOTS = mirror("""
........
........
........
..#####.
..#555#.
..#44r#.
..#443#.
..#433#.
..#432#.
.#44432.
#544432.
#544432.
#######.
........
........
........
""")

# ================================================================== Werkzeuge (diagonal, Kopf oben rechts)
SHARD_SWORD = """
..............##
.............#w#
............#5c#
...........#5c3#
..........#5c3#.
.........#5b3#..
..##....#5b2#...
..#5#..#5b2#....
...#5##5a2#.....
...#45cc2#......
....#4bb2##.....
...#j#5#42#.....
..#kj#..#2#.....
.#kh#....##.....
#wc#............
##..............
"""

COLOSSUS_SWORD = """
.............###
............#5c#
...........#5c3#
..........#5c32#
.........#5c32#.
........#5b32#..
.##....#5b32#...
.#5#..#5a32#....
.#45##5432#.....
..#45c5432#.....
..#r4cb32##.....
...#j#432r#.....
..#kj#.#22#.....
.#kh#...###.....
#cw#............
###.............
"""

SHARD_PICKAXE = """
................
................
.....######.....
...##w55443##...
..#wc######432#.
...##....#k##32#
........#kj#.#3#
.......#kj#..#3#
......#kj#...#3#
.....#kj#....#c#
....#kj#.....#w#
...#kj#.......#.
..#kj#..........
.#kh#...........
.##.............
................
"""

COLOSSUS_PICKAXE = """
................
....######......
...#555544##....
..#4c#####33#...
..#4#....#43#...
...#....#k#3#...
.......#kj#43#..
......#kj#.#43#.
.....#kj#..#r3#.
....#kj#...#cb#.
...#kj#....####.
..#kj#..........
.#kh#...........
#hh#............
.##.............
................
"""

SHARD_AXE = """
................
........####....
.......#5w55#...
.......#5c4443#.
.......#5c44433#
.......#5b444322
.......##44443#.
.......#k#4332#.
......#kj##22#..
.....#kj#.###...
....#kj#........
...#kj#.........
..#kj#..........
.#kh#...........
.#h#............
.##.............
"""

COLOSSUS_AXE = """
................
........####....
.......#5555#...
.......#5c4443#.
.......#5b44433#
.......#4b444322
.......##44443#.
.......#k#4332#.
......#kj##22#..
.....#kj#.###...
....#kj#........
...#kj#.........
..#kj#..........
.#kh#...........
.#h#............
.##.............
"""


# ================================================================== Spezialwaffen
QUEEN_P = {
    "#": hx("#120a22"), "1": hx("#26185a"), "2": hx("#3d2a86"), "3": hx("#5d44b8"), "4": hx("#8a70e6"), "5": hx("#c0aaff"),
    "a": hx("#2b5a9a"), "b": hx("#5fa0e0"), "c": hx("#a6dcff"), "w": hx("#f0fbff"),
    "h": hx("#2a1c30"), "j": hx("#47345a"), "k": hx("#6d5688"), "r": hx("#d9ccff"),
}

QUEENS_SHARDBLADE = """
..............##
.............#w#
...........#5w3#
..........#5c33#
.........#5c43#.
........#5c4#3#.
..##...#5c43#...
.#5c#.#5c43#....
..#c5##5b43#....
...#5c#b443#....
...#45cb33#.....
...#j#4#43#.....
..#kj#.##.......
.#kh#...........
#wc#............
##..............
"""

# Colossus-Gauntlet: geballte Faust von vorn, vier Fingerplatten, Daumen links, Manschette mit Kern
COLOSSUS_GAUNTLET = """
................
...##.##.##.##..
..#55#55#44#44#.
..#54#54#43#43#.
.##54#54#43#33#.
#5#44#44#33#32#.
#4#4444433333#..
#4##44444333##..
.#4#333333322#..
..##r#######r#..
..#5555555444#..
..#4444cc3333#..
..#33#bbbb#22#..
..#2##aaaa##1#..
...##########...
................
"""

# Herz der Weite: symmetrisches Kristallherz in der Palette der Koenigin, ein Eisriss laeuft durch den Kern
HEART_HALF = mirror("""
........
........
..####..
.#5ww4#.
#5ww444#
#5w54443
#5544433
#4444333
.#444332
.#44332.
..#4332.
...#332.
....#32.
.....#2.
......#1
.......#
""")


# Kristallbogen: Wurfarme aus dunklem Violettstahl, Spitzen und Griffstein aus Cyan-Kristall
BOW_P = dict(SHARD_P)
BOW_P.update({"a": COLOSSUS_P["a"], "b": COLOSSUS_P["b"], "c": COLOSSUS_P["c"], "w": COLOSSUS_P["w"], "s": hx("#d8cdf5")})


def bow(pull: int, arrow: bool) -> Tex:
    """Bogen von oben rechts nach unten links, nach oben links gewoelbt. `pull` 0-2 spannt die Sehne weiter."""
    t = Tex(16, 16)
    a, b, c = (13.0, 2.0), (2.0, 13.0), (1.5, 1.5)
    pts = []
    for k in range(0, 61):
        u = k / 60
        x = (1 - u) ** 2 * a[0] + 2 * u * (1 - u) * c[0] + u * u * b[0]
        y = (1 - u) ** 2 * a[1] + 2 * u * (1 - u) * c[1] + u * u * b[1]
        p = (int(round(x)), int(round(y)), u)
        if not pts or (pts[-1][0], pts[-1][1]) != (p[0], p[1]):
            pts.append(p)
    for (x, y, u) in pts:
        grip = 0.42 <= u <= 0.58
        tip = u < 0.1 or u > 0.9
        outer = BOW_P["c"] if tip else BOW_P["k"] if grip else BOW_P["5"] if x + y < 14 else BOW_P["4"]
        inner = BOW_P["b"] if tip else BOW_P["j"] if grip else BOW_P["3"]
        t.set(x, y, outer)
        t.set(x + 1, y + 1, inner)
    mid = min(pts, key=lambda p: abs(p[2] - 0.5))
    t.set(mid[0], mid[1], BOW_P["c"])                      # Griffstein
    sx, sy = (a[0] + b[0]) / 2 + pull * 1.9, (a[1] + b[1]) / 2 + pull * 1.9
    t.line(int(a[0]) + 1, int(a[1]) + 1, int(round(sx)), int(round(sy)), BOW_P["s"])
    t.line(int(round(sx)), int(round(sy)), int(b[0]) + 1, int(b[1]) + 1, BOW_P["s"])
    if arrow:
        px, py = int(round(sx)), int(round(sy))
        t.line(px, py, px - 8, py - 8, BOW_P["k"])
        t.set(px - 9, py - 9, BOW_P["w"])
        t.set(px - 8, py - 9, BOW_P["c"])
        t.set(px - 9, py - 8, BOW_P["c"])
        t.set(px - 1, py - 1, BOW_P["b"])
        t.set(px - 2, py - 2, BOW_P["b"])
    t.outline(BOW_P["#"])
    return t


def heart() -> Tex:
    t = icon(HEART_HALF, QUEEN_P, "heart_of_the_expanse")
    for (x, y, c) in ((7, 5, "c"), (8, 6, "w"), (7, 7, "c"), (8, 8, "b"), (7, 9, "a"), (6, 4, "w"), (9, 7, "b")):
        t.set(x, y, QUEEN_P[c])
    return t


def build_all():
    out = {}
    for prefix, pal, maps in (
            ("shard", SHARD_P, (SHARD_HELMET, SHARD_CHEST, SHARD_LEGS, SHARD_BOOTS,
                                SHARD_SWORD, SHARD_PICKAXE, SHARD_AXE, None)),
            ("colossus", COLOSSUS_P, (COLOSSUS_HELMET, COLOSSUS_CHEST, COLOSSUS_LEGS, COLOSSUS_BOOTS,
                                      COLOSSUS_SWORD, COLOSSUS_PICKAXE, COLOSSUS_AXE, None))):
        for part, rows in zip(("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel"), maps):
            if rows is not None:
                out[f"item/{prefix}_{part}"] = icon(rows, pal, f"{prefix}_{part}")
        out[f"item/{prefix}_shovel"] = shovel(pal, core=(12, 4, "c"), rivet="r")
    out["item/queens_shardblade"] = icon(QUEENS_SHARDBLADE, QUEEN_P, "queens_shardblade")
    out["item/colossus_gauntlet"] = icon(COLOSSUS_GAUNTLET, COLOSSUS_P, "colossus_gauntlet")
    out["item/heart_of_the_expanse"] = heart()
    out["item/crystal_bow"] = bow(0, False)
    for i in range(3):
        out[f"item/crystal_bow_pulling_{i}"] = bow(i, True)
    return out


def main():
    imgs = build_all()
    for name, tex in imgs.items():
        save_png(tex, name)
    print(f"gear: {len(imgs)} Icons")


if __name__ == "__main__":
    main()
