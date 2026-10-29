---
tags: [shardline, spielinhalte, strukturen]
---

# Strukturen

| Struktur | ID | Wo | Raster / Mindestabstand (Chunks) |
|---|---|---|---|
| [[#Splitterkrater]] | `shard_crater` | Oberwelt: Ebenen, Savannen, Wüsten, Tafelberge, Wiesen | 40 / 14 |
| [[#Ruinen-Turm]] | `ruin_tower` | Splitterweite, alle Biome | 20 / 8 |
| [[#Hüter-Schrein]] | `keeper_shrine` | Kristallwald | 26 / 9 |
| [[#Handelsposten]] | `trader_outpost` | Splitterweite, alle Biome | 32 / 12 |
| [[#Aschegruft]] | `ash_crypt` | Aschefelder | 24 / 8 |
| [[#Kristallgeode]] | `crystal_geode` | Leuchtende Höhlen | 22 / 7 |
| [[#Hüter-Ruinen]] | `keeper_ruin` | Splitterweite, alle Biome | 28 / 10 |
| [[#Überlebendenlager]] | `survivor_camp` | Kristallwald, Aschefelder | 44 / 16 |
| [[#Hüter-Bibliothek]] | `keeper_library` | Splitterweite, alle Biome | 40 / 14 |
| [[#Koloss-Arena]] | `colossus_arena` | Kristallwald | 36 / 14 |
| [[#Königinnen-Arena]] | `queen_arena` | Chunk 0/0 | fest |

Der **Resonanzkompass** (siehe [[Blöcke und Items]]) findet in der Splitterweite alle Bauwerke der Weite, in der Oberwelt den Splitterkrater. Schleichen + Rechtsklick wechselt das Ziel.

Alle elf Bauwerke werden **prozedural im Code** Block für Block gebaut. Es gibt keine `.nbt`-Vorlagen. Jedes Bauwerk errichtet sein eigenes kegelförmiges Fundament und funktioniert dadurch auch am Rand schwebender Inseln. Die Chunks bis 6 Chunks um den Ursprung bleiben für die Königinnen-Arena frei.

## Ruinen-Turm

| | |
|---|---|
| ID | `shardline:ruin_tower` |
| Biome | alle drei Biome der [[Splitterweite]] |
| Häufigkeit | im Raster von 20 Chunks, mindestens 8 Chunks Abstand |
| Bedingung | Unter mindestens 3 der 4 Turmecken muss Boden in ähnlicher Höhe liegen (±6 Blöcke) |

**Aufbau:**
- **Turm:** rund, Radius 4, 14 bis 20 Blöcke hoch, aus Splitterstein-Ziegeln (25 % rissig).
- **Verfall:** Die Mauerkrone ist teilweise eingestürzt, in der Wand fehlen vereinzelt Blöcke.
- **Fundament:** ein Kegel aus Splitterstein, 9 Blöcke tief.
- **Eingang:** Tür an der Nordseite, Fenster an Ost- und Westseite, Leuchtkristalle in der Wand.
- **Stockwerke:** Zwischenböden alle 5 Blöcke, verbunden durch eine durchgehende Leiter an der Südwand.

**Inhalt:**
- **Erdgeschoss:** eine Truhe mit Beute `chests/ruin_tower`.
- **Oberstes Stockwerk:** bei der Hälfte der Türme eine Schatztruhe mit Beute `chests/ruin_tower_treasure`.
- **Wachen:** 1–2 **Gesplitterte Ritter**. Sie bleiben dauerhaft und halten sich in 16 Blöcken Umkreis um ihren Turm.
- **Händler:** In jedem vierten Turm wartet oben ein [[Splitterhändler]].
- **Nachspawnen:** Innerhalb des Turms spawnen als Monster nur weitere Gesplitterte Ritter.

**Truhenbeute (4–7 Stapel):**
- Rohstoffe: Shard-Splitter, Roher Shard, Shard-Barren, Eisen, Gold, Obsidian, Enderperle
- Pflanzen und Nahrung: Leuchtbeeren, Leuchtblumen
- zufällig verzaubertes Buch

**Schatztruhe (2–4 Stapel):**
- Rohstoffe: Shard-Barren, Diamanten, Ritterwappen, Erweckungskristall, Goldene Äpfel
- Ausrüstung: verzauberte Shard-Rüstung und Shard-Schwert (Stufe 15–30), Kristallbogen

## Koloss-Arena

| | |
|---|---|
| ID | `shardline:colossus_arena` |
| Biome | nur **Kristallwald** |
| Häufigkeit | im Raster von 36 Chunks, mindestens 14 Chunks Abstand |
| Höhe | auf dem Boden; über dem Void schwebend auf Y 96 |
| Auffinden | Karte des [[Splitterhändler]]s (Struktur-Tag `on_colossus_maps`) |

**Aufbau:**
- **Kampffläche:** rund, Radius 18, aus Splitterstein-Ziegeln, mit einem leuchtenden Ring auf Radius 9 und Kristallmoos am Rand.
- **Freiraum:** Über der Fläche werden 22 Blöcke freigeräumt.
- **Randmauer:** Sie hat vier Eingänge in den Himmelsrichtungen.
- **Säulen:** acht Stück auf Radius 14, 7–10 Blöcke hoch, gekrönt von Leuchtkristall und einem Cluster.
- **Mitte:** der **Koloss-Altar** auf einem Podest aus Stufen mit vier Kristallclustern.
- **Fundament:** ein Kegel, 12 Blöcke tief.

**Vorratstruhe (3–5 Stapel)** am Nordrand:
- Leuchtkristall, Leuchtbeeren, Shard-Barren, Pfeile
- Goldene Karotten, gebratenes Rindfleisch
- Tränke und Wurftränke der Heilung II

## Königinnen-Arena

| | |
|---|---|
| ID | `shardline:queen_arena` |
| Lage | **immer** im Chunk 0/0, Mitte bei X 8 / Z 8, Boden auf Y 112 |
| Platzierung | eigene Platzierungsart `shardline:fixed_chunk` (genau ein Chunk) |

**Aufbau:**
- **Plattform:** rund, schwebend, Radius 28, aus Splitterstein-Ziegeln mit spiralförmigen Leuchtkristall-Adern.
- **Freiraum:** Über der Plattform werden 32 Blöcke freigeräumt.
- **Säulen:** zwölf Stück auf Radius 24 (2 × 2 Blöcke), 12–18 Blöcke hoch. Etwa 30 % davon sind eingestürzt.
- **Mitte:** der **Thron der Splitterweite** auf Y 115, auf einem zweistufigen Podest.
- **Fundament:** ein Kegel, 18 Blöcke tief, mit hängenden Kristallen an der Unterseite.

**Erinnerungskristalle:** Vision 3 „Das Ritual“ vor dem Thron, Vision 7 „Die Warnung“ dahinter (siehe [[Story und Shardvirus#Erinnerungskristalle (8 Visionen)]]).

**Entscheidungsaltar:** fünf Blöcke vor dem Thron. Er wächst aus dem Boden, wenn die Königin fällt; neue Arenen haben ihn von Anfang an (siehe [[Story und Shardvirus#Endentscheidung]]).

**Zwei Schatztruhen (je 4–6 Stapel)** links und rechts vom Podest:
- Shard-Barren, Diamanten, Shard-Block, Netheritplatten, Kolosskern, Enderperlen
- Totem der Unsterblichkeit, verzauberter Goldener Apfel
- hoch verzaubertes Buch (Stufe 25–39)

## Splitterkrater

Oberwelt-Struktur (`shardline:shard_crater`) in Ebenen, Savannen, Wüsten, Tafelbergen und Wiesen: eine Mulde aus Asche mit einem Brocken voller Shard-Erz, verstreuten Portalrahmen-Blöcken und der Truhe eines Wanderers (selten mit Shard-Schlüssel, Lore-Buch „Notizen eines Wanderers“). Der erste Hinweis auf die Splitterweite.

## Hüter-Schrein

Säulenkreis im Kristallwald mit einem **Erinnerungskristall** (zufällige Vision), einer Opfertruhe und Leuchtmotten. Lore-Bücher: Kristallreich Aurelis, Eid der Hüter, Selvaine.

## Handelsposten

Kristallholz-Hütte in allen Biomen der Weite mit einem [[Splitterhändler]] und einer Vorratstruhe. Lore-Bücher: Notizen eines Wanderers, Kassenbuch des Händlers.

## Aschegruft

Vergrabene Grabkammer in den Aschefeldern: Schacht mit Leiter, Sarkophag, zwei Truhen, ein Erinnerungskristall (Vision 4 oder 6), dazu ein Aschewandler und eine Kristallspinne als Wächter.

## Kristallgeode

Halb vergrabene Hohlkugel aus Leuchtkristall in den Leuchtenden Höhlen, mit Shard-Erz, Kristallspinnen und einer Truhe.

## Hüter-Ruinen

Eingestürzter Versammlungssaal der Hüter aus hellem Hüterstein, nach Süden zu eingestürzt, Säulen an Ecken und Seiten. Ein Runenpfad führt zum Podest am Nordende mit einem Erinnerungskristall (Aurelis, Der falsche Ton oder meist Der Rat der Hüter) und einer Truhe mit mindestens einem Lore-Buch und mehreren losen Lore-Seiten. Advancement „Wo die Hüter tagten“.

## Überlebendenlager

`shardline:survivor_camp` (Kapitel 7–9, siehe [[Nach dem Ausbruch]]): runde Lichtung mit Palisade, Lagerfeuer, drei Zelten mit Betten, Ilsas Werkstatt (Amboss, Braustand) und einer Vorratstruhe (`chests/survivor_camp`). Rhea, Ilsa und Kesh erscheinen mit Heimatpunkt. Im Lager spawnen keine Monster, und im Umkreis `aftermath.campRadius` (32) sind die NPCs unverwundbar.

## Hüter-Bibliothek

`shardline:keeper_library` (Kapitel 8–9, versiegelter Zweig): ein langer Saal (15 × 19 Blöcke) aus Hüterstein mit Säulen, hohen Kristallglasfenstern und einem zur Südostecke hin eingestürzten Runendach. Zwei Reihen Bücherregale bilden einen Mittelgang mit vier Lesepulten und Kristalllicht. Am Nordende steht das Archiv: ein Podest mit Truhe (`chests/keeper_library`: Lore-Bücher, Echosplitter). In der Mitte schwebt **Veyl**, der Geist des letzten Archivars; hier erwacht in Kapitel 9 der [[Bosse#Der Nachhall|Nachhall]] der Königin.

> [!info] Alte Welten und `/locate`
> - `/locate structure shardline:<id>` funktioniert für alle Strukturen außer der Königinnen-Arena, die immer bei 0/0 liegt.
> - Neue Strukturen entstehen in bestehenden Welten nur in **neu generierten Chunks**. Zum Testen: `/place structure shardline:survivor_camp`.
