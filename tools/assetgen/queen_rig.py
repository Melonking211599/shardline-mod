"""Splitterkoenigin (Selvaine, das Siegel): Rig, Texturen und Klon-Textur. Aufruf: python tools/assetgen/queen_rig.py [--preview <png>]

Schreibt geo/entity/shard_queen.geo.json, textures/entity/shard_queen(.png|_glowmask.png) und die Klon-Textur queen_clone
(mit Glowmaske). Der Echo-Geist (aftermath_assets.py) faerbt spaeter dieselbe Textur um. Die Animationen schreibt queen_anim.py.

Leitbild: schlanke, hohe Glasgestalt (46 px). Krone aus 7 Zacken, offene Brustmulde mit leuchtendem Kern, lange Arme mit
Kristallfingern, Rock aus Glasplatten, vierteilige Schleppe, drei schwebende Splitterringe, Rueckensplitter (Reste der Fluegel).
Geometrie in Bedrock-Koordinaten (y nach oben, Vorderseite -Z, rechts = negatives x). Rechts gezeichnet, links gespiegelt.
Drehvorzeichen (im Spiel geprueft): Wuerfel-Drehungen (`rot` je Wuerfel) kippen bei positivem x die Oberkante nach hinten.
Bone-Drehungen (Ruhepose hier und alle Clips) laufen in x umgekehrt: positives x kippt herabhaengende Spitzen nach hinten
(Arm: negatives x = nach vorn, Kopf: positives x = nach vorn). z-Drehungen sind gleich (rechter Arm: negatives z = nach aussen).
Keine Bone-Namen werden vom Java-Code gebraucht ausser `head` (Kopfdrehung zum Blickziel).
"""
from __future__ import annotations

import json
import math
import sys
from dataclasses import dataclass, field

from common import GEO, TEX, Tex, hx, mix
from uvlib import box_rects

# ---------------------------------------------------------------- Palette (Splitterweite: Violett, Akzent Eisblau)
QP = {
    "x": hx("#0b0620"),
    "g1": hx("#1a1040"), "g2": hx("#2d1b6b"), "g3": hx("#4a35a8"), "g4": hx("#7a60e0"), "g5": hx("#b9a6ff"),   # Violettglas
    "i1": hx("#2a5896"), "i2": hx("#5b9fe0"), "i3": hx("#a6dcff"), "i4": hx("#eefaff"),                       # Eis
    "v1": hx("#4b2a99"), "v2": hx("#7a45e8"), "v3": hx("#a56bff"), "v4": hx("#d3a8ff"), "v5": hx("#f3e4ff"),   # leuchtendes Violett
    "s0": hx("#7d6bb8"), "s1": hx("#a898dc"), "s2": hx("#cfc2f5"), "s3": hx("#eee8ff"),                        # Haut aus Glas
}
CLEAR = (0, 0, 0, 0)

FACE = [
    "ssssss",
    "ssbbss",
    "sbssbs",
    "sEssEs",
    "sTssss",
    "sTsnss",
    "ssmmss",
]


def _c(v, lo, hi):
    return max(lo, min(hi, v))


# ====================================================================== Rollen
def glass(P, face, fx, fy, fw, fh, o):
    """Violettglas: helle Kante oben, Schlieren, optional leuchtende Ader."""
    t = {"up": 3.6, "north": 2.9, "east": 2.3, "west": 2.5, "south": 1.9, "down": 1.2}[face] + o.get("lift", 0)
    side = face not in ("up", "down")
    if side and fy == 0:
        t += 1.0
    if side and fh > 2 and fy == fh - 1:
        t -= 1.3
    if fw > 2 and fx == 0:
        t += 0.4
    if fw > 2 and fx == fw - 1:
        t -= 0.6
    if fw >= 3 and fh >= 4 and (fx + fy) % 7 == 3:
        t += 1.1
    if o.get("vein") and side and fw >= 3 and fh >= 5:
        x = fw // 2 + (0, 1, 1, 0, -1, -1)[fy % 6]
        if fx == _c(x, 1, fw - 2):
            return P["v3"] if fy % 3 else P["v4"], True
    if o.get("hem") and side and fh >= 4:
        if fy == fh - 1:
            return P["i3"], True
        if fy == fh - 2:
            return P["i2"], True
    if t >= 5.4:
        return P["i3"], False
    return P["g" + str(_c(int(round(t)), 1, 5))], False


