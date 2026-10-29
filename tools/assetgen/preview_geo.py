"""Vorschau: Mobs mit Textur als 3D-Modell (Ruhepose) aus mehreren Blickwinkeln.

Aufruf: python tools/assetgen/preview_geo.py <ausgabe.png> <wesen> [<wesen> ...] [--yaws 0,90,180,45] [--scale 6]
Liest geo/entity/<wesen>.geo.json und textures/entity/<wesen>.png (+ _glowmask.png), rendert jeden Quader mit dem
Bedrock-Boxlayout als gefuellte Texel-Polygone (Malerhaus-Algorithmus). Nur zur Kontrolle der Texturen.
"""
from __future__ import annotations

import json
import math
import sys

from PIL import Image, ImageDraw

from common import GEO, TEX

LIGHT = {"up": 1.0, "down": 0.55, "north": 0.85, "south": 0.7, "west": 0.75, "east": 0.6}


def rot(p, r_deg, pivot):
    x, y, z = (p[0] - pivot[0], p[1] - pivot[1], p[2] - pivot[2])
    rx, ry, rz = (math.radians(a) for a in r_deg)
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return (x + pivot[0], y + pivot[1], z + pivot[2])


def load(name):
    geo = json.loads((GEO / f"{name}.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
    bones = {b["name"]: b for b in geo["bones"]}
    tex = Image.open(TEX / "entity" / f"{name}.png").convert("RGBA")
    gp = TEX / "entity" / f"{name}_glowmask.png"
    glow = Image.open(gp).convert("RGBA") if gp.exists() else None
    return geo, bones, tex, glow


def chain(bones, bone):
    out = []
    while bone is not None:
        out.append(bone)
        bone = bones.get(bone.get("parent"))
    return out


def texels(name):
    return texels_of(*load(name))


def texels_of(geo, bones, tex, glow, pose=None):
    """Texel-Polygone aus Geo-Daten, Bones-Dict, Texturbild und optionaler Leuchtmaske (auch fuer Ruestungen)."""
    tw, th = geo["description"]["texture_width"], geo["description"]["texture_height"]
    out = []
    for bone in geo["bones"]:
        for cube in bone.get("cubes", []):
            ox, oy, oz = cube["origin"]
            w, h, d = cube["size"]
            inf = cube.get("inflate", 0)
            ox, oy, oz, w, h, d = ox - inf, oy - inf, oz - inf, w + 2 * inf, h + 2 * inf, d + 2 * inf
            uv = cube["uv"]
            if isinstance(uv, dict):
                continue
            u, v = uv
            iw, ih, idd = (int(round(a)) for a in cube["size"])

            def tr(p):
                if "rotation" in cube:
                    p = rot(p, cube["rotation"], cube.get("pivot", bone.get("pivot", [0, 0, 0])))
                for b in chain(bones, bone):
                    pv = b.get("pivot", [0, 0, 0])
                    extra = (pose or {}).get(b["name"], {})
                    sc = extra.get("scale")
                    if sc:
                        p = (pv[0] + (p[0] - pv[0]) * sc[0], pv[1] + (p[1] - pv[1]) * sc[1], pv[2] + (p[2] - pv[2]) * sc[2])
                    r0 = b.get("rotation", (0, 0, 0))
                    r1 = extra.get("rotation", (0, 0, 0))
                    total = (r0[0] + r1[0], r0[1] + r1[1], r0[2] + r1[2])
                    if any(total):
                        # GeckoLib dreht Bones in x umgekehrt zu Wuerfeln (im Spiel geprueft)
                        p = rot(p, (-total[0], total[1], total[2]), pv)
                    ps = extra.get("position")
                    if ps:
                        p = (p[0] + ps[0], p[1] + ps[1], p[2] + ps[2])
                return p

            faces = {
                "east": (u, v + idd, idd, ih, lambda s, t: (ox + w, oy + h - t * h, oz + d - s * d)),
                "north": (u + idd, v + idd, iw, ih, lambda s, t: (ox + w - s * w, oy + h - t * h, oz)),
                "west": (u + idd + iw, v + idd, idd, ih, lambda s, t: (ox, oy + h - t * h, oz + s * d)),
                "south": (u + idd + iw + idd, v + idd, iw, ih, lambda s, t: (ox + s * w, oy + h - t * h, oz + d)),
                "up": (u + idd, v, iw, idd, lambda s, t: (ox + w - s * w, oy + h, oz + d - t * d)),
                "down": (u + idd + iw, v, iw, idd, lambda s, t: (ox + w - s * w, oy, oz + t * d)),
            }
            for face, (fx, fy, fw, fh, corner) in faces.items():
                for j in range(fh):
                    for i in range(fw):
                        ii = fw - 1 - i if cube.get("mirror") else i
                        px, py = fx + ii, fy + j
                        if not (0 <= px < tex.width and 0 <= py < tex.height):
                            continue
                        c = tex.getpixel((px, py))
                        if c[3] < 40:
                            continue
                        g = glow.getpixel((px, py)) if glow is not None else (0, 0, 0, 0)
                        s0, s1, t0, t1 = i / fw, (i + 1) / fw, j / fh, (j + 1) / fh
                        quad = [tr(corner(s0, t0)), tr(corner(s1, t0)), tr(corner(s1, t1)), tr(corner(s0, t1))]
                        out.append((quad, c, LIGHT[face], g[3] > 128))
    return out


def render(items, yaw_deg, scale, size, cx, base_y):
    yaw = math.radians(yaw_deg)
    sy, cy = math.sin(yaw), math.cos(yaw)
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
    drawn.sort(key=lambda t: -t[0])
    for _d, pts, col, light, glow in drawn:
        f = 1.0 if glow else light
        dr.polygon(pts, fill=tuple(min(255, int(col[k] * f)) for k in range(3)))
    return img


def main():
    args = sys.argv[1:]
    out = args[0]
    yaws = [0, 90, 180, 45]
    scale = 6
    names = []
    i = 1
    while i < len(args):
        if args[i] == "--yaws":
            yaws = [int(v) for v in args[i + 1].split(",")]
            i += 2
        elif args[i] == "--scale":
            scale = int(args[i + 1])
            i += 2
        else:
            names.append(args[i])
            i += 1
    cell = 24 * scale
    sheet = Image.new("RGB", (cell * len(yaws), cell * len(names)), (58, 62, 78))
    for r, name in enumerate(names):
        items = texels(name)
        ys = [p[1] for q, *_ in items for p in q]
        height = max(ys) if ys else 24
        sc = min(scale, int(cell * 0.9 / max(height, 1)))
        for c, yaw in enumerate(yaws):
            sheet.paste(render(items, yaw, sc, (cell, cell), cell // 2, cell - 6), (c * cell, r * cell))
    sheet.save(out)
    print("gespeichert:", out)


if __name__ == "__main__":
    main()
