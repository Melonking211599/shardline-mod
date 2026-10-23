"""Block-Texturen (16x16) der Shardline-Mod. Aufruf: python tools/assetgen/blocks.py"""
from __future__ import annotations

import math

from common import (ASH, BONE, CLEAR, CWOOD, CYAN, LILAC, MAGENTA, OUTLINE, SHARD, SLATE, TEAL, VIOLET,
                    Tex, clamp, hx, mix, nz, nz_tile, ramp, save_png, vnoise, with_alpha, TEX)

# Splitterstein: Schiefer mit violettem Stich (5 Stufen)
SHARDSTONE = [mix(SLATE[i], VIOLET[min(i, 4)], 0.45) for i in range(5)]
# Tiefenschiefer der Oberwelt
DEEPSLATE = [hx(c) for c in ("#0f0e14", "#191820", "#25232e", "#33303f", "#443f55")]
BARK = [hx(c) for c in ("#171126", "#251b3d", "#352858", "#493a77", "#64529a")]
MORTAR = SLATE[0]


# ------------------------------------------------------------------ Bausteine
def stone_base(t: Tex, seed: int, r, bias=2.0, spread=1.6, ox=0, oy=0, w=16, h=16):
    """Weicher, kachelnder Steinuntergrund aus zwei Rauschebenen."""
    for y in range(h):
        for x in range(w):
            coarse = vnoise((x + ox) / 3.0, (y + oy) / 3.0, seed, period=16 // 3 + 1 if False else 0)
            fine = nz_tile(x + ox, y + oy, 16, seed + 7)
            v = bias + (coarse - 0.5) * spread * 1.6 + (fine - 0.5) * 1.1
            t.set(x + ox, y + oy, ramp(r, v))


def stone_tile(seed: int, r, bias=2.0, spread=1.6) -> Tex:
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            # periodisches Value-Noise: Gitter 4x4 ueber 16 Pixel
            coarse = vnoise(x / 4.0, y / 4.0, seed, period=4)
            fine = nz_tile(x, y, 16, seed + 7)
            v = bias + (coarse - 0.5) * spread * 1.8 + (fine - 0.5) * 1.0
            t.set(x, y, ramp(r, v))
    return t


def crystal_glints(t: Tex, seed: int, count: int, color_a, color_b=None):
    """Kleine 1-2px Kristallglitzer."""
    for i in range(count):
        x = int(nz(i, 1, seed) * 14) + 1
        y = int(nz(i, 2, seed) * 14) + 1
        t.set(x, y, color_a)
        if color_b is not None:
            t.set(x + 1, y, color_b)


def bevel(t: Tex, light=1, dark=1):
    """Oben links heller, unten rechts dunkler (Lichtquelle links oben)."""
    def shift(c, d):
        f = 1.0 + 0.11 * d
        return (clamp(int(c[0] * f), 0, 255), clamp(int(c[1] * f), 0, 255), clamp(int(c[2] * f), 0, 255), c[3])
    for i in range(16):
        t.set(i, 0, shift(t.get(i, 0), light))
        t.set(0, i, shift(t.get(0, i), light))
        t.set(i, 15, shift(t.get(i, 15), -dark))
        t.set(15, i, shift(t.get(15, i), -dark))


def facets(seed: int, r, count=7, line_drop=1, light_bias=0.0) -> Tex:
    """Facettierter Edelstein-Block: Voronoi-Flaechen mit Lichtkante."""
    pts = []
    for i in range(count):
        pts.append((nz(i, 0, seed) * 16.0, nz(i, 1, seed) * 16.0, nz(i, 2, seed)))
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            ds = sorted(((x + 0.5 - px) ** 2 + (y + 0.5 - py) ** 2, k) for k, (px, py, _s) in enumerate(pts))
            d1, k1 = ds[0]
            d2, k2 = ds[1]
            px_, py_, sh = pts[k1]
            # Flaechen links oben sind heller
            lit = 1.0 - (px_ + py_) / 32.0
            idx = 0.8 + sh * 1.5 + lit * 0.8 + light_bias
            if math.sqrt(d2) - math.sqrt(d1) < 0.85:
                idx -= line_drop + 0.9
            idx = min(idx, 3.45)
            t.set(x, y, ramp(r, idx))
    return t


def brick_pattern(t: Tex, r, seed: int, mortar=MORTAR):
    """4 Reihen aus 8px breiten Ziegeln, jede zweite Reihe um 4px versetzt."""
    for row in range(4):
        y0 = row * 4
        off = 0 if row % 2 == 0 else 4
        for bx in range(-1, 3):
            x0 = bx * 8 + off
            tone = 1.6 + nz(bx + 5, row, seed) * 1.5
            for j in range(4):
                for i in range(8):
                    x, y = x0 + i, y0 + j
                    if not (0 <= x < 16):
                        continue
                    if j == 3 or i == 7:
                        t.set(x, y, mortar)
                        continue
                    v = tone + (nz_tile(x, y, 16, seed + 3) - 0.5) * 0.9
                    if j == 0:
                        v += 0.8
                    if i == 0:
                        v += 0.4
                    t.set(x, y, ramp(r, v))


def disc(t: Tex, cx: float, cy: float, rx: float, ry: float, fn):
    for y in range(t.h):
        for x in range(t.w):
            dx = (x + 0.5 - cx) / rx
            dy = (y + 0.5 - cy) / ry
            if dx * dx + dy * dy <= 1.0:
                c = fn(x, y, dx, dy)
                if c is not None:
                    t.set(x, y, c)


def crystal_spire(t: Tex, x_l: int, x_r: int, y_base: int, tip_x: int, tip_y: int, r):
    """Sechskantkristall: Schaft mit heller linker und dunkler rechter Kante und spitzer Kuppe."""
    h = y_base - tip_y
    shaft_top = tip_y + max(3, h // 3)
    w = x_r - x_l + 1
    for y in range(tip_y, y_base + 1):
        if y < shaft_top:
            f = (y - tip_y) / max(1, shaft_top - tip_y)
            lx = int(round(tip_x + (x_l - tip_x) * f))
            rx = int(round(tip_x + (x_r - tip_x) * f))
        else:
            lx, rx = x_l, x_r
        for x in range(lx, rx + 1):
            span = max(1, rx - lx)
            u = (x - lx) / span
            if u < 0.34:
                v = 3.2
            elif u < 0.72:
                v = 2.2
            else:
                v = 1.0
            if y < shaft_top:
                v += 0.6 if x <= tip_x else -0.2
            if y > y_base - 1:
                v -= 0.8
            t.set(x, y, ramp(r, v))
    t.set(tip_x, tip_y, ramp(r, 4))
    if w >= 3:
        t.set(x_l + 1, shaft_top + 1, ramp(r, 4))


# ------------------------------------------------------------------ Steine
def shardstone():
    t = stone_tile(11, SHARDSTONE, bias=2.0)
    # violette Kristalladern, diagonal von links oben nach rechts unten
    for (x, y) in ((2, 3), (3, 4), (4, 5), (10, 9), (11, 10), (12, 11), (6, 13)):
        t.set(x, y, VIOLET[3])
        t.set(x + 1, y, VIOLET[2])
    crystal_glints(t, 5, 3, CYAN[2], CYAN[0])
    for (x, y) in ((13, 2), (5, 10)):
        t.set(x, y, LILAC[1])
    return t


def shardstone_bricks():
    t = Tex(16, 16)
    brick_pattern(t, SHARDSTONE, 21)
    for (x, y) in ((2, 1), (11, 5), (6, 9), (13, 13)):
        t.set(x, y, VIOLET[3])
    return t


def cracked_shardstone_bricks():
    t = Tex(16, 16)
    brick_pattern(t, SHARDSTONE, 23)
    crack = [(3, 0), (3, 1), (4, 2), (4, 3), (5, 4), (5, 5), (6, 6), (6, 7), (5, 8), (5, 9), (4, 10), (4, 11),
             (9, 8), (10, 9), (10, 10), (11, 11), (12, 12), (12, 13), (13, 14)]
    for (x, y) in crack:
        t.set(x, y, SLATE[0])
    for (x, y) in ((5, 5), (6, 7), (10, 10), (12, 13)):
        t.set(x, y, CYAN[1])
    t.set(6, 6, CYAN[2])
    return t


def ore_clusters(t: Tex, spots, r):
    """Kleine Erzbrocken: je ein Dreier-Block mit Licht, Mittelton, Schatten und Glanzpunkt."""
    shapes = (
        ((0, 0, 3), (1, 0, 3), (0, 1, 2), (1, 1, 2), (2, 1, 1), (1, 2, 1), (2, 2, 0)),
        ((0, 0, 2), (1, 0, 3), (2, 0, 2), (0, 1, 2), (1, 1, 4), (2, 1, 1), (1, 2, 1)),
        ((1, 0, 3), (0, 1, 2), (1, 1, 3), (2, 1, 1), (0, 2, 1), (1, 2, 1)),
    )
    for k, (sx, sy) in enumerate(spots):
        for (dx, dy, v) in shapes[k % 3]:
            t.set(sx + dx, sy + dy, r[v])
        t.set(sx - 1 if k % 2 else sx + 3, sy + 1, with_alpha(SLATE[0], 255) if (sx + sy) % 3 == 0 else t.get(sx - 1, sy + 1))


def shard_ore():
    t = stone_tile(31, SHARDSTONE, bias=1.8)
    ore_clusters(t, [(2, 2), (9, 1), (5, 8), (11, 9), (1, 12)], SHARD)
    return t


def deepslate_shard_ore():
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            band = vnoise(x / 8.0, y / 1.6, 41, period=2) if False else vnoise(x / 4.0, y / 1.5, 41, period=4)
            fine = nz_tile(x, y, 16, 43)
            t.set(x, y, ramp(DEEPSLATE, 1.7 + (band - 0.5) * 2.6 + (fine - 0.5) * 0.9))
    ore_clusters(t, [(1, 3), (8, 2), (4, 9), (11, 8), (9, 12)], SHARD)
    return t


def shard_block():
    t = facets(51, SHARD, count=10, line_drop=1, light_bias=0.0)
    bevel(t, 2, 2)
    for (x, y) in ((3, 3), (10, 4), (7, 11)):
        t.set(x, y, SHARD[4])
    return t


def glow_crystal_block():
    t = facets(61, CYAN, count=10, line_drop=1, light_bias=0.0)
    bevel(t, 1, 2)
    for (x, y) in ((4, 3), (11, 6), (8, 12)):
        t.set(x, y, CYAN[4])
    return t


def ash_block():
    t = stone_tile(71, ASH, bias=1.9, spread=1.3)
    for (x, y) in ((2, 2), (9, 4), (5, 11), (12, 9), (14, 14), (1, 8)):
        t.set(x, y, ASH[4])
    for (x, y) in ((6, 3), (13, 6), (3, 13), (10, 12)):
        t.set(x, y, ASH[0])
    for (x, y) in ((8, 8), (12, 1)):
        t.set(x, y, MAGENTA[0])
    return t


# ------------------------------------------------------------------ Moos
def crystal_moss():
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            coarse = vnoise(x / 4.0, y / 4.0, 81, period=4)
            fine = nz_tile(x, y, 16, 83)
            t.set(x, y, ramp(TEAL, 2.0 + (coarse - 0.5) * 2.4 + (fine - 0.5) * 1.3))
    for (x, y) in ((3, 4), (11, 2), (7, 10), (13, 12), (1, 13)):
        t.set(x, y, TEAL[4])
    for (x, y) in ((5, 7), (12, 7)):
        t.set(x, y, CYAN[3])
    return t


def crystal_moss_side():
    t = stone_tile(31, SHARDSTONE, bias=1.7)
    for x in range(16):
        depth = 3 + int(nz(x, 9, 91) * 3.4)
        if x % 5 == 2:
            depth += 1
        for y in range(depth):
            v = 2.4 + (nz(x, y, 93) - 0.5) * 1.6 - y * 0.12
            t.set(x, y, ramp(TEAL, v))
        t.set(x, depth, TEAL[0] if nz(x, 5, 95) > 0.35 else TEAL[1])
    for (x, y) in ((4, 1), (11, 2)):
        t.set(x, y, TEAL[4])
    return t


# ------------------------------------------------------------------ Kristallholz
def crystal_log():
    t = Tex(16, 16)
    grooves = (0, 4, 8, 12, 15)
    for x in range(16):
        for y in range(16):
            colvar = nz(x, 0, 101)
            v = 2.0 + (colvar - 0.5) * 1.2 + (nz_tile(x, y, 16, 103) - 0.5) * 1.0
            if x in grooves:
                v = 0.4 + nz(x, y, 105) * 0.8
            elif x - 1 in grooves:
                v += 0.9
            elif x + 1 in grooves:
                v -= 0.5
            t.set(x, y, ramp(BARK, v))
    for (x, y) in ((4, 3), (4, 4), (8, 10), (8, 11), (12, 6), (0, 13)):
        t.set(x, y, CYAN[1])
    t.set(4, 3, CYAN[2])
    t.set(8, 10, CYAN[2])
    return t


def crystal_log_top():
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = max(abs(dx), abs(dy))  # quadratische Ringe wie bei Vanilla
            if d >= 6.5:
                v = 1.6 + (nz_tile(x, y, 16, 111) - 0.5) * 1.2
                t.set(x, y, ramp(BARK, v))
                continue
            ring = int(math.hypot(dx, dy) * 0.9)
            base = 2.3 if ring % 2 == 0 else 1.5
            base += (nz_tile(x, y, 16, 113) - 0.5) * 0.6
            base += 0.8 if (dx + dy) < 0 else 0
            t.set(x, y, ramp(CWOOD, base))
    t.set(7, 7, CYAN[3])
    t.set(8, 7, CYAN[2])
    t.set(7, 8, CYAN[2])
    t.set(8, 8, CYAN[1])
    for i in range(16):
        t.set(i, 0, BARK[3])
        t.set(0, i, BARK[3])
        t.set(i, 15, BARK[0])
        t.set(15, i, BARK[0])
    return t


def crystal_planks():
    t = Tex(16, 16)
    seams = {0: 11, 1: 4, 2: 13, 3: 7}
    for board in range(4):
        y0 = board * 4
        tone = 2.0 + (nz(board, 0, 121) - 0.5) * 1.0
        for j in range(4):
            for i in range(16):
                y = y0 + j
                v = tone + (nz_tile(i // 3 + board * 5, y, 16, 123) - 0.5) * 0.8
                if j == 0:
                    v += 0.9
                if j == 3:
                    v = 0.5 + nz(i, y, 125) * 0.5
                if i == seams[board] and j != 3:
                    v = 0.5
                if i == (seams[board] + 1) % 16 and j < 3:
                    v += 0.5
                t.set(i, y, ramp(CWOOD, v))
    for board in range(4):
        t.set(seams[board] - 2 if seams[board] > 2 else seams[board] + 3, board * 4 + 1, CWOOD[0])
    return t


def crystal_leaves():
    t = Tex(16, 16)
    pal = [VIOLET[2], VIOLET[3], LILAC[0], MAGENTA[2], MAGENTA[3]]
    for y in range(16):
        for x in range(16):
            blob = vnoise(x / 2.6, y / 2.6, 131, period=6)
            hole = nz_tile(x, y, 16, 133)
            if blob < 0.22 or (blob < 0.32 and hole < 0.45):
                continue
            v = 1.2 + (blob - 0.4) * 5.0 + (nz_tile(x, y, 16, 135) - 0.5) * 1.2
            t.set(x, y, ramp(pal, v))
    # Kontur: deckende Pixel mit transparentem unteren/rechten Nachbarn werden dunkler
    src = t.img.copy().load()
    for y in range(16):
        for x in range(16):
            if src[x, y][3] == 0:
                continue
            below = src[x, (y + 1) % 16][3] == 0
            right = src[(x + 1) % 16, y][3] == 0
            if below or right:
                t.set(x, y, VIOLET[1] if nz(x, y, 137) > 0.4 else VIOLET[0])
    for (x, y) in ((3, 3), (10, 6), (6, 11), (13, 13)):
        if t.get(x, y)[3]:
            t.set(x, y, MAGENTA[4])
    return t


# ------------------------------------------------------------------ Pflanzen (Kreuzmodelle)
def _plant_finish(t: Tex, edge):
    t.outline(edge)
    return t


def crystal_sapling():
    t = Tex(16, 16)
    # Stamm
    for y in range(8, 16):
        t.set(7, y, BARK[3])
        t.set(8, y, BARK[2])
    t.set(7, 15, BARK[1])
    t.set(8, 15, BARK[0])
    # Kristallkrone aus drei Klumpen
    def blob(cx, cy, rx, ry, seed):
        disc(t, cx, cy, rx, ry, lambda x, y, dx, dy: ramp(
            [VIOLET[2], VIOLET[3], LILAC[0], MAGENTA[2], MAGENTA[3]],
            1.6 - (dx + dy) * 1.5 + (nz(x, y, seed) - 0.5) * 0.9))
    blob(8, 5.5, 4.0, 3.6, 141)
    blob(4.8, 8.2, 3.0, 2.5, 143)
    blob(11.4, 8.0, 3.0, 2.5, 145)
    for (x, y) in ((6, 3), (10, 5), (4, 7)):
        t.set(x, y, MAGENTA[4])
    return _plant_finish(t, VIOLET[0])


def glowbloom():
    t = Tex(16, 16)
    for y in range(7, 16):
        t.set(7, y, TEAL[2])
        t.set(8, y, TEAL[1])
    for (x, y) in ((5, 11), (4, 10), (9, 12), (10, 11), (11, 10)):
        t.set(x, y, TEAL[3])
    for (x, y) in ((6, 12), (5, 12), (9, 13)):
        t.set(x, y, TEAL[1])
    # Glockenbluete: Rautenform mit Cyan-Leuchten
    petals = {
        1: (7, 8), 2: (6, 9), 3: (5, 10), 4: (5, 10), 5: (5, 10), 6: (6, 9), 7: (7, 8),
    }
    for y, (x0, x1) in petals.items():
        for x in range(x0, x1 + 1):
            u = (x - x0) / max(1, x1 - x0)
            v = 3.0 - u * 1.2 - (y - 1) * 0.12
            t.set(x + 1, y + 1, ramp(CYAN, v))
    t.set(8, 3, CYAN[4])
    t.set(7, 4, CYAN[4])
    t.set(8, 2, CYAN[4])
    t.set(9, 6, CYAN[1])
    return _plant_finish(t, TEAL[0])


def shard_fern():
    t = Tex(16, 16)
    stem = [(8, 15), (8, 14), (8, 13), (8, 12), (8, 11), (8, 10), (8, 9), (8, 8), (8, 7), (8, 6), (8, 5), (8, 4)]
    for (x, y) in stem:
        t.set(x, y, TEAL[1])
    # Fiederblaettchen links und rechts, nach oben kuerzer
    for k, y in enumerate(range(13, 3, -2)):
        reach = 5 - k
        for s in (-1, 1):
            for i in range(1, reach + 1):
                x = 8 + s * i
                yy = y - (i // 2 if i > 2 else 0) - (1 if i > 3 else 0)
                t.set(x, yy, ramp(TEAL, 3.2 - i * 0.35 + (0.4 if s < 0 else -0.2)))
    for (x, y) in ((3, 8), (13, 8), (4, 12), (12, 12), (8, 3)):
        t.set(x, y, VIOLET[4] if x != 8 else CYAN[3])
    return _plant_finish(t, TEAL[0])


def glowcap():
    t = Tex(16, 16)
    # Stiel
    for y in range(9, 16):
        t.set(7, y, BONE[4] if y % 3 else BONE[3])
        t.set(8, y, BONE[3])
        t.set(9, y, BONE[2])
    t.set(6, 15, BONE[2])
    t.set(10, 15, BONE[1])
    # Hut: Halbellipse, leuchtend cyan, Rand dunkler
    disc(t, 8.0, 8.5, 6.6, 5.2, lambda x, y, dx, dy: None if y > 9 else ramp(
        CYAN, 2.4 - dx * 0.9 - dy * 1.2 + (nz(x, y, 151) - 0.5) * 0.5))
    for x in range(3, 14):
        if t.get(x, 9)[3]:
            t.set(x, 9, CYAN[1] if x % 2 else CYAN[0])
    for (x, y) in ((6, 4), (9, 5), (11, 7), (5, 7)):
        t.set(x, y, CYAN[4])
    return _plant_finish(t, CYAN[0] if False else hx("#073a47"))


def luminous_berry_bush():
    t = Tex(16, 16)
    for y in range(11, 16):
        t.set(7, y, BARK[3])
        t.set(8, y, BARK[2])
    def blob(cx, cy, rx, ry, seed):
        disc(t, cx, cy, rx, ry, lambda x, y, dx, dy: ramp(
            TEAL, 2.2 - (dx + dy) * 1.2 + (nz(x, y, seed) - 0.5) * 1.0))
    blob(8, 8, 6.4, 4.8, 161)
    blob(5, 10, 3.6, 3.0, 163)
    blob(11.2, 10, 3.6, 3.0, 165)
    for (bx, by) in ((5, 6), (10, 5), (8, 9), (12, 9), (4, 10), (7, 12)):
        t.set(bx, by, MAGENTA[2])
        t.set(bx + 1, by, MAGENTA[1])
        t.set(bx, by + 1, MAGENTA[1])
        t.set(bx + 1, by + 1, MAGENTA[0])
        t.set(bx, by, MAGENTA[4] if (bx + by) % 2 else MAGENTA[3])
    return _plant_finish(t, hx("#062a31"))


def glow_crystal_cluster():
    t = Tex(16, 16)
    crystal_spire(t, 2, 5, 15, 3, 7, CYAN)
    crystal_spire(t, 10, 13, 15, 12, 5, CYAN)
    crystal_spire(t, 5, 10, 15, 7, 1, CYAN)
    crystal_spire(t, 8, 10, 15, 9, 8, CYAN)
    # Mittleren Kristall nochmals vorne zeichnen, damit er die Seitenkristalle ueberdeckt
    crystal_spire(t, 5, 9, 15, 7, 1, CYAN)
    t.outline(hx("#06303b"))
    return t


def crystal_web():
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
def shard_portal_frame():
    t = stone_tile(171, SLATE, bias=1.4, spread=1.2)
    # Rand: helle Kante oben/links, dunkle unten/rechts
    for i in range(16):
        t.set(i, 0, SLATE[4])
        t.set(0, i, SLATE[4])
        t.set(i, 15, SLATE[0])
        t.set(15, i, SLATE[0])
        t.set(i, 1, SLATE[3])
        t.set(1, i, SLATE[3])
        t.set(i, 14, SLATE[1])
        t.set(14, i, SLATE[1])
    # vertieftes Feld
    t.rect(3, 3, 10, 10, VIOLET[0])
    t.frame(3, 3, 10, 10, VIOLET[1])
    # Rauten-Rune
    diamond = [(8, 4), (7, 5), (9, 5), (6, 6), (10, 6), (5, 7), (11, 7), (5, 8), (11, 8), (6, 9), (10, 9),
               (7, 10), (9, 10), (8, 11)]
    for (x, y) in diamond:
        t.set(x, y, MAGENTA[2])
    for (x, y) in ((8, 5), (7, 6), (9, 6), (6, 7), (10, 7)):
        t.set(x, y, MAGENTA[1])
    t.rect(7, 7, 3, 3, CYAN[2])
    t.set(8, 8, CYAN[4])
    t.set(7, 7, CYAN[3])
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        t.set(x, y, CYAN[1])
    return t


def shard_portal() -> Tex:
    """16x128, 8 Frames. Alle Wellen haben ganzzahlige Wellenzahlen, daher laeuft die Animation nahtlos."""
    frames = 8
    t = Tex(16, 16 * frames)
    pal = [VIOLET[1], VIOLET[2], VIOLET[3], LILAC[0], MAGENTA[1], MAGENTA[2], CYAN[1]]
    for f in range(frames):
        ph = 2 * math.pi * f / frames
        for y in range(16):
            for x in range(16):
                u, v = 2 * math.pi * x / 16, 2 * math.pi * y / 16
                s = (math.sin(u + v + ph) * 1.0 + math.sin(2 * u - v - ph) * 0.55 + math.cos(v * 2 + ph * 2) * 0.35)
                n = clamp((s + 1.9) / 3.8, 0.0, 0.999)
                c = pal[int(n * len(pal))]
                a = 170 + int(n * 70)
                t.set(x, f * 16 + y, with_alpha(c, a))
        # wenige Funken wandern nach oben (Periode 8 Frames)
        for k in range(3):
            sx = int(nz(k, 0, 181) * 16)
            sy = (int(nz(k, 1, 181) * 16) - f * 2) % 16
            t.set(sx, f * 16 + sy, hx("#e9fbff", 245))
            t.set(sx, f * 16 + (sy + 1) % 16, with_alpha(CYAN[2], 225))
    return t


# ------------------------------------------------------------------ Altaere und Trophaeen
def altar_side(base, trim, glow_dark, glow, seed, symbol):
    t = stone_tile(seed, base, bias=1.6, spread=1.0)
    for i in range(16):
        for y in (0, 15):
            t.set(i, y, base[0])
        for x in (0, 15):
            t.set(x, i, base[0])
    # Zierband oben und unten
    for x in range(1, 15):
        t.set(x, 1, base[4])
        t.set(x, 2, trim[1] if x % 2 else trim[0])
        t.set(x, 13, trim[0] if x % 2 else trim[1])
        t.set(x, 14, base[1])
    # Rune in der Mitte
    for (x, y) in symbol:
        t.set(x, y, glow)
        t.set(x + 1, y, glow_dark) if (x + 1, y) not in symbol else None
    return t


COLOSSUS_RUNE = [(7, 4), (8, 4), (7, 5), (8, 5), (6, 6), (9, 6), (5, 7), (10, 7), (5, 8), (10, 8), (6, 9), (9, 9),
                 (7, 10), (8, 10), (7, 11), (8, 11)]
QUEEN_RUNE = [(4, 10), (5, 9), (6, 8), (7, 5), (8, 5), (8, 8), (9, 8), (10, 9), (11, 10), (7, 6), (8, 6), (7, 7),
              (8, 7), (5, 6), (10, 6), (6, 7), (9, 7)]


def colossus_altar():
    return altar_side(SLATE, CYAN, CYAN[1], CYAN[3], 191, COLOSSUS_RUNE)


def queen_altar():
    base = [hx(c) for c in ("#0d0816", "#170d28", "#241540", "#352058", "#493078")]
    return altar_side(base, MAGENTA, MAGENTA[1], MAGENTA[3], 193, QUEEN_RUNE)


def altar_top(base, light, mid, dark, seed):
    t = stone_tile(seed, base, bias=1.6, spread=1.0)
    for i in range(16):
        t.set(i, 0, base[0])
        t.set(0, i, base[0])
        t.set(i, 15, base[1])
        t.set(15, i, base[1])
    # Ruenenring
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.2 <= d < 6.3:
                gap = abs(x - 7.5) < 1.2 or abs(y - 7.5) < 1.2
                if gap:
                    t.set(x, y, dark)
                else:
                    t.set(x, y, mid if (x + y) % 2 else dark)
            elif d < 3.4:
                t.set(x, y, mix(dark, light, clamp(1.0 - d / 3.4, 0, 1)))
    t.rect(6, 6, 4, 4, mid)
    t.rect(7, 7, 2, 2, light)
    for (x, y) in ((7, 1), (8, 1), (7, 14), (8, 14), (1, 7), (1, 8), (14, 7), (14, 8)):
        t.set(x, y, mid)
    return t


def colossus_altar_top():
    return altar_top(SLATE, CYAN[4], CYAN[2], CYAN[0], 195)


def queen_altar_top():
    base = [hx(c) for c in ("#0d0816", "#170d28", "#241540", "#352058", "#493078")]
    return altar_top(base, MAGENTA[4], MAGENTA[3], MAGENTA[0], 197)


def colossus_trophy():
    t = facets(201, CYAN, count=7, line_drop=1, light_bias=0.2)
    bevel(t, 1, 1)
    return t


def queen_trophy():
    t = facets(203, MAGENTA, count=7, line_drop=1, light_bias=0.1)
    bevel(t, 1, 1)
    return t


# ------------------------------------------------------------------ Ausgabe
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


def main():
    for name, fn in ALL.items():
        save_png(fn(), name)
    # Animation des Portals: 8 Frames, je 2 Ticks, weich ueberblendet
    meta = TEX / "block" / "shard_portal.png.mcmeta"
    meta.write_text('{\n  "animation": {\n    "frametime": 2,\n    "interpolate": true\n  }\n}\n', encoding="utf-8")
    print(f"blocks: {len(ALL)} Texturen")


if __name__ == "__main__":
    main()