def blade(P, face, fx, fy, fw, fh, o):
    """Ringklinge: Violett mit Eisoberseite, leuchtet durchgehend."""
    key = {"up": "i3", "north": "v3", "west": "v3", "east": "v2", "south": "v2", "down": "v1"}[face]
    if face in ("north", "west") and fx == 0 and fw > 2:
        key = "v4"
    return P[key], True


def crystal(P, face, fx, fy, fw, fh, o):
    """Leuchtkristall: Spitze oben (fy = 0) am hellsten, Fuss dunkler, links hell und rechts dunkel."""
    if face == "down":
        return P["v1"], True
    k = fy / max(1, fh - 1)
    t = 4.7 - k * 2.5
    if face in ("east", "south"):
        t -= 0.7
    if face == "up":
        t = 5.2
    if o.get("tip") == "ice" and fy == 0 and face != "up":
        return P["i4"] if fx % 2 == 0 else P["i3"], True
    if face == "up" and o.get("tip") == "ice":
        return P["i4"], True
    return P["v" + str(_c(int(round(t)), 1, 5))], True


def core(P, face, fx, fy, fw, fh, o):
    """Eingesperrter Ton: ganzer Kern leuchtet, Mitte weiss."""
    cx, cy = (fw - 1) / 2.0, (fh - 1) / 2.0
    d = max(abs(fx - cx) / max(fw / 2.0, 0.5), abs(fy - cy) / max(fh / 2.0, 0.5))
    t = 5 - int(d * 3.2)
    if face in ("east", "south"):
        t -= 1
    return P["v" + str(_c(t, 1, 5))], True


def gem(P, face, fx, fy, fw, fh, o):
    if fx == 0 and fy == 0:
        return P["i4"], True
    return P["i3"] if face == "north" else P["i2"], True


def skin(P, face, fx, fy, fw, fh, o):
    t = {"up": 3, "north": 2, "east": 1, "west": 1, "south": 0, "down": 0}[face]
    return P["s" + str(t)], False


def head(P, face, fx, fy, fw, fh, o):
    """Gesicht auf der Vorderseite (traurige Brauen, leuchtende Augen, eine Traene), Haar aus Glas sonst."""
    if face == "north":
        ch = FACE[fy][fx]
        if ch == "E":
            return P["i3"], True
        if ch == "T":
            return (P["i4"] if fy == 5 else P["i3"]), True
        if ch == "b":
            return P["g3"], False
        if ch == "m":
            return P["g2"], False
        if ch == "n":
            return P["s0"], False
        edge = fx in (0, fw - 1)
        return P["s1"] if edge else P["s2"], False
    if face == "down":
        return P["s0"], False
    t = {"up": 3.6, "east": 2.7, "west": 2.9, "south": 2.3}[face]
    if (fx * 3 + fy) % 5 == 0:
        t += 1.0
    if face != "up" and fy == 0:
        t += 0.7
    return P["g" + str(_c(int(round(t)), 1, 5))], False


ROLES = {"blade": blade, "glass": glass, "crystal": crystal, "core": core, "gem": gem, "skin": skin, "head": head}


# ====================================================================== Rig
@dataclass
class Cube:
    bone: str
    name: str
    origin: tuple
    size: tuple
    role: str
    inflate: float = 0.0
    rot: tuple | None = None
    pivot: tuple | None = None
    opts: dict = field(default_factory=dict)
    uv: tuple = (0, 0)


