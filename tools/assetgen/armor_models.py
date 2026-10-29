"""Ruestungsmodelle (GeckoLib) fuer Shard- und Colossus-Set. Aufruf: python tools/assetgen/armor_models.py

Erzeugt geo/armor/<set>.geo.json, textures/armor/<set>.png (+ _glowmask.png) und animations/armor/<set>.animation.json.
Geometrie in Bedrock-Koordinaten (y nach oben, Vorderseite -Z, rechts = negatives x), Bones mit Standardpivots.
Vorschau: python tools/assetgen/armor_models.py --preview <aus.png>
"""
from __future__ import annotations

import sys

from armorkit import Model, preview
from gear import COLOSSUS_P, SHARD_P


def shard() -> Model:
    m = Model(SHARD_P)
    H, B = "armorHead", "armorBody"
    # ---------------------------------------------------------------- Helm: Kuppel, Brauenwulst, Nasensteg, Wangenplatten, Kamm
    m.add(H, "skull", (-4, 27, -4), (8, 3, 8), "plate", inflate=0.55)
    m.add(H, "dome", (-3, 30, -3), (6, 2, 6), "plate", inflate=0.5)
    m.add(H, "bevel_f", (-3, 30.2, -4.6), (6, 2, 1), "lame", inflate=0.2, rot=(-38, 0, 0), pivot=(0, 30.6, -3.4))
    m.pair(H, "bevel_s", (-4.9, 29.4, -3), (1, 2, 6), "lame", inflate=0.2, rot=(0, 0, -32), pivot=(-4.4, 30, 0))
    m.add(H, "band", (-4, 25.2, -4), (8, 2, 8), "lame", inflate=0.6)
    m.add(H, "brow", (-4, 25.0, -5.3), (8, 2, 1), "lame", inflate=0.2)
    m.add(H, "nasal", (-1, 22.8, -5.2), (2, 4, 1), "plate", inflate=0.1, groove=True)
    m.pair(H, "cheek", (-5.0, 22.6, -3), (1, 4, 6), "plate", inflate=0.2, rot=(0, 0, -8), pivot=(-4.5, 26.5, 0))
    m.add(H, "nape", (-4, 23, 3.6), (8, 5, 1), "plate", inflate=0.3, rot=(-12, 0, 0), pivot=(0, 28, 4))
    for i, (z, h, tilt) in enumerate(((-2.8, 3, 8), (-1.0, 4, 18), (0.9, 5, 28), (2.7, 4, 38), (4.3, 3, 48))):
        m.pair(H, f"crest{i}", (-1.3, 32.2, z), (1, h, 2), "crystal", rot=(tilt, 0, 7), pivot=(-0.8, 32.2, z + 1))
    m.add(H, "gem", (-1, 27.2, -5.5), (2, 2, 1), "gem")
    # ---------------------------------------------------------------- Brustplatte: V-Platten, Kristallsteg, Lamellen, Guertel, Kragen
    m.add(B, "under", (-4, 12, -2), (8, 12, 4), "under", inflate=0.3)
    m.add(B, "pec_r", (-4.2, 16, -3.0), (4, 7, 1), "plate", inflate=0.25, rot=(0, 10, 0), pivot=(0, 16, -3), rivets=True)
    m.add(B, "pec_l", (0.2, 16, -3.0), (4, 7, 1), "plate", inflate=0.25, rot=(0, -10, 0), pivot=(0, 16, -3), rivets=True)
    m.add(B, "sternum", (-1, 16, -3.9), (2, 6, 1), "crystal", dark=True)
    m.add(B, "rib1", (-4, 14.6, -2.9), (8, 1, 1), "lame", inflate=0.3)
    m.add(B, "rib2", (-4, 13.2, -2.9), (8, 1, 1), "lame", inflate=0.3)
    m.add(B, "belt", (-4, 11.6, -2.9), (8, 2, 1), "plate", inflate=0.4, rivets=True)
    m.add(B, "buckle", (-1, 11.4, -3.6), (2, 2, 1), "gem")
    m.add(B, "back", (-4, 12.4, 2.0), (8, 10, 1), "plate", inflate=0.4, groove=True)
    for i, (y, h) in enumerate(((13, 2), (16, 3), (19.5, 3))):
        m.add(B, f"spine{i}", (-0.5, y, 3.2), (1, h, 1), "crystal", dark=True, rot=(22, 0, 0), pivot=(0, y, 3.2))
    m.add(B, "collar_f", (-2.5, 23.0, -3.0), (5, 2, 1), "plate", inflate=0.2)
    m.pair(B, "collar_s", (-4.6, 23.0, -2.2), (1, 3, 4), "plate", inflate=0.1, rot=(0, 0, 10), pivot=(-4, 23, 0))
    m.add(B, "collar_b", (-3, 23.0, 2.0), (6, 3, 1), "plate", inflate=0.2, rot=(-10, 0, 0), pivot=(0, 23, 2))
    # ---------------------------------------------------------------- Arme: dreilagige Schulter, Kristall, Oberarm, Ellbogenspitze, Unterarmschiene
    A = "armorRightArm"
    m.pair(A, "sleeve", (-8, 12, -2), (4, 12, 4), "under", inflate=0.25)
    m.pair(A, "pauld1", (-9.3, 21.6, -3.4), (5, 2, 6), "plate", inflate=0.2, rot=(0, 0, 9), pivot=(-5, 22, 0), rivets=True)
    m.pair(A, "pauld2", (-8.9, 19.9, -3.1), (5, 2, 6), "plate", inflate=0.2, rot=(0, 0, 5), pivot=(-5, 22, 0))
    m.pair(A, "pauld3", (-8.5, 18.3, -2.8), (4, 2, 6), "plate", inflate=0.2, rot=(0, 0, 2), pivot=(-5, 22, 0))
    m.pair(A, "shcrystal", (-7.4, 23.0, -1), (2, 4, 2), "crystal", rot=(0, 0, 24), pivot=(-6, 23, 0))
    m.pair(A, "upper", (-8, 15.2, -2), (4, 3, 4), "plate", inflate=0.55)
    m.pair(A, "elbow", (-9.3, 13.6, -0.5), (1, 3, 1), "crystal", rot=(0, 0, 20), pivot=(-8.6, 13.6, 0))
    m.pair(A, "vambrace", (-8, 12, -2), (4, 3, 4), "plate", inflate=0.65, groove=True)
    # ---------------------------------------------------------------- Beine: Huefteplatten, Oberschenkel, Knieschale
    L = "armorRightLeg"
    m.pair(L, "under", (-4, 4, -2), (4, 8, 4), "under", inflate=0.25)
    m.pair(L, "fauld1", (-4.6, 9.2, -2.9), (5, 3, 5), "plate", inflate=0.2, rot=(6, 0, 0), pivot=(-2, 12, 0), rivets=True)
    m.pair(L, "fauld2", (-4.5, 7.1, -2.8), (5, 2, 5), "lame", inflate=0.2, rot=(3, 0, 0), pivot=(-2, 12, 0))
    m.pair(L, "thigh_out", (-4.9, 5.8, -1.5), (1, 4, 3), "plate", inflate=0.1)
    m.pair(L, "knee", (-3.6, 4.9, -3.4), (3, 3, 2), "plate", inflate=0.1, rot=(14, 0, 0), pivot=(-2, 6.5, -2.8), rivets=True)
    # ---------------------------------------------------------------- Stiefel: Stulpe, Schienbein, Fuss, Spitzkappe, Ferse, Knoechelkristall
    S = "armorRightBoot"
    m.pair(S, "cuff", (-4, 6.2, -2), (4, 1, 4), "plate", inflate=0.95)
    m.pair(S, "greave", (-4, 2.8, -2), (4, 3, 4), "plate", inflate=0.6)
    m.pair(S, "foot", (-4, 0, -2.6), (4, 2, 5), "shell", inflate=0.45)
    m.pair(S, "toe", (-3.7, 0, -5.4), (3, 2, 3), "plate", inflate=0.2, rot=(12, 0, 0), pivot=(-2.2, 0, -4))
    m.pair(S, "toe_tip", (-2.6, 0.4, -6.6), (1, 1, 2), "crystal")
    m.pair(S, "heel", (-3.7, 0, 2.2), (3, 3, 1), "plate", inflate=0.3)
    m.pair(S, "ankle", (-5.0, 3.4, -0.5), (1, 3, 1), "crystal", rot=(0, 0, 18), pivot=(-4.5, 3.4, 0))
    return m


