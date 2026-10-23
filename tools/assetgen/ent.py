"""Entity-Texturen: liest die .geo.json und malt exakt in deren UV-Layout.

Damit bleiben Modell und Textur immer synchron. Jeder Quader (Cube) wird ueber seinen Bone angesprochen.
"""
from __future__ import annotations

import json
import math

from clean import ordered
from common import (CLEAR, GEO, TEX, Tex, clamp, hx, mix, nz, ramp, vnoise)
from uvlib import FACE_LIGHT, box_extent, box_rects, edge_light, paint_box

FACE_ID = {"up": 1, "down": 2, "east": 3, "north": 4, "west": 5, "south": 6}


class Ent:
    def __init__(self, name: str, geo_name: str | None = None):
        self.name = name
        self.path = GEO / f"{geo_name or name}.geo.json"
        self.data = json.loads(self.path.read_text(encoding="utf-8"))
        g = self.data["minecraft:geometry"][0]
        self.geo = g
        self.W = g["description"]["texture_width"]
        self.H = g["description"]["texture_height"]
        self.tex = Tex(self.W, self.H)
        self.glow = Tex(self.W, self.H)
        self.cubes = {}
        for b in g["bones"]:
            self.cubes[b["name"]] = b.get("cubes", [])

    # -------------------------------------------------- Malen
    def cube_dims(self, c):
        w, h, d = c["size"]
        return c["uv"][0], c["uv"][1], w, h, d

    def paint(self, bone: str, painter, only=None):
        """Malt alle (oder nur bestimmte) Quader eines Bones."""
        cubes = self.cubes[bone]
        for i, c in enumerate(cubes):
            if only is not None and i not in (only if isinstance(only, (list, tuple, set)) else [only]):
                continue
            u, v, w, h, d = self.cube_dims(c)
            paint_box(self.tex, u, v, w, h, d, painter, self.glow)

    def paint_all(self, painter, skip=()):
        for bone in self.cubes:
            if bone not in skip:
                self.paint(bone, painter)

    def save(self, with_glow=True):
        self.tex.save(TEX / "entity" / f"{self.name}.png")
        if with_glow:
            self.glow.save(TEX / "entity" / f"{self.name}_glowmask.png")

    # -------------------------------------------------- Kontrolle
    def coverage_report(self):
        """Gibt zurueck: welche Pixel der UV-Layouts ueberlappen (verschiedene Quader)."""
        owner = {}
        clashes = []
        for bone, cubes in self.cubes.items():
            for i, c in enumerate(cubes):
                u, v, w, h, d = self.cube_dims(c)
                key = (c["uv"][0], c["uv"][1], tuple(c["size"]))
                for face, (x0, y0, fw, fh) in box_rects(u, v, w, h, d).items():
                    for y in range(y0, y0 + fh):
                        for x in range(x0, x0 + fw):
                            o = owner.get((x, y))
                            if o is not None and o[0] != key:
                                clashes.append(((x, y), o[1], bone))
                            owner[(x, y)] = (key, bone)
        return clashes


# ====================================================================== Materialien (Stufe 2, neu)
def _lit(face, fx, fy, fw, fh, bias=2.0):
    return bias + 0.35 + FACE_LIGHT[face] * 1.35 + edge_light(fx, fy, fw, fh) * 1.2


