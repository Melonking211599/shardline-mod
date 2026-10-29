# Shardline – Texturen und Modelle

Alle Pfade sind relativ zu `src/main/resources/assets/shardline/`. Jede Datei existiert bereits als Platzhalter. Erzeugt werden die Platzhalter mit `java tools/GeneratePlaceholders.java`. Fertige Texturen kannst du einfach mit demselben Namen darüberkopieren. Der Generator überschreibt vorhandene Dateien nur mit `--force`.

**Aktuelle Texturen:** Alle Texturen entstehen deterministisch aus `python tools/assetgen/run_all.py` (Pillow nötig, Prüfung mit `python tools/assetgen/validate.py`). Die Farbpaletten stehen in `tools/assetgen/common.py`. Blöcke (`fresh_blocks.py`, `fresh_blocks2.py`), infizierte und Story-Blöcke (`fresh_virus.py`) und Item-Icons (`fresh_items.py`) sind von Hand als Pixelbilder entworfen, mit dem Werkzeugkasten in `pixel.py` (Cluster-Grundtöne wie bei Vanilla statt Pixelrauschen, Stempel, weiche Konturen). Mobs und Bosse nutzen die Materialien in `ent.py`. `python tools/assetgen/preview_geo.py <bild.png> <wesen> ...` rendert Mobs mit Textur als 3D-Modell aus mehreren Blickwinkeln. Ein erneuter Lauf liefert immer dieselben Bilder.

**Bossleisten:** Fünf Bosse haben eine eigene Leiste (Kristallkoloss, Splitterkönigin, Virusherz, Nachhall, Schwärenherz; `client/story/BossBars.java`, Event `CustomizeGuiOverlayEvent.BossEventProgress`). Je Boss liegen unter `textures/gui/boss_bar/` drei Dateien, die `tools/assetgen/bossbars.py` erzeugt: `<boss>_background.png` (182×10, leere Leiste), `<boss>_fill.png` (198 × 10 je Phase, Muster mit 16 px Periode, die UI verschiebt es für das Schimmern) und `<boss>_overlay.png` (214 × 32 je Phase, Rahmen; Fenster bei x 16–197, y 12–21 bleibt frei). Die Zeile einer Phase wählt die Vanilla-Farbe, die der Server setzt (Koloss Lila/Rot, Königin Rosa/Violett/Rot, Virusherz Lila/Rosa/Rot). Sonderteile: `crystal_colossus_shield.png` (Schild am 50-%-Marker, zerbricht), `shard_queen_cracks.png` (drei Risstufen bei 2/3, 1/3 und 10 % Leben), `virus_heart_veins.png` und `festering_heart_veins.png` (Adern, die im Takt der Fieberwelle aufleuchten; das Herz sendet dafür das Entity-Event 100), der Nachhall wird durchscheinend gezeichnet. Neue Designs meldet `BossBars.register(Übersetzungsschlüssel, Design)` an; Bossleisten anderer Wesen bleiben Vanilla. In `config/shardline-client.toml` schaltet `customBossBars = false` alle eigenen Leisten ab.

**Ruestungen am Koerper:** Beide Sets werden als GeckoLib-Modelle getragen (`client/renderer/ShardlineArmorRenderer`, Item `ShardlineArmorItem` ist ein `GeoItem`): `geo/armor/<set>.geo.json` mit den acht Bones `armorHead`, `armorBody`, `armorRightArm`, `armorLeftArm`, `armorRightLeg`, `armorLeftLeg`, `armorRightBoot`, `armorLeftBoot` (Bedrock-Standardpivots), Textur `textures/armor/<set>.png` mit Leuchtmaske `<set>_glowmask.png` und leerer `animations/armor/<set>.animation.json`. Sichtbar je Slot: Helm nur Kopf, Brustplatte Rumpf und Arme, Hose nur die Beine (Hueftplatten haengen deshalb an den Bein-Bones), Stiefel nur die Stiefel-Bones. Rüstungsbesaetze (Trims) werden an diesen Teilen nicht angezeigt, der Verzauberungsglanz schon. Die Vanilla-Ebenen `textures/models/armor/<set>_layer_1/2.png` bleiben als Rueckfall fuer Fremdmods. Erzeugt wird alles von `tools/assetgen/armor_models.py` (Bausatz `armorkit.py`: Wuerfel-DSL, UV-Packer, Flaechenmalerei); `python tools/assetgen/armor_models.py --preview <bild.png> --compare` rendert beide Sets auf einer Puppe, neben Netherite aus dem lokalen Minecraft-Jar.

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
| `shard_queen` | 128×128 | ja | fly, idle, cast, summon, teleport, scream, hurt, death, void_zone, projectile_barrage, clone_summon, phase_transition (vom Code ausgelöst: fly, cast, summon, teleport, scream, hurt, death) |
| `crystal_pillar` | 64×64 | ja | warmup, rise |

