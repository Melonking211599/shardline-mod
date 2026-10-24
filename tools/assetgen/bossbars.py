"""Bossleisten-Texturen (eine Datei pro Boss). Aufruf: python tools/assetgen/bossbars.py

Atlas 256x64 pro Boss unter textures/gui/bossbar/<boss>.png:
  (  0,  0)  200x12   Hintergrund der Leiste (Track)
  (  0, 12)  224x12   Fuellung, waagerecht periodisch mit 16 Pixeln (die UI verschiebt sie fuer das Schimmern)
  (  0, 24)  224x20   Rahmen mit Verzierungen; Fenster fuer die Leiste bei x=16..215, y=8..19
Gezeichnet wird in client/story/BossBars.java. Alle Farben aus common.py.
"""
from __future__ import annotations

from common import CYAN, LILAC, MAGENTA, SLATE, VGLOW, VIOLET, VIRUS, Tex, TEX, hx, save_png

FW, FH = 232, 28
HOLE = (16, 8, 200, 12)


def in_hole(x, y):
    return HOLE[0] <= x < HOLE[0] + HOLE[2] and HOLE[1] <= y < HOLE[1] + HOLE[3]


def track(dark, mid) -> Tex:
    t = Tex(200, 12)
    for y in range(12):
        for x in range(200):
            c = mid if 2 <= y <= 9 else dark
            if x % 20 == 19 and 1 <= y <= 10:
                c = dark
            t.set(x, y, c)
    return t


def fill(light, mid, dark, accent) -> Tex:
    """Waagerecht periodisch (16): oben Glanzlinie, unten Schatten, schraege helle Streifen."""
    t = Tex(224, 12)
    for y in range(12):
        for x in range(224):
            if y == 0:
                c = light
            elif y == 1:
                c = mid
            elif y >= 10:
                c = dark
            else:
                c = mid if y < 7 else dark
            if 1 <= y <= 9 and (x + y * 2) % 16 < 3:
                c = accent if (x + y * 2) % 16 == 1 else light
            t.set(x, y, c)
    return t


def plate(t: Tex, ramp_):
    """Rahmenplatte um das Fenster: innen dunkle Linie, dann helle Oberkante bzw. dunkle Unterkante, 3 Pixel stark."""
    for y in range(HOLE[1] - 3, HOLE[1] + HOLE[3] + 3):
        for x in range(HOLE[0] - 3, HOLE[0] + HOLE[2] + 3):
            if in_hole(x, y):
                continue
            dx = max(HOLE[0] - x, x - (HOLE[0] + HOLE[2] - 1), 0)
            dy = max(HOLE[1] - y, y - (HOLE[1] + HOLE[3] - 1), 0)
            ring = max(dx, dy)
            top = y < HOLE[1] + 6
            c = ramp_[0] if ring == 1 else (ramp_[3] if top else ramp_[1]) if ring == 2 else (ramp_[4] if top else ramp_[0])
            if x % 24 == 8 and ring >= 2:
                c = ramp_[2] if top else ramp_[0]
            t.set(x, y, c)


def mirror_cap(t: Tex, w: int = 16):
    """Spiegelt die linke Kappe (x 0..w-1) nach rechts, rechts etwas dunkler."""
    for y in range(FH):
        for x in range(w):
            c = t.get(x, y)
            if c[3]:
                t.set(FW - 1 - x, y, (int(c[0] * 0.82), int(c[1] * 0.82), int(c[2] * 0.82), c[3]))


def gem(t: Tex, cx, cy, ramp_, r=3):
    for y in range(-r, r + 1):
        for x in range(-r, r + 1):
            d = abs(x) + abs(y)
            if d > r:
                continue
            tone = 4 if d == 0 else 3 if d <= 1 else 2 if d <= 2 else 1
            if x + y > 1:
                tone -= 1
            t.set(cx + x, cy + y, ramp_[max(0, min(4, tone))])


def hline(t, x0, x1, y, c):
    for x in range(x0, x1 + 1):
        t.set(x, y, c)


