"""Textur des Kristallkolosses (256x256). Die Splitterkoenigin samt Klon erzeugt queen_rig.py.

Beide haben deutlich mehr Detail als normale Mobs (Zusatz-Quader aus geo_prep.py) und eine Leuchtmaske.
Aufruf: python tools/assetgen/bosses.py
"""
from __future__ import annotations

import math

from clean import ordered
from common import BONE, CYAN, MAGENTA, SLATE, VIOLET, hx, mix, ramp
from ent import Ent, M_crystal, M_plate, M_stone, art_face, overlay
from uvlib import FACE_LIGHT

G = True
COLO = [mix(SLATE[i], VIOLET[min(i + 1, 4)], 0.38) for i in range(5)]
COLO_DARK = [mix(SLATE[max(i - 1, 0)], VIOLET[min(i, 4)], 0.25) for i in range(5)]


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


def main():
    crystal_colossus()
    print("bosse: crystal_colossus (Splitterkoenigin: queen_rig.py)")


if __name__ == "__main__":
    main()