Sonderfälle:
- **Kristallgolem:** Wütend nutzt er die Zusatztextur `textures/entity/crystal_golem_angry.png` (128×128).
- **Königin:** Neues Modell (`tools/assetgen/queen_rig.py`, Animationen `queen_anim.py`): schlanke Glasgestalt ohne Flügel mit Krone aus sieben Zacken, offener Brustmulde mit Kern, Rock, Schleppe, Rückensplittern und drei schwebenden Splitterringen (Bones `ring_N`/`ring_N_spin`). Bone `neck` trägt die Kopfneigungen der Clips, `head` folgt dem Blickziel. `hurt` läuft auf einem eigenen Controller (`hurt`) und unterbricht keine Zauber, `death` hält die letzte Pose und der Körper bleibt 44 Ticks stehen.
- **Klon der Königin:** Er hat kein eigenes Modell, sondern nutzt Modell und Animationen der Königin mit der eigenen Textur `textures/entity/queen_clone.png` (128×128, mit Glowmask). Sie wird durchscheinend gerendert. Der Nachhall (`echo_queen`) ist dieselbe Textur umgefärbt.
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
- **Shard-Set (dunkler Violettstahl, Kristallinlays):** `shard_helmet`, `shard_chestplate`, `shard_leggings`, `shard_boots`, `shard_sword`, `shard_pickaxe`, `shard_axe`, `shard_shovel`
- **Koloss-Set (schwerer Steinstahl, Cyan nur am Kern):** `colossus_helmet`, `colossus_chestplate`, `colossus_leggings`, `colossus_boots`, `colossus_sword`, `colossus_pickaxe`, `colossus_axe`, `colossus_shovel`
- Rüstungs-, Werkzeug- und Waffen-Icons zeichnet `tools/assetgen/gear.py` als Pixelkarten (Netherite-Kontrast: dunkler Grundton, helle Kante oben links, 1 px Kontur). Vergleich mit Vanilla: `python tools/assetgen/compare_vanilla.py <bild.png>`.
- **Spezialwaffen:** `crystal_bow` mit den Spannstufen `crystal_bow_pulling_0`, `_1` und `_2`, dazu `colossus_gauntlet` und `queens_shardblade`
- **Spawn-Eier:** Sie brauchen keine Textur, ihre Farben stehen im Code.

## Rüstung am Körper

- **Getragenes Modell:** `geo/armor/{shard,colossus}.geo.json`, `textures/armor/{shard,colossus}.png` (+ `_glowmask.png`), siehe oben
- **Rückfall-Ebenen** (`textures/models/armor/`, 64×32): `shard_layer_1.png` (Helm, Brust, Stiefel) und `shard_layer_2.png` (Hose), `colossus_layer_1.png` und `colossus_layer_2.png`

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

Alle 81 Sound-Events stehen in `src/generated/resources/assets/shardline/sounds.json` und verweisen vorerst auf passende Vanilla-Sounds. Für eigene Sounds legst du `.ogg`-Dateien unter `assets/shardline/sounds/` ab und trägst sie in [ModSoundDefinitionsProvider](src/main/java/eu/shardline/mod/datagen/ModSoundDefinitionsProvider.java) ein. Den Musik-Slot der Splitterweite bildet `music.splitterweite`, infizierte Gebiete nutzen `music.infected`.

## Story und Shardvirus

Auch diese Dateien existieren schon als Platzhalter. Alles Weitere zur Story steht in [STORY.md](STORY.md).

### Wesen

| Name | Modell | Textur | Leuchtmaske | Animationen |
|---|---|---|---|---|
| `virus_heart` | GeckoLib: `geo/entity/virus_heart.geo.json`, `animations/entity/virus_heart.animation.json` | `textures/entity/virus_heart.png` (128×128) | ja | idle, walk, pulse, summon |
| `shard_zombie` | Vanilla-Zombiemodell | `textures/entity/shard_zombie.png` (64×64, Zombie-Layout) | ja, `shard_zombie_glowmask.png` | – |
| `crystallized_cow` | Vanilla-Kuhmodell | `textures/entity/crystallized_cow.png` (64×32, Kuh-Layout) | ja, `crystallized_cow_glowmask.png` | – |

Der Umhang der Hüter liegt unter `textures/entity/keeper_cape.png` (64×32, Vanilla-Umhang-Layout, Vorderseite bei 1,1 bis 11,17).

