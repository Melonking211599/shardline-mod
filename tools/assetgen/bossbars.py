"""Bossleisten-Texturen, eigenes Design je Boss. Aufruf: python tools/assetgen/bossbars.py [--preview <aus.png>]

Je Boss unter textures/gui/boss_bar/ (Leiste 182 px breit wie Vanilla, 10 px hoch):
  <boss>_background.png   182x10        leere Leiste (Track)
  <boss>_fill.png         198x(10*P)    Fuellung, waagerecht periodisch mit 16 px; die UI verschiebt das 182-px-Fenster um 0..15 px
                                         fuer das Schimmern. P Phasen untereinander (Zeile = Phase, Reihenfolge wie `phases` in BossBars.java)
  <boss>_overlay.png      214x(28*P)    Rahmen mit Kappen und Verzierungen; Fenster fuer die Leiste bei x=16..197, y=8..17
Sonderteile:
  shard_queen_cracks.png  182x30        drei Risstufen (je 10 px), die UI blendet sie mit sinkendem Leben ueber die Leiste
  virus_heart_veins.png, festering_heart_veins.png   182x10   Adern, pulsieren im Takt des Herzschlags
  crystal_colossus_shield.png            20x10   Schildsymbol am 50-%-Marker (links ganz, rechts zerbrochen)
Gezeichnet wird in client/story/BossBars.java. Formen: Kappen und Kronen sind mit Spitzen (tip/base-Koordinaten) und
ASCII-Bildern gesetzt, Platten und Fuellungen folgen festen Zeilenprofilen, Farben nur aus den Paletten unten.
"""
from __future__ import annotations

import math
import sys

from PIL import Image, ImageDraw

from common import CYAN, EMBER, LILAC, MAGENTA, SLATE, TEX, VGLOW, VIOLET, VIRUS, Tex, hx, save_png

OY = 4                          # Rand oberhalb der Platte fuer Kronen und Zacken
FW, FH = 214, 28 + OY
WX, WY, WW, WH = 16, 8, 182, 10   # Fenster in Zeichenkoordinaten; im fertigen Rahmen liegt es bei y = WY + OY = 12
FILL_W = WW + 16

# ---------------------------------------------------------------- Paletten (je fuenf Stufen dunkel -> hell)
RED = [hx(c) for c in ("#3a0808", "#7a1212", "#c42222", "#ff5a3c", "#ffd0b0")]
PURP = [hx(c) for c in ("#2a1560", "#4a2a9c", "#7a55e0", "#b79cff", "#efe6ff")]
ICE = [hx(c) for c in ("#2a5896", "#5b9fe0", "#a6dcff", "#eefaff")]
ECHO = [hx(c) for c in ("#10222e", "#2a5272", "#5f9cc4", "#b4e0f4", "#f2fcff")]
PUS = [hx(c) for c in ("#1e2208", "#4a5210", "#8a9a1c", "#c4d03a", "#f2f4a0")]      # kranker Eiter: gelbgruen
ROT = [hx(c) for c in ("#1c0c0a", "#3a1a14", "#5c2a20", "#86402e", "#b0604a")]      # entzuendetes Fleisch


class Canvas(Tex):
    """Rahmenblatt: Zeichenkoordinaten y = -4..27 landen auf Zeile 0..31 der Datei (Kronen duerfen ueber die Platte ragen)."""

    def set(self, x, y, c):
        super().set(x, y + OY, c)

    def get(self, x, y):
        return super().get(x, y + OY)


def dim(c, f):
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), c[3])


def alpha(c, a):
    return (c[0], c[1], c[2], a)


def put(t: Tex, rows: str, pal: dict, x: int, y: int, name: str = "?"):
    """ASCII-Bild an x, y legen ('.' = unveraendert); prueft gleiche Zeilenlaenge und bekannte Zeichen."""
    lines = rows.strip("\n").split("\n")
    w = len(lines[0])
    for j, row in enumerate(lines):
        if len(row) != w:
            raise ValueError(f"{name}: Zeile {j} hat {len(row)} statt {w} Zeichen: {row!r}")
        for i, ch in enumerate(row):
            if ch == ".":
                continue
            if ch not in pal:
                raise ValueError(f"{name}: Zeichen {ch!r} in Zeile {j} ohne Farbe")
            t.set(x + i, y + j, pal[ch])


