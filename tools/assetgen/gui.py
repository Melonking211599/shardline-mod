"""Himmel, Mond, Partikel, Portal-Overlay, Advancement-Hintergrund und Mod-Logo. Aufruf: python tools/assetgen/gui.py"""
from __future__ import annotations

import math

from PIL import Image, ImageDraw, ImageFont

from fresh_blocks import SHARDSTONE
from pixel import clusters
from common import CYAN, LILAC, MAGENTA, RESOURCE_ROOT, SHARD, SLATE, TEX, VIOLET, Tex, clamp, hx, mix, nz, ramp, save_png, vnoise, with_alpha


def sky() -> Tex:
    """128x128 kachelnder Himmel: tiefes Violett mit Nebelbaendern, Cyan-Schleiern und Sternen."""
    t = Tex(128, 128)
    deep = [hx(c) for c in ("#07030f", "#0e0620", "#170a35", "#24104d", "#361a6e")]
    for y in range(128):
        for x in range(128):
            n = vnoise(x / 16.0, y / 16.0, 5001, period=8)
            m = vnoise(x / 8.0, y / 8.0, 5003, period=16)
            idx = 1.0 + n * 2.0 + (m - 0.5) * 0.9
            c = ramp(deep, idx)
            # Magenta- und Cyan-Schleier
            veil = vnoise(x / 24.0, y / 12.0, 5005, period=5)
            if veil > 0.68:
                c = mix(c, MAGENTA[0], clamp((veil - 0.68) * 3.0, 0, 0.6))
            veil2 = vnoise(x / 20.0, y / 20.0, 5007, period=6)
            if veil2 > 0.7:
                c = mix(c, CYAN[0], clamp((veil2 - 0.7) * 3.0, 0, 0.55))
            t.set(x, y, c)
    for k in range(110):
        x = int(nz(k, 0, 5011) * 128)
        y = int(nz(k, 1, 5011) * 128)
        b = nz(k, 2, 5011)
        col = (hx("#ffffff") if b > 0.93 else LILAC[3] if b > 0.6 else LILAC[1])
        t.set(x, y, col)
        if b > 0.93:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                t.set((x + dx) % 128, (y + dy) % 128, with_alpha(LILAC[2], 255))
    return t


def moon() -> Tex:
    """Zerbrochener Kristallmond (64x64): Scheibe mit Rissen und abgesplitterten Stuecken, Leuchtkante."""
    t = Tex(64, 64)
    cx = cy = 31.5
    pal = [hx(c) for c in ("#3b2470", "#5b3aa3", "#8559d6", "#b88bff", "#e6d2ff")]
    for y in range(64):
        for x in range(64):
            d = math.hypot(x - cx, y - cy)
            if d > 25.5:
                # abgesplitterte Fragmente ausserhalb
                continue
            idx = 2.6 - ((x - cx) + (y - cy)) / 30.0 + (vnoise(x / 4.0, y / 4.0, 5101) - 0.5) * 1.6
            if d > 23.5:
                idx += 0.8 if (x + y) < 62 else -0.9
            t.set(x, y, ramp(pal, idx))
    # Risse
    cracks = [((20, 8), (30, 26)), ((30, 26), (26, 44)), ((26, 44), (30, 58)), ((30, 26), (50, 22)), ((50, 22), (56, 30)),
              ((30, 26), (40, 40)), ((14, 34), (26, 44))]
    for (a, b) in cracks:
        t.line(a[0], a[1], b[0], b[1], hx("#1b0c3c"))
    for (a, b) in cracks:
        t.set(a[0] + 1, a[1], MAGENTA[2])
    # Splitterstuecke, die sich loesen
    for (px, py, s) in ((54, 10, 3), (58, 18, 2), (6, 48, 3), (10, 56, 2), (52, 54, 3)):
        for i in range(s):
            for j in range(s - i):
                t.set(px + i, py + j, ramp(pal, 2.8 - i * 0.7))
    # Cyan-Leuchtkante unten rechts
    for y in range(64):
        for x in range(64):
            d = math.hypot(x - cx, y - cy)
            if 24.0 < d <= 25.5 and (x + y) > 66:
                t.set(x, y, CYAN[2] if (x + y) % 3 else CYAN[3])
    return t


def overlay_tex() -> Tex:
    """256x256 Portal-Overlay: Violett zu den Raendern hin, Cyan-Schlieren, deterministische Blitze."""
    size = 256
    t = Tex(size, size)
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - 127.5, y - 127.5) / 127.5
            a = clamp(int((min(1.0, d) ** 1.6) * 215), 0, 215)
            wob = vnoise(x / 22.0, y / 22.0, 5201)
            if d < 0.5 and wob < 0.45:
                a = 0
            c = mix(VIOLET[2], MAGENTA[1], clamp(wob, 0, 1))
            if wob > 0.7:
                c = mix(c, CYAN[1], 0.5)
            t.set(x, y, with_alpha(c, a))
    # Lichtadern
    for k in range(18):
        x, y = int(nz(k, 0, 5203) * size), int(nz(k, 1, 5203) * size)
        for s in range(14):
            ang = nz(k, 2, 5203) * 6.28 + math.sin(s * 0.7 + k) * 0.5
            x += int(round(math.cos(ang) * 2))
            y += int(round(math.sin(ang) * 2))
            t.set(x, y, hx("#e7d8ff", 170))
    return t


