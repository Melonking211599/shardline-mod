"""Vorschau: Texturen auf Vanilla-Modellen (Schaf, Schwein, Kuh, Dorfbewohner, Zombie), auch mit Vlies- und Kragen-Ebene.

Aufruf: python tools/assetgen/preview_vanilla.py <ausgabe.png> [--scale 7]
Die Quader sind die Vanilla-Modelle in Bedrock-Koordinaten (y nach oben, Vorderseite = -Z). Je Wesen eine Zeile:
wild (4 Blickwinkel) und gezaehmt mit Kragen (2 Blickwinkel). Nur zur Kontrolle der Texturen.
"""
from __future__ import annotations

import sys

from PIL import Image

from common import TEX
from preview_geo import LIGHT, render, rot


def c(origin, size, uv, rotation=None, pivot=None, inflate=0.0, mirror=False):
    return {"origin": origin, "size": size, "uv": uv, "rotation": rotation, "pivot": pivot, "inflate": inflate, "mirror": mirror}


def quad_legs(y0, h, uv=(0, 16), inflate=0.0, front=-7, back=5):
    return [c([x, y0, z], [4, h, 4], list(uv), inflate=inflate) for x in (-5, 1) for z in (front, back)]


BODY90 = [-90, 0, 0]
SHEEP = [c([-3, 16, -14], [6, 6, 8], [0, 0]), c([-4, 13, -5], [8, 16, 6], [28, 8], BODY90, [0, 19, 2])] + quad_legs(0, 12)
SHEEP_FUR = ([c([-3, 16, -12], [6, 6, 6], [0, 0], inflate=0.6), c([-4, 13, -5], [8, 16, 6], [28, 8], BODY90, [0, 19, 2], 1.75)]
             + quad_legs(6, 6, inflate=0.5))
PIG = ([c([-4, 8, -14], [8, 8, 8], [0, 0]), c([-2, 9, -15], [4, 3, 1], [16, 16]), c([-5, 7, -5], [10, 16, 8], [28, 8], BODY90, [0, 13, 2])]
       + quad_legs(0, 6))
COW = [c([-4, 16, -14], [8, 8, 6], [0, 0]), c([-6, 11, -5], [12, 18, 10], [18, 4], BODY90, [0, 19, 2])] + quad_legs(0, 12, back=6)
ARMS = [-43, 0, 0]
VILLAGER = [c([-4, 24, -4], [8, 10, 8], [0, 0]), c([-4, 24, -4], [8, 10, 8], [32, 0], inflate=0.51), c([-1, 23, -6], [2, 4, 2], [24, 0]),
            c([-4, 12, -3], [8, 12, 6], [16, 20]), c([-4, 4, -3], [8, 20, 6], [0, 38], inflate=0.5),
            c([-8, 15, -3], [4, 8, 4], [44, 22], ARMS, [0, 21, -1]), c([4, 15, -3], [4, 8, 4], [44, 22], ARMS, [0, 21, -1], mirror=True),
            c([-4, 15, -3], [8, 4, 4], [40, 38], ARMS, [0, 21, -1]),
            c([-4, 0, -2], [4, 12, 4], [0, 22]), c([0, 0, -2], [4, 12, 4], [0, 22], mirror=True)]
ZOMBIE_ARMS = [90, 0, 0]
ZOMBIE = [c([-4, 24, -4], [8, 8, 8], [0, 0]), c([-4, 24, -4], [8, 8, 8], [32, 0], inflate=0.5), c([-4, 12, -2], [8, 12, 4], [16, 16]),
          c([-4, 12, -2], [8, 12, 4], [16, 32], inflate=0.25),
          c([-8, 12, -2], [4, 12, 4], [40, 16], ZOMBIE_ARMS, [-5, 22, 0]), c([4, 12, -2], [4, 12, 4], [32, 48], ZOMBIE_ARMS, [5, 22, 0]),
          c([-4, 0, -2], [4, 12, 4], [0, 16]), c([0, 0, -2], [4, 12, 4], [16, 48])]

# Wesen: (Name, [(Quader, Textur, Glowmask)], [(Quader, Kragen-Textur)])
MOBS = [
    ("crystallized_sheep", [(SHEEP, "crystallized_sheep"), (SHEEP_FUR, "crystallized_sheep_fur")], (SHEEP_FUR, "crystallized_sheep_collar")),
    ("crystallized_pig", [(PIG, "crystallized_pig")], (PIG, "crystallized_pig_collar")),
    ("crystallized_cow", [(COW, "crystallized_cow")], (COW, "crystallized_cow_collar")),
    ("crystallized_villager", [(VILLAGER, "crystallized_villager")], (VILLAGER, "crystallized_villager_collar")),
    ("shard_zombie", [(ZOMBIE, "shard_zombie")], (ZOMBIE, "shard_zombie_collar")),
]


def image(name):
    path = TEX / "entity" / f"{name}.png"
    return Image.open(path).convert("RGBA") if path.exists() else None


def texels(cubes, tex, glow):
    out = []
    for cube in cubes:
        ox, oy, oz = cube["origin"]
        w, h, d = cube["size"]
        inf = cube["inflate"]
        ox, oy, oz, w, h, d = ox - inf, oy - inf, oz - inf, w + 2 * inf, h + 2 * inf, d + 2 * inf
        u, v = cube["uv"]
        iw, ih, idd = cube["size"]

        def tr(p, cube=cube):
            return rot(p, cube["rotation"], cube["pivot"]) if cube["rotation"] else p

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
                    ii = fw - 1 - i if cube["mirror"] else i
                    px, py = fx + ii, fy + j
                    if not (0 <= px < tex.width and 0 <= py < tex.height):
                        continue
                    col = tex.getpixel((px, py))
                    if col[3] < 40:
                        continue
                    g = glow.getpixel((px, py)) if glow is not None else (0, 0, 0, 0)
                    s0, s1, t0, t1 = i / fw, (i + 1) / fw, j / fh, (j + 1) / fh
                    quad = [tr(corner(s0, t0)), tr(corner(s1, t0)), tr(corner(s1, t1)), tr(corner(s0, t1))]
                    out.append((quad, col, LIGHT[face], g[3] > 128))
    return out


def main():
    args = sys.argv[1:]
    out = args[0]
    scale = int(args[args.index("--scale") + 1]) if "--scale" in args else 7
    yaws_wild, yaws_tamed = [20, 110, 200, 290], [20, 200]
    cell = 40 * scale
    sheet = Image.new("RGB", (cell * (len(yaws_wild) + len(yaws_tamed)), cell * len(MOBS)), (58, 62, 78))
    for r, (name, layers, collar) in enumerate(MOBS):
        items = []
        for cubes, tex_name in layers:
            tex = image(tex_name)
            if tex is not None:
                items += texels(cubes, tex, image(tex_name + "_glowmask"))
        tamed = list(items)
        ctex = image(collar[1])
        if ctex is not None:
            tamed += texels(collar[0], ctex, None)
        for col, yaw in enumerate(yaws_wild + yaws_tamed):
            src = items if col < len(yaws_wild) else tamed
            sheet.paste(render(src, yaw, scale, (cell, cell), cell // 2, cell - 10), (col * cell, r * cell))
    sheet.save(out)
    print("gespeichert:", out)


if __name__ == "__main__":
    main()
