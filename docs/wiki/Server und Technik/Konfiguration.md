---
tags: [shardline, technik, konfiguration]
---

# Konfiguration

Es gibt zwei Dateien, die beim ersten Start angelegt werden. Im Spiel lassen sich alle Werte auch über **Mods → Shardline → Konfigurieren** ändern.

| Datei | Gilt für | Inhalt |
|---|---|---|
| `config/shardline-common.toml` | Server und Client | Spawns, Mobs, Bosse, Ausrüstung, Dimension, Story, Dialoge, Nach dem Ausbruch, Shardvirus, Gegenmittel, Ende, Fähigkeiten |
| `config/shardline-client.toml` | nur Client | Nebel, Portal-Overlay, Virus-Atmosphäre, Bildschirmeffekte, Kapitelkarte, Dialogfenster |

> [!note] Wann Änderungen greifen
> - **Spawnchancen** und **Fähigkeitswerte** (Schild, Faust, Herz) werden bei jeder Nutzung neu gelesen.
> - **Boss-HP und Schaden** gelten für **neu beschworene** Bosse.
> - Der **Schadensmultiplikator der Mobs** gilt für **neu gespawnte** Mobs, bereits existierende behalten ihre Werte.

## Spawns

| Schlüssel | Standard | Bereich | Bedeutung |
|---|---|---|---|
| `spawns.overworldSpawns` | `true` | – | Shardline-Mobs nachts in der Oberwelt |
| `spawns.overworldSpawnChance` | `0.35` | 0–1 | zusätzlicher Faktor für alle Oberwelt-Spawns |
| `spawns.spawnChances.<mob>` | `1.0` | 0–1 | Chance pro natürlichem Spawnversuch; 0 schaltet den Mob ab |

Der Platzhalter `<mob>` steht für einen dieser Werte: `shard_creeper`, `crystal_golem`, `glow_moth`, `ash_walker`, `shard_scout`, `void_ray`, `crystal_spider`, `shard_trader`, `shadow_fox`, `shattered_knight`, `crystal_snail`.

## Mobs

| Schlüssel | Standard | Bereich | Bedeutung |
|---|---|---|---|
| `mobs.mobDamageMultiplier` | `1.0` | 0–10 | Faktor für den Nahkampfschaden aller normalen Mobs |
| `mobs.scoutProjectileDamage` | `5.0` | 0–100 | Schaden pro Projektil des Shard-Spähers |
| `mobs.creeperShardCount` | `10` | 0–64 | Splitter pro Splitterkriecher-Explosion |
| `mobs.creeperShardDamage` | `4.0` | 0–100 | Schaden pro Splitter |
| `mobs.creeperBreaksBlocks` | `false` | – | Explosion zerstört Blöcke |
| `mobs.golemAggroRadius` | `12` | 1–64 | Radius, in dem Bergbau Kristallgolems verärgert |
| `mobs.ashWalkerBlindnessSeconds` | `3` | 0–60 | Blindheit durch den Aschewandler |
| `mobs.spiderWebChance` | `0.3` | 0–1 | Chance auf ein Kristallnetz pro Treffer |
| `mobs.foxInvisibilitySeconds` | `6` | 1–60 | Unsichtbarkeit des Schattenfuchses |

## Bosse

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `bosses.colossusHealth` | `400.0` | Lebenspunkte Kristallkoloss |
| `bosses.colossusAttackDamage` | `18.0` | Nahkampfschaden |
| `bosses.colossusSlamDamage` | `14.0` | maximaler Schaden des Bodenstampfers |
| `bosses.colossusPillarDamage` | `10.0` | Schaden pro Kristallsäule |
| `bosses.colossusMinionCount` | `3` | Splitterkriecher pro Ruf in Phase 2 (0–16) |
| `bosses.queenHealth` | `600.0` | Lebenspunkte Splitterkönigin |
| `bosses.queenProjectileDamage` | `8.0` | Schaden pro Projektil |
| `bosses.queenCloneCount` | `3` | Klone pro Ruf in Phase 2 (0–10, höchstens 6 gleichzeitig) |
| `bosses.queenVoidZoneDamage` | `4.0` | Schaden pro Sekunde in einer Void-Zone |

## Ausrüstung

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `equipment.shardProjectileResistance` | `0.5` | Schadensreduktion des Shard-Sets gegen Splitter (0–1) |
| `equipment.colossusBonusHearts` | `4` | Bonusherzen des Koloss-Sets |
| `equipment.shieldDurationSeconds` | `6` | Dauer des Kristallschilds |
| `equipment.shieldAbsorptionHearts` | `8` | Absorptionsherzen (gerade Zahl empfohlen) |
| `equipment.shieldCooldownSeconds` | `60` | Abklingzeit des Kristallschilds |
| `equipment.shieldPushRadius` | `4.0` | Radius der Rückstoßwelle (0 = aus) |
| `equipment.gauntletCooldownSeconds` | `10` | Abklingzeit der Kolossfaust |
| `equipment.gauntletDamage` | `10.0` | Flächenschaden der Kolossfaust |
| `equipment.heartBlinkDistance` | `12` | maximale Sprungweite des Herzens |
| `equipment.heartCooldownSeconds` | `8` | Abklingzeit des Herzens |
| `equipment.shardbladeVolleyCooldownSeconds` | `3` | Abklingzeit der Splittersalve |

