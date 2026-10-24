"""Texturen der Story-Erweiterung 2 (Bloecke, Items, GUI, Effekt, Partikel). Aufruf: python tools/assetgen/story2_assets.py

Paletten aus common.py: Virus = Schwarzviolett/Magenta (VIRUS, VGLOW), Reinheit = Elfenbein/Gold/Himmelblau
(IVORY, GOLD, SKYB), Splitterweite = Violett/Cyan (VIOLET, SHARD, CYAN). Licht kommt von oben links.
Leuchtschichten (<name>_glow.png) sind durchsichtig bis auf Adern, Runen und Kristalle.
"""
from __future__ import annotations

from common import BONE, CLEAR, COL, CYAN, GOLD, IVORY, MAGENTA, SHARD, SKYB, SLATE, VGLOW, VIOLET, VIRUS, Tex, hx, save_png
from pixel import clusters, ramps, stamp

# ------------------------------------------------------------------ Hilfen


def blank(w=16, h=16) -> Tex:
    return Tex(w, h)


def hline(t: Tex, x0, x1, y, c):
    for x in range(x0, x1 + 1):
        t.set(x, y, c)


def vline(t: Tex, x, y0, y1, c):
    for y in range(y0, y1 + 1):
        t.set(x, y, c)


def paste(dst: Tex, src: Tex, ox=0, oy=0, rows=None):
    for y in range(src.h):
        if rows is not None and not (rows[0] <= y + oy <= rows[1]):
            continue
        for x in range(src.w):
            c = src.get(x, y)
            if c[3]:
                dst.set(ox + x, oy + y, c)


def put(t: Tex, rows: str, pal: dict, ox=0, oy=0):
    """ASCII-Stempel ohne Wrap-around ('.' = unveraendert)."""
    for j, row in enumerate(rows.strip("\n").split("\n")):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set(ox + i, oy + j, pal[ch])
    return t


# ====================================================================== F2 Entscheidungsaltar
# Die Seiten der drei Modellteile liegen auf festen Zeilen: Schale y 3-5, Saeule y 6-12, Sockel y 13-15.
ALTAR_RUNE_GOLD = """
.g.
ggg
.g.
.g.
g.g
"""
ALTAR_VEIN = """
..m
.m.
mm.
.m.
m..
"""


def decision_altar_side() -> Tex:
    t = clusters(2101, IVORY, base=2, lights=4, darks=4, longest=3)
    # Schale: Goldrand mit dunkler Fuge
    for x in range(16):
        t.set(x, 3, GOLD[4] if x % 5 else GOLD[3])
        t.set(x, 4, GOLD[3])
        t.set(x, 5, GOLD[1])
    # Saeule: links Elfenbein (Siegel), rechts von Virus durchzogen (Herrschaft)
    for y in range(6, 13):
        t.set(4, y, IVORY[4])
        t.set(11, y, IVORY[0])
        for x in range(9, 12):
            if (x * 3 + y) % 5 == 0:
                t.set(x, y, VIRUS[2])
    put(t, ALTAR_RUNE_GOLD, {"g": GOLD[1]}, 5, 7)
    put(t, ALTAR_VEIN, {"m": VIRUS[3]}, 8, 7)
    # Sockel: Fase und Goldeinlage
    for x in range(16):
        t.set(x, 13, IVORY[4])
        t.set(x, 14, GOLD[2] if 2 <= x <= 13 else IVORY[2])
        t.set(x, 15, IVORY[0])
    return t


def decision_altar_side_glow() -> Tex:
    t = blank()
    put(t, ALTAR_RUNE_GOLD, {"g": GOLD[4]}, 5, 7)
    put(t, ALTAR_VEIN, {"m": VGLOW[2]}, 8, 7)
    for x in (2, 7, 12):
        t.set(x, 3, GOLD[4])                     # Funken am Schalenrand
    for x in range(3, 13, 3):
        t.set(x, 14, GOLD[4] if x < 8 else VGLOW[3])
    return t


