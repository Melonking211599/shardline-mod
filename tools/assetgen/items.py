"""Item-Texturen (16x16) der Shardline-Mod. Aufruf: python tools/assetgen/items.py

Konturen werden automatisch gesetzt (Tex.outline). Lichtquelle: oben links.
"""
from __future__ import annotations

import math

from common import (ASH, BONE, COL, CYAN, LILAC, MAGENTA, OUTLINE, SHARD, SLATE, TEAL, VIOLET, Tex, clamp, hx,
                    mix, nz, ramp, save_png, with_alpha)

EDGE = hx("#10091c")


# ------------------------------------------------------------------ Geometriehelfer
def inside(pts, x, y):
    """Punkt-in-Polygon (even-odd) fuer das Pixelzentrum."""
    px, py = x + 0.5, y + 0.5
    ok = False
    j = len(pts) - 1
    for i in range(len(pts)):
        xi, yi = pts[i]
        xj, yj = pts[j]
        if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi + 1e-9) + xi:
            ok = not ok
        j = i
    return ok


def poly_mask(pts, w=16, h=16):
    return {(x, y) for y in range(h) for x in range(w) if inside(pts, x, y)}


def shade_mask(t: Tex, mask, r, lo=1.0, hi=3.4, dirx=-1.0, diry=-1.0, edge_fx=True, seed=0):
    """Faerbt eine Pixelmenge mit einem Licht-Verlauf (heller links oben) und Kantenlicht."""
    if not mask:
        return
    xs = [p[0] for p in mask]
    ys = [p[1] for p in mask]
    cx, cy = (min(xs) + max(xs)) / 2, (min(ys) + max(ys)) / 2
    span = max(1.0, (max(xs) - min(xs) + max(ys) - min(ys)) / 2)
    for (x, y) in mask:
        s = ((x - cx) * dirx + (y - cy) * diry) / (2 * span) + 0.5
        idx = lo + clamp(s, 0, 1) * (hi - lo)
        if edge_fx:
            if (x - 1, y) not in mask or (x, y - 1) not in mask:
                idx += 0.7
            if (x + 1, y) not in mask or (x, y + 1) not in mask:
                idx -= 0.7
        t.set(x, y, ramp(r, idx))


def new() -> Tex:
    return Tex(16, 16)


def finish(t: Tex, edge=None) -> Tex:
    """Kontur setzen: ohne feste Farbe weich (dunklerer Ton der Nachbarpixel, wie bei Vanilla), sonst die gegebene."""
    if edge is None:
        t.selout(0.38, EDGE, 0.22)
    else:
        t.outline(edge)
    return t


# ------------------------------------------------------------------ Rohstoffe
def shard_fragment():
    t = new()
    m = poly_mask([(11, 1), (14, 5), (8, 14), (4, 10)])
    shade_mask(t, m, SHARD, 1.2, 3.6)
    # Mittelkante und Glanz
    for (x, y) in ((10, 3), (10, 4), (9, 6), (9, 7), (8, 9), (7, 11)):
        t.set(x, y, SHARD[1] if y > 5 else SHARD[2])
    t.set(11, 3, SHARD[4])
    t.set(10, 2, SHARD[4])
    t.set(6, 10, SHARD[3])
    return finish(t)


def raw_shard():
    t = new()
    dull = [mix(SHARD[i], SLATE[min(i + 1, 4)], 0.45) for i in range(5)]
    a = poly_mask([(4, 4), (9, 3), (11, 8), (8, 13), (3, 11)])
    b = poly_mask([(9, 6), (13, 7), (14, 12), (10, 13), (8, 10)])
    shade_mask(t, a, dull, 1.0, 3.2)
    shade_mask(t, b, dull, 0.8, 2.8)
    for (x, y) in ((6, 5), (5, 6), (11, 8), (12, 9)):
        t.set(x, y, SHARD[2])
    t.set(6, 6, SHARD[3])
    for (x, y) in ((7, 10), (10, 11), (5, 9)):
        t.set(x, y, SLATE[0])
    return finish(t)


