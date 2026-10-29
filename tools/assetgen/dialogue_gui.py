"""GUI-Texturen fuer Dialogfenster und Auftragsbuch. Aufruf: python tools/assetgen/dialogue_gui.py

Stil (STYLEBIBEL.md): dunkles Violett, Kristallkante (oben links hell Lila, unten rechts dunkel), Cyan-Splitter an den
Ecken als einziges Leuchten. 9-Slice-Rahmen: die Raender (border) bleiben fest, Kanten und Mitte werden gekachelt.

Ausgabe (textures/gui/...):
  dialogue/frame.png           32x32  9-Slice, Rand 8 (Dialogbox, auch Verlauf und Journal-Felder)
  dialogue/portrait_frame.png  80x80  Rahmen um das 64x64-Porträt (Mitte durchsichtig)
  dialogue/nameplate.png       48x16  3-Slice, Kappen 8 (Namensschild)
  dialogue/button.png          64x80  4 Zustaende untereinander (normal, hover, gedrueckt, gesperrt), 3-Slice, Kappen 4
  dialogue/icons.png           32x16  Weiter-Pfeil (2 Frames), Schloss, Haken; zweite Reihe: Verlauf, Schliessen, Punkt, Stern
  journal/background.png       256x192 Auftragsbuch (zwei Felder links/rechts)
  journal/tabs.png             128x56 4 Reiter (Kapitel, Auftraege, Figuren, Lore), oben aktiv, unten inaktiv
"""
from __future__ import annotations

from common import CLEAR, CYAN, GOLD, LILAC, OUTLINE, SLATE, VIOLET, Tex, hx, mix, save_png, with_alpha

FILL = hx("#150b2c", 238)
FILL_INNER = hx("#1d1040", 238)
EDGE_LIGHT = LILAC[1]
EDGE_MID = VIOLET[3]
EDGE_DARK = VIOLET[1]


def _bevel_box(t: Tex, x0, y0, w, h, fill, light, mid, dark, outline=OUTLINE):
    """Kasten mit 1-px-Kontur, Lichtkante oben/links, Schattenkante unten/rechts und Fuellung."""
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            edge = min(x - x0, y - y0, x0 + w - 1 - x, y0 + h - 1 - y)
            if edge == 0:
                c = outline
            elif edge == 1:
                bottom_right = y == y0 + h - 2 or x == x0 + w - 2
                c = dark if bottom_right else light
            elif edge == 2:
                c = mid
            else:
                c = fill
            t.set(x, y, c)


def _corner_shard(t: Tex, x, y, dx, dy):
    """Kleiner Cyan-Splitter in der Ecke, zeigt schraeg nach aussen (Fuss dunkel, Spitze hell); die Kontur bleibt."""
    t.set(x - dx, y - dy, CYAN[1])
    t.set(x, y, CYAN[2])
    t.set(x + dx, y + dy, CYAN[3])


def frame() -> Tex:
    t = Tex(32, 32)
    _bevel_box(t, 0, 0, 32, 32, FILL, EDGE_LIGHT, EDGE_MID, EDGE_DARK)
    for i in range(3, 29):                                    # innerer Schimmer an der Kante
        t.set(i, 3, with_alpha(VIOLET[2], 238))
        t.set(3, i, with_alpha(VIOLET[2], 238))
    for (x, y, dx, dy) in ((2, 2, -1, -1), (29, 2, 1, -1), (2, 29, -1, 1), (29, 29, 1, 1)):
        _corner_shard(t, x, y, dx, dy)
    return t


def portrait_frame() -> Tex:
    t = Tex(80, 80)
    _bevel_box(t, 0, 0, 80, 80, CLEAR, EDGE_LIGHT, EDGE_MID, EDGE_DARK)
    for y in range(3, 77):
        for x in range(3, 77):
            if 8 <= x < 72 and 8 <= y < 72:
                continue                                      # Fenster fuer das Porträt
            edge = min(x - 3, y - 3, 76 - x, 76 - y)
            t.set(x, y, VIOLET[1] if edge in (0, 4) else hx("#120926"))
    for i in range(7, 73):                                    # schmaler Goldfaden um das Fenster
        for (x, y) in ((i, 7), (i, 72), (7, i), (72, i)):
            t.set(x, y, GOLD[3] if x + y < 80 else GOLD[1])
    for (x, y, dx, dy) in ((2, 2, -1, -1), (77, 2, 1, -1), (2, 77, -1, 1), (77, 77, 1, 1)):
        _corner_shard(t, x, y, dx, dy)
    return t