def decision_altar_top() -> Tex:
    t = clusters(2103, IVORY, base=2, lights=3, darks=3, longest=2)
    for i in range(1, 15):                       # Sockelkante
        t.set(i, 1, IVORY[4])
        t.set(1, i, IVORY[4])
        t.set(i, 14, IVORY[1])
        t.set(14, i, IVORY[1])
    for y in range(3, 13):                       # Schale: dunkles Becken mit Goldring
        for x in range(3, 13):
            ring = max(abs(x * 2 - 15), abs(y * 2 - 15))
            if ring >= 9:
                t.set(x, y, GOLD[3] if (x + y) % 2 else GOLD[2])
            else:
                t.set(x, y, VIRUS[1] if (x + y) % 3 else VIRUS[2])
    return t


def decision_altar_top_glow() -> Tex:
    """Kristall und Ring in der Schale: links golden, rechts magenta - die beiden Wege."""
    t = blank()
    for y in range(16):
        for x in range(16):
            gold = x < 8
            if 6 <= x <= 9 and 6 <= y <= 9:
                edge = x in (6, 9) or y in (6, 9)
                t.set(x, y, (GOLD[3] if edge else GOLD[4]) if gold else (VGLOW[2] if edge else VGLOW[4]))
            elif (x in (4, 11) and 4 <= y <= 11) or (y in (4, 11) and 4 <= x <= 11):
                if (x + y) % 2 == 0:
                    t.set(x, y, GOLD[4] if gold else VGLOW[3])
    return t


def decision_altar_bottom() -> Tex:
    return clusters(2105, IVORY, base=1, lights=3, darks=4, longest=3)


# ====================================================================== F3 Hueter-Ruinen (Elfenbein, Gold, Himmelblau)
MOSS = [hx(c) for c in ("#1f5a5c", "#2c7f7a", "#46a99a")]
RUNE = """
................
................
.....gggggg.....
....g......g....
...g...ss...g...
..g...s..s...g..
..g..s....s..g..
..g..s.ss.s..g..
..g..s.ss.s..g..
..g..s....s..g..
..g...s..s...g..
...g...ss...g...
....g......g....
.....gggggg.....
................
................
"""


def keeper_ruin_bricks() -> Tex:
    """Helle Hueterziegel (versetzt, 8x4), dunkle Fugen, hier und da Goldeinlage, Moos und Risse."""
    t = Tex(16, 16)
    for y in range(16):
        row = y // 4
        for x in range(16):
            bx = (x + (4 if row % 2 else 0)) % 8
            by = y % 4
            if by == 3 or bx == 7:
                t.set(x, y, IVORY[0])
                continue
            tone = 2.0
            if by == 0 or bx == 0:
                tone = 3.0
            if by == 2 or bx == 6:
                tone = 1.6
            t.set(x, y, IVORY[int(round(tone))])
    base = clusters(3101, IVORY, base=2, lights=4, darks=4, longest=2)
    for (x, y) in ((2, 1), (11, 5), (5, 9), (13, 13), (9, 2)):
        t.set(x, y, base.get(x, y))
    for (x, y) in ((7, 3), (3, 11), (15, 7)):
        t.set(x, y, GOLD[2])                     # Goldeinlage in den Fugen
    for (x, y) in ((12, 10), (13, 10), (13, 9), (14, 8)):
        t.set(x, y, IVORY[0])                    # Riss
    for (x, y, c) in ((0, 14, 1), (1, 14, 2), (1, 13, 0), (2, 14, 1), (9, 15, 1), (10, 14, 2)):
        t.set(x, y, MOSS[c])                     # Moos am Fuss
    return t


def keeper_ruin_pillar() -> Tex:
    """Kannelierte Saeule mit goldenem Kapitell und Fuss."""
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            f = x % 4
            c = IVORY[3] if f == 0 else IVORY[2] if f in (1, 2) else IVORY[1]
            if y in (0, 15):
                c = GOLD[3] if y == 0 else GOLD[1]
            elif y in (1, 14):
                c = GOLD[2]
            elif y in (2, 13):
                c = IVORY[4] if y == 2 else IVORY[0]
            t.set(x, y, c)
    for (x, y) in ((6, 7), (6, 8), (10, 4), (2, 10)):
        t.set(x, y, IVORY[0])                    # Abplatzer
    return t


