"""Geometrie-Helfer: UV-Platzverwaltung, Reparatur ueberlappender UVs und zusaetzliche Detail-Quader.

Alle Eingriffe sind idempotent. Bestehende Bone-Namen und Pivots bleiben unveraendert, damit die
Animationen und der Java-Code weiter passen. Neue Bones tragen das Praefix `detail_` und haengen an
bestehenden Bones.
"""
from __future__ import annotations

import json
from pathlib import Path

from common import GEO
from uvlib import box_extent, box_rects


def load(name: str):
    path = GEO / f"{name}.geo.json"
    return path, json.loads(path.read_text(encoding="utf-8"))


def save(path: Path, data):
    path.write_text(json.dumps(data, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")


def occupied(geo, skip=lambda bone, cube: False):
    W = geo["description"]["texture_width"]
    H = geo["description"]["texture_height"]
    occ = [[False] * W for _ in range(H)]
    for b in geo["bones"]:
        for c in b.get("cubes", []):
            if skip(b["name"], c):
                continue
            w, h, d = c["size"]
            for (x0, y0, fw, fh) in box_rects(c["uv"][0], c["uv"][1], w, h, d).values():
                for y in range(y0, y0 + fh):
                    for x in range(x0, x0 + fw):
                        if 0 <= x < W and 0 <= y < H:
                            occ[y][x] = True
    return occ


def mark(occ, u, v, w, h, d):
    for (x0, y0, fw, fh) in box_rects(u, v, w, h, d).values():
        for y in range(y0, y0 + fh):
            for x in range(x0, x0 + fw):
                if 0 <= y < len(occ) and 0 <= x < len(occ[0]):
                    occ[y][x] = True


def find_free(occ, w, h, d, start=(0, 0)):
    """Erste freie Stelle (u, v) fuer einen Box-UV-Block; prueft jede Flaeche einzeln."""
    W, H = len(occ[0]), len(occ)
    ew, eh = int(round(2 * (w + d))), int(round(d + h))
    for v in range(start[1], H - eh + 1):
        for u in range(start[0] if v == start[1] else 0, W - ew + 1):
            ok = True
            for (x0, y0, fw, fh) in box_rects(u, v, w, h, d).values():
                if any(occ[y][x] for y in range(y0, y0 + fh) for x in range(x0, x0 + fw)):
                    ok = False
                    break
            if ok:
                return u, v
    raise RuntimeError(f"Kein freier UV-Platz fuer {w}x{h}x{d}")


def relocate(name: str, bone: str, cube_indices):
    """Verschiebt Quader eines Bones an freie UV-Plaetze (Quader mit gleichem alten UV bleiben gemeinsam)."""
    path, data = load(name)
    geo = data["minecraft:geometry"][0]
    b = next(x for x in geo["bones"] if x["name"] == bone)
    targets = [b["cubes"][i] for i in cube_indices]
    occ = occupied(geo, skip=lambda bn, c: any(c is t for t in targets))
    # gleiche (alte uv) -> gleicher neuer uv, es zaehlt der groesste Quader der Gruppe
    groups = {}
    for c in targets:
        groups.setdefault(tuple(c["uv"]), []).append(c)
    for old, cubes in sorted(groups.items(), key=lambda kv: -max(c["size"][0] * c["size"][1] * c["size"][2] for c in kv[1])):
        big = max(cubes, key=lambda c: (c["size"][0] + c["size"][2]) * 2 * (c["size"][1] + c["size"][2]))
        w = max(c["size"][0] for c in cubes)
        h = max(c["size"][1] for c in cubes)
        d = max(c["size"][2] for c in cubes)
        u, v = find_free(occ, w, h, d)
        mark(occ, u, v, w, h, d)
        for c in cubes:
            c["uv"] = [u, v]
    save(path, data)


def add_details(name: str, parent: str, bone_name: str, pivot, cubes):
    """Fuegt (oder ersetzt) einen Detail-Bone mit Quadern ein. UVs werden automatisch vergeben.

    cubes: Liste von Dicts mit origin, size und optional rotation/pivot/inflate. 'uv' wird gesetzt.
    """
    path, data = load(name)
    geo = data["minecraft:geometry"][0]
    geo["bones"] = [b for b in geo["bones"] if b["name"] != bone_name]
    occ = occupied(geo)
    new_cubes = []
    shared = {}
    for c in cubes:
        c = dict(c)
        key = ("share", c.pop("share", None))
        w, h, d = c["size"]
        if key[1] is not None and key in shared:
            c["uv"] = shared[key]
        else:
            u, v = find_free(occ, w, h, d)
            mark(occ, u, v, w, h, d)
            c["uv"] = [u, v]
            if key[1] is not None:
                shared[key] = [u, v]
        new_cubes.append(c)
    geo["bones"].append({"name": bone_name, "parent": parent, "pivot": list(pivot), "cubes": new_cubes})
    save(path, data)
