"""Item-Icons (16x16), Stufe 2: alle von Grund auf neu. Aufruf: python tools/assetgen/fresh_items.py

Lichtquelle oben links, weiche Kontur (dunklerer Ton der Nachbarpixel) wie bei Vanilla. Paletten aus common.py.
Rampen in den Bildern: A = '0'-'4', B = 'a'-'e', C = 'f'-'j', D = 'k'-'o' (je dunkel -> hell).
"""
from __future__ import annotations

import math

from common import ASH, BONE, COL, CYAN, GOLD, LILAC, MAGENTA, SHARD, SKYB, SLATE, TEAL, VIOLET, Tex, clamp, hx, mix, save_png
from pixel import art, ramps, shift

OUT = hx("#10091c")
HANDLE = [hx(c) for c in ("#1a1328", "#4a3d6e", "#6a5a94", "#9283bd")]


def fin(t: Tex, base=OUT) -> Tex:
    t.selout(0.38, base, 0.22)
    return t


def line(t: Tex, pts, c):
    for (x, y) in pts:
        t.set(x, y, c)


def poly(pts, x, y):
    px, py = x + 0.5, y + 0.5
    ok, j = False, len(pts) - 1
    for i in range(len(pts)):
        xi, yi = pts[i]
        xj, yj = pts[j]
        if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi + 1e-9) + xi:
            ok = not ok
        j = i
    return ok


def crystal(t: Tex, pts, ridge, ramp_, hi=4, mid=3, lo=1, deep=0):
    """Kristall aus Polygon: Facette links der Kante hell, rechts dunkel; Kanten oben links glaenzen."""
    (x0, y0), (x1, y1) = ridge
    mask = {(x, y) for y in range(16) for x in range(16) if poly(pts, x, y)}
    for (x, y) in mask:
        side = (x1 - x0) * (y - y0) - (y1 - y0) * (x - x0)
        c = hi - 1 if side > 0 else lo + 1
        if (x - 1, y) not in mask or (x, y - 1) not in mask:
            c += 1
        if (x + 1, y) not in mask or (x, y + 1) not in mask:
            c -= 1
        t.set(x, y, ramp_[clamp(c, deep, 4)])
    return mask


# ------------------------------------------------------------------ Rohstoffe
def shard_fragment():
    t = Tex(16, 16)
    crystal(t, [(10, 1), (13, 4), (13, 7), (6, 14), (3, 11), (3, 8)], ((11, 1), (4, 13)), SHARD)
    t.set(10, 2, SHARD[4])
    t.set(11, 3, SHARD[4])
    return fin(t)


RAW = """
................
......d.........
.....dcd....e...
.....dcc...ecd..
....dccb..eccb..
....dcbb..dcbb..
...2dcbb2.dbb...
..322222222222..
.33222222222221.
.333222222222211
.33322222222211.
..332222222211..
...1111111111...
................
................
................
"""


def raw_shard():
    pal = ramps(A=[hx(c) for c in ("#18102a", "#281b42", "#3d2b62", "#59438a", "#7c64b0")], B=SHARD)
    return fin(art(RAW, pal, "raw_shard"))


INGOT = """
................
................
................
................
.....eeeeeeee...
....edddddddcc..
...edddddddccbb.
..edddddddccbbb.
..dcccccccbbbba.
..ccbbbbbbbbaaa.
...aaaaaaaaaa...
................
................
................
................
................
"""


def shard_ingot():
    return fin(art(INGOT, ramps(B=SHARD), "ingot"))


DUST = """
................
................
................
................
.......3........
......343.......
.....33433......
....3344433.....
...334444433.2..
..33444444332.2.
..2334444332.2..
...22333332.....
....1222221.....
................
................
................
"""


def ash_dust():
    t = art(DUST, ramps(A=ASH), "dust")
    t.set(8, 7, MAGENTA[2])
    return fin(t)


EYE = """
................
................
.....33333......
...3322222233...
..322244442223..
..3224bbbb4223..
..324bbaabb423..
..324bbaabb423..
..3224bbbb4223..
..322244442223..
...3322222233...
.....33333......
................
................
................
................
"""

IVORY_RAMP = [hx(c) for c in ("#6f6a82", "#9f9ab4", "#cdc9dd", "#ecebf6", "#ffffff")]


def scout_eye():
    t = art(EYE, ramps(A=IVORY_RAMP, B=MAGENTA), "eye")
    t.set(6, 5, hx("#ffffff"))
    return fin(t)


