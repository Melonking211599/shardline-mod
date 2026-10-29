"""Item-Icons (16x16), Stufe 2: alle von Grund auf neu. Aufruf: python tools/assetgen/fresh_items.py

Lichtquelle oben links, weiche Kontur (dunklerer Ton der Nachbarpixel) wie bei Vanilla. Paletten aus common.py.
Rampen in den Bildern: A = '0'-'4', B = 'a'-'e', C = 'f'-'j', D = 'k'-'o' (je dunkel -> hell).
"""
from __future__ import annotations

import math

from common import ASH, CYAN, MAGENTA, SHARD, SLATE, TEAL, Tex, clamp, hx, save_png
from pixel import art, ramps

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



def build_all():
    items = {
        "item/shard_fragment": shard_fragment, "item/raw_shard": raw_shard, "item/shard_ingot": shard_ingot,
        "item/ash_dust": ash_dust, "item/scout_eye": scout_eye, "item/void_ray_hide": void_ray_hide,
        "item/shadow_pelt": shadow_pelt, "item/knight_crest": knight_crest, "item/luminous_berry": luminous_berry,
        "item/shard_key": shard_key, "item/colossus_summoning_crystal": summoning_crystal,
        "item/colossus_core": colossus_core, "item/colossus_upgrade_smithing_template": smithing_template,
    }
    return {name: fn() for name, fn in items.items()}


def main():
    imgs = build_all()
    for name, tex in imgs.items():
        save_png(tex, name)
    print(f"fresh_items: {len(imgs)} Texturen")


if __name__ == "__main__":
    main()
