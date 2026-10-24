"""Kristallisierte Tiere und Dorfbewohner (Vanilla-Modelle) und die Kragen gezaehmter infizierter Wesen.

Aufruf: python tools/assetgen/infected_animals.py

Alle Texturen liegen exakt im Vanilla-Layout (Schaf 64x32 + Vlies 64x32, Schwein 64x32, Dorfbewohner 64x64), die
Modelle sind die Vanilla-Modelle; Kristalle sind deshalb aufgemalt. Zu jeder Textur gibt es eine _glowmask
(deckend = leuchtet: Augen, Adern, Kristallspitzen).

Kragen (<wesen>_collar.png): durchsichtig bis auf das Halsband, im Layout des Modells, auf dem es gezeichnet wird:
Kuh/Schwein -> Kopf, Schaf -> Kopf des Vlies-Modells, Dorfbewohner -> Robe (jacket), Shard-Zombie -> Jacke.
"""
from __future__ import annotations

import math

from clean import ordered
from common import TEX, VGLOW, VIRUS, Tex, hx, ramp, vnoise
from ent import FACE_ID
from infected_entities import CRY, HEART, ZSKIN, flesh
from uvlib import FACE_LIGHT, edge_light, paint_box

# ------------------------------------------------------------------ Paletten (Virus: Schwarzviolett / Magenta)
SSKIN = [hx(c) for c in ("#221a2d", "#352b45", "#4c4061", "#665a80", "#8579a2")]     # geschorene Schafhaut
FLEECE = [hx(c) for c in ("#160818", "#28102e", "#3e1a48", "#5a2766", "#7a3886")]    # Kristallvlies
PSKIN = [hx(c) for c in ("#2e1a29", "#48293f", "#663a58", "#865074", "#a86c93")]     # kranke Schweinehaut
PSNOUT = [hx(c) for c in ("#3c2234", "#5c344f", "#7e4a6d", "#a2648d", "#c486ad")]
VROBE = [hx(c) for c in ("#100914", "#1d1126", "#2d1a3b", "#412754", "#58376f")]     # Robe des Dorfbewohners
VSASH = [hx(c) for c in ("#3a0c3a", "#5c1555", "#7d1f70", "#a3308e", "#c745ab")]
HOOF = [hx(c) for c in ("#0c070f", "#170e1c", "#22162a")]
LEATHER_V = [hx(c) for c in ("#2a0828", "#4a1145", "#6c1a64", "#922783", "#b73aa3")]  # Herrscher-Halsband


def lit(face, fx, fy, fw, fh, bias, seed=0):
    """Licht oben links, weiche Flecken aus Rauschen, nur ein Hauch Raster-Dither (keine Punktgitter)."""
    mottle = (vnoise(fx / 2.6 + FACE_ID[face] * 7.3, fy / 2.6, seed) - 0.5) * 0.9
    return bias + FACE_LIGHT[face] * 0.7 + edge_light(fx, fy, fw, fh) * 0.35 + mottle + ordered(fx, fy) * 0.18


def skin(r, bias=2.2, seed=0):
    def p(face, fx, fy, fw, fh):
        return ramp(r, lit(face, fx, fy, fw, fh, bias, seed))
    return p


def crust(cells: dict, base, faces=None):
    """Kristallkruste: Zellen {(x, y): stufe 0..4} auf ausgewaehlten Flaechen, ab Stufe 2 leuchtend."""
    def p(face, fx, fy, fw, fh):
        if faces is None or face in faces:
            c = cells.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
        return base(face, fx, fy, fw, fh)
    return p


def cluster(cx, cy, size=1):
    """Kleiner Kristall: helle Spitze oben links, nach unten rechts dunkler."""
    cells = {(cx, cy): 4, (cx + 1, cy): 3, (cx, cy + 1): 3, (cx + 1, cy + 1): 2}
    if size > 1:
        cells.update({(cx - 1, cy + 1): 2, (cx, cy + 2): 2, (cx + 1, cy + 2): 1, (cx + 2, cy + 1): 1})
    return cells