def plate(t: Tex, top, bot, x0=10, x1=FW - 11):
    """Rahmenplatte: drei Zeilen oben (y = 5..7, aussen -> innen) und unten (y = 18..20, innen -> aussen); None = frei."""
    for x in range(x0, x1 + 1):
        for k, c in enumerate(top):
            if c:
                t.set(x, WY - 3 + k, c)
        for k, c in enumerate(bot):
            if c:
                t.set(x, WY + WH + k, c)


def mirror_right(t: Tex, x1: int = 15, shade: float = 0.84):
    """Spiegelt die linke Kappe (x 0..x1) nach rechts, die rechte Seite etwas dunkler (Licht von links)."""
    for y in range(-OY, FH - OY):
        for x in range(x1 + 1):
            c = t.get(x, y)
            if c[3]:
                t.set(FW - 1 - x, y, dim(c, shade))


def shard(t: Tex, tip, base, w, ramp_, ice=None, light=-1):
    """Verjuengte Spitze von base (Breite w) nach tip: Seite zur Lichtquelle hell, abgewandte dunkel, Spitze eisblau."""
    (tx, ty), (bx, by) = tip, base
    ax, ay = tx - bx, ty - by
    ln = math.hypot(ax, ay)
    ux, uy = ax / ln, ay / ln
    px, py = -uy, ux
    for y in range(min(ty, by) - w, max(ty, by) + w + 1):
        for x in range(min(tx, bx) - w, max(tx, bx) + w + 1):
            dx, dy = x - bx, y - by
            s = (dx * ux + dy * uy) / ln
            if s < 0.0 or s > 1.0:
                continue
            half = (w / 2.0) * (1.0 - s) + 0.35
            d = dx * px + dy * py
            if abs(d) > half:
                continue
            side = d / max(half, 0.01) * light
            tone = 3 if side > 0.2 else (1 if side < -0.45 else 2)
            c = ramp_[tone]
            if ice and s > 0.8:
                c = ice[3] if s > 0.92 else ice[2]
            t.set(x, y, c)