def particle(kind: str, frame: int) -> Tex:
    """8x8 Partikel. shard_sparkle: farbiges Kreuz, schrumpfend. void_mote: weisse Wolke (wird im Code eingefaerbt)."""
    t = Tex(8, 8)
    if kind == "shard_sparkle":
        size = 3 - min(frame, 2) if frame < 3 else 1
        col = [SHARD[3], SHARD[2], MAGENTA[3], CYAN[3]][frame]
        for i in range(-size, size + 1):
            t.set(4 + i, 4, col if abs(i) < size else with_alpha(col, 160))
            t.set(4, 4 + i, col if abs(i) < size else with_alpha(col, 160))
        if frame < 3:
            t.set(3, 3, with_alpha(col, 140))
            t.set(5, 5, with_alpha(col, 140))
        t.set(4, 4, hx("#ffffff"))
    else:
        r = [3.0, 2.6, 2.0, 1.4][frame]
        for y in range(8):
            for x in range(8):
                d = math.hypot(x - 3.5, y - 3.5)
                if d <= r:
                    a = int(255 * (1 - d / (r + 0.7)))
                    g = 255 - int(40 * d)
                    t.set(x, y, (g, g, g, clamp(a + 40, 0, 255)))
    return t


def advancement_bg() -> Tex:
    t = clusters(5301, SHARDSTONE, base=1, lights=8, darks=6, longest=3)
    for y in range(16):
        for x in range(16):
            c = t.get(x, y)
            t.set(x, y, (int(c[0] * 0.7), int(c[1] * 0.7), int(c[2] * 0.75), 255))
    return t


def logo() -> Image.Image:
    """256x128 Logo fuer die Mod-Liste: Splitter-Emblem und Schriftzug (Pixelschrift, deterministisch)."""
    S = 2
    t = Tex(128, 64)
    # Hintergrund: tiefer Verlauf mit Sternen
    for y in range(64):
        for x in range(128):
            n = vnoise(x / 14.0, y / 14.0, 5401)
            c = mix(hx("#0e0620"), hx("#2a1260"), clamp((x + y) / 190.0 + (n - 0.5) * 0.25, 0, 1))
            t.set(x, y, c)
    for k in range(40):
        t.set(int(nz(k, 0, 5403) * 128), int(nz(k, 1, 5403) * 64), LILAC[2] if k % 4 else hx("#ffffff"))
    # Emblem: grosser Splitter links
    shards = [([(14, 10), (24, 22), (18, 54), (8, 30)], SHARD), ([(26, 18), (34, 30), (22, 52), (20, 30)], MAGENTA), ([(6, 34), (16, 56), (4, 52)], CYAN)]
    img = t.img
    d = ImageDraw.Draw(img)
    for pts, r in shards:
        d.polygon(pts, fill=r[2])
        d.polygon([pts[0], pts[1], ((pts[0][0] + pts[1][0]) // 2, (pts[0][1] + pts[2][1]) // 2)], fill=r[4])
        d.line(pts + [pts[0]], fill=r[0], width=1)
    # Schriftzug in 5x7-Pixelfont, selbst definiert
    font = {
        "S": ["01110", "10001", "10000", "01110", "00001", "10001", "01110"],
        "H": ["10001", "10001", "10001", "11111", "10001", "10001", "10001"],
        "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
        "R": ["11110", "10001", "10001", "11110", "10100", "10010", "10001"],
        "D": ["11110", "10001", "10001", "10001", "10001", "10001", "11110"],
        "L": ["10000", "10000", "10000", "10000", "10000", "10000", "11111"],
        "I": ["01110", "00100", "00100", "00100", "00100", "00100", "01110"],
        "N": ["10001", "11001", "10101", "10101", "10011", "10001", "10001"],
        "E": ["11111", "10000", "10000", "11110", "10000", "10000", "11111"],
    }
    word = "SHARDLINE"
    x0, y0 = 40, 20
    for k, ch in enumerate(word):
        for j, row in enumerate(font[ch]):
            for i, bit in enumerate(row):
                if bit == "1":
                    x, y = x0 + k * 9 + i, y0 + j * 2
                    for dy in range(2):
                        t.set(x, y + dy, LILAC[3] if j < 3 else LILAC[1])
                        t.set(x + 1, y + 2 + dy, VIOLET[0]) if False else None
    for k in range(len(word) * 9):
        t.set(x0 + k, y0 + 16, MAGENTA[2] if k % 2 else MAGENTA[1])
    return t.img.resize((256, 128), Image.NEAREST)


def main():
    save_png(sky(), "environment/splitterweite_sky")
    save_png(moon(), "environment/splitterweite_moon")
    save_png(overlay_tex(), "misc/shard_portal_overlay")
    for i in range(4):
        save_png(particle("shard_sparkle", i), f"particle/shard_sparkle_{i}")
        save_png(particle("void_mote", i), f"particle/void_mote_{i}")
    save_png(advancement_bg(), "gui/advancements/backgrounds/shardline")
    out = RESOURCE_ROOT / "shardline_logo.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    logo().save(out)
    print("gui: Himmel, Mond, Overlay, 8 Partikel, Advancement-Hintergrund, Logo")


if __name__ == "__main__":
    main()
