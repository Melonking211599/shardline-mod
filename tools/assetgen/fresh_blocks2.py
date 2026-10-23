"""Block-Texturen Teil 2 (Stufe 2, neu): Laub, Pflanzen, Netz, Portal, Altaere, Trophaeen."""
from __future__ import annotations

import math

from common import CYAN, LILAC, MAGENTA, SLATE, TEAL, TEX, VIOLET, Tex, clamp, hx, nz, ramp, with_alpha
from fresh_blocks import GLASS_EDGE, pyramid_block
from pixel import art, clusters, ramps, shift, stamp, _spots_for


# ------------------------------------------------------------------ Laub
LEAF = """
..4.
.434
3432
.32.
"""


def crystal_leaves() -> Tex:
    """Einzelne Blaetter in zwei Schichten (hinten dunkel, vorn hell) mit magentafarbenen Bluetenblaettern."""
    t = Tex(16, 16)
    violet = ramps(A=VIOLET, B=MAGENTA)
    back = {k: (VIOLET[max(0, int(k) - 2)] if k.isdigit() else v) for k, v in violet.items()}
    for (x, y) in _spots_for(131, 11, 1.6):
        stamp(t, LEAF, back, x, y)
    for i, (x, y) in enumerate(_spots_for(163, 12, 2.4)):
        if i % 4 == 3:
            tones = {"4": MAGENTA[3], "3": MAGENTA[2], "2": MAGENTA[1]}
        else:
            tones = {"4": VIOLET[4] if i % 2 else LILAC[0], "3": VIOLET[3], "2": VIOLET[2]}
        stamp(t, LEAF, tones, x, y)
    for (x, y) in ((3, 3), (10, 6), (6, 11), (13, 13)):
        if t.get(x, y)[3]:
            t.set(x, y, MAGENTA[4])
    return t


# ------------------------------------------------------------------ Pflanzen (Kreuzmodelle, transparent)
PLANT_PAL = ramps(A=VIOLET, B=MAGENTA, C=TEAL, D=CYAN)


def _plant(rows, name, edge=hx("#0b1830")) -> Tex:
    t = art(rows, PLANT_PAL, name)
    t.selout(0.38, edge, 0.25)
    return t


SAPLING = """
................
.....2333.......
....234443......
...23444443.....
...34444b443....
...3444444432...
....24444432....
.....222332.....
.......h........
.......h........
.......g........
.....i.g.i......
......igi.......
.......g........
.......g........
................
"""

GLOWBLOOM = """
................
.......o........
......onn.......
.....onmmm......
.....nmmmm......
......mll.......
.......g........
.......g........
....i..h.i......
.....ihhi.......
.......g........
.......g........
.......g........
......hgh.......
................
................
"""

FERN = """
................
.......i........
......ihg.......
...i..ihg..i....
....ihihgig.....
.....ihhg.......
..i..ihhg...i...
...ihihhgig.....
....ihhhg.......
.....ihg.i......
......hg........
......hg........
.......g........
.......g........
.......g........
................
"""

GLOWCAP = """
................
................
.....lmmml......
....lmnmmml.....
...lmmmmmnmml...
..lmmnmmmmmmml..
..lmmmmmmmnmml..
..llllllllllll..
.....cdd2.......
.....cdd2.......
.....cdd2.......
....ccdd22......
................
................
................
................
"""

BERRY_BUSH = """
................
................
....hhiihh......
..hiihhhiihh....
.hiihhhhhhiih...
.hhhbbhhhhhhh...
hhihbdbhhbbhih..
hhhhhhhhhbdbhhh.
hihhbbhhhhhhhhh.
hhhhbdbhhhbbhhi.
.hhhhhhhhhbdbhh.
..hhhgghhhhhhh..
....hgghh.......
.....gg.........
.....gg.........
................
"""


def crystal_sapling() -> Tex:
    return _plant(SAPLING, "sapling")


def glowbloom() -> Tex:
    return _plant(GLOWBLOOM, "glowbloom")


def shard_fern() -> Tex:
    return _plant(FERN, "fern")


