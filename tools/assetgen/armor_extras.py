"""3D-Zusatzteile der Ruestungen (Kristallkamm, Schulterplatten, Rueckenkristalle, Knieschuetzer, Stiefelflossen).

Die Vanilla-Ruestungstexturen (armor.py) koennen die Silhouette nicht veraendern. Diese Teile werden von
client/renderer/ArmorExtrasLayer.java zusaetzlich an Kopf, Koerper, Arme und Beine gehaengt.
Aufruf: python tools/assetgen/armor_extras.py   (auch Teil von run_all.py)

Erzeugt aus EINER Beschreibung (SETS) beides, damit Textur und Modell nie auseinanderlaufen:
  * textures/models/armor/<set>_extras.png und <set>_extras_glow.png   (64x64)
  * src/main/java/eu/shardline/mod/client/renderer/ArmorExtraModels.java

Koordinaten in Pixeln im Modellraum des jeweiligen Koerperteils (16 Pixel = 1 Block, y zeigt nach unten,
Vorderseite ist -z). Linke Seiten werden aus den rechten gespiegelt. Gleich grosse Teile teilen sich dieselbe Textur.
"""
from __future__ import annotations

import math
from dataclasses import dataclass, field
from pathlib import Path

from armor import STYLES, ArmorStyle, gem_px, plate
from common import PROJECT, TEX, Tex, ramp
from uvlib import FACE_LIGHT, box_extent, paint_box

JAVA_OUT = PROJECT / "src" / "main" / "java" / "eu" / "shardline" / "mod" / "client" / "renderer" / "ArmorExtraModels.java"


@dataclass
class Cube:
    group: str                      # helmet, torso, right_shoulder, left_shoulder, right_knee, left_knee, right_boot, left_boot
    kind: str                       # plate | crystal | gem | core
    size: tuple                     # (w, h, d) ganzzahlig
    pivot: tuple = (0.0, 0.0, 0.0)
    off: tuple = (0.0, 0.0, 0.0)    # Ecke der Box relativ zum Drehpunkt
    rot: tuple = (0.0, 0.0, 0.0)    # (x, y, z) Radiant
    uv: tuple = field(default=(0, 0))


def box(group, kind, x, y, z, w, h, d):
    """Unrotierte Box mit absoluter Ecke (x, y, z)."""
    return Cube(group, kind, (w, h, d), (0.0, 0.0, 0.0), (x, y, z))


def spike(group, kind, bx, by, bz, w, h, d, rx=0.0, ry=0.0, rz=0.0):
    """Spitze, die an ihrer Basismitte (bx, by, bz) haengt und nach oben zeigt; Rotation um die Basis."""
    return Cube(group, kind, (w, h, d), (bx, by, bz), (-w / 2, -h, -d / 2), (rx, ry, rz))


def mirror(c: Cube, group: str) -> Cube:
    px, py, pz = c.pivot
    ox, oy, oz = c.off
    w, h, d = c.size
    rx, ry, rz = c.rot
    return Cube(group, c.kind, c.size, (-px, py, pz), (-(ox + w), oy, oz), (rx, -ry, -rz))


def both(group_r, group_l, cubes):
    return cubes + [mirror(c, group_l) for c in cubes]


def tip(base, angle_z, h):
    """Spitze eines Segments nach Drehung um z (Basis -> oberes Ende)."""
    return (base[0] + h * math.sin(angle_z), base[1] - h * math.cos(angle_z), base[2])