def void_ray_hide():
    t = Tex(16, 16)
    body = [(8, 2), (13, 4), (15, 8), (12, 10), (10, 14), (6, 14), (4, 10), (1, 8), (3, 4)]
    mask = {(x, y) for y in range(16) for x in range(16) if poly(body, x, y)}
    pal = [hx(c) for c in ("#0b0a24", "#14123a", "#1f1c5a", "#2b2a80", "#3b3aa8")]
    for (x, y) in mask:
        c = 2 + (1 if (x + y) % 5 == 0 else 0) - (1 if y > 9 else 0) + (1 if x + y < 12 else 0)
        t.set(x, y, pal[clamp(c, 0, 4)])
    for (x, y) in ((8, 4), (8, 6), (8, 8), (8, 10)):
        t.set(x, y, pal[4])
    for (x, y) in ((5, 7), (11, 7), (6, 11), (10, 11)):
        t.set(x, y, CYAN[2])
    return fin(t)


def shadow_pelt():
    t = Tex(16, 16)
    pelt = [hx(c) for c in ("#0d0a18", "#161228", "#211b3a", "#2d2650", "#3c3366")]
    body = [(3, 2), (6, 1), (10, 1), (13, 2), (14, 6), (13, 12), (11, 14), (9, 13), (8, 15), (7, 13), (5, 14), (3, 12), (2, 6)]
    mask = {(x, y) for y in range(16) for x in range(16) if poly(body, x, y)}
    for (x, y) in mask:
        c = 2 + (1 if (x * 3 + y) % 4 == 0 else 0) - (1 if y > 10 else 0) + (1 if x < 6 and y < 6 else 0)
        t.set(x, y, pelt[clamp(c, 0, 4)])
    for (x, y) in ((6, 4), (9, 4)):
        t.set(x, y, MAGENTA[3])
    for (x, y) in ((7, 7), (8, 8), (7, 9)):
        t.set(x, y, pelt[0])
    return fin(t)


CREST = """
................
..444444444444..
..433333333332..
..432222222232..
..4322bcb22232..
..432bcdcb2232..
..432bcdcb2232..
..4322bcb22232..
...4322222232...
....43222232....
.....432232.....
......4332......
.......22.......
................
................
................
"""


def knight_crest():
    return fin(art(CREST, ramps(A=SLATE, B=SHARD), "crest"))


BERRY = """
................
.......ff.......
......fgf.......
.......h........
....bccc.bcc....
...bcddcbcddc...
..bcdddebcdddc..
..bcdddebcdddc..
..bbcddebbcddc..
...abbbaabbbba..
....aaaa..aaa...
................
................
................
................
................
"""


def luminous_berry():
    pal = ramps(B=MAGENTA, C=TEAL)
    return fin(art(BERRY, pal, "berry"))


KEY = """
................
...bbbbb........
..bcccccb.......
.bcddeddcb......
.bcd...dcb......
.bcd...dcb......
.bcddeddcb......
..bcccccb.......
...bbbbb........
................
................
................
................
................
................
................
"""


def shard_key():
    t = art(KEY, ramps(B=SHARD), "key")
    for k in range(6):
        t.set(8 + k, 8 + k, SHARD[3] if k else SHARD[2])
        t.set(9 + k, 8 + k, SHARD[1])
    for (x, y) in ((11, 11), (13, 13)):
        t.set(x, y + 2 if False else y, SHARD[3])
    for (x, y) in ((10, 11), (12, 13), (13, 14)):
        t.set(x - 1, y, SHARD[2])
        t.set(x - 2, y, SHARD[1])
    return fin(t)


def summoning_crystal():
    t = Tex(16, 16)
    crystal(t, [(8, 0), (11, 4), (11, 11), (8, 15), (5, 11), (5, 4)], ((8, 0), (8, 15)), CYAN, hi=4, lo=1)
    for y in range(6, 12):
        t.set(8, y, CYAN[4])
    t.set(7, 4, CYAN[4])
    return fin(t, hx("#06303b"))


def colossus_core():
    t = Tex(16, 16)
    crystal(t, [(8, 1), (14, 8), (8, 15), (2, 8)], ((8, 1), (8, 15)), CYAN, hi=4, lo=1)
    for (x, y) in ((8, 6), (7, 7), (8, 7), (9, 7), (8, 8)):
        t.set(x, y, CYAN[4])
    t.set(8, 7, hx("#ffffff"))
    for (x, y) in ((5, 7), (11, 7), (8, 4), (8, 11)):
        t.set(x, y, CYAN[3])
    return fin(t, hx("#06303b"))


