---
tags: [shardline, technik, code]
---

# Code-Struktur

Package `eu.shardline.mod`, Quellcode unter `src/main/java/eu/shardline/mod/`. Alles wird über `DeferredRegister` registriert. Die Einstiegsklasse `Shardline` meldet alle Register am Mod-Event-Bus an und registriert die beiden Configs.

## Pakete

| Paket | Inhalt |
|---|---|
| `(Wurzel)` | `Shardline`: Mod-Einstiegspunkt, `Shardline.id(path)` erzeugt `shardline:`-IDs |
| `config` | `ShardlineConfig` (common), `ShardlineClientConfig` (client), siehe [[Konfiguration]] |
| `core` | gemeinsame Register und Schlüssel: `ModSounds` (inkl. `FILES`: welche Events eigene Dateien haben), `ModParticles`, `ModAttachments`, `ModTags`, `ModDamageTypes`, `ModLootTables`, `ModDataComponents`, `ModEnchantments` |
| `block` | `ModBlocks` (Blöcke + Block-Items) |
| `block.custom` | Portal, Altäre (`BossAltarBlock` → Koloss/Königin, `DecisionAltarBlock`), Trophäe, Pflanzen, Setzling, Kristallnetz, Mottenlicht, `MemoryCrystalBlock`, `InfectedBlock`/`InfectedPillarBlock`, `PurityAnchorBlock`, `FesteringNodeBlock`, `ShardOreBlock` |
| `block.entity` | `ModBlockEntities`, `PurityAnchorBlockEntity` (wachsende Reinheitszone) |
| `item` | `ModItems`, `ModArmorMaterials`, `ModTiers`, `ModCreativeTabs` |
| `item.custom` | Shard-Schlüssel, Rüstung, Kristallbogen, Kolossfaust, Herz, Splitterklinge, Schmiedevorlage, `LoreBookItem`, `LorePageItem`, `PurificationEssenceItem`, `BreachCoreItem`, `ResonanceCompassItem`, `KeeperTuningForkItem`, `ChronicleJournalItem`, `ChapterEmblemItem` |
| `effect` | `ModEffects`, `ShardFeverEffect`, `ImmunityEffect` |
| `entity` | `ModEntities`, `ModEntityEvents` (Attribute, Spawnplatzierung), `ModSpawnRules`, `ShardlineAnimations` |
| `entity.mob` | die elf Wesen der Weite inkl. `CrystalSnailEntity`, siehe [[Mobs]] |
| `entity.infected` | `InfectedMob`, `InfectedMobs` (Verwandlung, Zähmen, Reinigen), Shard-Zombie und kristallisierte Kuh, Schaf, Schwein, Dorfbewohner, Piglin, Enderman |
| `entity.npc` | `AftermathNpcEntity` (gemeinsame Basis: Heimat, Schlaf, Flucht, Zusammenbruch), `RheaEntity`, `IlsaEntity`, `KeshEntity`, `VeylEntity` |
| `entity.boss` | `CrystalColossusEntity`, `ShardQueenEntity`, `QueenCloneEntity`, `VirusHeartEntity`, `EchoQueenEntity`, `FesteringHeartEntity`, siehe [[Bosse]] |
| `entity.misc` | `CrystalPillarEntity`, `VoidZoneEntity` |
| `entity.projectile` | `ShardProjectileEntity` (alle Splitter-Projektile) |
| `entity.ai` | `HoveringMoveControl` (Schwebeflug der Königin und Klone) |
| `event` | `SetBonusEvents`, `CrystalShieldHandler`, `ModGameEvents` (Golem-Aggro bei Bergbau) |
| `story` | `StoryChapter`, `StoryEnding`, `ShardlineWorldData` (SavedData), `StoryManager`, `StoryEvents`, `StoryScheduler` (Nachbeben, Aussaat), `StoryDataLoader` (Dialoge und Aufträge aus Datapacks), `Encounters` (`summon_encounter`) |
| `story.dialogue` | Dialog-Engine: `DialogueDefinition`, `DialogueParser`, `Condition`, `Action`, `DialogueContext`, `DialogueSessions`, `Dialogues`, `DialogueNpc` |
| `story.quest` | `QuestDefinition`, `QuestProgress`, `Quests`, `QuestSync` (Auftragsbuch) |
| `story.lore` | `LoreBooks`, `TraderDialogue`, `Visions` |
| `story.ending` | `EndingManager`, `RulerAbilities`, `RulerAllegiance` |
| `virus` | `Infected`, `InfectionMap`, `VirusSpreadManager` (Warteschlange pro Dimension), `VirusStrain` (Shardvirus, Glutbrand, Leerenfäule), `VirusDrift` (Tiefe), `Purification`, `PurityAnchors`, `VirusEffects`, `VirusOutbreak`, `VirusHeartSpawner`, `VirusEvents` |
| `command` | `ShardlineCommands` (`/shardline story|lore|virus|dialogue|quest …`) |
| `network` | `ModNetworking` (Protokoll 5) und alle Payloads, siehe [[Installation und Server#Netzwerk]] |
| `world` | `ModPoiTypes` (Portal-POI), `ModBiomeModifiers` (Oberwelt) |
| `world.dimension` | `ModDimensions`: Dimensionstyp, LevelStem, Chunk-Generator |
| `world.noise` | `ModNoiseSettings`: Rauschen, Dichtefunktion der Inseln, Oberflächenregeln |
| `world.biome` | `ModBiomes`: drei Biome, globale Feature-Reihenfolge |
| `world.feature` | `ModConfiguredFeatures`, `ModPlacedFeatures` |
| `world.structure` | `ModStructures` (Typen, Sets), `ShardlineStructures`, `ShardlinePiece` und je ein Bauteil pro Struktur (elf), `FixedChunkPlacement` |
| `world.portal` | `ShardPortalShape` (Rahmenerkennung), `ShardPortalForcer` (Zielportal suchen/bauen) |
| `client` | `ShardlineClient` (Renderer, Partikel, Tasten, Himmel, Overlay), `ClientGameEvents`, `KeyBindings` (V, G, H, J), `PortalOverlay` |
| `client.renderer` | GeckoLib-Renderer, `InfectedRenderers`, `NpcRenderers`, `KeeperCapeLayer`, `PurityAnchorRenderer`, `SlimArmorModels`, `ArmorExtrasLayer`/`ArmorExtraModels` (generiert) |
| `client.story` | `StoryClientState`, `ClientStoryHandler`, `ChapterCardOverlay`, `StoryScreenEffects`, `InfectionAtmosphere`, `BossBars`, Fenster: `EndingChoiceScreen`, `TraderDialogueScreen`, `LorePageScreen`, `DialogueScreen`, `QuestJournalScreen`; `ClientDialogue`, `ClientJournal` |
| `client.particle` | `ShardParticle` (Funkeln, Void, Sporen, Reinheit) |
| `client.sky` | `SplitterweiteEffects` (Himmel, Mond, Nebelfarbe, violetter Himmel) |
| `compat` | JEI- und EMI-Plugins mit gemeinsamen Infoseiten (`ShardlineCompatInfo`) |
| `datagen` | alle Datengeneratoren inkl. `ModStoryAdvancements`, siehe [[Build und Tests]] |
| `gametest` | `ShardlineGameTests` (45 Tests) |

## Wichtige Abläufe

### Portalreise

1. **Aktivierung:** `ShardKeyItem.useOn` → `ShardPortalShape.findEmptyPortalShape` → `createPortalBlocks`.
2. **Im Portal:** `ShardPortalBlock.entityInside` startet über die Vanilla-Portalmechanik von 1.21 (`Portal`-Interface) den Übergang.
3. **Ziel:** `getPortalDestination` skaliert die Koordinaten, sucht per POI ein bestehendes Portal (`ShardPortalForcer.findClosestPortalPosition`) oder baut eines (`createPortal`).
4. **Ankunft:** `arrivalEffects()` spielt Sound und Partikel und gibt Sanften Fall.

### Kristallschild

1. **Client:** `ClientGameEvents.onClientTick` liest die Taste und schickt `CrystalShieldPayload`.
2. **Server:** `ModNetworking` → `CrystalShieldHandler.tryActivate` prüft das volle Set und die Abklingzeit (Attachment `crystal_shield_ready_at`, bleibt nach dem Tod erhalten), gibt Absorption und stößt Gegner zurück.

### Boss-KI

- **Koloss:** `ColossusAttackGoal` wählt pro Tick zwischen Nahkampf, Stampfer, Säulen und Beschwörung. Fähigkeiten haben eine Ausholzeit, die zur Animation passt. Animationen werden per `triggerAnim` vom Server ausgelöst.
- **Königin:** Angriffe und Phasen laufen in `customServerAiStep`. Die Bewegung übernimmt `HoverGoal` mit `HoveringMoveControl`.
- **Beide:** Phasenwechsel, Arena-Bindung, Regeneration oder Zurücksetzen sowie die Bossleiste (`ServerBossEvent`) liegen im Entity selbst.

### Shardvirus

1. **Quellen** (Random Ticks von infizierten Blöcken, Shard-Erz, Portalen, infizierten Mobs, Aussaat-Timer) schlagen nur Positionen vor.
2. `VirusSpreadManager` sammelt sie in einer Warteschlange pro Dimension, nach Chunk gruppiert, und verarbeitet am Tickende höchstens `maxBlocksPerTick`. Gelesen wird nur über `getChunkNow`, es wird nie ein Chunk geladen.
3. `VirusStrain` wählt je Dimension Stamm, Zielblock und Wirkung; `VirusDrift` entscheidet, ob ein Block ohne Luftkontakt befallen wird (Tiefe, „poröse“ Blöcke).
4. Große Reinigungen laufen als Hintergrundauftrag (höchstens 12 Chunk-Abschnitte pro Tick).

### Dialoge und Aufträge

1. **Laden:** `StoryDataLoader` liest `data/shardline/dialogues` und `data/shardline/quests` bei `/reload`; `DialogueParser` prüft jede Datei, eine kaputte Datei schaltet nur sich selbst ab.
2. **Öffnen:** Rechtsklick auf ein NPC → `DialogueSessions` startet eine Sitzung → `OpenDialoguePayload` an den Client → `DialogueScreen`.
3. **Antworten:** `DialogueChoicePayload` → der Server prüft Sitzung, Abstand, NPC und Bedingungen erneut, führt die Aktionen aus und schickt den nächsten Knoten.
4. **Auftragsbuch:** `QuestSync` schickt `JournalPayload` beim Öffnen und bei jeder Änderung.

### Weltgenerierung

- **Gelände:** Die Inseln entstehen aus einer Dichtefunktion im `NoiseRouter`: 3D-Rauschen + 0,55 × 2D-Maske + Höhengradienten − 0,3.
- **Biome:** Die Wahl erfolgt über `MultiNoiseBiomeSource` nach Temperatur und Feuchtigkeit.
- **Strukturen:** Sie bauen in `postProcess` mit absoluten Koordinaten und einem gespeicherten Seed. Dadurch entsteht ein Bauwerk über Chunkgrenzen hinweg identisch.

> [!warning] Feature-Reihenfolge
> Neue Deko-Features müssen in `ModPlacedFeatures.VEGETATION_ORDER` eingetragen und in den Biomen über `ModBiomes.vegetation(...)` hinzugefügt werden. Andere Reihenfolgen in verschiedenen Biomen führen zum Absturz „Feature order cycle“. Der GameTest `terrainAndBiomes` prüft das.

## Ressourcen

| Ort | Inhalt |
|---|---|
| `src/main/resources/assets/shardline/` | handgeschrieben: Sprachdateien, GeckoLib-Modelle und -Animationen, Texturen, Partikeldefinitionen |
| `src/main/resources/data/shardline/structure/test/empty.nbt` | leeres Template für die GameTests |
| `src/generated/resources/` | **generiert**, nicht von Hand bearbeiten: Blockstates, Modelle, Loot, Rezepte, Tags, Advancements, Worldgen, Schadenstypen, `sounds.json` |
| `src/main/templates/META-INF/neoforge.mods.toml` | Mod-Metadaten mit Platzhaltern aus `gradle.properties` |
