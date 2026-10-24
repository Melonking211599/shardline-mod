"""Texturen fuer "Nach dem Ausbruch", Kapitel 7-9. Aufruf: python tools/assetgen/aftermath_assets.py

  item/echo_shard            Echosplitter: blasser Kristall, um den sich zwei Schallringe legen
  item/keeper_tuning_fork    Stimmgabel der Hueter: zwei Goldzinken, Elfenbeingriff, Rune
  item/chronicle_journal     Chronik-Tagebuch (Auftragsbuch): Leder, Goldkante, Emblem "Ringe"
  block/festering_node       Schwaere: verseuchtes Fleisch mit Kristallherz, Adern pulsieren (_glow, 4 Frames)
  item/chapter_emblem_7, _8_seal, _8_dominate, _9_seal, _9_dominate   Kapitelembleme im Rahmen von story2_assets
  entity/echo_queen          Nachhall: die Koenigin als blasser Geist (aus shard_queen umgefaerbt, Alpha in der Textur)
  entity/festering_heart     Schwaerenherz: das Virusherz in Eiter-Gelb und Rost (aus virus_heart umgefaerbt)
Umgefaerbte Texturen behalten UV und Helligkeitsverlauf des Originals; nur die Rampe wechselt.
"""
from __future__ import annotations

from PIL import Image

from common import CYAN, GOLD, IVORY, LEATHER, SKYB, TEX, VGLOW, VIRUS, Tex, hx, ramp, save_png
from fresh_virus import infect, pulse_frames, roots_at, veins, write_mcmeta
from pixel import patches
from story2_assets import EMBLEMS, chapter_emblem, put
from story_items import EDGE, book

# ------------------------------------------------------------------ Items
PALE = [hx(c) for c in ("#2c4a58", "#5f8fa0", "#9fd0dc", "#d6f4fa", "#ffffff")]


def echo_shard() -> Tex:
    """Aufrechter Kristall (links hell, rechts dunkel) mit kleinem Nebensplitter; zwei Schallbogen rechts daneben."""
    t = Tex(16, 16)
    put(t, """
.....4.......
....443......
....4332.....
...44332.....
...43322.....
...43322.....
.4.43322.....
443433221....
4324332211...
.3213322.....
..21.321.....
......1......
""", {"4": PALE[4], "3": PALE[3], "2": PALE[2], "1": PALE[1]}, 1, 2)
    for (x, y) in ((11, 4), (12, 5), (12, 6), (12, 7), (11, 8)):
        t.set(x, y, CYAN[3])
    for (x, y) in ((13, 3), (14, 4), (14, 5), (14, 6), (14, 7), (14, 8), (13, 9)):
        t.set(x, y, CYAN[2])
    t.outline(EDGE)
    return t


def keeper_tuning_fork() -> Tex:
    """Stimmgabel schraeg (Griff unten links): Goldzinken mit Lichtkante, Elfenbeingriff mit Goldring, Cyan-Rune."""
    t = Tex(16, 16)
    put(t, """
..........g..g..
.........gG.gG..
........gG.gG...
.......gG.gG....
......gGggG.....
.....gGGGg......
.....gGGg.......
....iI..........
...iIi..........
..iIi...........
.iIi............
.ii.............
""", {"g": GOLD[2], "G": GOLD[4], "i": IVORY[2], "I": IVORY[4]}, 1, 2)
    t.set(6, 8, GOLD[1])
    t.set(7, 7, CYAN[3])
    t.outline(EDGE)
    return t


def chronicle_journal() -> Tex:
    epal = {"x": GOLD[3], "y": CYAN[3], "z": GOLD[1]}
    return book(LEATHER, GOLD, "rings", epal, ribbon=CYAN, glow_corner=False)


# ------------------------------------------------------------------ Schwaere (Block)
FLESH = [hx(c) for c in ("#1a0714", "#2e0c22", "#461432", "#611f44", "#7d2c58")]


def festering_node():
    """Verseuchtes Fleisch, viele kurze Adern und Kristallspitzen; in der Mitte ein pulsierendes Kristallherz."""
    base = patches(8101, FLESH, fractions=(0.06, 0.24, 0.42, 0.22, 0.06), cells=(4, 4), detail=0.4)
    base, glow = infect(base, 8105, veins(8103, roots_at(8103, 3), length=13, fork=0.3), shards=3, spores=5, tint=0.6)
    heart = {(7, 6): 4, (8, 6): 3, (6, 7): 3, (7, 7): 4, (8, 7): 4, (9, 7): 2, (6, 8): 2, (7, 8): 3, (8, 8): 3, (9, 8): 2,
             (7, 9): 2, (8, 9): 1}
    for (x, y), v in heart.items():
        base.set(x, y, VIRUS[min(4, v + 1)])
        glow.set(x, y, VGLOW[v])
    return base, glow


