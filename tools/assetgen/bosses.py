"""Texturen der beiden Bosse: Kristallkoloss (256x256) und Splitterkoenigin (128x128, plus Klon).

Beide haben deutlich mehr Detail als normale Mobs (Zusatz-Quader aus geo_prep.py) und eine Leuchtmaske.
Aufruf: python tools/assetgen/bosses.py
"""
from __future__ import annotations

import math

from clean import ordered
from common import ASH, BONE, COL, CYAN, LILAC, MAGENTA, SHARD, SLATE, VIOLET, hx, mix, nz, ramp, vnoise
from ent import Ent, M_cloth, M_crystal, M_flat, M_fur, M_plate, M_stone, art_face, overlay
from uvlib import FACE_LIGHT, edge_light

G = True
COLO = [mix(SLATE[i], VIOLET[min(i + 1, 4)], 0.38) for i in range(5)]
COLO_DARK = [mix(SLATE[max(i - 1, 0)], VIOLET[min(i, 4)], 0.25) for i in range(5)]
ROBE = [hx(c) for c in ("#190c33", "#2a1556", "#40227f", "#5a35ad", "#7e55d6")]
HAIR = [hx(c) for c in ("#06030c", "#0e0719", "#190d2d", "#2a1a4a", "#432a73")]


def P(**kw):
    return kw


