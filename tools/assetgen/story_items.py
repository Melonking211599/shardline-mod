"""Story-Items: Lore-Buch, Reinigungsessenz, Kern des Bruchs, Umhang der Hueter. Aufruf: python tools/assetgen/story_items.py

Dazu als Zusatz: 12 Lore-Buch-Cover (item/lore_book_<id>.png). Sie sind NICHT im Code verdrahtet; der Code kennt nur
ein einziges `lore_book`-Icon. Siehe README-Hinweis in der Ausgabe von run_all.

Reinheitspalette (Gold, Weiss, Hellblau) steht fuer alles, was den Virus bekaempft; Viruspalette fuer den Virus.
"""
from __future__ import annotations

import math

from clean import ordered
from common import (BONE, GOLD, IVORY, LEATHER, SHARD, SKYB, SLATE, TEX, VGLOW, VIRUS, Tex, clamp, hx, mix, nz, ramp,
                    save_png)

EDGE = hx("#10091c")


def new() -> Tex:
    return Tex(16, 16)


def paint_rows(t: Tex, rows: str, pal: dict, ox=0, oy=0):
    for y, row in enumerate(rows.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch != ".":
                t.set(ox + x, oy + y, pal[ch])


def inside(pts, x, y):
    px, py = x + 0.5, y + 0.5
    ok, j = False, len(pts) - 1
    for i in range(len(pts)):
        xi, yi = pts[i]
        xj, yj = pts[j]
        if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi + 1e-9) + xi:
            ok = not ok
        j = i
    return ok


# ====================================================================== Buecher
EMBLEMS = {
    "crystal": ["..y..", ".yxy.", "yxxxy", ".yxy.", "..x.."],
    "flame": ["..y..", ".yxy.", ".yxy.", "yxxxy", ".xxx."],
    "rings": [".xxx.", "x...x", "x.y.x", "x...x", ".xxx."],
    "drip": [".zzz.", "zzzzz", "zzyzz", ".z.z.", "..z.."],
    "crown": ["y.y.y", "yxyxy", "xxxxx", "xyxyx", "....."],
    "lock": [".xxx.", "xyyyx", "xyzyx", "xyyyx", ".xxx."],
    "coin": [".xxx.", "xyyyx", "xyxyx", "xyyyx", ".xxx."],
    "gem": ["..x..", ".zxy.", "zzxyy", ".zxy.", "..x.."],
    "star": ["..y..", "..x..", "yxxxy", "..x..", "..y.."],
    "none": [".....", ".....", ".....", ".....", "....."],
}


def book(cover, trim, emblem: str, epal: dict, ribbon=None, strap=None, glow_corner=False) -> Tex:
    """Geschlossenes Buch von vorne. cover/trim: 5er-Rampen. Emblem: 5x5 aus EMBLEMS, epal: x,y,z -> Farbe."""
    t = new()
    for y in range(1, 15):
        for x in range(2, 14):
            idx = 2.5
            if x <= 3:
                idx -= 0.9                       # Buchruecken, dunkler
            if y == 1:
                idx += 0.9
            if y == 14:
                idx -= 1.0
            if x == 13:
                idx -= 0.8
            if x == 4:
                idx += 0.4
            t.set(x, y, ramp(cover, idx))
    # Seitenschnitt (Pergament) rechts
    for y in range(2, 14):
        t.set(13, y, ramp(IVORY, 2.6 if y % 2 else 2.0))
    # Rahmen aus Zierleiste
    for x in range(5, 13):
        t.set(x, 3, ramp(trim, 3.2))
        t.set(x, 12, ramp(trim, 1.8))
    for y in range(3, 13):
        t.set(5, y, ramp(trim, 3.0))
        t.set(12, y, ramp(trim, 1.6))
    t.set(5, 3, ramp(trim, 4)); t.set(12, 12, ramp(trim, 1.2))
    # Ruecken-Baender
    for y in (3, 7, 12):
        t.set(2, y, ramp(trim, 2.8)); t.set(3, y, ramp(trim, 2.2))
    # Emblem
    pal = dict(epal)
    for j, row in enumerate(EMBLEMS[emblem]):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(6 + i, 5 + j, pal[ch])
    if strap is not None:
        for y in range(1, 15):
            t.set(10, y, ramp(strap, 2.0))
            t.set(11, y, ramp(strap, 1.0))
        for (x, y) in ((9, 8), (10, 8), (11, 8), (9, 9), (10, 9), (11, 9)):
            t.set(x, y, ramp(GOLD, 3.0 if y == 8 else 1.6))
    if ribbon is not None:
        t.set(11, 14, ramp(ribbon, 3)); t.set(11, 15, ramp(ribbon, 2)); t.set(10, 15, ramp(ribbon, 2))
    t.outline(EDGE)
    return t