def colossus() -> Model:
    m = Model(COLOSSUS_P)
    H, B = "armorHead", "armorBody"
    # ---------------------------------------------------------------- Helm: schwerer Visierhelm, Brauenplatte, Kinnschutz, Hoerner
    m.add(H, "skull", (-4, 28, -4), (8, 4, 8), "plate", inflate=0.6)
    m.add(H, "band", (-4, 25.2, -4), (8, 3, 8), "plate", inflate=0.7, groove=True)
    m.add(H, "brow", (-4, 27.0, -5.8), (8, 2, 2), "plate", inflate=0.2)
    m.add(H, "slit", (-3, 26.0, -5.2), (6, 1, 1), "gem")
    m.add(H, "chin", (-3, 23.2, -5.0), (6, 2, 1), "plate", inflate=0.2)
    m.pair(H, "cheek", (-5.0, 22.8, -3.5), (1, 5, 6), "plate", inflate=0.25, rot=(0, 0, -6), pivot=(-4.5, 27.5, 0), rivets=True)
    m.add(H, "nape", (-4, 22.6, 3.5), (8, 6, 1), "plate", inflate=0.3, rot=(-10, 0, 0), pivot=(0, 28, 4))
    m.add(H, "stone", (-1, 29.2, -5.4), (2, 2, 1), "gem")
    m.pair(H, "horn", (-5.6, 28.6, -1.5), (2, 4, 4), "plate", inflate=0.1, rot=(0, 0, 24), pivot=(-4.6, 28.6, 0), rivets=True)
    # ---------------------------------------------------------------- Brustplatte: Plattenpanzer, Kerngehaeuse mit Cyan-Kern, Guertel
    m.add(B, "under", (-4, 12, -2), (8, 12, 4), "under", inflate=0.3)
    m.add(B, "pec", (-4, 16.2, -3.0), (8, 7, 1), "plate", inflate=0.3, rivets=True, rune=True)
    m.add(B, "housing", (-2, 17.2, -4.1), (4, 4, 1), "plate", inflate=0.2, lift=-1)
    m.add(B, "core", (-1, 18.2, -4.8), (2, 2, 1), "gem")
    m.add(B, "rib1", (-4, 14.8, -2.9), (8, 1, 1), "lame", inflate=0.35)
    m.add(B, "rib2", (-4, 13.4, -2.9), (8, 1, 1), "lame", inflate=0.35)
    m.add(B, "belt", (-4, 11.5, -3.0), (8, 2, 1), "plate", inflate=0.45, rivets=True)
    m.add(B, "buckle", (-2, 11.2, -3.9), (4, 3, 1), "plate", inflate=0.1, lift=1)
    m.add(B, "buckle_gem", (-1, 11.9, -4.6), (2, 1, 1), "gem")
    m.add(B, "back", (-4, 12.4, 2.0), (8, 11, 1), "plate", inflate=0.45, groove=True, rune=True)
    m.pair(B, "blade", (-3.4, 16.5, 3.3), (3, 5, 1), "plate", inflate=0.1, rot=(14, 0, 0), pivot=(-2, 16.5, 3.3))
    m.add(B, "collar_f", (-3, 23.0, -3.2), (6, 2, 1), "plate", inflate=0.25)
    m.pair(B, "collar_s", (-4.8, 23.0, -2.4), (1, 4, 5), "plate", inflate=0.1, rot=(0, 0, 8), pivot=(-4, 23, 0))
    m.add(B, "collar_b", (-4, 23.0, 2.0), (8, 4, 1), "plate", inflate=0.25, rot=(-10, 0, 0), pivot=(0, 23, 2))
    # ---------------------------------------------------------------- Arme: Plattenschulter, Stachel, Oberarm, Ellbogenplatte, Unterarmschiene
    A = "armorRightArm"
    m.pair(A, "sleeve", (-8, 12, -2), (4, 12, 4), "under", inflate=0.25)
    m.pair(A, "pauld1", (-10.0, 21.8, -3.6), (6, 2, 7), "plate", inflate=0.25, rot=(0, 0, 7), pivot=(-5, 22, 0), rivets=True, rune=True)
    m.pair(A, "pauld2", (-9.6, 19.8, -3.5), (5, 2, 7), "plate", inflate=0.25, rot=(0, 0, 3), pivot=(-5, 22, 0))
    m.pair(A, "pspike", (-9.2, 23.4, -0.5), (1, 3, 1), "plate", inflate=0.1, rot=(0, 0, 26), pivot=(-8.6, 23.4, 0))
    m.pair(A, "upper", (-8, 15.5, -2), (4, 4, 4), "plate", inflate=0.6)
    m.pair(A, "elbow", (-8, 13.8, -2), (4, 2, 4), "plate", inflate=1.0, rivets=True)
    m.pair(A, "vambrace", (-8, 12, -2), (4, 2, 4), "plate", inflate=0.75, groove=True, rune=True)
    # ---------------------------------------------------------------- Beine: schwere Huefteplatten, Oberschenkel, grosse Knieplatte
    L = "armorRightLeg"
    m.pair(L, "under", (-4, 4, -2), (4, 8, 4), "under", inflate=0.25)
    m.pair(L, "fauld1", (-4.7, 8.8, -2.9), (5, 4, 5), "plate", inflate=0.25, rot=(5, 0, 0), pivot=(-2, 12, 0), rivets=True)
    m.pair(L, "fauld2", (-4.6, 6.8, -2.8), (5, 2, 5), "lame", inflate=0.25, rot=(2, 0, 0), pivot=(-2, 12, 0))
    m.pair(L, "thigh_out", (-4.95, 5.2, -1.5), (1, 5, 3), "plate", inflate=0.1, groove=True)
    m.pair(L, "knee", (-4, 4.2, -3.8), (4, 4, 2), "plate", inflate=0.15, rot=(12, 0, 0), pivot=(-2, 6.5, -2.8), rivets=True, rune=True)
    # ---------------------------------------------------------------- Stiefel: Stulpe, Schienbein, wuchtige Kappe, Ferse
    S = "armorRightBoot"
    m.pair(S, "cuff", (-4, 6.2, -2), (4, 1, 4), "plate", inflate=1.0)
    m.pair(S, "greave", (-4, 2.6, -2), (4, 4, 4), "plate", inflate=0.8, rune=True)
    m.pair(S, "foot", (-4, 0, -2.6), (4, 2, 5), "shell", inflate=0.5)
    m.pair(S, "toe", (-4, 0, -5.4), (4, 3, 3), "plate", inflate=0.25, rot=(10, 0, 0), pivot=(-2, 0, -4), rivets=True)
    m.pair(S, "heel", (-4, 0, 2.2), (4, 3, 1), "plate", inflate=0.35)
    return m