def block(t: Tex, x0, y0, x1, y1, ramp_, chamfer=2):
    """Abgeschraegter Block mit Lichtkante oben/links (ramp_[3]/[4]) und Schattenkante unten/rechts (ramp_[0]/[1])."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            cx = min(x - x0, x1 - x)
            cy = min(y - y0, y1 - y)
            if cx + cy < chamfer:
                continue
            if y == y0 or x == x0:
                c = ramp_[4] if cx + cy >= chamfer else ramp_[3]
                c = ramp_[3] if (y == y0 and x == x0) else c
            elif y == y1 or x == x1:
                c = ramp_[0]
            elif y == y1 - 1 or x == x1 - 1:
                c = ramp_[1]
            else:
                c = ramp_[2]
            t.set(x, y, c)


def hline(t, x0, x1, y, c):
    for x in range(x0, x1 + 1):
        t.set(x, y, c)


# ---------------------------------------------------------------- Fuellungen (198 x 10, Periode 16)
def fill_tex(rows_fn) -> Tex:
    t = Tex(FILL_W, WH)
    for y in range(WH):
        for x in range(FILL_W):
            t.set(x, y, rows_fn(x, y))
    return t


def stack(parts: list[Tex]) -> Tex:
    out = Tex(parts[0].w, sum(p.h for p in parts))
    y = 0
    for p in parts:
        out.img.paste(p.img, (0, y))
        y += p.h
    out.px = out.img.load()
    return out


# ================================================================ Kristallkoloss: Steinrahmen, Leuchtrunen, Schild
GEM_COLOSSUS = """
....d....
...dkd...
..dkwCd..
.dkwkCcd.
dkwwkCccd
dkwkCCccd
dCkCCcccd
.dCCcccd.
.dCcccd..
..dccd...
...dd....
"""

SHIELD = """
.oooooooo.
occkkkkcco
ocCkwwkCco
ocCkwwkCco
ocCkkkkCco
occCkkCcco
.occCCccco
..occCCco.
...occo...
....oo....
"""


def colossus_overlay(acc, enraged: bool) -> Tex:
    t = Canvas(FW, FH)
    plate(t, (SLATE[4], SLATE[3], SLATE[0]), (SLATE[0], SLATE[1], SLATE[0]))
    for x in range(32, FW - 24, 24):                       # Nieten
        t.rect(x, 5, 2, 2, SLATE[2])
        t.set(x, 5, SLATE[4])
    for x in range(30, FW - 30, 12):                       # Leuchtrunen in der Unterplatte: kurze Striche mit Haken
        hline(t, x, x + 3, 19, acc[1])
        t.set(x + 3, 20, acc[0])
    # Kappe links: Steinpfeiler mit Kristall
    block(t, 0, 3, 15, 24, SLATE, chamfer=3)
    for y in range(WY, WY + WH):                           # Innenkante des Fensters
        t.set(15, y, SLATE[0])
    put(t, GEM_COLOSSUS, {"d": acc[0], "c": acc[1], "C": acc[2], "k": acc[3], "w": acc[4]}, 3, 8, "colossus gem")
    for (x, y) in ((2, 6), (3, 6), (12, 21), (13, 21), (2, 20)):   # Kerben im Stein
        t.set(x, y, SLATE[0])
    mirror_right(t)
    # Krone ueber der Mitte: Stein mit kleinem Kristall
    block(t, 93, -3, 121, 4, SLATE, chamfer=5)
    for x in range(96, 119, 4):                                # Runenkerben im Kronenstein
        t.set(x, 2, SLATE[0])
    for k, y in enumerate(range(-2, 4)):                       # Kristall im Kronenstein
        t.set(106, y, acc[min(4, 4 - k // 2 - (1 if k > 3 else 0))])
        t.set(107, y, acc[max(0, 3 - k // 2 - (1 if k > 3 else 0))])
    # Zaehne unten
    for x in range(40, FW - 40, 24):
        for k in range(3):
            hline(t, x + k, x + 5 - k, 21 + k, SLATE[2 - k // 2])
    if enraged:                                             # Risse im Stein und gluehende Fugen
        for (x0, y0, x1, y1) in ((5, 3, 9, 7), (9, 7, 7, 10), (11, 24, 8, 20), (60, 5, 63, 7), (150, 18, 154, 20), (206, 4, 203, 8)):
            t.line(x0, y0, x1, y1, SLATE[0])
        for (x, y) in ((8, 6), (7, 9), (62, 6), (152, 19)):
            t.set(x, y, acc[2])
    return t


def colossus_fill(acc):
    prof = (acc[4], acc[3], acc[2], acc[2], acc[2], acc[2], acc[1], acc[1], acc[0], dim(acc[0], 0.6))

    def px(x, y):
        c = prof[y]
        m = x % 16
        if m == 15 and 1 <= y <= 8:                         # Fuge zwischen den Kristallsegmenten
            return acc[0] if y < 8 else dim(acc[0], 0.6)
        if m == 0 and 1 <= y <= 7:
            return acc[3] if y < 5 else c
        if 1 <= y <= 8 and (m + y * 2) % 16 in (6, 7):      # schraeger Glanz
            return acc[min(4, acc.index(c) + 1)] if c in acc else c
        return c
    return fill_tex(px)


def colossus_background() -> Tex:
    t = Tex(WW, WH)
    base = [hx(c) for c in ("#07080d", "#0d0f18", "#141826")]
    for y in range(WH):
        for x in range(WW):
            c = base[1]
            if y <= 1:
                c = base[0]
            elif y == WH - 1:
                c = base[2]
            if x % 26 == 25 and 1 <= y <= 8:                 # Steinfugen
                c = base[0]
            t.set(x, y, c)
    return t


def colossus_shield() -> Tex:
    t = Tex(20, 10)
    put(t, SHIELD, {"o": CYAN[0], "c": CYAN[1], "C": CYAN[2], "k": CYAN[3], "w": CYAN[4]}, 0, 0, "shield")
    put(t, SHIELD, {"o": SLATE[0], "c": SLATE[1], "C": SLATE[2], "k": SLATE[3], "w": SLATE[4]}, 10, 0, "shield broken")
    for k in range(10):                                      # Bruch: diagonale Luecke, rechte Haelfte sackt ab
        t.set(10 + 4 + (k % 3) - 1, k, (0, 0, 0, 0))
    for y in range(8, 0, -1):
        for x in range(15, 20):
            t.set(x, y + 1, t.get(x, y))
        t.set(15, 1, (0, 0, 0, 0))
    return t


# ================================================================ Splitterkoenigin: Glasrahmen, Kronenspitzen, Risse
def queen_overlay(acc) -> Tex:
    t = Canvas(FW, FH)
    plate(t, (LILAC[1], VIOLET[4], VIOLET[0]), (VIOLET[0], VIOLET[3], VIOLET[2]))
    for x in range(40, FW - 30, 30):                         # Glassplitter in der Platte
        t.set(x, 6, ICE[3])
        t.set(x + 1, 6, ICE[2])
        t.set(x, 19, acc[3])
    # Kappe links: Sockel mit einem grossen und zwei kleinen Kristallen, die nach aussen zeigen
    block(t, 8, 6, 15, 21, VIOLET, chamfer=2)
    for y in range(WY, WY + WH):
        t.set(15, y, VIOLET[0])
    shard(t, (1, 14), (11, 14), 8, VIOLET, ICE, light=1)
    shard(t, (3, 3), (11, 10), 5, VIOLET, ICE, light=1)
    shard(t, (3, 25), (11, 18), 5, VIOLET, ICE, light=1)
    for (x, y, k) in ((11, 13, 4), (12, 13, 3), (11, 14, 3), (12, 14, 2), (11, 15, 2), (12, 15, 1)):
        t.set(x, y, acc[k])
    mirror_right(t)
    # Krone: sieben Zacken auf einem Reif, aussen leicht nach aussen geneigt
    for x in range(92, 123):
        t.set(x, 4, LILAC[1])
        t.set(x, 3, VIOLET[4] if x % 4 else LILAC[0])
    for off, h, w in ((-12, 5, 3), (-8, 7, 4), (-4, 6, 4), (0, 10, 5), (4, 6, 4), (8, 7, 4), (12, 5, 3)):
        lean = int(off / 5)
        shard(t, (107 + off + lean, 3 - h), (107 + off, 3), w, LILAC, ICE, light=1)
    t.rect(105, 6, 5, 2, acc[2])                              # Stein im Reif
    t.set(105, 6, acc[4])
    t.set(106, 6, acc[3])
    return t


def queen_fill(acc):
    prof = (acc[4], acc[3], acc[3], acc[2], acc[2], acc[2], acc[1], acc[1], acc[0], dim(acc[0], 0.6))

    def px(x, y):
        c = prof[y]
        m = x % 16
        d = (m + y) % 16                                      # schraege Glasschliere
        if 1 <= y <= 8 and d in (4, 5, 6):
            return acc[min(4, acc.index(c) + 1)] if c in acc else c
        if y in (2, 3) and m == 11:
            return acc[4]                                     # Funkeln
        return c
    return fill_tex(px)


def queen_background() -> Tex:
    t = Tex(WW, WH)
    base = [hx(c) for c in ("#07020f", "#100520", "#1a0a33")]
    for y in range(WH):
        for x in range(WW):
            c = base[1]
            if y <= 1:
                c = base[0]
            elif y == WH - 1:
                c = base[2]
            if (x + y * 2) % 22 == 0 and 1 <= y <= 8:
                c = base[2]
            t.set(x, y, c)
    return t


CRACK_LINES = [  # (Stufe, Polylinie) - Stufe 1 ab 2/3 Leben, 2 ab 1/3, 3 ab 10 %
    (1, [(30, 0), (32, 3), (31, 5), (34, 9)]),
    (1, [(120, 9), (118, 6), (121, 4), (119, 0)]),
    (1, [(160, 2), (162, 4), (161, 7)]),
    (2, [(64, 0), (62, 3), (65, 5), (63, 9)]),
    (2, [(90, 9), (92, 6), (89, 4), (91, 0)]),
    (2, [(140, 0), (143, 3), (141, 6), (144, 9)]),
    (2, [(10, 3), (13, 5), (12, 8)]),
    (3, [(45, 0), (47, 4), (44, 6), (46, 9)]),
    (3, [(76, 0), (78, 3), (75, 6), (77, 9)]),
    (3, [(105, 0), (103, 4), (106, 6), (104, 9)]),
    (3, [(175, 1), (172, 4), (174, 7), (171, 9)]),
]


def queen_cracks() -> Tex:
    t = Tex(WW, WH * 3)
    for stage in (1, 2, 3):
        for lv, pts in CRACK_LINES:
            if lv > stage:
                continue
            for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
                t.line(x0 + 1, y0 + (stage - 1) * WH, x1 + 1, y1 + (stage - 1) * WH, alpha(VIOLET[0], 200))
            for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
                t.line(x0, y0 + (stage - 1) * WH, x1, y1 + (stage - 1) * WH, alpha(ICE[3], 210))
    return t


# ================================================================ Virusherz: wucherndes Fleisch, Dornen, Adern
def heart_overlay(acc) -> Tex:
    t = Canvas(FW, FH)
    plate(t, (VIRUS[3], VIRUS[2], VIRUS[0]), (VIRUS[0], VIRUS[1], VIRUS[0]))
    for (x, r) in ((34, 3), (62, 2), (92, 3), (132, 3), (160, 2), (180, 3)):   # Wucherungen oben
        for dx in range(-r, r + 1):
            h = int(round(math.sqrt(max(0.0, r * r - dx * dx) * 0.9)))
            for k in range(h):
                t.set(x + dx, 4 - k, VIRUS[3] if k == h - 1 else VIRUS[2])
    for (x, r) in ((48, 2), (78, 3), (112, 2), (146, 3), (172, 2)):            # und unten
        for dx in range(-r, r + 1):
            h = int(round(math.sqrt(max(0.0, r * r - dx * dx) * 0.9)))
            for k in range(h):
                t.set(x + dx, 21 + k, VIRUS[1] if k < h - 1 else VIRUS[2])
    # Kappe links: Herzknoten mit grossem, leuchtendem Kern und Dornen
    for y in range(3, 25):
        for x in range(0, 16):
            d = math.hypot(x - 8.5, y - 13.5)
            if d <= 8.2:
                lit = (x - 8.5) * -0.55 + (y - 13.5) * -0.55
                tone = 3 if lit > 2.2 else 2 if lit > -2.4 else 1
                if d > 7.0:
                    tone = 0 if lit < 0 else 2
                t.set(x, y, VIRUS[tone])
    for y in range(WY, WY + WH):
        t.set(15, y, VIRUS[0])
    for y in range(9, 19):                                    # Kern: leuchtende Raute mit Hof
        for x in range(4, 14):
            d = abs(x - 8.5) + abs(y - 13.5)
            if d <= 5.0:
                t.set(x, y, acc[0] if d > 4.0 else acc[1] if d > 2.8 else acc[2] if d > 1.6 else acc[3] if d > 0.8 else acc[4])
    for k in range(6):                                       # Dornen
        t.set(2 + k, 4 - k // 3, VIRUS[3])
        t.set(2 + k, 23 + k // 3, VIRUS[2])
    for x in range(16, 44):                                  # Adern laufen aus dem Knoten in die Platte
        if x % 4 != 3:
            t.set(x, 6, acc[1])
        if x % 5 != 4:
            t.set(x, 19, acc[0])
    mirror_right(t)
    for x in range(FW - 44, FW - 16):
        if (FW - 1 - x) % 4 != 3:
            t.set(x, 6, acc[1])
        if (FW - 1 - x) % 5 != 4:
            t.set(x, 19, acc[0])
    # grosser Dorn mit leuchtender Spitze in der Mitte, zwei kleine daneben
    shard(t, (107, -4), (107, 5), 7, VIRUS, None, light=1)
    shard(t, (98, -1), (101, 5), 4, VIRUS, None, light=1)
    shard(t, (116, -1), (113, 5), 4, VIRUS, None, light=1)
    t.set(107, -4, acc[3])
    t.set(107, -3, acc[2])
    t.set(107, -2, acc[1])
    # Tropfen unten
    for x in (60, 107, 154):
        for k in range(4):
            t.set(x, 21 + k, VIRUS[2] if k < 3 else acc[2])
    return t


def heart_fill(acc):
    prof = (acc[4], acc[3], acc[2], acc[2], acc[2], acc[2], acc[1], acc[1], acc[0], dim(acc[0], 0.6))

    def px(x, y):
        c = prof[y]
        wave = 5.0 + 2.0 * math.sin((x % 16) / 16.0 * math.tau)
        if abs(y - wave) < 0.55 and 1 <= y <= 8:              # dunkle Ader quer durch die Fuellung
            return acc[0]
        if abs(y - (wave - 1.6)) < 0.5 and y >= 1:
            return acc[min(4, acc.index(c) + 1)] if c in acc else c
        return c
    return fill_tex(px)


def heart_background(base) -> Tex:
    t = Tex(WW, WH)
    for y in range(WH):
        for x in range(WW):
            c = base[1]
            if y <= 1:
                c = base[0]
            elif y == WH - 1:
                c = base[2]
            t.set(x, y, c)
    return t


def veins(ramp_) -> Tex:
    """Aderngeflecht ueber die ganze Leiste (wird mit Herzschlag-Helligkeit aufgeblendet)."""
    t = Tex(WW, WH)
    for x in range(WW):
        y = int(round(5.0 + 2.2 * math.sin(x / 9.0) * math.cos(x / 31.0)))
        t.set(x, y, alpha(ramp_[2], 255))
        t.set(x, y + 1, alpha(ramp_[0], 200))
    for x0 in range(14, WW - 10, 29):                        # Verzweigungen nach oben und unten
        y0 = int(round(5.0 + 2.2 * math.sin(x0 / 9.0) * math.cos(x0 / 31.0)))
        up = (x0 // 29) % 2 == 0
        for k in range(1, 5):
            t.set(x0 + k // 2, y0 - k if up else y0 + k, alpha(ramp_[1] if k < 4 else ramp_[3], 255))
    return t


# ================================================================ Nachhall: Geist, durchscheinend, Runen
RUNES = (
    """
