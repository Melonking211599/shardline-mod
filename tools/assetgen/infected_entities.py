"""Infizierte Wesen: Virusherz (GeckoLib), Shard-Zombie (Vanilla-Zombie-UV), kristallisierte Kuh (Vanilla-Kuh-UV).

Aufruf: python tools/assetgen/infected_entities.py

* Virusherz: Das vorhandene Modell bekommt zusaetzliche Detail-Bones (`detail_*`): sechs Tentakel-Adern, Kristall-
  Auswuechse und Aorta-Stuempfe am Kern. Bestehende Bones, Pivots und die Clips idle/walk/pulse/summon bleiben
  unveraendert (idle/walk bekommen nur zusaetzliche Spuren fuer die NEUEN Bones). Neue Clips: spawn, attack_tendrils,
  summon_infected, hurt, death.
* Shard-Zombie / Kuh: nur Texturen. Sie liegen exakt im Vanilla-Layout (Zombie 64x64, Kuh 64x32), die Modelle sind die
  Vanilla-Modelle. Die "herauswachsenden Splitter" sind deshalb aufgemalt (Hut-Ebene beim Zombie), keine Geometrie.
* Zu jeder Textur gibt es eine _glowmask (deckend = leuchtet).
"""
from __future__ import annotations

import json
import math

import geofix
from clean import ordered
from common import ANIM, GEO, TEX, VGLOW, VIRUS, Tex, clamp, hx, mix, nz, ramp, vnoise
from ent import FACE_ID, Ent
from uvlib import FACE_LIGHT, edge_light, paint_box

# ------------------------------------------------------------------ Paletten (Viruspalette + Hautfarben)
CRY = [hx(c) for c in ("#3a0c3a", "#7a1a68", "#c42c9e", "#ff6fdc", "#ffd0f4")]       # Virus-Kristall
HEART = [hx(c) for c in ("#17051a", "#2f0a33", "#561048", "#841a66", "#b3267f")]      # krankes Herzfleisch
GROUND = [hx(c) for c in ("#0b030c", "#16071a", "#240c2a", "#391340", "#52205a")]     # verseuchter Boden
ZSKIN = [hx(c) for c in ("#1c1424", "#2c2138", "#41334f", "#5a4a6c", "#77668a")]
ZSHIRT = [hx(c) for c in ("#0c1c26", "#12303c", "#1b4a58", "#27687a", "#3a8a9a")]
ZPANTS = [hx(c) for c in ("#120d2b", "#1c1442", "#2a2060", "#3b2f85", "#5446a8")]
CHIDE = [hx(c) for c in ("#14101c", "#201a2c", "#322a42", "#463c5c", "#5d5078")]
CPATCH = [hx(c) for c in ("#6b5f84", "#8b7fa6", "#aea3c6", "#cfc6e2", "#ece6f6")]
CSNOUT = [hx(c) for c in ("#3b2a3c", "#5a4258", "#7c6076", "#9d8296", "#bda5b4")]
BONES_IDLE = 1  # Platzhalter, damit Linter nicht meckert


# ====================================================================== Virusherz: Geometrie
TENDRILS = 6


def _tendril_cubes(theta: float):
    """Ein Tentakel in lokalen Koordinaten (+z nach aussen), um die Y-Achse auf `theta` gedreht."""
    rot = {"pivot": [0, 3, 0], "rotation": [0, theta, 0]}
    return [
        dict(origin=[-1, 3, 6], size=[2, 2, 9], **rot),       # Wurzelstrang am Boden
        dict(origin=[-1, 3, 13], size=[2, 7, 2], **rot),      # steigt auf
        dict(origin=[-0.5, 9, 10], size=[1, 1, 4], **rot),    # kruemmt sich zurueck (Haken)
        dict(origin=[-1, 8.5, 8.5], size=[2, 2, 2], **rot),   # leuchtende Spitze
    ]


