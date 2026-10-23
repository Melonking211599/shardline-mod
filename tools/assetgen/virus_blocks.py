"""Infizierte Bloecke, Erinnerungskristall und Reinheitsanker. Aufruf: python tools/assetgen/virus_blocks.py

Der Virus soll WACHSEN, nicht nur umgefaerbt aussehen: In jedem Block frisst sich ein Netz aus Kristalladern durch
das Material. Die Ader besteht aus drei Schichten:
  1. Rille   : die Ader ist in die Grundtextur eingegraben (fast schwarzes Violett),
  2. Hof     : das Material rund um die Ader ist angefressen (dunkler, ins Magenta gekippt),
  3. Kern    : die leuchtende Ader selbst liegt in der <name>_glow.png (volle Eigenhelligkeit),
dazu kleine Kristallsplitter, die an einzelnen Aderpunkten herauswachsen.
Gras (oben/Seite) und Laub haben eine animierte Leuchtschicht (4 Frames, pulsierende Adern, langsam).
Alle Texturen kacheln nahtlos. Rauschen kommt aus fester Hashfunktion, kein unseeded Zufall.
"""
from __future__ import annotations

import json
import math

from common import (CYAN, GOLD, IVORY, SKYB, SLATE, TEX, VGLOW, VIRUS, Tex, clamp, hx, mix, nz, ramp, save_png)

N = 16

# ------------------------------------------------------------------ Grundrampen der kranken Materialien
INF_STONE = [hx(c) for c in ("#181120", "#281c36", "#3b2a4b", "#51396a", "#6c4f88")]
INF_DIRT = [hx(c) for c in ("#1c0f1c", "#2f1a2e", "#46283f", "#5f3a54", "#7b5169")]
INF_GRASS = [hx(c) for c in ("#17081f", "#2a1138", "#44205a", "#63307f", "#8545a3")]
INF_BARK = [hx(c) for c in ("#140a1a", "#25142c", "#3a2140", "#52305a", "#6e4575")]
INF_LEAF = [hx(c) for c in ("#12051a", "#2a0b36", "#4a1450", "#76206c", "#a43a92")]
INF_SAND = [hx(c) for c in ("#42324c", "#63506e", "#8a7391", "#b098b4", "#d3bdd3")]


# ------------------------------------------------------------------ Rauschen, das exakt kachelt
def tn(x: float, y: float, cx: int, cy: int, seed: int) -> float:
    """Value-Noise mit cx mal cy Gitterzellen ueber 16 Pixel. Wrap-around, daher kachelnd."""
    fx, fy = x * cx / N, y * cy / N
    x0, y0 = int(fx // 1), int(fy // 1)
    tx, ty = fx - x0, fy - y0
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)

    def g(ix, iy):
        return nz(ix % cx, iy % cy, seed)

    a = g(x0, y0) * (1 - tx) + g(x0 + 1, y0) * tx
    b = g(x0, y0 + 1) * (1 - tx) + g(x0 + 1, y0 + 1) * tx
    return a * (1 - ty) + b * ty


def new() -> Tex:
    return Tex(N, N)


def neighbours4(p):
    x, y = p
    return ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))


# ------------------------------------------------------------------ Adernetz
def vein_mask(seed: int, cx1=4, cy1=4, w1=0.05, cx2=8, cy2=8, w2=0.028, region=None) -> set:
    """Zusammenhaengende, kachelnde Linien: dort, wo das Rauschen seinen Mittelwert kreuzt (Kamm)."""
    out = set()
    for y in range(N):
        for x in range(N):
            if region and not region(x, y):
                continue
            a = abs(tn(x + 0.5, y + 0.5, cx1, cy1, seed) - 0.5)
            b = abs(tn(x + 0.5, y + 0.5, cx2, cy2, seed + 7) - 0.5)
            if a < w1 or b < w2:
                out.add((x, y))
    return out


_DIRS = ((1, 0), (0, 1), (-1, 0), (0, -1))


