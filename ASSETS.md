# Shardline – Texturen und Modelle

Alle Pfade sind relativ zu `src/main/resources/assets/shardline/`. Jede Datei existiert bereits als Platzhalter. Erzeugt werden die Platzhalter mit `java tools/GeneratePlaceholders.java`. Fertige Texturen kannst du einfach mit demselben Namen darüberkopieren. Der Generator überschreibt vorhandene Dateien nur mit `--force`.

## Entity-Modelle (GeckoLib)

Jedes Wesen hat drei Dateien: ein Modell unter `geo/entity/<name>.geo.json`, die Animationen unter `animations/entity/<name>.animation.json` und die Textur unter `textures/entity/<name>.png`. Steht bei Leuchtmaske ein Ja, gibt es zusätzlich `textures/entity/<name>_glowmask.png`: Was dort deckend ist, leuchtet im Dunkeln.

| Name | Texturgröße | Leuchtmaske | Pflicht-Animationen |
|---|---|---|---|
| `shard_creeper` | 64×64 | ja | idle, walk, swell |
| `crystal_golem` | 128×128 | ja | idle, walk, attack |
| `glow_moth` | 64×64 | ja | fly, sit |
| `ash_walker` | 64×64 | nein | idle, walk, attack |
| `shard_scout` | 64×64 | ja | fly, shoot |
| `void_ray` | 128×128 | ja | fly (Bone `saddle` wird nur gesattelt angezeigt) |
| `crystal_spider` | 64×64 | ja | idle, walk, climb, attack |
| `shard_trader` | 64×64 | nein | idle, walk, trade |
| `shadow_fox` | 64×64 | ja | idle, walk, attack |
| `shattered_knight` | 64×64 | ja | idle, walk, attack, charge |
| `crystal_colossus` | 256×256 | ja | idle, walk, attack, slam, pillar, summon, roar |
| `shard_queen` | 128×128 | ja | fly, cast, summon, teleport, scream |
| `crystal_pillar` | 64×64 | ja | warmup, rise |

Sonderfälle:
- **Kristallgolem:** Wütend nutzt er die Zusatztextur `textures/entity/crystal_golem_angry.png` (128×128).
- **Klon der Königin:** Er hat kein eigenes Modell, sondern nutzt Modell und Animationen der Königin mit der eigenen Textur `textures/entity/queen_clone.png` (128×128). Sie wird durchscheinend gerendert.
- **Kopfdrehung:** Ein Bone mit dem Namen `head` dreht sich automatisch zum Blickziel.
- **Splitter-Projektil:** Es wird als Item (`textures/item/shard_fragment.png`) dargestellt. Die Void-Zone besteht nur aus Partikeln.

## Blöcke (`textures/block/`, 16×16)

| Datei | Verwendung |
|---|---|
| `shardstone.png` | Splitterstein (auch Unterseite von Kristallmoos) |
| `shardstone_bricks.png` | Ziegel, Treppe, Stufe, Mauer, Trophäensockel |
| `cracked_shardstone_bricks.png` | Rissige Ziegel |
| `shard_ore.png`, `deepslate_shard_ore.png` | Shard-Erz |
| `shard_block.png` | Shard-Block |
| `glow_crystal_block.png` | Leuchtkristall (auch Partikel des Mottenlichts) |
| `glow_crystal_cluster.png` | Leuchtkristall-Cluster (Kreuzmodell) |
| `crystal_moss.png`, `crystal_moss_side.png` | Kristallmoos oben / Seite |
| `ash_block.png` | Ascheboden |
| `crystal_log.png`, `crystal_log_top.png` | Kristallholzstamm Seite / Stirnseite |
| `crystal_planks.png` | Kristallholzbretter |
| `crystal_leaves.png` | Kristallblätter (cutout_mipped) |
| `crystal_sapling.png`, `glowbloom.png`, `shard_fern.png`, `glowcap.png`, `luminous_berry_bush.png` | Pflanzen (Kreuzmodell, auch als Item-Icon) |
| `crystal_web.png` | Kristallnetz |
| `shard_portal_frame.png` | Splitterrahmen |
| `shard_portal.png` + `shard_portal.png.mcmeta` | Portal, 16×128 mit 8 animierten Frames |
| `colossus_altar.png`, `colossus_altar_top.png` | Koloss-Altar Seiten / Oberseite und Kristall |
| `queen_altar.png`, `queen_altar_top.png` | Thron der Splitterweite |
| `colossus_trophy.png`, `queen_trophy.png` | Kristall der Trophäen |

