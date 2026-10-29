"""Bausatz fuer GeckoLib-Ruestungsmodelle: Wuerfel-DSL, UV-Packer, Flaechenmalerei, Geo-Ausgabe und Vorschau.

Ein Set besteht aus acht Bones (armorHead, armorBody, armorRightArm, armorLeftArm, armorRightLeg, armorLeftLeg,
armorRightBoot, armorLeftBoot) mit den Bedrock-Standardpivots; GeckoLib verschiebt und dreht sie wie die Vanilla-Teile.
Alle Wuerfel haben ganzzahlige Groessen (die UV-Flaeche liegt damit exakt auf Texeln); Zwischenmasse kommen ueber `inflate`.
Linke Teile sind geometrisch gespiegelt und haben eigene UV-Flaechen (kein `mirror`, damit die Pruefung eindeutig bleibt).
"""
from __future__ import annotations

import json
import math
from dataclasses import dataclass, field

from PIL import Image, ImageDraw

from common import ASSETS, Tex
from uvlib import box_rects

GEO_ARMOR = ASSETS / "geo" / "armor"
TEX_ARMOR = ASSETS / "textures" / "armor"
ANIM_ARMOR = ASSETS / "animations" / "armor"

PIVOTS = {
    "armorHead": (0, 24, 0), "armorBody": (0, 24, 0),
    "armorRightArm": (-5, 22, 0), "armorLeftArm": (5, 22, 0),
    "armorRightLeg": (-1.9, 12, 0), "armorLeftLeg": (1.9, 12, 0),
    "armorRightBoot": (-1.9, 12, 0), "armorLeftBoot": (1.9, 12, 0),
}
BONE_ORDER = list(PIVOTS)
MIRROR_BONE = {"armorRightArm": "armorLeftArm", "armorRightLeg": "armorLeftLeg", "armorRightBoot": "armorLeftBoot"}


@dataclass
class Cube:
    bone: str
    name: str
    origin: tuple
    size: tuple
    role: str = "plate"
    inflate: float = 0.0
    rotation: tuple | None = None
    pivot: tuple | None = None
    opts: dict = field(default_factory=dict)
    uv: tuple = (0, 0)


class Model:
    def __init__(self, palette: dict, tex_size=(128, 128)):
        self.P = palette
        self.cubes: list[Cube] = []
        self.tex_w, self.tex_h = tex_size

    # ------------------------------------------------------------ DSL
    def add(self, bone, name, origin, size, role="plate", inflate=0.0, rot=None, pivot=None, **opts):
        self.cubes.append(Cube(bone, name, tuple(origin), tuple(int(s) for s in size), role, inflate,
                               tuple(rot) if rot else None, tuple(pivot) if pivot else None, opts))

    def pair(self, bone, name, origin, size, role="plate", inflate=0.0, rot=None, pivot=None, **opts):
        """Rechtes Teil plus gespiegeltes linkes Teil (`bone` ist der rechte Bone)."""
        self.add(bone, name + "_r", origin, size, role, inflate, rot, pivot, **opts)
        ox, oy, oz = origin
        w = size[0]
        lrot = (rot[0], -rot[1], -rot[2]) if rot else None
        lpiv = (-pivot[0], pivot[1], pivot[2]) if pivot else None
        self.add(MIRROR_BONE.get(bone, bone), name + "_l", (-(ox + w), oy, oz), size, role, inflate, lrot, lpiv, **opts)

    # ------------------------------------------------------------ UV-Packer
    def pack(self, gap=1):
        """Regalpackung, deterministisch: hoechste Boxen zuerst. Waechst die Hoehe nach Bedarf (Vielfache von 16)."""
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
        used = y + row_h
        self.tex_h = max(16, int(math.ceil(used / 16.0)) * 16)
        return used

    # ------------------------------------------------------------ Geo-Ausgabe
    def geo(self, identifier):
        bones = []
        for name in BONE_ORDER:
            cubes = []
            for c in self.cubes:
                if c.bone != name:
                    continue
                d = {"origin": [round(v, 3) for v in c.origin], "size": list(c.size), "uv": list(c.uv)}
                if c.inflate:
                    d["inflate"] = c.inflate
                if c.rotation:
                    d["pivot"] = [round(v, 3) for v in (c.pivot or PIVOTS[name])]
                    d["rotation"] = [round(v, 3) for v in c.rotation]
                cubes.append(d)
            bones.append({"name": name, "pivot": list(PIVOTS[name]), "cubes": cubes})
        return {
            "format_version": "1.12.0",
            "minecraft:geometry": [{
                "description": {"identifier": f"geometry.{identifier}", "texture_width": self.tex_w, "texture_height": self.tex_h,
                                "visible_bounds_width": 3, "visible_bounds_height": 3.5, "visible_bounds_offset": [0, 1, 0]},
                "bones": bones,
            }],
        }

    # ------------------------------------------------------------ Malerei
    def paint(self):
        tex, glow = Tex(self.tex_w, self.tex_h), Tex(self.tex_w, self.tex_h)
        for c in self.cubes:
            w, h, d = c.size
            painter = ROLES[c.role]
            for face, (x0, y0, fw, fh) in box_rects(c.uv[0], c.uv[1], w, h, d).items():
                for fy in range(fh):
                    for fx in range(fw):
                        res = painter(self.P, face, fx, fy, fw, fh, c.opts)
                        if res is None:
                            continue
                        col, glows = res if isinstance(res[1], bool) else (res, False)
                        tex.set(x0 + fx, y0 + fy, col)
                        if glows:
                            glow.set(x0 + fx, y0 + fy, col)
        return tex, glow

    # ------------------------------------------------------------ Dateien
    def write(self, name, identifier=None):
        self.pack()
        tex, glow = self.paint()
        GEO_ARMOR.mkdir(parents=True, exist_ok=True)
        TEX_ARMOR.mkdir(parents=True, exist_ok=True)
        ANIM_ARMOR.mkdir(parents=True, exist_ok=True)
        (GEO_ARMOR / f"{name}.geo.json").write_text(json.dumps(self.geo(identifier or f"armor_{name}"), indent=2) + "\n", encoding="utf-8")
        tex.save(TEX_ARMOR / f"{name}.png")
        glow.save(TEX_ARMOR / f"{name}_glowmask.png")
        (ANIM_ARMOR / f"{name}.animation.json").write_text(json.dumps({"format_version": "1.8.0", "animations": {}}, indent=2) + "\n",
                                                           encoding="utf-8")
        return tex, glow


