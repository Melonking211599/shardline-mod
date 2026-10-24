"""NPCs von "Nach dem Ausbruch": Modelle (geo), Animationen, Texturen mit Glowmaske und Porträts.

Aufruf: python tools/assetgen/npc_assets.py
Schreibt pro NPC geo/entity/<npc>.geo.json, animations/entity/<npc>.animation.json, textures/entity/<npc>.png und
<npc>_glowmask.png sowie textures/gui/portraits/<npc>_<pose>.png (64x64; Posen neutral, happy, sad, angry).

Alle NPCs teilen ein Menschenmodell (64x64, Arme und Beine links gespiegelt, Kopf-Zweitlage mit 0.4 Aufblaehung fuer Haar
oder Kapuze); dazu kommt Zubehoer je Figur. Animationen: idle, walk, talk, hurt, sleep (liegt), death (haelt den letzten
Frame). Leuchten nur an Kristallen und Anhaengern (STYLEBIBEL.md).
"""
from __future__ import annotations

import json
import math

from common import ANIM, CYAN, GEO, GOLD, IVORY, LEATHER, MAGENTA, OUTLINE, SKYB, SLATE, VGLOW, VIOLET, Tex, hx, mix, nz, ramp, save_png, vnoise
from ent import Ent, art_face, overlay
from uvlib import FACE_LIGHT, edge_light

G = True

# ------------------------------------------------------------------ Paletten
SKIN_WARM = [hx(c) for c in ("#5a3a2c", "#8a5a43", "#b47f62", "#d9a585", "#f0c9a8")]
HAIR_AUBURN = [hx(c) for c in ("#2c120c", "#4d1f14", "#74321e", "#9a4a2a", "#c06a3c")]
COAT_STEEL = [hx(c) for c in ("#151c28", "#24324a", "#36506e", "#506f92", "#7393b4")]
TROUSERS = [hx(c) for c in ("#17141c", "#25212c", "#36303f", "#4a4256", "#625a70")]
EYES_GREEN = hx("#3f7a62")


# ------------------------------------------------------------------ Modell
def humanoid_bones():
    """Menschenmodell im Vanilla-Raster (Kopf 8, Koerper 8x12x4, Arme/Beine 4x12x4)."""
    return [
        {"name": "root", "pivot": [0, 0, 0]},
        {"name": "body", "parent": "root", "pivot": [0, 24, 0], "cubes": [
            {"origin": [-4, 12, -2], "size": [8, 12, 4], "uv": [16, 16]}]},
        {"name": "head", "parent": "body", "pivot": [0, 24, 0], "cubes": [
            {"origin": [-4, 24, -4], "size": [8, 8, 8], "uv": [0, 0]},
            {"origin": [-4, 24, -4], "size": [8, 8, 8], "uv": [32, 0], "inflate": 0.4}]},
        {"name": "right_arm", "parent": "body", "pivot": [-5, 22, 0], "cubes": [
            {"origin": [-8, 12, -2], "size": [4, 12, 4], "uv": [40, 16]}]},
        {"name": "left_arm", "parent": "body", "pivot": [5, 22, 0], "cubes": [
            {"origin": [4, 12, -2], "size": [4, 12, 4], "uv": [40, 16], "mirror": True}]},
        {"name": "right_leg", "parent": "root", "pivot": [-1.9, 12, 0], "cubes": [
            {"origin": [-3.9, 0, -2], "size": [4, 12, 4], "uv": [0, 16]}]},
        {"name": "left_leg", "parent": "root", "pivot": [1.9, 12, 0], "cubes": [
            {"origin": [-0.1, 0, -2], "size": [4, 12, 4], "uv": [0, 16], "mirror": True}]},
    ]