def glowcap() -> Tex:
    pal = dict(PLANT_PAL)
    pal.update({"c": hx("#8b79ad"), "d": hx("#b9a9d6"), "2": hx("#5d4c7b")})
    t = art(GLOWCAP, pal, "glowcap")
    t.selout(0.38, hx("#0b1830"), 0.25)
    return t


def luminous_berry_bush() -> Tex:
    t = art(BERRY_BUSH, PLANT_PAL, "berry_bush")
    t.selout(0.38, hx("#0b1830"), 0.25)
    return t


def crystal_web() -> Tex:
    t = Tex(16, 16)
    web = hx("#f1ecff", 235)
    web2 = hx("#bfe9ff", 200)
    cx, cy = 7, 7
    for (x1, y1) in ((0, 0), (15, 0), (0, 15), (15, 15), (7, 0), (8, 15), (0, 7), (15, 8)):
        t.line(cx, cy, x1, y1, web)
    for rad in (3, 6):
        pts = [(cx, cy - rad), (cx + rad, cy - rad // 2 - 1), (cx + rad, cy + rad // 2 + 1), (cx, cy + rad),
               (cx - rad, cy + rad // 2 + 1), (cx - rad, cy - rad // 2 - 1)]
        for k in range(len(pts)):
            a, b = pts[k], pts[(k + 1) % len(pts)]
            t.line(a[0], a[1], b[0], b[1], web2)
    t.set(cx, cy, hx("#ffffff"))
    return t


# ------------------------------------------------------------------ Portal
FRAME_RUNE = [
    "wwwwddwwww",
    "wwwdbbdwww",
    "wwdbbbbdww",
    "wdbbhhbbdw",
    "dbbhjjhbbd",
    "dbbhjjhbbd",
    "wdbbhhbbdw",
    "wwdbbbbdww",
    "wwwdbbdwww",
    "wwwwddwwww",
]


def shard_portal_frame() -> Tex:
    t = clusters(171, SLATE, base=2, lights=8, darks=8, longest=3)
    for i in range(16):
        t.set(i, 0, SLATE[4])
        t.set(0, i, SLATE[4])
        t.set(i, 15, SLATE[0])
        t.set(15, i, SLATE[0])
        t.set(i, 1, SLATE[3])
        t.set(1, i, SLATE[3])
        t.set(i, 14, SLATE[1])
        t.set(14, i, SLATE[1])
    pal = ramps(B=MAGENTA, C=CYAN)
    pal.update({"w": VIOLET[0], "v": VIOLET[1]})
    stamp(t, "\n".join(FRAME_RUNE), pal, 3, 3)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        t.set(x, y, CYAN[1])
    return t


def shard_portal() -> Tex:
    """16x128, 8 Frames. Ganzzahlige Wellenzahlen, daher laeuft die Animation nahtlos. Fuenf klare Stufen statt Verlauf."""
    frames = 8
    t = Tex(16, 16 * frames)
    pal = [VIOLET[1], VIOLET[2], VIOLET[3], LILAC[0], MAGENTA[1], MAGENTA[2], CYAN[1]]
    for f in range(frames):
        ph = 2 * math.pi * f / frames
        for y in range(16):
            for x in range(16):
                u, v = 2 * math.pi * x / 16, 2 * math.pi * y / 16
                s = math.sin(u + v + ph) + 0.55 * math.sin(2 * u - v - ph) + 0.35 * math.cos(2 * v + 2 * ph)
                n = clamp((s + 1.9) / 3.8, 0.0, 0.999)
                c = pal[int(n * len(pal))]
                t.set(x, f * 16 + y, with_alpha(c, 170 + int(n * 70)))
        for k in range(3):
            sx = int(nz(k, 0, 181) * 16)
            sy = (int(nz(k, 1, 181) * 16) - f * 2) % 16
            t.set(sx, f * 16 + sy, hx("#e9fbff", 245))
            t.set(sx, f * 16 + (sy + 1) % 16, with_alpha(CYAN[2], 225))
    return t


# ------------------------------------------------------------------ Altaere und Trophaeen
ALTAR_BASE = [hx(c) for c in ("#0d0816", "#170d28", "#241540", "#352058", "#493078")]

COLOSSUS_RUNE = """
...ii...
..i..i..
.i.jj.i.
i..jj..i
i..jj..i
.i.jj.i.
..i..i..
...ii...
"""

QUEEN_RUNE = """
d..dd..d
dd.dd.dd
dddddddd
dbbbbbbd
.dddddd.
........
...ee...
...ee...
"""


def altar_side(base, trim, rune, rune_pal, seed) -> Tex:
    t = clusters(seed, base, base=2, lights=7, darks=8, longest=3)
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            t.set(x, y, base[0])
    for x in range(1, 15):
        t.set(x, 1, base[4])
        t.set(x, 2, trim[1] if x % 2 else trim[0])
        t.set(x, 13, trim[0] if x % 2 else trim[1])
        t.set(x, 14, base[1])
    art(rune, rune_pal, "rune", tex=t, ox=4, oy=4)
    return t


def colossus_altar() -> Tex:
    pal = ramps(C=CYAN)
    return altar_side(SLATE, CYAN, COLOSSUS_RUNE, pal, 191)


def queen_altar() -> Tex:
    pal = ramps(B=MAGENTA)
    return altar_side(ALTAR_BASE, MAGENTA, QUEEN_RUNE, pal, 193)


def altar_top(base, light, mid, dark, seed) -> Tex:
    t = clusters(seed, base, base=2, lights=7, darks=8, longest=3)
    for i in range(16):
        t.set(i, 0, base[0])
        t.set(0, i, base[0])
        t.set(i, 15, base[1])
        t.set(15, i, base[1])
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.2 <= d < 6.3:
                gap = abs(x - 7.5) < 1.2 or abs(y - 7.5) < 1.2
                t.set(x, y, dark if gap else (mid if (x + y) % 2 else dark))
            elif d < 3.4:
                t.set(x, y, tuple(int(dark[k] + (light[k] - dark[k]) * clamp(1.0 - d / 3.4, 0, 1)) for k in range(4)))
    t.rect(6, 6, 4, 4, mid)
    t.rect(7, 7, 2, 2, light)
    for (x, y) in ((7, 1), (8, 1), (7, 14), (8, 14), (1, 7), (1, 8), (14, 7), (14, 8)):
        t.set(x, y, mid)
    return t


def colossus_altar_top() -> Tex:
    return altar_top(SLATE, CYAN[4], CYAN[2], CYAN[0], 195)


def queen_altar_top() -> Tex:
    return altar_top(ALTAR_BASE, MAGENTA[4], MAGENTA[3], MAGENTA[0], 197)


def colossus_trophy() -> Tex:
    return pyramid_block(CYAN, top=4, left=3, right=2, bottom=1, center=3, spark=((5, 5), (6, 5), (9, 3), (3, 9)))


def queen_trophy() -> Tex:
    return pyramid_block(MAGENTA, top=4, left=3, right=2, bottom=1, center=3, spark=((5, 5), (6, 5), (9, 3), (3, 9)))


ALL = {
    "block/crystal_leaves": crystal_leaves,
    "block/crystal_sapling": crystal_sapling,
    "block/glowbloom": glowbloom,
    "block/shard_fern": shard_fern,
    "block/glowcap": glowcap,
    "block/luminous_berry_bush": luminous_berry_bush,
    "block/crystal_web": crystal_web,
    "block/shard_portal_frame": shard_portal_frame,
    "block/shard_portal": shard_portal,
    "block/colossus_altar": colossus_altar,
    "block/colossus_altar_top": colossus_altar_top,
    "block/queen_altar": queen_altar,
    "block/queen_altar_top": queen_altar_top,
    "block/colossus_trophy": colossus_trophy,
    "block/queen_trophy": queen_trophy,
}


def extras():
    meta = TEX / "block" / "shard_portal.png.mcmeta"
    meta.write_text('{\n  "animation": {\n    "frametime": 2,\n    "interpolate": true\n  }\n}\n', encoding="utf-8")
