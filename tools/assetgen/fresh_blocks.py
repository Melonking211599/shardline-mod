"""Block-Texturen (16x16), Stufe 2: alle von Grund auf neu entworfen. Aufruf: python tools/assetgen/fresh_blocks.py

Teil 1: Steine, Erze, Kristallbloecke, Moos, Asche, Holz. Teil 2 (Pflanzen, Portal, Altaere, Trophaeen) steht in
fresh_blocks2.py. Lichtquelle oben links. Paletten aus common.py.
"""
from __future__ import annotations

import math

from common import ASH, BONE, CWOOD, CYAN, EMBER, LILAC, MAGENTA, OUTLINE, SHARD, SLATE, TEAL, VIOLET, Tex, hx, mix, nz, ramp, save_png
from pixel import art, bevel, clusters, despeckle, patch_idx, patches, ramps, shift, spots, stamp, tnoise, walk

# Splitterstein: Schiefer mit violettem Stich
SHARDSTONE = [mix(SLATE[i], VIOLET[min(i, 4)], 0.45) for i in range(5)]
DEEPSLATE = [hx(c) for c in ("#0f0e14", "#191820", "#25232e", "#33303f", "#443f55")]
BARK = [hx(c) for c in ("#171126", "#251b3d", "#352858", "#493a77", "#64529a")]
GLASS_EDGE = hx("#06303b")


# ------------------------------------------------------------------ Stein
# Varianten: pro Familie drei Seeds (Datei <name>.png = Seed 1, <name>_v2.png, <name>_v3.png). Die Verdrahtung
# (Blockstate mit zufaelligen Modellen) ist Sache des Datagens.
STONE_SEEDS = (11, 23, 37)


def _flecks(t: Tex, seed: int, count: int = 2):
    """Wenige Cyan-Kristallsplitter (2 Pixel, hell oben links) an unregelmaessigen Stellen, nie im Raster."""
    for k, (x, y) in enumerate(spots(seed, count, 6.0)):
        dx, dy = (0, 1) if nz(k, 5, seed) < 0.5 else (1, 0)
        t.set(x, y, CYAN[3])
        t.set((x + dx) % 16, (y + dy) % 16, CYAN[1])


def _hairline(t: Tex, seed: int, ramp_, length: int = 6):
    """Dunkler Haarriss als zusammenhaengender Pfad."""
    x, y = int(nz(1, 1, seed) * 16), int(nz(2, 2, seed) * 16)
    for (px, py) in walk(seed, x, y, length, heading=(1 if nz(3, 3, seed) < 0.5 else -1, 1), wobble=0.25):
        t.set(px, py, ramp_[0])


def shardstone(seed: int = STONE_SEEDS[0]) -> Tex:
    """Schiefer mit violettem Stich: grosse ruhige Flecken in vier Tonstufen, ein Haarriss, zwei Kristallsplitter."""
    t = patches(seed, SHARDSTONE, fractions=(0.0, 0.27, 0.50, 0.21, 0.02), cells=(4, 4), detail=0.3)
    _hairline(t, seed + 2, SHARDSTONE, 6)
    _flecks(t, seed + 3, 1 if seed == STONE_SEEDS[0] else 2)
    return t


NUGGETS = (
    """
.dd.
decb
ccbb
.bb.
""",
    """
.ed
dcb
.bb
""",
    """
.d.
dcb
cbb
.b.
""",
    """
dc
cb
""",
)


