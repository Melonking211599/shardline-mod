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


# ------------------------------------------------------------------ Strukturiertes, kachelndes Rauschen (Polish-Stufe)
# Grundsatz: Flaechen bestehen aus zusammenhaengenden Tonflecken, deren Anteile fest vorgegeben sind (Rangzuordnung),
# nie aus gleichmaessigem Pixelrauschen oder Stempeln auf einem Raster. Alles kachelt (Wrap-around).
import math


def tnoise(x: float, y: float, cx: int, cy: int, seed: int, n: int = 16) -> float:
    """Kachelndes Value-Noise: cx, cy = Zellen pro Kachel (Wiederholung nach n Pixeln). cx != cy streckt die Flecken."""
    u, v = x / n * cx, y / n * cy
    x0, y0 = math.floor(u), math.floor(v)
    fx, fy = u - x0, v - y0
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)

    def g(i, j):
        return nz(i % cx, j % cy, seed)

    a = g(x0, y0) * (1 - fx) + g(x0 + 1, y0) * fx
    b = g(x0, y0 + 1) * (1 - fx) + g(x0 + 1, y0 + 1) * fx
    return a * (1 - fy) + b * fy


def field(seed: int, cells=(4, 4), detail: float = 0.3, detail_cells=(8, 8), n: int = 16):
    """Zweistufiges kachelndes Feld (grobe Flecken + feinere Kante), Werte 0..1 pro Pixel als Zeilenliste."""
    return [[(1 - detail) * tnoise(x + 0.5, y + 0.5, cells[0], cells[1], seed, n)
             + detail * tnoise(x + 0.5, y + 0.5, detail_cells[0], detail_cells[1], seed + 7, n)
             for x in range(n)] for y in range(n)]


def rank_tones(values, fractions):
    """Rangbasierte Zuordnung Wert -> Tonindex: Anteil jedes Tons ist fest (z. B. 6 % dunkel, 44 % Mitte, 6 % hell)."""
    h, w = len(values), len(values[0])
    order = sorted((v, y, x) for y, row in enumerate(values) for x, v in enumerate(row))
    cum, s = [], 0.0
    for f in fractions:
        s += f
        cum.append(s)
    idx = [[0] * w for _ in range(h)]
    for rank, (_, y, x) in enumerate(order):
        r = (rank + 0.5) / (w * h)
        tone = 0
        while tone < len(cum) - 1 and r > cum[tone]:
            tone += 1
        idx[y][x] = tone
    return idx


def despeckle(idx, passes: int = 1):
    """Einzelpixel ohne gleichen 4er-Nachbarn nehmen den Ton des (kleineren) Nachbarn an. Macht aus Rauschen Cluster."""
    h, w = len(idx), len(idx[0])
    for _ in range(passes):
        src = [row[:] for row in idx]
        for y in range(h):
            for x in range(w):
                me = src[y][x]
                near = [src[(y + dy) % h][(x + dx) % w] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
                if me not in near:
                    near.sort()
                    idx[y][x] = near[1]          # Median der vier Nachbarn
    return idx


def paint(idx, ramp, offset: int = 0) -> Tex:
    h, w = len(idx), len(idx[0])
    t = Tex(w, h)
    for y in range(h):
        for x in range(w):
            t.set(x, y, ramp[max(0, min(len(ramp) - 1, idx[y][x] + offset))])
    return t


def patch_idx(seed: int, fractions=(0.06, 0.22, 0.44, 0.22, 0.06), cells=(4, 4), detail: float = 0.3,
              detail_cells=(8, 8), clean: int = 1, n: int = 16):
    return despeckle(rank_tones(field(seed, cells, detail, detail_cells, n), fractions), clean)


def patches(seed: int, ramp, fractions=(0.06, 0.22, 0.44, 0.22, 0.06), cells=(4, 4), detail: float = 0.3,
            detail_cells=(8, 8), clean: int = 1, offset: int = 0, n: int = 16) -> Tex:
    """Kachelnde Tonflecken in einer Rampe. fractions legt die Anteile der Rampenstufen fest (Summe 1)."""
    return paint(patch_idx(seed, fractions, cells, detail, detail_cells, clean, n), ramp, offset)


def walk(seed: int, x: int, y: int, length: int, heading=(1, 1), wobble: float = 0.3, n: int = 16):
    """Zufaelliger, 4er-zusammenhaengender Pfad (Riss, Ader) mit Hauptrichtung; kachelt (Wrap). Liefert Punktliste."""
    pts = [(x % n, y % n)]
    hx_, hy_ = heading
    for k in range(length - 1):
        along_x = (k % 2 == 0)
        if nz(k, 9, seed) < wobble:
            along_x = not along_x
        x += hx_ if along_x else 0
        y += 0 if along_x else hy_
        pts.append((x % n, y % n))
    return pts


def spots(seed: int, count: int, gap: float, n: int = 16):
    """Verteilte Punkte mit Mindestabstand (Poisson-artig, kachelnd), ohne Gitter."""
    return _spots_for(seed, count, gap, n)


def grow(seed: int, roots, length: int = 12, fork: float = 0.14, wobble: float = 0.28, max_gen: int = 2, allow=None,
         n: int = 16, min_branch: int = 4):
    """Gewachsene Aderbaeume (Infektion). roots: [((x, y), (dx, dy))] mit Richtung in {-1, 1}^2.

    Jede Ader ist 4er-zusammenhaengend, kachelt (Wrap) und zweigt im rechten Winkel ab (Zweige kuerzer). Der Stamm
    gewinnt, wo sich Pfade ueberlagern. Liefert ({(x, y): (u, gen)}, tips): u = 0 an der Wurzel bis 1 an der Spitze
    (pro Zweig), gen = Zweigebene. `allow(x, y)` darf Zellen sperren (z. B. transparente Pixel)."""
    cells, tips = {}, set()
    queue = [((x, y), d, length, 0) for (x, y), d in roots]
    steps = 0
    while queue and steps < 800:
        (x, y), (dx, dy), size, gen = queue.pop(0)
        path = []
        for k in range(size):
            steps += 1
            if allow is not None and not allow(x % n, y % n):
                break
            path.append((x % n, y % n))
            along_x = (k % 2 == 0)
            if nz(steps, 11, seed) < wobble:
                along_x = not along_x
            if along_x:
                x += dx
            else:
                y += dy
            if k >= 3 and gen < max_gen and size - k > min_branch and nz(steps, 13, seed) < fork:
                bd = (dx, -dy) if nz(steps, 14, seed) < 0.5 else (-dx, dy)
                queue.append(((x, y), bd, max(min_branch, (size - k) // 2 + 2), gen + 1))
        for i, cell in enumerate(path):
            u = i / max(1, len(path) - 1)
            old = cells.get(cell)
            if old is None or gen < old[1]:
                cells[cell] = (u, gen)
        if len(path) > 1:
            tips.add(path[-1])
    return cells, tips
