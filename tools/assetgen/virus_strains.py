"""Virusstaemme und neue befallene Bloecke (Nach dem Ausbruch, B6). Aufruf: python tools/assetgen/virus_strains.py

Drei Staemme, gleiche Wachstumslogik wie fresh_virus.infect (Rille, Frasshof, Leuchtadern, Kristallspitzen), aber
eigene Farben, damit man den Stamm auf einen Blick erkennt:
  Shardvirus (Oberwelt):   Violett/Magenta           -> infected_deepslate, infected_planks
  Glutbrand (Nether):      Russrot/Orange, glimmt    -> infected_netherrack, _blackstone, _soul_soil, _nylium, _wart_block
  Leerenfaeule (End):      Schwarzviolett/Blassgruen -> infected_end_stone, infected_purpur
Grundtexturen folgen dem gesunden Vanilla-Block (Schichtung, Bretter, Kacheln), nur dunkler und angefressen.
Ausgabe: textures/block/<name>.png + <name>_glow.png (Leuchtschicht); grosse Flaechen mit _v2/_v3.
"""
from __future__ import annotations

import math

from clean import ordered
from common import TEX, VGLOW, VIRUS, Tex, hx, mix, nz, ramp, save_png
from fresh_virus import N, infect, net_tree, pulse_frames, roots_at, veins, write_mcmeta
from infected_animals import collar_band
from pixel import patch_idx, patches, spots
from uvlib import FACE_LIGHT, edge_light, paint_box

# ------------------------------------------------------------------ Staemme (Rille, Leuchten)
EMBER = [hx(c) for c in ("#120403", "#2a0905", "#4a1206", "#7a2408", "#b3400c")]
EMBER_GLOW = [hx(c) for c in ("#a3300c", "#e0601a", "#ff8a2a", "#ffc46a", "#fff2c8")]
VOID = [hx(c) for c in ("#06040c", "#120a20", "#1e1236", "#2a1d4c", "#3b2a66")]
VOID_GLOW = [hx(c) for c in ("#1f6e5c", "#2fa384", "#5fd6ac", "#a8f5d6", "#f0fff6")]
STRAINS = {"shard": (VIRUS, VGLOW), "ember": (EMBER, EMBER_GLOW), "void": (VOID, VOID_GLOW)}

# ------------------------------------------------------------------ Grundfarben (befallen, also dunkler und violetter als Vanilla)
INF_DEEPSLATE = [hx(c) for c in ("#0e0b14", "#17121f", "#211a2c", "#2c233b", "#3a2f4c")]
INF_PLANKS = [hx(c) for c in ("#1e1220", "#2e1c30", "#422a42", "#583a56")]
INF_NETHERRACK = [hx(c) for c in ("#1e0710", "#330c18", "#4a1420", "#621d29", "#7c2a33")]
INF_BLACKSTONE = [hx(c) for c in ("#0b080d", "#151018", "#201823", "#2b2130", "#3a2c3f")]
INF_SOUL = [hx(c) for c in ("#140d10", "#21161b", "#302128", "#3f2d35", "#523c45")]
INF_NYLIUM = [hx(c) for c in ("#2a0612", "#46091c", "#650f28", "#861a36", "#a82a46")]
INF_WART = [hx(c) for c in ("#2a040c", "#470714", "#650b1c", "#841426", "#a52234")]
INF_END = [hx(c) for c in ("#3c3a2c", "#55523c", "#6f6b4c", "#8a855e", "#a6a074")]
INF_PURPUR = [hx(c) for c in ("#2a1a30", "#3d2645", "#52345c", "#6a4675", "#84598f")]

VARIANT_SEEDS = (0, 1000, 2000)


def _pair(base: Tex, seed: int, strain: str, roots: int = 1, length: int = 17, fork: float = 0.24, **kw):
    groove, light = STRAINS[strain]
    return infect(base, seed + 4, net_tree(seed + 2), groove=groove, light=light, **kw)