.#.#.
.###.
..#..
.###.
.#.#.
""",
    """
#...#
.#.#.
..#..
.#.#.
#...#
""",
    """
..#..
.###.
#.#.#
.###.
..#..
""",
)


def echo_overlay(acc) -> Tex:
    t = Canvas(FW, FH)
    plate(t, (None, alpha(acc[3], 235), alpha(acc[1], 240)), (alpha(acc[1], 240), alpha(acc[2], 200), alpha(acc[0], 150)))
    for x in range(26, FW - 26, 4):                          # gestrichelte Aussenlinie, wie von Hand gezogen
        if (x // 4) % 3:
            t.set(x, 4, alpha(acc[2], 130))
    # Kappe: offener Ring mit Kern und zwei Splittern
    for y in range(3, 25):
        for x in range(0, 16):
            d = math.hypot(x - 8.5, y - 13.5)
            if 6.2 <= d < 7.8:
                t.set(x, y, alpha(acc[3] if x + y < 22 else acc[2], 235))
            elif 7.8 <= d < 8.6:
                t.set(x, y, alpha(acc[1], 150))
    for (x, y, k) in ((8, 13, 4), (9, 13, 3), (8, 14, 3), (9, 14, 2)):
        t.set(x, y, acc[k])
    for y in range(WY, WY + WH):
        t.set(15, y, alpha(acc[1], 220))
    shard(t, (1, 7), (6, 9), 2, ECHO, None, light=1)
    shard(t, (1, 20), (6, 18), 2, ECHO, None, light=1)
    mirror_right(t, shade=0.9)
    # Runen oben (drei links, drei rechts gespiegelt) und eine Raute in der Mitte
    for i, x in enumerate((34, 62, 90)):
        rows = RUNES[i]
        put(t, rows.replace("#", "k"), {"k": alpha(acc[3], 230)}, x, 0, "rune")
        put(t, rows.replace("#", "k"), {"k": alpha(acc[3], 230)}, FW - 5 - x, 0, "rune")
    for (x, y, k) in ((107, 0, 4), (106, 1, 3), (107, 1, 4), (108, 1, 3), (107, 2, 3), (107, 3, 2)):
        t.set(x, y, alpha(acc[k], 240))
    return t


def echo_fill(acc):
    prof = (acc[4], acc[3], acc[3], acc[2], acc[2], acc[2], acc[1], acc[1], acc[1], acc[0])

    def px(x, y):
        c = prof[y]
        m = x % 16
        a = 215 if y % 2 == 0 else 150                         # Zeilen schimmern durch
        if (m, y) in ((5, 3), (13, 6)):
            return alpha(acc[4], 255)
        return alpha(c, a)
    return fill_tex(px)


def echo_background() -> Tex:
    t = Tex(WW, WH)
    for y in range(WH):
        for x in range(WW):
            t.set(x, y, alpha(ECHO[0], 170 if y % 2 == 0 else 120))
    return t


# ================================================================ Schwaerenherz: Eiter, Beulen, Tropfen
def festering_overlay(acc) -> Tex:
    t = Canvas(FW, FH)
    plate(t, (ROT[4], ROT[3], ROT[0]), (ROT[0], ROT[1], ROT[0]))
    for (x, r) in ((30, 3), (56, 2), (84, 3), (128, 3), (156, 2), (184, 3)):   # Beulen oben mit Eiterkuppe
        for dx in range(-r, r + 1):
            h = int(round(math.sqrt(max(0.0, r * r - dx * dx) * 1.1)))
            for k in range(h):
                t.set(x + dx, 4 - k, acc[3] if k == h - 1 and dx <= 0 else acc[2] if k == h - 1 else ROT[3])
    # Kappe links: grosse Beule mit Glanzpunkt
    for y in range(3, 25):
        for x in range(0, 16):
            d = math.hypot(x - 8.5, y - 13.5)
            if d <= 8.2:
                lit = (x - 8.5) * -0.55 + (y - 13.5) * -0.55
                tone = 3 if lit > 2.4 else 2 if lit > -2.0 else 1
                if d > 7.0:
                    tone = 0 if lit < 0 else 2
                t.set(x, y, acc[tone])
    for (x, y, k) in ((5, 9, 4), (6, 9, 4), (5, 10, 3)):
        t.set(x, y, acc[k])
    for (x, y) in ((10, 16), (11, 15), (9, 17)):
        t.set(x, y, acc[0])
    for y in range(WY, WY + WH):
        t.set(15, y, ROT[0])
    for k in range(5):                                       # Krusten
        t.set(1 + k, 4 - k // 3, ROT[3])
        t.set(1 + k, 23 + k // 3, ROT[3])
    mirror_right(t)
    # Spitze in der Mitte
    shard(t, (107, -3), (107, 5), 6, ROT, None, light=1)
    t.set(107, -3, acc[4])
    t.set(107, -2, acc[3])
    t.set(107, -1, acc[2])
    # Tropfen unten (verschieden lang)
    for x, ln in ((36, 5), (68, 3), (104, 6), (130, 4), (164, 5), (190, 3)):
        for k in range(ln):
            t.set(x, 21 + k, acc[2] if k < ln - 1 else acc[3])
        t.set(x - 1, 21, ROT[1])
        t.set(x + 1, 21, ROT[1])
    return t


def festering_fill(acc):
    prof = (acc[4], acc[3], acc[3], acc[2], acc[2], acc[2], acc[1], acc[1], acc[0], dim(acc[0], 0.6))
    bubbles = ((4, 3), (12, 7))

    def px(x, y):
        c = prof[y]
        m = x % 16
        for (bx, by) in bubbles:
            d = math.hypot(m - bx, y - by)
            if d < 1.6:
                return acc[4] if (m, y) == (bx - 1, by - 1) else acc[3]
            if d < 2.5:
                return acc[0]
        return c
    return fill_tex(px)


def festering_background() -> Tex:
    t = Tex(WW, WH)
    base = [hx(c) for c in ("#0a0704", "#16100a", "#22180e")]
    for y in range(WH):
        for x in range(WW):
            c = base[1]
            if y <= 1:
                c = base[0]
            elif y == WH - 1:
                c = base[2]
            if (x * 7 + y * 3) % 31 == 0 and 2 <= y <= 8:
                c = base[2]
            t.set(x, y, c)
    return t


# ================================================================ Zusammenbau
def build():
    """Liefert {dateiname: Tex}."""
    out = {}
    # Kristallkoloss: Phase 1 Cyan, Phase 2 (Raserei) Glut
    out["crystal_colossus_background"] = colossus_background()
    out["crystal_colossus_fill"] = stack([colossus_fill(CYAN), colossus_fill(EMBER)])
    out["crystal_colossus_overlay"] = stack([colossus_overlay(CYAN, False), colossus_overlay(EMBER, True)])
    out["crystal_colossus_shield"] = colossus_shield()
    # Splitterkoenigin: Rosa -> Violett -> Rot
    out["shard_queen_background"] = queen_background()
    out["shard_queen_fill"] = stack([queen_fill(MAGENTA), queen_fill(PURP), queen_fill(RED)])
    out["shard_queen_overlay"] = stack([queen_overlay(MAGENTA), queen_overlay(PURP), queen_overlay(RED)])
    out["shard_queen_cracks"] = queen_cracks()
    # Virusherz: Violett -> Rosa -> Rot
    violet_acc = [hx(c) for c in ("#2a0a4a", "#5a1a9c", "#8a3ae0", "#c08cff", "#f0e0ff")]
    out["virus_heart_background"] = heart_background([hx(c) for c in ("#07020a", "#12061a", "#1e0a28")])
    out["virus_heart_fill"] = stack([heart_fill(violet_acc), heart_fill(VGLOW), heart_fill(RED)])
    out["virus_heart_overlay"] = stack([heart_overlay(violet_acc), heart_overlay(VGLOW), heart_overlay(RED)])
    out["virus_heart_veins"] = veins(VGLOW)
    # Nachhall: eine Phase, blassblau
    out["echo_queen_background"] = echo_background()
    out["echo_queen_fill"] = echo_fill(ECHO)
    out["echo_queen_overlay"] = echo_overlay(ECHO)
    # Schwaerenherz: eine Phase, Eiter
    out["festering_heart_background"] = festering_background()
    out["festering_heart_fill"] = festering_fill(PUS)
    out["festering_heart_overlay"] = festering_overlay(PUS)
    out["festering_heart_veins"] = veins(PUS)
    return out


# Phasen je Boss: Reihenfolge der Zeilen in fill/overlay; Anzeige: Vanilla-Farbe, die der Server setzt
BOSSES = {
    "crystal_colossus": {"phases": 2, "marks": (0.5,), "features": "shield"},
    "shard_queen": {"phases": 3, "marks": (2 / 3, 1 / 3), "features": "cracks"},
    "virus_heart": {"phases": 3, "marks": (2 / 3, 1 / 3), "features": "veins"},
    "echo_queen": {"phases": 1, "marks": (2 / 3, 1 / 3), "features": "ghost"},
    "festering_heart": {"phases": 1, "marks": (2 / 3, 1 / 3), "features": "veins"},
}


def main():
    for name, tex in build().items():
        save_png(tex, f"gui/boss_bar/{name}")
    print(f"bossbars: {len(BOSSES)} Bosse (Hintergrund, Fuellung, Rahmen; Sonderteile Schild, Risse, Adern), Leiste 182 px")


# ---------------------------------------------------------------- Vorschau (nur Kontrolle, schreibt nichts in die Mod)
def preview(out: str, scale: int = 3):
    """Jeder Boss bei 100 %, 50 % und 10 %, je Phase passend; Herzschlag-Adern einmal hell."""
    parts = build()
    BG = (96, 128, 176)
    rows = []
    for boss, cfg in BOSSES.items():
        P = cfg["phases"]
        for pct, phase in ((100, 0), (50, min(1, P - 1)), (10, P - 1)):
            rows.append((boss, cfg, pct, phase))
    img = Image.new("RGBA", (FW * scale + 20, len(rows) * (FH + 6) * scale + 10), BG + (255,))
    for i, (boss, cfg, pct, phase) in enumerate(rows):
        y0 = 6 + i * (FH + 6) * scale
        tile = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
        bar = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
        bg = parts[f"{boss}_background"].img
        fillimg = parts[f"{boss}_fill"].img
        ov = parts[f"{boss}_overlay"].img
        bar.alpha_composite(bg, (WX, WY + OY))
        fw = round(WW * pct / 100.0)
        if fw:
            bar.alpha_composite(fillimg.crop((5, phase * WH, 5 + fw, phase * WH + WH)), (WX, WY + OY))
        if cfg["features"] == "cracks":
            stage = 0 if pct > 66 else 1 if pct > 33 else 2 if pct > 10 else 3
            if pct <= 10:
                stage = 3
            if stage:
                cr = parts["shard_queen_cracks"].img.crop((0, (stage - 1) * WH, WW, stage * WH))
                bar.alpha_composite(cr, (WX, WY + OY))
        if cfg["features"] == "veins":
            vv = parts[f"{boss}_veins"].img.copy()
            bar.alpha_composite(vv, (WX, WY + OY)) if pct <= 50 else None
        for mk in cfg["marks"]:
            x = WX + round(WW * mk)
            passed = pct / 100.0 <= mk
            if cfg["features"] == "shield":
                continue
            ImageDraw.Draw(bar).line([(x, WY + OY), (x, WY + OY + WH - 1)], fill=(0, 0, 0, 153) if passed else (255, 255, 255, 204))
        if cfg["features"] == "shield":
            sh = parts["crystal_colossus_shield"].img
            half = sh.crop((10, 0, 20, 10)) if pct <= 50 else sh.crop((0, 0, 10, 10))
            bar.alpha_composite(half, (WX + WW // 2 - 5, WY + OY))
        bar.alpha_composite(ov.crop((0, phase * FH, FW, phase * FH + FH)))
        tile.alpha_composite(bar)
        big = tile.resize((FW * scale, FH * scale), Image.NEAREST)
        img.alpha_composite(big, (10, y0))
        ImageDraw.Draw(img).text((12, y0 - 5), f"{boss} {pct}% (Phase {phase + 1})", fill=(255, 255, 255, 255))
    img.convert("RGB").save(out)
    print("Vorschau:", out, img.size)


if __name__ == "__main__":
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1], int(sys.argv[sys.argv.index("--scale") + 1]) if "--scale" in sys.argv else 3)
    else:
        main()