# ====================================================================== Shard-Set: schlank, hell, spitz
def shard_cubes():
    c = []
    # Helm: Kristallkamm (mittig hoch, vorn kleine Spitzen, hinten nach hinten geneigt), Stirnleiste, Wangenflossen
    c.append(spike("helmet", "crystal", 0, -8.5, 0, 2, 7, 2))
    c.append(spike("helmet", "crystal", 0, -8.5, 2.5, 2, 5, 2, rx=-0.6))
    c += [spike("helmet", "crystal", -2.6, -8.5, -1.5, 1, 4, 1, rx=0.3, rz=-0.35),
          spike("helmet", "crystal", 2.6, -8.5, -1.5, 1, 4, 1, rx=0.3, rz=0.35)]
    c.append(box("helmet", "plate", -3.5, -7.4, -5.6, 7, 1, 1))
    c.append(box("helmet", "gem", -0.5, -7.4, -5.8, 1, 1, 1))
    c += [box("helmet", "plate", -6.0, -5.5, -1.5, 1, 3, 3), box("helmet", "plate", 5.0, -5.5, -1.5, 1, 3, 3)]
    # Brustplatte: Rueckenkristalle und Kristall vorn
    c.append(spike("torso", "crystal", 0, 4.0, 3.4, 2, 9, 2, rx=-0.28))
    c += [spike("torso", "crystal", -3.0, 5.0, 3.4, 2, 6, 1, rx=-0.2, rz=-0.35),
          spike("torso", "crystal", 3.0, 5.0, 3.4, 2, 6, 1, rx=-0.2, rz=0.35)]
    c.append(box("torso", "gem", -1.0, 2.5, -3.4, 2, 3, 1))
    # Schulterplatten mit zwei Spitzen
    r = [box("right_shoulder", "plate", -4.6, -3.9, -3.0, 5, 1, 6),
         spike("right_shoulder", "crystal", -3.8, -3.8, -1.5, 2, 5, 2, rx=-0.15, rz=-0.45),
         spike("right_shoulder", "crystal", -2.4, -3.8, 1.8, 1, 3, 1, rx=-0.45, rz=-0.3)]
    c += both("right_shoulder", "left_shoulder", r)
    # Knieschuetzer (Hose)
    k = [spike("right_knee", "crystal", -1.9, 7.0, -3.0, 2, 4, 1, rx=0.9)]
    c += both("right_knee", "left_knee", k)
    # Stiefel: Knoechelflossen
    b = [box("right_boot", "plate", -3.4, 8.0, -1.5, 1, 3, 3), spike("right_boot", "crystal", -3.6, 10.0, 1.2, 1, 3, 1, rx=-0.9, rz=-0.3)]
    c += both("right_boot", "left_boot", b)
    return c


# ====================================================================== Koloss-Set: wuchtig, Platten, Kernkristall
def colossus_cubes():
    c = []
    # Helm: Stirnplatte, Mittelflosse, zwei geschwungene Hoerner aus zwei Segmenten
    c.append(box("helmet", "plate", -4.5, -7.4, -5.6, 9, 1, 1))
    c.append(box("helmet", "gem", -1.0, -7.1, -6.1, 2, 1, 1))
    c.append(box("helmet", "plate", -0.5, -11.5, -3.5, 1, 3, 7))
    c.append(spike("helmet", "crystal", 0, -11.5, -4.0, 2, 4, 2, rx=0.35))
    horn = []
    base = (-5.6, -6.0, 0.0)
    a1, a2, h1, h2 = -0.9, -0.15, 4, 6
    horn.append(spike("helmet", "plate", base[0], base[1], base[2], 2, h1, 2, rz=a1))
    p2 = tip(base, a1, h1 - 0.8)
    horn.append(spike("helmet", "crystal", p2[0], p2[1], p2[2], 2, h2, 2, rz=a2))
    c += horn + [mirror(h, "helmet") for h in horn]
    # Brustplatte: Kernkristall auf dem Ruecken, zwei hohe Flankenkristalle, Kragenplatte vorn
    c.append(box("torso", "core", -2.0, 1.5, 2.9, 4, 7, 2))
    c += [spike("torso", "crystal", -3.6, 4.5, 3.2, 2, 7, 2, rx=-0.2, rz=-0.5), spike("torso", "crystal", 3.6, 4.5, 3.2, 2, 7, 2, rx=-0.2, rz=0.5)]
    c.append(box("torso", "plate", -4.5, -0.4, -3.3, 9, 1, 1))
    c.append(box("torso", "gem", -1.5, 3.0, -3.4, 3, 3, 1))
    # Schulterpanzer: grosse Platte, zweite kleinere Platte, drei Spitzen
    r = [box("right_shoulder", "plate", -5.4, -4.3, -4.0, 6, 2, 8),
         box("right_shoulder", "plate", -4.4, -5.3, -2.5, 4, 1, 5),
         spike("right_shoulder", "crystal", -4.8, -4.3, -3.0, 2, 3, 2, rx=0.3, rz=-0.55),
         spike("right_shoulder", "crystal", -4.8, -4.3, 3.0, 2, 3, 2, rx=-0.3, rz=-0.55)]
    c += both("right_shoulder", "left_shoulder", r)
    # Knieschuetzer: Platte mit Spitze
    k = [box("right_knee", "plate", -2.5, 5.0, -3.0, 3, 3, 1), spike("right_knee", "crystal", -1.0, 5.2, -2.8, 2, 3, 1, rx=0.9)]
    c += both("right_knee", "left_knee", k)
    # Stiefel: Flossen und Spitzenplatte
    b = [box("right_boot", "plate", -3.2, 8.0, -1.5, 1, 3, 3), box("right_boot", "plate", -2.0, 10.0, -3.8, 4, 1, 1),
         spike("right_boot", "crystal", -3.8, 10.0, 1.4, 1, 4, 2, rx=-0.8, rz=-0.25)]
    c += both("right_boot", "left_boot", b)
    return c


