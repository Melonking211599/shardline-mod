"""Block-Texturen (16x16), Stufe 2: alle von Grund auf neu entworfen. Aufruf: python tools/assetgen/fresh_blocks.py

Teil 1: Steine, Erze, Kristallbloecke, Moos, Asche, Holz. Teil 2 (Pflanzen, Portal, Altaere, Trophaeen) steht in
fresh_blocks2.py. Lichtquelle oben links. Paletten aus common.py.
"""
from __future__ import annotations

import math

from common import ASH, BONE, CWOOD, CYAN, LILAC, MAGENTA, OUTLINE, SHARD, SLATE, TEAL, VIOLET, Tex, hx, mix, nz, ramp, save_png
from pixel import art, bevel, clusters, ramps, shift, stamp

# Splitterstein: Schiefer mit violettem Stich
SHARDSTONE = [mix(SLATE[i], VIOLET[min(i, 4)], 0.45) for i in range(5)]
DEEPSLATE = [hx(c) for c in ("#0f0e14", "#191820", "#25232e", "#33303f", "#443f55")]
BARK = [hx(c) for c in ("#171126", "#251b3d", "#352858", "#493a77", "#64529a")]
GLASS_EDGE = hx("#06303b")


# ------------------------------------------------------------------ Stein
CRACK = """
.0.
.00
..0
"""
NUGGET = """
.dd.
decb
ccbb
.bb.
"""
SPLINTER = """
.c.
cdb
.b.
"""


def shardstone() -> Tex:
    t = clusters(11, SHARDSTONE, base=2, lights=9, darks=8, longest=3)
    pal = ramps(A=SHARDSTONE, B=SHARD, C=CYAN)
    stamp(t, SPLINTER, pal, 3, 2)
    stamp(t, SPLINTER, pal, 10, 10)
    stamp(t, CRACK, pal, 8, 4)
    stamp(t, CRACK, pal, 2, 10)
    t.set(13, 3, CYAN[2])
    t.set(6, 13, CYAN[1])
    return t


def shard_ore() -> Tex:
    t = clusters(31, SHARDSTONE, base=2, lights=8, darks=8, longest=3)
    pal = ramps(A=SHARDSTONE, B=SHARD)
    for (x, y) in ((1, 1), (9, 0), (5, 7), (11, 9), (1, 11)):
        stamp(t, NUGGET, pal, x, y)
        t.set((x + 3) % 16, (y + 3) % 16, SHARDSTONE[0])
    return t


def deepslate_shard_ore() -> Tex:
    t = clusters(41, DEEPSLATE, base=2, lights=7, darks=8, longest=5, horizontal=1.0)
    pal = ramps(A=DEEPSLATE, B=SHARD)
    for (x, y) in ((0, 2), (8, 1), (4, 8), (11, 8), (8, 12)):
        stamp(t, NUGGET, pal, x, y)
        t.set((x + 3) % 16, (y + 3) % 16, DEEPSLATE[0])
    return t


def shardstone_bricks() -> Tex:
    """Vier Ziegelreihen, jede zweite versetzt; Ziegel mit heller Oberkante und dunklerer Unterkante."""
    t = Tex(16, 16, SHARDSTONE[0])
    for row in range(4):
        off = 0 if row % 2 == 0 else 4
        for k in (-1, 0, 1):
            x0 = off + k * 8
            tone = 2 + (1 if (row + k) % 3 == 0 else 0) - (1 if (row * 2 + k) % 5 == 0 else 0)
            for j in range(3):
                for i in range(7):
                    x, y = x0 + i, row * 4 + j
                    if not 0 <= x < 16:
                        continue
                    c = tone
                    if j == 0:
                        c += 1
                    if i == 0:
                        c += 0
                    if j == 2 or i == 6:
                        c -= 1
                    t.set(x, y, SHARDSTONE[max(0, min(4, c))])
    return t


