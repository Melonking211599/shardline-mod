"""Infizierte Bloecke und Story-Bloecke (Stufe 2, von Grund auf neu). Aufruf: python tools/assetgen/fresh_virus.py

Konzept: Der Virus WAECHST. In jedem Block frisst sich ein Netz duenner, diagonaler Wurzeladern durch das Material:
  Rille (dunkel eingegraben) + Hof (angefressenes Material) in der Grundtextur, der leuchtende Kern in <name>_glow.png,
dazu kleine Kristallsplitter, die aus den Adern herauswachsen. Alle Bilder kacheln.
"""
from __future__ import annotations

import json
import math

from common import CYAN, GOLD, IVORY, SKYB, TEX, VGLOW, VIRUS, Tex, clamp, hx, mix, nz, save_png
from fresh_blocks import crystal_log, crystal_log_top
from fresh_blocks2 import crystal_leaves
from pixel import clusters, grow, patches, ramps, shift, spots, stamp

N = 16

INF_STONE = [hx(c) for c in ("#181120", "#281c36", "#3b2a4b", "#51396a", "#6c4f88")]
INF_DIRT = [hx(c) for c in ("#1c0f1c", "#2f1a2e", "#46283f", "#5f3a54", "#7b5169")]
INF_GRASS = [hx(c) for c in ("#17081f", "#2a1138", "#44205a", "#63307f", "#8545a3")]
INF_BARK = [hx(c) for c in ("#140a1a", "#25142c", "#3a2140", "#52305a", "#6e4575")]
INF_LEAF = [hx(c) for c in ("#12051a", "#2a0b36", "#4a1450", "#76206c", "#a43a92")]
INF_SAND = [hx(c) for c in ("#2c2036", "#4a3a56", "#6e587a", "#947c9f", "#b99fc2")]

VARIANT_SEEDS = (0, 1000, 2000)       # Zusatz zu allen Seeds einer Familie; Datei <name>.png = 0, _v2 = 1000, _v3 = 2000


# ------------------------------------------------------------------ Adern (gewachsene Baeume, siehe pixel.grow)
def veins(seed: int, roots, length: int = 13, fork: float = 0.2, allow=None):
    """Aderbaeume aus gegebenen Wurzeln. Gibt ({(x, y): (u, gen)}, tips) zurueck."""
    return grow(seed, roots, length=length, fork=fork, wobble=0.3, max_gen=2, allow=allow)


def roots_at(seed: int, count: int = 2, margin: int = 2):
    """Wurzeln am Kachelrand, damit die Adern ueber Bloeckgrenzen weiterwachsen (Netz statt Inseln)."""
    out = []
    for i in range(count):
        edge = int(nz(i, 0, seed) * 4)
        pos = margin + int(nz(i, 1, seed) * (N - 2 * margin))
        x, y = ((0, pos), (N - 1, pos), (pos, 0), (pos, N - 1))[edge]
        dx = 1 if x < N // 2 else -1
        dy = 1 if y < N // 2 else -1
        out.append(((x, y), (dx, dy)))
    return out


def infect(base: Tex, seed: int, tree, shards: int = 1, spores: int = 3, tint: float = 0.5, opaque=None,
           shard_skip=lambda p: False, groove=VIRUS, light=VGLOW):
    """Legt Rille, Fraßhof, Leuchtadern und Kristallspitzen auf `base`. Liefert (base, glow).

    Aelteres Gewebe (nahe der Wurzel) ist staerker angefressen (breiterer dunkler Hof), die Adern leuchten zur
    Spitze hin heller, die Zweigspitzen sind fast weiss. Kristalle wachsen nur an Zweigenden.
    groove/light: Rillen- und Leuchtrampe (5 Stufen) des Virusstamms; Standard ist der Shardvirus (Violett/Magenta)."""
    VIRUS, VGLOW = groove, light
    cells, tips = tree
    glow = Tex(N, N)
    px = base.px
    ok = (lambda x, y: True) if opaque is None else opaque
    for (x, y), (u, gen) in cells.items():
        if not ok(x, y):
            continue
        reach = 1 if u < 0.65 else 0
        for q in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            q = (q[0] % N, q[1] % N)
            if q not in cells and ok(*q) and reach:
                c = mix(px[q], VIRUS[2], tint * (1.0 - u))
                px[q] = (int(c[0] * 0.78), int(c[1] * 0.64), int(c[2] * 0.82), c[3])
    for (x, y), (u, gen) in sorted(cells.items()):
        if not ok(x, y):
            continue
        px[x, y] = VIRUS[0] if u < 0.7 else VIRUS[1]
        tone = 0 if u < 0.3 else 1 if u < 0.65 else 2 if u < 0.92 else 3
        glow.set(x, y, VGLOW[tone])
    for (x, y) in tips:
        if ok(x, y):
            glow.set(x, y, VGLOW[4])
    # Kristallspitze wachsen aus Zweigenden (nach oben), dunkler Fuss, helle Spitze
    placed = 0
    for k, (x, y) in enumerate(sorted(tips)):
        if placed >= shards or shard_skip((x, y)):
            continue
        spots_ = [((x, (y - 1) % N), VGLOW[3], VIRUS[4]), ((x, (y - 2) % N), VGLOW[2], VIRUS[3])]
        if all(ok(*q) and q not in cells for q, _, _ in spots_):
            for q, g, b in spots_:
                glow.set(q[0], q[1], g)
                px[q] = b
            placed += 1
    # einzelne Sporenfunken im Hof der Adern
    halo = sorted({((x + dx) % N, (y + dy) % N) for (x, y) in cells for dx, dy in ((2, 0), (-2, 0), (0, 2), (0, -2))} - set(cells))
    placed_n, k = 0, 0
    while placed_n < spores and halo and k < 80:
        p = halo[int(nz(k, 9, seed) * len(halo)) % len(halo)]
        if ok(*p) and glow.get(*p)[3] == 0 and px[p][3]:
            glow.set(p[0], p[1], VGLOW[1])
            placed_n += 1
        k += 1
    return base, glow