# Mittelpunkte der Koerperteile im jeweiligen Teilraum und Abstand, um den die schlanke Ruestung naeher am Koerper liegt
CENTER = {"helmet": (0, -4, 0), "torso": (0, 6, 0), "right_shoulder": (-1, 4, 0), "left_shoulder": (1, 4, 0),
          "right_knee": (0, 6, 0), "left_knee": (0, 6, 0), "right_boot": (0, 6, 0), "left_boot": (0, 6, 0)}
TIGHT = {"right_knee": 0.2, "left_knee": 0.2}
TIGHT_DEFAULT = 0.45


def tighten(cubes):
    """Verschiebt jedes Teil, das ausserhalb des Koerperteils sitzt (>= 3 Pixel von der Mitte), um 0,45 nach innen
    (Hose 0,2): die schlanken Ruestungsmodelle (SlimArmorModels) liegen so viel enger an als die Vanilla-Ruestung."""
    for c in cubes:
        d = TIGHT.get(c.group, TIGHT_DEFAULT)
        ref = CENTER[c.group]
        rotated = c.rot != (0.0, 0.0, 0.0)
        w, h, dd = c.size
        point = c.pivot if rotated else (c.off[0] + w / 2, c.off[1] + h / 2, c.off[2] + dd / 2)
        shift = [0.0, 0.0, 0.0]
        for i in range(3):
            delta = point[i] - ref[i]
            if abs(delta) >= 3:
                shift[i] = -d if delta > 0 else d
        if rotated:
            c.pivot = (c.pivot[0] + shift[0], c.pivot[1] + shift[1], c.pivot[2] + shift[2])
        else:
            c.off = (c.off[0] + shift[0], c.off[1] + shift[1], c.off[2] + shift[2])
    return cubes


def build_set(name):
    return tighten(SETS_RAW[name]())


SETS_RAW = {"shard": shard_cubes, "colossus": colossus_cubes}
SETS = {n: (lambda n=n: build_set(n)) for n in SETS_RAW}


