"""Saubere Texturbausteine: ruhige Flaechen, klare Kanten, geordnetes Raster statt Pixelrauschen.

Die frueheren Texturen entstanden aus Zufallsrauschen pro Pixel. Das ergibt fleckige "Klumpen" ohne Struktur.
Hier gibt es stattdessen:
  * plates():  kantige, kachelnde Bruchplatten (Voronoi) mit Lichtkante oben links und Fuge unten rechts,
  * ordered(): Bayer-Raster fuer weiche Uebergaenge ohne Zufallsgekrissel,
  * despeckle(): entfernt einzelne Tonwert-Ausreisser, laesst farbige Akzente (Glitzer, Adern) stehen.
Alle Funktionen kacheln nahtlos und sind deterministisch. Die Paletten kommen unveraendert aus common.py.
"""
from __future__ import annotations

import colorsys

from common import Tex, clamp, nz, ramp

BAYER4 = ((0, 8, 2, 10), (12, 4, 14, 6), (3, 11, 1, 9), (15, 7, 13, 5))


def ordered(x: int, y: int) -> float:
    """Schwellwert -0.5 .. +0.5 aus dem 4x4-Bayer-Raster."""
    return BAYER4[y % 4][x % 4] / 16.0 - 0.47


def _wrap(d: float, n: int) -> float:
    d = (d + n / 2) % n - n / 2
    return d


def plate_map(seed: int, cells: int, n: int = 16, jitter: float = 0.62):
    """Kachelnde Voronoi-Zellen: liefert (id-Karte, Punkte). Je Zelle ein Punkt im Gitter cells x cells."""
    pts = []
    for j in range(cells):
        for i in range(cells):
            px = (i + 0.5 + (nz(i, j, seed) - 0.5) * jitter * 2) * n / cells
            py = (j + 0.5 + (nz(i, j, seed + 1) - 0.5) * jitter * 2) * n / cells
            pts.append((px, py, nz(i, j, seed + 2)))
    ids = [[0] * n for _ in range(n)]
    for y in range(n):
        for x in range(n):
            best, bk = 1e9, 0
            for k, (px, py, _s) in enumerate(pts):
                dx, dy = _wrap(x + 0.5 - px, n), _wrap(y + 0.5 - py, n)
                d = dx * dx + dy * dy
                if d < best:
                    best, bk = d, k
            ids[y][x] = bk
    return ids, pts


def plates(seed: int, r, cells: int = 3, bias: float = 2.0, tone: float = 0.55, tilt: float = 0.8,
           grain: float = 0.22, edges: bool = True, edge_hi: float = 0.8, edge_lo: float = 0.9, n: int = 16,
           jitter: float = 0.62) -> Tex:
    """Kantige Bruchplatten.

    tone:  Helligkeitsunterschied zwischen den Platten (Stufen der Rampe),
    tilt:  Licht-Verlauf innerhalb einer Platte (oben links hell, unten rechts dunkel),
    grain: Staerke des geordneten Rasters (0 = glatte Platten),
    edge_hi / edge_lo: helle Kante oben links / dunkle Fuge unten rechts.
    """
    ids, pts = plate_map(seed, cells, n, jitter)
    t = Tex(n, n)
    size = n / cells
    for y in range(n):
        for x in range(n):
            k = ids[y][x]
            px, py, s = pts[k]
            dx, dy = _wrap(x + 0.5 - px, n), _wrap(y + 0.5 - py, n)
            idx = bias + (s - 0.5) * 2 * tone - (dx + dy) / size * tilt * 0.5
            idx += ordered(x, y) * grain * 2
            if edges:
                left, up = ids[y][(x - 1) % n], ids[(y - 1) % n][x]
                right, down = ids[y][(x + 1) % n], ids[(y + 1) % n][x]
                if right != k or down != k:
                    idx -= edge_lo
                elif left != k or up != k:
                    idx += edge_hi
            t.set(x, y, ramp(r, idx))
    return t


def _hls(c):
    h, l, s = colorsys.rgb_to_hls(c[0] / 255, c[1] / 255, c[2] / 255)
    return h, l, s