def shard_ingot():
    t = new()
    top = poly_mask([(2, 9), (9, 4), (14, 6), (7, 11)])
    front = poly_mask([(2, 9), (7, 11), (7, 14), (2, 12)])
    side = poly_mask([(7, 11), (14, 6), (14, 9), (7, 14)])
    shade_mask(t, top, SHARD, 2.4, 3.6, edge_fx=False)
    shade_mask(t, front, SHARD, 1.6, 2.1, edge_fx=False)
    shade_mask(t, side, SHARD, 0.8, 1.3, edge_fx=False)
    for (x, y) in ((4, 8), (5, 7), (6, 6), (7, 6)):
        t.set(x, y, SHARD[4])
    t.set(3, 10, SHARD[3])
    t.set(3, 11, SHARD[2])
    return finish(t)


def ash_dust():
    t = new()
    m = {(x, y) for y in range(7, 14) for x in range(16)
         if ((x - 7.5) / (6.5 - (13 - y) * 0.0)) ** 2 + ((y - 13.5) / 6.0) ** 2 <= 1.0 and y >= 7}
    shade_mask(t, m, ASH, 1.2, 3.6, dirx=-0.6, diry=-1.2)
    for k in range(10):
        x = 3 + int(nz(k, 0, 301) * 10)
        y = 8 + int(nz(k, 1, 301) * 5)
        if (x, y) in m:
            t.set(x, y, ASH[4] if k % 2 else ASH[1])
    for (x, y) in ((6, 5), (9, 4), (11, 6), (4, 6)):
        t.set(x, y, ASH[3])
    t.set(8, 3, ASH[4])
    t.set(7, 10, MAGENTA[1])
    return finish(t)


def scout_eye():
    t = new()
    m = {(x, y) for y in range(16) for x in range(16) if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 30.0}
    shade_mask(t, m, BONE, 2.2, 4.0)
    for (x, y) in m:
        if (x - 8) ** 2 + (y - 8) ** 2 <= 11:
            t.set(x, y, MAGENTA[2] if (x + y) % 5 else MAGENTA[3])
        if (x - 8) ** 2 + (y - 8) ** 2 <= 5:
            t.set(x, y, MAGENTA[0])
    t.rect(7, 7, 2, 2, hx("#07020d"))
    t.set(6, 6, WHITE_PIXEL)
    for (x, y) in ((3, 9), (4, 10), (11, 4), (12, 5)):
        if (x, y) in m:
            t.set(x, y, MAGENTA[1])
    return finish(t)


WHITE_PIXEL = hx("#ffffff")


def void_ray_hide():
    t = new()
    pts = [(1, 3), (6, 2), (9, 4), (14, 3), (14, 8), (12, 10), (13, 14), (8, 12), (4, 14), (3, 9), (1, 8)]
    m = poly_mask(pts)
    base = [hx(c) for c in ("#0d0a1f", "#17123a", "#241c5a", "#352a80", "#4d3fae")]
    shade_mask(t, m, base, 1.0, 3.0, edge_fx=False)
    for (x0, y0, x1, y1) in ((3, 4, 8, 8), (9, 5, 12, 9), (5, 10, 7, 12)):
        t.line(x0, y0, x1, y1, base[4])
    for (x, y) in ((4, 5), (10, 7), (6, 11)):
        t.set(x, y, CYAN[2])
    return finish(t)


def shadow_pelt():
    t = new()
    pts = [(3, 3), (6, 4), (8, 2), (10, 4), (13, 3), (13, 8), (11, 11), (12, 14), (8, 13), (4, 14), (4, 10), (2, 7)]
    m = poly_mask(pts)
    fur = [hx(c) for c in ("#0a0713", "#141020", "#1f1932", "#2e2548", "#443669")]
    shade_mask(t, m, fur, 1.0, 3.0)
    for k in range(14):
        x = 3 + int(nz(k, 0, 311) * 9)
        y = 4 + int(nz(k, 1, 311) * 9)
        if (x, y) in m:
            t.set(x, y, VIOLET[2] if k % 3 else fur[4])
            if (x, y + 1) in m:
                t.set(x, y + 1, fur[0])
    t.set(7, 6, MAGENTA[1])
    return finish(t)


