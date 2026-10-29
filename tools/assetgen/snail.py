"""Kristallschnecke: Modell (geo), Animationen und Textur. Aufruf: python tools/assetgen/snail.py

Schreibt geo/entity/crystal_snail.geo.json, animations/entity/crystal_snail.animation.json und
textures/entity/crystal_snail.png (+ _glowmask.png). Animationen: idle, walk, hide (haelt den letzten Frame).
"""
from __future__ import annotations

import json

from common import ANIM, BONE, CYAN, GEO, LILAC, SHARD, VIOLET, hx
from ent import Ent, M_crystal, art_face, overlay, tone
from uvlib import FACE_LIGHT, edge_light

NAME = "crystal_snail"


def geo():
    bones = [
        {"name": "root", "pivot": [0, 0, 0]},
        {"name": "body", "parent": "root", "pivot": [0, 0, 0], "cubes": [
            {"origin": [-2, 0, -7], "size": [4, 2, 13], "uv": [0, 0]}]},
        {"name": "head", "parent": "body", "pivot": [0, 1, -6], "cubes": [
            {"origin": [-1.5, 0.5, -9.5], "size": [3, 3, 3], "uv": [0, 16]}]},
        {"name": "eye_left", "parent": "head", "pivot": [1, 3.5, -8], "cubes": [
            {"origin": [0.5, 3.5, -8.5], "size": [1, 4, 1], "uv": [12, 16]}]},
        {"name": "eye_right", "parent": "head", "pivot": [-1, 3.5, -8], "cubes": [
            {"origin": [-1.5, 3.5, -8.5], "size": [1, 4, 1], "uv": [12, 16]}]},
        {"name": "shell", "parent": "body", "pivot": [0, 2, 1], "cubes": [
            {"origin": [-3, 2, -3], "size": [6, 7, 8], "uv": [0, 24]},
            {"origin": [-2, 9, -1], "size": [4, 2, 4], "uv": [28, 24]}]},
        {"name": "crystals", "parent": "shell", "pivot": [0, 11, 1], "cubes": [
            {"origin": [-1, 11, 0], "size": [2, 4, 2], "uv": [44, 24]},
            {"origin": [1.5, 10, 2], "size": [1, 3, 1], "pivot": [2, 10, 2.5], "rotation": [0, 0, -25], "uv": [52, 24]},
            {"origin": [-2.5, 10, -1], "size": [1, 3, 1], "pivot": [-2, 10, -0.5], "rotation": [15, 0, 25], "uv": [52, 24]}]},
    ]
    data = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": f"geometry.{NAME}", "texture_width": 64, "texture_height": 64,
                        "visible_bounds_width": 2, "visible_bounds_height": 1.5, "visible_bounds_offset": [0, 0.5, 0]},
        "bones": bones}]}
    (GEO / f"{NAME}.geo.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def anims():
    sway = "math.sin(query.anim_time * 120) * 8"
    data = {"format_version": "1.8.0", "animations": {
        "idle": {"loop": True, "animation_length": 3, "bones": {
            "eye_left": {"rotation": [sway, 0, "math.sin(query.anim_time * 90) * 6"]},
            "eye_right": {"rotation": ["math.sin(query.anim_time * 120 + 60) * 8", 0, "math.sin(query.anim_time * 90 + 45) * -6"]},
            "head": {"rotation": ["math.sin(query.anim_time * 60) * 3", 0, 0]}}},
        "walk": {"loop": True, "animation_length": 1.2, "bones": {
            "body": {"scale": [1, 1, "1 + math.sin(query.anim_time * 300) * 0.06"]},
            "head": {"position": [0, 0, "math.sin(query.anim_time * 300) * -0.4"]},
            "shell": {"rotation": [0, 0, "math.sin(query.anim_time * 300) * 2"]},
            "eye_left": {"rotation": ["-12 + math.sin(query.anim_time * 300) * 6", 0, 0]},
            "eye_right": {"rotation": ["-12 + math.sin(query.anim_time * 300 + 40) * 6", 0, 0]}}},
        "hide": {"loop": False, "animation_length": 0.4, "bones": {
            "head": {"position": {"0.0": [0, 0, 0], "0.4": [0, -0.5, 4]}, "scale": {"0.0": [1, 1, 1], "0.4": [0.2, 0.2, 0.2]}},
            "eye_left": {"scale": {"0.0": [1, 1, 1], "0.25": [1, 0.1, 1]}},
            "eye_right": {"scale": {"0.0": [1, 1, 1], "0.25": [1, 0.1, 1]}},
            "shell": {"position": {"0.0": [0, 0, 0], "0.4": [0, -0.5, -0.5]}},
            "body": {"scale": {"0.0": [1, 1, 1], "0.4": [1, 1, 0.75]}}}},
    }}
    (ANIM / f"{NAME}.animation.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


G = True
SKIN = [hx(c) for c in ("#3b2f52", "#5d4c7b", "#8b79ad", "#b9a9d6", "#e4daf3")]  # = BONE (Pergament-Lila)

SPIRAL = """
44444443
4ddddd43
4d4443d3
4d4dd3d3
4d43d3d3
4dddd3d3
32222222
"""


def M_skin(r, seed, bias=2.0):
    """Schneckenhaut: weiche, feuchte Flaeche mit hellem Glanz oben."""
    def p(face, fx, fy, fw, fh):
        t = tone(face, fx, fy, fw, fh, seed, cw=3, ch=2, lo=0.25, hi=0.75)
        idx = bias + 0.3 + FACE_LIGHT[face] * 1.1 + edge_light(fx, fy, fw, fh) * 0.6 + t * 0.6
        if face == "up" and fx == fw // 2 and fy % 3 != 0:
            return (CYAN[2], G)  # leuchtende Spur entlang des Rueckens
        return r[max(0, min(4, int(round(idx))))]
    return p


def M_shell(seed):
    """Haus: violette Kristallschale, Seiten mit Spirale, Rand unten leuchtend."""
    pal = {"4": SHARD[3], "3": SHARD[2], "2": SHARD[1], "d": VIOLET[1]}

    def base(face, fx, fy, fw, fh):
        t = tone(face, fx, fy, fw, fh, seed, cw=2, ch=2)
        idx = 2.2 + FACE_LIGHT[face] * 1.1 + edge_light(fx, fy, fw, fh) * 0.8 + t * 0.6
        if face in ("north", "south") and fy == fh - 1:
            return (CYAN[3], G)
        if face == "up" and (fx + fy) % 4 == 0:
            return SHARD[3]
        return SHARD[max(0, min(4, int(round(idx))))]

    return art_face({"east": SPIRAL, "west": SPIRAL}, pal, base)


def texture():
    e = Ent(NAME)
    skin = M_skin(SKIN, 1301)
    e.paint("body", skin)
    face = art_face("""
...
e.e
.m.
""", {"e": VIOLET[0], "m": SKIN[1]}, M_skin(SKIN, 1303, bias=2.3))
    e.paint("head", face)
    stalk = overlay(M_skin(SKIN, 1305, bias=2.2), lambda f, x, y, w, h: (CYAN[4], G) if (y == 0 and f != "down") or f == "up" else None)
    e.paint("eye_left", stalk)
    e.paint("eye_right", stalk)
    e.paint("shell", M_shell(1307))
    e.paint("crystals", M_crystal(CYAN, 1309, glow_from=1.5))
    e.save()


def main():
    geo()
    anims()
    texture()
    print("snail: Kristallschnecke (Geo, Animationen, Textur, Glowmask)")


if __name__ == "__main__":
    main()