EXTRAS = {
    "rhea": [
        {"name": "ponytail", "parent": "head", "pivot": [0, 30, 4], "cubes": [
            {"origin": [-1, 23, 4], "size": [2, 6, 2], "uv": [24, 32]}]},
        {"name": "cloak", "parent": "body", "pivot": [0, 24, 2], "cubes": [
            {"origin": [-4.5, 7, 2], "size": [9, 17, 1], "uv": [0, 32]}]},
        {"name": "pauldron", "parent": "right_arm", "pivot": [-6, 23, 0], "cubes": [
            {"origin": [-8.5, 21, -2.5], "size": [5, 3, 5], "uv": [32, 32]}]},
        {"name": "sheath", "parent": "body", "pivot": [4.5, 13, 0], "cubes": [
            {"origin": [4, 5, -1], "size": [1, 8, 2], "uv": [24, 40]}]},
        {"name": "pendant", "parent": "body", "pivot": [0, 20, -2], "cubes": [
            {"origin": [-1, 19, -2.5], "size": [2, 2, 1], "uv": [56, 32]}]},
    ],
}


def geo(npc: str):
    data = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": f"geometry.{npc}", "texture_width": 64, "texture_height": 64,
                        "visible_bounds_width": 2, "visible_bounds_height": 3, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": humanoid_bones() + EXTRAS.get(npc, [])}]}
    (GEO / f"{npc}.geo.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


# ------------------------------------------------------------------ Animationen
def anims(npc: str, floating: bool = False):
    """Gemeinsame Menschen-Animationen. Ein schwebender NPC (Geist) bewegt die Beine kaum und schwebt auf und ab."""
    t = "query.anim_time"
    bob = {"position": [0, f"math.sin({t} * 90) * 0.6", 0]} if floating else None
    extra_idle = {}
    if npc == "rhea":
        extra_idle = {"ponytail": {"rotation": [f"6 + math.sin({t} * 70) * 4", 0, 0]},
                      "cloak": {"rotation": [f"4 + math.sin({t} * 60) * 1.5", 0, 0]}}
    walk_legs = 6 if floating else 32
    data = {"format_version": "1.8.0", "animations": {
        "idle": {"loop": True, "animation_length": 4, "bones": {
            **({"root": bob} if bob else {}),
            "body": {"scale": [1, f"1 + math.sin({t} * 90) * 0.01", 1]},
            "head": {"rotation": [f"math.sin({t} * 45) * 2", f"math.sin({t} * 30) * 4", 0]},
            "right_arm": {"rotation": [0, 0, f"3 + math.sin({t} * 90) * 2"]},
            "left_arm": {"rotation": [0, 0, f"-3 - math.sin({t} * 90) * 2"]},
            **extra_idle}},
        "walk": {"loop": True, "animation_length": 1, "bones": {
            **({"root": bob} if bob else {}),
            "right_leg": {"rotation": [f"math.sin({t} * 360) * {walk_legs}", 0, 0]},
            "left_leg": {"rotation": [f"math.sin({t} * 360) * -{walk_legs}", 0, 0]},
            "right_arm": {"rotation": [f"math.sin({t} * 360) * -28", 0, 3]},
            "left_arm": {"rotation": [f"math.sin({t} * 360) * 28", 0, -3]},
            "body": {"rotation": [0, f"math.sin({t} * 360) * 3", 0]},
            **({"ponytail": {"rotation": [f"14 + math.sin({t} * 720) * 8", 0, 0]},
                "cloak": {"rotation": [f"12 + math.sin({t} * 720) * 4", 0, 0]}} if npc == "rhea" else {})}},
        "talk": {"loop": True, "animation_length": 2, "bones": {
            **({"root": bob} if bob else {}),
            "head": {"rotation": [f"math.sin({t} * 360) * 4", 0, f"math.sin({t} * 180) * 2"]},
            "right_arm": {"rotation": [f"-25 + math.sin({t} * 180) * 12", f"-10 + math.sin({t} * 180) * 6", 6]},
            "left_arm": {"rotation": [0, 0, -4]},
            "body": {"rotation": [f"math.sin({t} * 180) * 1.5", 0, 0]}}},
        "hurt": {"loop": False, "animation_length": 0.4, "bones": {
            "body": {"rotation": {"0.0": [0, 0, 0], "0.1": [-10, 0, 4], "0.4": [0, 0, 0]}},
            "head": {"rotation": {"0.0": [0, 0, 0], "0.1": [-14, 0, 0], "0.4": [0, 0, 0]}},
            "right_arm": {"rotation": {"0.0": [0, 0, 0], "0.1": [-40, 0, 20], "0.4": [0, 0, 0]}},
            "left_arm": {"rotation": {"0.0": [0, 0, 0], "0.1": [-40, 0, -20], "0.4": [0, 0, 0]}}}},
        "sleep": {"loop": True, "animation_length": 4, "bones": {
            "root": {"rotation": [-90, 0, 0], "position": [0, 2, 12]},
            "head": {"rotation": [-10, 15, 0]},
            "body": {"scale": [1, f"1 + math.sin({t} * 90) * 0.02", 1]},
            "right_arm": {"rotation": [0, 0, 8]},
            "left_arm": {"rotation": [0, 0, -8]}}},
        "death": {"loop": "hold_on_last_frame", "animation_length": 1.2, "bones": {
            "root": {"rotation": {"0.0": [0, 0, 0], "0.6": [0, 0, 6], "1.2": [0, 0, 90]},
                     "position": {"0.0": [0, 0, 0], "1.2": [6, 2, 0]}},
            "head": {"rotation": {"0.0": [0, 0, 0], "1.2": [20, 0, 0]}},
            "right_arm": {"rotation": {"0.0": [0, 0, 0], "0.6": [-60, 0, 10], "1.2": [-10, 0, 40]}},
            "left_arm": {"rotation": {"0.0": [0, 0, 0], "0.6": [-60, 0, -10], "1.2": [-10, 0, -20]}}}},
    }}
    (ANIM / f"{npc}.animation.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


# ------------------------------------------------------------------ Materialien
def M_smooth(r, bias=2.6, edge=0.35):
    """Glatte Flaeche (Haut, Leder): Licht oben links, Kanten leicht abgesetzt, keine Flecken."""
    def p(face, fx, fy, fw, fh):
        return ramp(r, bias + FACE_LIGHT[face] * 0.8 + edge_light(fx, fy, fw, fh) * edge)
    return p


def M_fabric(r, seed, bias=2.2, folds=True):
    """Stoff: ruhiger Grundton, senkrechte Falten als 1-px-Linien an festen, unregelmaessigen Stellen."""
    def p(face, fx, fy, fw, fh):
        idx = bias + FACE_LIGHT[face] * 0.9 + edge_light(fx, fy, fw, fh) * 0.5
        if folds and face in ("north", "south", "east", "west") and fw >= 4 and nz(fx, FACE_LIGHT[face] > 0, seed) < 0.18:
            idx -= 0.8
        return ramp(r, idx + (vnoise(fx / 3.0, fy / 4.0, seed) - 0.5) * 0.5)
    return p


def M_hair(r, seed, bias=2.3):
    """Haar: senkrechte Straehnen (1 px breit, wechselnde Helligkeit), Licht oben."""
    def p(face, fx, fy, fw, fh):
        strand = (nz(fx, 0, seed) - 0.5) * 1.1
        return ramp(r, bias + FACE_LIGHT[face] * 0.9 + strand - (fy / max(1, fh)) * 0.6)
    return p


# ------------------------------------------------------------------ Rhea
RHEA_FACE = """
hhhhhhhh
hHhhhhhh
hbbssbbh
hwissiwh
hssnnssh
hsssssss
hssmmssh
.ssssss.
"""


def rhea_texture():
    e = Ent("rhea")
    skin = M_smooth(SKIN_WARM, bias=2.7)
    hair = M_hair(HAIR_AUBURN, 5101)
    face_pal = {"h": HAIR_AUBURN[2], "H": HAIR_AUBURN[4], "b": HAIR_AUBURN[1], "s": SKIN_WARM[3], "w": hx("#f2ece4"),
                "i": EYES_GREEN, "n": SKIN_WARM[2], "m": hx("#9c4a3e")}

    def head(face, fx, fy, fw, fh):
        if face in ("up", "south"):
            return hair(face, fx, fy, fw, fh)
        if face in ("east", "west"):
            if fy <= 4 or (face == "east" and fx >= 5) or (face == "west" and fx <= 2):
                return hair(face, fx, fy, fw, fh)
            return skin(face, fx, fy, fw, fh)
        if face == "down":
            return SKIN_WARM[1]
        return skin(face, fx, fy, fw, fh)

    e.paint("head", art_face(RHEA_FACE, face_pal, head), only=[0])

    def hair_layer(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 0 or (fy == 1 and fx in (0, 1, 6, 7)) or (fy <= 3 and fx in (0, 7)):
                return hair(face, fx, fy, fw, fh)
            return None
        if face in ("east", "west"):
            return hair(face, fx, fy, fw, fh) if fy <= 3 or (face == "east" and fx >= 5) or (face == "west" and fx <= 2) else None
        if face == "down":
            return None
        return hair(face, fx, fy, fw, fh)

    e.paint("head", hair_layer, only=[1])
    e.paint("ponytail", M_hair(HAIR_AUBURN, 5103, bias=2.0))

    coat = M_fabric(COAT_STEEL, 5105, bias=2.3)

    def body(face, fx, fy, fw, fh):
        if fy in (8, 9) and face != "up":
            return M_smooth(LEATHER, bias=2.0)(face, fx, fy, fw, fh)          # Guertel
        if face == "north":
            if fy == 8 and fx == 3:
                return GOLD[3]                                               # Schnalle
            if fy <= 1:
                return ramp(COAT_STEEL, 3.3)                                 # Kragen
            if fx in (3, 4) and fy >= 2 and fy != 8:
                return COAT_STEEL[1]                                         # Knopfleiste
            if (fx, fy) == (6, 3):
                return GOLD[4]                                               # Hauptmannsabzeichen
            if (fx, fy) in ((5, 3), (6, 4), (7, 3)):
                return GOLD[2]
        return coat(face, fx, fy, fw, fh)

    e.paint("body", body)

    def arm(face, fx, fy, fw, fh):
        if fy >= 9 and face != "up":
            return M_smooth(LEATHER, bias=2.2)(face, fx, fy, fw, fh)          # Armschienen
        if fy == 8 and face != "up":
            return LEATHER[1]
        if face == "down":
            return SKIN_WARM[2]
        return coat(face, fx, fy, fw, fh)

    e.paint("right_arm", arm)
    e.paint("left_arm", arm)
    trousers = M_fabric(TROUSERS, 5107, bias=2.2)

    def leg(face, fx, fy, fw, fh):
        if fy >= 8 and face != "up":
            idx = 2.0 + FACE_LIGHT[face] * 0.8 + (0.6 if fy == 8 else 0)
            return ramp(LEATHER, idx)                                        # Stiefel mit hellem Schaftrand
        return trousers(face, fx, fy, fw, fh)

    e.paint("right_leg", leg)
    e.paint("left_leg", leg)

    def cloak(face, fx, fy, fw, fh):
        if face in ("north", "south") and fy == fh - 1 and nz(fx, 1, 5109) < 0.4:
            return None                                                      # ausgefranster Saum
        return M_fabric([hx(c) for c in ("#10151d", "#1b2430", "#283646", "#38495c", "#4c6075")], 5111, bias=2.1)(face, fx, fy, fw, fh)

    e.paint("cloak", cloak)

    def pauldron(face, fx, fy, fw, fh):
        if fx == 0 or fy == 0 or fx == fw - 1:
            return GOLD[2] if face == "up" else GOLD[1]
        return ramp(SLATE, 2.4 + FACE_LIGHT[face] * 1.0 + edge_light(fx, fy, fw, fh) * 0.6)

    e.paint("pauldron", pauldron)
    e.paint("sheath", lambda f, x, y, w, h: GOLD[2] if y >= h - 2 else ramp(LEATHER, 1.6 + FACE_LIGHT[f] * 0.8))
    e.paint("pendant", lambda f, x, y, w, h: (SKYB[4] if (x + y) == 0 else SKYB[2] if f == "north" else SKYB[1], True))
    e.save()


# ------------------------------------------------------------------ Porträts (64x64)
POSES = ("neutral", "happy", "sad", "angry")


def _fill_ellipse(t: Tex, cx, cy, rx, ry, color_fn):
    for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
        for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
            if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0:
                c = color_fn(x, y)
                if c is not None:
                    t.set(x, y, c)


def portrait(spec: dict, pose: str) -> Tex:
    """Brustbild 64x64: Hintergrund in der Farbwelt der Figur, Schultern, Hals, Kopf mit Haar, Gesicht mit Stimmung.
    Licht oben links. Gesichtsform und Augen sind fuer alle NPCs gleich gebaut, damit die Serie einheitlich wirkt."""
    t = Tex(64, 64)
    bg = spec["background"]
    for y in range(64):
        for x in range(64):
            t.set(x, y, bg[0] if (y // 8) % 2 else bg[1])
    skin, hair, cloth = spec["skin"], spec["hair"], spec["cloth"]
    # Schultern und Kleidung
    for y in range(46, 64):
        half = 15 + (y - 46) * 1.1
        for x in range(64):
            dx = x - 31.5
            if abs(dx) <= half:
                lit = 2.6 - (dx / half) * 0.9 - (y - 46) / 24.0
                t.set(x, y, ramp(cloth, lit))
    for y in range(46, 52):                                  # Kragen
        for x in range(25, 39):
            if abs(x - 31.5) >= (y - 46) * 0.9 + 3:
                t.set(x, y, ramp(cloth, 3.6 if x < 32 else 2.8))
    # Hals
    for y in range(38, 48):
        for x in range(27, 37):
            t.set(x, y, ramp(skin, 1.6 if x > 34 else 2.3 if y < 41 else 2.0))
    # Kopf: oben rund, unten zum Kinn schmaler
    def head_shade(x, y):
        dx, dy = x + 0.5 - 32, y + 0.5 - 27
        if dy > 6 and abs(dx) > 13 - (dy - 6) * 0.9:
            return None
        # breite Lichtflaeche, Schatten nur als schmale Zone rechts und unter dem Kinn
        light = 3.3 if dx < -8 else 3.0 if dx < 6 else 2.0
        if dy > 11:
            light -= 0.6
        return ramp(skin, light)
    _fill_ellipse(t, 32, 27, 13, 15, head_shade)
    # Ohren
    for (x, y) in ((18, 27), (18, 28), (18, 29), (45, 27), (45, 28), (45, 29)):
        t.set(x, y, ramp(skin, 2.2 if x < 30 else 1.6))
    spec["hair_fn"](t, hair)
    _face(t, spec, pose)
    for fn in spec.get("extras", ()):
        fn(t, pose)
    t.outline(spec.get("outline", OUTLINE))
    return t


def _face(t: Tex, spec: dict, pose: str):
    skin = spec["skin"]
    dark = skin[0]
    brow = spec["brow"]
    # Augenbrauen: neutral gerade, happy angehoben, sad innen hoch, angry innen tief
    for side, x0 in ((-1, 23), (1, 36)):
        for i in range(5):
            x = x0 + i
            inner = i if side == 1 else 4 - i                     # 0 = innen
            y = 22
            if pose == "happy":
                y = 21 if 1 <= i <= 3 else 22
            elif pose == "sad":
                y = 21 if inner <= 1 else 22 + (1 if inner >= 4 else 0)
            elif pose == "angry":
                y = 23 if inner <= 1 else 22 - (1 if inner >= 4 else 0)
            t.set(x, y, brow)
    # Augen: 5x3 mit Weiss, Iris 2x2, Pupille, Glanzpunkt
    white = hx("#f2ece4")
    iris = spec["iris"]
    for x0 in (23, 36):
        lid = 1 if pose in ("happy", "angry") else 0
        look = 1 if pose == "sad" else 0
        for dy in range(3):
            for dx in range(5):
                if dy < lid:
                    t.set(x0 + dx, 25 + dy, ramp(skin, 2.0))        # Lid
                    continue
                c = white if dx not in (0, 4) or dy == 1 else ramp(skin, 2.2)
                t.set(x0 + dx, 25 + dy, c)
        for (dx, dy) in ((1, 1), (2, 1), (1, 2), (2, 2)):
            t.set(x0 + dx + (1 if x0 == 23 else 0), 25 + min(2, dy + look - (1 if lid else 0) + (1 if lid else 0)), iris)
        t.set(x0 + 2, 26 + look, hx("#120c10"))
        t.set(x0 + 1 + (1 if x0 == 23 else 0), 25 + lid, hx("#ffffff") if pose != "sad" else white)
        for dx in range(5):
            t.set(x0 + dx, 24 + lid, dark)                           # Oberlid
    if pose == "sad":
        t.set(26, 29, SKYB[3])                                       # Traene
        t.set(26, 30, SKYB[2])
    # Nase
    for (x, y) in ((32, 29), (32, 30), (33, 31), (31, 32)):
        t.set(x, y, ramp(skin, 1.9))
    t.set(30, 32, ramp(skin, 1.6))
    # Mund
    mouth = spec["mouth"]
    if pose == "happy":
        for x in range(28, 37):
            t.set(x, 36 - (1 if x in (28, 36) else 0), mouth)
        for x in range(30, 35):
            t.set(x, 37, ramp(skin, 1.8))
        for x in (24, 25, 38, 39):
            t.set(x, 31, mix(skin[3], hx("#e07a6a"), 0.4))          # Wangenrot
    elif pose == "sad":
        for x in range(29, 36):
            t.set(x, 36 + (1 if x in (29, 35) else 0), mouth)
    elif pose == "angry":
        for x in range(29, 36):
            t.set(x, 36, mouth)
        for x in range(30, 35):
            t.set(x, 37, hx("#f0ebe0"))                              # Zaehne
        for x in range(29, 36):
            t.set(x, 38, mouth)
    else:
        for x in range(29, 35):
            t.set(x, 36, mouth)


def rhea_hair(t: Tex, hair):
    """Rotbraunes Haar, Seitenscheitel, Straehnen bis zum Kiefer, Zopf hinten rechts sichtbar."""
    def shade(x, y):
        dx = x + 0.5 - 32
        strand = (nz(x, 3, 5121) - 0.5) * 0.9
        return ramp(hair, 3.0 - (dx + 14) / 30.0 * 1.4 + strand - y / 60.0)
    _fill_ellipse(t, 32, 20, 15, 11, lambda x, y: shade(x, y) if y <= 21 or abs(x + 0.5 - 32) > 11 else None)
    for y in range(18, 34):                                          # Seitenstraehnen
        for x in (17, 18, 19, 45, 46):
            if y < 30 or x in (18, 45):
                t.set(x, y, shade(x, y))
    for x in range(22, 36):                                          # Pony, schraeg
        for y in range(16, 20 + (1 if x < 27 else 0) - (1 if x > 32 else 0)):
            t.set(x, y, shade(x, y))
    for y in range(26, 44):                                          # Zopf hinten rechts
        w = 3 if y < 38 else 2
        for x in range(47, 47 + w):
            t.set(x, y, ramp(hair, 2.4 - (y - 26) / 20.0 + (0.6 if x == 47 else 0)))


def rhea_extras(t: Tex, pose):
    for x in range(41, 49):                                          # Schulterplatte rechts im Bild
        for y in range(48, 52):
            t.set(x, y, GOLD[2] if y == 48 else ramp(SLATE, 3.0 - (y - 48) * 0.4))
    t.set(31, 54, SKYB[4])                                           # Reinheitsanhaenger
    t.set(32, 54, SKYB[2])
    t.set(31, 55, SKYB[2])
    t.set(32, 55, SKYB[1])
    for y in range(47, 53):
        t.set(31 if y % 2 else 32, y, GOLD[2])                       # Kettchen


SPECS = {
    "rhea": {
        "skin": SKIN_WARM, "hair": HAIR_AUBURN, "cloth": COAT_STEEL, "iris": EYES_GREEN, "brow": HAIR_AUBURN[1],
        "mouth": hx("#8c3f36"), "background": [hx("#1b2333"), hx("#1f293b")], "hair_fn": rhea_hair, "extras": [rhea_extras],
    },
}

TEXTURES = {"rhea": rhea_texture}


def main():
    for npc in TEXTURES:
        geo(npc)
        anims(npc, floating=SPECS[npc].get("floating", False))
        TEXTURES[npc]()
        for pose in POSES:
            save_png(portrait(SPECS[npc], pose), f"gui/portraits/{npc}_{pose}")
    print(f"npc_assets: {len(TEXTURES)} NPCs (Geo, Animationen, Textur, Glowmaske, {len(POSES)} Porträts)")


if __name__ == "__main__":
    main()