class Rig:
    def __init__(self, ident="shard_queen"):
        self.ident = ident
        self.bones: dict[str, dict] = {}
        self.cubes: list[Cube] = []
        self.tex_w, self.tex_h = 128, 128

    def bone(self, name, pivot, parent=None, rot=None):
        self.bones[name] = {"pivot": tuple(pivot), "parent": parent, "rot": tuple(rot) if rot else None}

    def cube(self, bone, name, origin, size, role="glass", rot=None, pivot=None, inflate=0.0, **opts):
        self.cubes.append(Cube(bone, name, tuple(origin), tuple(int(s) for s in size), role, inflate,
                               tuple(rot) if rot else None, tuple(pivot) if pivot else None, opts))

    def sym(self, bone_r, bone_l, name, origin, size, role="glass", rot=None, pivot=None, **kw):
        """Rechtes Teil (negatives x) und sein Spiegelbild im linken Bone."""
        self.cube(bone_r, name + "_r", origin, size, role, rot, pivot, **kw)
        ox, oy, oz = origin
        self.cube(bone_l, name + "_l", (-(ox + size[0]), oy, oz), size, role,
                  (rot[0], -rot[1], -rot[2]) if rot else None, (-pivot[0], pivot[1], pivot[2]) if pivot else None, **kw)

    # ------------------------------------------------------------ UV-Packer
    def pack(self, gap=1):
        boxes = []
        for c in self.cubes:
            w, h, d = c.size
            boxes.append((2 * (w + d), d + h, c))
        boxes.sort(key=lambda b: (-b[1], -b[0], b[2].bone, b[2].name))
        x = y = row_h = 0
        for bw, bh, c in boxes:
            if x + bw > self.tex_w:
                x, y, row_h = 0, y + row_h + gap, 0
            c.uv = (x, y)
            x += bw + gap
            row_h = max(row_h, bh)
        return y + row_h

    # ------------------------------------------------------------ Ausgabe
    def geo(self):
        bones = []
        for name, b in self.bones.items():
            d = {"name": name}
            if b["parent"]:
                d["parent"] = b["parent"]
            d["pivot"] = list(b["pivot"])
            if b["rot"]:
                d["rotation"] = list(b["rot"])
            cubes = []
            for c in self.cubes:
                if c.bone != name:
                    continue
                e = {"origin": [round(v, 3) for v in c.origin], "size": list(c.size), "uv": list(c.uv)}
                if c.inflate:
                    e["inflate"] = c.inflate
                if c.rot:
                    e["pivot"] = [round(v, 3) for v in (c.pivot or b["pivot"])]
                    e["rotation"] = [round(v, 3) for v in c.rot]
                cubes.append(e)
            if cubes:
                d["cubes"] = cubes
            bones.append(d)
        return {"format_version": "1.12.0", "minecraft:geometry": [{
            "description": {"identifier": f"geometry.{self.ident}", "texture_width": self.tex_w, "texture_height": self.tex_h,
                            "visible_bounds_width": 6, "visible_bounds_height": 5, "visible_bounds_offset": [0, 1.5, 0]},
            "bones": bones}]}

    def paint(self):
        tex, glow = Tex(self.tex_w, self.tex_h), Tex(self.tex_w, self.tex_h)
        for c in self.cubes:
            w, h, d = c.size
            fn = ROLES[c.role]
            for face, (x0, y0, fw, fh) in box_rects(c.uv[0], c.uv[1], w, h, d).items():
                for fy in range(fh):
                    for fx in range(fw):
                        col, glows = fn(QP, face, fx, fy, fw, fh, c.opts)
                        tex.set(x0 + fx, y0 + fy, col)
                        if glows:
                            glow.set(x0 + fx, y0 + fy, col)
        return tex, glow


def spike_rot(angle_deg, tilt):
    """Kleine Neigung nach aussen fuer ein Teil am Umfang (Winkel 0 = vorn, -Z)."""
    a = math.radians(angle_deg)
    return (-tilt * math.cos(a), 0.0, -tilt * math.sin(a))