def keeper_ruin_pillar_top() -> Tex:
    t = clusters(3103, IVORY, base=2, lights=3, darks=3, longest=2)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 6.5 <= d < 7.6:
                t.set(x, y, GOLD[2] if (x + y) % 2 else GOLD[3])
            elif d < 2.2:
                t.set(x, y, GOLD[3] if x + y < 15 else GOLD[2])
            elif 2.2 <= d < 3.0:
                t.set(x, y, IVORY[0])
    return t


def keeper_rune_slab() -> Tex:
    t = clusters(3105, IVORY, base=2, lights=4, darks=3, longest=2)
    for i in range(16):
        t.set(i, 0, IVORY[4])
        t.set(0, i, IVORY[4])
        t.set(i, 15, IVORY[1])
        t.set(15, i, IVORY[1])
    put(t, RUNE, {"g": GOLD[1], "s": IVORY[0]})  # eingemeisselte Rune
    return t


def keeper_rune_slab_glow() -> Tex:
    t = blank()
    put(t, RUNE, {"g": SKYB[3], "s": GOLD[4]})
    return t


def keeper_rune_slab_side() -> Tex:
    """Zwei uebereinanderliegende Stufen (die Seite der unteren Stufe nutzt die untere Haelfte)."""
    t = Tex(16, 16)
    for half in (0, 8):
        for y in range(half, half + 8):
            for x in range(16):
                ly = y - half
                c = IVORY[4] if ly == 0 else IVORY[0] if ly == 7 else IVORY[2]
                if ly == 4 and 1 <= x <= 14:
                    c = GOLD[2] if x % 3 else GOLD[3]
                elif ly in (1, 6) and x % 5 == 2:
                    c = IVORY[3] if ly == 1 else IVORY[1]
                t.set(x, y, c)
    return t


# ====================================================================== F4 Immunitaet (Effekt-Icon 18x18)
IMMUNITY_ICON = """
..................
...oooooooooooo...
..oGGGGGGGGGGGGo..
..oGWWWWWWWWWWgo..
..oGWWWWbbWWWWgo..
..oGWWWbBBbWWWgo..
..oGWWbBSSBbWWgo..
..oGWWbBSBBbWWgo..
..oGWWbBBBBbWWgo..
..oGWWWbBBbWWWgo..
...oGWWWbbWWWgo...
...oGWWWWWWWWgo...
....oGWWWWWWgo....
.....oGWWWWgo.....
......oGWWgo......
.......oggo.......
........oo........
..................
"""