### Blöcke (`textures/block/`, 16×16)

Jeder infizierte Block besteht aus einer Grundtextur und einer **Leuchtschicht** `<name>_glow.png`. Die Leuchtschicht ist transparent bis auf die leuchtenden Adern und Funken. Sie wird mit voller Eigenhelligkeit über die Grundtextur gelegt.

| Dateien | Verwendung |
|---|---|
| `infected_stone.png`, `infected_stone_glow.png` | Infizierter Stein |
| `infected_dirt.png`, `infected_dirt_glow.png` | Infizierte Erde (auch Unterseite des Grasblocks) |
| `infected_grass_top.png`, `infected_grass_top_glow.png`, `infected_grass_side.png`, `infected_grass_side_glow.png` | Infizierter Grasblock |
| `infected_log.png`, `infected_log_glow.png`, `infected_log_top.png`, `infected_log_top_glow.png` | Infiziertes Holz Seite / Stirnseite |
| `infected_leaves.png`, `infected_leaves_glow.png` | Infiziertes Laub (cutout_mipped) |
| `infected_sand.png`, `infected_sand_glow.png` | Infizierter Sand |
| `memory_crystal.png` | Erinnerungskristall (Element-Modell, Sockel aus `shardstone_bricks.png`) |
| `purity_anchor.png`, `purity_anchor_crystal.png` | Reinheitsanker: Sockel und Säule / leuchtender Kristall |

### Items (`textures/item/`, 16×16)

`lore_book`, `purification_essence`, `breach_core`

### Weitere Texturen

| Datei | Größe | Verwendung |
|---|---|---|
| `textures/mob_effect/shard_fever.png` | 18×18 (16×16 geht auch) | Symbol des Splitterfiebers |
| `textures/particle/virus_spore_0..3.png` | 8×8 | Virussporen (4 Frames, werden violett eingefärbt) |
| `textures/misc/breach_overlay.png` | 256×256 | Risse beim Virusausbruch (transparent, Mitte frei) |
| `textures/gui/advancements/backgrounds/story.png` | 16×16 | Hintergrund des Tabs „Shardline: Chronik“ |

Die Vignette der Bildschirmeffekte nutzt die Vanilla-Textur `minecraft:textures/misc/vignette.png`.

### Neue Sounds (Platzhalter → Vanilla)

| Sound-Event | Platzhalter |
|---|---|
| `story.chapter` | `ui.toast.challenge_complete` |
| `block.memory_crystal.vision` | `block.beacon.activate` |
| `item.lore_book.open` | `item.book.page_turn` |
| `event.virus_outbreak` | `entity.wither.spawn` |
| `event.virus_sealed` | `block.beacon.power_select` |
| `event.virus_dominated` | `entity.warden.emerge` |
| `block.purity_anchor.activate` | `block.conduit.activate` |
| `item.purification_essence.use` | `block.amethyst_block.resonate` |
| `entity.shard_zombie.ambient` / `hurt` / `death` | Zombie |
| `entity.crystallized_cow.ambient` / `hurt` / `death` | Kuh |
| `entity.virus_heart.ambient` / `hurt` / `death` / `pulse` | Warden-Herzschlag, Amethyst, Seelenanker, Warden-Aufladung |
| `ability.shard_call` / `ability.crystal_wave` | Magier-Beschwörung / Warden-Schallwelle |
| `music.infected` | `music.nether.soul_sand_valley` |

## Splitterhandwerk

Alle Dateien erzeugt `tools/assetgen/fresh_update.py` (Blöcke, Items) und `tools/assetgen/snail.py` (Kristallschnecke); `run_all.py` ruft beide auf.

### Wesen

| Name | Modell | Textur | Leuchtmaske | Animationen |
|---|---|---|---|---|
| `crystal_snail` | GeckoLib: `geo/entity/crystal_snail.geo.json`, `animations/entity/crystal_snail.animation.json` | `textures/entity/crystal_snail.png` (64×64) | ja | idle, walk, hide (hält den letzten Frame) |

### Blöcke (`textures/block/`, 16×16)

| Dateien | Verwendung |
|---|---|
| `polished_shardstone.png` | Polierter Splitterstein, Treppe und Stufe |
| `chiseled_shardstone_bricks.png` | Gemeißelte Splitterstein-Ziegel |
| `crystal_door_top.png`, `crystal_door_bottom.png` | Kristallholztür (cutout) |
| `crystal_trapdoor.png` | Kristallholz-Falltür (cutout) |
| `crystal_glass.png`, `crystal_glass_pane_top.png` | Kristallglas und Scheibe (cutout) |
| `shard_lantern.png` | Splitterlaterne (Vanilla-Laternen-Layout) |