def _ore(seed: int, base_ramp, cells) -> Tex:
    """Vanilla-Erz: ruhiges Grundgestein plus zwei Erzadern aus je zwei bis drei Nestern verschiedener Form
    (vier Splitterstufen, Licht oben links). Jedes Nest wirft einen Schattenpixel ins Gestein. Die Adern liegen
    weit auseinander, dazwischen bleibt reines Gestein, damit kein Gitter entsteht."""
    t = patches(seed, base_ramp, fractions=(0.0, 0.27, 0.50, 0.21, 0.02), cells=cells, detail=0.3)
    pal = ramps(B=SHARD)
    anchors = spots(seed + 5, 2, 8.0)
    offsets = ((0, 0, 0), (4, 3, 1), (-1, 5, 3)), ((0, 0, 2), (3, 4, 3), (5, -1, 1))
    for v, (ax, ay) in enumerate(anchors):
        for (dx, dy, shape) in offsets[v][: 3 if v == 0 else 2]:
            rows = NUGGETS[shape].strip("\n").split("\n")
            x, y = ax + dx, ay + dy
            stamp(t, "\n".join(rows), pal, x, y)
            t.set((x + len(rows[-1]) - 1) % 16, (y + len(rows)) % 16, base_ramp[0])
            t.set((x + len(rows[0])) % 16, (y + len(rows) - 1) % 16, base_ramp[0])
    return t


def shard_ore(seed: int = 31) -> Tex:
    return _ore(seed, SHARDSTONE, (4, 4))


def deepslate_shard_ore(seed: int = 41) -> Tex:
    return _ore(seed, DEEPSLATE, (2, 8))


def _brick_tone(idx, x, y, lo=1, hi=3):
    return max(lo, min(hi, idx[y % 16][x % 16]))


def shardstone_bricks(seed: int = 51) -> Tex:
    """Vier Ziegelreihen, jede zweite versetzt. Jeder Ziegel: Oberkante hell, Unterkante dunkel, Fuellung mit sanften
    Flecken (Tonwechsel pro Ziegel statt Rauschen), Fugen in der dunkelsten Stufe."""
    idx = patch_idx(seed, fractions=(0.0, 0.3, 0.45, 0.25, 0.0), cells=(4, 2), detail=0.2)
    t = Tex(16, 16, SHARDSTONE[0])
    for row in range(4):
        off = 0 if row % 2 == 0 else 4
        for k in (-1, 0, 1):
            x0 = off + k * 8
            for j in range(3):
                for i in range(7):
                    x, y = x0 + i, row * 4 + j
                    if not 0 <= x < 16:
                        continue
                    c = _brick_tone(idx, x, y)
                    if j == 0:
                        c += 1
                    elif j == 2 or i == 6:
                        c -= 1
                    t.set(x, y, SHARDSTONE[max(0, min(4, c))])
    return t


def cracked_shardstone_bricks(seed: int = 51) -> Tex:
    t = shardstone_bricks(seed)
    path = walk(seed + 3, 3, 1, 18, heading=(1, 1), wobble=0.35)
    for (x, y) in path:                        # helle Kante links vom Riss, nur in Ziegelflaeche
        left = t.get((x - 1) % 16, y)
        if left in SHARDSTONE and SHARDSTONE.index(left) in (2, 3):
            t.set((x - 1) % 16, y, SHARDSTONE[4])
    for (x, y) in path:
        t.set(x, y, SHARDSTONE[0])
    for k in (3, 5, 12):                       # Kristall schimmert nur in drei Rissstellen
        x, y = path[k]
        t.set(x, y, CYAN[1] if k != 5 else CYAN[2])
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
MOSS_SEEDS = (81, 93, 107)
ASH_SEEDS = (71, 83, 97)
BLADE = """
3.4
.3.
"""


def crystal_moss(seed: int = MOSS_SEEDS[0]) -> Tex:
    """Tuerkisgruen: grosse ruhige Moosflecken, drei kleine Hoehenbueschel, zwei Kristalltropfen."""
    t = patches(seed, TEAL, fractions=(0.03, 0.25, 0.47, 0.22, 0.03), cells=(4, 4), detail=0.35)
    pal = ramps(A=TEAL)
    for (x, y) in spots(seed + 1, 3, 5.0):
        stamp(t, BLADE, pal, x, y)
    for k, (x, y) in enumerate(spots(seed + 2, 2, 7.0)):
        t.set(x, y, CYAN[3] if k == 0 else CYAN[2])
    return t