# ====================================================================== Geometrie
def build() -> Rig:
    r = Rig()
    r.bone("root", (0, 0, 0))
    r.bone("body", (0, 20, 0), "root")
    # ---- Rock: lange, gezackte Glasplatten (vorn fuenf, hinten vier, seitlich je zwei), nach unten ausgestellt
    for n in ("front", "back", "left", "right"):
        r.bone(f"gown_{n}", (0, 20, 0), "body")
    r.cube("gown_front", "gf_up", (-3, 13, -3), (6, 7, 1), "glass", vein=True)
    for i, (x, ln) in enumerate(((-4.2, 9), (-2.2, 11), (-0.6, 12), (1.0, 11), (3.0, 9))):
        flare = 7 + 3 * abs(x + 0.6) / 1.2
        r.cube("gown_front", f"gf{i}", (x, 13.5 - ln, -3.5), (2, ln, 1), "glass", rot=(flare, 0, 0), pivot=(0, 13.5, -3),
               lift=(0.9 if i % 2 else -0.3), hem=True)
    r.cube("gown_back", "gb_up", (-3, 13, 2), (6, 7, 1), "glass")
    for i, (x, ln) in enumerate(((-3.8, 8), (-1.8, 10), (0.0, 10), (1.8, 8))):
        r.cube("gown_back", f"gb{i}", (x, 13.5 - ln, 2.5), (2, ln, 1), "glass", rot=(-(7 + i % 2 * 3), 0, 0), pivot=(0, 13.5, 3),
               lift=(0.2 if i % 2 else -0.8), hem=True)
    r.cube("gown_right", "gr_up", (-3, 13, -2.5), (1, 7, 5), "glass")
    r.cube("gown_right", "gr_a", (-3.5, 3.5, -2.5), (1, 10, 2), "glass", rot=(0, 0, -13), pivot=(-2.5, 13.5, 0), lift=0.7, hem=True)
    r.cube("gown_right", "gr_b", (-3.5, 4.5, 0.0), (1, 9, 2), "glass", rot=(0, 0, -17), pivot=(-2.5, 13.5, 0), lift=-0.4, hem=True)
    r.cube("gown_left", "gl_up", (2, 13, -2.5), (1, 7, 5), "glass")
    r.cube("gown_left", "gl_a", (2.5, 3.5, -2.5), (1, 10, 2), "glass", rot=(0, 0, 13), pivot=(2.5, 13.5, 0), lift=0.7, hem=True)
    r.cube("gown_left", "gl_b", (2.5, 4.5, 0.0), (1, 9, 2), "glass", rot=(0, 0, 17), pivot=(2.5, 13.5, 0), lift=-0.4, hem=True)
    # ---- Gurt
    r.bone("waist", (0, 20, 0), "body")
    r.cube("waist", "belt", (-3, 19.5, -2.5), (6, 1, 5), "glass", lift=1.4)
    r.cube("waist", "belt_gem", (-0.5, 19.6, -3.1), (1, 1, 1), "gem")
    # ---- Oberkoerper: Rippenkaefig mit offener Front, dunkle Rueckwand hinter dem Kern
    r.bone("torso", (0, 21, 0), "body")
    r.cube("torso", "back", (-3.5, 21, 1), (7, 9, 1), "glass", vein=True, lift=-0.6)
    r.sym("torso", "torso", "side", (-3.5, 21, -2), (1, 9, 3), "glass", lift=0.5)
    r.cube("torso", "top_bar", (-2.5, 29, -2), (5, 1, 2), "glass", lift=1.2)
    r.cube("torso", "bottom_bar", (-2.5, 21, -2), (5, 1, 2), "glass", lift=1.2)
    r.cube("torso", "yoke", (-4.5, 29, -1.5), (9, 1, 3), "glass", lift=1.8)
    r.sym("torso", "torso", "collar", (-4.2, 30, -2), (2, 1, 3), "glass", rot=(0, 0, 14), pivot=(-3.2, 30, 0), lift=1.8)
    r.sym("torso", "torso", "ruff", (-4.6, 30, 0.6), (1, 7, 2), "glass", rot=(8, 0, 12), pivot=(-4.2, 30, 1), lift=1.0)
    # ---- Kern in der Brustmulde (drei verschraenkte Koerper)
    r.bone("core", (0, 26, -0.3), "torso")
    r.cube("core", "core_a", (-1.5, 23.5, -1.8), (3, 5, 3), "core")
    r.cube("core", "core_b", (-1.5, 23.5, -1.8), (3, 5, 3), "core", rot=(0, 45, 0), pivot=(0, 26, -0.3))
    r.cube("core", "core_c", (-1.5, 23.5, -1.8), (3, 5, 3), "core", rot=(0, 0, 45), pivot=(0, 26, -0.3))
    r.cube("core", "core_up", (-0.5, 28.2, -1.3), (1, 2, 1), "crystal")
    r.cube("core", "core_down", (-0.5, 22.4, -1.3), (1, 1, 1), "crystal")
    # ---- Kopf, Kronreif, Krone, Schleier
    r.bone("neck", (0, 31, 0), "torso")                    # Kopfneigungen der Clips; `head` selbst folgt dem Blickziel
    r.cube("neck", "neck_c", (-1, 30, -1), (2, 2, 2), "glass", lift=0.8)
    r.bone("head", (0, 32, 0), "neck")
    r.cube("head", "skull", (-3, 32, -3), (6, 7, 6), "head")
    r.cube("head", "circlet", (-3.5, 37, -3.5), (7, 1, 7), "glass", lift=2.6)
    r.bone("crown", (0, 38, 0), "head")
    for i, (ang, h, w) in enumerate(((0, 8, 2), (-40, 6, 1), (40, 6, 1), (-80, 5, 1), (80, 5, 1), (-125, 4, 1), (125, 4, 1))):
        a = math.radians(ang)
        px, pz = 3.2 * math.sin(a), -3.2 * math.cos(a)
        r.cube("crown", f"spike{i}", (px - w / 2.0, 38, pz - 0.5), (w, h, 1), "crystal", rot=spike_rot(ang, 14), pivot=(px, 38, pz), tip="ice")
    r.bone("veil_r", (-2.5, 37, 3), "head", rot=(6, 0, 3))
    r.bone("veil_l", (2.5, 37, 3), "head", rot=(6, 0, -3))
    r.sym("veil_r", "veil_l", "veil_a", (-3, 28, 2.6), (1, 9, 1), "glass", pivot=(-2.5, 37, 3), lift=-0.2)
    r.sym("veil_r", "veil_l", "veil_b", (-1.8, 30, 3.0), (1, 7, 1), "glass", pivot=(-2.5, 37, 3), lift=0.3)
    # ---- Arme: leicht abgespreizt, Unterarm angewinkelt
    for sfx, s in (("r", -1), ("l", 1)):
        r.bone(f"shoulder_{sfx}", (5.0 * s, 29.5, 0), "torso")
        r.bone(f"upperarm_{sfx}", (5.5 * s, 29, 0), f"shoulder_{sfx}", rot=(0, 0, -8 * -s))
        r.bone(f"forearm_{sfx}", (5.5 * s, 21, 0), f"upperarm_{sfx}", rot=(-12, 0, -4 * -s))
        r.bone(f"hand_{sfx}", (5.5 * s, 12, 0), f"forearm_{sfx}", rot=(-8, 0, 0))
    r.sym("shoulder_r", "shoulder_l", "pauld", (-7.5, 29, -2), (4, 1, 4), "glass", rot=(0, 0, -6), pivot=(-5, 29.5, 0), lift=1.8)
    r.sym("shoulder_r", "shoulder_l", "pcrys_a", (-7.0, 29.5, -1), (2, 4, 2), "crystal", rot=(0, 0, 24), pivot=(-6.2, 29.5, 0), tip="ice")
    r.sym("upperarm_r", "upperarm_l", "arm_u", (-6.5, 21, -1), (2, 8, 2), "glass", vein=True)
    r.sym("upperarm_r", "upperarm_l", "arm_us", (-7.6, 23.5, -0.5), (1, 4, 1), "crystal", rot=(0, 0, 10), pivot=(-7.1, 24, 0))
    r.sym("forearm_r", "forearm_l", "arm_f", (-6.5, 12, -1), (2, 9, 2), "glass", lift=0.5)
    r.sym("forearm_r", "forearm_l", "elbow", (-7.8, 19.5, -0.5), (1, 3, 1), "crystal", rot=(0, 0, 28), pivot=(-7.1, 19.5, 0))
    r.sym("forearm_r", "forearm_l", "blade", (-7.3, 13.5, -0.5), (1, 6, 1), "crystal", rot=(0, 0, 6), pivot=(-7.3, 14, 0), tip="ice")
    r.sym("hand_r", "hand_l", "palm", (-6.5, 10, -1), (2, 2, 2), "glass", lift=1.6)
    for i, ang in enumerate((-15, -5, 5, 15)):
        r.sym("hand_r", "hand_l", f"finger{i}", (-6.5 + i * 0.5, 5.2, -0.5 + (i % 2) * 0.6), (1, 5, 1), "crystal",
              rot=(0, 0, ang), pivot=(-5.5, 10, 0), tip="ice")
    # ---- Rueckensplitter (Reste der Fluegel): zwei kraeftige Zacken je Seite
    r.bone("back_r", (-2, 28.5, 2), "torso")
    r.bone("back_l", (2, 28.5, 2), "torso")
    for i, (h, rz) in enumerate(((10, 24), (7, 46))):
        r.sym("back_r", "back_l", f"bshard{i}", (-3.0, 28.5, 1.8), (2, h, 1), "crystal", rot=(14, 0, rz), pivot=(-2, 28.5, 2), tip="ice")
    # ---- Schleppe: drei breite Glassegmente, fallen nach hinten ab
    for i, (py, w) in enumerate(((14, 9), (10, 8), (6, 6))):
        name = f"train_{i + 1}"
        r.bone(name, (0, py, 2.5), "body" if i == 0 else f"train_{i}", rot=(9, 0, 0))
        r.cube(name, f"t{i}_plate", (-w / 2.0, py - 4, 2), (w, 4, 1), "glass", lift=0.1 + 0.4 * i, hem=True)
        r.sym(name, name, f"t{i}_s", (-w / 2.0 - 0.8, py - 3.5, 2.2), (1, 4, 1), "crystal", rot=(0, 0, 22), pivot=(-w / 2.0, py - 1, 2.5))
    # ---- Splitterringe wie ein Astrolabium: einer waagerecht um die Huefte, zwei geneigt um Brust und Schultern
    for idx, (y, rad, n, rest) in enumerate(((15.5, 9, 12, (0, 0, 0)), (27, 11, 14, (-34, 0, 0)), (25, 13, 16, (-40, 0, -46)))):
        tilt = f"ring_{idx + 1}"
        name = f"ring_{idx + 1}_spin"
        r.bone(tilt, (0, y, 0), "body", rot=rest)          # Neigung des Rings (fest)
        r.bone(name, (0, y, 0), tilt)                      # dreht sich in der geneigten Ringebene
        step = 2 * math.pi * rad / n
        for i in range(n):
            ang = i * 360.0 / n
            a = math.radians(ang)
            px, pz = rad * math.sin(a), -rad * math.cos(a)
            dy = 0.5 if i % 2 else -0.5
            if i % 5 == 4:
                r.cube(name, f"rs{i}", (px - 0.5, y - 2 + dy, pz - 0.5), (1, 4, 1), "crystal", rot=spike_rot(ang, 14), pivot=(px, y + dy, pz), tip="ice")
            else:
                ln = int(round(step * 1.12)) + (i % 2)
                r.cube(name, f"rs{i}", (px - ln / 2.0, y - 1 + dy, pz - 0.5), (ln, 2, 1), "blade", rot=(0, -ang, 0), pivot=(px, y + dy, pz))
    return r