def immunity_icon() -> Tex:
    t = Tex(18, 18)
    pal = {"o": hx("#2a1d10"), "G": GOLD[3], "g": GOLD[1], "b": SKYB[0], "B": SKYB[2], "S": SKYB[4]}
    for y, row in enumerate(IMMUNITY_ICON.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch == "W":
                t.set(x, y, IVORY[4] if x < 9 else IVORY[3] if y < 12 else IVORY[2])
            elif ch != ".":
                t.set(x, y, pal[ch])
    return t


# ====================================================================== F6 Haendler-Dialog und lose Lore-Seite
PARCH = [hx(c) for c in ("#4a3826", "#7d6346", "#ad9168", "#d6bf95", "#efe2c4")]
INK = hx("#2b1840")
PAGE = """
..oooooooooo....
.oPPPPPPPPPPo...
.oPWWWWWWWWPPo..
.oPWiiiiiiWWPo..
.oPWWWWWWWWWPo..
.oPWiiiiiiiiWPo.
.oPWWWWWWWWWWPo.
.oPWiiiiiWWWWPo.
.oPWWWWWWWWWWPo.
.oPWiiiiiiiWWPo.
.oPWWWWWWWWWWPo.
.oPWssWiiiWWPo..
.oPsSSsWWWWPPo..
.oPPssPPPPoo....
..ooooooo.......
................
"""


def lore_page() -> Tex:
    """Herausgerissene Seite: Pergament, Schriftzeilen in Tinte, violettes Siegel, ausgerissene Ecke rechts unten."""
    pal = {"o": PARCH[0], "P": PARCH[2], "W": PARCH[4], "i": INK, "s": SHARD[1], "S": SHARD[3]}
    t = Tex(16, 16)
    for y, row in enumerate(PAGE.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch == "W":
                t.set(x, y, PARCH[4] if x + y < 14 else PARCH[3])
            elif ch != ".":
                t.set(x, y, pal[ch])
    return t


def portrait() -> Tex:
    """24x24-Porträt des Splitterhaendlers: violette Kapuze mit Goldsaum (einst ein Hueter), fahles Gesicht,
    Magenta-Augen, grosse Nase, Amulett. Hintergrund: Nachthimmel der Splitterweite."""
    t = Tex(24, 24)
    for y in range(24):
        for x in range(24):
            t.set(x, y, VIOLET[0] if y < 12 else hx("#140a26"))
    for (x, y) in ((2, 3), (20, 2), (4, 9), (21, 7), (1, 15)):
        t.set(x, y, CYAN[3])
    for y in range(24):
        for x in range(24):
            hood = ((x - 11.5) / 10.0) ** 2 + ((y - 11.0) / 11.5) ** 2 <= 1.0 or (y >= 17 and abs(x - 11.5) <= 6 + (y - 17) * 1.2)
            if not hood:
                continue
            face = ((x - 11.5) / 5.2) ** 2 + ((y - 13.0) / 6.2) ** 2 <= 1.0 and y <= 19
            trim = not face and ((x - 11.5) / 6.4) ** 2 + ((y - 13.0) / 7.4) ** 2 <= 1.0 and y <= 20
            if face:
                shade = 2.6 - (0.9 if y <= 8 else 0) + (0.4 if x < 11 else -0.2)
                t.set(x, y, BONE[max(0, min(4, int(round(shade))))])
            elif trim:
                t.set(x, y, GOLD[3] if x + y < 24 else GOLD[2])
            else:
                tone = 3 if x < 9 and y < 14 else 2 if x < 15 else 1
                if y >= 20:
                    tone -= 1
                t.set(x, y, VIOLET[max(0, tone)])
    # Schatten der Kapuze ueber der Stirn
    for x in range(8, 16):
        t.set(x, 7, VIOLET[0])
        if 9 <= x <= 14:
            t.set(x, 8, BONE[1])
    # Augen (leuchtend) und Brauen
    for (x, y, c) in ((8, 11, BONE[0]), (9, 11, BONE[0]), (14, 11, BONE[0]), (15, 11, BONE[0]),
                      (8, 12, VGLOW[3]), (9, 12, VGLOW[1]), (14, 12, VGLOW[3]), (15, 12, VGLOW[1])):
        t.set(x, y, c)
    # Nase
    for y in range(12, 18):
        t.set(11, y, BONE[3])
        t.set(12, y, BONE[2])
    t.set(11, 17, BONE[2])
    t.set(12, 17, BONE[1])
    hline(t, 10, 13, 18, BONE[1])
    # Amulett
    for (x, y, c) in ((11, 21, GOLD[3]), (12, 21, GOLD[2]), (11, 22, CYAN[3]), (12, 22, CYAN[2]), (10, 22, GOLD[2]), (13, 22, GOLD[1])):
        t.set(x, y, c)
    return t


def trader_dialogue() -> Tex:
    """Atlas 256x128: Dialograhmen 240x100 mit Porträtrahmen (6,6, 56x56) und Namensleiste."""
    W, H = 240, 100
    t = Tex(256, 128)
    for y in range(H):
        for x in range(W):
            base = hx("#170c28") if y < 50 else hx("#130a22")
            t.set(x, y, base)
    # Aussenrahmen: dunkel, Gold (oben/links heller), dunkel
    for x in range(W):
        for y in range(H):
            ring = min(x, y, W - 1 - x, H - 1 - y)
            if ring == 0:
                t.set(x, y, hx("#07030c"))
            elif ring in (1, 2):
                light = min(x, y) <= min(W - 1 - x, H - 1 - y)   # oben und links hell, unten und rechts dunkel
                t.set(x, y, GOLD[3] if ring == 1 and light else GOLD[2] if light else GOLD[1])
            elif ring == 3:
                t.set(x, y, hx("#0b0516"))
    # Eckornamente (Goldraute mit Magenta-Edelstein)
    for (cx, cy) in ((4, 4), (W - 5, 4), (4, H - 5), (W - 5, H - 5)):
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                d = abs(dx) + abs(dy)
                if d <= 3:
                    t.set(cx + dx, cy + dy, VGLOW[3] if d == 0 else VGLOW[1] if d == 1 else GOLD[3] if d == 2 else GOLD[1])
    # Porträtrahmen
    px, py, pw = 6, 6, 56
    for y in range(py, py + pw):
        for x in range(px, px + pw):
            ring = min(x - px, y - py, px + pw - 1 - x, py + pw - 1 - y)
            if ring == 0:
                t.set(x, y, hx("#07030c"))
            elif ring == 1:
                t.set(x, y, GOLD[3] if min(x - px, y - py) <= min(px + pw - 1 - x, py + pw - 1 - y) else GOLD[1])
            elif ring == 2:
                t.set(x, y, hx("#07030c"))
    art = portrait()
    for y in range(48):
        for x in range(48):
            t.set(px + 4 + x, py + 4 + y, art.get(x // 2, y // 2))
    # Namensleiste mit Goldlinie
    for x in range(66, W - 8):
        for y in range(5, 18):
            t.set(x, y, hx("#0e0719"))
        t.set(x, 18, GOLD[2] if (x // 3) % 2 == 0 else GOLD[1])
    # Textfeld leicht abgesetzt
    for x in range(66, W - 8):
        for y in range(20, 70):
            if (x + y) % 2 == 0:
                c = t.get(x, y)
                t.set(x, y, (min(255, c[0] + 4), min(255, c[1] + 2), min(255, c[2] + 8), 255))
    return t


# ====================================================================== F7 Kapitel-Embleme (16x16) und Kapitelkarte
# Symbol je Kapitel (10x10, zentriert im Medaillon). Zeichen -> Farbe je Emblem.
EMBLEMS = {
    1: ("""
......44..
.....443..
....4432..
...44321..
..44321...
..4321....
.4321.....
.321......
.21.......
..........
""", {"4": SHARD[4], "3": SHARD[3], "2": SHARD[2], "1": SHARD[1]}, VIOLET[0]),     # der erste Splitter
    2: ("""
.pppppppp.
.pccccccp.
.pc3333cp.
.pc3bb3cp.
.pc3bb3cp.
.pc3333cp.
.pccccccp.
.pccccccp.
.pppppppp.
..........
""", {"p": VIOLET[3], "c": VIOLET[1], "3": CYAN[2], "b": CYAN[4]}, hx("#0d0820")),   # das Tor
    3: ("""
....44....
...4443...
...4432...
..443322..
..443321..
..443321..
...4321...
.ssssssss.
.s222222s.
..........
""", {"4": CYAN[4], "3": CYAN[3], "2": CYAN[2], "1": CYAN[1], "s": IVORY[1]}, hx("#0a1424")),  # Echos: Erinnerungskristall
    4: ("""
....44....
...4443...
..444332..
.44433322.
.44333221.
..333221..
...3221...
....21....
..........
..........
""", {"4": COL[4], "3": COL[3], "2": COL[2], "1": COL[1]}, hx("#071a20")),       # Herz der Weite: Kern des Kolosses
    5: ("""
....m.....
....mm....
...gm.....
...mmm....
....gm....
...mm.g...
..mm..m...
..m....m..
.m......m.
..........
""", {"m": VGLOW[2], "g": VGLOW[4]}, VIRUS[0]),                                 # der Bruch: Riss
    6: ("""
....gg....
...gGGg...
....gg....
....gg....
..s.gg.s..
..sggggs..
...ggggg..
....gg....
...gggg...
..........
""", {"g": GOLD[2], "G": SKYB[3], "s": GOLD[3]}, hx("#1a1406")),                 # Eindaemmung: Reinheitsanker
}


def chapter_emblem(n: int) -> Tex:
    art, pal, inner = EMBLEMS[n]
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if d <= 7.6:
                if d > 6.4:
                    t.set(x, y, GOLD[3] if x + y < 15 else GOLD[1])      # Goldring, Licht oben links
                elif d > 5.6:
                    t.set(x, y, GOLD[0])
                else:
                    t.set(x, y, inner)
    put(t, art, pal, 3, 3)
    t.outline(hx("#140b05"))
    return t


def chapter_card() -> Tex:
    """Banner 256x72: dunkles Violett, an den Enden ausgefranst, Goldlinien oben/unten mit Raute, Medaillonrahmen links."""
    W, H = 256, 72
    t = Tex(256, 128)
    for y in range(H):
        for x in range(W):
            edge = min(x, W - 1 - x)
            fade = min(1.0, edge / 6.0)
            if (x + y) % 3 == 0 and edge < 4:
                continue                                       # ausgefranste Enden
            a = int(215 * fade)
            if a <= 0:
                continue
            c = hx("#140a26") if y < H // 2 else hx("#10081f")
            t.set(x, y, (c[0], c[1], c[2], a))
    for x in range(16, W - 16):
        fade = min(1.0, min(x - 16, W - 17 - x) / 30.0)
        if fade <= 0:
            continue
        for y, col in ((3, GOLD[3]), (4, GOLD[1]), (H - 5, GOLD[2]), (H - 4, GOLD[1])):
            t.set(x, y, (col[0], col[1], col[2], int(255 * fade)))
    for (cx, cy) in ((W // 2, 4), (W // 2, H - 5)):
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                d = abs(dx) + abs(dy)
                if d <= 3:
                    t.set(cx + dx, cy + dy, VGLOW[3] if d == 0 else GOLD[4] if d == 1 else GOLD[2])
    # Medaillonrahmen fuer das Emblem (bei 10,12, 48x48; das Emblem wird mit 32x32 bei 18,20 gezeichnet)
    for y in range(12, 60):
        for x in range(10, 58):
            d = ((x - 33.5) ** 2 + (y - 35.5) ** 2) ** 0.5
            if 21.5 < d <= 23.5:
                t.set(x, y, GOLD[3] if x + y < 69 else GOLD[1])
            elif 20.5 < d <= 21.5:
                t.set(x, y, hx("#07030c"))
            elif d <= 20.5:
                t.set(x, y, hx("#0b0618"))
    return t


# ====================================================================== F8 Reinheitszone: goldene Funken (8x8, 4 Frames)
MOTES = [
    """
...w....
...g....
..ggg...
wgggggw.
..ggg...
...g....
...w....
........
""",
    """
........
...g....
..gwg...
.gwwwg..
..gwg...
...g....
........
........
""",
    """
........
........
...g....
..gwg...
...g....
........
........
........
""",
    """
........
........
........
...w....
........
........
........
........
""",
]


def purity_mote(frame: int) -> Tex:
    """Goldener Funke, wird von Frame zu Frame kleiner (setSpriteFromAge); w = fast weisser Kern."""
    t = Tex(8, 8)
    put(t, MOTES[frame], {"g": GOLD[3], "w": GOLD[4]})
    return t


OTHER = {
    "mob_effect/immunity": immunity_icon,
    **{f"particle/purity_mote_{n}": (lambda n=n: purity_mote(n)) for n in range(4)},
    "gui/chapter_card": chapter_card,
    "gui/trader_dialogue": trader_dialogue,
}

BLOCKS = {
    "block/keeper_ruin_bricks": keeper_ruin_bricks,
    "block/keeper_ruin_pillar": keeper_ruin_pillar,
    "block/keeper_ruin_pillar_top": keeper_ruin_pillar_top,
    "block/keeper_rune_slab": keeper_rune_slab,
    "block/keeper_rune_slab_glow": keeper_rune_slab_glow,
    "block/keeper_rune_slab_side": keeper_rune_slab_side,
    "block/decision_altar_side": decision_altar_side,
    "block/decision_altar_side_glow": decision_altar_side_glow,
    "block/decision_altar_top": decision_altar_top,
    "block/decision_altar_top_glow": decision_altar_top_glow,
    "block/decision_altar_bottom": decision_altar_bottom,
}
ITEMS = {
    "item/lore_page": lore_page,
    **{f"item/chapter_emblem_{n}": (lambda n=n: chapter_emblem(n)) for n in range(1, 7)},
}


def main():
    for name, fn in {**BLOCKS, **ITEMS, **OTHER}.items():
        save_png(fn(), name)
    print(f"story2_assets: {len(BLOCKS)} Block-, {len(ITEMS)} Item- und {len(OTHER)} weitere Texturen")


if __name__ == "__main__":
    main()
