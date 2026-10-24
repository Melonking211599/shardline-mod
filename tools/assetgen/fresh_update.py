"""Texturen des Inhalts-Updates "Splitterhandwerk" (Bloecke und Items). Aufruf: python tools/assetgen/fresh_update.py

Bloecke: polierter Splitterstein, gemeisselte Ziegel, Kristallholz-Tuer/-Falltuer, Kristallglas (+ Scheibenkante),
Splitterlaterne (Vanilla-Laternen-Layout). Items: Tuer, Laterne, Kristallschleim, Leuchtkappen-Eintopf, Kristallkandis,
Leuchtbeerentoertchen, Resonanzkompass. Treppen, Stufen, Zaeune, Knoepfe und Druckplatten nutzen die Brettertextur.
"""
from __future__ import annotations

from common import BONE, CWOOD, CYAN, LILAC, MAGENTA, SHARD, SLATE, TEAL, VIOLET, Tex, hx, save_png
from fresh_blocks import BARK, SHARDSTONE
from pixel import clusters, ramps

OUT = hx("#10091c")


def sprite(rows: str, pal: dict, w: int = 16, h: int = 16) -> Tex:
    """Wie pixel.art, aber nachsichtig: kurze Zeilen werden mit '.' aufgefuellt, fehlende Zeilen ergaenzt."""
    t = Tex(w, h)
    for y, row in enumerate(rows.strip("\n").split("\n")[:h]):
        for x, ch in enumerate(row[:w]):
            if ch != ".":
                t.set(x, y, pal[ch])
    return t


def fin(t: Tex, base=OUT) -> Tex:
    t.selout(0.38, base, 0.22)
    return t


# ====================================================================== Bloecke
def polished_shardstone() -> Tex:
    """Glatt geschliffen: ruhiger Grundton, wenige Flecken, doppelte Fase am Rand."""
    t = clusters(1401, SHARDSTONE, base=2, lights=3, darks=3, longest=4, horizontal=1.0)
    for i in range(16):
        t.set(i, 0, SHARDSTONE[4])
        t.set(0, i, SHARDSTONE[4])
        t.set(i, 15, SHARDSTONE[0])
        t.set(15, i, SHARDSTONE[0])
        if 0 < i < 15:
            t.set(i, 1, SHARDSTONE[3])
            t.set(1, i, SHARDSTONE[3])
            t.set(i, 14, SHARDSTONE[1])
            t.set(14, i, SHARDSTONE[1])
    return t


CHISELED = """
4444444444444443
4333333333333321
4322222222222221
432.....d.....21
432....dcd....21
432...dcecd...21
432..dce.ecd..21
432.dce.f.ecd.21
432..dce.ecd..21
432...dcecd...21
432....dcd....21
432.....d.....21
432...........21
4322222222222221
4211111111111111
3111111111111110
"""