# ====================================================================== Klon-Textur (blasser, durchscheinend gerendert)
CLONE = [hx(c) for c in ("#4a3f7e", "#6e62a8", "#9488cc", "#bcb2ee", "#e4deff")]
CLONE_GLOW = [hx(c) for c in ("#8a5cd0", "#b890f4", "#dcc2ff", "#ffffff")]


def recolor(tex: Tex, ramp_, alpha=1.0, glow_ramp=None) -> Tex:
    out = Tex(tex.w, tex.h)
    for y in range(tex.h):
        for x in range(tex.w):
            r_, g_, b_, a = tex.px[x, y]
            if a == 0:
                continue
            lum = (0.3 * r_ + 0.55 * g_ + 0.15 * b_) / 255.0
            rp = glow_ramp or ramp_
            i = min(len(rp) - 1.0, lum * (len(rp) - 1) * 1.15)
            c = rp[int(round(i))]
            out.set(x, y, (c[0], c[1], c[2], int(a * alpha)))
    return out


def write(rig: Rig):
    used = rig.pack()
    if used > rig.tex_h:
        raise SystemExit(f"queen_rig: UV-Bedarf {used} px uebersteigt die Texturhoehe {rig.tex_h}")
    tex, glow = rig.paint()
    GEO.mkdir(parents=True, exist_ok=True)
    (GEO / "shard_queen.geo.json").write_text(json.dumps(rig.geo(), indent=2) + "\n", encoding="utf-8")
    tex.save(TEX / "entity" / "shard_queen.png")
    glow.save(TEX / "entity" / "shard_queen_glowmask.png")
    recolor(tex, CLONE).save(TEX / "entity" / "queen_clone.png")
    # Glowmaske des Klons: nur die leuchtenden Pixel, in derselben Farbe wie die Klon-Basistextur
    from PIL import Image
    cb = Image.open(TEX / "entity" / "queen_clone.png").convert("RGBA")
    cg = Tex(tex.w, tex.h)
    for y in range(tex.h):
        for x in range(tex.w):
            if glow.px[x, y][3]:
                cg.set(x, y, cb.getpixel((x, y)))
    cg.save(TEX / "entity" / "queen_clone_glowmask.png")
    return tex, glow, used


def main():
    rig = build()
    tex, glow, used = write(rig)
    print(f"queen_rig: {len(rig.bones)} Bones, {len(rig.cubes)} Wuerfel, UV-Bedarf {used} von {rig.tex_h} Zeilen")
    if "--preview" in sys.argv:
        import subprocess
        out = sys.argv[sys.argv.index("--preview") + 1]
        from pathlib import Path
        subprocess.run([sys.executable, str(Path(__file__).with_name("preview_geo.py")), out, "shard_queen", "--yaws", "0,35,90,180", "--scale", "11"], check=True)


if __name__ == "__main__":
    main()
