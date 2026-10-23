"""Entity-Texturen aller Mobs und Bosse. Aufruf: python tools/assetgen/entities.py

Malt direkt in das UV-Layout der Modelle (geo/entity/*.geo.json) und erzeugt zusaetzlich <name>_glowmask.png
(deckend = leuchtet im Dunkeln). Mobs ohne Leuchtmaske (ash_walker, shard_trader) bekommen keine.
"""
from __future__ import annotations

import math

from fresh_blocks import SHARDSTONE
from clean import ordered
from common import (ASH, BONE, COL, CYAN, LILAC, MAGENTA, SHARD, SLATE, TEAL, VIOLET, Tex, clamp, hx, mix, nz,
                    ramp, vnoise)
from ent import (Ent, M_chitin, M_cloth, M_crystal, M_flat, M_fur, M_membrane, M_plate, M_stone, art_face, overlay)
from uvlib import FACE_LIGHT, edge_light

G = True  # leuchtet
GOLEM_STONE = [mix(SLATE[i], CYAN[0], 0.22) for i in range(5)]
FOX = [hx(c) for c in ("#07050d", "#0e0a1a", "#181230", "#271d49", "#3c2d70")]
LEATHER = [hx(c) for c in ("#140f1f", "#211833", "#33264d", "#4b3a70", "#6a5599")]
ROBE = [hx(c) for c in ("#190c33", "#2a1556", "#40227f", "#5a35ad", "#7e55d6")]
HAIR = [hx(c) for c in ("#06030c", "#0e0719", "#190d2d", "#2a1a4a", "#432a73")]
CHITIN = [hx(c) for c in ("#0a0714", "#140d26", "#201640", "#322363", "#4a3590")]
MEMB = [hx(c) for c in ("#0b0820", "#150f38", "#221a5a", "#352a86", "#4f41b8")]


def P(**kw):
    """Palette-Helfer: Werte sind Farben oder (Farbe, leuchtet)."""
    return kw


# ====================================================================== Schard-Kriecher
def shard_creeper():
    e = Ent("shard_creeper")
    stone = M_stone(SHARDSTONE, 101, rough=0.45, crack=MAGENTA[2], crack_glow=True)
    face = art_face("""
........
........
.EE..EE.
.ee..ee.
...dd...
..dddd..
..d..d..
........
""", P(E=(CYAN[3], G), e=(CYAN[2], G), d=SLATE[0]), stone)
    e.paint("head", face)
    e.paint("body", art_face("""
........
........
...mm...
..mmmm..
...mm...
""", P(m=(MAGENTA[2], G)), stone))
    e.paint("crystals", M_crystal(SHARD, 103, glow_from=1.6))
    e.paint("leg_front_left", M_stone(SHARDSTONE, 105, rough=0.4, bias=1.4))
    e.paint("leg_front_right", M_stone(SHARDSTONE, 105, rough=0.4, bias=1.4))
    e.paint("leg_back_left", M_stone(SHARDSTONE, 105, rough=0.4, bias=1.4))
    e.paint("leg_back_right", M_stone(SHARDSTONE, 105, rough=0.4, bias=1.4))
    e.save()


