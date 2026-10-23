"""Infizierte Bloecke und Story-Bloecke (Stufe 2, von Grund auf neu). Aufruf: python tools/assetgen/fresh_virus.py

Konzept: Der Virus WAECHST. In jedem Block frisst sich ein Netz duenner, diagonaler Wurzeladern durch das Material:
  Rille (dunkel eingegraben) + Hof (angefressenes Material) in der Grundtextur, der leuchtende Kern in <name>_glow.png,
dazu kleine Kristallsplitter, die aus den Adern herauswachsen. Alle Bilder kacheln.
"""
from __future__ import annotations

import json
import math

from common import CYAN, GOLD, IVORY, SKYB, TEX, VGLOW, VIRUS, Tex, clamp, hx, mix, nz, save_png
from pixel import _spots_for, clusters, ramps, shift, stamp

N = 16

INF_STONE = [hx(c) for c in ("#181120", "#281c36", "#3b2a4b", "#51396a", "#6c4f88")]
INF_DIRT = [hx(c) for c in ("#1c0f1c", "#2f1a2e", "#46283f", "#5f3a54", "#7b5169")]
INF_GRASS = [hx(c) for c in ("#17081f", "#2a1138", "#44205a", "#63307f", "#8545a3")]
INF_BARK = [hx(c) for c in ("#140a1a", "#25142c", "#3a2140", "#52305a", "#6e4575")]
INF_LEAF = [hx(c) for c in ("#12051a", "#2a0b36", "#4a1450", "#76206c", "#a43a92")]
INF_SAND = [hx(c) for c in ("#42324c", "#63506e", "#8a7391", "#b098b4", "#d3bdd3")]

_DIR8 = ((1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0), (-1, -1), (0, -1), (1, -1))