def chiseled_shardstone_bricks() -> Tex:
    t = clusters(1403, SHARDSTONE, base=1, lights=4, darks=3, longest=3)
    pal = ramps(A=SHARDSTONE, B=SHARD)
    pal["f"] = CYAN[4]
    for y, row in enumerate(CHISELED.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch != ".":
                t.set(x, y, pal[ch])
    return t


DOOR_TOP = """
1111111111111111
1433333331433331
14ggggggg14gggg1
14g.ggg.g14g.gg1
14ggggggg14gggg1
14g.ggg.g14g.gg1
14ggggggg14gggg1
1422222221422221
1111111111111111
1433333333333331
1432222222222321
1432333333332321
1432322222232321
1432323333232321
1432323443232321
1432323443232321
"""

DOOR_BOTTOM = """
1432323443232321
1432323333232321
1432322222232321
1432333333332321
1432222222222321
1433333333333331
1111111111111111
1433333333333331
1432222222222321
1432333333332321
1432322222232321
1432323333232321
1432322222232321
1432333333332321
1422222222222221
1111111111111111
"""


def door_pal():
    pal = {str(i): CWOOD[i] for i in range(5)}
    pal["1"] = BARK[2]
    pal["g"] = CYAN[3]
    return pal


def crystal_door_top() -> Tex:
    t = sprite(DOOR_TOP, door_pal())
    return t


def crystal_door_bottom() -> Tex:
    return sprite(DOOR_BOTTOM, door_pal())


TRAPDOOR = """
1111111111111111
1433333133333331
143..3313..33321
143..3313..33321
1433333133333331
1111111111111111
1433333133333331
1432222132222221
1432222132222221
1433333133333331
1111111111111111
1433333133333331
143..3313..33321
143..3313..33321
1422222122222221
1111111111111111
"""


def crystal_trapdoor() -> Tex:
    return sprite(TRAPDOOR, door_pal())


def crystal_glass() -> Tex:
    """Rand aus hellem Kristall, schraege Glanzstriche, innen durchsichtig (cutout wie Vanilla-Glas)."""
    t = Tex(16, 16)
    for i in range(16):
        t.set(i, 0, CYAN[3])
        t.set(0, i, CYAN[3])
        t.set(i, 15, CYAN[1])
        t.set(15, i, CYAN[1])
    for (x, y) in ((3, 2), (2, 3), (4, 2), (2, 4), (12, 9), (11, 10), (10, 11), (9, 12), (13, 3)):
        t.set(x, y, CYAN[4])
    for (x, y) in ((6, 13), (7, 12), (13, 6)):
        t.set(x, y, LILAC[2])
    t.set(0, 0, CYAN[4])
    return t


def crystal_glass_pane_top() -> Tex:
    t = Tex(16, 16)
    for y in range(16):
        t.set(7, y, CYAN[3])
        t.set(8, y, CYAN[1])
    return t


def shard_lantern() -> Tex:
    """Vanilla-Laternen-Layout: Koerper (0,2)-(6,9), Boden/Deckel (0,9)-(6,15), Kappe (1,0)-(5,2) und (1,10)-(5,14),
    Buegel (11,1)-(14,3) und (11,10)-(14,12)."""
    t = Tex(16, 16)
    metal = [SLATE[0], SLATE[1], SLATE[2], SLATE[3], SLATE[4]]
    # Kappe (Seiten)
    for x in range(1, 5):
        t.set(x, 0, metal[4] if x < 4 else metal[3])
        t.set(x, 1, metal[2])
    # Koerper: Metallrahmen, innen leuchtender Splitter
    for y in range(2, 9):
        for x in range(0, 6):
            if x in (0, 5) or y in (2, 8):
                c = metal[3] if (x == 0 or y == 2) else metal[1]
            else:
                c = SHARD[3] if (x + y) % 3 else SHARD[4]
                if x in (2, 3) and y in (4, 5, 6):
                    c = hx("#fff0ff")
            t.set(x, y, c)
    # Boden/Deckel
    for y in range(9, 15):
        for x in range(0, 6):
            c = metal[2] if (x in (0, 5) or y in (9, 14)) else metal[1]
            t.set(x, y, c)
    for x in range(1, 5):
        for y in range(10, 14):
            t.set(x, y, metal[3] if (x + y) % 2 else metal[2])
    # Buegel
    for x in range(11, 14):
        t.set(x, 1, metal[3])
        t.set(x, 2, metal[1])
        t.set(x, 10, metal[3])
        t.set(x, 11, metal[1])
    return t


# ====================================================================== Items
DOOR_ITEM = """
....11111111....
....14g4g4g1....
....14ggggg1....
....14g4g4g1....
....11111111....
....14333331....
....14322231....
....14323231....
....14322231....
....14333331....
....111111e1....
....14333331....
....14322231....
....14333331....
....11111111....
"""


def item_crystal_door() -> Tex:
    pal = door_pal()
    pal["e"] = CYAN[4]
    return fin(sprite(DOOR_ITEM, pal))


LANTERN_ITEM = """
.......44.......
......4..3......
......3..2......
.....444443.....
....42222221....
....4bcddcb1....
....4cdeedc1....
....4cdeedc1....
....4bcddcb1....
....4bccccb1....
....42222221....
.....333332.....
"""


def item_shard_lantern() -> Tex:
    pal = {str(i): SLATE[i] for i in range(5)}
    pal.update({"b": SHARD[1], "c": SHARD[2], "d": SHARD[3], "e": hx("#fff0ff")})
    return fin(sprite(LANTERN_ITEM, pal))


SLIME = """
................
................
................
......dddd......
....ddeeeddd....
...deeccdddcc...
..dceccccdddcc..
..dcccccccdccb..
..dccccbcccccb..
..dcccbbbccccb..
...ccccbbccbb...
....bbbbbbbb....
......bbbb......
"""


def item_crystal_slime() -> Tex:
    pal = {"b": TEAL[2], "c": CYAN[2], "d": CYAN[3], "e": CYAN[4]}
    t = sprite(SLIME, pal)
    t.set(6, 6, hx("#ffffff"))
    t.set(9, 8, LILAC[1])
    t.set(10, 9, LILAC[2])
    return fin(t)


STEW = """
................
................
................
................
................
.....nmmmn......
...nmmlmmmlmn...
..kmmlmmmmmlmk..
.2kkmmmmmmmmkk1.
.24333333333321.
..2433333333321.
...24333333321..
....243333321...
.....2222221....
"""


def item_glowcap_stew() -> Tex:
    pal = {str(i): BONE[i] for i in range(5)}
    pal.update({"k": TEAL[1], "l": CYAN[4], "m": CYAN[2], "n": CYAN[3]})
    pal["2"] = hx("#5a3a24")
    pal["3"] = hx("#8a5a34")
    pal["4"] = hx("#b07a4a")
    pal["1"] = hx("#3a2414")
    return fin(sprite(STEW, pal))


CANDY = """
................
..........ed....
.........edcd...
........edcbcd..
........dcbabc..
.........dcbc...
..........ed....
.........h......
........h.......
.......h........
......h.........
.....h..........
....h...........
...h............
................
"""


def item_crystal_candy() -> Tex:
    pal = {"a": MAGENTA[1], "b": MAGENTA[2], "c": MAGENTA[3], "d": CYAN[3], "e": CYAN[4], "h": BONE[3]}
    t = sprite(CANDY, pal)
    t.set(10, 3, hx("#ffffff"))
    return fin(t)


TART = """
................
................
................
................
......ab.bc.....
....abcdabcdb...
...bcddcbcddcb..
..2bcdbcddbcdc2.
..433333333333 2
..4444444444442.
...3222222222 2.
....22222222 2..
"""


def item_berry_tart() -> Tex:
    rows = TART.replace(" ", "3")
    pal = {"a": MAGENTA[1], "b": MAGENTA[2], "c": MAGENTA[3], "d": MAGENTA[4]}
    pal.update({"2": hx("#7a4a24"), "3": hx("#b07a3a"), "4": hx("#e0aa5a")})
    return fin(sprite(rows, pal))


COMPASS = """
................
.....444443.....
....42222223....
...4211c11123...
..421111c11112..
..42111cdc1112..
..4211cdedc112..
..4211cdedc112..
..4211bcdcb112..
..42111bcb1112..
..421111b11112..
...4211111123...
....43222221....
.....333332.....
"""


def item_resonance_compass() -> Tex:
    pal = {str(i): SLATE[i] for i in range(5)}
    pal["1"] = VIOLET[0]
    pal.update({"b": SHARD[1], "c": SHARD[2], "d": SHARD[3], "e": CYAN[4]})
    t = sprite(COMPASS, pal)
    for (x, y) in ((4, 4), (11, 4), (4, 11), (11, 11)):
        t.set(x, y, CYAN[2])
    return fin(t)


BLOCKS = {
    "block/polished_shardstone": polished_shardstone,
    "block/chiseled_shardstone_bricks": chiseled_shardstone_bricks,
    "block/crystal_door_top": crystal_door_top,
    "block/crystal_door_bottom": crystal_door_bottom,
    "block/crystal_trapdoor": crystal_trapdoor,
    "block/crystal_glass": crystal_glass,
    "block/crystal_glass_pane_top": crystal_glass_pane_top,
    "block/shard_lantern": shard_lantern,
}

ITEMS = {
    "item/crystal_door": item_crystal_door,
    "item/shard_lantern": item_shard_lantern,
    "item/crystal_slime": item_crystal_slime,
    "item/glowcap_stew": item_glowcap_stew,
    "item/crystal_candy": item_crystal_candy,
    "item/berry_tart": item_berry_tart,
    "item/resonance_compass": item_resonance_compass,
}


def main():
    for name, fn in {**BLOCKS, **ITEMS}.items():
        save_png(fn(), name)
    print(f"fresh_update: {len(BLOCKS)} Block- und {len(ITEMS)} Item-Texturen")


if __name__ == "__main__":
    main()
