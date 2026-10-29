---
tags: [shardline, technik, assets]
---

# Assets und Texturen

Alle Texturen sind **fertig gezeichnet**: Sie entstehen deterministisch aus Python-Skripten in `tools/assetgen/` (ein erneuter Lauf liefert immer dieselben Bilder). Die vollständige Liste mit Größen und Animationen steht im Projekt in `ASSETS.md`, die Story-Assets und der Stand der Klänge in `tools/assetgen/STORY_ASSETS.md`, die Stilregeln in `tools/assetgen/STYLEBIBEL.md`.

Basis aller Pfade: `src/main/resources/assets/shardline/`

## Texturen erzeugen

| Befehl | Zweck |
|---|---|
| `python tools/assetgen/run_all.py` | alle Texturen neu erzeugen (Pillow nötig) |
| `python tools/assetgen/validate.py` | Größen, fehlende Dateien, Modelle und Sounds prüfen |
| `python tools/assetgen/preview_geo.py <bild.png> <wesen> …` | Wesen mit Textur als 3D-Modell aus mehreren Blickwinkeln |
| `python tools/assetgen/armor_models.py --preview <bild.png> [--compare]` | beide Rüstungsmodelle auf einer Puppe aus vier Blickwinkeln, mit `--compare` neben Netherite (aus dem lokalen Minecraft-Jar) |
| `python tools/assetgen/compare_vanilla.py <bild.png> [--before <alte textures/>]` | Rüstungs- und Werkzeug-Icons neben Netherite |

**Aufbau:**
- **Werkzeugkasten:** `pixel.py` (Cluster-Grundtöne wie bei Vanilla statt Pixelrauschen, Stempel, weiche Konturen), Farbpaletten in `common.py`.
- **Blöcke:** `fresh_blocks.py`, `fresh_blocks2.py`; infizierte und Story-Blöcke `fresh_virus.py`, Virusstämme `virus_strains.py`.
- **Items:** `fresh_items.py`, `story_items.py`.
- **Wesen und Bosse:** Materialien in `ent.py`, dazu `entities.py`, `bosses.py`, `infected_entities.py`, `infected_animals.py`, `npc_assets.py`, `snail.py`, `aftermath_assets.py`.
- **Rüstung:** `armor_models.py` mit dem Bausatz `armorkit.py` erzeugt die getragenen GeckoLib-Modelle (`geo/armor`, `textures/armor`), `armor.py` die Vanilla-Rückfallebenen. `gear.py` zeichnet die Icons von Rüstung, Werkzeugen und Spezialwaffen als Pixelkarten.
- **Oberflächen:** `gui.py`, `effects_gui.py`, `dialogue_gui.py` (Dialogfenster, Porträts, Auftragsbuch), `bossbars.py` (eigene Bossleisten).

> [!note] Eigene Texturen
> Eine eigene Textur kann man weiterhin unter **demselben Pfad und Namen** ablegen (PNG, gleiche Größe). Ein späterer Lauf von `run_all.py` überschreibt sie aber – dann das zuständige Skript anpassen oder die Datei dort ausnehmen. `java tools/GeneratePlaceholders.java` erzeugt nur noch fehlende Platzhalter.

## GeckoLib-Modelle

19 Modelle. Jedes Wesen besteht aus drei Dateien mit gleichem Namen:

| Datei | Pfad |
|---|---|
| Modell | `geo/entity/<name>.geo.json` |
| Animationen | `animations/entity/<name>.animation.json` |
| Textur | `textures/entity/<name>.png` |
| Leuchtmaske (optional) | `textures/entity/<name>_glowmask.png` |

**Bearbeitung in Blockbench:**
1. Ein neues Projekt vom Typ „GeckoLib Animated Model“ anlegen.
2. Die `.geo.json` importieren. Die Texturgröße muss zu `texture_width` und `texture_height` passen.
3. Beim Export Modell und Animation unter den bestehenden Namen speichern.

**Regeln:**
- **Animationsnamen:** Die Pflicht-Animationen (siehe `ASSETS.md`) müssen genau so heißen, sonst bleibt das Wesen starr. NPCs brauchen `idle`, `walk`, `talk`, `hurt`, `sleep`, `death` (hält den letzten Frame).
- **Kopfdrehung:** Ein Bone mit dem Namen `head` dreht sich automatisch zum Blickziel.
- **Sattel:** Der Bone `saddle` des Void-Rochens wird nur angezeigt, wenn er gesattelt ist.
- **Leuchtmasken:** Deckende Pixel in der Maske leuchten im Dunkeln.
- **Gemeinsame Modelle:** Infizierte Wesen nutzen die Vanilla-Modelle mit eigener Textur (`InfectedRenderers`); der Nachhall nutzt das Modell der Königin, das Schwärenherz das des Virusherzens.