# ------------------------------------------------------------------ Adern
def veins(seed: int, paths: int = 3, length: int = 11, branch: float = 0.09, dirs=range(8), allow=None) -> set:
    """Duenne Wurzeladern mit 8 Richtungen, leichten Biegungen und Verzweigungen. Laufen ueber den Rand (kachelt)."""
    out: set = set()
    dirs = list(dirs)
    queue = []
    for i in range(paths):
        queue.append((int(nz(i, 0, seed) * N), int(nz(i, 1, seed) * N), dirs[int(nz(i, 2, seed) * len(dirs)) % len(dirs)], length))
    steps = 0
    while queue and steps < 400:
        x, y, d, left = queue.pop(0)
        prev = None
        while left > 0 and steps < 400:
            steps += 1
            if allow is None or allow(x, y):
                out.add((x, y))
            r = nz(steps, 5, seed)
            if r < 0.18:
                d = (d + 1) % 8
            elif r < 0.36:
                d = (d + 7) % 8
            if dirs and d not in dirs:
                d = min(dirs, key=lambda k: min((k - d) % 8, (d - k) % 8))
            if nz(steps, 8, seed) < branch and left > 4:
                queue.append((x, y, (d + (2 if steps % 2 else 6)) % 8, left // 2 + 2))
            nx, ny = (x + _DIR8[d][0]) % N, (y + _DIR8[d][1]) % N
            if allow is not None and not allow(nx, ny):
                break
            x, y = nx, ny
            left -= 1
    return out


SHARD_GLOW = """
.2.
232
131
"""


def infect(base: Tex, seed: int, vs: set, shards: int = 2, spores: int = 4, tint: float = 0.4, opaque=None, shard_skip=lambda p: False):
    """Legt Rille, Hof und Splitter auf `base` und liefert (base, glow)."""
    glow = Tex(N, N)
    px = base.px
    vp = ramps(A=VIRUS, B=VGLOW)
    halo = set()
    for (x, y) in vs:
        for q in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            q = (q[0] % N, q[1] % N)
            if q not in vs and (opaque is None or opaque(*q)):
                halo.add(q)
    for (x, y) in halo:
        c = mix(px[x, y], VIRUS[2], tint)
        px[x, y] = (int(c[0] * 0.75), int(c[1] * 0.62), int(c[2] * 0.8), c[3])
    for (x, y) in sorted(vs):
        if opaque is not None and not opaque(x, y):
            continue
        px[x, y] = VIRUS[0] if (x + y) % 3 == 0 else VIRUS[1]
        glow.set(x, y, VGLOW[2] if (x * 2 + y * 3) % 7 == 0 else VGLOW[1])
    # Splitter wachsen aus den Adern: leuchtende Kristallspitze mit dunklem Fuss
    pts = sorted(p for p in vs if not shard_skip(p))
    placed = []
    k = 0
    while pts and len(placed) < shards and k < 200:
        p = pts[int(nz(k, 3, seed) * len(pts)) % len(pts)]
        if all(abs(p[0] - q[0]) + abs(p[1] - q[1]) > 5 for q in placed):
            placed.append(p)
        k += 1
    for (x, y) in placed:
        for j, row in enumerate(SHARD_GLOW.strip("\n").split("\n")):
            for i, ch in enumerate(row):
                if ch == ".":
                    continue
                tx, ty = (x + i - 1) % N, (y + j - 2) % N
                if opaque is not None and not opaque(tx, ty):
                    continue
                tone = {"1": 1, "2": 3, "3": 2}[ch]
                px[tx, ty] = VIRUS[3] if ch != "2" else VGLOW[1]
                glow.set(tx, ty, VGLOW[tone])
    # einzelne Sporenfunken neben den Adern
    cand = sorted(halo)
    placed_n = 0
    k = 0
    while placed_n < spores and cand and k < 80:
        p = cand[int(nz(k, 9, seed) * len(cand)) % len(cand)]
        if glow.get(*p)[3] == 0:
            glow.set(p[0], p[1], VGLOW[1])
            placed_n += 1
        k += 1
    return base, glow


# ------------------------------------------------------------------ Grundtexturen
def stone_base(seed, ramp_, base=2):
    return clusters(seed, ramp_, base=base, lights=8, darks=8, longest=3)


def dirt_base(seed, ramp_):
    t = clusters(seed, ramp_, base=2, lights=8, darks=8, longest=3, horizontal=0.5)
    pal = ramps(A=ramp_)
    for (x, y) in _spots_for(seed + 5, 5, 3.4):
        stamp(t, "43\n32", pal, x, y)
    return t


def infected_stone():
    base = stone_base(6001, INF_STONE)
    return infect(base, 6005, veins(6003, paths=2, length=12), shards=3, spores=5)


def infected_dirt():
    base = dirt_base(6101, INF_DIRT)
    return infect(base, 6105, veins(6103, paths=2, length=12), shards=2, spores=4)


BLADE = """
4
3
2
"""


def grass_base(seed, ramp_, rows=range(N)):
    t = clusters(seed, ramp_, base=2, lights=8, darks=8, longest=2, horizontal=0.3)
    for (x, y) in _spots_for(seed + 5, 10, 2.6):
        if y in rows:
            t.set(x, y, ramp_[4])
            t.set(x, (y + 1) % N, ramp_[3])
            t.set(x, (y + 2) % N, ramp_[2])
    return t


def infected_grass_top():
    base = grass_base(6201, INF_GRASS)
    return infect(base, 6205, veins(6203, paths=2, length=12), shards=3, spores=5, tint=0.5)


def infected_grass_side():
    dirt = dirt_base(6301, INF_DIRT)
    base = Tex(N, N)
    fringe = [4 + (1 if nz(x, 0, 6303) > 0.5 else 0) + (1 if nz(x, 1, 6303) > 0.82 else 0) for x in range(N)]
    for y in range(N):
        for x in range(N):
            if y < fringe[x]:
                tone = 3 if y == 0 else (1 if y == fringe[x] - 1 else 2)
                if (x + y) % 5 == 0 and 0 < y < fringe[x] - 1:
                    tone = 3
                base.set(x, y, INF_GRASS[tone])
            else:
                base.set(x, y, dirt.get(x, y))
    vs = veins(6307, paths=2, length=9, allow=lambda x, y: y >= fringe[x] + 1)
    for x in range(N):
        if nz(x, 5, 6309) > 0.74:
            for y in range(fringe[x] - 1, min(N, fringe[x] + 3 + int(nz(x, 6, 6309) * 4))):
                vs.add((x, y))
    return infect(base, 6311, vs, shards=2, spores=3, tint=0.5, shard_skip=lambda p: p[1] > 12)


def bark_strips(ramp_, tones=(2, 3, 2, 1), cuts=(5, 8, 3, 10)):
    t = Tex(N, N)
    for strip in range(4):
        for y in range(N):
            for i in range(4):
                c = tones[strip] + (1 if y > cuts[strip] and strip % 2 == 0 else 0) - (1 if y > cuts[strip] and strip % 2 else 0)
                if i == 0:
                    c = 0
                elif i == 1:
                    c += 1
                elif i == 3:
                    c -= 1
                if y == cuts[strip]:
                    c = 0
                t.set(strip * 4 + i, y, ramp_[max(0, min(4, c))])
    return t


def infected_log():
    base = bark_strips(INF_BARK)
    return infect(base, 6407, veins(6405, paths=2, length=11, dirs=(2, 6, 1, 3, 5, 7)), shards=2, spores=3)


def infected_log_top():
    base = Tex(N, N)
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if max(abs(x - 7.5), abs(y - 7.5)) >= 6.5:
                base.set(x, y, INF_BARK[2] if (x + y) % 4 else INF_BARK[1])
            else:
                base.set(x, y, INF_BARK[3] if int(d * 0.9) % 2 == 0 else INF_BARK[2])
    vs = set()
    for (ang, ln) in ((0.35, 7.5), (2.5, 6.5), (4.5, 7.0)):
        for s in range(int(ln * 2)):
            t = s / 2.0
            x = int(round(7.5 + math.cos(ang) * t - math.sin(ang) * math.sin(t * 1.3 + ang * 3) * 0.7))
            y = int(round(7.5 + math.sin(ang) * t + math.cos(ang) * math.sin(t * 1.3 + ang * 3) * 0.7))
            if 0 <= x < N and 0 <= y < N:
                vs.add((x, y))
    vs |= {(7, 7), (8, 7), (7, 8), (8, 8)}
    return infect(base, 6505, vs, shards=1, spores=2)


LEAF = """
..4.
.434
3432
.32.
"""


def _leaf_tex():
    t = Tex(N, N)
    back = {k: INF_LEAF[max(0, int(k) - 2)] for k in "234"}
    front = {k: INF_LEAF[int(k)] for k in "234"}
    for (x, y) in _spots_for(6601, 11, 1.6):
        stamp(t, LEAF, back, x, y)
    for (x, y) in _spots_for(6633, 11, 2.4):
        stamp(t, LEAF, front, x, y)
    return t


def infected_leaves():
    leaf = _leaf_tex()
    opaque = lambda x, y: leaf.get(x % N, y % N)[3] > 0
    return infect(leaf, 6611, veins(6609, paths=3, length=8, allow=opaque), shards=2, spores=4, opaque=opaque)


def infected_sand():
    base = clusters(6701, INF_SAND, base=2, lights=9, darks=8, longest=4, horizontal=0.85)
    for y in range(N):
        for x in range(N):
            if (x + 2 * y) % 8 == 0:
                base.set(x, y, INF_SAND[1])
    return infect(base, 6707, veins(6705, paths=2, length=12), shards=2, spores=3, tint=0.45)


# ------------------------------------------------------------------ Story-Bloecke
def memory_crystal() -> Tex:
    """Kristallsaeule: senkrechte Facetten (hell, mittel, dunkel), Schnitte quer, oben heller, weisse Funken."""
    t = Tex(N, N)
    facet = [3, 2, 2, 1, 1]  # hell links -> dunkel rechts, Periode 5
    for y in range(N):
        for x in range(N):
            c = facet[x % 5] + (1 if y < 5 else 0) - (1 if y > 11 else 0)
            if y in (5, 11):
                c -= 1
            if x % 5 == 0:
                c += 1
            t.set(x, y, CYAN[max(0, min(4, c))])
    for y in range(N):
        t.set(8, y, CYAN[4] if y % 6 != 3 else CYAN[3])
    for (x, y) in ((5, 3), (10, 7), (6, 11), (12, 12), (3, 8), (9, 1)):
        t.set(x, y, hx("#ffffff"))
    return t


def purity_anchor() -> Tex:
    """Sockel und Saeule aus hellem Stein mit goldenen Gravuren (Modell: Sockel Zeilen 13-15, Saeule Spalten 4-11)."""
    t = clusters(6901, IVORY, base=3, lights=7, darks=6, longest=3, hi=4, lo=2)
    for x in range(N):
        t.set(x, 13, GOLD[3])
        t.set(x, 14, IVORY[2])
        t.set(x, 15, IVORY[0])
    for x in (0, 5, 10):
        t.set(x, 14, IVORY[1])
    for y in range(4, 13):
        for x, tone in ((4, 4), (5, 3), (6, 3), (9, 2), (10, 1), (11, 1)):
            t.set(x, y, IVORY[tone])
    rune = {"G": GOLD[2], "W": GOLD[4], "B": SKYB[2]}
    for j, row in enumerate(("..GG..", ".GWWG.", "GWBBWG", "GWBBWG", ".GWWG.", "..GG..")):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(5 + i, 5 + j, rune[ch])
    for x in range(6, 10):
        t.set(x, 4, GOLD[2])
        t.set(x, 12, GOLD[1])
    for i in range(1, 15):
        t.set(i, 1, GOLD[2])
        if i < 13:
            t.set(1, i, GOLD[2])
            t.set(14, i, GOLD[1])
    return t


def purity_anchor_crystal(frames: int = 8) -> Tex:
    """Animierter Kern: goldweisser Kristall mit drei Facettenstreifen; ein Lichtband steigt auf, der Kern pulsiert."""
    sheet = Tex(N, N * frames)
    for k in range(frames):
        ph = k / frames
        for y in range(N):
            for x in range(N):
                facet = [3, 4, 2][(x // 3) % 3]
                idx = facet - y / 15.0 * 0.8
                band = math.sin(2 * math.pi * (ph - y / 16.0))
                idx += max(0.0, band) * 1.4
                idx += max(0.0, 3.4 - math.hypot(x - 7.5, y - 7.5)) * 0.35 * (0.7 + 0.3 * math.sin(2 * math.pi * ph))
                if x % 3 == 0:
                    idx += 0.4
                i = int(round(idx))
                if i >= 6:
                    c = hx("#ffffff")
                elif i >= 5:
                    c = GOLD[4]
                elif i <= 1:
                    c = mix(GOLD[1], SKYB[1], 0.3)
                else:
                    c = GOLD[min(4, i - 1)]
                sheet.set(x, y + k * N, c)
    return sheet


# ------------------------------------------------------------------ Ausgabe
def pulse_frames(glow: Tex, frames: int = 4, lo: float = 0.45, hi: float = 1.0, wave: float = 16.0) -> Tex:
    """Stapelt `frames` Bilder uebereinander; die Helligkeit laeuft als Welle ueber die Adern (kachelnd)."""
    sheet = Tex(N, N * frames)
    for k in range(frames):
        for y in range(N):
            for x in range(N):
                c = glow.get(x, y)
                if c[3] == 0:
                    continue
                f = lo + (hi - lo) * (0.5 + 0.5 * math.sin(2 * math.pi * (k / frames + (x + y) / wave)))
                sheet.set(x, y + k * N, (int(c[0] * f), int(c[1] * f), int(c[2] * f), c[3]))
    return sheet


def write_mcmeta(name: str, frametime: int, interpolate: bool = True):
    p = TEX / "block" / f"{name}.png.mcmeta"
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps({"animation": {"frametime": frametime, "interpolate": interpolate}}, indent=2) + "\n", encoding="utf-8")


def save_pair(name, pair, animate=False):
    base, glow = pair
    save_png(base, f"block/{name}")
    if animate:
        save_png(pulse_frames(glow), f"block/{name}_glow")
        write_mcmeta(f"{name}_glow", 10)
    else:
        save_png(glow, f"block/{name}_glow")


def main():
    save_pair("infected_stone", infected_stone())
    save_pair("infected_dirt", infected_dirt())
    save_pair("infected_grass_top", infected_grass_top(), animate=True)
    save_pair("infected_grass_side", infected_grass_side(), animate=True)
    save_pair("infected_log", infected_log())
    save_pair("infected_log_top", infected_log_top())
    save_pair("infected_leaves", infected_leaves(), animate=True)
    save_pair("infected_sand", infected_sand())
    save_png(memory_crystal(), "block/memory_crystal")
    save_png(purity_anchor(), "block/purity_anchor")
    save_png(purity_anchor_crystal(), "block/purity_anchor_crystal")
    write_mcmeta("purity_anchor_crystal", 3)
    print("fresh_virus: 8 infizierte Texturen + Leuchtschichten, memory_crystal, purity_anchor(+Kristall)")


if __name__ == "__main__":
    main()