def smithing_template():
    t = Tex(16, 16)
    t.rect(1, 1, 14, 14, SLATE[1])
    t.frame(1, 1, 14, 14, SLATE[3])
    t.frame(2, 2, 12, 12, SLATE[0])
    t.rect(3, 3, 10, 10, SLATE[0])
    crystal(t, [(8, 4), (10, 8), (8, 12), (6, 8)], ((8, 4), (8, 12)), CYAN, hi=4, lo=1)
    for (x, y) in ((3, 3), (12, 3), (3, 12), (12, 12)):
        t.set(x, y, CYAN[1])
    t.set(8, 8, CYAN[4])
    return fin(t, hx("#08121a"))


HEART = """
................
................
..bbb....bbb....
.bcccb..bcccb...
bccdddbbcdddcb..
bcddeddcddedcb..
bcdeeeddeeedcb..
.bcdeeeeeeedb...
..bcddeeeddb....
...bccddddb.....
....bccddb......
.....bcdb.......
......bb........
................
................
................
"""


def heart_of_the_expanse():
    t = art(HEART, ramps(B=MAGENTA), "heart")
    for (x, y) in ((6, 6), (7, 6), (6, 7), (9, 6), (10, 6)):
        t.set(x, y, CYAN[3])
    t.set(7, 7, CYAN[4])
    t.set(6, 6, CYAN[4])
    return fin(t, hx("#2a0825"))


# ------------------------------------------------------------------ Waffen und Werkzeuge
def _pal(r, glow=None, guard=SLATE, acc=None):
    p = {str(i + 1): r[i] for i in range(5)}
    p.update({"h": HANDLE[1], "i": HANDLE[2], "j": HANDLE[3]})
    p.update({k: guard[n] for n, k in enumerate("abcde")})
    if glow is not None:
        p["g"] = glow
    if acc is not None:
        p["m"] = acc
    return p


def sword(r, glow=None, guard=SLATE, acc=None, wide=False, pommel=None):
    p = _pal(r, glow, guard, acc)
    t = Tex(16, 16)
    for y in range(0, 9):
        for D in ((13, 14, 15) if wide else (14, 15)):
            if y == 0 and D != 14:
                continue
            tone = {13: 4, 14: 3, 15: 2}[D] if wide else {14: 4, 15: 2}[D]
            if y <= 1 and D == 14:
                tone = 5
            if D == 13 and y == 1:
                tone = 5
            t.set(D - y, y, p[str(tone)])
    if glow is not None:
        for y in range(2, 8):
            t.set(14 - y, y, p["g"])
    for k in range(5):
        t.set(3 + k, 7 + k, p["d"])
        t.set(4 + k, 7 + k, p["b"])
    t.set(3, 7, p["e"])
    t.set(8, 11, p["a"])
    if acc is not None:
        t.set(5, 9, p["m"])
    for (x, y), c in zip(((5, 10), (4, 11), (3, 12), (2, 13)), ("j", "i", "j", "i")):
        t.set(x, y, p[c])
    t.set(1, 14, pommel or p["e"])
    return fin(t)


def shard_sword():
    return sword(SHARD, guard=[SLATE[2], SLATE[3], SLATE[4], BONE[3], BONE[4]], acc=MAGENTA[3], pommel=MAGENTA[3])


def colossus_sword():
    return sword(COL, glow=CYAN[4], guard=[SLATE[2], COL[2], COL[2], COL[3], CYAN[3]], acc=CYAN[3], wide=True, pommel=CYAN[3])