# ------------------------------------------------------------------ Kristallkoloss: schwerer Steinrahmen mit Leuchtrunen
def colossus_frame() -> Tex:
    t = Tex(FW, FH)
    plate(t, SLATE)
    # Kappe links: Steinpfeiler mit Fase, Leuchtstreifen, grosser Kristall, kleine Spitze oben
    for y in range(3, 25):
        for x in range(0, 16):
            if (x, y) in ((0, 3), (0, 24), (1, 3), (0, 4)):
                continue
            tone = 3 if y < 9 else 2 if y < 18 else 1
            if x == 0 or y == 3:
                tone += 1
            if y == 24 or x == 15:
                tone -= 1
            t.set(x, y, SLATE[max(0, min(4, tone))])
    for y in range(8, 20):
        t.set(11, y, CYAN[1])
        t.set(12, y, CYAN[0])
    gem(t, 6, 14, CYAN, r=4)
    for k in range(3):
        t.set(7 - k // 2 + 1, 2 - k if 2 - k >= 0 else 0, CYAN[3 - k])
    mirror_cap(t)
    # Krone aus Stein ueber der Mitte mit Kristall und Runenstrichen
    for y in range(2, 8):
        for x in range(92 + (7 - y) // 2, 140 - (7 - y) // 2):
            tone = 3 if y < 4 else 2 if y < 6 else 1
            t.set(x, y, SLATE[tone])
    for x in range(96, 136, 4):
        t.set(x, 5, CYAN[1])
        t.set(x + 1, 5, CYAN[2])
    gem(t, 116, 5, CYAN, r=4)
    # Zaehne unten
    for x in range(40, 200, 24):
        for k in range(3):
            hline(t, x + k, x + 5 - k, 23 + k, SLATE[2 - k // 2])
    return t


# ------------------------------------------------------------------ Splitterkoenigin: schlanker Glasrahmen mit Federfluegeln und Krone
def queen_frame() -> Tex:
    t = Tex(FW, FH)
    plate(t, VIOLET)
    # Fluegel links: Federn, nach aussen spitz zulaufend
    for x in range(0, 16):
        half = 2 + int(x * 0.62)
        for y in range(14 - half, 14 + half):
            feather = (x + y) % 5
            tone = 4 if feather == 0 else 3 if y < 14 else 2
            if y == 14 - half or y == 14 + half - 1:
                tone = 4 if y < 14 else 0
            t.set(x, y, LILAC[min(3, max(0, tone - 1))] if x < 8 else VIOLET[max(0, min(4, tone))])
    hline(t, 2, 15, 14, MAGENTA[3])
    for k in range(4):
        t.set(3 + k * 3, 13, MAGENTA[4])
    mirror_cap(t)
    # Krone ueber der Mitte
    for i, h in enumerate((3, 5, 4, 7, 4, 5, 3)):
        x = 103 + i * 4
        for y in range(h):
            yy = 7 - y
            t.set(x, yy, MAGENTA[3] if y == h - 1 else MAGENTA[2])
            t.set(x + 1, yy, MAGENTA[2] if y == h - 1 else MAGENTA[1])
    hline(t, 103, 129, 7, MAGENTA[2])
    gem(t, 116, 4, MAGENTA, r=3)
    # Perlenreihe unten
    for x in range(60, 172, 8):
        t.set(x, 22, LILAC[1])
        t.set(x + 1, 22, LILAC[0])
    return t


# ------------------------------------------------------------------ Virusherz: organischer Rahmen mit Dornen und Tropfen
def heart_frame() -> Tex:
    t = Tex(FW, FH)
    plate(t, VIRUS)
    # Kappe links: pulsierender Knoten mit Dornen
    for y in range(4, 24):
        for x in range(0, 15):
            d = ((x - 8) ** 2 + (y - 14) ** 2) ** 0.5
            if d <= 8.5:
                tone = 3 if d < 6.5 else 2
                t.set(x, y, VIRUS[tone] if d >= 4 else VGLOW[1])
    for (x, y) in ((7, 13), (8, 13), (7, 14), (8, 14)):
        t.set(x, y, VGLOW[3])
    for k in range(6):
        t.set(2 + k, 5 - k // 2, VIRUS[3])
        t.set(2 + k, 23 + k // 2, VIRUS[3])
    t.set(2, 4, VGLOW[1])
    t.set(2, 24, VGLOW[1])
    mirror_cap(t)
    # Dornen oben (nach oben), Tropfen unten
    for x in range(36, 200, 14):
        for k in range(5):
            t.set(x + 2 - abs(k - 2) // 1 if False else x + k // 2, 5 - k, VIRUS[3] if k < 3 else VGLOW[1])
        for k in range(4):
            t.set(x + 6, 23 + k, VIRUS[2] if k < 3 else VGLOW[1])
    # grosser Dorn in der Mitte mit leuchtender Spitze
    for k in range(7):
        for w in range(max(1, 7 - k) // 2 + 1):
            t.set(116 - w, 6 - k if 6 - k >= 0 else 0, VIRUS[3])
            t.set(116 + w, 6 - k if 6 - k >= 0 else 0, VIRUS[2])
    gem(t, 116, 3, VGLOW, r=2)
    return t


def atlas(frame, bg_dark, bg_mid, f_light, f_mid, f_dark, f_acc) -> Tex:
    a = Tex(256, 64)
    for (ox, oy, src) in ((0, 0, track(bg_dark, bg_mid)), (0, 12, fill(f_light, f_mid, f_dark, f_acc)), (0, 24, frame)):
        for y in range(src.h):
            for x in range(src.w):
                a.set(ox + x, oy + y, src.get(x, y))
    return a


def main():
    save_png(atlas(colossus_frame(), hx("#050b10"), hx("#0c1a24"), CYAN[4], CYAN[2], CYAN[1], hx("#ffffff")), "gui/bossbar/crystal_colossus")
    save_png(atlas(queen_frame(), hx("#0a0314"), hx("#170826"), MAGENTA[4], MAGENTA[2], MAGENTA[1], hx("#ffffff")), "gui/bossbar/shard_queen")
    save_png(atlas(heart_frame(), hx("#080208"), hx("#1a0620"), VGLOW[3], VGLOW[1], VIRUS[3], hx("#ffffff")), "gui/bossbar/virus_heart")
    print("bossbars: 3 Atlanten (crystal_colossus, shard_queen, virus_heart)")


if __name__ == "__main__":
    main()