def tablet(stone, glyph, emblem: str, epal: dict, cracks=False, seed=0) -> Tex:
    """Steinerne Inschriftentafel mit angeschlagenen Ecken, eingemeisselten Zeilen und leuchtendem Symbol."""
    t = new()
    for y in range(1, 15):
        for x in range(2, 14):
            if (x, y) in ((2, 1), (13, 1), (2, 14), (13, 14)) or (seed % 2 and (x, y) in ((3, 1), (13, 2))):
                continue
            idx = 2.4 + ordered(x, y) * 0.81
            if y <= 2 or x <= 3:
                idx += 0.6
            if y >= 13 or x >= 12:
                idx -= 0.8
            t.set(x, y, ramp(stone, idx))
    # Gravur-Zeilen
    for y in (3, 12):
        for x in range(4, 12):
            if t.get(x, y)[3]:
                t.set(x, y, ramp(stone, 0.8))
    for y in (11, 13):
        pass
    for k, y in enumerate((10, 11)):
        for x in range(4, 12):
            if (x + k * 2) % 4 != 3:
                t.set(x, y, ramp(stone, 0.9))
    for j, row in enumerate(EMBLEMS[emblem]):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(6 + i, 4 + j, epal[ch])
    if cracks:
        for (x, y) in ((8, 2), (8, 3), (7, 4), (7, 5), (6, 6), (6, 7), (5, 8), (9, 9), (10, 10), (10, 11), (11, 12)):
            if t.get(x, y)[3]:
                t.set(x, y, VIRUS[0])
        for (x, y) in ((8, 3), (7, 5), (6, 7), (10, 10)):
            t.set(x, y, VGLOW[1])
    t.outline(EDGE)
    return t


def pal_book(x, y, z=None):
    p = {"x": x, "y": y}
    p["z"] = z if z is not None else x
    return p


def lore_covers():
    """12 Cover: Hueter-Tagebuecher gold, Haendlernotizen braun, Inschriften steinern."""
    out = {}
    out["wanderer_notes"] = book(LEATHER, LEATHER, "star", pal_book(LEATHER[4], GOLD[3]), strap=LEATHER)
    out["traders_ledger"] = book(LEATHER, GOLD, "coin", pal_book(GOLD[2], GOLD[4]), ribbon=SKYB)
    out["crystal_kingdom"] = book(GOLD, GOLD, "crystal", pal_book(SKYB[2], SKYB[4]), ribbon=SKYB)
    out["keepers_oath"] = book(GOLD, GOLD, "flame", pal_book(IVORY[3], hx("#ffffff")), ribbon=IVORY)
    out["the_resonance"] = book(GOLD, GOLD, "rings", pal_book(SKYB[2], SKYB[4]), ribbon=SKYB)
    out["first_infection"] = book(GOLD, GOLD, "drip", {"x": GOLD[2], "y": VGLOW[3], "z": VGLOW[1]}, ribbon=VIRUS)
    out["queen_selvaine"] = book(GOLD, GOLD, "crown", pal_book(IVORY[3], hx("#ffffff")), ribbon=IVORY)
    out["the_seal"] = book(GOLD, GOLD, "lock", {"x": GOLD[4], "y": GOLD[1], "z": SKYB[3]}, ribbon=SKYB)
    out["the_breach_core"] = book(GOLD, GOLD, "gem", {"x": IVORY[4], "y": GOLD[4], "z": VGLOW[2]}, ribbon=VIRUS)
    out["the_colossus"] = tablet(SLATE, None, "gem", {"x": hx("#84efff"), "y": hx("#2cc3d6"), "z": hx("#e3ffff")}, seed=1)
    out["the_last_knights"] = tablet(SLATE, None, "lock", {"x": hx("#cdc9dd"), "y": hx("#9f9ab4"), "z": hx("#ecebf6")}, seed=2)
    out["the_shattering"] = tablet(SLATE, None, "none", {"x": hx("#cdc9dd"), "y": hx("#9f9ab4"), "z": hx("#ecebf6")}, cracks=True, seed=3)
    return out