def cracked_shardstone_bricks() -> Tex:
    t = shardstone_bricks()
    dark = SHARDSTONE[0]
    for (x, y) in ((3, 0), (3, 1), (4, 2), (4, 3), (5, 4), (5, 5), (6, 6), (6, 7), (5, 8), (5, 9), (4, 10), (4, 11),
                   (10, 8), (11, 9), (11, 10), (12, 11), (12, 12), (13, 13), (13, 14)):
        t.set(x, y, dark)
    for (x, y) in ((6, 5), (7, 7), (11, 10), (13, 14)):
        t.set(x, y, CYAN[1])
    t.set(7, 6, CYAN[2])
    t.set(12, 12, CYAN[2])
    return t


# ------------------------------------------------------------------ Kristallbloecke
def pyramid_block(ramp_, top, left, right, bottom, center, spark, outer=None) -> Tex:
    """Facettenblock: vier Dreiecksflaechen wie bei einer Pyramide, Kanten hell oben/links und dunkel unten/rechts."""
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            if max(abs(dx), abs(dy)) < 2.9:
                idx = center
            elif abs(abs(dx) - abs(dy)) < 0.6:
                idx = None  # Facettenkante
            elif abs(dx) > abs(dy):
                idx = left if dx < 0 else right
            else:
                idx = top if dy < 0 else bottom
            if idx is None:
                c = shift(ramp_[2], 0.8)
            else:
                c = ramp_[idx]
            t.set(x, y, c)
    for i in range(16):
        t.set(i, 0, ramp_[min(4, top + 1)])
        t.set(0, i, ramp_[min(4, left + 1)])
        t.set(i, 15, shift(ramp_[bottom], 0.7))
        t.set(15, i, shift(ramp_[right], 0.75))
    t.set(0, 0, ramp_[4])
    for (x, y) in spark:
        t.set(x, y, ramp_[4])
    return t


def shard_block() -> Tex:
    return pyramid_block(SHARD, top=3, left=2, right=1, bottom=0, center=2, spark=((6, 6), (7, 6), (6, 7), (11, 3), (3, 11)))


def glow_crystal_block() -> Tex:
    return pyramid_block(CYAN, top=3, left=2, right=1, bottom=1, center=3, spark=((6, 6), (7, 6), (10, 4), (4, 10), (11, 11)))


GLOW_CLUSTER = """
................
.......ee.......
......edcb......
......edcb......
......edcb......
...e..edcb..e...
..edc.edcb.edb..
..edc.edcb.edb..
..dcb.dcbb.dcb..
..dcb.dcbb.dcb..
..dcb.dcbb.dcb..
.edcbbdcbbbdcbb.
.dcbbbdcbbbdcbb.
.dcbbbbbbbbbcbb.
.bbbbbbbbbbbbbb.
................
"""


def glow_crystal_cluster() -> Tex:
    pal = ramps(B=CYAN)
    t = art(GLOW_CLUSTER, pal, "glow_crystal_cluster")
    t.outline(GLASS_EDGE)
    return t


# ------------------------------------------------------------------ Moos und Asche
def crystal_moss() -> Tex:
    t = clusters(81, TEAL, base=2, lights=9, darks=7, longest=3, horizontal=0.5)
    pal = ramps(A=TEAL)
    tuft = """
.4.
434
3.2
"""
    for (x, y) in ((2, 2), (10, 1), (6, 9), (12, 11)):
        stamp(t, tuft, pal, x, y)
    t.set(5, 6, CYAN[3])
    t.set(13, 6, CYAN[2])
    return t


def crystal_moss_side() -> Tex:
    t = clusters(31, SHARDSTONE, base=2, lights=8, darks=8, longest=3)
    # Moosdecke oben mit herabhaengenden Zotteln
    for x in range(16):
        depth = 4 + (1 if x % 3 == 0 else 0) + (2 if x in (2, 7, 12) else 0) - (1 if x in (4, 10) else 0)
        for y in range(depth):
            tone = 3 if y == 0 else (2 if y < depth - 1 else 1)
            if (x + y) % 5 == 0 and y > 0 and y < depth - 1:
                tone = 3
            t.set(x, y, TEAL[tone])
        t.set(x, depth, TEAL[0])
    for (x, y) in ((4, 1), (11, 2)):
        t.set(x, y, TEAL[4])
    return t


