"""Gemeinsame Palette und Zeichenhelfer fuer alle Shardline-Texturskripte.

Alle Farben der Mod stehen hier. Jede Rampe geht von dunkel (Index 0) nach hell (letzter Index).
Lichtquelle ist oben links. Es gibt keinen Zufall ohne Seed: Rauschen kommt aus einer festen Hashfunktion.
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image

# ---------------------------------------------------------------- Pfade
PROJECT = Path(__file__).resolve().parents[2]
ASSETS = PROJECT / "src" / "main" / "resources" / "assets" / "shardline"
TEX = ASSETS / "textures"
GEO = ASSETS / "geo" / "entity"
ANIM = ASSETS / "animations" / "entity"
RESOURCE_ROOT = PROJECT / "src" / "main" / "resources"


def hx(s: str, a: int = 255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)


# ---------------------------------------------------------------- Palette (Hauptpalette der Mod)
OUTLINE = hx("#0b0713")
CLEAR = (0, 0, 0, 0)
WHITE = hx("#ffffff")

# Dunkles Schiefergrau (5 Stufen)
SLATE = [hx(c) for c in ("#151220", "#221d2f", "#332c44", "#4a4260", "#655b7e")]
# Warmes Aschegrau fuer Aschewandler / Asche
ASH = [hx(c) for c in ("#181516", "#292425", "#3d3637", "#554d4e", "#746a6b")]
# Tiefes Violett (5 Stufen) + Lilaakzente
VIOLET = [hx(c) for c in ("#1b0e36", "#2f1959", "#4a2987", "#6c44bd", "#9870ec")]
LILAC = [hx(c) for c in ("#7b56d0", "#a681f4", "#c8a9ff", "#e8dcff")]
# Cyan-Leuchten (5 Stufen)
CYAN = [hx(c) for c in ("#0a4a58", "#158799", "#2cc3d6", "#84efff", "#e3ffff")]
# Magenta-Akzente (5 Stufen)
MAGENTA = [hx(c) for c in ("#470b3f", "#831877", "#c933b8", "#ff5edb", "#ffc3f2")]
# Splitter-Edelstein (Shard): Violett nach Rosa, Hauptfarbe aller Shard-Gegenstaende
SHARD = [hx(c) for c in ("#34176f", "#6535c4", "#9658ee", "#cf8bff", "#f6d6ff")]
# Kristallholz (blaulila) und Kristallmoos (Tuerkis)
CWOOD = [hx(c) for c in ("#3f4a8e", "#5c6cb8", "#7f90da", "#a9b9f4", "#d4defd")]
TEAL = [hx(c) for c in ("#0b3841", "#125a64", "#1c8885", "#33b8a4", "#70e8cb")]
# Koloss-Stahl: dunkles Schiefertuerkis (Kolossset)
COL = [hx(c) for c in ("#12262f", "#1c4757", "#27758a", "#4fb2c9", "#a4ebf6")]
# Knochen/Pergament fuer Haendler und Zaehne (violett getoent, bleibt in der Palette)
BONE = [hx(c) for c in ("#3b2f52", "#5d4c7b", "#8b79ad", "#b9a9d6", "#e4daf3")]

CARDINAL = ("up", "down", "east", "north", "west", "south")


def clamp(v, lo, hi):
    return lo if v < lo else hi if v > hi else v


def ramp(r, i):
    """Rampenzugriff mit Begrenzung (auch fuer Float-Indizes)."""
    return r[clamp(int(round(i)), 0, len(r) - 1)]


def mix(c1, c2, t):
    return tuple(int(round(c1[k] + (c2[k] - c1[k]) * t)) for k in range(4))


def with_alpha(c, a):
    return (c[0], c[1], c[2], a)


# ---------------------------------------------------------------- deterministisches Rauschen
def _h(x: int, y: int, seed: int) -> int:
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    n ^= n >> 16
    return n & 0xFFFFFFFF


def nz(x: int, y: int, seed: int = 0) -> float:
    """Reproduzierbares Rauschen im Bereich [0, 1)."""
    return _h(x, y, seed) / 4294967296.0


def nz_tile(x: int, y: int, size: int, seed: int = 0) -> float:
    """Rauschen, das mit Periode `size` kachelt (fuer Bloecke)."""
    return nz(x % size, y % size, seed)


def vnoise(x: float, y: float, seed: int = 0, period: int = 0) -> float:
    """Weiches Value-Noise (bilinear). Mit period > 0 kachelt es."""
    x0, y0 = int(x // 1), int(y // 1)
    fx, fy = x - x0, y - y0
    fx = fx * fx * (3 - 2 * fx)
    fy = fy * fy * (3 - 2 * fy)

    def g(ix, iy):
        if period:
            ix %= period
            iy %= period
        return nz(ix, iy, seed)

    a = g(x0, y0) * (1 - fx) + g(x0 + 1, y0) * fx
    b = g(x0, y0 + 1) * (1 - fx) + g(x0 + 1, y0 + 1) * fx
    return a * (1 - fy) + b * fy


# ---------------------------------------------------------------- Leinwand
class Tex:
    """Duenne Huelle um ein PIL-Bild mit Begrenzungspruefung."""

    def __init__(self, w: int, h: int, fill=CLEAR):
        self.w, self.h = w, h
        self.img = Image.new("RGBA", (w, h), fill)
        self.px = self.img.load()

    def set(self, x: int, y: int, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = c

    def get(self, x: int, y: int):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[x, y]
        return CLEAR

    def rect(self, x: int, y: int, w: int, h: int, c):
        for j in range(h):
            for i in range(w):
                self.set(x + i, y + j, c)

    def frame(self, x: int, y: int, w: int, h: int, c):
        for i in range(w):
            self.set(x + i, y, c)
            self.set(x + i, y + h - 1, c)
        for j in range(h):
            self.set(x, y + j, c)
            self.set(x + w - 1, y + j, c)

    def line(self, x0: int, y0: int, x1: int, y1: int, c):
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def paste_art(self, art, palette, ox: int = 0, oy: int = 0):
        """Zeichnet ASCII-Pixelkunst. '.' ist transparent, andere Zeichen kommen aus `palette`."""
        rows = [r for r in art.strip("\n").split("\n")]
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch == "." or ch == " ":
                    continue
                if ch not in palette:
                    raise KeyError(f"Unbekanntes Pixelzeichen {ch!r} in Zeile {j}")
                self.set(ox + i, oy + j, palette[ch])

    def outline(self, c=OUTLINE, diagonal=False):
        """Legt eine 1px Kontur um alle deckenden Pixel (nur auf transparente Nachbarn)."""
        src = self.img.copy()
        sp = src.load()
        steps = [(1, 0), (-1, 0), (0, 1), (0, -1)]
        if diagonal:
            steps += [(1, 1), (-1, -1), (1, -1), (-1, 1)]
        for y in range(self.h):
            for x in range(self.w):
                if sp[x, y][3] != 0:
                    continue
                for dx, dy in steps:
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < self.w and 0 <= ny < self.h and sp[nx, ny][3] != 0:
                        self.px[x, y] = c
                        break

    def selout(self, factor: float = 0.4, base=OUTLINE, keep: float = 0.2):
        """Weiche Kontur wie bei Vanilla: jeder Randpixel bekommt einen dunkleren Ton seiner Nachbarn
        (Anteil `keep` der festen Konturfarbe `base` bleibt beigemischt, damit es auf dunklem Grund sichtbar bleibt)."""
        src = self.img.copy()
        sp = src.load()
        for y in range(self.h):
            for x in range(self.w):
                if sp[x, y][3] != 0:
                    continue
                near = [sp[nx, ny] for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))
                        if 0 <= nx < self.w and 0 <= ny < self.h and sp[nx, ny][3] != 0]
                if not near:
                    continue
                r = sum(c[0] for c in near) / len(near)
                g = sum(c[1] for c in near) / len(near)
                b = sum(c[2] for c in near) / len(near)
                self.px[x, y] = (int((r * factor) * (1 - keep) + base[0] * keep),
                                 int((g * factor) * (1 - keep) + base[1] * keep),
                                 int((b * factor) * (1 - keep) + base[2] * keep), 255)

    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.img.save(path, "PNG", optimize=False)


def save_png(tex: Tex, relative: str):
    """Speichert unter textures/<relative>.png."""
    tex.save(TEX / f"{relative}.png")


def write_json(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


# ---------------------------------------------------------------- Story-Paletten (Erweiterung)
# Viruspalette: krankes Magenta, giftiges Pink, fast schwarzes Violett. VGLOW ist die pulsierende Adernschicht.
VIRUS = [hx(c) for c in ("#0d0412", "#240a2c", "#481044", "#7c1a68", "#b8268f")]
VGLOW = [hx(c) for c in ("#a3208a", "#e03cc0", "#ff6fdc", "#ffb3f0", "#fff0fb")]
TOXIC = hx("#ff3fb4")
# Reinheitspalette: warmes Gold, Weiss und sanftes Hellblau als klarer Gegenpol zum Virus.
GOLD = [hx(c) for c in ("#4a2c08", "#8a5a14", "#cf9425", "#f4c95a", "#fff1b0")]
IVORY = [hx(c) for c in ("#6f6a82", "#9f9ab4", "#cdc9dd", "#ecebf6", "#ffffff")]
SKYB = [hx(c) for c in ("#4879ad", "#79b0e0", "#acd6f5", "#d8eeff", "#f4fbff")]
# Leder / Pergament fuer Haendlernotizen
LEATHER = [hx(c) for c in ("#2a1810", "#46281a", "#6b4128", "#93603a", "#bf8a55")]

# Glut fuer den Aschland (Grau/Orange): dunkle Kohle, rote Glut, Orange, helles Gelb
EMBER = [hx(c) for c in ("#3a1208", "#7a2a0c", "#c4501a", "#f08a2c", "#ffd070")]
