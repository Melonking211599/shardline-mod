# Shardline – Stilbibel (verbindlich für alle Texturskripte)

Gilt für jede neue und überarbeitete Textur. Paletten stehen als Konstanten in `common.py`, der Werkzeugkasten in `pixel.py`.

## Regeln

1. **Licht** kommt von oben links, Schatten fallen nach unten rechts. Jede Rampe hat 4–5 Stufen. Keine Zwischenverläufe, keine Weichzeichner, keine Alpha-Verläufe (außer Overlays, Partikel und die Schimmerkanten der GUI).
2. **Kontur**
   - Items und Pflanzen: 1 px dunkle Kontur in einem abgedunkelten Ton des Materials (`Tex.selout`), nie reines Schwarz.
   - Blöcke: keine Kontur. Einzelteile in einem Block (Ziegel, Bretter, Facetten) haben eine hellere Oberkante und eine dunklere Unterkante. Natürliche Blöcke (Stein, Moos, Asche, Erde) haben **keine** Kantenfase am Kachelrand, sonst entsteht auf 3×3 ein Gitter.
3. **Form vor Detail.** Jede Textur muss bei 1× an der Silhouette erkennbar sein. Ein Detail bleibt nur, wenn es bei 1× noch lesbar ist. Werkzeugköpfe, Helme, Handschuhe, Gesichter von Wesen entscheiden über die Silhouette.
4. **Rauschen** ist immer strukturiert: Flecken (`patches`), Maserung (gestreckte Zellen, z. B. `cells=(8, 2)` senkrecht, `(2, 8)` waagerecht), Schichten, Adern (`walk`). Nie gleichmäßiges Pixelrauschen, nie Stempel auf einem Raster (`spots` hält Mindestabstand ohne Gitter).
5. **Leuchten** nur an Adern, Augen, Kristallspitzen, Runen und Zeichen. Nie flächig. Die Glowmaske deckt exakt diese Pixel ab.
6. **Farbwelten** (max. 2 Akzentfarben pro Textur):

   | Welt | Grundton | Akzente | Konstanten |
   |---|---|---|---|
   | Splitterweite | Schiefer/Violett | Cyan, Magenta nur sparsam | `SHARDSTONE`, `VIOLET`, `SHARD`, `CYAN`, `TEAL`, `CWOOD` |
   | Virus | Schwarzviolett | Magenta, Pink | `VIRUS`, `VGLOW`, `MAGENTA` |
   | Reinheit | Elfenbein | Gold, Himmelblau | `IVORY`, `GOLD`, `SKYB` |
   | Aschland | Grau | Orange-Glut | `ASH`, `EMBER` |
7. **Vanilla-Nähe:** Proportionen und Pixelraster wie Vanilla (Schwerter diagonal mit 1 px Klinge und Lichtkante, Rüstungsicons mit klaren Teilen und Lücken, Wesen mit lesbarem Gesicht). Kein Pixel außerhalb der Boxen einer UV-Fläche.

## Kachelblöcke

* Alle Flächen kacheln (Wrap-around). Auf 3×3 darf kein Gitter aus gleichen Stempeln sichtbar sein.
* Anteile der Tonstufen sind fest (`rank_tones`): z. B. Stein 0/27/50/21/2 %. Dadurch sehen alle Varianten gleich ruhig aus.
* **Varianten:** Familien mit großen Strukturen bekommen 2–3 Seeds. `<name>.png` ist Seed 1, `<name>_v2.png` und `<name>_v3.png` die weiteren. Das Datagen kann sie als zufällige Modelle eintragen (siehe Abschlussbericht).

## Werkzeugkasten (`pixel.py`)

| Funktion | Zweck |
|---|---|
| `patches(seed, ramp, fractions, cells, detail)` | kachelnde Tonflecken mit festen Anteilen |
| `patch_idx(...)` + `paint(idx, ramp)` | dasselbe als Index-Raster, z. B. für Ziegel, die den Ton pro Pixel brauchen |
| `walk(seed, x, y, length, heading)` | zusammenhängender Pfad für Risse und Adern |
| `spots(seed, count, gap)` | verteilte Punkte ohne Gitter |
| `tnoise`, `field`, `rank_tones`, `despeckle` | Bausteine darunter |

## Varianten (für das Datagen, noch nicht verdrahtet)

Jede Variante ist eine eigene Datei mit eigenem Seed; `<name>.png` bleibt die Hauptvariante. Infizierte Blöcke brauchen zusätzlich `<name>_vN_glow.png` (liegt bei, animierte Leuchtschichten haben ihre `.mcmeta`).

| Familie | Dateien |
|---|---|
| Splitterstein | `shardstone_v2`, `shardstone_v3` |
| Erze | `shard_ore_v2`, `deepslate_shard_ore_v2` |
| Kristallmoos (Oberseite) | `crystal_moss_v2`, `crystal_moss_v3` |
| Asche | `ash_block_v2`, `ash_block_v3` |
| Kristalllaub | `crystal_leaves_v2`, `crystal_leaves_v3` |
| Infizierter Stein / Erde / Sand | `infected_{stone,dirt,sand}_v2`, `_v3` (je mit `_glow`) |
| Infiziertes Gras (Oberseite) / Laub | `infected_{grass_top,leaves}_v2`, `_v3` (je mit animierter `_glow` und `.mcmeta`) |

## Prüfen

* `python tools/assetgen/validate.py` prüft zusätzlich: keine bemalten Pixel außerhalb der UV-Boxen, Glowmaske ⊂ Basistextur (gleiche Farbe), sichtbare Kachelnähte (Verhältnis Randsprung zu Innensprung ≤ 1,3, Erze ≤ 1,6), keine Leuchtpixel auf transparentem Laub, genug Lücken im Kristalllaub.
* `python tools/assetgen/review_sheets.py blocks|items|atlas <aus.png> <pfade…> [--before <alte textures/>] [--glow]` rendert 3×3-Kacheln, Items auf Slot (groß und in 1×) und Atlanten, auf Wunsch neben dem alten Stand.
* `python tools/assetgen/preview_geo.py <aus.png> <wesen…> --yaws 20,200 --scale 12` zeigt Wesen als 3D-Modell.
* Vorher/Nachher-Blätter der Polish-Runde: `tools/assetgen/polish_before_after/`.