def nameplate() -> Tex:
    """Namensschild mit spitzen Enden und Goldfaden oben und unten."""
    t = Tex(48, 16)
    for y in range(16):
        inset = abs(y - 7.5) - 4.5                            # spitze Enden: oben/unten weiter innen
        for x in range(48):
            left = x - max(0.0, inset)
            right = 47 - x - max(0.0, inset)
            if left < 0 or right < 0:
                continue
            if left < 1 or right < 1 or y in (0, 15):
                c = OUTLINE
            elif y == 1:
                c = GOLD[3]
            elif y == 14:
                c = GOLD[1]
            elif y == 2:
                c = VIOLET[2]
            else:
                c = hx("#1a0d36")
            t.set(x, y, c)
    for (x, y) in ((3, 7), (3, 8), (44, 7), (44, 8)):
        t.set(x, y, CYAN[3])
    return t


BUTTON_STATES = {
    "normal": (hx("#24124a"), LILAC[0], VIOLET[3], VIOLET[1]),
    "hover": (hx("#33196a"), CYAN[3], CYAN[1], VIOLET[2]),
    "pressed": (hx("#170b33"), VIOLET[1], VIOLET[2], LILAC[0]),
    "locked": (hx("#1c1a26"), SLATE[3], SLATE[2], SLATE[1]),
}


def button() -> Tex:
    t = Tex(64, 80)
    for i, (fill, light, mid, dark) in enumerate(BUTTON_STATES.values()):
        _bevel_box(t, 0, i * 20, 64, 20, fill, light, mid, dark)
        if i == 1:                                            # hover: leuchtende Kristallspitzen links und rechts
            for (x, y) in ((2, 9), (2, 10), (61, 9), (61, 10)):
                t.set(x, i * 20 + y, CYAN[4])
    return t


ARROW = ["#######", ".#####.", "..###..", "...#..."]
LOCK = ["..###..", ".#...#.", ".#...#.", "#######", "###.###", "###.###", "#######"]
CHECK = ["......#", ".....##", "#...##.", "##.##..", ".###...", "..#...."]
HISTORY = ["#######", "#.....#", "#.###.#", "#.....#", "#.###.#", "#.....#", "#######"]
CLOSE = ["#.....#", ".#...#.", "..#.#..", "...#...", "..#.#..", ".#...#.", "#.....#"]
DOT = ["..#..", ".###.", "#####", ".###.", "..#.."]
STAR = ["...#...", "..###..", "#######", ".#####.", ".##.##.", "#.....#"]


def _icon(t: Tex, rows, ox, oy, color, shade):
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch == "#":
                t.set(ox + i, oy + j, color if j < len(rows) / 2 else shade)


def icons() -> Tex:
    t = Tex(32, 16)
    _icon(t, ARROW, 0, 2, LILAC[2], LILAC[0])
    _icon(t, ARROW, 8, 3, CYAN[3], CYAN[2])
    _icon(t, LOCK, 16, 0, SLATE[4], SLATE[3])
    _icon(t, CHECK, 24, 1, GOLD[3], GOLD[2])
    _icon(t, HISTORY, 0, 8, LILAC[2], LILAC[1])
    _icon(t, CLOSE, 8, 8, LILAC[2], LILAC[1])
    _icon(t, DOT, 17, 9, GOLD[3], GOLD[2])
    _icon(t, STAR, 24, 9, GOLD[4], GOLD[3])
    t.selout(0.35, OUTLINE, 0.4)
    return t


