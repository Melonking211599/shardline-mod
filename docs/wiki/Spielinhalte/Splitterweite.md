---
tags: [shardline, spielinhalte, dimension]
---

# Splitterweite

Dimension `shardline:splitterweite`: schwebende Inseln aus Kristallgestein über einem dunklen Void. Erreichbar über das [[Splitterportal]].

## Dimensionseigenschaften

| Eigenschaft | Wert | Auswirkung |
|---|---|---|
| Tageszeit | fest auf 18000 (Mitternacht) | ewige Nacht, Himmelslicht stark gedämpft |
| Umgebungslicht | 0,08 | sehr dunkel. Das Shard-Set gibt hier Nachtsicht, die Leuchtbeere kurz ebenfalls |
| Höhe | Y 0 bis 255 | unter Y 0 liegt das Void |
| Koordinatenmaßstab | 4 | 4 Blöcke Oberwelt entsprechen 1 Block Splitterweite |
| Betten / Seelenanker | funktionieren nicht | Betten explodieren wie im Nether oder End |
| Kompass / Uhr | natürlich = nein | sie drehen sich wild |
| Wetter | kein Niederschlag | |

## Gelände

- **Inseln:** Eine 3D-Rauschfunktion formt die Inseln, eine großflächige 2D-Maske reißt dazwischen weite Void-Lücken auf.
- **Höhenband:** Zwei Höhengradienten halten die Inseln etwa zwischen Y 40 und Y 190. Die Oberkanten liegen meist bei Y 58 bis 170.
- **Verteilung:** Auf etwa der Hälfte der Fläche (rund 48 %) liegt mindestens eine Insel, der Rest ist offenes Void (gemessen im Servertest).
- **Gestein und Wasser:** Standardgestein ist **Splitterstein**. Es gibt weder Wasser noch Lava und keine Aquifere.

## Biome

Die Biomwahl hängt von Temperatur und Feuchtigkeit ab. Die Biome sind einige hundert Blöcke groß.

| Biom | Anteil (Messung) | Oberfläche | Besonderheiten |
|---|---|---|---|
| **Kristallwald** (`crystal_forest`) | ca. 56 % | Kristallmoos | Kristallbäume, Leuchtblumen, Splitterfarn, Leuchtbeerensträucher, hängende Kristallcluster. **Nur hier stehen Koloss-Arenen.** |
| **Aschefelder** (`ash_fields`) | ca. 20 % | Ascheboden (2 Schichten) | vereinzelte Kristallbäume, Splitterfarn, Kristallcluster am Boden, weiße Asche in der Luft |
| **Leuchtende Höhlen** (`glowing_caves`) | ca. 23 % | Kristallmoos, Decken mit Leuchtkristall | Höhlen in den Inseln, dichte Leuchtkristall-Adern, Leuchtkappen, sehr viele hängende Cluster |

**In allen Biomen:**
- Shard-Erz (14 Adern pro Chunk, Y 20–200, bis zu 8 Erze)
- Leuchtkristall-Adern
- Ruinen-Türme

### Atmosphäre

- **Himmel:** eigener violetter Sternennebel, der sich langsam dreht, mit einem zerbrochenen Kristallmond.
- **Wolken:** Es gibt keine Wolken.
- **Nebel:** dicht und violett. Sichtweite standardmäßig 96 Blöcke, einstellbar in der [[Konfiguration#Client]].
- **Partikel:** Kristallfunkeln im Kristallwald und in den Höhlen, weiße Asche in den Aschefeldern.
- **Musik:** eigener Slot `shardline:music.splitterweite`, derzeit verweist er auf die End-Musik.
- **Hintergrundklang:** Ambient-Loop wie im Wirrwald, dazu Höhlengeräusche.

## Spawns in der Splitterweite

Feindliche Mobs spawnen hier **unabhängig vom Licht**, also auch neben Fackeln. Ausnahme ist die friedliche Schwierigkeit. Alle Gewichte, Spawnchancen und Gruppengrößen stehen in [[Mobs#Spawn-Übersicht]].

## Oberwelt

Die Mod verändert auch die Oberwelt.

| Was | Wo | Menge |
|---|---|---|
| Tiefenschiefer-Shard-Erz | alle Oberwelt-Biome, Y −64 bis 0 (häufigste Höhe Y −32) | 3 Adern pro Chunk, bis zu 5 Erze, 50 % weniger an Luft |
| Splitterkriecher, Aschewandler, Kristallspinne, Schattenfuchs | alle Oberwelt-Biome außer Pilzinseln, **nur nachts und im Dunkeln** | selten, zusätzlich durch den Oberwelt-Faktor 0,35 gedämpft |
| Leuchtmotte | wie oben, nachts | selten |
| Shardvirus | nach dem Bruch in allen Dimensionen (`virus.dimensions`) | neue Herde alle 4 Minuten nahe Spielern, siehe [[Story und Shardvirus#Shardvirus]] |
| Splitterkrater | Ebenen, Savannen, Wüsten, Tafelberge, Wiesen | siehe [[Strukturen#Splitterkrater]] |

Die Oberwelt-Spawns lassen sich in der [[Konfiguration]] abschalten oder anpassen.

## Nach dem Bruch

Stirbt die Splitterkönigin, färbt sich der Himmel in allen Dimensionen mit Tageslicht violett, und der Shardvirus breitet sich aus – in der Splitterweite, der Oberwelt, dem Nether (Stamm **Glutbrand**) und dem End (Stamm **Leerenfäule**). Infizierte Gebiete haben eigenen violetten Nebel, eigene Musik und Sporen-Partikel (abschaltbar in der [[Konfiguration#Client]]). Ab Kapitel 7 bebt die Weite regelmäßig nach, dabei entstehen **Schwären** (siehe [[Nach dem Ausbruch#Schwären und Nachbeben]]).