def heart_geometry():
    n = "virus_heart"
    # Kristall-Auswuechse und Aorta am Kern (laufen mit dem Herzschlag mit, weil Kind von `core`)
    geofix.add_details(n, "core", "detail_growths", [0, 13, 0], [
        {"origin": [-10, 7, -3], "size": [4, 9, 5], "pivot": [-8, 8, 0], "rotation": [0, 0, 12]},
        {"origin": [6, 6, -2], "size": [4, 10, 5], "pivot": [8, 8, 0], "rotation": [0, 0, -14]},
        {"origin": [-3, 9, 6], "size": [6, 10, 4], "pivot": [0, 10, 7], "rotation": [-12, 0, 0]},
        {"origin": [-9, 11, -9], "size": [3, 8, 3], "pivot": [-8, 11, -8], "rotation": [20, 0, -18], "share": "frontcry"},
        {"origin": [6, 11, -9], "size": [3, 8, 3], "pivot": [7, 11, -8], "rotation": [20, 0, 18], "share": "frontcry"},
        {"origin": [-6, 19.5, -3], "size": [3, 4, 3], "share": "aorta"},
        {"origin": [3, 19.5, 1], "size": [3, 4, 3], "share": "aorta"},
    ])
    # sechs Tentakel, jeder ein eigener Bone, damit sie einzeln schwanken koennen
    path, data = geofix.load(n)
    geo = data["minecraft:geometry"][0]
    geo["bones"] = [b for b in geo["bones"] if not b["name"].startswith("detail_tendril_")]
    geofix.save(path, data)
    geofix.add_details(n, "roots", "detail_tendril_0", [0, 3, 0], _tendril_cubes(30.0))
    path, data = geofix.load(n)
    geo = data["minecraft:geometry"][0]
    first = next(b for b in geo["bones"] if b["name"] == "detail_tendril_0")
    for k in range(1, TENDRILS):
        theta = 30.0 + 60.0 * k
        cubes = _tendril_cubes(theta)
        for c, f in zip(cubes, first["cubes"]):
            c["uv"] = list(f["uv"])        # alle Tentakel teilen sich dieselbe Textur
        geo["bones"].append({"name": f"detail_tendril_{k}", "parent": "roots", "pivot": [0, 3, 0], "cubes": cubes})
    geofix.save(path, data)


# ====================================================================== Virusherz: Animationen
def _kf(**kw):
    return kw