def knight_crest():
    t = new()
    pts = [(3, 2), (13, 2), (13, 8), (8, 14), (3, 8)]
    m = poly_mask(pts)
    shade_mask(t, m, SLATE, 1.4, 3.6)
    for (x, y) in m:
        if x in (4, 12) or y == 3:
            t.set(x, y, SHARD[1] if (x + y) % 2 else SHARD[2])
    # Splitter-Emblem in der Mitte
    sh = poly_mask([(8, 4), (11, 8), (8, 12), (5, 8)])
    shade_mask(t, sh, SHARD, 1.0, 3.8, edge_fx=False)
    t.set(7, 6, SHARD[4])
    t.set(8, 8, MAGENTA[3])
    return finish(t)


def luminous_berry():
    t = new()
    for (cx, cy, r) in ((5.5, 9.5, 3.4), (10.5, 7.5, 3.4)):
        m = {(x, y) for y in range(16) for x in range(16) if (x + .5 - cx) ** 2 + (y + .5 - cy) ** 2 <= r * r}
        shade_mask(t, m, MAGENTA, 1.2, 3.4)
        t.set(int(cx) - 1, int(cy) - 1, MAGENTA[4])
    for (x, y) in ((8, 3), (9, 3), (10, 3), (9, 4), (8, 5), (7, 6), (10, 4), (11, 4)):
        t.set(x, y, TEAL[3] if x > 8 else TEAL[2])
    t.set(7, 4, TEAL[1])
    return finish(t, hx("#2a0825"))


# ------------------------------------------------------------------ Fortschritt
def shard_key():
    t = new()
    # Ring oben rechts
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 11, y + .5 - 4.5)
            if 1.6 <= d <= 3.8:
                t.set(x, y, ramp(SHARD, 3.4 - (x - 8) * 0.18 + (y - 2) * -0.1 if False else 3.5 - (x + y - 12) * 0.12))
    t.set(11, 4, CYAN[3])
    t.set(10, 4, CYAN[2])
    # Schaft diagonal mit Zaehnen
    for i in range(9):
        t.set(9 - i, 7 + i, SHARD[3] if i % 2 == 0 else SHARD[2])
        t.set(10 - i, 7 + i, SHARD[1])
    for (x, y) in ((6, 12), (5, 13), (3, 12), (4, 13)):
        t.set(x, y, SHARD[2])
    t.set(4, 14, SHARD[1])
    t.set(2, 14, SHARD[0])
    return finish(t)


def _spire(t, x_l, x_r, y_base, tip_x, tip_y, r):
    from blocks import crystal_spire
    crystal_spire(t, x_l, x_r, y_base, tip_x, tip_y, r)


def colossus_summoning_crystal():
    t = new()
    # Sockel
    t.rect(4, 13, 8, 2, SLATE[2])
    t.rect(4, 13, 8, 1, SLATE[4])
    t.rect(4, 14, 8, 1, SLATE[0])
    _spire(t, 5, 10, 13, 7, 1, CYAN)
    # Leuchtkern
    t.set(7, 7, CYAN[4])
    t.set(7, 8, CYAN[4])
    t.set(8, 8, CYAN[3])
    t.set(6, 6, CYAN[3])
    return finish(t, hx("#06303b"))


