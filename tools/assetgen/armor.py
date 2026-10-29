"""Ruestungs-Layer (64x32, Vanilla-Format) fuer Shard- und Kolossset. Aufruf: python tools/assetgen/armor.py

Die getragene Ruestung ist das GeckoLib-Modell aus armor_models.py; diese Ebenen sind nur der Rueckfall fuer Fremdmods,
die direkt die Vanilla-Ebene ansprechen (gleiche Paletten, vereinfachte Formen).

Ausgabe: textures/models/armor/<set>_layer_1.png und <set>_layer_2.png
Zusaetzlich <set>_layer_N_glow.png (nur die leuchtenden Pixel), falls ein Glow-Layer im Code registriert wird.
Mapping: layer_1 = Helm, Brust, Arme, Stiefel; layer_2 = Hose (Guertel + Beine).
"""
from __future__ import annotations

from common import SLATE, TEX, Tex, hx, nz, ramp
from gear import COLOSSUS_P, SHARD_P
from uvlib import FACE_LIGHT, edge_light, paint_box

HEAD = (0, 0, 8, 8, 8)
BODY = (16, 16, 8, 12, 4)
ARM = (40, 16, 4, 12, 4)
LEG = (0, 16, 4, 12, 4)


class ArmorStyle:
    def __init__(self, name, r, gem, glow_seam, seam_glows):
        self.name, self.r, self.gem = name, r, gem
        self.seam = glow_seam
        self.seam_glows = seam_glows
        self.trim = SLATE


def plate(st: ArmorStyle, face, fx, fy, fw, fh, seed, detail=0.0):
    """Grundplatte ohne Rauschen: Kantenlicht, sanfter Verlauf von oben nach unten und eine Plattenfuge.

    Der Verlauf ist auf ganze Stufen gerundet, damit grosse ruhige Flaechen entstehen statt Gekrissel.
    """
    idx = 2.2 + FACE_LIGHT[face] * 0.8 + edge_light(fx, fy, fw, fh) * 0.9 + detail
    idx += 0.5 - (fy / max(1, fh - 1)) * 1.0
    if fh >= 8 and fy % 4 == 3 and 0 < fx < fw - 1:
        idx -= 0.9
    return ramp(st.r, idx)


def seam(st: ArmorStyle):
    return (st.seam, st.seam_glows)


def gem_px(st: ArmorStyle, hot=False):
    return (st.gem[4] if hot else st.gem[3], True)


# ----------------------------------------------------------------- Layer 1
def head_painter(st):
    def p(face, fx, fy, fw, fh):
        base = plate(st, face, fx, fy, fw, fh, 11)
        if face == "north":
            if fy == 3 and 1 <= fx <= 6:
                return st.r[0]
            if fy == 4 and 1 <= fx <= 6:
                return (st.gem[2], True) if 2 <= fx <= 5 else st.r[0]
            if fy == 5 and 1 <= fx <= 6:
                return st.r[1]
            if (fx, fy) in ((3, 1), (4, 1)):
                return gem_px(st, True)
            if fy == 2 and fx in (3, 4):
                return gem_px(st)
            if fy >= 5 and fx in (0, 7):
                return st.r[1]
            if fy == 6 and 2 <= fx <= 5:
                return ramp(st.r, 1.2)
            return base
        if face == "up":
            if fx in (3, 4):
                return ramp(st.r, 3.8)
            return base
        if face in ("east", "west"):
            if fy in (3, 4):
                return ramp(st.trim, 2.5 - (0.8 if fy == 4 else 0))
            if fy == 3 and fx == 3:
                return gem_px(st)
            return base
        if face == "south":
            if fy == 4:
                return seam(st) if st.seam_glows else st.r[0]
            return base
        return ramp(st.r, 0.8)

    return p


def body_painter(st, leggings=False):
    def p(face, fx, fy, fw, fh):
        base = plate(st, face, fx, fy, fw, fh, 21)
        if leggings:
            # nur Guertel (obere 4 Zeilen) sichtbar machen, Rest bleibt transparent
            if face in ("down",):
                return ramp(st.trim, 1.4)
            if fy >= 4 and face not in ("up",):
                return None
            if face == "up":
                return ramp(st.trim, 2.0)
            if fy == 0:
                return ramp(st.trim, 3.4)
            if fy == 3:
                return ramp(st.trim, 0.8)
            col = ramp(st.trim, 2.2 + FACE_LIGHT[face] + edge_light(fx, fy, fw, fh) * 0.5)
            if face == "north":
                if fx in (3, 4) and fy in (1, 2):
                    return gem_px(st, fy == 1)
                if fx in (2, 5) and fy in (1, 2):
                    return ramp(st.r, 2.5)
            return col
        if face == "north":
            # Brustplatte mit Raute
            diamond = {(3, 3), (4, 3), (2, 4), (3, 4), (4, 4), (5, 4), (3, 5), (4, 5), (3, 2), (4, 2)}
            if (fx, fy) in diamond:
                hot = (fx, fy) in ((3, 4), (4, 4))
                return gem_px(st, hot)
            if (fx, fy) in {(1, 3), (6, 3), (2, 2), (5, 2), (2, 5), (5, 5), (3, 6), (4, 6), (3, 1), (4, 1)}:
                return ramp(st.r, 1.0)
            if fy == 7:
                return seam(st)
            if fy in (10, 11):
                c = ramp(st.trim, 2.4 - (fy - 10) * 1.4)
                if fy == 10 and fx in (3, 4):
                    return gem_px(st)
                return c
            if fy == 0 and fx in (2, 3, 4, 5):
                return ramp(st.r, 3.6)
            return base
        if face == "south":
            if fx in (3, 4) and fy < 10:
                return ramp(st.r, 3.5 - (fy % 3) * 0.7)
            if fy == 7:
                return seam(st)
            if fy in (10, 11):
                return ramp(st.trim, 2.0 - (fy - 10) * 1.2)
            return base
        if face in ("east", "west"):
            if fy in (10, 11):
                return ramp(st.trim, 2.0 - (fy - 10) * 1.2)
            if fy == 7:
                return st.r[0]
            return base
        return base

    return p