def despeckle(t: Tex, lum_gap: float = 0.07, hue_tol: float = 0.07, passes: int = 1, wrap: bool = True) -> Tex:
    """Ersetzt einzelne Pixel, die sich nur in der Helligkeit vom Umfeld abheben (Rauschen).

    Pixel mit anderem Farbton (Glitzer, Adern, Erz) und transparente Pixel bleiben unberuehrt. Ein Pixel gilt als
    Ausreisser, wenn alle vier Nachbarn hell/dunkel anders liegen als es selbst; es bekommt dann den Wert des Nachbarn
    mit der mittleren Helligkeit.
    """
    for _ in range(passes):
        src = [[t.get(x, y) for x in range(t.w)] for y in range(t.h)]

        def at(x, y):
            if wrap:
                return src[y % t.h][x % t.w]
            return src[clamp(y, 0, t.h - 1)][clamp(x, 0, t.w - 1)]

        for y in range(t.h):
            for x in range(t.w):
                c = src[y][x]
                if c[3] == 0:
                    continue
                nb = [at(x - 1, y), at(x + 1, y), at(x, y - 1), at(x, y + 1)]
                if any(q[3] == 0 for q in nb):
                    continue
                h, l, s = _hls(c)
                ls = []
                same_hue = True
                for q in nb:
                    qh, ql, qs = _hls(q)
                    ls.append(ql)
                    dh = abs(qh - h)
                    dh = min(dh, 1 - dh)
                    if dh > hue_tol and s > 0.18 and qs > 0.18:
                        same_hue = False
                if not same_hue:
                    continue
                if all(ql - l > lum_gap for ql in ls) or all(l - ql > lum_gap for ql in ls):
                    order = sorted(range(4), key=lambda i: ls[i])
                    t.set(x, y, nb[order[1]])
    return t


def smooth_field(x: float, y: float, seed: int, cells: int, n: int = 16) -> float:
    """Weiches, kachelndes Value-Noise mit cells x cells Gitterzellen ueber n Pixel."""
    fx, fy = x * cells / n, y * cells / n
    x0, y0 = int(fx // 1), int(fy // 1)
    tx, ty = fx - x0, fy - y0
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)

    def g(ix, iy):
        return nz(ix % cells, iy % cells, seed)

    a = g(x0, y0) * (1 - tx) + g(x0 + 1, y0) * tx
    b = g(x0, y0 + 1) * (1 - tx) + g(x0 + 1, y0 + 1) * tx
    return a * (1 - ty) + b * ty


def soft(seed: int, r, bias: float = 2.0, amp: float = 1.8, cells: int = 4, detail: float = 0.0, dither: float = 0.55,
         squash: float = 1.0, n: int = 16) -> Tex:
    """Weiche Flaechen: grosses Rauschen ergibt ruhige Verlaeufe, das Bayer-Raster macht die Uebergaenge sauber.

    squash > 1 streckt die Flecken waagerecht (z. B. Erdschichten), detail mischt eine zweite, feinere Ebene dazu.
    """
    t = Tex(n, n)
    for y in range(n):
        for x in range(n):
            v = smooth_field(x + 0.5, (y + 0.5) * squash, seed, cells, n) - 0.5
            if detail:
                v += (smooth_field(x + 0.5, y + 0.5, seed + 9, cells * 2, n) - 0.5) * detail
            idx = bias + v * amp * 2 + ordered(x, y) * dither
            t.set(x, y, ramp(r, idx))
    return t


def _spots(seed: int, count: int, n: int = 16, gap: float = 3.2):
    """Deterministische, gleichmaessig verteilte Punkte (kachelnd, mit Mindestabstand)."""
    out = []
    k = 0
    while len(out) < count and k < 400:
        x, y = int(nz(k, 4, seed) * n), int(nz(k, 5, seed) * n)
        if all(min(abs(x - a), n - abs(x - a)) ** 2 + min(abs(y - b), n - abs(y - b)) ** 2 >= gap * gap for a, b in out):
            out.append((x, y))
        k += 1
    return out