## Dimension

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `dimension.portalSlowFallingSeconds` | `10` | Sanfter Fall nach einer Portalreise (0 = aus) |

## Story

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `story.chapterTitles` | `true` | Kapitelkarte bzw. Titel beim Erreichen eines Kapitels |
| `story.traderDialogue` | `true` | Dialogfenster des Splitterhändlers |
| `story.dialogueLineDelayTicks` | `50` | Abstand der Chatzeilen (für Clients ohne Dialogfenster) |
| `story.lorePagePrice` | `2` | Shard-Splitter pro verkaufter Lore-Seite |

## Dialoge

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `dialogue.maxDistance` | `8.0` | maximaler Abstand zum NPC, sonst endet der Dialog |
| `dialogue.commandWhitelist` | `effect give @s `, `particle `, `playsound ` | erlaubte Befehlsanfänge für die Aktion `run_command_safe` |
| `dialogue.questCheckTicks` | `40` | wie oft `collect`-Schritte das Inventar prüfen |

## Nach dem Ausbruch

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `aftermath.campRadius` | `32` | Umkreis, in dem NPCs unverwundbar sind |
| `aftermath.npcSleep` | `true` | NPCs schlafen nachts an ihrem Platz |
| `aftermath.aftershockMinutes` | `8` | Abstand der Nachbeben ab Kapitel 7 (0 = aus) |
| `aftermath.maxNodesNearPlayer` | `3` | höchstens so viele Schwären im Umkreis von 96 Blöcken |
| `aftermath.echoQueenHealth` | `320.0` | Leben des Nachhalls |
| `aftermath.festeringHeartHealth` | `320.0` | Leben des Schwärenherzens |

## Shardvirus

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `virus.enabled` | `true` | Shardvirus aktiv; aus = kein Ausbruch beim Tod der Königin |
| `virus.spreadPaused` | `false` | Ausbreitung angehalten (wie `/shardline virus pause`) |
| `virus.spreadChance` | `0.75` | Chance pro Ausbreitungsversuch |
| `virus.spreadAttempts` | `2` | Versuche pro Random Tick |
| `virus.spreadRange` | `2` | Reichweite eines Versuchs in Blöcken |
| `virus.maxRange` | `0` | höchster Abstand zum nächsten Herd (0 = unbegrenzt) |
| `virus.maxBlocksPerTick` | `80` | Blöcke, die pro Tick aus der Warteschlange verarbeitet werden |
| `virus.queueLimit` | `8192` | Größe der Warteschlange pro Dimension |
| `virus.oreSeedChance` | `0.2` | Chance, dass freiliegendes Shard-Erz Nachbarn infiziert |
| `virus.portalSeedChance` / `portalSeedRadius` | `0.75` / `6` | Infektion rund um Splitterportale außerhalb der Weite |
| `virus.infectedMobSeedChance` | `0.065` | Chance, dass infizierte Mobs den Boden infizieren |
| `virus.mobConversionChance` | `0.02` | Kristallisierung pro Sekunde auf infiziertem Boden |
| `virus.villagersFlee` / `villagersTransform` / `animalsTransform` | `true` | Flucht und Verwandlung von Dorfbewohnern und Tieren |
| `virus.overworldSeedMinutes` | `4` | neue Herde in der Oberwelt (0 = aus) |
| `virus.netherEndSeedMinutes` | `10` | neue Herde im Nether und End (0 = aus) |
| `virus.maxDepth` | `6` | wie tief sich der Virus von einer Oberfläche ins Gestein frisst |
| `virus.depthChance` | `0.35` | Anteil „poröser“ Blöcke in der Tiefe |
| `virus.surfaceOnly` | `false` | altes Verhalten: nur Oberflächen |
| `virus.dimensions` | Oberwelt, Nether, End, Splitterweite | erlaubte Dimensionen |
| `virus.emberSpreadMultiplier` | `1.25` | Ausbreitungsfaktor Glutbrand (Nether) |
| `virus.voidSpreadMultiplier` | `0.75` | Ausbreitungsfaktor Leerenfäule (End) |
| `virus.strainEffects` | `true` | Brand (Glutbrand) und Dunkelheit (Leerenfäule) |
| `virus.netherEndMobsTransform` | `true` | Piglins und Endermen kristallisieren |
| `virus.feverChanceOnInfectedBlock` | `0.04` | Fieberchance pro Sekunde auf infiziertem Boden |
| `virus.feverDurationSeconds` | `30` | Dauer des Splitterfiebers |
| `virus.feverDamage` | `1.0` | Schaden alle 3 Sekunden |
| `virus.feverCanKill` | `false` | Fieber darf töten |
| `virus.feverCuredByMilk` | `false` | Milch heilt Fieber |