def merge(*dicts):
    out = {}
    for d in dicts:
        out.update(d)
    return out


# ====================================================================== Schaf (Vanilla-Schaf 64x32)
S_HEAD, S_BODY, S_LEG = (0, 0, 6, 6, 8), (28, 8, 8, 16, 6), (0, 16, 4, 12, 4)
F_HEAD, F_BODY, F_LEG = (0, 0, 6, 6, 6), (28, 8, 8, 16, 6), (0, 16, 4, 6, 4)


def sheep_head():
    base = skin(SSKIN, 2.3, 7311)

    def p(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 2 and fx in (1, 4):
                return (VGLOW[3], True)                   # Augen
            if fy == 1 and fx in (1, 4):
                return SSKIN[0]
            if fy == 4 and fx in (2, 3):
                return SSKIN[0]                           # Nase
            if fy == 5 and 1 <= fx <= 4:
                return ramp(SSKIN, 1.2 + ordered(fx, fy) * 0.4)
            if fx == 3 and fy == 0:
                return (VGLOW[1], True)
        if face == "east" and (fx, fy) in ((5, 2), (6, 3), (6, 4)):
            return (VGLOW[1], True)                       # Ader hinter dem Auge
        return base(face, fx, fy, fw, fh)
    return p


def sheep_body():
    base = skin(SSKIN, 2.1, 7313)
    spine = merge(cluster(3, 2), cluster(4, 7, 2), cluster(3, 12))

    def p(face, fx, fy, fw, fh):
        if face == "north":
            c = spine.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
        if face in ("east", "west") and (fx, fy) in ((1, 5), (2, 6), (2, 7), (3, 8), (3, 9), (4, 10)):
            return (VGLOW[1], True)
        return base(face, fx, fy, fw, fh)
    return p


def leg(r, seed, hoof_rows=1):
    base = skin(r, 2.0, 7320 + seed)

    def p(face, fx, fy, fw, fh):
        if face == "down":
            return HOOF[0]
        if fy >= fh - hoof_rows and face != "up":
            return ramp(HOOF, 1.4 + FACE_LIGHT[face] * 0.6)
        if fy == fh - hoof_rows - 2 and face != "up":
            return (ramp(CRY, 3.0 + FACE_LIGHT[face] * 0.5), (fx + seed) % 2 == 0)
        return base(face, fx, fy, fw, fh)
    return p


def fleece(seed, bias=2.2):
    """Vlies aus weichen Bueschen (helle Oberkante, dunkle Fugen); dazwischen wenige Kristalle mit leuchtender Spitze."""
    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 13.7
        n = vnoise(fx / 2.8 + off, fy / 2.8, seed)
        up = vnoise(fx / 2.8 + off, (fy - 1) / 2.8, seed)
        idx = bias + FACE_LIGHT[face] * 0.8 + (n - 0.5) * 1.2 + (n - up) * 3.0 + edge_light(fx, fy, fw, fh) * 0.3
        k = vnoise(fx / 1.7 + off, fy / 1.7, seed + 99)
        if k > 0.83:
            tip = k > 0.9 and (fx + fy) % 2 == 0
            return (ramp(CRY, 3.6 if tip else 2.0 + (k - 0.83) * 14), tip)
        return ramp(FLEECE, idx + ordered(fx, fy) * 0.15)
    return p


def sheep_texture():
    t, g = Tex(64, 32), Tex(64, 32)
    paint_box(t, *S_HEAD, sheep_head(), g)
    paint_box(t, *S_BODY, sheep_body(), g)
    paint_box(t, *S_LEG, leg(SSKIN, 1), g)
    t.save(TEX / "entity" / "crystallized_sheep.png")
    g.save(TEX / "entity" / "crystallized_sheep_glowmask.png")

    ft, fg = Tex(64, 32), Tex(64, 32)
    paint_box(ft, *F_HEAD, fleece(7301, 2.4), fg)
    paint_box(ft, *F_BODY, fleece(7303), fg)
    paint_box(ft, *F_LEG, fleece(7305, 2.0), fg)
    ft.save(TEX / "entity" / "crystallized_sheep_fur.png")
    fg.save(TEX / "entity" / "crystallized_sheep_fur_glowmask.png")
    return t, g, ft, fg


# ====================================================================== Schwein (Vanilla-Schwein 64x32)
P_HEAD, P_SNOUT, P_BODY, P_LEG = (0, 0, 8, 8, 8), (16, 16, 4, 3, 1), (28, 8, 10, 16, 8), (0, 16, 4, 6, 4)


def pig_head():
    base = skin(PSKIN, 2.3, 7401)

    def p(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 3 and fx in (1, 6):
                return (VGLOW[3], True)
            if fy == 3 and fx in (0, 7):
                return PSKIN[0]
            if fy == 2 and fx in (1, 6):
                return PSKIN[1]
            if fx == 3 and fy in (0, 1):
                return (VGLOW[1], True)                   # Riss ueber der Stirn
        if face == "up":
            c = merge(cluster(2, 2), cluster(5, 4)).get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
        return base(face, fx, fy, fw, fh)
    return p


def pig_snout():
    def p(face, fx, fy, fw, fh):
        if face == "north" and fy == 1 and fx in (0, 3):
            return PSNOUT[0]                              # Nasenloecher
        return ramp(PSNOUT, lit(face, fx, fy, fw, fh, 2.4, 7405))
    return p


def pig_body():
    base = skin(PSKIN, 2.1, 7403)
    spine = merge(cluster(4, 2, 2), cluster(5, 7), cluster(4, 11, 2))

    def p(face, fx, fy, fw, fh):
        if face == "north":
            c = spine.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
            if fx == 5 and fy % 4 == 0:
                return (VGLOW[1], True)
        if face in ("east", "west"):
            n = vnoise(fx / 3.0 + FACE_ID[face] * 5, fy / 3.0, 7411)
            if n > 0.72:
                return ramp(HEART, 2.2 + (n - 0.72) * 6)  # kranke Flecken
            if (fx, fy) in ((2, 4), (3, 5), (3, 6), (4, 7), (5, 8)):
                return (VGLOW[1], True)
        return base(face, fx, fy, fw, fh)
    return p


def pig_texture():
    t, g = Tex(64, 32), Tex(64, 32)
    paint_box(t, *P_HEAD, pig_head(), g)
    paint_box(t, *P_SNOUT, pig_snout(), g)
    paint_box(t, *P_BODY, pig_body(), g)
    paint_box(t, *P_LEG, leg(PSKIN, 2), g)
    t.save(TEX / "entity" / "crystallized_pig.png")
    g.save(TEX / "entity" / "crystallized_pig_glowmask.png")
    return t, g


# ====================================================================== Dorfbewohner (Vanilla-Dorfbewohner 64x64)
V_HEAD, V_HAT, V_NOSE = (0, 0, 8, 10, 8), (32, 0, 8, 10, 8), (24, 0, 2, 4, 2)
V_BODY, V_JACKET = (16, 20, 8, 12, 6), (0, 38, 8, 20, 6)
V_ARMS, V_HANDS, V_LEG = (44, 22, 4, 8, 4), (40, 38, 8, 4, 4), (0, 22, 4, 12, 4)


def robe(seed, bias=2.0):
    def p(face, fx, fy, fw, fh):
        fold = math.sin((fx + FACE_ID[face] * 3) * 1.7) * 0.35
        return ramp(VROBE, lit(face, fx, fy, fw, fh, bias, seed) + fold)
    return p


def villager_head():
    base = skin(ZSKIN, 2.3, 7531)

    def p(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 3 and 1 <= fx <= 6 and fx not in (3, 4):
                return ZSKIN[0]                           # schwere Brauen
            if fy == 4 and fx in (1, 6):
                return ZSKIN[4]
            if fy == 4 and fx in (2, 5):
                return (VGLOW[3], True)                   # Pupillen leuchten
            if fy == 7 and 2 <= fx <= 5:
                return ZSKIN[0] if fx in (2, 5) else VIRUS[2]
            c = merge(cluster(6, 6), {(7, 5): 2}).get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
            if fx == 2 and fy in (0, 1, 2):
                return (VGLOW[1], True)
        if face == "west" and (fx, fy) in ((2, 4), (3, 5), (3, 6), (4, 7)):
            return (VGLOW[1], True)
        return base(face, fx, fy, fw, fh)
    return p


def villager_hat():
    """Hut-Ebene: nur Kristalle, die aus dem Kopf wachsen; sonst durchsichtig."""
    top = merge(cluster(1, 1, 2), cluster(5, 3), cluster(3, 5, 2))
    north = {(1, 0): 3, (2, 0): 4, (2, 1): 3, (6, 0): 2}
    side = merge(cluster(2, 0), {(5, 0): 2})

    def p(face, fx, fy, fw, fh):
        cells = {"up": top, "north": north, "east": side, "west": {(4, 0): 3, (4, 1): 2}}.get(face, {})
        c = cells.get((fx, fy))
        if c is None:
            return None
        return (ramp(CRY, c), c >= 3)
    return p


def villager_nose():
    base = skin(ZSKIN, 2.6)

    def p(face, fx, fy, fw, fh):
        if fy == 3 and face == "north":
            return (ramp(CRY, 3), True)                   # Kristallspitze an der Nase
        return base(face, fx, fy, fw, fh)
    return p


def villager_body():
    base = robe(7501)
    chest = merge(cluster(4, 3, 2), cluster(2, 7))

    def p(face, fx, fy, fw, fh):
        if face == "north":
            c = chest.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
            if fx == 3 and 6 <= fy <= 9:
                return (VGLOW[1], True)
        if fy == 8 and face != "up":
            return ramp(VSASH, 2.0 + FACE_LIGHT[face] * 0.6)   # Guertel
        return base(face, fx, fy, fw, fh)
    return p


def villager_jacket():
    """Uebergewand: Robe bis zu den Knoecheln, unten zerfetzt (durchsichtige Zacken), Kristalle wachsen hindurch."""
    base = robe(7511, 2.1)
    shard = merge(cluster(5, 4, 2), cluster(1, 12), cluster(6, 15))

    def p(face, fx, fy, fw, fh):
        if face in ("up", "down"):
            return None
        hem = fh - 1 - ((fx * 5 + FACE_ID[face] * 3) % 4)
        if fy > hem:
            return None
        if face == "north":
            c = shard.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 3)
            if fy == 0 and 2 <= fx <= 5:
                return ramp(VSASH, 2.4)                   # Kragenborte
        if fy == 8:
            return ramp(VSASH, 2.0 + FACE_LIGHT[face] * 0.6)
        if fy == hem:
            return ramp(VROBE, 0.8)
        return base(face, fx, fy, fw, fh)
    return p


def villager_arms():
    base = robe(7521, 2.0)

    def p(face, fx, fy, fw, fh):
        if fy >= fh - 1 and face != "up":
            return ramp(VSASH, 1.8 + FACE_LIGHT[face] * 0.5)   # Aermelsaum
        if face == "north" and (fx, fy) in ((1, 2), (2, 2), (2, 3)):
            return (ramp(CRY, 3 if (fx, fy) == (1, 2) else 2), (fx, fy) == (1, 2))
        return base(face, fx, fy, fw, fh)
    return p


def villager_hands():
    base = skin(ZSKIN, 2.2)

    def p(face, fx, fy, fw, fh):
        if face == "north" and (fx, fy) in ((2, 1), (5, 2)):
            return (VGLOW[2], True)
        return base(face, fx, fy, fw, fh)
    return p


def villager_leg():
    base = skin(ZSKIN, 1.9)

    def p(face, fx, fy, fw, fh):
        if face == "down" or fy >= fh - 1:
            return HOOF[1]
        return base(face, fx, fy, fw, fh)
    return p


def villager_texture():
    t, g = Tex(64, 64), Tex(64, 64)
    paint_box(t, *V_HEAD, villager_head(), g)
    paint_box(t, *V_HAT, villager_hat(), g)
    paint_box(t, *V_NOSE, villager_nose(), g)
    paint_box(t, *V_BODY, villager_body(), g)
    paint_box(t, *V_JACKET, villager_jacket(), g)
    paint_box(t, *V_ARMS, villager_arms(), g)
    paint_box(t, *V_HANDS, villager_hands(), g)
    paint_box(t, *V_LEG, villager_leg(), g)
    t.save(TEX / "entity" / "crystallized_villager.png")
    g.save(TEX / "entity" / "crystallized_villager_glowmask.png")
    return t, g


# ====================================================================== Kragen (Halsband der Splitterherrscher)
def collar_band(width=2, top_rows=None, back_offset=1):
    """Halsband um den Kopf (nahe der Rueckseite) bzw. oben um einen Rumpf; Kristallnieten, vorne ein Anhaenger."""
    def leather(face, fx, fy, along):
        edge = fy if face in ("east", "west", "north", "south") else fx
        c = ramp(LEATHER_V, 2.0 + FACE_LIGHT[face] * 0.8 + (0.6 if along == 0 else -0.3))
        if along == 0 and edge % 3 == 1:
            return VGLOW[3]                               # Niete
        return c

    lo, hi = back_offset, back_offset + width

    def head(face, fx, fy, fw, fh):
        if face == "east" and lo <= fx < hi:
            return leather(face, fx, fy, fx - lo)
        if face == "west" and fw - hi <= fx < fw - lo:
            return leather(face, fx, fy, fw - 1 - lo - fx)
        if face == "up" and lo <= fy < hi:
            return leather(face, fx, fy, fy - lo)
        if face == "down" and fh - hi <= fy < fh - lo:
            if fx in (fw // 2 - 1, fw // 2):
                return CRY[4] if fx == fw // 2 - 1 else CRY[3]   # Anhaenger unter dem Kinn
            return leather(face, fx, fy, fh - 1 - lo - fy)
        return None

    def torso(face, fx, fy, fw, fh):
        if face == "up":
            return leather(face, fx, fy, 0) if (fy in (0, fh - 1) or fx in (0, fw - 1)) else None
        if face == "down" or fy >= top_rows:
            return None
        if face == "north" and fx in (fw // 2 - 1, fw // 2) and fy == top_rows - 1:
            return CRY[4] if fx == fw // 2 - 1 else CRY[3]   # Anhaenger vorne
        return leather(face, fx, fy, fy)

    return torso if top_rows else head


def collar(size, box, painter, name):
    t = Tex(*size)
    paint_box(t, *box, painter)
    t.save(TEX / "entity" / f"{name}_collar.png")
    return t


def collars():
    head = collar_band(2)
    out = [
        collar((64, 32), (0, 0, 8, 8, 6), head, "crystallized_cow"),
        collar((64, 32), F_HEAD, collar_band(2, back_offset=0), "crystallized_sheep"),
        collar((64, 32), P_HEAD, head, "crystallized_pig"),
        collar((64, 64), V_JACKET, collar_band(top_rows=2), "crystallized_villager"),
        collar((64, 64), (16, 32, 8, 12, 4), collar_band(top_rows=2), "shard_zombie"),
    ]
    return out


def main():
    sheep_texture()
    pig_texture()
    villager_texture()
    collars()
    print("infected_animals: crystallized_sheep (+Vlies), crystallized_pig, crystallized_villager, 5 Kragen")


if __name__ == "__main__":
    main()