| Wesen | Texturgröße |
|---|---|
| Kristallkoloss | 256 × 256 |
| Kristallgolem, Void-Rochen, Splitterkönigin (+ Klon, Nachhall) | 128 × 128 |
| alle anderen | 64 × 64 |

## Block- und Itemtexturen

- **Größe:** Block- und Itemtexturen sind 16 × 16 Pixel.
- **Portal:** `textures/block/shard_portal.png` ist ein 16 × 128-Streifen mit 8 Frames, gesteuert über `shard_portal.png.mcmeta`.
- **Infizierte Blöcke:** Grundtextur plus Leuchtschicht (`*_overlay`), die mit voller Eigenhelligkeit gerendert wird.
- **Kristallbogen:** `crystal_bow` und `crystal_bow_pulling_0/1/2`.
- **Rüstung am Körper:** GeckoLib-Modell `geo/armor/<set>.geo.json` mit Textur `textures/armor/<set>.png` und `<set>_glowmask.png` (acht Bones `armorHead` … `armorLeftBoot`). Rüstungsbesätze (Trims) erscheinen an diesen Teilen nicht. Die Vanilla-Ebenen `textures/models/armor/<set>_layer_1.png` / `_layer_2.png` (64 × 32) sind nur der Rückfall.

## Weitere Grafik

| Datei | Zweck |
|---|---|
| `textures/environment/splitterweite_sky.png`, `splitterweite_moon.png` | Himmel und zerbrochener Mond |
| `textures/misc/shard_portal_overlay.png` | Bildschirm-Overlay im Portal |
| `textures/particle/…` | Funkeln, Void, Virus-Sporen, Reinheit (8 × 8) |
| `textures/gui/boss_bar/<boss>_{background,fill,overlay}.png` | eigene Bossleisten (Leiste 182 × 10; Füllung und Rahmen mit einer Zeile je Phase), dazu `crystal_colossus_shield`, `shard_queen_cracks`, `virus_heart_veins`, `festering_heart_veins` |
| `textures/gui/…` | Dialogfenster, Porträts, Auftragsbuch, Lore-Seite, Entscheidung, Kapitelkarte |
| `textures/mob_effect/…` | Splitterfieber, Immunität |
| `textures/gui/advancements/backgrounds/shardline.png` | Hintergrund der Advancement-Tabs |
| `shardline_logo.png` (Ressourcen-Wurzel) | Logo in der Mod-Liste |

## Sounds

- **Events:** 135 Sound-Events, alle mit deutschen und englischen Untertiteln.
- **Eigene Klänge (24 Events, 23 Dateien)** liegen als Ogg Vorbis (48 kHz) unter `sounds/`: Koloss (Brüllen, Stampfen), Königin (Magie, Schrei, Teleport), Shard-Zombie, kristallisierte Kuh (Tod), Virusherz (Puls), Portal und Shard-Schlüssel, Erinnerungskristall, Reinheitsanker, Shard-Erz abbauen, Lore-Buch, Reinigungsessenz, Shard-Splitter aufheben, Splitterruf, Kristallwelle, Kapitel, Ausbruch, Versiegelt, Beherrscht und zwei Musikstücke: **Splitterweite** (`music/splitterweite.ogg`) und **infizierte Gebiete** (`music/infected.ogg`), je 2 min Stereo, gestreamt.
- **Platzhalter:** Die übrigen **111 Events** zeigen in `sounds.json` noch auf passende Vanilla-Klänge. Die Liste steht in [[ToDos#Fehlende Sounds]].

**Neue Sounds einbinden:**
1. WAV-Dateien (16 bit) liefern, Dateiname wie „Kristallkoloss — Brüllen.wav“.
2. Im Skript `tools/assetgen/import_sounds.py` die Datei in `SOUNDS` eintragen (Zielpfad, Mono/Stereo).
3. `python tools/assetgen/import_sounds.py <ordner>` ausführen: kürzt Stille, normalisiert auf −1 dBFS, Mono für Klänge mit Position, Stereo nur für globale Ereignisse, speichert als Ogg.
4. Das Event in `ModSounds.FILES` eintragen und `gradlew runData` ausführen.
5. `validate.py` und der GameTest `soundFiles` prüfen, dass jede eingetragene Datei existiert.

Musik wird automatisch gestreamt (`stream: true` für alle `music.*`-Events mit eigener Datei).

## Sprachdateien

`lang/de_de.json` und `lang/en_us.json` werden **von Hand gepflegt** (je rund 1460 Einträge). Sie enthalten alle Texte: Namen, Tooltips, Meldungen, Advancements, Untertitel, JEI-Infoseiten, Todesmeldungen, Config-Einträge, Lore-Bücher, Visionen, Händler- und NPC-Dialoge, Aufträge. Der GameTest `storyTexts` bzw. `storyDataFiles` prüft, dass alle Schlüssel vorhanden sind. Für eine neue Sprache die englische Datei kopieren und übersetzen, z. B. als `fr_fr.json`.