def arm_painter(st):
    def p(face, fx, fy, fw, fh):
        base = plate(st, face, fx, fy, fw, fh, 31)
        if face == "up":
            return ramp(st.r, 3.2)
        if face == "down":
            return ramp(st.trim, 1.0)
        # Schulterkappe (Zeilen 0-3) heller und kantiger
        if fy <= 3:
            idx = 2.9 + FACE_LIGHT[face] + edge_light(fx, fy, fw, fh) * 0.4
            if fy == 3:
                return seam(st) if st.seam_glows else st.r[0]
            if face in ("north", "east") and fy == 1 and fx in (1, 2):
                return gem_px(st, face == "north")
            return ramp(st.r, idx + (0.5 if fy == 0 else 0))
        if fy in (6, 7):
            return seam(st) if (st.seam_glows and fy == 6) else ramp(st.r, 0.8)
        if fy >= 10:
            return ramp(st.trim, 2.2 - (fy - 10) * 1.1 + FACE_LIGHT[face] * 0.5)
        return base

    return p


def boots_painter(st):
    def p(face, fx, fy, fw, fh):
        if face == "up":
            return None
        if face == "down":
            return ramp(st.trim, 0.7)
        if fy < 7:
            return None
        base = plate(st, face, fx, fy, fw, fh, 41)
        if fy == 7:
            return ramp(st.trim, 3.0 + FACE_LIGHT[face] * 0.6)
        if fy == 8:
            return seam(st) if st.seam_glows else st.r[0]
        if fy == 11:
            return st.r[0] if face != "north" else ramp(st.r, 0.9)
        if face == "north" and fy == 10 and fx in (1, 2):
            return gem_px(st, fx == 1)
        if face == "north" and fy == 10:
            return ramp(st.r, 3.6)
        return base

    return p


# ----------------------------------------------------------------- Layer 2
def legs_painter(st):
    def p(face, fx, fy, fw, fh):
        base = plate(st, face, fx, fy, fw, fh, 51)
        if face == "up":
            return ramp(st.trim, 1.6)
        if face == "down":
            return ramp(st.trim, 0.7)
        if fy == 0:
            return ramp(st.trim, 3.2 + FACE_LIGHT[face] * 0.4)
        if fy == 1:
            return seam(st) if st.seam_glows else st.r[0]
        if face == "north":
            if fy in (6, 7) and fx in (1, 2):
                return gem_px(st, fy == 6)
            if fy == 5:
                return ramp(st.r, 3.6)
            if fy == 8:
                return seam(st) if st.seam_glows else st.r[0]
        elif fy == 6:
            return st.r[0]
        if fy >= 10:
            return ramp(st.r, 1.8 + FACE_LIGHT[face] + edge_light(fx, fy, fw, fh))
        return base

    return p


# ----------------------------------------------------------------- Zusammenbau
def build(st: ArmorStyle):
    res = {}
    for layer in (1, 2):
        tex = Tex(64, 32)
        glow = Tex(64, 32)
        if layer == 1:
            paint_box(tex, *HEAD, head_painter(st), glow)
            paint_box(tex, *BODY, body_painter(st), glow)
            paint_box(tex, *ARM, arm_painter(st), glow)
            paint_box(tex, *LEG, boots_painter(st), glow)
        else:
            paint_box(tex, *BODY, body_painter(st, leggings=True), glow)
            paint_box(tex, *LEG, legs_painter(st), glow)
        res[layer] = (tex, glow)
    return res


def _ramp(p):
    return [p[k] for k in "12345"]


def _gem(p):
    return [p["a"], p["a"], p["b"], p["c"], p["w"]]


# Rueckfall-Ebenen in den Paletten der Rundum-Modelle (geo/armor): dunkler Violettstahl bzw. Steinstahl mit Cyan-Kern
STYLES = [
    ArmorStyle("shard", _ramp(SHARD_P), _gem(SHARD_P), SHARD_P["1"], False),
    ArmorStyle("colossus", _ramp(COLOSSUS_P), _gem(COLOSSUS_P), COLOSSUS_P["b"], True),
]


def main():
    n = 0
    for st in STYLES:
        for layer, (tex, glow) in build(st).items():
            tex.save(TEX / "models" / "armor" / f"{st.name}_layer_{layer}.png")
            n += 1
            if st.name == "colossus":
                glow.save(TEX / "models" / "armor" / f"{st.name}_layer_{layer}_glow.png")
                n += 1
    print(f"armor: {n} Dateien")


if __name__ == "__main__":
    main()