# ====================================================================== Rollen (Flaechenmaler)
def _c(i):
    return max(1, min(5, int(round(i))))


BASE = {"up": 4, "north": 3, "east": 3, "west": 2.5, "south": 2.6, "down": 1}


def plate(P, face, fx, fy, fw, fh, o):
    """Platte: Lichtkante oben, dunkle Fuge unten, Seitenkanten leicht dunkler; optional Nieten und Rune."""
    t = BASE[face] + 0.4 + o.get("lift", 0)
    big = fw >= 3 and fh >= 3
    if fh >= 2:
        if fy == 0:
            t += 1
        elif fy == fh - 1:
            t -= 2 if big else 1
    if big and fx in (0, fw - 1) and 0 < fy < fh - 1:
        t -= 1
    if o.get("rivets") and fw >= 5 and fh >= 4 and face in ("north", "south") and fy in (1, fh - 2) and fx in (1, fw - 2):
        return P["r"], False
    if o.get("groove") and fh >= 5 and fy == fh // 2 and 2 <= fx <= fw - 3 and face in ("north", "south"):
        return P[str(_c(t - 2))], False
    if o.get("rune") and fh >= 5 and fy == fh // 2 and 2 <= fx <= fw - 3 and face == "north" and fx % 2 == 0:
        return P["b"], True
    return P[str(_c(t))], False


def lame(P, face, fx, fy, fw, fh, o):
    """Schmale Lamelle: kraeftiger Kontrast, damit uebereinanderliegende Streifen als einzelne Teile lesen."""
    t = BASE[face] + o.get("lift", 0)
    if fh == 1:
        return P[str(_c(t))], False
    if fy == 0:
        t += 1.5
    elif fy == fh - 1:
        t -= 2.5
    return P[str(_c(t))], False


def under(P, face, fx, fy, fw, fh, o):
    """Grundschicht unter den Platten: dunkel, mit feiner Fuge, damit keine Luecke durchscheint."""
    t = 1 if (fx + fy) % 4 else 2
    if face == "up":
        t = 2
    return P[str(t)], False