def lore_book():
    """Das im Code registrierte Standard-Icon: goldener Hueter-Einband mit violettem Splitter-Wappen."""
    return book(GOLD, GOLD, "crystal", pal_book(SHARD[3], SHARD[4]), ribbon=SHARD)


# ====================================================================== Reinigungsessenz
BOTTLE = """
................
......KKkk......
......kkkk......
......GgwG......
......GgggG......
.....GggggG.....
....GgwlllmG....
...GgwllllmnG...
...GglllllmnG...
...GgllllmmnG...
...GglllmmnnG...
...GgllmmnnqG...
...GgmmmnnqqG...
....GmnnnqqG....
.....GGGGGG.....
................
"""


def purification_essence():
    t = new()
    pal = {"K": hx("#b98a5a"), "k": hx("#7a5233"), "G": SKYB[1], "g": SKYB[3], "w": hx("#ffffff"),
           "l": GOLD[4], "m": GOLD[3], "n": GOLD[2], "q": GOLD[1]}
    rows = BOTTLE.strip("\n").split("\n")
    # Zeile 4 hat 11 Zeichen (Tippfehler abgefangen): auf 16 kuerzen/auffuellen
    rows = [r[:16].ljust(16, ".") for r in rows]
    paint_rows(t, "\n".join(rows), pal)
    # kleiner Glanz im Glas und ein paar Lichtfunken in der Fluessigkeit
    for (x, y) in ((9, 9), (6, 11), (10, 7)):
        t.set(x, y, hx("#fffbe0"))
    t.outline(EDGE)
    return t