## Items (`textures/item/`, 16×16)

- **Rohstoffe:** `shard_fragment`, `raw_shard`, `shard_ingot`, `ash_dust`, `scout_eye`, `void_ray_hide`, `shadow_pelt`, `knight_crest`, `luminous_berry`
- **Fortschritt:** `shard_key`, `colossus_summoning_crystal`, `colossus_core`, `colossus_upgrade_smithing_template`, `heart_of_the_expanse`
- **Shard-Set:** `shard_helmet`, `shard_chestplate`, `shard_leggings`, `shard_boots`, `shard_sword`, `shard_pickaxe`, `shard_axe`, `shard_shovel`
- **Koloss-Set:** `colossus_helmet`, `colossus_chestplate`, `colossus_leggings`, `colossus_boots`, `colossus_sword`, `colossus_pickaxe`, `colossus_axe`, `colossus_shovel`
- **Spezialwaffen:** `crystal_bow` mit den Spannstufen `crystal_bow_pulling_0`, `_1` und `_2`, dazu `colossus_gauntlet` und `queens_shardblade`
- **Spawn-Eier:** Sie brauchen keine Textur, ihre Farben stehen im Code.

## Rüstung am Körper (`textures/models/armor/`, 64×32)

- `shard_layer_1.png` (Helm, Brust, Stiefel) und `shard_layer_2.png` (Hose)
- `colossus_layer_1.png` und `colossus_layer_2.png`

## Weitere Texturen

| Datei | Größe | Verwendung |
|---|---|---|
| `textures/particle/shard_sparkle_0..3.png` | 8×8 | Kristallfunkeln (4 Frames) |
| `textures/particle/void_mote_0..3.png` | 8×8 | Void-Partikel (wird im Code dunkel eingefärbt) |
| `textures/environment/splitterweite_sky.png` | 128×128 | Himmelswürfel der Splitterweite (gekachelt) |
| `textures/environment/splitterweite_moon.png` | 64×64 | Zerbrochener Kristallmond |
| `textures/misc/shard_portal_overlay.png` | 256×256 | Bildschirm-Overlay im Portal |
| `textures/gui/advancements/backgrounds/shardline.png` | 16×16 | Hintergrund des Advancement-Tabs |
| `../../shardline_logo.png` (Ressourcen-Wurzel) | 256×128 | Logo in der Mod-Liste |

## Block- und Item-Modelle

Blockstates sowie Block- und Item-Modelle entstehen per Datengenerator in `src/generated/resources/assets/shardline/`. Altäre und Trophäen sind dabei Element-Modelle aus mehreren Quadern. Nach Änderungen an [ModBlockStateProvider](src/main/java/eu/shardline/mod/datagen/ModBlockStateProvider.java) oder [ModItemModelProvider](src/main/java/eu/shardline/mod/datagen/ModItemModelProvider.java) führst du `gradlew runData` aus.

## Sounds

Alle 60 Sound-Events stehen in `src/generated/resources/assets/shardline/sounds.json` und verweisen vorerst auf passende Vanilla-Sounds. Für eigene Sounds legst du `.ogg`-Dateien unter `assets/shardline/sounds/` ab und trägst sie in [ModSoundDefinitionsProvider](src/main/java/eu/shardline/mod/datagen/ModSoundDefinitionsProvider.java) ein. Den Musik-Slot der Splitterweite bildet `music.splitterweite`.