def journal_background() -> Tex:
    """Auftragsbuch 256x192: Aussenrahmen wie die Dialogbox, links Liste (Feld 96 breit), rechts Detailfeld."""
    t = Tex(256, 192)
    _bevel_box(t, 0, 0, 256, 192, FILL, EDGE_LIGHT, EDGE_MID, EDGE_DARK)
    for (x0, w) in ((8, 96), (110, 138)):
        _bevel_box(t, x0, 22, w, 162, hx("#100822", 245), VIOLET[1], hx("#160b30"), LILAC[0], outline=hx("#0a0616"))
    for x in range(8, 248):                                   # Goldlinie unter der Kopfzeile
        t.set(x, 18, GOLD[2] if 20 < x < 236 else GOLD[1])
    for (x, y, dx, dy) in ((2, 2, -1, -1), (253, 2, 1, -1), (2, 189, -1, 1), (253, 189, 1, 1)):
        _corner_shard(t, x, y, dx, dy)
    return t


TAB_ICONS = {
    "chapters": ["...#...", "..###..", ".##.##.", "##...##", ".##.##.", "..###..", "...#..."],     # Emblem-Raute
    "quests": ["######.", "#....#.", "#.##.#.", "#....#.", "#.##.#.", "#....##", "######."],       # Auftragsblatt
    "npcs": ["..###..", ".#####.", ".#.#.#.", ".#####.", "..###..", ".#####.", "#######"],         # Kopf und Schultern
    "lore": ["#######", "#..#..#", "#..#..#", "#..#..#", "#..#..#", "#######", "...#..."],         # aufgeschlagenes Buch
}


def tabs() -> Tex:
    t = Tex(128, 56)
    for i, rows in enumerate(TAB_ICONS.values()):
        for active, oy in ((True, 0), (False, 28)):
            ox = i * 32
            fill = hx("#2a1556") if active else hx("#170c30")
            light = LILAC[1] if active else VIOLET[2]
            for y in range(oy, oy + 28):
                for x in range(ox + 1, ox + 31):
                    edge = min(x - ox - 1, y - oy, ox + 30 - x)
                    if y == oy + 27 and active:
                        c = fill                              # aktiver Reiter geht in die Seite ueber
                    elif edge == 0 or (y == oy + 27):
                        c = OUTLINE
                    elif edge == 1:
                        c = light if x - ox < 16 or y - oy < 2 else VIOLET[1]
                    else:
                        c = fill
                    t.set(x, y, c)
            col, shade = (GOLD[3], GOLD[2]) if active else (SLATE[4], SLATE[3])
            _icon(t, rows, ox + 12, oy + 10, col, shade)
    return t


def unknown_portrait() -> Tex:
    """Rueckfall-Porträt 64x64: Kapuzengestalt vor dunklem Violett, Gesicht im Schatten (fuer NPCs ohne eigenes Porträt)."""
    t = Tex(64, 64)
    for y in range(64):
        for x in range(64):
            band = (y // 8) % 2
            t.set(x, y, hx("#140a2a") if band else hx("#170c30"))
    for y in range(10, 64):
        for x in range(64):
            dx = x - 31.5
            head = (dx / 15.0) ** 2 + ((y - 30) / 19.0) ** 2 <= 1.0 and y < 44
            shoulders = y >= 44 and abs(dx) <= 14 + (y - 44) * 0.9
            if head or shoulders:
                lit = dx < 0
                t.set(x, y, SLATE[2] if lit else SLATE[1])
                if head and (dx / 11.0) ** 2 + ((y - 33) / 12.0) ** 2 <= 1.0:
                    t.set(x, y, hx("#07050c"))                # Gesicht im Schatten der Kapuze
    for (x, y) in ((27, 33), (36, 33)):
        t.set(x, y, CYAN[2])
    return t


ALL = {
    "gui/dialogue/frame": frame,
    "gui/dialogue/portrait_frame": portrait_frame,
    "gui/dialogue/nameplate": nameplate,
    "gui/dialogue/button": button,
    "gui/dialogue/icons": icons,
    "gui/journal/background": journal_background,
    "gui/journal/tabs": tabs,
    "gui/portraits/unknown": unknown_portrait,
}


def main():
    for name, fn in ALL.items():
        save_png(fn(), name)
    print(f"dialogue_gui: {len(ALL)} GUI-Texturen")


if __name__ == "__main__":
    main()