def vein_walk(seed: int, paths=2, length=12, turn=0.35, branch=0.12, start_dirs=(0, 1, 2, 3), allow=None) -> set:
    """Aderwachstum: ein paar Wurzelpunkte kriechen als 4-verbundene Linien ueber die Flaeche und verzweigen sich.

    Die Pfade laufen ueber den Rand (Wrap-around), die Textur kachelt also nahtlos. Kurze, stark abbiegende Pfade
    verhindern durchgehende Linien. Die Reihenfolge und alle
    Entscheidungen stammen aus der festen Hashfunktion, das Ergebnis ist reproduzierbar.
    """
    veins: set = set()
    queue = []
    for i in range(paths):
        x, y = int(nz(i, 0, seed) * N), int(nz(i, 1, seed) * N)
        d = start_dirs[int(nz(i, 2, seed) * len(start_dirs)) % len(start_dirs)]
        queue.append((x, y, d, length))
    k = 0
    while queue and k < 600:
        x, y, d, left = queue.pop(0)
        while left > 0 and k < 600:
            k += 1
            if allow is None or allow(x, y):
                veins.add((x, y))
            if nz(k, 5, seed) < turn:
                d = (d + (1 if nz(k, 6, seed) > 0.5 else 3)) % 4
                if len(start_dirs) == 2 and d not in start_dirs and nz(k, 4, seed) < 0.7:
                    d = (d + 2) % 4 if (d + 2) % 4 in start_dirs else start_dirs[0]
            if nz(k, 8, seed) < branch and left > 4:
                queue.append((x, y, (d + (1 if k % 2 else 3)) % 4, left // 2 + 1))
            nx, ny = (x + _DIRS[d][0]) % N, (y + _DIRS[d][1]) % N
            if allow is not None and not allow(nx, ny):
                break
            x, y = nx, ny
            left -= 1
    return veins


def grow_pick(veins: set, seed: int, count: int, skip=lambda p: False):
    """Waehlt deterministisch Aderpunkte, an denen ein Kristallsplitter herauswaechst."""
    pts = sorted(p for p in veins if not skip(p))
    if not pts:
        return []
    out, k = [], 0
    while len(out) < count and k < 200:
        p = pts[int(nz(k, 3, seed) * len(pts)) % len(pts)]
        if all(abs(p[0] - q[0]) + abs(p[1] - q[1]) > 4 for q in out):
            out.append(p)
        k += 1
    return out


def infect(base: Tex, seed: int, veins: set, shards=3, vein_cols=(1, 2), spores=4, tint=0.4,
           shard_skip=lambda p: False, opaque=None):
    """Legt das Adernetz auf `base` und liefert (base, glow). `opaque(x, y)` begrenzt auf deckende Pixel (Laub)."""
    glow = new()
    px = base.px
    halo = set()
    for p in veins:
        for q in neighbours4(p):
            qx, qy = q[0] % N, q[1] % N
            if (qx, qy) not in veins and (opaque is None or opaque(qx, qy)):
                halo.add((qx, qy))
    # Hof: angefressenes Material
    for (x, y) in halo:
        c = px[x, y]
        c = mix(c, VIRUS[2], tint)
        px[x, y] = (int(c[0] * 0.72), int(c[1] * 0.6), int(c[2] * 0.78), c[3])
    # Rille und Kern
    for (x, y) in veins:
        if opaque is not None and not opaque(x, y):
            continue
        px[x, y] = VIRUS[1] if (x + y) % 3 else VIRUS[0]
        # Kern: sichtbar heller Farbwechsel, damit die Ader "fliesst"
        v = vein_cols[0] if (x * 3 + y * 2) % 5 else vein_cols[1]
        glow.set(x, y, VGLOW[v])
    # Splitter, die aus der Ader herauswachsen (oben links hell, unten rechts dunkel)
    for (x, y) in grow_pick(veins, seed, shards, shard_skip):
        for (dx, dy, bi, gi) in ((0, 0, 4, 4), (-1, -1, 3, 3), (1, 1, 2, None), (0, -1, 3, 2), (-1, 0, 3, 2)):
            xx, yy = (x + dx) % N, (y + dy) % N
            if opaque is not None and not opaque(xx, yy):
                continue
            px[xx, yy] = VIRUS[bi] if gi is None else VGLOW[gi] if bi == 4 else VIRUS[bi]
            if gi is not None:
                glow.set(xx, yy, VGLOW[gi])
            else:
                glow.set(xx, yy, (0, 0, 0, 0))
    # einzelne Sporenfunken neben der Ader
    k = 0
    placed = 0
    cand = sorted(halo)
    while placed < spores and cand and k < 80:
        p = cand[int(nz(k, 9, seed) * len(cand)) % len(cand)]
        if glow.get(*p)[3] == 0:
            glow.set(p[0], p[1], VGLOW[1])
            placed += 1
        k += 1
    return base, glow


# ------------------------------------------------------------------ Bloecke
def infected_stone():
    from blocks import stone_tile
    base = stone_tile(6001, INF_STONE, bias=2.0, spread=1.5)
    base.outline  # (nur Hinweis: keine Kontur bei Bloecken)
    veins = vein_walk(6003, paths=5, length=8, turn=0.5, branch=0.10)
    return infect(base, 6005, veins, shards=3, spores=5)


def infected_dirt():
    from blocks import stone_tile
    base = stone_tile(6101, INF_DIRT, bias=1.9, spread=1.3)
    veins = vein_walk(6103, paths=5, length=8, turn=0.5, branch=0.10)
    return infect(base, 6105, veins, shards=2, spores=4)


def _grass_blades(base: Tex, seed: int, r, rows=range(N), top=True):
    for y in rows:
        for x in range(N):
            n = tn(x + 0.5, y + 0.5, 8, 8, seed)
            idx = 1.7 + (n - 0.5) * 2.4 + (nz(x, y, seed + 2) - 0.5) * 0.8
            base.set(x, y, ramp(r, idx))
    # Halme: kurze helle Striche mit Spitze
    for y in rows:
        for x in range(N):
            if nz(x, y, seed + 5) > 0.83:
                base.set(x, y, ramp(r, 3.4))
                if (y - 1) in rows or y - 1 >= 0:
                    base.set(x, (y - 1) % N, ramp(r, 2.6))


def infected_grass_top():
    base = new()
    _grass_blades(base, 6201, INF_GRASS)
    veins = vein_walk(6203, paths=5, length=8, turn=0.5, branch=0.10)
    return infect(base, 6205, veins, shards=3, spores=5, tint=0.5)


def infected_grass_side():
    base = new()
    # unten: kranke Erde, oben: Grasschopf mit unregelmaessigem Saum
    from blocks import stone_tile
    dirt = stone_tile(6301, INF_DIRT, bias=1.9, spread=1.3)
    fringe = [3 + (1 if nz(x, 0, 6303) > 0.45 else 0) + (1 if nz(x, 1, 6303) > 0.8 else 0) for x in range(N)]
    for y in range(N):
        for x in range(N):
            if y < fringe[x]:
                n = tn(x + 0.5, y + 0.5, 8, 4, 6305)
                idx = 1.8 + (n - 0.5) * 2.0 - (0.5 if y == fringe[x] - 1 else 0) + (0.4 if y == 0 else 0)
                base.set(x, y, ramp(INF_GRASS, idx))
            else:
                base.set(x, y, dirt.get(x, y))
    veins = vein_walk(6307, paths=4, length=7, turn=0.5, branch=0.10, allow=lambda x, y: y >= fringe[x] + 1)
    # Adern, die vom Gras in die Erde "tropfen"
    drips = set()
    for x in range(N):
        if nz(x, 5, 6309) > 0.72:
            ln = 3 + int(nz(x, 6, 6309) * 5)
            for y in range(fringe[x] - 1, min(N, fringe[x] + ln)):
                drips.add((x, y))
    veins |= drips
    return infect(base, 6311, veins, shards=2, spores=3, tint=0.5, shard_skip=lambda p: p[1] > 12)


def infected_log():
    base = new()
    for y in range(N):
        for x in range(N):
            streak = nz(x, 0, 6401)
            n = tn(x + 0.5, y + 0.5, 8, 2, 6403)
            idx = 1.9 + (streak - 0.5) * 1.5 + (n - 0.5) * 1.6
            if (x % 4) == 0:
                idx -= 0.9  # Rindenfurche
            base.set(x, y, ramp(INF_BARK, idx))
    veins = vein_walk(6405, paths=5, length=9, turn=0.2, branch=0.08, start_dirs=(1, 3))
    return infect(base, 6407, veins, shards=2, spores=3)


def infected_log_top():
    base = new()
    cx = cy = 7.5
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - cx, y - cy)
            if d > 6.4:
                idx = 1.3 + (nz(x, y, 6501) - 0.5) * 1.2  # Rinde
                if max(abs(x - cx), abs(y - cy)) > 7:
                    idx -= 0.6
            else:
                ring = math.sin(d * 1.75 + 0.6)
                idx = 2.7 + ring * 1.0 + (nz(x, y, 6503) - 0.5) * 0.5
            base.set(x, y, ramp(INF_BARK, idx))
    veins = set()
    # drei Risse laufen vom Kern nach aussen
    for (ang, ln) in ((0.35, 7.5), (2.5, 6.5), (4.5, 7.0)):
        for s in range(int(ln * 2)):
            t = s / 2.0
            wob = math.sin(t * 1.3 + ang * 3) * 0.7
            x = int(round(cx + math.cos(ang) * t - math.sin(ang) * wob))
            y = int(round(cy + math.sin(ang) * t + math.cos(ang) * wob))
            if 0 <= x < N and 0 <= y < N:
                veins.add((x, y))
    veins |= {(7, 7), (8, 7), (7, 8), (8, 8)}
    return infect(base, 6505, veins, shards=1, spores=2, vein_cols=(1, 3))


def _leaf_alpha(x, y):
    a = tn(x + 0.5, y + 0.5, 8, 8, 6601)
    b = tn(x + 0.5, y + 0.5, 4, 4, 6603)
    return a * 0.65 + b * 0.35 > 0.36


def infected_leaves():
    base = new()
    for y in range(N):
        for x in range(N):
            if not _leaf_alpha(x, y):
                continue
            n = tn(x + 0.5, y + 0.5, 8, 8, 6605)
            idx = 1.7 + (n - 0.5) * 2.4 + (nz(x, y, 6607) - 0.5) * 0.7
            # Lichtkante oben links des Blattbuschs
            if not _leaf_alpha(x - 1, y) or not _leaf_alpha(x, y - 1):
                idx += 0.7
            if not _leaf_alpha(x + 1, y) or not _leaf_alpha(x, y + 1):
                idx -= 0.7
            base.set(x, y, ramp(INF_LEAF, idx))
    opaque = lambda x, y: _leaf_alpha(x, y)
    veins = vein_walk(6609, paths=6, length=7, turn=0.5, branch=0.12, allow=_leaf_alpha)
    return infect(base, 6611, veins, shards=2, spores=4, opaque=opaque)


def infected_sand():
    base = new()
    for y in range(N):
        for x in range(N):
            ripple = math.sin(2 * math.pi * (x + 2 * y) / N) * 0.55
            idx = 2.3 + ripple + (nz(x, y, 6701) - 0.5) * 1.1 + (tn(x + 0.5, y + 0.5, 4, 4, 6703) - 0.5) * 0.9
            base.set(x, y, ramp(INF_SAND, idx))
    veins = vein_walk(6705, paths=5, length=8, turn=0.5, branch=0.10)
    return infect(base, 6707, veins, shards=2, spores=3, tint=0.45)


# ------------------------------------------------------------------ Animation der Leuchtschicht
def pulse_frames(glow: Tex, frames=4, lo=0.45, hi=1.0, wave=16.0) -> Tex:
    """Stapelt `frames` Bilder uebereinander. Die Helligkeit laeuft als Welle ueber die Adern (x+y), kachelnd."""
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


def write_mcmeta(name: str, frametime: int, interpolate=True, frames=None):
    anim = {"frametime": frametime, "interpolate": interpolate}
    if frames:
        anim["frames"] = frames
    p = TEX / "block" / f"{name}.png.mcmeta"
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps({"animation": anim}, indent=2) + "\n", encoding="utf-8")