# ====================================================================== Malen
def painter_for(st: ArmorStyle, kind: str, name: str):
    colossus = st.name == "colossus"

    def p(face, fx, fy, fw, fh):
        if kind == "plate":
            if face == "down":
                return ramp(st.trim, 0.8)
            col = plate(st, face, fx, fy, fw, fh, 91)
            if colossus and fw >= 4 and fh >= 2 and fy == fh - 1 and face in ("north", "east", "west", "south"):
                return (st.gem[2], True)
            return col
        if kind == "crystal":
            band = 0.8 if fx < max(1, fw // 2) else -0.3
            idx = 2.2 + FACE_LIGHT[face] * 0.9 + band * (1 if fw > 1 else 0)
            if face == "up":
                return (ramp(st.r, 4.0), True)
            if fy == 0:
                return (ramp(st.r, 4.0), True)
            if fy >= fh - 1:
                idx -= 0.9
            glow = colossus and fy <= 1
            return (ramp(st.r, idx), glow)
        if kind == "gem":
            return (ramp(st.gem, 3.0 + FACE_LIGHT[face] * 0.8 + (0.8 if fx == 0 and fy == 0 else 0)), True)
        if kind == "core":
            ring = abs(fx - (fw - 1) / 2) / max(1, fw / 2) + abs(fy - (fh - 1) / 2) / max(1, fh / 2)
            return (ramp(st.gem, 4.0 - ring * 2.2 + FACE_LIGHT[face] * 0.5), True)
        return ramp(st.r, 2)

    return p


# ====================================================================== Atlas
def allocate(cubes, size=64):
    """Weist jedem eindeutigen (kind, w, h, d) einen Platz im Atlas zu (Regalpackung)."""
    keys = []
    for c in cubes:
        k = (c.kind, *c.size)
        if k not in keys:
            keys.append(k)
    ext = {k: box_extent(0, 0, k[1], k[2], k[3]) for k in keys}
    order = sorted(keys, key=lambda k: (-ext[k][1], -ext[k][0]))
    x = y = shelf = 0
    place = {}
    for k in order:
        w, h = ext[k]
        if x + w > size:
            x, y, shelf = 0, y + shelf, 0
        if y + h > size:
            raise SystemExit(f"Atlas {size}x{size} zu klein fuer {len(keys)} Teile")
        place[k] = (x, y)
        x += w
        shelf = max(shelf, h)
    for c in cubes:
        c.uv = place[(c.kind, *c.size)]
    return place


def fmt(v):
    s = f"{v:.3f}".rstrip("0").rstrip(".")
    return (s if s not in ("", "-0") else "0") + "F"


def java_for(set_name, cubes):
    groups = []
    for c in cubes:
        if c.group not in groups:
            groups.append(c.group)
    lines = [f"    public static LayerDefinition {set_name}() {{",
             "        MeshDefinition mesh = new MeshDefinition();",
             "        PartDefinition root = mesh.getRoot();"]
    for g in groups:
        lines.append(f'        PartDefinition {g} = root.addOrReplaceChild("{g}", CubeListBuilder.create(), PartPose.ZERO);')
        n = 0
        for c in (x for x in cubes if x.group == g):
            w, h, d = c.size
            px, py, pz = c.pivot
            rx, ry, rz = c.rot
            ox, oy, oz = c.off
            lines.append(f'        {g}.addOrReplaceChild("c{n}", CubeListBuilder.create().texOffs({c.uv[0]}, {c.uv[1]})'
                         f'.addBox({fmt(ox)}, {fmt(oy)}, {fmt(oz)}, {w}, {h}, {d}), '
                         f'PartPose.offsetAndRotation({fmt(px)}, {fmt(py)}, {fmt(pz)}, {fmt(rx)}, {fmt(ry)}, {fmt(rz)}));')
            n += 1
    lines.append("        return LayerDefinition.create(mesh, 64, 64);")
    lines.append("    }")
    return "\n".join(lines)


HEADER = '''package eu.shardline.mod.client.renderer;

import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

/**
 * Zusatzteile der Ruestungen. DATEI WIRD ERZEUGT von tools/assetgen/armor_extras.py, nicht von Hand aendern.
 * Gruppen: helmet, torso, right_shoulder, left_shoulder, right_knee, left_knee, right_boot, left_boot.
 * Texturen: textures/models/armor/<set>_extras.png (+ _glow.png), 64x64.
 */
public final class ArmorExtraModels {
    private ArmorExtraModels() {
    }

'''


def main():
    styles = {s.name: s for s in STYLES}
    methods = []
    for name, build in SETS.items():
        st = styles[name]
        cubes = build()
        allocate(cubes)
        tex, glow = Tex(64, 64), Tex(64, 64)
        done = set()
        for c in cubes:
            k = (c.kind, *c.size)
            if k in done:
                continue
            done.add(k)
            w, h, d = c.size
            paint_box(tex, c.uv[0], c.uv[1], w, h, d, painter_for(st, c.kind, name), glow)
        tex.save(TEX / "models" / "armor" / f"{name}_extras.png")
        glow.save(TEX / "models" / "armor" / f"{name}_extras_glow.png")
        methods.append(java_for(name, cubes))
    JAVA_OUT.write_text(HEADER + "\n\n".join(methods) + "\n}\n", encoding="utf-8")
    print(f"armor_extras: {len(SETS)} Sets (Textur + Glow + {JAVA_OUT.name})")


if __name__ == "__main__":
    main()
