"""Box-UV-Layout (Bedrock/GeckoLib) und Flaechenmalerei."""
from __future__ import annotations

from common import Tex, clamp, nz, ramp

# Beleuchtung pro Flaeche: Lichtquelle oben links, daher oben hell, unten dunkel, linke Seiten heller.
FACE_LIGHT = {"up": 0.9, "north": 0.0, "east": 0.25, "west": -0.35, "south": -0.5, "down": -1.0}


def box_rects(u: float, v: float, w: float, h: float, d: float):
    """Pixelrechtecke (x, y, breite, hoehe) der sechs Flaechen eines Box-UV-Quaders."""
    u, v = int(u), int(v)
    w, h, d = int(round(w)), int(round(h)), int(round(d))
    return {
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + d + w + d, v + d, w, h),
    }


def box_extent(u, v, w, h, d):
    w, h, d = int(round(w)), int(round(h)), int(round(d))
    return int(u) + 2 * (w + d), int(v) + d + h


def paint_box(tex: Tex, u, v, w, h, d, painter, glow: Tex | None = None):
    """Ruft painter(face, fx, fy, fw, fh, gx, gy) fuer jedes Pixel jeder Flaeche auf.

    painter liefert entweder eine Farbe oder (Farbe, leuchtet_bool). Leuchtende Pixel landen zusaetzlich in `glow`.
    """
    for face, (x0, y0, fw, fh) in box_rects(u, v, w, h, d).items():
        for fy in range(fh):
            for fx in range(fw):
                res = painter(face, fx, fy, fw, fh)
                if res is None:
                    continue
                glows = False
                if isinstance(res, tuple) and len(res) == 2 and isinstance(res[1], bool):
                    res, glows = res
                tex.set(x0 + fx, y0 + fy, res)
                if glow is not None:
                    glow.set(x0 + fx, y0 + fy, res if glows else (0, 0, 0, 0))


def edge_light(fx, fy, fw, fh):
    """+1 an der oberen/linken Kante, -1 an der unteren/rechten Kante, sonst 0."""
    e = 0.0
    if fy == 0 or fx == 0:
        e += 0.6
    if fy == fh - 1 or fx == fw - 1:
        e -= 0.8
    return e