def colossus_core():
    t = new()
    # Oktaeder aus vier Dreiecken
    tl = poly_mask([(8, 1), (8, 8), (2, 8)])
    tr = poly_mask([(8, 1), (14, 8), (8, 8)])
    bl = poly_mask([(2, 8), (8, 8), (8, 15)])
    br = poly_mask([(8, 8), (14, 8), (8, 15)])
    shade_mask(t, tl, CYAN, 3.0, 3.6, edge_fx=False)
    shade_mask(t, tr, CYAN, 2.2, 2.7, edge_fx=False)
    shade_mask(t, bl, CYAN, 1.5, 2.0, edge_fx=False)
    shade_mask(t, br, CYAN, 0.7, 1.2, edge_fx=False)
    t.rect(7, 7, 2, 2, CYAN[4])
    t.set(7, 6, CYAN[4])
    t.set(6, 7, CYAN[4])
    for (x, y) in ((3, 3), (13, 3), (3, 13), (13, 13)):
        t.set(x, y, MAGENTA[3])
    return finish(t, hx("#06303b"))


def colossus_upgrade_smithing_template():
    t = new()
    t.rect(1, 1, 14, 14, SLATE[2])
    t.frame(1, 1, 14, 14, SLATE[4])
    t.rect(2, 13, 13, 1, SLATE[1])
    t.rect(13, 2, 1, 12, SLATE[1])
    t.frame(2, 2, 12, 12, CYAN[0])
    # Ecknieten
    for (x, y) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        t.set(x, y, CYAN[2])
    # Kristallsymbol
    _spire(t, 6, 9, 11, 7, 3, CYAN)
    t.set(7, 6, CYAN[4])
    # Pfeil nach unten zeigt: wird veredelt
    t.set(11, 7, MAGENTA[3])
    t.set(11, 8, MAGENTA[3])
    t.set(10, 9, MAGENTA[2])
    t.set(12, 9, MAGENTA[2])
    t.set(11, 9, MAGENTA[3])
    return finish(t)


def heart_of_the_expanse():
    t = new()
    pts = [(8, 4), (10, 2), (13, 2), (15, 5), (14, 9), (8, 15), (2, 9), (1, 5), (3, 2), (6, 2)]
    m = poly_mask(pts)
    shade_mask(t, m, MAGENTA, 1.0, 3.6)
    # Cyan-Facetten im Inneren
    inner = poly_mask([(8, 6), (10, 5), (12, 6), (11, 9), (8, 12), (5, 9), (4, 6), (6, 5)])
    shade_mask(t, inner, CYAN, 0.8, 3.0, edge_fx=False)
    t.set(5, 4, MAGENTA[4])
    t.set(4, 5, MAGENTA[4])
    t.set(7, 7, CYAN[4])
    t.set(8, 8, CYAN[4])
    return finish(t, hx("#2a0825"))


# ------------------------------------------------------------------ Werkzeuge
# Wie bei Vanilla: schlanker 1px-Griff, klar erkennbarer Kopf, 3 bis 5 Toene, Licht von links oben.
# Zeichen in der ASCII-Kunst: 1-5 = Materialrampe (dunkel -> hell), a-e = Schiefer (Parierstange),
# h/i/j = Griff, g = leuchtend, m = Akzent. Die Kontur setzt finish() automatisch.
HANDLE = [hx(c) for c in ("#1a1328", "#4a3d6e", "#6a5a94", "#9283bd")]


def _pal(r, glow=None, acc=None, guard=SLATE):
    p = {str(i + 1): r[i] for i in range(5)}
    p.update({"h": HANDLE[1], "i": HANDLE[2], "j": HANDLE[3]})
    p.update({k: guard[n] for n, k in enumerate("abcde")})
    if glow is not None:
        p["g"] = glow
    if acc is not None:
        p["m"] = acc
    return p