def ash_block() -> Tex:
    t = clusters(71, ASH, base=2, lights=10, darks=9, longest=3)
    pal = ramps(A=ASH)
    pebble = """
43
32
"""
    for (x, y) in ((2, 3), (9, 1), (6, 9), (12, 8), (1, 12)):
        stamp(t, pebble, pal, x, y)
    for (x, y) in ((8, 8), (12, 1)):
        t.set(x, y, MAGENTA[0])
    return t


# ------------------------------------------------------------------ Kristallholz
def crystal_log() -> Tex:
    """Rinde aus senkrechten Streifen: dunkle Furche, helle Kante, versetzte Bruchstellen und Kristalladern."""
    t = Tex(16, 16)
    tones = [2, 3, 2, 1]
    for strip in range(4):
        x0 = strip * 4
        base = tones[strip]
        cut = (strip * 5 + 3) % 11 + 2
        for y in range(16):
            seg = 0 if y < cut else 1
            for i in range(4):
                c = base + (1 if seg else 0) * (1 if strip % 2 == 0 else -1)
                if i == 0:
                    c = 0
                elif i == 1:
                    c += 1
                elif i == 3:
                    c -= 1
                if y == cut:
                    c = 0
                t.set(x0 + i, y, BARK[max(0, min(4, c))])
    for (x, y) in ((5, 3), (5, 4), (9, 10), (9, 11), (13, 6), (1, 13)):
        t.set(x, y, CYAN[1])
    t.set(5, 3, CYAN[2])
    t.set(9, 10, CYAN[2])
    return t


def crystal_log_top() -> Tex:
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d >= 6.5:
                c = BARK[2] if (x + y) % 4 else BARK[1]
                t.set(x, y, c)
            else:
                ring = int(math.hypot(x - 7.5, y - 7.5) * 0.9)
                tone = 3 if ring % 2 == 0 else 2
                t.set(x, y, CWOOD[tone] if (x + y) < 15 or ring % 2 else CWOOD[tone - 1])
    for (x, y, c) in ((7, 7, 3), (8, 7, 2), (7, 8, 2), (8, 8, 1)):
        t.set(x, y, CYAN[c])
    for i in range(16):
        t.set(i, 0, BARK[3])
        t.set(0, i, BARK[3])
        t.set(i, 15, BARK[0])
        t.set(15, i, BARK[0])
    return t


def crystal_planks() -> Tex:
    t = Tex(16, 16)
    seams = {0: 11, 1: 4, 2: 13, 3: 7}
    tones = [3, 2, 3, 2]
    for board in range(4):
        for j in range(4):
            for i in range(16):
                y = board * 4 + j
                c = tones[board]
                if j == 0:
                    c += 1
                if j == 3:
                    c = 0
                if i == seams[board] and j != 3:
                    c = 0
                elif i == (seams[board] + 1) % 16 and j < 3:
                    c += 1
                t.set(i, y, CWOOD[max(0, min(4, c))])
        # Astloch / Maserung
        gx = (seams[board] + 6) % 16
        t.set(gx, board * 4 + 1, CWOOD[1])
        t.set((gx + 1) % 16, board * 4 + 1, CWOOD[1])
    return t


ALL = {
    "block/shardstone": shardstone,
    "block/shardstone_bricks": shardstone_bricks,
    "block/cracked_shardstone_bricks": cracked_shardstone_bricks,
    "block/shard_ore": shard_ore,
    "block/deepslate_shard_ore": deepslate_shard_ore,
    "block/shard_block": shard_block,
    "block/glow_crystal_block": glow_crystal_block,
    "block/glow_crystal_cluster": glow_crystal_cluster,
    "block/crystal_moss": crystal_moss,
    "block/crystal_moss_side": crystal_moss_side,
    "block/ash_block": ash_block,
    "block/crystal_log": crystal_log,
    "block/crystal_log_top": crystal_log_top,
    "block/crystal_planks": crystal_planks,
}


def main():
    import fresh_blocks2
    for name, fn in {**ALL, **fresh_blocks2.ALL}.items():
        save_png(fn(), name)
    fresh_blocks2.extras()
    print(f"fresh_blocks: {len(ALL) + len(fresh_blocks2.ALL)} Texturen")


if __name__ == "__main__":
    main()