# ====================================================================== Kristallkoloss
def crystal_colossus():
    e = Ent("crystal_colossus")
    stone = M_stone(COLO, 2001, rough=0.55, crack=CYAN[2], crack_glow=True, strata=True, crack_w=0.03)

    def groove(face, fx, fy, fw, fh, rows, dash=4):
        if fy in rows and 1 < fx < fw - 2:
            return (CYAN[2], G) if fx % dash != 0 else COLO[0]
        return None

    def body(face, fx, fy, fw, fh):
        if face == "north":
            r = groove(face, fx, fy, fw, fh, (4, 21))
            if r:
                return r
            if fx in (2, fw - 3) and 4 < fy < 21:
                return COLO[0]
        if face == "south":
            if fx in (14, 15) and 2 < fy < fh - 3 and fy % 4 != 0:
                return (CYAN[3] if fx == 14 else CYAN[1], G)
            r = groove(face, fx, fy, fw, fh, (6, 18), 3)
            if r:
                return r
        if face in ("east", "west"):
            r = groove(face, fx, fy, fw, fh, (7, 17), 3)
            if r:
                return r
        if face == "up" and fx % 6 == 3 and fy % 6 == 3:
            return (CYAN[2], G)
        return None

    e.paint("body", overlay(stone, body))

    head_art = """
pppppppppppp
pppppppppppp
pddddddddddp
pdEEEddEEEdp
pdeeeddeeedp
ppdddddddppp
pppppnnppppp
pppppnnppppp
ppdddddddddp
pdgdgdgdgdgp
ppdddddddddp
pppppppppppp
"""
    e.paint("head", art_face(head_art, P(p=COLO[2], d=COLO[0], E=(CYAN[4], G), e=(CYAN[3], G), n=COLO[1], g=(CYAN[1], G)), stone))

    def arm(face, fx, fy, fw, fh):
        if face in ("north", "south", "east", "west"):
            if fx in (4, 5) and 6 < fy < 26 and fy % 5 != 0:
                return (CYAN[2] if fx == 4 else CYAN[1], G)
            if fy >= 27:
                return ramp(COLO_DARK, 1.2 + FACE_LIGHT[face] * 0.8 + ordered(fx, fy) * 0.54 + (0.9 if fy == 27 else 0))
            if fy in (11, 12):
                return COLO[0]
        return None

    e.paint("right_arm", overlay(stone, arm))
    e.paint("left_arm", overlay(stone, arm))

    def fist(face, fx, fy, fw, fh):
        base = M_plate(COLO_DARK, 2005, bias=1.7)(face, fx, fy, fw, fh)
        if face in ("north", "south", "east", "west") and fy in (3, 7) and 0 < fx < fw - 1:
            return (CYAN[1], G) if fx % 2 == 0 else COLO_DARK[0]
        return base

    e.paint("right_fist", fist)
    e.paint("left_fist", fist)

    def leg(face, fx, fy, fw, fh):
        if face in ("north", "south", "east", "west"):
            if fx in (4, 5) and 3 < fy < 12:
                return (CYAN[2], G)
            if fy >= 19:
                return ramp(COLO_DARK, 1.3 + FACE_LIGHT[face] * 0.8 + ordered(fx, fy) * 0.54 + (0.9 if fy == 19 else 0))
        return None

    e.paint("right_leg", overlay(stone, leg))
    e.paint("left_leg", overlay(stone, leg))

    # Ruecken-Kristalle: grosse in Cyan, kleine in Magenta
    e.paint("crystals", M_crystal(CYAN, 2009, glow_from=1.1), only=[0, 1, 2])
    e.paint("crystals", M_crystal(MAGENTA, 2011, glow_from=1.1), only=[3, 4])

    # ---- Zusatz-Details
    plate = M_plate(COLO_DARK, 2013, rivets=4, bias=1.9)

    def chest_plate(face, fx, fy, fw, fh):
        base = plate(face, fx, fy, fw, fh)
        if face != "north":
            return base
        dx, dy = fx - 11.5, fy - 7.0
        d = abs(dx) * 0.7 + abs(dy)
        if 5.0 <= d < 6.0:
            return (CYAN[2], G)
        if d < 5.0 and (abs(dx) < 0.8 or abs(dy) < 0.8):
            return (CYAN[1], G)
        if fy == 0 or fy == fh - 1:
            return COLO_DARK[4] if fy == 0 else COLO_DARK[0]
        return base

    e.paint("detail_body", chest_plate, only=[0])

    def core(face, fx, fy, fw, fh):
        d = math.hypot(fx - 3.5, fy - 3.5)
        if face == "north":
            if d < 1.6:
                return (CYAN[4], G)
            if d < 3.0:
                return (CYAN[3], G)
            return (CYAN[2], G) if d < 3.9 else COLO_DARK[0]
        return (CYAN[2], G)

    e.paint("detail_body", core, only=[1])
    e.paint("detail_body", overlay(plate, lambda f, x, y, w, h: (CYAN[2], G) if f in ("north", "south") and y == 2 and x % 4 == 1 else None), only=[2])
    e.paint("detail_body", M_crystal(CYAN, 2015, glow_from=1.2), only=[3])
    e.paint("detail_body", plate, only=[4])

    e.paint("detail_head", overlay(plate, lambda f, x, y, w, h: (CYAN[1], G) if f == "north" and w == 14 and y == 1 and x % 3 == 1 else None), only=[0])
    e.paint("detail_head", overlay(plate, lambda f, x, y, w, h: (BONE[4], G) if f == "north" and w == 10 and x % 2 == 1 and y == 1 else None), only=[1])
    e.paint("detail_head", M_crystal(CYAN, 2017, glow_from=1.1), only=[2, 3, 4])

    def pauldron(face, fx, fy, fw, fh):
        base = plate(face, fx, fy, fw, fh)
        if face == "up":
            d = math.hypot(fx - fw / 2.0 + 0.5, (fy - fh / 2.0 + 0.5) * 1.1)
            if 4.0 <= d < 5.0:
                return (CYAN[2], G)
            if d < 2.0:
                return (CYAN[4], G) if d < 1.2 else (CYAN[3], G)
            if 5.0 <= d < 6.0 and (fx + fy) % 2 == 0:
                return (CYAN[0], G)
        return base

    e.paint("detail_right_arm", pauldron, only=[0])
    e.paint("detail_left_arm", pauldron, only=[0])
    band = overlay(plate, lambda f, x, y, w, h: (CYAN[2], G) if f in ("north", "south", "east", "west") and y == 1 and x % 3 != 0 else None)
    e.paint("detail_right_arm", band, only=[1, 2])
    e.paint("detail_left_arm", band, only=[1, 2])
    e.paint("detail_right_fist", M_crystal(CYAN, 2019, glow_from=1.1))
    e.paint("detail_left_fist", M_crystal(CYAN, 2019, glow_from=1.1))
    knee = overlay(plate, lambda f, x, y, w, h: (CYAN[3], G) if f == "north" and 4 <= x <= 7 and 2 <= y <= 4 else None)
    e.paint("detail_right_leg", knee, only=[0])
    e.paint("detail_left_leg", knee, only=[0])
    e.paint("detail_right_leg", plate, only=[1])
    e.paint("detail_left_leg", plate, only=[1])
    e.save()