# ------------------------------------------------------------------ Oberwelt (Shardvirus, tiefer im Boden und in Haeusern)
def infected_deepslate(v=0):
    """Waagerecht geschichtet wie Tiefenschiefer (gestreckte Flecken), die Adern folgen den Schichten."""
    base = patches(7001 + v, INF_DEEPSLATE, fractions=(0.04, 0.26, 0.44, 0.22, 0.04), cells=((2, 8), (3, 7), (2, 6))[(v // 1000) % 3], detail=0.25,
                   detail_cells=(4, 16), warp=0.6)
    return _pair(base, 7000 + v, "shard", shards=1, spores=2)


def infected_planks():
    """Vier Bretter mit Stossfugen und Naegeln; die Adern kriechen quer ueber die Bretter."""
    idx = patch_idx(7101, fractions=(0.0, 0.28, 0.48, 0.24, 0.0), cells=(2, 8), detail=0.3, detail_cells=(4, 8))
    t = Tex(N, N)
    seams = {0: 11, 1: 4, 2: 13, 3: 7}
    tone = [2, 2, 1, 2]
    for board in range(4):
        for j in range(4):
            for i in range(N):
                y = board * 4 + j
                c = tone[board] + (idx[y][i] - 2)
                if j == 0:
                    c += 1
                elif j == 3:
                    c = tone[board] - 1
                if i == seams[board] and j != 3:
                    c = 0
                t.set(i, y, INF_PLANKS[max(0, min(3, c))])
        for nx in ((seams[board] + 3) % N, (seams[board] + 12) % N):
            t.set(nx, board * 4 + 1, INF_PLANKS[0])
    return _pair(t, 7100, "shard", shards=1, spores=2, tint=0.4)


# ------------------------------------------------------------------ Nether (Glutbrand)
def infected_netherrack(v=0):
    """Grobe, kantige Klumpen wie Netherrack; Glutadern statt Violett."""
    base = patches(7231 + v, INF_NETHERRACK, fractions=(0.06, 0.22, 0.40, 0.24, 0.08), cells=((4, 4), (3, 5), (5, 3))[(v // 1000) % 3], detail=0.45,
                   detail_cells=(8, 8), warp=0.85)
    return _pair(base, 7230 + v, "ember", shards=1, spores=3)


def infected_blackstone():
    """Schwarzer Stein mit feinen hellen Koernern; die Glut sitzt tief in den Rillen."""
    base = patches(7301, INF_BLACKSTONE, fractions=(0.08, 0.30, 0.40, 0.18, 0.04), cells=(4, 4), detail=0.35)
    for (x, y) in spots(7305, 6, 3.2):
        base.set(x, y, INF_BLACKSTONE[4])
    return _pair(base, 7300, "ember", shards=1, spores=2)


def infected_soul_soil():
    """Seelenerde: dunkle, weiche Flecken mit eingesunkenen Gesichtern (drei dunkle Punkte), Glut darunter."""
    base = patches(7401, INF_SOUL, fractions=(0.06, 0.26, 0.42, 0.22, 0.04), cells=(4, 2), detail=0.35, detail_cells=(8, 4))
    for (x, y) in spots(7405, 3, 6.0):
        for dx, dy in ((0, 0), (2, 0), (1, 2)):
            base.set((x + dx) % N, (y + dy) % N, INF_SOUL[0])
    return _pair(base, 7400, "ember", shards=0, spores=3, tint=0.4)


def infected_nylium_top():
    """Nezelrasen: fleischig rote Faeden, die Glutadern wachsen hindurch."""
    base = patches(7501, INF_NYLIUM, fractions=(0.05, 0.25, 0.44, 0.21, 0.05), cells=(4, 4), detail=0.45)
    for (x, y) in spots(7505, 6, 3.4):
        base.set(x, y, INF_NYLIUM[4])
        base.set(x, (y + 1) % N, INF_NYLIUM[3])
    return _pair(base, 7500, "ember", shards=1, spores=3, tint=0.45)


def infected_nylium_side():
    """Netherrack mit herabhaengendem Nezelrasen (4-6 Pixel), Adern wachsen aus dem Rasen nach unten."""
    rack = patches(7601, INF_NETHERRACK, fractions=(0.06, 0.22, 0.40, 0.24, 0.08), cells=(4, 4), detail=0.45)
    base = Tex(N, N)
    fringe = [4 + (1 if nz(x, 0, 7603) > 0.5 else 0) + (1 if nz(x, 1, 7603) > 0.8 else 0) for x in range(N)]
    for y in range(N):
        for x in range(N):
            if y < fringe[x]:
                tone = 3 if y == 0 else (1 if y == fringe[x] - 1 else 2)
                base.set(x, y, INF_NYLIUM[tone])
            else:
                base.set(x, y, rack.get(x, y))
    allow = lambda x, y: y >= fringe[x] + 1
    tree = veins(7607, [((4, fringe[4] + 1), (1, 1)), ((12, fringe[12] + 1), (-1, 1))], length=10, allow=allow)
    return infect(base, 7611, tree, shards=1, spores=2, groove=EMBER, light=EMBER_GLOW, shard_skip=lambda p: p[1] > 12)


def infected_wart_block():
    """Warzenblock: weiche, runde Knoten; die Glut pocht zwischen den Knoten."""
    base = patches(7701, INF_WART, fractions=(0.04, 0.22, 0.46, 0.24, 0.04), cells=(4, 4), detail=0.3)
    for (x, y) in spots(7705, 5, 4.0):
        base.set(x, y, INF_WART[4])
        base.set((x + 1) % N, y, INF_WART[3])
        base.set(x, (y + 1) % N, INF_WART[3])
    return _pair(base, 7700, "ember", shards=0, spores=4, tint=0.4)


# ------------------------------------------------------------------ End (Leerenfaeule)
def infected_end_stone(v=0):
    """Endstein: blasse, poroese Flecken mit Loechern; die Faeule frisst schwarzviolette Rillen mit blassgruenem Kern."""
    base = patches(7801 + v, INF_END, fractions=(0.04, 0.22, 0.46, 0.24, 0.04), cells=((4, 4), (3, 5), (5, 3))[(v // 1000) % 3], detail=0.35, warp=0.85)
    for (x, y) in spots(7805 + v, 5, 3.6):
        base.set(x, y, INF_END[0])
    return _pair(base, 7800 + v, "void", shards=1, spores=3, tint=0.6)


def infected_purpur():
    """Purpurkacheln (2x2 Felder mit heller Ober-/Linkskante), von Faeuleadern durchzogen."""
    idx = patch_idx(7901, fractions=(0.0, 0.25, 0.5, 0.25, 0.0), cells=(4, 4), detail=0.2)
    t = Tex(N, N)
    for y in range(N):
        for x in range(N):
            lx, ly = x % 8, y % 8
            if lx == 7 or ly == 7:
                c = 0
            elif lx == 0 or ly == 0:
                c = 4
            else:
                c = max(1, min(3, idx[y][x]))
            t.set(x, y, INF_PURPUR[c])
    return _pair(t, 7900, "void", shards=1, spores=2, tint=0.5)


# ------------------------------------------------------------------ Wesen (Vanilla-UV, nur Texturen)
# Kristallisierter Piglin (Glutbrand, Piglin-Modell 64x64) und kristallisierter Enderman (Leerenfaeule, 64x32).
EMBER_CRY = [hx(c) for c in ("#4a1206", "#9a3208", "#e0601a", "#ffa43a", "#fff0b0")]
VOID_CRY = [hx(c) for c in ("#123a33", "#1f6e5c", "#3fb894", "#9ff2cf", "#f0fff6")]
PSKIN = [hx(c) for c in ("#3a1a22", "#5a2a33", "#7c3d46", "#9c5559", "#b9706f")]
PLEATHER = [hx(c) for c in ("#1c120c", "#2c1d13", "#40291b", "#563824", "#6e4a30")]
PGOLD = [hx(c) for c in ("#5a3a08", "#8a5e10", "#c08a1c", "#e8b838", "#fff07a")]
ESKIN = [hx(c) for c in ("#030305", "#08070c", "#100d17", "#181423", "#221c31")]

P_HEAD, P_SNOUT = (0, 0, 10, 8, 8), (31, 1, 4, 4, 1)
P_TUSK_R, P_TUSK_L = (2, 0, 1, 2, 1), (2, 4, 1, 2, 1)
P_EAR_R, P_EAR_L = (39, 6, 1, 5, 4), (51, 6, 1, 5, 4)
P_BODY, P_RARM, P_LARM = (16, 16, 8, 12, 4), (40, 16, 4, 12, 4), (32, 48, 4, 12, 4)
P_RLEG, P_LLEG = (0, 16, 4, 12, 4), (16, 48, 4, 12, 4)
E_HEAD, E_JAW, E_BODY, E_LIMB = (0, 0, 8, 8, 8), (0, 16, 8, 8, 8), (32, 16, 8, 12, 4), (56, 0, 2, 30, 2)


def shade(r, bias=2.2, grain=0.45):
    def p(face, fx, fy, fw, fh):
        return ramp(r, bias + FACE_LIGHT[face] * 0.7 + edge_light(fx, fy, fw, fh) * 0.4 + ordered(fx, fy) * grain)
    return p


def crusted(base, cells, cry, faces=("north",)):
    """Legt Kristallkrusten {(fx, fy): Stufe 0-4} auf die genannten Flaechen; ab Stufe 2 leuchtend."""
    def p(face, fx, fy, fw, fh):
        if face in faces:
            c = cells.get((fx, fy))
            if c is not None:
                return (ramp(cry, c), c >= 2)
        return base(face, fx, fy, fw, fh)
    return p


def piglin_head():
    base = shade(PSKIN, 2.3)
    crust = {(7, 0): 4, (8, 0): 3, (8, 1): 3, (9, 1): 2, (7, 1): 2, (9, 2): 2, (0, 6): 2, (0, 7): 3, (1, 7): 2}

    def p(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 3 and fx in (2, 7):
                return (EMBER_GLOW[4], True)          # gluehende Augen
            if fy == 3 and fx in (1, 3, 6, 8):
                return PSKIN[0]
            if fy == 2 and 1 <= fx <= 3 or fy == 2 and 6 <= fx <= 8:
                return PSKIN[1]                       # schwere Brauen
            if fx == 5 and fy in (0, 1):
                return (EMBER_GLOW[1], True)          # Riss in der Stirn
        if face == "up" and (fx, fy) in ((3, 3), (4, 3), (4, 4), (6, 5)):
            return (ramp(EMBER_CRY, 3), True)
        return crusted(base, crust, EMBER_CRY, ("north", "east", "west"))(face, fx, fy, fw, fh)
    return p


def piglin_snout(face, fx, fy, fw, fh):
    if face == "north" and fy == 2 and fx in (1, 2):
        return PSKIN[0]                               # Nasenloecher
    return ramp(PSKIN, 3.0 + FACE_LIGHT[face] * 0.6 + edge_light(fx, fy, fw, fh) * 0.3)


def piglin_body():
    base = shade(PLEATHER, 2.0)

    def p(face, fx, fy, fw, fh):
        if fy in (8, 9) and face in ("north", "south", "east", "west"):
            return ramp(PGOLD, 2.6 + (0.8 if fy == 8 else -0.4) + ordered(fx, fy) * 0.3)    # Goldguertel
        if face == "north":
            c = {(4, 2): 4, (5, 2): 3, (4, 3): 3, (3, 3): 2, (5, 3): 2, (4, 4): 2, (3, 5): 1}.get((fx, fy))
            if c is not None:
                return (ramp(EMBER_CRY, c), c >= 2)
            if (fx, fy) in ((2, 5), (2, 6), (1, 7)):
                return (EMBER_GLOW[1], True)
        if fy < 3 and face in ("north", "south") and fx in (0, 7):
            return ramp(PSKIN, 2.0)                    # nackte Schultern
        return base(face, fx, fy, fw, fh)
    return p


def piglin_arm(seed):
    sk = shade(PSKIN, 2.2)
    cells = {(1, 6): 3, (2, 6): 4, (1, 7): 2, (2, 7): 3, (2, 8): 2} if seed % 2 else {(2, 2): 3, (1, 3): 2, (2, 3): 4}

    def p(face, fx, fy, fw, fh):
        if fy == 10 and face != "up":
            return ramp(PGOLD, 2.8 + FACE_LIGHT[face] * 0.4)   # Goldreif am Handgelenk
        return crusted(sk, cells, EMBER_CRY, ("north", "east"))(face, fx, fy, fw, fh)
    return p


def piglin_leg(seed):
    pants = shade(PLEATHER, 2.1)
    sk = shade(PSKIN, 2.0)

    def p(face, fx, fy, fw, fh):
        if fy >= 9 and face != "up":
            if fy == 11 and (fx + seed) % 2 == 0 and face in ("north", "south"):
                return (ramp(EMBER_CRY, 3), True)
            return sk(face, fx, fy, fw, fh)
        return pants(face, fx, fy, fw, fh)
    return p


def crystallized_piglin_texture():
    t, g = Tex(64, 64), Tex(64, 64)
    paint_box(t, *P_HEAD, piglin_head(), g)
    paint_box(t, *P_SNOUT, piglin_snout, g)
    for box in (P_TUSK_R, P_TUSK_L):
        paint_box(t, *box, lambda face, fx, fy, fw, fh: (ramp(EMBER_CRY, 3.4 - fy * 0.8), fy == 0), g)
    for box in (P_EAR_R, P_EAR_L):
        paint_box(t, *box, lambda face, fx, fy, fw, fh: ((ramp(EMBER_CRY, 3), True) if fy == fh - 1 and fx % 2 == 0
                                                       else ramp(PSKIN, 2.2 + FACE_LIGHT[face] * 0.6)), g)
    paint_box(t, *P_BODY, piglin_body(), g)
    paint_box(t, *P_RARM, piglin_arm(1), g)
    paint_box(t, *P_LARM, piglin_arm(2), g)
    paint_box(t, *P_RLEG, piglin_leg(1), g)
    paint_box(t, *P_LLEG, piglin_leg(2), g)
    t.save(TEX / "entity" / "crystallized_piglin.png")
    g.save(TEX / "entity" / "crystallized_piglin_glowmask.png")
    return t, g


def enderman_head():
    base = shade(ESKIN, 2.0, 0.35)
    crust = {(1, 0): 3, (2, 0): 4, (2, 1): 3, (6, 1): 2, (6, 0): 3}

    def p(face, fx, fy, fw, fh):
        if face == "north" and fy == 4:
            if fx in (0, 2, 5, 7):
                return (VOID_GLOW[2], True)
            if fx in (1, 6):
                return (VOID_GLOW[4], True)          # blassgruene Augen statt violett
        if face == "up":
            c = {(2, 2): 4, (3, 2): 3, (2, 3): 3, (5, 5): 3, (5, 4): 2}.get((fx, fy))
            if c is not None:
                return (ramp(VOID_CRY, c), True)
        return crusted(base, crust, VOID_CRY, ("north", "east", "west"))(face, fx, fy, fw, fh)
    return p


def enderman_body():
    base = shade(ESKIN, 2.0, 0.35)
    cells = {(5, 3): 4, (6, 3): 3, (5, 4): 3, (6, 4): 2, (4, 4): 2, (5, 5): 2, (3, 8): 2, (3, 9): 3}

    def p(face, fx, fy, fw, fh):
        if face == "north" and (fx, fy) in ((4, 6), (4, 7), (3, 7)):
            return (VOID_GLOW[1], True)
        return crusted(base, cells, VOID_CRY, ("north", "south"))(face, fx, fy, fw, fh)
    return p


def enderman_limb(face, fx, fy, fw, fh):
    if face in ("north", "east") and fy in (7, 8, 19, 20) and fx == 0:
        return (ramp(VOID_CRY, 3 if fy in (7, 19) else 2), True)
    return ramp(ESKIN, 1.9 + FACE_LIGHT[face] * 0.7 + ordered(fx, fy) * 0.35)


def crystallized_enderman_texture():
    t, g = Tex(64, 32), Tex(64, 32)
    paint_box(t, *E_HEAD, enderman_head(), g)
    paint_box(t, *E_JAW, shade(ESKIN, 1.8, 0.3), g)
    paint_box(t, *E_BODY, enderman_body(), g)
    paint_box(t, *E_LIMB, enderman_limb, g)
    t.save(TEX / "entity" / "crystallized_enderman.png")
    g.save(TEX / "entity" / "crystallized_enderman_glowmask.png")
    return t, g


def mob_collars():
    for size, box, name in (((64, 64), P_BODY, "crystallized_piglin"), ((64, 32), E_BODY, "crystallized_enderman")):
        t = Tex(*size)
        paint_box(t, *box, collar_band(top_rows=2))
        t.save(TEX / "entity" / f"{name}_collar.png")


def save_pair(name, pair, animate=False):
    base, glow = pair
    save_png(base, f"block/{name}")
    if animate:
        save_png(pulse_frames(glow), f"block/{name}_glow")
        write_mcmeta(f"{name}_glow", 10)
    else:
        save_png(glow, f"block/{name}_glow")


# Name -> (Funktion, Varianten, animiert)
BLOCKS = {
    "infected_deepslate": (infected_deepslate, 3, False),
    "infected_planks": (infected_planks, 1, False),
    "infected_netherrack": (infected_netherrack, 3, True),
    "infected_blackstone": (infected_blackstone, 1, False),
    "infected_soul_soil": (infected_soul_soil, 1, True),
    "infected_nylium_top": (infected_nylium_top, 1, True),
    "infected_nylium_side": (infected_nylium_side, 1, True),
    "infected_wart_block": (infected_wart_block, 1, True),
    "infected_end_stone": (infected_end_stone, 3, False),
    "infected_purpur": (infected_purpur, 1, False),
}


def main():
    count = 0
    for name, (fn, variants, animate) in BLOCKS.items():
        save_pair(name, fn() if variants == 1 else fn(0), animate)
        count += 1
        for n in range(2, variants + 1):
            save_pair(f"{name}_v{n}", fn(VARIANT_SEEDS[n - 1]), animate)
            count += 1
    crystallized_piglin_texture()
    crystallized_enderman_texture()
    mob_collars()
    print(f"virus_strains: {count} Blocktexturen + Leuchtschichten (Shardvirus, Glutbrand, Leerenfaeule), "
          "crystallized_piglin, crystallized_enderman (+Glowmask, Kragen)")


if __name__ == "__main__":
    main()