# ------------------------------------------------------------------ Kapitelembleme 7-9 (Rahmen aus story2_assets)
EMBLEMS.update({
    "7": ("""
..........
...cccc...
..c....c..
.c..mm..c.
.c.mggm.c.
.c.mggm.c.
.c..mm..c.
..c....c..
...cccc...
..........
""", {"c": CYAN[2], "m": VGLOW[2], "g": VGLOW[4]}, VIRUS[0]),                       # Echo der Wunde: Schallringe um den Riss
    "8_seal": ("""
....gg....
...gGGg...
..gGGGGg..
.gGGGGGGg.
..iIIIIi..
..iI..Ii..
..iI..Ii..
..iIssIi..
..iiiiii..
..........
""", {"g": GOLD[2], "G": GOLD[4], "i": IVORY[1], "I": IVORY[3], "s": SKYB[3]}, hx("#1a1406")),   # Wiederaufbau: Haus
    "8_dominate": ("""
..........
.m..m...m.
.mm.mm.mm.
.mMmMMmMm.
.mMMMMMMm.
.mMMgMMMm.
.mmmmmmmm.
..........
..........
..........
""", {"m": VIRUS[3], "M": VGLOW[2], "g": VGLOW[4]}, hx("#12040e")),              # Herrschaft: Kristallkrone
    "9_seal": ("""
...g..g...
...g..g...
...g..g...
...gGGg...
....gg....
....ii....
....ii....
..c.ii.c..
.c..ii..c.
..........
""", {"g": GOLD[2], "G": GOLD[4], "i": IVORY[3], "c": SKYB[2]}, hx("#0a1420")),    # Nachhall: Stimmgabel
    "9_dominate": ("""
..........
..mm..mm..
.mMMmmMMm.
.mMMg.MMm.
..mMg.Mm..
...mg.m...
....gm....
.....m....
..........
..........
""", {"m": hx("#7a3a10"), "M": hx("#c0701c"), "g": hx("#ffd060")}, hx("#140804")),  # Gegengesang: gesprungenes Herz
})


# ------------------------------------------------------------------ Umgefaerbte Bosse
GHOST = [hx(c) for c in ("#3a4a5e", "#6c8098", "#a6bccf", "#d8e8f2", "#ffffff")]
GHOST_GLOW = [hx(c) for c in ("#3fb8c8", "#7fe0ec", "#b8f4fa", "#ffffff")]
PUS = [hx(c) for c in ("#160a04", "#341a06", "#5e360c", "#956014", "#cf9a2c")]
PUS_GLOW = [hx(c) for c in ("#a05a10", "#e09a28", "#ffd060", "#fff0a0")]


def recolor(src: str, dst: str, ramp_, alpha_scale: float = 1.0):
    """Ersetzt jede Farbe durch die Rampe an ihrer Helligkeit (Alpha bleibt, optional skaliert)."""
    im = Image.open(TEX / "entity" / f"{src}.png").convert("RGBA")
    out = Tex(im.width, im.height)
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = (0.3 * r + 0.55 * g + 0.15 * b) / 255.0
            c = ramp(ramp_, min(len(ramp_) - 1.0, lum * (len(ramp_) - 1) * 1.15))
            out.set(x, y, (c[0], c[1], c[2], int(a * alpha_scale)))
    save_png(out, f"entity/{dst}")


def main():
    save_png(echo_shard(), "item/echo_shard")
    save_png(keeper_tuning_fork(), "item/keeper_tuning_fork")
    save_png(chronicle_journal(), "item/chronicle_journal")
    base, glow = festering_node()
    save_png(base, "block/festering_node")
    save_png(pulse_frames(glow, lo=0.35), "block/festering_node_glow")
    write_mcmeta("festering_node_glow", 6)
    for key in ("7", "8_seal", "8_dominate", "9_seal", "9_dominate"):
        save_png(chapter_emblem(key), f"item/chapter_emblem_{key}")
    recolor("shard_queen", "echo_queen", GHOST, 0.8)
    recolor("shard_queen_glowmask", "echo_queen_glowmask", GHOST_GLOW)
    recolor("virus_heart", "festering_heart", PUS)
    recolor("virus_heart_glowmask", "festering_heart_glowmask", PUS_GLOW)
    print("aftermath_assets: echo_shard, keeper_tuning_fork, chronicle_journal, festering_node (+Glow), 5 Kapitelembleme, "
          "echo_queen, festering_heart (+Glowmasken)")


if __name__ == "__main__":
    main()