def queens_shardblade():
    p = _pal(MAGENTA, CYAN[3], SHARD, CYAN[3])
    t = Tex(16, 16)
    for y in range(0, 9):
        for D in (13, 14, 15):
            if y == 0 and D != 14:
                continue
            tone = {13: 4, 14: 3, 15: 2}[D]
            if y <= 1 and D in (13, 14):
                tone = 5
            t.set(D - y, y, p[str(tone)])
    for y in range(2, 8, 2):
        t.set(14 - y, y, p["g"])
    for (x, y, v) in ((9, 3, 4), (8, 4, 5), (6, 6, 4), (5, 7, 5), (13, 3, 1), (12, 4, 2), (10, 6, 1)):
        t.set(x, y, p[str(v)])
    for k in range(5):
        t.set(3 + k, 7 + k, p["d"])
        t.set(4 + k, 7 + k, p["b"])
    for (x, y) in ((3, 7), (3, 6)):
        t.set(x, y, p["e"])
    for (x, y) in ((8, 11), (9, 11)):
        t.set(x, y, p["a"])
    t.set(5, 9, p["m"])
    for (x, y), c in zip(((5, 10), (4, 11), (3, 12), (2, 13)), ("j", "i", "j", "i")):
        t.set(x, y, p[c])
    t.set(1, 14, p["m"])
    return fin(t, hx("#2a0825"))


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
....5...........
....55..........
....5544........
....55443.......
....554433......
....5544332.....
.....54433.2....
.....5443.22....
......543.......
.......2........
"""

SHOVEL = """
...........555..
..........5554..
.........55543..
.........55443..
........554432..
........54432...
.........432....
"""


def _tool(rows, r, n, glow=None, glow_px=()):
    p = _pal(r, glow)
    t = Tex(16, 16)
    for k in range(n):
        t.set(2 + k, 13 - k, p["i"] if k == 0 else p["j"])
    art(rows, p, "tool", tex=t)
    for (x, y) in glow_px:
        t.set(x, y, p["g"])
    return fin(t)


def shard_pickaxe():
    return _tool(PICK, SHARD, 10)


def shard_axe():
    return _tool(AXE, SHARD, 9)


def shard_shovel():
    return _tool(SHOVEL, SHARD, 7)


def colossus_pickaxe():
    return _tool(PICK, COL, 10, CYAN[4], ((8, 3),))


def colossus_axe():
    return _tool(AXE, COL, 9, CYAN[4], ((6, 5),))


def colossus_shovel():
    return _tool(SHOVEL, COL, 7, CYAN[4], ((11, 3),))


def colossus_gauntlet():
    """Geballte Faust von oben: vier Knoechelbuckel, Fingerfugen, Daumen links, breite Manschette mit Kristall."""
    t = Tex(16, 16)
    for y in range(2, 10):
        for x in range(2, 15):
            if (x in (2, 14)) and y in (2, 9):
                continue                                   # abgerundete Ecken
            c = 3
            if y == 2 or x == 2:
                c = 4
            if y >= 8 or x >= 13:
                c = 2
            if y >= 9 and x >= 12:
                c = 1
            t.set(x, y, COL[c])
    for (x0, x1) in ((3, 4), (6, 7), (9, 10), (12, 13)):   # Knoechelbuckel
        t.set(x0, 1, COL[4])
        t.set(x1, 1, COL[3])
    for x in (5, 8, 11):                                   # Fingerfugen
        for y in range(2, 7):
            t.set(x, y, COL[0])
    for x in range(3, 14):
        if x not in (5, 8, 11):
            t.set(x, 6, COL[1])                            # Fingerkruemmung
    for (x, y, c) in ((0, 5, 3), (1, 5, 4), (0, 6, 3), (1, 6, 3), (0, 7, 2), (1, 7, 3), (1, 8, 2), (1, 9, 1)):
        t.set(x, y, COL[c])                                # Daumen
    for y in range(10, 15):                                # Manschette
        for x in range(1, 15):
            c = SLATE[3] if y == 10 else SLATE[2] if y < 13 else SLATE[1] if y == 13 else SLATE[0]
            t.set(x, y, c)
    for x in range(1, 15):
        t.set(x, 10, COL[3] if x < 12 else COL[2])
    for x in (2, 4, 11, 13):
        t.set(x, 12, CYAN[2])                              # Nieten
    crystal(t, [(8, 9), (10, 12), (8, 15), (6, 12)], ((8, 9), (8, 15)), CYAN, hi=4, lo=1)
    return fin(t, hx("#06303b"))


def bow(pull: int, arrow: bool):
    """Bogen von oben rechts nach unten links, nach oben links gewoelbt; Sehne gerade oder gespannt, optional mit Pfeil."""
    t = Tex(16, 16)
    a, b, c = (13.0, 2.0), (2.0, 13.0), (1.5, 1.5)
    pts = []
    for k in range(0, 41):
        u = k / 40
        x = (1 - u) ** 2 * a[0] + 2 * u * (1 - u) * c[0] + u * u * b[0]
        y = (1 - u) ** 2 * a[1] + 2 * u * (1 - u) * c[1] + u * u * b[1]
        pts.append((int(round(x)), int(round(y))))
    seen = []
    for p in pts:
        if p not in seen:
            seen.append(p)
    for i, (x, y) in enumerate(seen):
        t.set(x, y, CYAN[3] if i % 4 else CYAN[4])
        t.set(x + 1, y + 1, CYAN[1])
    string = hx("#e8dcff")
    mx, my = (a[0] + b[0]) / 2 + pull * 1.8, (a[1] + b[1]) / 2 + pull * 1.8
    t.line(int(a[0]), int(a[1]), int(round(mx)), int(round(my)), string)
    t.line(int(round(mx)), int(round(my)), int(b[0]), int(b[1]), string)
    if arrow:
        px, py = int(round(mx)), int(round(my))
        t.line(px, py, px - 8, py - 8, hx("#b9a9d6"))
        t.set(px - 9, py - 9, MAGENTA[3])
        t.set(px - 8, py - 9, MAGENTA[2])
        t.set(px - 9, py - 8, MAGENTA[2])
    return fin(t, hx("#06303b"))


# ------------------------------------------------------------------ Ruestungs-Icons (links gezeichnet, rechts gespiegelt)
_DARKER = {"4": "3", "3": "2", "2": "1", "1": "1", "g": "g"}


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
....4444
...44444
..444333
..443333
..431111
..43g111
..43332.
..4332.3
..4322.3
""")