SETS = {"shard": shard, "colossus": colossus}


def compare(models, out, yaws=(0, 35, 90, 180), scale=11):
    """Zeilen: Netherite (Vanilla), Shard, Colossus; Spalten: Blickwinkel. Nur zur Kontrolle."""
    from PIL import Image
    from armorkit import mannequin_items, model_items, render_layers, vanilla_reference
    cell_w, cell_h = 26 * scale, 40 * scale
    rows = [("netherite", None)] + [(n, models[n]) for n in ("shard", "colossus") if n in models]
    sheet = Image.new("RGB", (cell_w * len(yaws), cell_h * len(rows)), (58, 62, 78))
    for r, (name, data) in enumerate(rows):
        if data is None:
            fg = vanilla_reference("netherite")
            if fg is None:
                continue
        else:
            fg = model_items(data[0], data[1], data[2])
        for c, yaw in enumerate(yaws):
            sheet.paste(render_layers([mannequin_items(), fg], yaw, scale, (cell_w, cell_h), cell_w // 2, cell_h - 8), (c * cell_w, r * cell_h))
    sheet.save(out)
    print("gespeichert:", out)


def main():
    args = sys.argv[1:]
    only = [a for a in args if a in SETS]
    out = args[args.index("--preview") + 1] if "--preview" in args else None
    models = {}
    for name, fn in SETS.items():
        if only and name not in only:
            continue
        model = fn()
        tex, glow = model.write(name)
        print(f"armor_models: {name}: {len(model.cubes)} Wuerfel, Atlas {model.tex_w}x{model.tex_h}")
        if out:
            preview(model, tex, glow, out.replace(".png", f"_{name}.png"))
            models[name] = (model, tex, glow)
    if out and "--compare" in args:
        compare(models, out.replace(".png", "_compare.png"))


if __name__ == "__main__":
    main()
