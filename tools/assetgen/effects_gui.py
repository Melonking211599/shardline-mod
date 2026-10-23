"""Effekte & GUI der Story-Erweiterung.

* mob_effect/shard_fever.png        18x18   Effekt-Icon "Splitterfieber" (Totenkopf-Silhouette im Kristall + Virusadern)
* particle/virus_spore_0..3.png     8x8     weisslich/grau, der Code tintet sie (Rosa) -> 4 Phasen einer pulsierenden Spore
* misc/breach_overlay.png           256x256 transparent, Risslinien von den Raendern, Mitte frei, leuchtend am Riss
* gui/advancements/backgrounds/story.png  16x16 kachelnd, infizierter Stein (dunkel, Adern leicht leuchtend)

Aufruf: python tools/assetgen/effects_gui.py
"""
from __future__ import annotations

import math

from common import CLEAR, OUTLINE, TEX, VGLOW, VIRUS, Tex, clamp, hx, mix, nz, ramp, save_png, vnoise

CRY = [hx(c) for c in ("#3a0c3a", "#7a1a68", "#c42c9e", "#ff6fdc", "#ffd0f4")]


# ------------------------------------------------------------------ Effekt-Icon
SKULL = """
................
................
....11111111....
...1222222221...
..122222222221..
..122222222221..
..122332233221..
..123332333321..
..123332333321..
..122333333221..
...1223443221...
....12233221....
....12222221....
.....1212121....
.....1212121....
......11111.....
"""


def shard_fever() -> Tex:
    """18x18: rosa leuchtende Kristall-Raute, darin ein dunkler Schaedel; Adern laufen heraus."""
    t = Tex(18, 18)
    cx = cy = 8.5
    # Kristall-Raute (Hintergrund)
    for y in range(18):
        for x in range(18):
            d = abs(x - cx) / 8.2 + abs(y - cy) / 8.2
            if d <= 1.0:
                idx = 1.0 + (1 - d) * 2.2 + (0.7 if x + y < 17 else -0.2) + (nz(x, y, 8801) - 0.5) * 0.5
                if d > 0.88:
                    idx = 0.4 + (0.6 if x + y < 17 else 0)
                t.set(x, y, ramp(CRY, idx))
    # Schaedel (dunkel) mittig, 2px Versatz: 16x16 Kunst wird auf 18x18 zentriert, aber verkleinert -> 10x10 Variante
    skull = [
        "..XXXXXX..",
        ".XXXXXXXX.",
        "XXXXXXXXXX",
        "XX..XX..XX",
        "XX..XX..XX",
        "XXXXXXXXXX",
        ".XXX..XXX.",
        "..XXXXXX..",
        "..X.XX.X..",
        "..X.XX.X..",
    ]
    ox, oy = 4, 4
    for j, row in enumerate(skull):
        for i, ch in enumerate(row):
            if ch == "X":
                t.set(ox + i, oy + j, VIRUS[1] if j < 7 else VIRUS[2])
    # Augen glimmen
    for (x, y) in ((ox + 2, oy + 3), (ox + 2, oy + 4), (ox + 7, oy + 3), (ox + 7, oy + 4)):
        t.set(x, y, VGLOW[3])
    # Kontur
    t.outline(OUTLINE)
    return t


# ------------------------------------------------------------------ Partikel
def spore(phase: int) -> Tex:
    """8x8, grauweiss. Phase 0 klein -> 3 gross/hell (Pulsieren). Der Code faerbt sie rosa."""
    t = Tex(8, 8)
    shades = [(150, 150, 150), (200, 200, 200), (235, 235, 235), (255, 255, 255)]
    r = [1.2, 2.0, 2.8, 3.4][phase]
    for y in range(8):
        for x in range(8):
            d = math.hypot(x - 3.5, y - 3.5)
            if d <= r:
                lvl = 3 if d < r * 0.4 else 2 if d < r * 0.7 else 1
                if d > r - 0.6 and phase > 1:
                    lvl = 0
                c = shades[lvl]
                a = 255 if d < r - 0.3 else 170
                t.set(x, y, (c[0], c[1], c[2], a))
    if phase >= 2:                                   # kleine Stachel
        for (x, y) in ((3, 0), (4, 7), (0, 3), (7, 4)):
            t.set(x, y, (210, 210, 210, 200))
    return t