# ------------------------------------------------------------------ Story-Bloecke
def memory_crystal():
    """Kristallcluster: senkrechte Facettenbaender, die im Element-Modell (Saeule + 2 schraege Splitter) passen.

    Die Flaechen des Modells lesen verschiedene Ausschnitte desselben 16x16-Bildes, deshalb ist die Textur
    ein gleichmaessiges Muster aus Facettenstreifen mit hellen Kanten und einem warmen Kern in der Mitte.
    """
    t = new()
    bands = [CYAN[2], CYAN[3], CYAN[1], CYAN[3], CYAN[2], CYAN[1]]
    for y in range(N):
        for x in range(N):
            b = (x * 3 // 2 + (y // 5)) % 6
            idx = 2.0 + [0.0, 1.0, -1.0, 0.8, 0.0, -0.8][b]
            idx += (0.9 - y / 15.0 * 1.6)                    # oben heller
            idx += (nz(x, y, 6801) - 0.5) * 0.4
            if x % 4 == 0:
                idx += 0.8                                     # helle Facettenkante
            if x % 4 == 3:
                idx -= 0.7
            t.set(x, y, ramp(CYAN, idx))
    # sanfter Kern: helle Linie und ein paar Glanzpunkte
    for y in range(N):
        c = CYAN[4] if y % 5 != 3 else CYAN[3]
        t.set(8, y, c)
        t.set(7, y, mix(t.get(7, y), CYAN[4], 0.55))
    for (x, y) in ((5, 3), (10, 6), (6, 10), (11, 12), (4, 7), (9, 1)):
        t.set(x, y, hx("#ffffff"))
    return t


def purity_anchor():
    """Sockel und Saeule: heller Stein mit goldenen Gravuren. Siehe Modell: Sockel liest Zeilen 13-15, Saeule Spalten 4-11."""
    t = new()
    for y in range(N):
        for x in range(N):
            idx = 2.6 + (nz(x, y, 6901) - 0.5) * 0.5 + (tn(x + 0.5, y + 0.5, 4, 4, 6903) - 0.5) * 0.8
            t.set(x, y, ramp(IVORY, idx))
    # Steinfugen im Sockel (Zeilen 13-15) und im Rand
    for x in range(N):
        t.set(x, 13, ramp(GOLD, 3.2))
        t.set(x, 14, ramp(IVORY, 2.2))
        t.set(x, 15, ramp(IVORY, 0.9))
    for x in (0, 5, 10):
        t.set(x, 14, ramp(IVORY, 1.2))
    # Saeule (Spalten 4-11, Zeilen 4-12): gekehlt, goldene Mittelrune
    for y in range(4, 13):
        t.set(4, y, ramp(IVORY, 3.6))
        t.set(5, y, ramp(IVORY, 3.0))
        t.set(6, y, ramp(IVORY, 2.4))
        t.set(9, y, ramp(IVORY, 1.8))
        t.set(10, y, ramp(IVORY, 1.2))
        t.set(11, y, ramp(IVORY, 0.8))
    rune = [
        "..GG..",
        ".GWWG.",
        "GWBBWG",
        "GWBBWG",
        ".GWWG.",
        "..GG..",
    ]
    pal = {"G": GOLD[2], "W": GOLD[4], "B": SKYB[2]}
    for j, row in enumerate(rune):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(5 + i, 5 + j, pal[ch])
    # Gravurlinien ober- und unterhalb der Rune
    for x in range(6, 10):
        t.set(x, 4, ramp(GOLD, 2.4))
        t.set(x, 12, ramp(GOLD, 1.4))
    # Goldeinlagen im Randring (Sockeloberseite)
    for i in range(1, 15):
        t.set(i, 1, ramp(GOLD, 2.6))
        t.set(1, i, ramp(GOLD, 2.2)) if i < 13 else None
        t.set(14, i, ramp(GOLD, 1.6)) if i < 13 else None
    return t


def purity_anchor_crystal(frames=8) -> Tex:
    """Animierter Kern: goldweisser Kristall, ein Lichtband wandert nach oben, Kern pulsiert. 8 Frames."""
    sheet = Tex(N, N * frames)
    for k in range(frames):
        ph = k / frames
        for y in range(N):
            for x in range(N):
                facet = (x // 3) % 3
                idx = 2.3 + [0.7, 1.4, -0.3][facet] + (0.5 - y / 15.0) * 0.8
                band = math.sin(2 * math.pi * (ph - y / 16.0))
                idx += max(0.0, band) * 1.2
                core = math.hypot(x - 7.5, y - 7.5)
                idx += max(0.0, 3.4 - core) * 0.3 * (0.7 + 0.3 * math.sin(2 * math.pi * ph))
                if x % 3 == 0:
                    idx += 0.35
                if idx > 4.8:
                    c = hx("#ffffff")
                elif idx > 4.2:
                    c = GOLD[4]
                elif idx < 1.6:
                    c = mix(ramp(GOLD, 1.6), SKYB[1], 0.35)  # sanfter Hellblau-Schimmer in den Schatten
                else:
                    c = ramp(GOLD, idx - 0.1)
                sheet.set(x, y + k * N, c)
    return sheet


# ------------------------------------------------------------------ Ausgabe
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
    print("virus_blocks: 8 infizierte Texturen + Leuchtschichten (3 animiert), memory_crystal, purity_anchor(+Kristall animiert)")


if __name__ == "__main__":
    main()