def crystal_moss_side() -> Tex:
    """Splitterstein darunter (gleiche Flecken wie shardstone), oben eine Moosdecke mit unregelmaessigen Zotteln."""
    t = patches(STONE_SEEDS[0], SHARDSTONE, fractions=(0.07, 0.23, 0.42, 0.22, 0.06), cells=(4, 4), detail=0.3)
    depths = (5, 6, 4, 5, 7, 5, 4, 6, 5, 8, 5, 4, 6, 5, 5, 4)
    for x in range(16):
        d = depths[x]
        for y in range(d):
            tone = 3 if y == 0 else (2 if y < d - 1 else 1)
            if y == 0 and x % 3 == 0:
                tone = 4
            t.set(x, y, TEAL[tone])
        t.set(x, d, TEAL[0])
    t.set(4, 2, TEAL[4])
    t.set(11, 3, TEAL[4])
    return t


def ash_block(seed: int = ASH_SEEDS[0]) -> Tex:
    """Aschland: warmgrauer Boden mit waagerechten Windschlieren, zwei Glutadern in dunklen Rissen (Grau + Orange)."""
    t = patches(seed, ASH, fractions=(0.10, 0.26, 0.38, 0.20, 0.06), cells=(2, 6), detail=0.35, detail_cells=(8, 4))
    for k in range(2):
        x, y = int(nz(k, 1, seed) * 16), int(nz(k, 2, seed) * 16)
        path = walk(seed + k, x, y, 5, heading=(1, 1), wobble=0.3)
        for (px, py) in path:
            t.set(px, py, ASH[0])
        mx, my = path[2]
        t.set(mx, my, EMBER[2])
        nx, ny = path[3]
        t.set(nx, ny, EMBER[1])
    return t