def heart_animations():
    p = ANIM / "virus_heart.animation.json"
    data = json.loads(p.read_text(encoding="utf-8"))
    anims = data["animations"]
    tend = [f"detail_tendril_{k}" for k in range(TENDRILS)]

    # --- idle/walk: nur Spuren fuer die neuen Bones ergaenzen
    for clip in ("idle", "walk"):
        bones = anims[clip]["bones"]
        for k, b in enumerate(tend):
            if b in bones:
                continue
            bones[b] = {
                "rotation": [f"math.sin(query.anim_time * 240 + {k * 60}) * 4",
                             f"math.sin(query.anim_time * 240 + {k * 95}) * 6",
                             f"math.sin(query.anim_time * 240 + {k * 45}) * 4"],
                "scale": [1, f"1 + math.max(0, math.sin(query.anim_time * 480 - 40)) * 0.07", 1],
            }

    def all_tendrils(make):
        return {b: make(k) for k, b in enumerate(tend)}

    # --- spawn: Herz waechst aus dem Boden, Tentakel schiessen hoch
    anims["spawn"] = {
        "animation_length": 1.6,
        "bones": {
            "core": {"scale": {"0.0": [0.05, 0.05, 0.05], "0.7": [1.3, 0.6, 1.3], "1.1": [0.9, 1.2, 0.9], "1.6": [1, 1, 1]}},
            "roots": {"scale": {"0.0": [0.2, 1, 0.2], "1.0": [1.1, 1, 1.1], "1.6": [1, 1, 1]}},
            "spikes": {"scale": {"0.0": [0, 0, 0], "0.9": [0, 0, 0], "1.3": [1.2, 1.2, 1.2], "1.6": [1, 1, 1]},
                       "position": {"0.0": [0, -10, 0], "1.3": [0, 2, 0], "1.6": [0, 0, 0]}},
            **all_tendrils(lambda k: {"scale": {"0.0": [1, 0, 1], f"{0.5 + k * 0.08:.2f}": [1, 0.1, 1],
                                                f"{1.2 + k * 0.04:.2f}": [1, 1.25, 1], "1.6": [1, 1, 1]}}),
        },
    }
    # --- attack_tendrils: Tentakel peitschen nach vorn
    anims["attack_tendrils"] = {
        "animation_length": 1.2,
        "bones": {
            "core": {"scale": {"0.0": [1, 1, 1], "0.2": [0.9, 0.9, 0.9], "0.4": [1.2, 1.15, 1.2], "1.2": [1, 1, 1]}},
            **all_tendrils(lambda k: {
                "rotation": {"0.0": [0, 0, 0], "0.25": [-35 if k % 2 else 35, 0, 0],
                             "0.5": [25 if k % 2 else -25, 0, 0], "0.9": [0, 0, 0]},
                "scale": {"0.0": [1, 1, 1], "0.25": [1.3, 1.5, 1.3], "0.9": [1, 1, 1]}}),
        },
    }
    # --- summon_infected: dreifacher Herzschlag, Dornen kreisen, Tentakel strecken sich
    anims["summon_infected"] = {
        "animation_length": 2.0,
        "bones": {
            "core": {"scale": {"0.0": [1, 1, 1], "0.3": [1.25, 1.2, 1.25], "0.6": [1, 1, 1], "0.9": [1.25, 1.2, 1.25],
                               "1.2": [1, 1, 1], "1.5": [1.35, 1.3, 1.35], "2.0": [1, 1, 1]}},
            "spikes": {"rotation": {"0.0": [0, 0, 0], "2.0": [0, 720, 0]},
                       "position": {"0.0": [0, 0, 0], "1.0": [0, 5, 0], "2.0": [0, 0, 0]}},
            "roots": {"scale": {"0.0": [1, 1, 1], "1.0": [1.3, 1, 1.3], "2.0": [1, 1, 1]}},
            **all_tendrils(lambda k: {"scale": {"0.0": [1, 1, 1], "1.0": [1, 1.4, 1], "2.0": [1, 1, 1]}}),
        },
    }
    # --- hurt: kurzes Zusammenzucken
    anims["hurt"] = {
        "animation_length": 0.4,
        "bones": {
            "core": {"scale": {"0.0": [1, 1, 1], "0.1": [1.12, 0.86, 1.12], "0.25": [0.94, 1.06, 0.94], "0.4": [1, 1, 1]}},
            "spikes": {"rotation": {"0.0": [0, 0, 0], "0.1": [0, 0, 8], "0.2": [0, 0, -6], "0.4": [0, 0, 0]}},
            **all_tendrils(lambda k: {"rotation": {"0.0": [0, 0, 0], "0.1": [12 if k % 2 else -12, 0, 0], "0.4": [0, 0, 0]}}),
        },
    }
    # --- death: schwillt an, platzt (Scale -> 0), Dornen fliegen auseinander, Tentakel erschlaffen
    anims["death"] = {
        "loop": "hold_on_last_frame",
        "animation_length": 2.2,
        "bones": {
            "core": {"scale": {"0.0": [1, 1, 1], "0.6": [1.15, 1.1, 1.15], "1.2": [0.9, 0.9, 0.9],
                               "1.7": [1.7, 1.6, 1.7], "1.8": [0, 0, 0], "2.2": [0, 0, 0]}},
            "spikes": {"position": {"0.0": [0, 0, 0], "1.7": [0, 0, 0], "2.2": [0, 18, 0]},
                       "scale": {"0.0": [1, 1, 1], "1.7": [1.2, 1.2, 1.2], "2.2": [0, 0, 0]},
                       "rotation": {"0.0": [0, 0, 0], "2.2": [0, 540, 0]}},
            "roots": {"scale": {"0.0": [1, 1, 1], "1.7": [1, 1, 1], "2.2": [0.6, 0.4, 0.6]}},
            **all_tendrils(lambda k: {"rotation": {"0.0": [0, 0, 0], "1.2": [-60 if k % 2 else 60, 0, 0]},
                                      "scale": {"0.0": [1, 1, 1], "1.2": [1, 0.35, 1], "2.2": [1, 0, 1]}}),
        },
    }
    p.write_text(json.dumps(data, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")


# ====================================================================== Malwerkzeuge
def flesh(r, seed, vein_w=0.045, bias=2.0, vein_col=None, fibers=True):
    """Organisches Fleisch: weiche Flecken, Muskelfasern und zusammenhaengende, leuchtende Adern (Kammlinien)."""
    vc = vein_col or VGLOW[1]

    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 13
        idx = bias + FACE_LIGHT[face] * 0.7 + edge_light(fx, fy, fw, fh) * 0.6
        idx += (vnoise(fx / 3.5 + off, fy / 3.5, seed) - 0.5) * 1.0
        if fibers:
            idx += math.sin((fx * 0.9 + fy * 0.4 + off) * 1.1) * 0.35
        ridge = abs(vnoise(fx / 3.2 + off, fy / 3.2, seed + 9) - 0.5)
        if ridge < vein_w and 0 < fx < fw - 1 and 0 < fy < fh - 1:
            return (VGLOW[2] if (fx * 3 + fy * 5) % 11 == 0 else vc, True)
        return ramp(r, idx)
    return p


def crystal(r, seed, glow_from=2.6):
    """Facetten-Kristall: drei senkrechte Facetten (links hell, Mitte mittel, rechts dunkel), helle Kante oben links,
    dunkle unten rechts; die Spitze (obere Haelfte) leuchtet. Keine Raster, nur klare Flaechen."""
    def p(face, fx, fy, fw, fh):
        col_f = (fx + FACE_ID[face] * 2) % 3
        idx = (3.2, 2.2, 1.2)[col_f] + FACE_LIGHT[face] * 0.5
        if fy < fh * 0.35:
            idx += 0.7
        if fx == 0 or fy == 0:
            idx += 0.8
        if fx == fw - 1 or fy == fh - 1:
            idx -= 0.6
        col = ramp(r, idx)
        return (col, idx >= glow_from)
    return p


def patch(cells: dict, base, r=CRY, glow_from=2):
    """Legt aufgemalte Kristallflecken auf eine Flaeche. cells: {(fx, fy): Helligkeitsindex 0..4}."""
    def p(face, fx, fy, fw, fh):
        idx = cells.get((fx, fy))
        if idx is not None:
            return (ramp(r, idx), idx >= glow_from)
        return base(face, fx, fy, fw, fh)
    return p


def shard_cells(cx, cy, h=4, w=2):
    """Kleiner Kristallsplitter als Pixelmenge: Spitze oben, links hell, rechts dunkel."""
    cells = {}
    for j in range(h):
        wid = 1 + (j * w) // h
        for i in range(-(wid // 2), wid - wid // 2):
            cells[(cx + i, cy + j)] = 4 if (i == -(wid // 2) and j < h - 1) else 3 if j < h - 1 else 2
    cells[(cx, cy + h - 1)] = 1
    return cells


# ====================================================================== Virusherz: Textur
def heart_texture():
    e = Ent("virus_heart")
    g = e.geo
    # Boden und Wurzeln
    e.paint("roots", flesh(GROUND, 7001, vein_w=0.075, bias=1.6, fibers=False))
    # Kern: pulsierendes Fleisch. Kerngehaeuse (Frontplatte) siehe unten.
    e.paint("core", flesh(HEART, 7003, vein_w=0.05, bias=2.1), only=0)

    def window(face, fx, fy, fw, fh):
        if face != "north":
            return VIRUS[2]
        d = abs(fx - (fw - 1) / 2) / ((fw - 1) / 2) + abs(fy - (fh - 1) / 2) / ((fh - 1) / 2)
        if d < 0.28:
            return (VGLOW[4], True)
        if d < 0.55:
            return (VGLOW[3], True)
        if d < 0.82:
            return (VGLOW[2] if (fx + fy) % 2 else VGLOW[1], True)
        if d < 1.05:
            return (VGLOW[0], True)
        return VIRUS[1]
    e.paint("core", window, only=1)
    e.paint("spikes", crystal(CRY, 7005, 2.7))
    e.paint("detail_growths", crystal(CRY, 7007, 2.7), only=[0, 1, 2, 3, 4])
    e.paint("detail_growths", flesh(HEART, 7009, vein_w=0.07, bias=2.0), only=[5, 6])

    # Tentakel: dunkles Fleisch, leuchtender Strang der Laenge nach, Spitze komplett leuchtend
    def strand(face, fx, fy, fw, fh):
        long_x = fw >= fh
        base_idx = 1.5 + FACE_LIGHT[face] * 0.6 + edge_light(fx, fy, fw, fh) * 0.5
        if long_x and fy == fh // 2 and fh >= 2 or (not long_x and fx == fw // 2):
            return (VGLOW[1] if (fx + fy) % 2 else VGLOW[2], True)
        return ramp(HEART, base_idx)

    def tip(face, fx, fy, fw, fh):
        return (ramp(VGLOW, 3.3 - (fx + fy) * 0.4), True)
    for k in range(TENDRILS):
        b = f"detail_tendril_{k}"
        e.paint(b, strand, only=[0, 1, 2])
        e.paint(b, tip, only=3)
    e.save()
    return e


# ====================================================================== Shard-Zombie (Vanilla-Zombie-UV 64x64)
# Boxen: (u, v, w, h, d)
Z_HEAD, Z_HAT = (0, 0, 8, 8, 8), (32, 0, 8, 8, 8)
Z_BODY = (16, 16, 8, 12, 4)
Z_RARM, Z_LARM = (40, 16, 4, 12, 4), (32, 48, 4, 12, 4)
Z_RLEG, Z_LLEG = (0, 16, 4, 12, 4), (16, 48, 4, 12, 4)


def skin(seed, bias=2.3):
    def p(face, fx, fy, fw, fh):
        idx = bias + FACE_LIGHT[face] * 0.7 + edge_light(fx, fy, fw, fh) * 0.4 + ordered(fx, fy) * 0.45
        return ramp(ZSKIN, idx)
    return p


def cloth(r, seed, bias=2.1):
    def p(face, fx, fy, fw, fh):
        fold = math.sin((fx + FACE_ID[face] * 3) * 1.6) * 0.35
        idx = bias + FACE_LIGHT[face] * 0.7 + fold + ordered(fx, fy) * 0.36 + edge_light(fx, fy, fw, fh) * 0.3
        return ramp(r, idx)
    return p


def zombie_head():
    base = skin(7101)
    cells_cheek = {}
    for (x, y, v) in ((6, 4, 3), (7, 4, 2), (5, 5, 2), (6, 5, 4), (7, 5, 3), (6, 6, 3), (7, 6, 2), (7, 7, 1), (6, 7, 2)):
        cells_cheek[(x, y)] = v

    def p(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 2 and fx in (1, 2, 5, 6):
                return ZSKIN[0]
            if fy == 3 and fx in (1, 2, 5, 6):
                return (VGLOW[4], True) if fx in (2, 5) else (VGLOW[1], True)
            if fy == 4 and fx in (1, 2, 5, 6):
                return ZSKIN[1]
            if fy == 4 and fx in (3, 4):
                return ZSKIN[1]
            if fy == 6 and 2 <= fx <= 5:
                return ZSKIN[0] if fx in (2, 5) else (VIRUS[2])
            if fy == 7 and fx in (3, 4):
                return ZSKIN[1]
            if fx == 4 and fy in (0, 1):
                return (VGLOW[1], True)               # Riss in der Stirn
            c = cells_cheek.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 2)
        if face == "east":
            c = {(1, 1): 3, (2, 1): 4, (1, 2): 2, (2, 2): 3, (3, 2): 2, (2, 3): 2}.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 2)
        return base(face, fx, fy, fw, fh)
    return p


def zombie_hat():
    """Hut-Ebene (Vanilla zeichnet sie 0.5 groesser): nur Kristalle, der Rest bleibt durchsichtig."""
    top = {}
    for (cx, cy) in ((2, 2), (5, 4), (3, 6)):
        for (dx, dy, v) in ((0, 0, 4), (1, 0, 3), (0, 1, 3), (1, 1, 2), (-1, 0, 3), (0, -1, 3)):
            top[(cx + dx, cy + dy)] = v
    north = {(1, 0): 3, (2, 0): 4, (2, 1): 3, (6, 0): 3, (5, 0): 2, (5, 1): 2}
    side = {(3, 0): 4, (3, 1): 3, (4, 0): 3, (4, 1): 2, (5, 0): 2}

    def p(face, fx, fy, fw, fh):
        cells = {"up": top, "north": north, "east": side, "west": side}.get(face, {})
        c = cells.get((fx, fy))
        if c is None:
            return None
        return (ramp(CRY, c), c >= 2)
    return p


def zombie_body():
    base = cloth(ZSHIRT, 7121)
    tears = {(0, 8), (0, 9), (1, 9), (0, 10), (1, 10), (2, 10), (0, 11), (1, 11), (2, 11), (3, 11), (6, 10), (7, 10), (6, 11), (7, 11), (5, 11)}
    chest = {}
    for (x, y, v) in ((4, 2, 4), (5, 2, 3), (4, 3, 3), (5, 3, 2), (3, 3, 2), (4, 4, 2), (5, 4, 1), (5, 1, 3)):
        chest[(x, y)] = v

    def p(face, fx, fy, fw, fh):
        if face in ("north", "south") and (fx, fy) in tears:
            idx = 1.6 + ordered(fx, fy) * 0.54
            return ramp(ZSKIN, idx - (0.5 if fy == 11 else 0))
        if face == "north":
            c = chest.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 2)
            if (fx, fy) in ((3, 4), (3, 5), (2, 6), (2, 7)):
                return (VGLOW[1], True)               # Ader wandert vom Kristall nach unten links
        return base(face, fx, fy, fw, fh)
    return p


def zombie_arm(mirror=False, seed=7131):
    sleeve = cloth(ZSHIRT, seed)
    sk = skin(seed + 3)
    crust = {(1, 5): 3, (2, 5): 4, (1, 6): 2, (2, 6): 3, (2, 7): 2, (1, 4): 3}

    def p(face, fx, fy, fw, fh):
        if face == "down":
            return ZSKIN[1]
        if fy <= 3 and face != "up":
            return sleeve(face, fx, fy, fw, fh)
        if face == "up":
            return sleeve(face, fx, fy, fw, fh)
        if fy == 4:
            return ramp(ZSHIRT, 0.8)                  # ausgefranster Aermelsaum
        if face in ("north", "east"):
            c = crust.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 2)
        if face == "north" and fx == 3 and 8 <= fy <= 10:
            return (VGLOW[1], True)
        return sk(face, fx, fy, fw, fh)
    return p


def zombie_leg(seed=7141):
    pants = cloth(ZPANTS, seed)
    sk = skin(seed + 2)

    def p(face, fx, fy, fw, fh):
        if face == "down":
            return ZSKIN[0]
        if fy >= 9 and face != "up":
            if fy == 11 and (fx + seed) % 2 == 0 and face in ("north", "south"):
                return (ramp(CRY, 3), True)           # Kristallreif am Knoechel
            if fy >= 10 and fx == 1 and face == "north":
                return (VGLOW[1], True)
            return sk(face, fx, fy, fw, fh)
        if fy == 8 and face != "up":
            return ramp(ZPANTS, 0.9)                  # zerfetzter Saum
        return pants(face, fx, fy, fw, fh)
    return p


def zombie_texture():
    t, g = Tex(64, 64), Tex(64, 64)
    paint_box(t, *Z_HEAD, zombie_head(), g)
    paint_box(t, *Z_HAT, zombie_hat(), g)
    paint_box(t, *Z_BODY, zombie_body(), g)
    paint_box(t, *Z_RARM, zombie_arm(), g)
    paint_box(t, *Z_LARM, zombie_arm(seed=7137), g)
    paint_box(t, *Z_RLEG, zombie_leg(7141), g)
    paint_box(t, *Z_LLEG, zombie_leg(7143), g)
    t.save(TEX / "entity" / "shard_zombie.png")
    g.save(TEX / "entity" / "shard_zombie_glowmask.png")
    return t, g


# ====================================================================== Kristallisierte Kuh (Vanilla-Kuh-UV 64x32)
C_HEAD, C_HORN = (0, 0, 8, 8, 6), (22, 0, 1, 3, 1)
C_BODY, C_LEG, C_UDDER = (18, 4, 12, 18, 10), (0, 16, 4, 12, 4), (52, 0, 4, 6, 1)


def hide(seed, patch_thr=0.58):
    """Dunkles Fell mit blassen Kristallflecken (statt Kuh-Schwarzweiss)."""
    def p(face, fx, fy, fw, fh):
        off = FACE_ID[face] * 17
        n = vnoise(fx / 3.5 + off, fy / 3.5, seed)
        idx = 2.3 + FACE_LIGHT[face] * 0.6 + edge_light(fx, fy, fw, fh) * 0.4 + ordered(fx, fy) * 0.54
        if n > patch_thr:
            return ramp(CPATCH, 0.9 + (n - patch_thr) * 5 + FACE_LIGHT[face] * 0.5 + ordered(fx, fy) * 0.36)
        return ramp(CHIDE, idx)
    return p


def cow_head():
    base = hide(7201, 0.62)

    def p(face, fx, fy, fw, fh):
        if face == "north":
            if fy == 3 and fx in (1, 6):
                return (VGLOW[3], True)
            if fy == 2 and fx in (1, 6):
                return CHIDE[0]
            if fy == 4 and fx in (1, 6):
                return CHIDE[1]
            if fy >= 5 and 2 <= fx <= 5:                    # Maul
                c = ramp(CSNOUT, 2.0 + FACE_LIGHT[face] + ordered(fx, fy) * 0.45 - (0.5 if fy == 7 else 0))
                if fy == 6 and fx in (3, 4):
                    return CSNOUT[0]
                return c
            if fx == 4 and fy in (0, 1):
                return (VGLOW[1], True)
        if face == "up":
            c = {(3, 2): 4, (4, 2): 3, (3, 3): 3, (4, 3): 2, (3, 1): 3}.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 2)
        return base(face, fx, fy, fw, fh)
    return p


def cow_body():
    base = hide(7211, 0.56)
    spine = {}
    for (cy, h) in ((3, 4), (8, 5), (13, 4)):
        for (x, y, v) in ((5, cy, 4), (6, cy, 3), (5, cy + 1, 3), (6, cy + 1, 2), (5, cy + 2, 2), (6, cy + 2, 1)) + (((5, cy + 3, 1),) if h > 4 else ()):
            spine[(x, y)] = v
        spine[(4, cy + 1)] = 3
        spine[(7, cy + 1)] = 2

    def p(face, fx, fy, fw, fh):
        if face == "north":            # Ruecken
            c = spine.get((fx, fy))
            if c is not None:
                return (ramp(CRY, c), c >= 2)
            if fx in (5, 6) and fy % 5 == 2:
                return (VGLOW[1], True)
        if face in ("east", "west") and (fx, fy) in ((2, 6), (3, 7), (3, 8), (4, 9), (5, 10), (5, 11)):
            return (VGLOW[1], True)                         # Ader an der Flanke
        return base(face, fx, fy, fw, fh)
    return p


def cow_leg():
    base = hide(7221, 0.6)

    def p(face, fx, fy, fw, fh):
        if face == "down":
            return CHIDE[0]
        if fy >= 9 and face != "up":
            band = fy == 9 or fy == 11
            c = ramp(CRY, 1.5 + FACE_LIGHT[face] + (0.9 if fy == 10 else 0))
            return (c, fy == 10)
        return base(face, fx, fy, fw, fh)
    return p


def cow_udder():
    base = flesh(HEART, 7231, vein_w=0.08, bias=2.4, fibers=False)
    return base


def cow_texture():
    t, g = Tex(64, 32), Tex(64, 32)
    paint_box(t, *C_HEAD, cow_head(), g)
    paint_box(t, *C_HORN, lambda face, fx, fy, fw, fh: (ramp(CRY, 3.2 - fy * 0.6), True), g)
    paint_box(t, *C_BODY, cow_body(), g)
    paint_box(t, *C_LEG, cow_leg(), g)
    paint_box(t, *C_UDDER, cow_udder(), g)
    t.save(TEX / "entity" / "crystallized_cow.png")
    g.save(TEX / "entity" / "crystallized_cow_glowmask.png")
    return t, g


def main():
    heart_geometry()
    heart_animations()
    heart_texture()
    zombie_texture()
    cow_texture()
    print("infected_entities: Virusherz (Geo+Anim+Textur+Glow), shard_zombie, crystallized_cow (je Textur+Glowmask)")


if __name__ == "__main__":
    main()
