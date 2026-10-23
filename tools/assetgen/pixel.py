"""Neuer Pixel-Werkzeugkasten (Stufe 2: alle Texturen von Grund auf neu).

Grundsaetze:
  * Handgezeichnete Pixelbilder als ASCII ("art"): jede Textur hat eine klare Idee, Lichtquelle oben links.
  * Fuer Materialflaechen (Stein, Erde ...) kein Pixelrauschen, sondern kleine zusammenhaengende Cluster wie bei Vanilla
    (clusters): ein ruhiger Grundton mit wenigen, 1-3 Pixel grossen helleren und dunkleren Flecken, die kachelnd umlaufen.
  * Farbpaletten bleiben die der Mod (common.py). Pro Bild wird die Zuordnung Zeichen -> Farbe angegeben.
"""
from __future__ import annotations

from common import CLEAR, Tex, nz


def ramps(**named):
    """ramps(A=ramp, B=ramp) -> Zeichen '0'-'4' fuer A, 'a'-'e' fuer B, 'f'-'j' fuer C, 'k'-'o' fuer D."""
    base = {"A": "01234", "B": "abcde", "C": "fghij", "D": "klmno"}
    pal = {}
    for key, ramp in named.items():
        for ch, col in zip(base[key], ramp):
            pal[ch] = col
    return pal


def art(rows: str, pal: dict, name: str = "?", size: int = 16, tex: Tex | None = None, ox: int = 0, oy: int = 0) -> Tex:
    """ASCII -> Tex. '.' = transparent. Prueft Zeilenzahl und -laenge."""
    lines = rows.strip("\n").split("\n")
    t = tex or Tex(size, size)
    for y, row in enumerate(lines):
        if len(row) != size and tex is None:
            raise ValueError(f"{name}: Zeile {y} hat {len(row)} statt {size} Zeichen: {row!r}")
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch not in pal:
                raise ValueError(f"{name}: Zeichen {ch!r} in Zeile {y} ohne Farbe")
            t.set(ox + x, oy + y, pal[ch])
    if tex is None and len(lines) != size:
        raise ValueError(f"{name}: {len(lines)} Zeilen statt {size}")
    return t


def clusters(seed: int, ramp, base: int = 2, lights: int = 7, darks: int = 7, size: int = 16, longest: int = 3,
             horizontal: float = 0.65, hi: int | None = None, lo: int | None = None, n: int = 16) -> Tex:
    """Ruhiger Grundton mit kleinen Clustern (Vanilla-Stil). Kachelt (Wrap-around).

    hi / lo: Rampenindex der hellen / dunklen Cluster (Standard base+1 / base-1).
    """
    hi = base + 1 if hi is None else hi
    lo = base - 1 if lo is None else lo
    t = Tex(n, n, ramp[base])
    taken = set()

    def place(k, tone):
        x, y = int(nz(k, 1, seed + tone) * n), int(nz(k, 2, seed + tone) * n)
        length = 1 + int(nz(k, 3, seed + tone) * longest)
        horiz = nz(k, 4, seed + tone) < horizontal
        for i in range(length):
            px, py = (x + i) % n, y % n
            if not horiz:
                px, py = x % n, (y + i) % n
            if (px, py) in taken:
                return
        for i in range(length):
            px, py = ((x + i) % n, y % n) if horiz else (x % n, (y + i) % n)
            taken.add((px, py))
            t.set(px, py, ramp[tone])

    for k in range(darks):
        place(k, lo)
    for k in range(lights):
        place(k + 100, hi)
    return t


def stamp(t: Tex, rows: str, pal: dict, x: int, y: int, name: str = "stamp") -> Tex:
    """Legt ein kleines ASCII-Bild (beliebige Groesse, '.' = unveraendert) an Position x, y (mit Wrap-around)."""
    for j, row in enumerate(rows.strip("\n").split("\n")):
        for i, ch in enumerate(row):
            if ch != ".":
                t.set((x + i) % t.w, (y + j) % t.h, pal[ch])
    return t


def bevel(t: Tex, lighter, darker, n: int = 16) -> Tex:
    """Obere und linke Kante heller, untere und rechte dunkler (Lichtquelle oben links). lighter/darker: Funktionen Farbe -> Farbe."""
    for i in range(n):
        t.set(i, 0, lighter(t.get(i, 0)))
        t.set(0, i, lighter(t.get(0, i)))
        t.set(i, n - 1, darker(t.get(i, n - 1)))
        t.set(n - 1, i, darker(t.get(n - 1, i)))
    return t


def shift(c, f):
    """Farbe heller (f > 1) oder dunkler (f < 1)."""
    return (min(255, int(c[0] * f)), min(255, int(c[1] * f)), min(255, int(c[2] * f)), c[3])


def _spots_for(seed: int, count: int, gap: float, n: int = 16):
    """Gleichmaessig verteilte Punkte mit Mindestabstand (kachelnd), deterministisch."""
    out = []
    k = 0
    while len(out) < count and k < 500:
        x, y = int(nz(k, 4, seed) * n), int(nz(k, 5, seed) * n)
        if all(min(abs(x - a), n - abs(x - a)) ** 2 + min(abs(y - b), n - abs(y - b)) ** 2 >= gap * gap for a, b in out):
            out.append((x, y))
        k += 1
    return out