# ====================================================================== Splitterkoenigin
def queen(variant="queen"):
    ghost = variant == "clone"
    if ghost:
        robe = [hx(c) for c in ("#4a3a7c", "#6a58a6", "#8c7cc8", "#b0a2e8", "#d8ceff")]
        hair = [hx(c) for c in ("#3a2e66", "#554688", "#7060ac", "#8f80d0", "#b4a6f0")]
        skin = [BONE[2], BONE[3], BONE[4], BONE[4]]
        accent = [hx(c) for c in ("#8a5c9c", "#b878c8", "#e0a0f0", "#ffc4f8", "#ffe6fb")]
        wing_base = [hx(c) for c in ("#4a3a7c", "#6a58a6", "#8c7cc8", "#b0a2e8", "#d8ceff")]
    else:
        robe, hair, skin, accent, wing_base = ROBE, HAIR, [BONE[2], BONE[3], BONE[4], BONE[4]], MAGENTA, VIOLET
    e = Ent(variant if ghost else "shard_queen", "shard_queen")
    if ghost:
        e.name = "queen_clone"

    cloth = M_cloth(robe, 3001, bias=1.9)

    def dress(face, fx, fy, fw, fh):
        if fy == fh - 1 and fx % 2 == 0:
            return (accent[3], G)
        if fy == fh - 2:
            return accent[1]
        if face == "north" and fx == fw // 2 and fy % 3 != 2:
            return (accent[2], G) if fy % 2 == 0 else accent[1]
        if face in ("north", "south") and (fx + fy * 2) % 7 == 0:
            return ramp(robe, 4)
        return cloth(face, fx, fy, fw, fh)

    e.paint("skirt_lower", dress)
    e.paint("skirt_upper", overlay(cloth, lambda f, x, y, w, h: accent[1] if y == 0 and f != "up" else (
        (accent[2], G) if f == "north" and y == 4 and x % 3 == 1 else None)))
    e.paint("torso", overlay(cloth, lambda f, x, y, w, h: (accent[2], G) if f == "north" and y in (3, 5, 7) and 1 < x < w - 2 and x % 2 == 1 else (
        accent[1] if f in ("north", "south") and y == 0 else (accent[0] if f in ("north", "south") and y == h - 1 else None))))

    face_art = """
hhhhhhhh
hhhhhhhh
hssssssh
hsEssEsh
hssssssh
hssmmssh
hhsssshh
hhhhhhhh
"""
    hair_mat = M_fur(hair, 3003, bias=1.7)
    e.paint("head", art_face(face_art, P(h=hair[2], s=skin[2], E=(accent[3], G), m=accent[1]), hair_mat))

    # Krone und Heiligenschein leuchten
    e.paint("crown", M_crystal(accent, 3005, glow_from=1.0))
    e.paint("halo", M_flat(accent[4], G))

    sleeve = overlay(cloth, lambda f, x, y, w, h: skin[2] if y >= h - 3 and f != "up" else (accent[2] if y == h - 4 else None))
    e.paint("right_arm", sleeve)
    e.paint("left_arm", sleeve)

    def wing(face, fx, fy, fw, fh):
        if face in ("north", "south"):
            # Federn: diagonale Splitter, nach aussen laenger, Kanten leuchten
            stripe = (fx + fy // 2) % 5
            edge = (fx - fy // 2 + 40) % 5 == 0
            n = ordered(fx, fy) * 0.8 + 0.5
            if stripe == 0:
                return (accent[3], G) if fy % 2 == 0 else (accent[2], G)
            if fx == 0 or fy == 0 or fy == fh - 1:
                return ramp(wing_base, 3.2)
            return ramp(wing_base, 1.0 + (stripe * 0.35) + (n - 0.5) * 0.7 + (0.6 if edge else 0))
        return ramp(wing_base, 2.0)

    e.paint("right_wing", wing)
    e.paint("left_wing", wing)

    # ---- Zusatz-Details
    e.paint("detail_head", M_fur(hair, 3009, bias=1.5), only=[0, 1, 2])
    e.paint("detail_head", M_crystal(accent, 3011, glow_from=1.0), only=[3, 4])
    e.paint("detail_torso", M_crystal(accent, 3013, glow_from=1.0), only=[0, 1])
    e.paint("detail_torso", M_flat(accent[3], G), only=[2])
    e.paint("detail_torso", overlay(cloth, lambda f, x, y, w, h: (accent[2], G) if f in ("north", "south") and y == 0 else None), only=[3])
    e.paint("detail_skirt", M_crystal(accent, 3015, glow_from=1.0))
    feather = M_crystal(wing_base if not ghost else accent, 3017, glow_from=2.8)
    e.paint("detail_right_wing", feather)
    e.paint("detail_left_wing", feather)
    e.save()


def main():
    crystal_colossus()
    queen("queen")
    queen("clone")
    print("bosse: crystal_colossus, shard_queen, queen_clone")


if __name__ == "__main__":
    main()