def crystal(P, face, fx, fy, fw, fh, o):
    """Kristall: heller Grat oben, links hell, rechts dunkler, Spitze am hellsten. Leuchtet."""
    key = {"up": "w", "north": "c", "south": "b", "east": "b", "west": "c", "down": "a"}[face]
    if fw >= 2 and fx == fw - 1 and face in ("north", "south"):
        key = "a" if key == "b" else "b"
    if fh >= 3 and fy == 0 and face != "up":
        key = "w" if key == "c" else "c"
    if o.get("dark"):
        key = {"w": "c", "c": "b", "b": "a", "a": "a"}[key]
    return P[key], True


def gem(P, face, fx, fy, fw, fh, o):
    if face != "north":
        return P["b"], True
    if fx == 0 and fy == 0:
        return P["w"], True
    return (P["c"] if fx + fy < fw + fh - 2 else P["b"]), True


def trim(P, face, fx, fy, fw, fh, o):
    """Heller Zierstreifen (Kante, Niete)."""
    return P[str(_c(BASE[face] + 1.5))], False


def shell(P, face, fx, fy, fw, fh, o):
    """Dunkle Schale (Innenseite, Stiefelsohle)."""
    return P[str(_c(BASE[face] - 1.5))], False


ROLES = {"plate": plate, "lame": lame, "under": under, "crystal": crystal, "gem": gem, "trim": trim, "shell": shell}


# ====================================================================== Vorschau
LIGHT = {"up": 1.0, "down": 0.55, "north": 0.88, "south": 0.7, "west": 0.78, "east": 0.62}


def _rot(p, r_deg, pivot):
    x, y, z = (p[0] - pivot[0], p[1] - pivot[1], p[2] - pivot[2])
    rx, ry, rz = (math.radians(a) for a in r_deg)
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return (x + pivot[0], y + pivot[1], z + pivot[2])


def _box_quads(origin, size, color, rot=None, pivot=None, infl=0.0):
    ox, oy, oz = (origin[0] - infl, origin[1] - infl, origin[2] - infl)
    w, h, d = size[0] + 2 * infl, size[1] + 2 * infl, size[2] + 2 * infl
    xs, ys, zs = (ox, ox + w), (oy, oy + h), (oz, oz + d)
    faces = {
        "east": [(xs[1], ys[1], zs[0]), (xs[1], ys[1], zs[1]), (xs[1], ys[0], zs[1]), (xs[1], ys[0], zs[0])],
        "west": [(xs[0], ys[1], zs[1]), (xs[0], ys[1], zs[0]), (xs[0], ys[0], zs[0]), (xs[0], ys[0], zs[1])],
        "north": [(xs[1], ys[1], zs[0]), (xs[0], ys[1], zs[0]), (xs[0], ys[0], zs[0]), (xs[1], ys[0], zs[0])],
        "south": [(xs[0], ys[1], zs[1]), (xs[1], ys[1], zs[1]), (xs[1], ys[0], zs[1]), (xs[0], ys[0], zs[1])],
        "up": [(xs[1], ys[1], zs[1]), (xs[0], ys[1], zs[1]), (xs[0], ys[1], zs[0]), (xs[1], ys[1], zs[0])],
        "down": [(xs[1], ys[0], zs[0]), (xs[0], ys[0], zs[0]), (xs[0], ys[0], zs[1]), (xs[1], ys[0], zs[1])],
    }
    out = []
    for face, quad in faces.items():
        if rot:
            quad = [_rot(p, rot, pivot) for p in quad]
        out.append((quad, color, LIGHT[face], False))
    return out


MANNEQUIN = [((-4, 24, -4), (8, 8, 8)), ((-4, 12, -2), (8, 12, 4)), ((-8, 12, -2), (4, 12, 4)), ((4, 12, -2), (4, 12, 4)),
             ((-4, 0, -2), (4, 12, 4)), ((0, 0, -2), (4, 12, 4))]


def model_items(model: Model, tex: Tex, glow: Tex, slots=("head", "chest", "legs", "feet")):
    """Texel-Polygone des Modells (nur die gewaehlten Slots)."""
    from preview_geo import texels_of
    geo = model.geo("preview")["minecraft:geometry"][0]
    keep = {"head": ("armorHead",), "chest": ("armorBody", "armorRightArm", "armorLeftArm"),
            "legs": ("armorRightLeg", "armorLeftLeg"), "feet": ("armorRightBoot", "armorLeftBoot")}
    names = {n for s in slots for n in keep[s]}
    geo["bones"] = [b for b in geo["bones"] if b["name"] in names]
    bones = {b["name"]: b for b in geo["bones"]}
    return texels_of(geo, bones, tex.img, glow.img)