CHEST = _mirror("""
........
..4444..
.4433g..
.443333.
.4433333
.4433g33
.443.333
.432.333
.432.333
.....333
.....333
.....222
.....222
.....111
""")

LEGS = _mirror("""
........
........
..444444
..4333g3
..433333
..433222
..4332..
..4332..
..4332..
..4332..
..4332..
..4332..
..4322..
..2221..
""")

BOOTS = _mirror("""
........
........
..4443..
..4332..
..4332..
..4332..
..4332..
..4332..
.443332.
4433332.
4333321.
.111111.
""")


def armor_icon(rows, r, gem):
    pal = {str(i + 1): r[i] for i in range(5)}
    pal["g"] = gem
    return fin(art(rows, pal, "armor"))


def armor_set(prefix, r, gem):
    return {
        f"item/{prefix}_helmet": armor_icon(HELMET, r, gem),
        f"item/{prefix}_chestplate": armor_icon(CHEST, r, gem),
        f"item/{prefix}_leggings": armor_icon(LEGS, r, gem),
        f"item/{prefix}_boots": armor_icon(BOOTS, r, gem),
    }


def build_all():
    items = {
        "item/shard_fragment": shard_fragment, "item/raw_shard": raw_shard, "item/shard_ingot": shard_ingot,
        "item/ash_dust": ash_dust, "item/scout_eye": scout_eye, "item/void_ray_hide": void_ray_hide,
        "item/shadow_pelt": shadow_pelt, "item/knight_crest": knight_crest, "item/luminous_berry": luminous_berry,
        "item/shard_key": shard_key, "item/colossus_summoning_crystal": summoning_crystal,
        "item/colossus_core": colossus_core, "item/colossus_upgrade_smithing_template": smithing_template,
        "item/heart_of_the_expanse": heart_of_the_expanse,
        "item/shard_sword": shard_sword, "item/shard_pickaxe": shard_pickaxe, "item/shard_axe": shard_axe,
        "item/shard_shovel": shard_shovel, "item/colossus_sword": colossus_sword,
        "item/colossus_pickaxe": colossus_pickaxe, "item/colossus_axe": colossus_axe,
        "item/colossus_shovel": colossus_shovel, "item/queens_shardblade": queens_shardblade,
        "item/colossus_gauntlet": colossus_gauntlet,
        "item/crystal_bow": lambda: bow(0, False), "item/crystal_bow_pulling_0": lambda: bow(0, True),
        "item/crystal_bow_pulling_1": lambda: bow(1, True), "item/crystal_bow_pulling_2": lambda: bow(2, True),
    }
    out = {name: fn() for name, fn in items.items()}
    out.update(armor_set("shard", SHARD, MAGENTA[3]))
    out.update(armor_set("colossus", COL, CYAN[3]))
    return out


def main():
    imgs = build_all()
    for name, tex in imgs.items():
        save_png(tex, name)
    print(f"fresh_items: {len(imgs)} Texturen")


if __name__ == "__main__":
    main()
