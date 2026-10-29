"""Vergleichsbogen: eigene Icons neben den Vanilla-Netherite-Icons (nur zur Kontrolle, schreibt nichts in die Mod).

Aufruf: python tools/assetgen/compare_vanilla.py <aus.png> [--scale 8] [--before ORDNER] [--parts helmet,axe,...]
Die Vanilla-Texturen werden zur Laufzeit aus dem Minecraft-Client-Jar im Gradle-Cache gelesen und nie kopiert.
Zeilen: Netherite, (alter Stand Shard/Colossus mit --before), Shard neu, Colossus neu. Rechts je Icon die 1x-Groesse.
"""
from __future__ import annotations

import glob
import io
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

from common import TEX

PARTS = ("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel")
BG = (44, 46, 58)
SLOT = (139, 139, 139)


def vanilla_jar():
    hits = glob.glob(str(Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_*_client.jar"))
    return hits[0] if hits else None


def vanilla(name: str):
    jar = vanilla_jar()
    if not jar:
        return None
    with zipfile.ZipFile(jar) as z:
        try:
            return Image.open(io.BytesIO(z.read(f"assets/minecraft/textures/item/{name}.png"))).convert("RGBA")
        except KeyError:
            return None


def load(root: Path, name: str):
    p = root / "item" / f"{name}.png"
    return Image.open(p).convert("RGBA") if p.exists() else None


def sheet(out: str, scale: int = 8, before: Path | None = None, parts=PARTS):
    rows = [("Netherite", lambda p: vanilla(f"netherite_{p}"))]
    if before:
        rows += [("Shard alt", lambda p: load(before, f"shard_{p}")), ("Colossus alt", lambda p: load(before, f"colossus_{p}"))]
    rows += [("Shard", lambda p: load(TEX, f"shard_{p}")), ("Colossus", lambda p: load(TEX, f"colossus_{p}"))]
    cell = 16 * scale + 8
    W = 110 + len(parts) * (cell + 22)
    H = 8 + len(rows) * (cell + 8)
    im = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(im)
    for r, (label, fn) in enumerate(rows):
        y = 8 + r * (cell + 8)
        d.text((6, y + cell // 2 - 5), label, fill=(230, 230, 240))
        for c, part in enumerate(parts):
            x = 110 + c * (cell + 22)
            d.rectangle([x, y, x + cell - 1, y + cell - 1], fill=SLOT)
            icon = fn(part)
            if icon is None:
                continue
            big = icon.resize((16 * scale, 16 * scale), Image.NEAREST)
            im.paste(big, (x + 4, y + 4), big)
            d.rectangle([x + cell + 2, y, x + cell + 19, y + 17], fill=SLOT)
            im.paste(icon, (x + cell + 3, y + 1), icon)
    im.save(out)
    print(f"gespeichert: {out} {im.size}")


if __name__ == "__main__":
    args = sys.argv[1:]
    sc = int(args[args.index("--scale") + 1]) if "--scale" in args else 8
    bf = Path(args[args.index("--before") + 1]) if "--before" in args else None
    pt = tuple(args[args.index("--parts") + 1].split(",")) if "--parts" in args else PARTS
    sheet(args[0], sc, bf, pt)
