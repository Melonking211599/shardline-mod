---
tags: [shardline, technik, build, tests]
---

# Build und Tests

Projektordner: `C:\Users\colli\Desktop\wiwiyi` (Git-Zweig `main-x285x0`, siehe [[Git Commands]])

## Werkzeuge

| | |
|---|---|
| Build-System | Gradle 9.2.1 (Wrapper `gradlew` / `gradlew.bat`) mit ModDevGradle 2.0.148 |
| JDK | Java 21; ein portables Temurin-JDK liegt in `.tools/jdk-21` |
| Mappings | Parchment 2024.11.17 (lesbare Parameternamen) |

Für die Befehle muss `JAVA_HOME` auf ein JDK 21 zeigen, zum Beispiel auf `.tools/jdk-21`.

## Befehle

| Befehl | Zweck |
|---|---|
| `gradlew build` | baut die Mod nach `build/libs/shardline-1.0.0.jar` |
| `gradlew runData` | führt alle Datengeneratoren aus → `src/generated/resources/` |
| `gradlew runClient` | startet einen Entwicklungs-Client (mit JEI) |
| `gradlew runServer` | startet einen Entwicklungs-Server in `run/` |
| `gradlew runGameTestServer` | führt die GameTests aus und beendet sich danach |

**Version:** Sie steht in `gradle.properties` (`mod_version`). Dort stehen auch die Versionen von NeoForge, GeckoLib, JEI und EMI.

## Datengeneratoren

Nach Änderungen an Blöcken, Items, Rezepten, Beute, Tags, Advancements, Sounds oder Worldgen führst du `gradlew runData` aus. Die Generatoren im Paket `datagen`:

| Klasse | Erzeugt |
|---|---|
| `ModWorldGenProvider` | Dimension, Dimensionstyp, Rauschen, Noise-Settings, Biome, Features, Strukturen, Struktursets, Schadenstypen, Biome-Modifier |
| `ModBlockStateProvider` / `ModItemModelProvider` | Blockstates, Block- und Itemmodelle |
| `ModBlockTagsProvider` / `ModItemTagsProvider` / `ModWorldTagsProvider` | Tags für Blöcke, Items, Biome, Strukturen, Schadenstypen |
| `ModBlockLootProvider` / `ModEntityLootProvider` / `ModChestLootProvider` | Beutetabellen |
| `ModRecipeProvider` | alle [[Rezepte]] |
| `ModAdvancements` / `ModStoryAdvancements` | [[Advancements]]: Tab „Shardline“ und „Shardline: Chronik“ |
| `ModSoundDefinitionsProvider` | `sounds.json` (eigene Dateien laut `ModSounds.FILES`, sonst Vanilla-Platzhalter) |
| `ModDataMapProvider` | Brennstoffe, Kompostierbarkeit |

Die Generatoren prüfen beim Lauf, ob alle referenzierten Texturen existieren. Eine fehlende Textur lässt `runData` abbrechen.

## Automatische Tests (GameTests)

`ShardlineGameTests` enthält **45 Tests**. Letzter Stand: alle bestanden.

| Bereich | Tests |
|---|---|
| Welt und Strukturen | `terrainAndBiomes`, `structurePieces` (alle Bauwerke inkl. Erinnerungskristalle und Hüter-Ruinen), `portal`, `splitterweiteWorld`, `chunkGenerationStress`, `aftermathStructures` (Lager, Bibliothek) |
| Wesen und Inhalte | `mobsTick` (alle Wesen 10 s ohne Absturz), `crystalSnailHides`, `shardEnchantments`, `craftingContentComplete` |
| Shardvirus | `virusSpreadAnchorPurify`, `virusQueueProcessing`, `virusRandomTickSpread`, `infectedAnimals`, `immunity`, `purityZone`, `virusHeartPhases`, `virusStrainBlocks`, `virusDepth`, `virusStrainMobs` |
| Story und Ende | `storyDataRoundTrip`, `storyTexts` (alle Sprachschlüssel vorhanden), `loreBookItems`, `lorePageTrading`, `chapterDisplay`, `decisionAltar`, `endingSealStopsVirus`, `rulerAllegiance` |
| Nach dem Ausbruch | `dialogueEngine`, `dialogueValidation`, `questFlow`, `aftermathChapters`, `aftermathSaveData`, `dialogueSessions`, `dialoguePayloadCodec`, `storyDataFiles`, `rheaNpc`, `veylAndIlsa`, `keshEscort`, `markedHealing` (Kesh heilen über die echten Dialoge), `aftermathFlowSeal` (Kapitel 7 → 8 über die echten Dialoge), `aftermathBossesAndEpilogue`, `festeringNodeDominated`, `journalSync` |
| Assets | `soundFiles` (jede eingetragene Sounddatei existiert) |

> [!info] GameTest-Server und Dimensionen
> `runGameTestServer` erzeugt eine flache Testwelt und **lädt keine Mod-Dimensionen**. `splitterweiteWorld` wird dort übersprungen, `portal` prüft nur den Rahmen. Für die volle Prüfung dient der Server-Rauchtest.

### Server-Rauchtest mit echter Welt

1. `tools/devtest_datapack` nach `run/world/datapacks/shardline_devtest` kopieren.
2. `gradlew runServer` starten. Das Datapack führt beim Laden `test runall` aus und stoppt den Server nach 60 Sekunden.
3. Im Log nach `[GameTest]` suchen. Fehlschläge erscheinen als `ERROR … failed`.
4. **Danach `run/world` löschen**, sonst stoppt jeder weitere Serverstart nach 60 Sekunden.

Die EULA in `run/eula.txt` ist für die lokale Testumgebung akzeptiert. `run/server.properties` steht auf Offline-Modus und erlaubt Funktionen das `stop`-Kommando (`function-permission-level=4`).

**Letzter Lauf (Seed `shardline`):**
- **Gelände:** Inselabdeckung 48 %, Oberkante Y 58–168.
- **Bauwerke:** Thron bei (8, 115, 8), Ruinen-Turm bei (168, 142, 136), Koloss-Altar bei (−488, 97, 856).
- **Portal und Spawns:** Zielportal erzeugt, alle Spawnregeln erfüllbar.

## Asset-Werkzeuge (Python)

Unter `tools/assetgen/` (Python 3 mit Pillow und NumPy):

| Skript | Zweck |
|---|---|
| `run_all.py` | erzeugt **alle** Texturen deterministisch neu |
| `validate.py` | prüft Texturen, Modelle und Sounddateien (Größen, fehlende Dateien) |
| `import_sounds.py <ordner>` | übernimmt gelieferte WAV-Sounds als Ogg (siehe [[Assets und Texturen#Sounds]]) |
| `preview_geo.py`, `preview_armor.py`, `preview_journal.py`, `contact_sheet.py` | Vorschaubilder und Kontaktbögen |

## Was die Tests nicht abdecken

Ohne Spieler im Spiel bleiben ungetestet:
- Darstellung von Modellen, Animationen, Himmel, Nebel und Overlay
- natürliches Spawnen
- Boss-Kämpfe gegen echte Spieler
- Reiten und Zähmen
- Aussehen der Fenster (Dialog, Auftragsbuch, Entscheidung) und der Kapitelkarte

Dafür gibt es `gradlew runClient`.
