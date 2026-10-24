"""Vorschau des Auftragsbuchs (QuestJournalScreen) aus den echten GUI-Texturen, ohne Spielclient.

Aufruf: python tools/assetgen/preview_journal.py <ausgabe.png> [fensterbreite fensterhoehe]
Rechnet Groesse und Verkleinerung wie QuestJournalScreen.init() fuer GUI-Skalierung 1 bis 4 und zeigt alle vier
(jeweils auf die echte Pixelgroesse vergroessert). Text in einer Ersatzschrift (6 px pro Zeichen). Nur Kontrolle von
Platz und Ueberlappungen; schreibt nichts in die Mod.
"""
from __future__ import annotations

import sys

from PIL import Image, ImageDraw, ImageFont

from common import TEX

BOOK_W, BOOK_H, TAB_W, TAB_H = 256, 192, 32, 28
BOOK_Y = TAB_H - 3
LIST_X, PANEL_Y, LIST_W, PANEL_H, DETAIL_X, DETAIL_W, ROW_H, LINE_H = 8, 22, 96, 162, 110, 138, 12, 10
CHAR_W = 6
ROWS = [("Die erste Schwäre", True, False), ("Echoernte", True, False), ("Holz für das Lager", False, True),
        ("Ritter ohne Herrn", True, False)]
DETAIL = ["Die erste Schwäre", "Auftrag von Rhea Tannwald", "", "Seit dem letzten Beben schwärt der Boden. Kesh hört, wo.", "",
          "✔ Sprich mit Kesh", "▸ Bring eine Schwäre mit der Stimmgabel zum Schweigen", "…"]


def tex(name):
    return Image.open(TEX / f"{name}.png").convert("RGBA")


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


def book() -> Image.Image:
    """Das Buch in Buchkoordinaten (256 x 217), Reiter 2 aktiv."""
    img = Image.new("RGBA", (BOOK_W, BOOK_H + BOOK_Y), (0, 0, 0, 0))
    tabs = tex("gui/journal/tabs")
    active = 1
    for i in range(4):
        if i != active:
            img.alpha_composite(tabs.crop((i * TAB_W, TAB_H, i * TAB_W + TAB_W, 2 * TAB_H)), (10 + i * (TAB_W + 2), 2))
    img.alpha_composite(tex("gui/journal/background"), (0, BOOK_Y))
    img.alpha_composite(tabs.crop((active * TAB_W, 0, active * TAB_W + TAB_W, TAB_H)), (10 + active * (TAB_W + 2), 0))
    d = ImageDraw.Draw(img)
    font = ImageFont.load_default()
    d.text((12, BOOK_Y + 6), "Aufträge", fill=(244, 201, 90), font=font)
    d.text((BOOK_W - 12 - 9 * CHAR_W, BOOK_Y + 6), "Kapitel 7", fill=(140, 127, 174), font=font)
    top = BOOK_Y + PANEL_Y + 4
    icons = tex("gui/dialogue/icons")
    for i, (label, active_q, done) in enumerate(ROWS):
        y = top + i * ROW_H
        if i == 0:
            d.rectangle((LIST_X + 3, y, LIST_X + LIST_W - 4, y + ROW_H - 2), fill=(63, 42, 120, 102))
        max_chars = (LIST_W - 10 - (9 if done else 0)) // CHAR_W
        text = label if len(label) <= max_chars else label[:max_chars - 1] + "…"
        d.text((LIST_X + 5, y + 2), text, fill=(244, 201, 90) if active_q else (140, 127, 174), font=font)
        if done:
            img.alpha_composite(icons.crop((24, 0, 32, 8)), (LIST_X + LIST_W - 13, y + 2))
    x, y = DETAIL_X + 6, BOOK_Y + PANEL_Y + 6
    lines = []
    for para in DETAIL:
        lines += [""] if not para else wrap(para, DETAIL_W - 12)
    for i, line in enumerate(lines[: (PANEL_H - 12) // LINE_H]):
        d.text((x, y + i * LINE_H), line, fill=(232, 220, 255), font=font)
    return img


def render(width, height):
    img = Image.new("RGBA", (width, height), (60, 80, 60, 255))
    d = ImageDraw.Draw(img)
    for y in range(height):
        d.line([(0, y), (width, y)], fill=(110, 140, 190, 255) if y < height * 0.55 else (70, 95, 55, 255))
    img.alpha_composite(Image.new("RGBA", (width, height), (0, 0, 0, 110)))
    scale = min(1.0, (width - 8) / BOOK_W, (height - 8) / (BOOK_H + BOOK_Y))
    b = book()
    if scale < 1.0:
        b = b.resize((int(b.width * scale), int(b.height * scale)), Image.BILINEAR)
    img.alpha_composite(b, (int((width - b.width) / 2), int((height - b.height) / 2)))
    return img, scale


def main():
    out = sys.argv[1]
    win_w = int(sys.argv[2]) if len(sys.argv) > 2 else 1280
    win_h = int(sys.argv[3]) if len(sys.argv) > 3 else 720
    shots = []
    for gui_scale in (1, 2, 3, 4):
        gui, scale = render(win_w // gui_scale, win_h // gui_scale)
        gui = gui.resize((gui.width * gui_scale, gui.height * gui_scale), Image.NEAREST)
        shot = gui.resize((gui.width // 2, gui.height // 2), Image.NEAREST)
        ImageDraw.Draw(shot).text((6, 4), f"GUI-Skalierung {gui_scale} (Buch x{scale:.2f})", fill=(255, 255, 255))
        shots.append(shot)
    sheet = Image.new("RGB", (shots[0].width * 2 + 10, shots[0].height * 2 + 10), (30, 30, 40))
    for i, shot in enumerate(shots):
        sheet.paste(shot, ((i % 2) * (shots[0].width + 10), (i // 2) * (shots[0].height + 10)))
    sheet.save(out)
    print("gespeichert:", out, sheet.size)


if __name__ == "__main__":
    main()