## Gegenmittel

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `remedies.anchorRadius` | `24` | Endradius der Reinheitszone |
| `remedies.anchorStartRadius` / `anchorGrowSeconds` | `6` / `300` | Startradius und Wachstumszeit der Zone |
| `remedies.anchorPurifyPerPulse` | `24` | zufällige Reinigungen alle 2 Sekunden |
| `remedies.essenceRadius` | `6` | Wirkradius der Reinigungsessenz |
| `remedies.immunityEssenceSeconds` / `immunityAnchorSeconds` | `120` / `15` | Immunität durch Essenz bzw. Anker |
| `remedies.heartPurifyRadius` | `40` | Reinigung beim Tod eines Virusherzens |
| `remedies.virusHeartHealth` | `200.0` | Leben des Virusherzens |
| `remedies.virusHeartDifficulty` | `NORMAL` | `EASY`, `NORMAL` oder `HARD` |
| `remedies.heartSpawnThreshold` | `160` | infizierte Blöcke pro Chunk, ab denen ein Herz entsteht |
| `remedies.heartSpawnCooldownMinutes` | `8` | Abstand zwischen zwei Herzen |
| `remedies.heartsForCore` | `3` | Herzen für den Kern des Bruchs |

## Ende

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `ending.mode` | `VOTE` | `VOTE` (Abstimmung aller Spieler mit Kapitel 6) oder `FIRST_PLAYER` |
| `ending.voteDurationSeconds` | `120` | Dauer der Abstimmung |
| `ending.titlePrefix` | `true` | [Hüter] / [Splitterherrscher] vor dem Namen |

## Fähigkeiten des Splitterherrschers

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `abilities.shardCallCooldownSeconds` | `90` | Abklingzeit Splitterruf (G) |
| `abilities.shardCallMaxMobs` | `3` | gerufene Diener |
| `abilities.shardCallStrengthSeconds` | `10` | Stärke nach dem Ruf |
| `abilities.crystalWaveCooldownSeconds` | `45` | Abklingzeit Kristallwelle (H) |
| `abilities.crystalWaveRadius` / `crystalWaveDamage` | `6.0` / `8.0` | Radius und Schaden der Welle |
| `abilities.crystalWaveFeverSeconds` | `4` | Fieber durch die Welle |
| `abilities.virusRun` | `true` | Tempo II und Regeneration I auf infiziertem Boden |

## Client

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `denseFog` | `true` | dichter Nebel in der Splitterweite |
| `fogDistance` | `96` | Sichtweite des Nebels in Blöcken (16–512) |
| `portalOverlay` | `true` | Kristallschleier beim Stehen im Portal |
| `infectionFog` | `true` | violetter Nebel in infizierten Gebieten |
| `infectionMusic` | `true` | eigene Musik in infizierten Gebieten |
| `infectedSky` | `true` | violetter Himmel nach dem Bruch |
| `screenShake` | `true` | Kamerawackeln (Bruch, Nachbeben) |
| `feverDistortion` | `true` | verzerrte Sicht bei Splitterfieber |
| `chapterCard` | `true` | Kapitelkarte statt einfachem Titel |
| `dialogue.typewriter` | `true` | Schreibmaschinentext im Dialogfenster |
| `dialogue.textSpeed` | `45` | Zeichen pro Sekunde (5–400) |
| `dialogue.typeSound` | `true` | Tippgeräusch |
| `dialogue.autoAdvance` / `autoAdvanceSeconds` | `false` / `4` | automatisch weiterblättern |

## Was nicht in der Config steht

Folgende Werte liegen im Datapack und lassen sich mit einem eigenen Datapack überschreiben:
- Spawngewichte und Gruppengrößen der Biome
- Erzmengen
- Strukturabstände
- Rezepte, Beute und Advancements
- Dialoge, Aufträge und welche Blöcke der Virus befällt

Dateien und Ordner dazu:
- `data/shardline/worldgen/…`
- `data/shardline/neoforge/biome_modifier/…` (Oberwelt)
- `data/shardline/recipe/…`
- `data/shardline/loot_table/…`
- `data/shardline/dialogues/<npc>.json` und `data/shardline/quests/<id>.json` (Dialoge und Aufträge, siehe [[Nach dem Ausbruch#Dialoge]])
- Block-Tags `shardline:virus_infectable/<art>` (was der Virus befällt)