# ====================================================================== Kern des Bruchs
def breach_core():
    """Zersprungene Kugel: links Virus (Magenta), rechts Reinheit (Gold), dazwischen ein leuchtender Zickzack-Riss."""
    t = new()
    R = 6.2
    vramp = [VIRUS[1], VIRUS[2], VIRUS[3], VIRUS[4], VGLOW[1]]
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            d = math.hypot(dx, dy)
            if d > R:
                continue
            crack = 8.0 + (0.9 if (y // 2) % 2 else -0.9)
            left = x + 0.5 < crack
            lit = (-dx - dy) / (2 * R)
            idx = 2.2 + lit * 1.8 - (0.8 if d > R - 1.0 else 0.0)
            t.set(x, y, ramp(vramp if left else GOLD, idx if left else idx + 0.3))
    # leuchtender Riss: links rosa, rechts weissgold
    for y in range(2, 14):
        crack = 8.0 + (0.9 if (y // 2) % 2 else -0.9)
        xl = int(crack - 0.5)
        if t.get(xl, y)[3]:
            t.set(xl, y, VGLOW[3])
        if t.get(xl + 1, y)[3]:
            t.set(xl + 1, y, hx("#fff6cc"))
    # je ein Glanzpunkt und ein Splitter pro Haelfte
    t.set(5, 5, VGLOW[4]); t.set(10, 5, hx("#ffffff"))
    for (x, y, col) in ((1, 3, VIRUS[4]), (2, 2, VGLOW[2]), (14, 3, GOLD[3]), (13, 2, GOLD[4])):
        t.set(x, y, col)
    t.outline(hx("#1a0a18"))
    return t


# ====================================================================== Umhang der Hueter (64x32)
def keeper_cape() -> Tex:
    t = Tex(64, 32)

    def fold(x, y, w):
        return math.sin((x + 0.5) / w * math.pi * 3.0) * 0.35

    # --- Aussenseite (Region 1,1, 10x16): was man von hinten sieht
    for y in range(16):
        for x in range(10):
            idx = 3.0 + fold(x, y, 10) - (y / 15.0) * 0.8 + (0.35 if y < 2 else 0.0)
            c = ramp(IVORY, idx)
            t.set(1 + x, 1 + y, c)
    # Kragenband (oben) und goldener Rand links/rechts
    for x in range(10):
        t.set(1 + x, 1, ramp(GOLD, 2.2))
        t.set(1 + x, 2, ramp(GOLD, 3.0))
        t.set(1 + x, 3, ramp(GOLD, 1.4))
    for y in range(3, 16):
        t.set(1, 1 + y, ramp(GOLD, 3.0))
        t.set(10, 1 + y, ramp(GOLD, 1.6))
        t.set(2, 1 + y, ramp(GOLD, 2.0)) if y < 15 else None
        t.set(9, 1 + y, ramp(GOLD, 1.2)) if y < 15 else None
    # Wappen: aufgehende Sonne ueber Kristall (Hueter), 6 breit, 9 hoch
    crest = [
        "..GG..",
        ".GWWG.",
        "GWWWWG",
        "..SS..",
        ".SSSS.",
        "SSBBSS",
        "SSBBSS",
        ".SSSS.",
        "..SS..",
    ]
    pal = {"G": GOLD[3], "W": hx("#fff6cc"), "S": SKYB[2], "B": SKYB[4]}
    for j, row in enumerate(crest):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(3 + i, 5 + j, pal[ch])
    for (x, y) in ((3, 4), (10 - 3, 4)):
        pass
    # Saum unten mit drei Spitzen (Alpha 0 dazwischen => gezackter Umhang)
    for x in range(10):
        t.set(1 + x, 16, ramp(GOLD, 3.0 if x % 3 == 1 else 2.0))
    for x in range(10):
        if x % 3 == 2 or x == 9:
            t.set(1 + x, 16, (0, 0, 0, 0))
            t.set(1 + x, 15, ramp(GOLD, 1.0))
    t.set(5, 16, ramp(GOLD, 2.4))
    # --- Seiten (Kanten des Quaders) und Innenseite
    for y in range(16):
        t.set(0, 1 + y, ramp(GOLD, 1.5))      # Ost
        t.set(11, 1 + y, ramp(GOLD, 1.0))     # West
    for x in range(10):
        t.set(1 + x, 0, ramp(GOLD, 3.0))      # Oben
        t.set(11 + x, 0, ramp(GOLD, 1.0))     # Unten
    for y in range(16):
        for x in range(10):
            idx = 2.1 + fold(x, y, 10) * 0.8 - (y / 15.0) * 0.4
            t.set(12 + x, 1 + y, ramp(IVORY, idx))
    # --- Elytren-Fluegel (Region 22,0): gleiche Flaeche, damit der Umhang auch mit Elytren passt
    for face_x, mirror in ((24, False), (36, True)):
        for y in range(20):
            for x in range(10):
                xx = (9 - x) if mirror else x
                idx = 3.0 - (y / 19.0) * 0.9 + (0.3 if xx < 2 else 0) + fold(xx, y, 10) * 0.4
                c = ramp(IVORY, idx)
                if (xx + y) % 6 == 0:
                    c = ramp(GOLD, 2.6 - (y / 19.0))      # goldene Federrippen
                if xx == 0 or y == 0:
                    c = ramp(GOLD, 3.0)
                # Fluegelspitze: unten aussen durchsichtig
                if y > 11 and (xx - 9) + (y - 11) * 0.9 > 0 and xx > 4:
                    c = (0, 0, 0, 0)
                t.set(face_x + x, 2 + y, c)
    for y in range(20):
        t.set(22, 2 + y, ramp(GOLD, 1.6)); t.set(23, 2 + y, ramp(GOLD, 1.2))
        t.set(34, 2 + y, ramp(GOLD, 1.0)); t.set(35, 2 + y, ramp(GOLD, 0.6))
    for x in range(10):
        t.set(24 + x, 0, ramp(GOLD, 3.0)); t.set(24 + x, 1, ramp(GOLD, 2.4))
        t.set(34 + x, 0, ramp(GOLD, 1.4)); t.set(34 + x, 1, ramp(GOLD, 1.0))
    return t


def main():
    save_png(lore_book(), "item/lore_book")
    save_png(purification_essence(), "item/purification_essence")
    save_png(breach_core(), "item/breach_core")
    save_png(keeper_cape(), "entity/keeper_cape")
    covers = lore_covers()
    for name, tex in covers.items():
        save_png(tex, f"item/lore_book_{name}")
    print(f"story_items: lore_book, purification_essence, breach_core, keeper_cape, {len(covers)} Lore-Cover (Zusatz)")


if __name__ == "__main__":
    main()