# ------------------------------------------------------------------ Kristallholz
def _band(y: int) -> int:
    """Vier Zeilenbaender; das letzte entspricht dem ersten, damit die Furchen am Kachelrand nicht springen."""
    b = (y // 4) % 4
    return 0 if b == 3 else b


def crystal_log(seed: int = 61, bark=None, veins: bool = True) -> Tex:
    """Rinde aus senkrechten Streifen unterschiedlicher Breite; Furchen wandern um ein Pixel, Streifen haben
    hellere linke und dunklere rechte Kante; zwei Kristalladern laufen in den Furchen (Cyan, nach unten dunkler)."""
    bark = bark or BARK
    t = Tex(16, 16)
    furrows = (0, 5, 9, 13)                     # unterschiedlich breite Streifen (5, 4, 4, 3)
    body = patch_idx(seed, fractions=(0.0, 0.34, 0.44, 0.22, 0.0), cells=(8, 2), detail=0.25, detail_cells=(8, 4))
    for y in range(16):
        shifts = [1 if nz(_band(y), k, seed) < 0.3 else 0 for k in range(4)]
        pos = [(furrows[k] + shifts[k]) % 16 for k in range(4)]
        for x in range(16):
            dist = min(range(4), key=lambda k: (x - pos[k]) % 16)
            off = (x - pos[dist]) % 16
            width = (pos[(dist + 1) % 4] - pos[dist]) % 16
            if off == 0:
                t.set(x, y, bark[0])
            else:
                tone = max(1, min(3, body[y][x]))
                if off == 1:
                    tone += 1
                elif off == width - 1:
                    tone -= 1
                t.set(x, y, bark[max(0, min(4, tone))])
    for (k, y0, length) in (((1, 3, 4), (3, 9, 5)) if veins else ()):   # (Furche, Start-y, Laenge)
        for i in range(length):
            y = (y0 + i) % 16
            x = (furrows[k] + (1 if nz(_band(y), k, seed) < 0.3 else 0)) % 16
            t.set(x, y, CYAN[2] if i < 2 else CYAN[1] if i < length - 1 else CYAN[0])
    return t


def crystal_log_top(seed: int = 67, wood=None, bark=None, core: bool = True) -> Tex:
    """Jahresringe mit leichter Verwerfung, Kern mit Cyan-Kreuz, dunkle Rinde aussen (zwei Pixel breit)."""
    wood, bark = wood or CWOOD, bark or BARK
    t = Tex(16, 16)
    warp = [[(tnoise(x + 0.5, y + 0.5, 2, 2, seed) - 0.5) * 1.6 for x in range(16)] for y in range(16)]
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d >= 6.0:
                tone = 2 if (x * 3 + y * 5) % 7 else 1
                t.set(x, y, bark[tone])
            else:
                ring = int((math.hypot(x - 7.5, y - 7.5) + warp[y][x]) * 0.85)
                tone = 3 if ring % 2 == 0 else 2
                if (x + y) < 12 and ring % 2 == 0:
                    tone = 4
                t.set(x, y, wood[tone])
    for (x, y, c) in (((7, 7, 3), (8, 7, 2), (7, 8, 2), (8, 8, 1), (7, 6, 1), (6, 7, 1)) if core else ()):
        t.set(x, y, CYAN[c])
    for i in range(16):
        t.set(i, 0, bark[3])
        t.set(0, i, bark[3])
        t.set(i, 15, bark[0])
        t.set(15, i, bark[0])
    return t


def crystal_planks(seed: int = 71) -> Tex:
    """Vier Bretter mit eigener Tonlage, waagerechter Maserung (gestreckte Flecken), heller Oberkante, dunkler
    Unterkante, einem versetzten Stossfuge pro Brett und zwei Nagelkoepfen."""
    idx = patch_idx(seed, fractions=(0.0, 0.28, 0.48, 0.24, 0.0), cells=(2, 8), detail=0.3, detail_cells=(4, 8))
    t = Tex(16, 16)
    seams = {0: 11, 1: 4, 2: 13, 3: 7}
    base = [2, 2, 1, 2]
    for board in range(4):
        for j in range(4):
            for i in range(16):
                y = board * 4 + j
                c = base[board] + (idx[y][i] - 2)
                if j == 0:
                    c += 1
                elif j == 3:
                    c = base[board] - 1
                if i == seams[board] and j != 3:
                    c = 0
                t.set(i, y, CWOOD[max(0, min(3, c))])
        for nx in ((seams[board] + 3) % 16, (seams[board] + 12) % 16):
            t.set(nx, board * 4 + 1, CWOOD[0])
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

# Zusatzvarianten (Datagen kann sie als zufaellige Modelle verdrahten, siehe tools/assetgen/STYLEBIBEL.md)
VARIANTS = {
    **{f"block/shardstone_v{n}": (lambda s=s: shardstone(s)) for n, s in enumerate(STONE_SEEDS[1:], 2)},
    **{f"block/crystal_moss_v{n}": (lambda s=s: crystal_moss(s)) for n, s in enumerate(MOSS_SEEDS[1:], 2)},
    **{f"block/ash_block_v{n}": (lambda s=s: ash_block(s)) for n, s in enumerate(ASH_SEEDS[1:], 2)},
    "block/shard_ore_v2": lambda: shard_ore(131),
    "block/deepslate_shard_ore_v2": lambda: deepslate_shard_ore(141),
}


def main():
    import fresh_blocks2
    for name, fn in {**ALL, **VARIANTS, **fresh_blocks2.ALL, **fresh_blocks2.VARIANTS}.items():
        save_png(fn(), name)
    fresh_blocks2.extras()
    print(f"fresh_blocks: {len(ALL) + len(fresh_blocks2.ALL)} Texturen, {len(VARIANTS) + len(fresh_blocks2.VARIANTS)} Varianten")


if __name__ == "__main__":
    main()