Treppe, Stufe, Zaun, Zauntor, Knopf und Druckplatte aus Kristallholz verwenden `crystal_planks.png`.

### Items (`textures/item/`, 16×16)

`crystal_door`, `shard_lantern`, `crystal_slime`, `glowcap_stew`, `crystal_candy`, `berry_tart`, `resonance_compass`

### Neue Sounds (Platzhalter → Vanilla)

| Sound-Event | Platzhalter |
|---|---|
| `entity.crystal_snail.ambient` | `block.amethyst_cluster.step` |
| `entity.crystal_snail.hurt` / `death` | kleiner Schleim |
| `entity.crystal_snail.hide` | `block.amethyst_block.place` |

## Story-Erweiterung 2

Alle Dateien erzeugen `tools/assetgen/infected_animals.py` (Wesen, Kragen) und `tools/assetgen/story2_assets.py` (Blöcke, Items, GUI, Effekt, Partikel); `run_all.py` ruft beide auf, `validate.py` prüft sie.
Vorschau: `python tools/assetgen/preview_vanilla.py <png>` zeigt die Vanilla-Modelle mit Textur, Vlies und Kragen. `python tools/assetgen/contact_sheet.py <png> <pfade…>` erstellt einen Kontaktbogen.

### Wesen (Vanilla-Modelle, Texturen im Vanilla-Layout)

| Name | Modell | Textur | Leuchtmaske |
|---|---|---|---|
| `crystallized_sheep` | Vanilla-Schaf | `entity/crystallized_sheep.png` (64×32) + Vlies `crystallized_sheep_fur.png` (64×32, Schaf-Fell-Layout) | ja, für beide |
| `crystallized_pig` | Vanilla-Schwein | `entity/crystallized_pig.png` (64×32) | ja |
| `crystallized_villager` | Vanilla-Dorfbewohner | `entity/crystallized_villager.png` (64×64; Hut-Ebene = Kristalle, Robe unten zerfetzt) | ja |

Kragen gezähmter Infizierter (`entity/<wesen>_collar.png`, durchsichtig bis auf das Halsband):

| Datei | Größe | liegt auf |
|---|---|---|
| `crystallized_cow_collar.png`, `crystallized_pig_collar.png` | 64×32 | Kopf (hinten) |
| `crystallized_sheep_collar.png` | 64×32 | Kopf des Vlies-Modells |
| `crystallized_villager_collar.png` | 64×64 | Robe (jacket), oben |
| `shard_zombie_collar.png` | 64×64 | Jacke, oben |

### Blöcke (`textures/block/`, 16×16)

| Dateien | Verwendung |
|---|---|
| `decision_altar_side.png`, `_top.png`, `_bottom.png` | Entscheidungsaltar (Sockel y 13–15, Säule y 6–12, Schale y 3–5 auf der Seitentextur) |
| `decision_altar_side_glow.png`, `decision_altar_top_glow.png` | Leuchtschicht im erwachten Zustand (links Gold = Siegel, rechts Magenta = Herrschaft) |
| `keeper_ruin_bricks.png` | Hüterruinen-Ziegel (Elfenbein, Goldfugen, Moos) |
| `keeper_ruin_pillar.png`, `keeper_ruin_pillar_top.png` | Hüterruinen-Säule (drehbar) |
| `keeper_rune_slab.png`, `keeper_rune_slab_side.png`, `keeper_rune_slab_glow.png` | Runenstufe: Oberseite, Seite (zwei Stufen übereinander), leuchtende Rune |

### Items (`textures/item/`, 16×16)

`lore_page`, `chapter_emblem_1` … `chapter_emblem_6` (Erster Splitter, Tor, Erinnerungskristall, Kern des Kolosses, Riss, Reinheitsanker; Modell-Overrides über die Eigenschaft `shardline:chapter`)

### Weitere Texturen

| Datei | Größe | Verwendung |
|---|---|---|
| `mob_effect/immunity.png` | 18×18 | Effekt Immunität (Schild in Elfenbein/Gold, himmelblauer Kristall) |
| `gui/trader_dialogue.png` | 256×128 | Dialograhmen 240×100 mit Porträt des Splitterhändlers (48×48 bei 10,10) und Namensleiste |
| `gui/chapter_card.png` | 256×128 | Kapitelkarte 256×72 mit Medaillonrahmen (Emblem 32×32 bei 18,20) |
| `particle/purity_mote_0..3.png` | 8×8 | goldene Funken der Reinheitszone (4 Frames, werden kleiner) |