def mannequin_items():
    items = []
    for o, s in MANNEQUIN:
        items += _box_quads(o, s, (104, 104, 114))
    return items


def render_layers(layers, yaw_deg, scale, size, cx, base_y):
    """Zeichnet mehrere Ebenen nacheinander (jede nach Tiefe sortiert), damit die Puppe immer hinter der Ruestung liegt."""
    from preview_geo import render
    img = None
    for items in layers:
        yaw = math.radians(yaw_deg)
        sy, cy = math.sin(yaw), math.cos(yaw)
        if img is None:
            img = Image.new("RGB", size, (58, 62, 78))
        dr = ImageDraw.Draw(img)
        drawn = []
        for quad, col, light, glow in items:
            pts, depth = [], 0.0
            for (x, y, z) in quad:
                sx = -(x * cy + z * sy)
                dz = -x * sy + z * cy
                pts.append((cx + sx * scale, base_y - y * scale))
                depth += dz
            drawn.append((depth / 4, pts, col, light, glow))
        drawn.sort(key=lambda d: -d[0])
        for _d, pts, col, light, glow in drawn:
            f = 1.0 if glow else light
            dr.polygon(pts, fill=tuple(min(255, int(col[k] * f)) for k in range(3)))
    return img


def preview(model: Model, tex: Tex, glow: Tex, out: str, yaws=(0, 35, 90, 180), scale=14, slots=("head", "chest", "legs", "feet")):
    layers = [mannequin_items(), model_items(model, tex, glow, slots)]
    cell_w, cell_h = 26 * scale, 42 * scale
    sheet = Image.new("RGB", (cell_w * len(yaws), cell_h), (58, 62, 78))
    for i, yaw in enumerate(yaws):
        sheet.paste(render_layers(layers, yaw, scale, (cell_w, cell_h), cell_w // 2, cell_h - 10), (i * cell_w, 0))
    sheet.save(out)
    return sheet


# ====================================================================== Vanilla-Referenz (nur Vorschau, nichts wird kopiert)
def vanilla_reference(set_name="netherite"):
    """Baut aus dem Minecraft-Client-Jar (Gradle-Cache) Texel-Polygone der Vanilla-Ruestung auf derselben Puppe.
    Rueckgabe: Liste von Texel-Polygonen oder None, wenn das Jar fehlt."""
    import glob
    import io
    import zipfile
    from pathlib import Path
    from preview_geo import texels_of
    jars = glob.glob(str(Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_*_client.jar"))
    if not jars:
        return None
    out = []
    with zipfile.ZipFile(jars[0]) as z:
        l1 = Image.open(io.BytesIO(z.read(f"assets/minecraft/textures/models/armor/{set_name}_layer_1.png"))).convert("RGBA")
        l2 = Image.open(io.BytesIO(z.read(f"assets/minecraft/textures/models/armor/{set_name}_layer_2.png"))).convert("RGBA")

    def build(img, cubes):
        geo = {"description": {"texture_width": 64, "texture_height": 32}, "bones": [{"name": "b", "pivot": [0, 0, 0], "cubes": cubes}]}
        return texels_of(geo, {"b": geo["bones"][0]}, img, None)

    cube = lambda o, s, uv, inf, mirror=False: {"origin": o, "size": s, "uv": uv, "inflate": inf, "mirror": mirror}
    out += build(l1, [cube([-4, 24, -4], [8, 8, 8], [0, 0], 1.0), cube([-4, 12, -2], [8, 12, 4], [16, 16], 1.0),
                      cube([-8, 12, -2], [4, 12, 4], [40, 16], 1.0), cube([4, 12, -2], [4, 12, 4], [40, 16], 1.0, True),
                      cube([-4, 0, -2], [4, 12, 4], [0, 16], 1.0), cube([0, 0, -2], [4, 12, 4], [0, 16], 1.0, True)])
    out += build(l2, [cube([-4, 12, -2], [8, 12, 4], [16, 16], 0.5), cube([-4, 0, -2], [4, 12, 4], [0, 16], 0.5),
                      cube([0, 0, -2], [4, 12, 4], [0, 16], 0.5, True)])
    return out