# ====================================================================== Kristallgolem
def crystal_golem(angry=False):
    e = Ent("crystal_golem")
    glow = MAGENTA if angry else CYAN
    base = [mix(GOLEM_STONE[i], VIOLET[min(i + 1, 4)], 0.35) for i in range(5)] if angry else GOLEM_STONE
    stone = M_stone(base, 201, rough=0.5, crack=glow[2], crack_glow=True, strata=True)

    def chest(face, fx, fy, fw, fh):
        if face != "north":
            return None
        cx, cy = 8.5, 7.0
        d = math.hypot(fx - cx, (fy - cy) * 1.0)
        if d < 2.2:
            return (ramp(glow, 4.2 - d), True)
        if d < 3.6:
            return (ramp(glow, 1.8 if (fx + fy) % 2 else 2.6), True)
        if 4.6 < d < 5.4 and (fx + fy) % 2 == 0:
            return (glow[1], True)
        return None

    e.paint("body", overlay(stone, chest))
    eyes = """
........
.ddddd..
.EEddEE.
.eeddee.
..dddd..
..d..d..
........
........
........
"""
    if angry:
        eyes = """
........
.dddddd.
dEEddEEd
.eeddee.
..dddd..
.dsssd..
..d..d..
........
........
"""
    e.paint("head", art_face(eyes, P(E=(glow[3], G), e=(glow[2], G), d=base[0], s=base[1]), stone))
    arm = overlay(stone, lambda f, x, y, w, h: (glow[2], G) if y in (11, 12) and 0 < x < w - 1 and f in ("north", "south") else
                  (ramp(base, 0.8) if y >= 18 else None))
    e.paint("right_arm", arm)
    e.paint("left_arm", arm)
    leg = overlay(stone, lambda f, x, y, w, h: (glow[2], G) if y == 5 and f == "north" and 1 < x < w - 2 else
                  (ramp(base, 0.8) if y >= 13 else None))
    e.paint("right_leg", leg)
    e.paint("left_leg", leg)
    e.paint("crystals", M_crystal(glow, 207, glow_from=1.4))
    if angry:
        e.name = "crystal_golem_angry"
    e.save()


# ====================================================================== Leuchtmotte
def glow_moth():
    e = Ent("glow_moth")
    fur = M_fur(ROBE, 301, bias=1.8)
    e.paint("body", overlay(fur, lambda f, x, y, w, h: (CYAN[2], G) if y == 1 and f in ("north", "south", "east", "west") and 0 < x else None))
    e.paint("head", art_face({"north": """
E.E
...
"""}, P(E=(CYAN[4], G)), M_fur(ROBE, 303, bias=2.1)), only=[0])
    e.paint("head", M_crystal(CYAN, 305, glow_from=1.0), only=[1, 2])

    def wing(mirror):
        def p(face, fx, fy, fw, fh):
            ax = (fw - 1 - fx) if mirror else fx  # Abstand zum Koerper = ax
            n = ordered(fx, fy) * 0.8 + 0.5
            if face == "up":
                # Flugelrand leuchtet, Adern faecherfoermig, Augenfleck
                if ax >= fw - 1 or fy == 0 or fy == fh - 1:
                    return (MAGENTA[2], G) if (fx + fy) % 2 == 0 else MAGENTA[0]
                if abs(ax - 4) + abs(fy - 2) <= 1:
                    return (CYAN[4] if abs(ax - 4) + abs(fy - 2) == 0 else CYAN[3], G)
                if abs(ax - 4) + abs(fy - 2) == 2:
                    return (CYAN[1], G)
                if (ax * 2 - fy * 3) % 7 == 0:
                    return VIOLET[4]
                return ramp(VIOLET, 1.2 + (ax / fw) * 1.4 + (n - 0.5) * 0.7)
            return ramp(VIOLET, 0.8 + (n - 0.5) * 0.8)
        return p

    e.paint("left_wing", wing(False))
    e.paint("right_wing", wing(True))
    e.save()


# ====================================================================== Aschewandler
def ash_walker():
    e = Ent("ash_walker")
    stone = M_stone(ASH, 401, rough=0.5, crack=MAGENTA[1], crack_glow=False)
    e.paint("head", art_face("""
........
.dddddd.
.dEddEd.
.dddddd.
..dnnd..
.dtdtdd.
.dddddd.
........
""", P(E=MAGENTA[3], d=ASH[1], n=ASH[0], t=ASH[4]), stone))
    ribs = overlay(stone, lambda f, x, y, w, h: ASH[0] if f in ("north", "south") and y in (3, 6, 9) and 0 < x < w - 1 else (
        MAGENTA[1] if f == "north" and (x, y) in ((3, 4), (4, 7), (2, 10)) else None))
    e.paint("body", ribs)
    limb = overlay(stone, lambda f, x, y, w, h: ASH[0] if y >= 9 else None)
    for b in ("right_arm", "left_arm", "right_leg", "left_leg"):
        e.paint(b, limb)
    e.save(with_glow=False)


