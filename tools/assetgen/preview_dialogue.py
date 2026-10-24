"""Vorschau des Dialogfensters (DialogueScreen) aus den echten GUI-Texturen, ohne Spielclient.

Aufruf: python tools/assetgen/preview_dialogue.py <ausgabe.png> [fensterbreite fensterhoehe]
Rechnet das Layout wie DialogueScreen.init() fuer GUI-Skalierung 1 bis 4 und zeigt alle vier nebeneinander (jeweils
auf die echte Pixelgroesse vergroessert). Text in einer Ersatzschrift (6 px pro Zeichen wie die Minecraft-Schrift).
Nur zur Kontrolle von Platz, Ueberlappungen und Lesbarkeit; schreibt nichts in die Mod.
"""
from __future__ import annotations

import sys

from PIL import Image, ImageDraw, ImageFont

from common import TEX

PORTRAIT_BOX, BOX_H, PAD, LINE_H, BUTTON_H = 80, 80, 9, 10, 20
CHAR_W = 6
TEXT = ("Du hast das Beben auch gespuert? Seit dem Thron bebt es jede Nacht. Die Kristalle singen falsch, "
        "und die Leute im Lager schlafen schlecht. Ich brauche Holz, Licht und jemanden, der nicht wegrennt.")
CHOICES = [("Was ist hier passiert?", True), ("Ich helfe dir.", True), ("Ich will das Siegel pruefen.", False), ("Leb wohl.", True)]


def tex(name):
    return Image.open(TEX / f"{name}.png").convert("RGBA")


def nine(dst, img, x, y, w, h, border):
    tw, th = img.size
    iw, ih = tw - 2 * border, th - 2 * border
    def part(u, v, pw, ph, px, py):
        dst.alpha_composite(img.crop((u, v, u + pw, v + ph)), (px, py))
    part(0, 0, border, border, x, y)
    part(tw - border, 0, border, border, x + w - border, y)
    part(0, th - border, border, border, x, y + h - border)
    part(tw - border, th - border, border, border, x + w - border, y + h - border)
    for dx in range(0, w - 2 * border, iw):
        cw = min(iw, w - 2 * border - dx)
        part(border, 0, cw, border, x + border + dx, y)
        part(border, th - border, cw, border, x + border + dx, y + h - border)
        for dy in range(0, h - 2 * border, ih):
            ch = min(ih, h - 2 * border - dy)
            part(border, border, cw, ch, x + border + dx, y + border + dy)
    for dy in range(0, h - 2 * border, ih):
        ch = min(ih, h - 2 * border - dy)
        part(0, border, border, ch, x, y + border + dy)
        part(tw - border, border, border, ch, x + w - border, y + border + dy)


def hslice(dst, img, x, y, w, h, cap, v=0, region_w=None):
    region_w = region_w or img.size[0]
    def part(u, pw, px):
        dst.alpha_composite(img.crop((u, v, u + pw, v + h)), (px, y))
    part(0, cap, x)
    part(region_w - cap, cap, x + w - cap)
    inner = region_w - 2 * cap
    for dx in range(0, w - 2 * cap, inner):
        part(cap, min(inner, w - 2 * cap - dx), x + cap + dx)


def wrap(text, width):
    words, lines, line = text.split(), [], ""
    for word in words:
        test = (line + " " + word).strip()
        if len(test) * CHAR_W > width and line:
            lines.append(line)
            line = word
        else:
            line = test
    return lines + [line]


def render(width, height):
    img = Image.new("RGBA", (width, height), (90, 120, 80, 255))
    d = ImageDraw.Draw(img)
    for y in range(height):                                   # Ersatz-Spielwelt: Himmel und Boden
        c = (110, 140, 190, 255) if y < height * 0.55 else (70, 95, 55, 255)
        d.line([(0, y), (width, y)], fill=c)
    shade = Image.new("RGBA", (width, height), (0, 0, 0, 48))
    img.alpha_composite(shade)
    font = ImageFont.load_default()
    available = width - 16
    compact = available < 300 or height < 220
    box_h = 64 if height < 220 else BOX_H
    portrait_space = 0 if compact else PORTRAIT_BOX + 4
    box_w = min(available - portrait_space, 360)
    portrait_x = (width - (portrait_space + box_w)) // 2
    box_x, box_y = portrait_x + portrait_space, height - box_h - 18
    text_w = box_w - PAD * 2 - 8
    if not compact:
        img.alpha_composite(tex("gui/portraits/unknown"), (portrait_x + 8, box_y + 8))
        img.alpha_composite(tex("gui/dialogue/portrait_frame"), (portrait_x, box_y))
    nine(img, tex("gui/dialogue/frame"), box_x, box_y, box_w, box_h, 8)
    name = "Rhea Tannwald"
    plate_x = box_x if compact else portrait_x
    hslice(img, tex("gui/dialogue/nameplate"), plate_x, box_y - 15, len(name) * CHAR_W + 22, 16, 8)
    d = ImageDraw.Draw(img)
    d.text((plate_x + 11, box_y - 12), name, fill=(244, 201, 90), font=font)
    for i, line in enumerate(wrap(TEXT, text_w)[: (box_h - PAD * 2) // LINE_H]):
        d.text((box_x + PAD, box_y + PAD + i * LINE_H), line, fill=(232, 220, 255), font=font)
    widest = max(len(t) * CHAR_W for t, _ in CHOICES)
    cw = max(120, min(box_w, widest + 34))
    button = tex("gui/dialogue/button")
    for i, (text, enabled) in enumerate(CHOICES):
        y = box_y - 4 - (len(CHOICES) - i) * (BUTTON_H + 2)
        state = 0 if enabled else 3
        if i == 1:
            state = 1
        hslice(img, button, box_x + box_w - cw, y, cw, BUTTON_H, 4, v=state * 20, region_w=64)
        d.text((box_x + box_w - cw + 7, y + 5), str(i + 1), fill=(244, 201, 90) if enabled else (119, 112, 138), font=font)
        d.text((box_x + box_w - cw + 18, y + 5), text, fill=(232, 220, 255) if enabled else (119, 112, 138), font=font)
    hint = "Enter/Leertaste weiter · 1-4 Antwort · L Verlauf · Esc schliessen"
    if len(hint) * CHAR_W > width - 8:
        hint = "Enter · 1-4 · L · Esc"
    d.text(((width - len(hint) * CHAR_W) // 2, box_y + box_h + 5), hint, fill=(154, 140, 184), font=font)
    return img


def main():
    out = sys.argv[1]
    win_w = int(sys.argv[2]) if len(sys.argv) > 2 else 1280
    win_h = int(sys.argv[3]) if len(sys.argv) > 3 else 720
    shots = []
    for scale in (1, 2, 3, 4):
        gui = render(win_w // scale, win_h // scale).resize((win_w // scale * scale, win_h // scale * scale), Image.NEAREST)
        shots.append(gui.resize((gui.width // 2, gui.height // 2), Image.NEAREST))
    sheet = Image.new("RGB", (shots[0].width * 2 + 10, shots[0].height * 2 + 10), (30, 30, 40))
    for i, shot in enumerate(shots):
        sheet.paste(shot, ((i % 2) * (shots[0].width + 10), (i // 2) * (shots[0].height + 10)))
        ImageDraw.Draw(sheet).text(((i % 2) * (shots[0].width + 10) + 6, (i // 2) * (shots[0].height + 10) + 4),
                                   f"GUI-Skalierung {i + 1}", fill=(255, 255, 255))
    sheet.save(out)
    print("gespeichert:", out, sheet.size)


if __name__ == "__main__":
    main()