def pebbles(t: Tex, seed: int, r, count: int, base: float = 2.0, wide: bool = False, gap: float = 3.4) -> Tex:
    """Kleine Steinchen: Lichtpunkt oben links, Koerper, Schatten unten rechts. Kachelnd (Wrap-around)."""
    n = t.w
    for (x, y) in _spots(seed, count, n, gap):
        w = 3 if wide and nz(x, y, seed + 7) > 0.4 else 2
        for dx in range(w):
            t.set((x + dx) % n, y % n, ramp(r, base + 1.7 if dx == 0 else base + 0.9))
            t.set((x + dx) % n, (y + 1) % n, ramp(r, base - 0.1 if dx < w - 1 else base - 1.4))
        t.set((x + w) % n, (y + 1) % n, ramp(r, base - 1.3)) if nz(x, y, seed + 8) > 0.5 else None
    return t


def tufts(t: Tex, seed: int, r, count: int, base: float = 2.0, gap: float = 3.0) -> Tex:
    """Moos-/Grasbueschel: kurze helle Dreiecke mit dunklem Fuss. Kachelnd."""
    n = t.w
    for (x, y) in _spots(seed, count, n, gap):
        t.set(x % n, y % n, ramp(r, base + 1.6))
        t.set((x - 1) % n, (y + 1) % n, ramp(r, base + 0.9))
        t.set(x % n, (y + 1) % n, ramp(r, base + 0.9))
        t.set((x + 1) % n, (y + 1) % n, ramp(r, base - 0.9))
        t.set((x + 1) % n, (y + 2) % n, ramp(r, base - 1.2))
    return t


def leaf_cluster(seed: int, back, fronts, n: int = 16, back_count: int = 16, front_count: int = 12,
                 back_gap: float = 1.6, front_gap: float = 2.5) -> Tex:
    """Laubtextur aus einzelnen Blaettern: hinten dunkle Blaetter fuer Tiefe, vorn helle mit Lichtkante.

    back / fronts: Farbtripel (hell, mittel, dunkel). Die Blaetter sind 6 Pixel gross und kacheln (Wrap-around).
    Zwischen den Blaettern bleiben echte Luecken (transparent).
    """
    t = Tex(n, n)
    shape = ((0, -1, 0), (1, -1, 1), (-1, 0, 0), (0, 0, 0), (1, 0, 1), (-1, 1, 1), (0, 1, 1), (1, 1, 2), (0, 2, 2))

    def put(cx, cy, tones, alpha_skip=False):
        for dx, dy, k in shape:
            t.set((cx + dx) % n, (cy + dy) % n, tones[k])

    for (x, y) in _spots(seed, back_count, n, back_gap):
        put(x, y, back)
    for i, (x, y) in enumerate(_spots(seed + 31, front_count, n, front_gap)):
        put(x, y, fronts[i % len(fronts)])
    return t


def bark(seed: int, r, n: int = 16, strips=(0, 4, 8, 12), bias: float = 2.0, hi: float = 0.9) -> Tex:
    """Rinde aus senkrechten Streifen: jede Rindenplatte hat einen eigenen Ton, links eine helle Kante, rechts eine Furche.
    Waagerechte Bruchstellen versetzen die Streifen. Die Rampe laeuft von dunkel nach hell."""
    t = Tex(n, n)
    widths = [(strips[(i + 1) % len(strips)] - s) % n or n for i, s in enumerate(strips)]
    for i, s in enumerate(strips):
        w = widths[i]
        tone = bias + (nz(i, 0, seed) - 0.5) * 0.9
        for dx in range(w):
            for y in range(n):
                x = (s + dx) % n
                # Bruchstellen: alle paar Pixel kurz heller/dunkler
                seg = (y + int(nz(i, 1, seed) * 7)) // 5
                v = tone + (nz(i, seg, seed + 3) - 0.5) * 0.7 + ordered(x, y) * 0.35
                if dx == 0:
                    v = tone - 1.6
                elif dx == 1:
                    v += hi
                elif dx == w - 1:
                    v -= 0.6
                t.set(x, y, ramp(r, v))
    return t
