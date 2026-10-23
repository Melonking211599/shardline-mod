"""Vorschau: Spieler mit Shardline-Ruestung samt 3D-Zusatzteilen aus vier Blickwinkeln.

Aufruf: python tools/assetgen/preview_armor.py <ausgabe.png>
Rendert den Spieler (graue Quader), die Vanilla-Ruestungslayer (armor.py) und die Zusatzteile (armor_extras.py)
mit demselben Texturlayout wie Minecraft. Reine Pillow-Software-Darstellung, nur zur Kontrolle der Form.
"""
from __future__ import annotations

import math
import sys

from PIL import Image, ImageDraw

import armor_extras as ax
from common import TEX

ARMOR = TEX / "models" / "armor"
LIGHT = {"up": 1.0, "down": 0.55, "north": 0.85, "south": 0.7, "west": 0.75, "east": 0.6}


def rot(v, r):
    """MC-Reihenfolge: erst x, dann y, dann z."""
    x, y, z = v
    rx, ry, rz = r
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return (x, y, z)


def add(a, b):
    return (a[0] + b[0], a[1] + b[1], a[2] + b[2])


class Scene:
    def __init__(self):
        self.texels = []  # (corners[4], farbe, licht, leuchtet)

    def box(self, img, uv, size, off, pivot, rotation, anchor, grow=0.0, mirror=False, glow_img=None, flat=None):
        """Eine Box mit Minecraft-Boxlayout. off/pivot im Teilraum, anchor = Ursprung des Koerperteils."""
        u0, v0 = uv
        w, h, d = size
        x0, y0, z0 = off[0] - grow, off[1] - grow, off[2] - grow
        x1, y1, z1 = off[0] + w + grow, off[1] + h + grow, off[2] + d + grow

        def tr(p):
            return add(add(rot(p, rotation), pivot), anchor)

        def texels(face, rx, ry, rw, rh, corner):
            for j in range(rh):
                for i in range(rw):
                    ii = rw - 1 - i if mirror else i
                    px, py = rx + ii, ry + j
                    col = img.getpixel((px, py)) if flat is None else flat
                    if col[3] < 128:
                        continue
                    gl = glow_img.getpixel((px, py)) if glow_img is not None else (0, 0, 0, 0)
                    a = (i / rw, j / rh)
                    b = ((i + 1) / rw, (j + 1) / rh)
                    quad = [corner(a[0], a[1]), corner(b[0], a[1]), corner(b[0], b[1]), corner(a[0], b[1])]
                    self.texels.append(([tr(q) for q in quad], col, LIGHT[face], gl[3] > 128, rot((0, 0, 0), rotation)))

        lerp = lambda a, b, t: a + (b - a) * t
        # -x (west): links hinten (+z) nach rechts vorn (-z)
        texels("west", u0, v0 + d, d, h, lambda s, t: (x0, lerp(y0, y1, t), lerp(z1, z0, s)))
        # front (-z): links -x nach rechts +x
        texels("north", u0 + d, v0 + d, w, h, lambda s, t: (lerp(x0, x1, s), lerp(y0, y1, t), z0))
        # +x (east)
        texels("east", u0 + d + w, v0 + d, d, h, lambda s, t: (x1, lerp(y0, y1, t), lerp(z0, z1, s)))
        # hinten (+z): links +x nach rechts -x
        texels("south", u0 + d + w + d, v0 + d, w, h, lambda s, t: (lerp(x1, x0, s), lerp(y0, y1, t), z1))
        # oben: obere Zeile hinten, untere vorn
        texels("up", u0 + d, v0, w, d, lambda s, t: (lerp(x0, x1, s), y0, lerp(z1, z0, t)))
        texels("down", u0 + d + w, v0, w, d, lambda s, t: (lerp(x0, x1, s), y1, lerp(z0, z1, t)))

    def render(self, yaw, scale=12, size=(300, 520), cx=150, cy=215):
        sy, cyaw = math.sin(yaw), math.cos(yaw)
        img = Image.new("RGB", size, (64, 68, 82))
        dr = ImageDraw.Draw(img)
        items = []
        for corners, col, light, glow, _r in self.texels:
            pts, depth = [], 0.0
            for (x, y, z) in corners:
                sx = x * cyaw + z * sy
                dz = -x * sy + z * cyaw
                pts.append((cx + sx * scale, cy + y * scale))
                depth += dz
            items.append((depth / 4, pts, col, light, glow))
        items.sort(key=lambda t: -t[0])  # entfernte zuerst (Blick entlang +z nach Drehung: groesseres dz = weiter weg)
        for _d, pts, col, light, glow in items:
            f = 1.0 if glow else light
            c = tuple(min(255, int(col[k] * f)) for k in range(3))
            dr.polygon(pts, fill=c)
        return img