def tone(face, fx, fy, fw, fh, seed, cw=0, ch=0, lo=0.2, hi=0.8, shift_rows=True):
    """Zusammenhaengendes Cluster-Rauschen: -1 (dunkler), 0, +1 (heller) in Bloecken von cw x ch Pixeln.

    Die Blockgroesse waechst mit der Flaechengroesse (kleine Mobs 2x2, grosse Bosse 3x3 oder 4x4). Jede zweite Blockreihe
    ist versetzt, damit kein Gitter sichtbar wird. So entstehen Vanilla-artige Flecken statt Pixelrauschen.
    """
    if cw == 0:
        cw = 2 if fw < 16 else (3 if fw < 40 else 4)
    if ch == 0:
        ch = 2 if fh < 16 else (3 if fh < 40 else 4)
    off = FACE_ID[face] * 17
    cy = fy // ch
    cx = (fx + ((cy % 2) * (cw // 2) if shift_rows else 0) + off) // cw
    h = nz(cx, cy, seed)
    return 1 if h > hi else (-1 if h < lo else 0)


def M_stone(r, seed, rough=0.5, crack=None, crack_glow=False, bias=2.0, strata=False, crack_w=0.028):
    """Stein: ruhiger Grundton, Cluster-Flecken, optional waagerechte Schichten und lange, zusammenhaengende Risse."""
    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 13
        if strata:
            t = tone(face, fx, fy, fw, fh, seed, cw=max(4, fw // 3), ch=1)
        else:
            t = tone(face, fx, fy, fw, fh, seed)
        idx = _lit(face, fx, fy, fw, fh, bias) + t * (0.55 + rough)
        if crack is not None:
            ridge = abs(vnoise(fx / 6.0 + off, fy / 6.0, seed + 9) - 0.5)
            if ridge < crack_w * 0.55 and 0 < fx < fw - 1 and 0 < fy < fh - 1:
                return (crack, True) if crack_glow else crack
        return ramp(r, idx)
    return p


def M_crystal(r, seed, glow_from=2.0):
    """Facettenkristall: diagonale Baender, helle Kanten; leuchtet ab einer Helligkeit."""
    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 5
        band = ((fx + fy * 2 + off) // 2) % 3
        idx = 0.9 + band * 0.8 + FACE_LIGHT[face] * 0.9
        if fx == 0 or fy == 0:
            idx += 1.0
        if fx == fw - 1 or fy == fh - 1:
            idx -= 0.6
        col = ramp(r, idx)
        return (col, idx >= glow_from)
    return p


def M_fur(r, seed, bias=2.0):
    """Fell: senkrechte Straehnen (2 Pixel breit) mit helleren Spitzen alle drei Zeilen."""
    def p(face, fx, fy, fw, fh):
        t = tone(face, fx, fy, fw, fh, seed, cw=2, ch=4, shift_rows=False)
        idx = bias + 0.35 + FACE_LIGHT[face] * 1.2 + t * 0.9
        if fy % 3 == 0:
            idx += 0.5
        return ramp(r, idx)
    return p


def M_cloth(r, seed, bias=2.0):
    """Stoff: weiche senkrechte Falten."""
    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 7
        fold = math.sin((fx + off) * 1.7) * 0.5
        idx = bias + 0.35 + FACE_LIGHT[face] * 1.1 + fold + edge_light(fx, fy, fw, fh) * 0.6
        return ramp(r, idx)
    return p


def M_chitin(r, seed, bias=2.0):
    """Panzerplatten in 3x3-Schuppen, versetzt."""
    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 3
        row = fy // 3
        cx = (fx + (row % 2) * 1 + off) % 4
        cy = fy % 3
        idx = bias + 0.35 + FACE_LIGHT[face] * 1.2 + (nz(fx // 2 + off, row, seed) - 0.5) * 0.45
        if cx == 0 or cy == 2:
            idx -= 1.0
        elif cx == 1 and cy == 0:
            idx += 0.7
        return ramp(r, idx)
    return p


def M_plate(r, seed, rivets=None, bias=2.0, seam_every=0):
    """Plattenmetall: Kantenlicht, Cluster-Toene, Nieten und Fugen."""
    def p(face, fx, fy, fw, fh):
        t = tone(face, fx, fy, fw, fh, seed, lo=0.12, hi=0.88)
        idx = _lit(face, fx, fy, fw, fh, bias) + t * 0.7
        if seam_every and fy % seam_every == seam_every - 1:
            idx -= 1.0
        if rivets and fx % rivets == 1 and fy % rivets == 1 and 0 < fx < fw - 1:
            idx += 1.2
        return ramp(r, idx)
    return p


def M_flat(color, glow=False):
    return lambda face, fx, fy, fw, fh: (color, glow)


def M_membrane(r, seed, vein=None, vein_glow=False, bias=1.6):
    """Haeutige Flaeche mit weichen Toenen und radial verlaufenden Adern."""
    def p(face, fx, fy, fw, fh):
        t = tone(face, fx, fy, fw, fh, seed, cw=3, ch=3, lo=0.25, hi=0.75)
        idx = bias + 0.3 + FACE_LIGHT[face] * 0.9 + t * 0.7
        col = ramp(r, idx)
        if vein is not None:
            if (fx * 2 + fy) % 9 == 0 or (fx - fy * 2) % 13 == 0:
                return (vein, vein_glow)
        return col
    return p


# ====================================================================== Kompositoren
def art_face(art: str, palette: dict, base):
    """Legt ASCII-Pixelkunst auf passende Flaechen. `art` ist ein Dict {face: ascii} oder ein ASCII-String
    (gilt dann fuer 'north'). '.' laesst das Basismaterial stehen.
    Paletteneintraege: Farbe oder (Farbe, leuchtet)."""
    arts = art if isinstance(art, dict) else {"north": art}
    parsed = {}
    for face, a in arts.items():
        rows = a.strip("\n").split("\n")
        parsed[face] = rows

    def p(face, fx, fy, fw, fh):
        rows = parsed.get(face)
        if rows is not None and fy < len(rows) and fx < len(rows[fy]):
            ch = rows[fy][fx]
            if ch != ".":
                v = palette[ch]
                return v
        return base(face, fx, fy, fw, fh)
    return p


def overlay(base, fn):
    """fn(face, fx, fy, fw, fh) -> Farbe/None. None = Basismaterial."""
    def p(face, fx, fy, fw, fh):
        r = fn(face, fx, fy, fw, fh)
        if r is not None:
            return r
        return base(face, fx, fy, fw, fh)
    return p


def shaded(painter, factor_by_face=None):
    return painter


def darker(c, f=0.8):
    return (clamp(int(c[0] * f), 0, 255), clamp(int(c[1] * f), 0, 255), clamp(int(c[2] * f), 0, 255), c[3])