# ------------------------------------------------------------------ Grundtexturen (gleiche Strukturen wie die gesunden Bloecke)
def stone_base(seed, ramp_):
    return patches(seed, ramp_, fractions=(0.0, 0.27, 0.50, 0.21, 0.02), cells=(4, 4), detail=0.3)


def dirt_base(seed, ramp_):
    t = patches(seed, ramp_, fractions=(0.04, 0.26, 0.46, 0.20, 0.04), cells=(4, 2), detail=0.35, detail_cells=(8, 4))
    for (x, y) in spots(seed + 5, 3, 5.0):
        t.set(x, y, ramp_[4])
        t.set((x + 1) % N, y, ramp_[3])
    return t


def infected_stone(v=0):
    base = stone_base(6001 + v, INF_STONE)
    return infect(base, 6005 + v, veins(6003 + v, roots_at(6003 + v, 1), length=17, fork=0.24), shards=1, spores=3)


def infected_dirt(v=0):
    base = dirt_base(6101 + v, INF_DIRT)
    return infect(base, 6105 + v, veins(6103 + v, roots_at(6103 + v, 1), length=17, fork=0.24), shards=1, spores=3)


def grass_base(seed, ramp_):
    t = patches(seed, ramp_, fractions=(0.05, 0.25, 0.44, 0.21, 0.05), cells=(4, 4), detail=0.4)
    for (x, y) in spots(seed + 5, 5, 3.6):                 # einzelne Halme
        t.set(x, y, ramp_[4])
        t.set(x, (y + 1) % N, ramp_[3])
    return t


def infected_grass_top(v=0):
    base = grass_base(6201 + v, INF_GRASS)
    return infect(base, 6205 + v, veins(6203 + v, roots_at(6203 + v, 1), length=17, fork=0.24), shards=1, spores=3, tint=0.55)


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
    allow = lambda x, y: y >= fringe[x] + 1
    roots = [((3, fringe[3] + 1), (1, 1)), ((11, fringe[11] + 1), (-1, 1))]
    tree = veins(6307, roots, length=10, allow=allow)
    cells, tips = tree
    for x in range(N):                                   # Adern wachsen aus der Grasnarbe nach unten
        if nz(x, 5, 6309) > 0.78 and (x, fringe[x]) not in cells:
            for y in range(fringe[x] - 1, min(N, fringe[x] + 2 + int(nz(x, 6, 6309) * 3))):
                cells[(x, y)] = (0.2 + 0.1 * (y - fringe[x]), 1)
    return infect(base, 6311, tree, shards=1, spores=2, tint=0.5, shard_skip=lambda p: p[1] > 12)


def infected_log():
    base = crystal_log(bark=INF_BARK, veins=False)
    return infect(base, 6407, veins(6405, roots_at(6405, 1), length=17, fork=0.22), shards=1, spores=2)


def infected_log_top():
    base = crystal_log_top(wood=INF_BARK, bark=INF_BARK, core=False)
    return infect(base, 6505, veins(6503, [((7, 7), (1, 1)), ((8, 8), (-1, 1))], length=11, fork=0.18), shards=0, spores=2)


def _leaf_tex():
    return crystal_leaves(6601, tones=INF_LEAF, blossoms=False)


def infected_leaves(v=0):
    leaf = _leaf_tex()
    opaque = lambda x, y: leaf.get(x % N, y % N)[3] > 0
    tree = veins(6609 + v, roots_at(6609 + v, 3), length=9, fork=0.18, allow=opaque)
    return infect(leaf, 6611 + v, tree, shards=1, spores=3, opaque=opaque)


def infected_sand(v=0):
    base = patches(6701 + v, INF_SAND, fractions=(0.04, 0.24, 0.46, 0.22, 0.04), cells=(2, 6), detail=0.35, detail_cells=(8, 4))
    return infect(base, 6707 + v, veins(6705 + v, roots_at(6705 + v, 1), length=17, fork=0.24), shards=1, spores=3, tint=0.45)


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
    for n, v in ((2, VARIANT_SEEDS[1]), (3, VARIANT_SEEDS[2])):          # Varianten gegen sichtbare Wiederholung
        save_pair(f"infected_stone_v{n}", infected_stone(v))
        save_pair(f"infected_dirt_v{n}", infected_dirt(v))
        save_pair(f"infected_grass_top_v{n}", infected_grass_top(v), animate=True)
        save_pair(f"infected_leaves_v{n}", infected_leaves(v), animate=True)
        save_pair(f"infected_sand_v{n}", infected_sand(v))
    save_png(memory_crystal(), "block/memory_crystal")
    save_png(purity_anchor(), "block/purity_anchor")
    save_png(purity_anchor_crystal(), "block/purity_anchor_crystal")
    write_mcmeta("purity_anchor_crystal", 3)
    print("fresh_virus: 8 infizierte Texturen + Leuchtschichten (+10 Varianten), memory_crystal, purity_anchor(+Kristall)")


if __name__ == "__main__":
    main()