# ====================================================================== Shard-Spaeher
def shard_scout():
    e = Ent("shard_scout")
    e.paint("core", M_crystal(SHARD, 501, glow_from=1.8), only=[0])
    e.paint("core", M_crystal(VIOLET, 503, glow_from=1.5), only=[1, 2])
    e.paint("head", art_face("""
.E.
EPE
.E.
""", P(E=(CYAN[3], G), P=(MAGENTA[3], G)), M_flat(CYAN[4], G)))
    e.paint("orbit", M_crystal(CYAN, 507, glow_from=1.4))
    e.save()


# ====================================================================== Void-Rochen
def void_ray():
    e = Ent("void_ray")

    def dorsal(face, fx, fy, fw, fh):
        n = ordered(fx, fy) * 0.8 + 0.5
        if face == "up":
            if fx in (fw // 2, fw // 2 - 1):
                return ramp(MEMB, 3.0 - (fy % 4) * 0.35)
            if (fx % 4 == 1 and fy % 5 == 2) or (fx % 4 == 3 and fy % 5 == 0):
                return (CYAN[2], G)
            return ramp(MEMB, 1.1 + (vnoise(fx / 3.5, fy / 3.5, 603) - 0.5) * 1.0)
        if face == "down":
            return ramp(LILAC[:2] + [LILAC[2]], 0.2 + n * 1.5) if False else ramp(
                [VIOLET[2], VIOLET[3], LILAC[0], LILAC[1]], 0.8 + (vnoise(fx / 3.5, fy / 3.0, 605) - 0.5) * 1.0 + n * 0.5)
        return ramp(MEMB, 0.8 + n)

    e.paint("body", dorsal)

    def wing(face, fx, fy, fw, fh):
        n = ordered(fx, fy) * 0.8 + 0.5
        if face == "up":
            if fy == fh - 1 or fx == fw - 1:
                return (CYAN[2] if (fx + fy) % 3 == 0 else CYAN[0], (fx + fy) % 3 == 0)
            if (fx * 3 - fy) % 8 == 0 or (fx - fy * 2) % 11 == 0:
                return ramp(MEMB, 4)
            return ramp(MEMB, 1.0 + (vnoise(fx / 3.5, fy / 3.5, 613) - 0.5) * 0.9)
        if face == "down":
            return ramp([VIOLET[1], VIOLET[2], VIOLET[3], LILAC[0]], 1.2 + (n - 0.5) * 1.2)
        return ramp(MEMB, 1.5)

    for b in ("left_wing", "left_wing_tip", "right_wing", "right_wing_tip"):
        e.paint(b, wing)
    e.paint("head", overlay(M_plate(MEMB, 615, bias=1.4), lambda f, x, y, w, h: (CYAN[3], G) if f in ("up", "north") and ((x, y) in ((0, 0), (5, 0), (0, 1), (5, 1)) if w == 6 else False) else None))
    e.paint("saddle", M_plate(LEATHER, 617, rivets=3, bias=2.0))
    e.paint("tail", lambda f, x, y, w, h: ramp(MEMB, 1.8 + (x + y) % 3 * 0.5))
    e.save()


# ====================================================================== Kristallspinne
def crystal_spider():
    e = Ent("crystal_spider")
    shell = M_chitin(CHITIN, 701, bias=1.7)
    e.paint("neck", shell)
    e.paint("head", art_face("""
........
.E.EE.E.
..E..E..
........
.dddddd.
.dfddf d
.d....d.
........
""".replace(" ", "."), P(E=(CYAN[3], G), d=CHITIN[0], f=BONE[4]), shell))

    def abdomen(face, fx, fy, fw, fh):
        base = shell(face, fx, fy, fw, fh)
        if face == "up" and fx in (2, 5, 8) and 1 < fy < fh - 1:
            return (CYAN[2], G)
        if face in ("east", "west", "north", "south") and fy == 3 and 0 < fx < fw - 1:
            return (CYAN[1], G)
        return base

    e.paint("abdomen", abdomen)
    e.paint("crystals", M_crystal(CYAN, 703, glow_from=1.2))

    def leg(face, fx, fy, fw, fh):
        seg = fx // 7
        if fx % 7 == 6 and face in ("north", "south", "up", "down"):
            return (CYAN[1], G)
        return ramp(CHITIN, 1.5 + FACE_LIGHT[face] * 0.8 + (fx % 7) * 0.12 + ordered(fx, fy) * 0.36)

    for b in ("leg_left_1", "leg_left_2", "leg_left_3", "leg_left_4", "leg_right_1", "leg_right_2", "leg_right_3", "leg_right_4"):
        e.paint(b, leg)
    e.save()


# ====================================================================== Splitterhaendler
def shard_trader():
    e = Ent("shard_trader")
    cloth = M_cloth(ROBE, 801)
    skin = BONE[3]
    e.paint("head", art_face("""
rrrrrrrr
rrrrrrrr
rkkkkkkr
rsEssEsr
rssssssr
rsssssbr
rssmmssr
rsssssbr
rrssssrr
rrrrrrrr
""", P(r=ROBE[1], k=BONE[1], s=skin, E=MAGENTA[3], m=BONE[1], b=BONE[2]), cloth), only=[0])
    e.paint("head", M_flat(BONE[2]), only=[1])
    e.paint("body", overlay(cloth, lambda f, x, y, w, h: MAGENTA[2] if f == "north" and y == 6 and 0 < x < w - 1 else (
        MAGENTA[1] if f == "north" and y == 7 and 0 < x < w - 1 else None)), only=[0])
    e.paint("body", overlay(M_cloth(ROBE, 803, bias=1.7), lambda f, x, y, w, h: MAGENTA[1] if y == h - 1 else None), only=[1])
    e.paint("arms", overlay(cloth, lambda f, x, y, w, h: BONE[3] if f in ("north", "south", "east", "west") and w == 4 and y >= 6 else None))
    e.paint("backpack", overlay(M_plate(LEATHER, 805, bias=1.8), lambda f, x, y, w, h: LEATHER[4] if f in ("north", "south") and x in (1, w - 2) else (
        MAGENTA[2] if f == "south" and y == 4 and x in (2, 3) else None)))
    e.paint("pack_crystals", M_crystal(MAGENTA, 807, glow_from=9))
    leg = overlay(M_cloth(ROBE, 809, bias=1.4), lambda f, x, y, w, h: LEATHER[2] if y >= 9 else None)
    e.paint("right_leg", leg)
    e.paint("left_leg", leg)
    e.save(with_glow=False)


# ====================================================================== Schattenfuchs
def shadow_fox():
    e = Ent("shadow_fox")
    fur = M_fur(FOX, 901, bias=1.7)

    def body(face, fx, fy, fw, fh):
        if face == "up" and fx in (1, 4) and 1 < fy < fh - 1 and fy % 3 != 0:
            return (VIOLET[3], G)
        if face in ("east", "west") and fy == 1 and fx % 2 == 0 and fx > 0:
            return (VIOLET[2], G)
        return fur(face, fx, fy, fw, fh)

    e.paint("body", body)
    e.paint("head", art_face("""
........
.E....E.
.EE..EE.
........
...dd...
""", P(E=(MAGENTA[3], G), d=FOX[0]), fur), only=[0])
    e.paint("head", overlay(M_fur(FOX, 903, bias=1.2), lambda f, x, y, w, h: FOX[0] if f == "north" and y == 0 and 0 < x < w - 1 else None), only=[1])
    e.paint("head", overlay(fur, lambda f, x, y, w, h: MAGENTA[1] if f == "north" and w == 2 and h == 2 else None), only=[2, 3])
    e.paint("tail", overlay(fur, lambda f, x, y, w, h: (VIOLET[3], G) if f == "south" and 0 < x < w - 1 and 0 < y < h - 1 else (
        (VIOLET[2], G) if f in ("up", "down") and y >= 6 and x in (1, 2) else None)))
    paw = overlay(M_fur(FOX, 905, bias=1.0), lambda f, x, y, w, h: FOX[4] if y == h - 1 and f != "down" else None)
    for b in ("leg_front_left", "leg_front_right", "leg_back_left", "leg_back_right"):
        e.paint(b, paw)
    e.save()


def fx_ok(x, w):
    return x in (1, 2) and w == 4


# ====================================================================== Gesplitterter Ritter
def shattered_knight():
    e = Ent("shattered_knight")
    plate = M_plate(SLATE, 1001, rivets=4, bias=2.0)

    def helmet(face, fx, fy, fw, fh):
        return plate(face, fx, fy, fw, fh)

    e.paint("head", art_face("""
........
........
.vvvvvv.
.EEEEEE.
.vvvvvv.
..vvvv..
..v..v..
........
""", P(v=SLATE[0], E=(MAGENTA[3], G)), plate), only=[0])
    e.paint("head", M_crystal(SHARD, 1003, glow_from=1.8), only=[1])

    def torso(face, fx, fy, fw, fh):
        if face == "north":
            # gesprungener Kristallkern in der Brust
            core = {(4, 4), (5, 4), (4, 5), (5, 5), (4, 3), (5, 3), (3, 4), (6, 4), (4, 6), (5, 6)}
            if (fx, fy) in core:
                return (MAGENTA[3] if (fx, fy) in ((4, 4), (5, 4), (4, 5), (5, 5)) else MAGENTA[2], G)
            if (fx, fy) in ((3, 2), (2, 1), (6, 7), (7, 8), (3, 6), (2, 7), (6, 2)):
                return (VIOLET[3], G)
            if fy == 10 or fy == 11:
                return ramp(SLATE, 0.8 + (fy - 10) * 0.8)
        return plate(face, fx, fy, fw, fh)

    e.paint("body", torso)
    e.paint("shoulder_crystals", M_crystal(SHARD, 1005, glow_from=1.6))
    arm = overlay(plate, lambda f, x, y, w, h: SLATE[0] if y in (8, 9) and 0 < x < w - 1 else (SLATE[4] if y == 0 else None))
    e.paint("right_arm", arm)
    e.paint("left_arm", arm)
    e.paint("sword", M_crystal(VIOLET if False else SHARD, 1007, glow_from=1.5), only=[1])
    e.paint("sword", M_plate(SLATE, 1009, bias=2.4), only=[0])
    leg = overlay(plate, lambda f, x, y, w, h: SLATE[4] if f == "north" and y == 7 and 0 < x < w - 1 else (
        SLATE[0] if y in (8, 9) and f != "up" else None))
    e.paint("right_leg", leg)
    e.paint("left_leg", leg)
    e.save()


# ====================================================================== Kristallsaeule (Boss-Fertigkeit)
def crystal_pillar():
    e = Ent("crystal_pillar")
    e.paint("base", M_stone(GOLEM_STONE, 1101, rough=0.5, crack=CYAN[2], crack_glow=True))
    e.paint("crystals", M_crystal(CYAN, 1103, glow_from=1.2))
    e.save()


ALL = [shard_creeper, lambda: crystal_golem(False), lambda: crystal_golem(True), glow_moth, ash_walker, shard_scout,
       void_ray, crystal_spider, shard_trader, shadow_fox, shattered_knight, crystal_pillar]


def main():
    for fn in ALL:
        fn()
    print(f"entities (Mobs): {len(ALL)} Texturen")


if __name__ == "__main__":
    main()