def build_scene(set_name: str, with_extras=True) -> Scene:
    from PIL import Image as I
    sc = Scene()
    skin = (150, 120, 100, 255)
    l1 = I.open(ARMOR / f"{set_name}_layer_1.png").convert("RGBA")
    l2 = I.open(ARMOR / f"{set_name}_layer_2.png").convert("RGBA")
    anchors = {"head": (0, 0, 0), "body": (0, 0, 0), "rarm": (-5, 2, 0), "larm": (5, 2, 0), "rleg": (-1.9, 12, 0), "lleg": (1.9, 12, 0)}
    z3 = (0, 0, 0)
    # Spieler
    sc.box(None, (0, 0), (8, 8, 8), (-4, -8, -4), z3, z3, anchors["head"], flat=skin)
    sc.box(None, (0, 0), (8, 12, 4), (-4, 0, -2), z3, z3, anchors["body"], flat=(70, 110, 160, 255))
    sc.box(None, (0, 0), (4, 12, 4), (-3, -2, -2), z3, z3, anchors["rarm"], flat=skin)
    sc.box(None, (0, 0), (4, 12, 4), (-1, -2, -2), z3, z3, anchors["larm"], flat=skin)
    sc.box(None, (0, 0), (4, 12, 4), (-2, 0, -2), z3, z3, anchors["rleg"], flat=(60, 60, 130, 255))
    sc.box(None, (0, 0), (4, 12, 4), (-2, 0, -2), z3, z3, anchors["lleg"], flat=(60, 60, 130, 255))
    # Vanilla-Ruestungslayer
    sc.box(l1, (0, 0), (8, 8, 8), (-4, -8, -4), z3, z3, anchors["head"], grow=0.55)
    sc.box(l1, (16, 16), (8, 12, 4), (-4, 0, -2), z3, z3, anchors["body"], grow=0.55)
    sc.box(l1, (40, 16), (4, 12, 4), (-3, -2, -2), z3, z3, anchors["rarm"], grow=0.55)
    sc.box(l1, (40, 16), (4, 12, 4), (-1, -2, -2), z3, z3, anchors["larm"], grow=0.55, mirror=True)
    sc.box(l1, (0, 16), (4, 12, 4), (-2, 0, -2), z3, z3, anchors["rleg"], grow=0.55)
    sc.box(l1, (0, 16), (4, 12, 4), (-2, 0, -2), z3, z3, anchors["lleg"], grow=0.55, mirror=True)
    sc.box(l2, (16, 16), (8, 12, 4), (-4, 0, -2), z3, z3, anchors["body"], grow=0.3)
    sc.box(l2, (0, 16), (4, 12, 4), (-2, 0, -2), z3, z3, anchors["rleg"], grow=0.3)
    sc.box(l2, (0, 16), (4, 12, 4), (-2, 0, -2), z3, z3, anchors["lleg"], grow=0.3, mirror=True)
    if with_extras:
        tex = I.open(ARMOR / f"{set_name}_extras.png").convert("RGBA")
        glow = I.open(ARMOR / f"{set_name}_extras_glow.png").convert("RGBA")
        anchor_of = {"helmet": "head", "torso": "body", "right_shoulder": "rarm", "left_shoulder": "larm",
                     "right_knee": "rleg", "left_knee": "lleg", "right_boot": "rleg", "left_boot": "lleg"}
        cubes = ax.SETS[set_name]()
        ax.allocate(cubes)
        for c in cubes:
            sc.box(tex, c.uv, c.size, c.off, c.pivot, c.rot, anchors[anchor_of[c.group]], glow_img=glow)
    return sc


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "armor_preview.png"
    yaws = [0, math.pi, math.pi / 2, math.pi * 0.25]
    sheet = Image.new("RGB", (300 * 4, 520 * 2), (64, 68, 82))
    for row, name in enumerate(("shard", "colossus")):
        sc = build_scene(name)
        for col, yaw in enumerate(yaws):
            sheet.paste(sc.render(yaw), (col * 300, row * 520))
    sheet.save(out)
    print("gespeichert:", out)


if __name__ == "__main__":
    main()