def _art(t: Tex, rows: str, p):
    for y, row in enumerate(rows.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch != ".":
                t.set(x, y, p[ch])


def _handle(t, p, x0, y0, n):
    """1px-Griff, diagonal von (x0, y0) nach oben rechts."""
    for k in range(n):
        t.set(x0 + k, y0 - k, p["i"] if k == 0 else p["j"])


def _blade_sword(r, glow=None, guard=SLATE, acc=None, wide=False, pommel=None):
    p = _pal(r, glow, acc, guard)
    t = new()
    # Klinge: Diagonalen D = x + y. Schmal: 14/15, breit: 13/14/15.
    for y in range(0, 9):
        for D in ((13, 14, 15) if wide else (14, 15)):
            if y == 0 and D != 14:
                continue
            x = D - y
            if wide:
                v = {13: 4, 14: 3, 15: 2}[D]
            else:
                v = {14: 4, 15: 2}[D]
            if y <= 1 and D == 14:
                v = 5
            if D == 13 and y == 1:
                v = 5
            t.set(x, y, p[str(v)])
    if glow is not None:
        for y in range(2, 8):
            t.set(14 - y, y, p["g"])
    # Parierstange (2px diagonal quer zur Klinge)
    for k in range(5):
        t.set(3 + k, 7 + k, p["d"])
        t.set(4 + k, 7 + k, p["b"])
    t.set(3, 7, p["e"])
    t.set(8, 11, p["a"])
    if acc is not None:
        t.set(5, 9, p["m"])
    # Griff und Knauf
    for (x, y), c in zip(((5, 10), (4, 11), (3, 12), (2, 13)), ("j", "i", "j", "i")):
        t.set(x, y, p[c])
    t.set(1, 14, p["m"] if pommel is None and acc is not None else (pommel or p["e"]))
    return finish(t)


def shard_sword():
    return _blade_sword(SHARD, guard=[SLATE[2], SLATE[3], SLATE[4], BONE[3], BONE[4]], acc=MAGENTA[3], pommel=MAGENTA[3])


def colossus_sword():
    return _blade_sword(COL, glow=CYAN[4], guard=[SLATE[2], COL[2], COL[2], COL[3], CYAN[3]], acc=CYAN[3],
                        wide=True, pommel=CYAN[3])


def queens_shardblade():
    """Breite Magenta-Klinge mit seitlich abstehenden Splittern und Kronen-Parierstange."""
    p = _pal(MAGENTA, CYAN[3], CYAN[3], SHARD)
    t = new()
    for y in range(0, 9):
        for D in (13, 14, 15):
            if y == 0 and D != 14:
                continue
            x = D - y
            v = {13: 4, 14: 3, 15: 2}[D]
            if y <= 1 and D in (13, 14):
                v = 5
            t.set(x, y, p[str(v)])
    for y in range(2, 8):
        if y % 2 == 0:
            t.set(14 - y, y, p["g"])
    # Splitter-Zacken an den Seiten
    for (x, y, v) in ((9, 3, 4), (8, 4, 5), (6, 6, 4), (5, 7, 5), (13, 3, 1), (12, 4, 2), (10, 6, 1)):
        t.set(x, y, p[str(v)])
    # Krone als Parierstange
    for k in range(5):
        t.set(3 + k, 7 + k, p["d"])
        t.set(4 + k, 7 + k, p["b"])
    t.set(3, 7, p["e"])
    t.set(3, 6, p["e"])
    t.set(8, 11, p["a"])
    t.set(9, 11, p["a"])
    t.set(5, 9, p["m"])
    for (x, y), c in zip(((5, 10), (4, 11), (3, 12), (2, 13)), ("j", "i", "j", "i")):
        t.set(x, y, p[c])
    t.set(1, 14, p["m"])
    return finish(t, hx("#2a0825"))


# Koepfe nach Vanilla-Proportionen: Spitzhacke mit Bogen und nach unten reichendem rechten Arm, Axt mit Keilklinge
# oben rechts, Spaten mit rundem Blatt. Zeichen 1-5 = Materialrampe (dunkel -> hell).
PICK = """
................
................
................
......5544332...
......4....32...
...........32...
............32..
............32..
............22..
.............1..
"""

AXE = """
................
................
..........54....
.........5443...
........554332..
.......5544332..
.......443322...
........33221...
..........21....
"""

SHOVEL = """
................
................
..........5543..
.........554432.
.........544332.
.........443322.
.........44332..
..........322...
"""


def _head_tool(rows, r, glow=None, glow_px=(), n=8):
    p = _pal(r, glow)
    t = new()
    _handle(t, p, 2, 13, n)
    _art(t, rows, p)
    for (x, y) in glow_px:
        t.set(x, y, p["g"])
    return finish(t)


def _shovel(r, glow=None):
    return _head_tool(SHOVEL, r, glow, ((12, 4),) if glow is not None else (), n=7)


def shard_pickaxe():
    return _head_tool(PICK, SHARD, n=10)


def shard_axe():
    return _head_tool(AXE, SHARD, n=9)


def shard_shovel():
    return _shovel(SHARD)


def colossus_pickaxe():
    return _head_tool(PICK, COL, CYAN[4], ((8, 3),), n=10)


def colossus_axe():
    return _head_tool(AXE, COL, CYAN[4], ((10, 4),), n=9)


def colossus_shovel():
    return _shovel(COL, CYAN[4])


# ------------------------------------------------------------------ Ruestungs-Icons
# Vanilla-Silhouetten. Es wird nur die linke Haelfte (8 Spalten) gezeichnet; die rechte entsteht gespiegelt
# und eine Stufe dunkler (Licht von links). g = leuchtender Edelstein.
_DARKER = {"5": "4", "4": "3", "3": "2", "2": "2", "1": "1"}


def _mirror(rows):
    out = []
    for row in rows.strip("\n").split("\n"):
        row = row.ljust(8, ".")[:8]
        out.append(row + "".join(_DARKER.get(c, c) for c in reversed(row)))
    while len(out) < 16:
        out.append("." * 16)
    return "\n".join(out)


HELMET = _mirror("""
........
........
.....555
....5444
...54444
..544444
..54443g
..544433
..522222
..5433..
..4433..
..333...
""")

CHEST = _mirror("""
........
..5544..
.54444..
.5444433
.5444433
.444433g
.443133g
.4331333
.3321333
....4333
....4333
....2222
....3322
....2211
""")

LEGGINGS = _mirror("""
........
........
..555444
..54443g
..422222
..5443..
..5443..
..4433..
..5543..
..4433..
..4433..
..4433..
..3322..
..2211..
""")

BOOTS = _mirror("""
........
........
........
..5555..
..5444..
..4443..
..4433..
..4433..
..5433..
.55443..
554443..
444333..
432221..
221111..
""")


def armor_icon(art, r, glow, trim):
    p = _pal(r, glow)
    t = new()
    _art(t, art, p)
    return finish(t)


def armor_set(prefix, r, glow, trim):
    return {
        f"item/{prefix}_helmet": armor_icon(HELMET, r, glow, trim),
        f"item/{prefix}_chestplate": armor_icon(CHEST, r, glow, trim),
        f"item/{prefix}_leggings": armor_icon(LEGGINGS, r, glow, trim),
        f"item/{prefix}_boots": armor_icon(BOOTS, r, glow, trim),
    }


# ------------------------------------------------------------------ Spezialwaffen
def colossus_gauntlet():
    t = new()
    # Geballte Faust von der Seite: Fingerblock, Daumen, Manschette mit Kristall
    fist = poly_mask([(3, 3), (11, 2), (14, 4), (14, 9), (12, 11), (4, 11), (2, 8)])
    shade_mask(t, fist, COL, 1.2, 3.6)
    for y in (5, 7, 9):
        t.line(5, y, 13, y, COL[0])
    t.line(4, 5, 4, 9, COL[1])
    for (x, y) in ((6, 4), (8, 4), (10, 4), (12, 5)):
        t.set(x, y, COL[4])
    cuff = poly_mask([(3, 11), (13, 11), (13, 15), (3, 15)])
    shade_mask(t, cuff, SLATE, 1.0, 3.2, edge_fx=False)
    t.rect(3, 11, 11, 1, COL[3])
    t.rect(3, 14, 11, 1, SLATE[0])
    for x in range(4, 13, 2):
        t.set(x, 13, CYAN[2])
    _spire(t, 7, 9, 11, 8, 6, CYAN)
    t.set(8, 7, CYAN[4])
    return finish(t, hx("#06303b"))


def crystal_bow_state(pull: int, arrow: bool):
    t = new()
    tips_a = (13, 1)
    tips_b = (1, 13)
    limb = CYAN
    # Bogenkoerper: Bulge zur oberen linken Ecke
    belly = 5.0 - pull * 0.3
    for v in range(0, 13):
        b = int(round(belly * (1 - ((v - 6) / 6.4) ** 2)))
        for k, shade in ((b, 3.6), (b - 1, 2.2)):
            x, y = 13 - v - k, 1 + v - k
            t.set(x, y, ramp(limb, shade - (0.8 if v > 8 else 0)))
            t.set(x + 1, y, ramp(limb, shade - 0.6 - (0.8 if v > 8 else 0)))
    for (x, y) in ((4, 4), (3, 3)):
        t.set(x, y, CYAN[4])
    # Griff
    t.set(2, 3, SLATE[3])
    t.set(3, 2, SLATE[3])
    # Sehne
    string = hx("#e4daf3")
    nx, ny = 7 + pull * 2 + (1 if pull else 0), 7 + pull * 2 + (1 if pull else 0)
    if pull == 0:
        nx, ny = 7, 7
    t.line(tips_a[0], tips_a[1], nx, ny, string)
    t.line(nx, ny, tips_b[0], tips_b[1], string)
    if arrow:
        t.line(nx - 1, ny - 1, nx - 6, ny - 6, SLATE[4])
        t.set(nx - 7, ny - 7, CYAN[4])
        t.set(nx - 6, ny - 7, CYAN[3])
        t.set(nx - 7, ny - 6, CYAN[2])
        t.set(nx, ny, MAGENTA[3])
    return finish(t, hx("#06303b"))


def build_all():
    items = {
        "item/shard_fragment": shard_fragment,
        "item/raw_shard": raw_shard,
        "item/shard_ingot": shard_ingot,
        "item/ash_dust": ash_dust,
        "item/scout_eye": scout_eye,
        "item/void_ray_hide": void_ray_hide,
        "item/shadow_pelt": shadow_pelt,
        "item/knight_crest": knight_crest,
        "item/luminous_berry": luminous_berry,
        "item/shard_key": shard_key,
        "item/colossus_summoning_crystal": colossus_summoning_crystal,
        "item/colossus_core": colossus_core,
        "item/colossus_upgrade_smithing_template": colossus_upgrade_smithing_template,
        "item/heart_of_the_expanse": heart_of_the_expanse,
        "item/shard_sword": shard_sword,
        "item/shard_pickaxe": shard_pickaxe,
        "item/shard_axe": shard_axe,
        "item/shard_shovel": shard_shovel,
        "item/colossus_sword": colossus_sword,
        "item/colossus_pickaxe": colossus_pickaxe,
        "item/colossus_axe": colossus_axe,
        "item/colossus_shovel": colossus_shovel,
        "item/queens_shardblade": queens_shardblade,
        "item/colossus_gauntlet": colossus_gauntlet,
        "item/crystal_bow": lambda: crystal_bow_state(0, False),
        "item/crystal_bow_pulling_0": lambda: crystal_bow_state(0, True),
        "item/crystal_bow_pulling_1": lambda: crystal_bow_state(1, True),
        "item/crystal_bow_pulling_2": lambda: crystal_bow_state(2, True),
    }
    out = {name: fn() for name, fn in items.items()}
    out.update(armor_set("shard", SHARD, MAGENTA[3], SLATE))
    out.update(armor_set("colossus", COL, CYAN[3], SLATE))
    return out


def main():
    imgs = build_all()
    for name, tex in imgs.items():
        save_png(tex, name)
    print(f"items: {len(imgs)} Texturen")


if __name__ == "__main__":
    main()