Die goldene Linie der Reinheitszone zeichnet der Block-Entity-Renderer ohne Textur (`RenderType.lines`). Das Virusherz nutzt seine vorhandenen Clips `spawn`, `attack_tendrils`, `summon_infected`, `hurt` und `death`.

### Neue Sounds (Platzhalter → Vanilla)

| Sound-Event | Platzhalter |
|---|---|
| `entity.crystallized_sheep.ambient` / `hurt` / `death` | Schaf |
| `entity.crystallized_pig.ambient` / `hurt` / `death` | Schwein |
| `entity.crystallized_villager.ambient` / `hurt` / `death` | Zombie-Dorfbewohner |
| `entity.infected.cured` | `entity.zombie_villager.converted` |
| `block.decision_altar.rise` / `awaken` / `use` | `block.end_portal.spawn` / `block.beacon.activate` / `block.respawn_anchor.charge` |
| `entity.virus_heart.phase` / `tendrils` / `summon` | `entity.elder_guardian.curse` / `entity.evoker_fangs.attack` / `entity.evoker.prepare_summon` |
| `block.purity_anchor.grow` | `block.amethyst_block.resonate` |


## Nach dem Ausbruch (Kapitel 7–9)

Alle Texturen entstehen deterministisch mit Python/Pillow in `tools/assetgen/` und sind in `run_all.py` und `validate.py` eingehängt. Klangbeschreibungen, Paletten und die Testliste: [STORY_ASSETS.md](tools/assetgen/STORY_ASSETS.md).

| Generator | Ausgabe |
|---|---|
| `dialogue_gui.py` | `gui/dialogue/frame` (32×32, 9-Slice), `portrait_frame` (80×80), `nameplate` (48×16, 3-Slice), `button` (64×80, 4 Zustände), `icons` (32×16), `gui/journal/background` (256×192), `gui/journal/tabs` (128×56), `gui/portraits/unknown` (64×64) |
| `npc_assets.py` | GeckoLib-Modelle und -Animationen (`idle`, `walk`, `talk`, `hurt`, `sleep`, `death` hält den letzten Frame) für `rhea`, `veyl`, `ilsa`, `kesh`; Texturen 64×64 mit `_glowmask` (Veyl mit Alpha), `kesh_healed`; Porträts 64×64 je NPC in `neutral`, `happy`, `sad`, `angry` |
| `virus_strains.py` | `infected_deepslate`, `_planks`, `_netherrack`, `_blackstone`, `_soul_soil`, `_nylium_top`, `_nylium_side`, `_wart_block`, `_end_stone`, `_purpur` (je mit `_glow`, große Flächen mit `_v2`/`_v3`, Nether-Leuchten animiert); `crystallized_piglin` (64×64, Piglin-Layout) und `crystallized_enderman` (64×32) mit Glowmask und Kragen |
| `aftermath_assets.py` | Items `echo_shard`, `keeper_tuning_fork`, `chronicle_journal`; Block `festering_node` (+ animiertes `_glow`); Kapitelembleme `chapter_emblem_7`, `_8_seal`, `_8_dominate`, `_9_seal`, `_9_dominate`; `echo_queen` und `festering_heart` (umgefärbt aus Königin und Virusherz, mit Glowmask) |
| `preview_dialogue.py`, `preview_journal.py` | Layout-Vorschau des Dialogfensters bzw. Auftragsbuchs für GUI-Skalierung 1–4 (schreibt nichts in die Mod) |

Modelle der Wesen: NPCs eigene GeckoLib-Modelle; Nachhall und Schwärenherz nutzen die Modelle der Königin bzw. des Virusherzens mit eigener Textur; kristallisierte Piglins und Endermen nutzen die Vanilla-Modelle.

### Neue Sounds (Platzhalter → Vanilla)

`entity.rhea|veyl|ilsa|kesh.voice|hurt|death`, `entity.crystallized_piglin|crystallized_enderman.ambient|hurt|death`, `ui.dialogue.open|close|type|choice`, `ui.quest.update|complete`, `story.epilogue`, `event.aftershock`, `block.festering_node.hum|purify|bind|scream`, `item.keeper_tuning_fork.ring`.

### Eigene Klänge

21 gelieferte Soundeffekte sind eingebaut (`assets/shardline/sounds/`, importiert mit `tools/assetgen/import_sounds.py`, Zuordnung in `ModSounds.FILES`). Welche Events noch Platzhalter nutzen: [STORY_ASSETS.md → Stand der Klänge](tools/assetgen/STORY_ASSETS.md#stand-der-klänge).