# ------------------------------------------------------------------ Breach-Overlay
def breach_overlay() -> Tex:
    """256x256: von allen Raendern wachsen verzweigte Risse nach innen; die Mitte (ca. 140px) bleibt frei.
    Risskern leuchtet (hell), Rand dunkel/violett, davor ein weicher dunkler Vignetten-Schleier an den Raendern."""
    S = 256
    t = Tex(S, S)
    # Vignette: nur ganz aussen, halbtransparent
    for y in range(S):
        for x in range(S):
            d = max(abs(x - 127.5), abs(y - 127.5)) / 127.5            # 0 mitte .. 1 rand
            if d > 0.78:
                a = int(clamp((d - 0.78) / 0.22, 0, 1) ** 1.6 * 120)
                n = vnoise(x / 9.0, y / 9.0, 9901)
                a = int(a * (0.65 + n * 0.5))
                t.set(x, y, VIRUS[0][:3] + (clamp(a, 0, 150),))

    def crack(x, y, dx, dy, length, seed, depth=0, w=1):
        k = 0
        px, py = float(x), float(y)
        ang = math.atan2(dy, dx)
        for i in range(length):
            k += 1
            ang += (nz(i, depth * 7, seed) - 0.5) * 0.7
            px += math.cos(ang)
            py += math.sin(ang)
            ix, iy = int(px), int(py)
            if not (0 <= ix < S and 0 <= iy < S):
                break
            if max(abs(ix - 127.5), abs(iy - 127.5)) < 70:                    # Mitte freihalten
                break
            fade = 1.0 - i / length
            # dunkler Rand, leuchtender Kern
            for ox in range(-w, w + 1):
                for oy in range(-w, w + 1):
                    if abs(ox) + abs(oy) <= w:
                        t.set(ix + ox, iy + oy, VIRUS[1][:3] + (200,))
            t.set(ix, iy, VGLOW[3 if fade > 0.5 else 2][:3] + (255,))
            if w >= 1:
                t.set(ix + 1, iy, VGLOW[1][:3] + (255,))
            if i > 6 and nz(i, depth, seed + 3) > 0.9 and depth < 2:
                side = ang + (1.0 if nz(i, depth, seed + 5) > 0.5 else -1.0)
                crack(ix, iy, math.cos(side), math.sin(side), int(length * 0.55), seed + 11 * i, depth + 1, 0)

    starts = []
    for k in range(7):
        starts.append((18 + k * 36, 0, 0.1, 1))               # oben
        starts.append((18 + k * 36 + 10, S - 1, -0.1, -1))    # unten
        starts.append((0, 25 + k * 34, 1, 0.1))               # links
        starts.append((S - 1, 20 + k * 34, -1, -0.1))         # rechts
    for n, (sx, sy, dx, dy) in enumerate(starts):
        crack(sx, sy, dx, dy, 34 + int(nz(n, 1, 9903) * 34), 9905 + n * 13)
    return t


# ------------------------------------------------------------------ Advancement-Hintergrund
def story_background() -> Tex:
    """16x16 kachelnder, infizierter Stein: sehr dunkel (Advancement-Hintergrund wird gross gekachelt)."""
    from virus_blocks import infected_stone
    base, glow = infected_stone()
    t = Tex(16, 16)
    for y in range(16):
        for x in range(16):
            r, g, b, a = base.get(x, y)
            gr, gg, gb, ga = glow.get(x, y)
            c = (int(r * 0.55), int(g * 0.5), int(b * 0.6), 255)
            if ga:
                c = mix(c, (gr, gg, gb, 255), 0.5)
            t.set(x, y, c)
    return t


def main():
    save_png(shard_fever(), "mob_effect/shard_fever")
    for i in range(4):
        save_png(spore(i), f"particle/virus_spore_{i}")
    save_png(breach_overlay(), "misc/breach_overlay")
    save_png(story_background(), "gui/advancements/backgrounds/story")
    print("effects_gui: shard_fever, virus_spore_0..3, breach_overlay, advancement story-bg")


if __name__ == "__main__":
    main()
