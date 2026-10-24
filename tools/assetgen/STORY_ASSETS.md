# Shardline – Story-Erweiterung: Assets, Sounds, Atmosphäre

Alles wird von `python tools/assetgen/run_all.py` erzeugt (deterministisch, idempotent). Prüfung: `python tools/assetgen/validate.py`.
Vorschau: `tools/assetgen/preview_story_before_after.png` (Basisblock → infiziert → infiziert + Leuchtphase).

## Dateien (unter `src/main/resources/assets/shardline/`)

| Bereich | Datei | Größe |
|---|---|---|
| Blöcke | `textures/block/infected_{stone,dirt,grass_top,grass_side,log,log_top,leaves,sand}.png` | 16×16 |
| Leuchtschichten | `textures/block/infected_*_glow.png` (grass_top, grass_side, leaves animiert: 16×64 + `.mcmeta`, frametime 10) | 16×16 / 16×64 |
| Story-Blöcke | `block/memory_crystal.png`, `block/purity_anchor.png`, `block/purity_anchor_crystal.png` (+`.mcmeta`, 8 Frames, frametime 3) | 16×16 / 16×128 |
| Items | `item/lore_book.png`, `item/purification_essence.png`, `item/breach_core.png` | 16×16 |
| Lore-Cover (Zusatz, nicht verdrahtet) | `item/lore_book_<id>.png` ×12 | 16×16 |
| Cape | `entity/keeper_cape.png` (Cape + Elytra-Flügel) | 64×32 |
| Virusherz | `entity/virus_heart.png` + `_glowmask.png`, `geo/entity/virus_heart.geo.json`, `animations/entity/virus_heart.animation.json` | 128×128 |
| Shard-Zombie | `entity/shard_zombie.png` + `_glowmask.png` (Vanilla-Zombie-UV) | 64×64 |
| Kristall-Kuh | `entity/crystallized_cow.png` + `_glowmask.png` (Vanilla-Kuh-UV) | 64×32 |
| Effekt | `mob_effect/shard_fever.png` | 18×18 |
| Partikel | `particle/virus_spore_0..3.png` (weißlich, wird im Code getintet) | 8×8 |
| Overlay | `misc/breach_overlay.png` | 256×256 |
| Advancement | `gui/advancements/backgrounds/story.png` | 16×16 |

Virusherz-Clips: `idle` (+Tentakelschwanken), `walk`, `pulse`, `summon` (alt) sowie neu `spawn`, `attack_tendrils`, `summon_infected`, `hurt`, `death` (`hold_on_last_frame`).
Neue Bones: `detail_growths` (Kind von `core`), `detail_tendril_0..5` (Kinder von `roots`). Bestehende Bones/Pivots unverändert.

## Paletten

Virus: `#0d0412 #240a2c #481044 #7c1a68 #b8268f` · Leuchten: `#a3208a #e03cc0 #ff6fdc #ffb3f0 #fff0fb` · Toxic `#ff3fb4`
Reinheit: Elfenbein `#6f6a82 #9f9ab4 #cdc9dd #ecebf6 #ffffff` · Gold `#4a2c08 #8a5a14 #cf9425 #f4c95a #fff1b0` · Himmelblau `#4879ad #79b0e0 #acd6f5 #d8eeff #f4fbff`

## Atmosphäre (Hex, für Nebel/Himmel/Wasser in Code oder Biome-JSON)

| Zone | fog_color | sky_color | water_color | water_fog_color | Hinweis |
|---|---|---|---|---|---|
| Infiziert (Ausbruch) | `#2a0a2c` | `#3a1038` | `#5a1a58` | `#1c0620` | Nebel dicht, Fernsicht ~40 % |
| Infiziert, Puls-Spitze (Event) | `#5a1450` | `#7c1a68` | – | – | kurz mit dem Virusherz-Puls aufhellen |
| Virus dominiert (Endzustand) | `#1a0620` | `#240a2c` | `#3a1038` | `#0d0412` | fast schwarz, nur Adern leuchten |
| Reinheitszone (Anker aktiv) | `#cfe6f7` | `#8fc4ee` | `#79b0e0` | `#d8eeff` | Nebel weich, hell; goldene Partikel `#f4c95a` |
| Reinheit, Übergang | `#9a8fb8` | `#6a5a94` | – | – | Mischzone zwischen Virus und Reinheit |
| Splitterweite (Basis, zum Vergleich) | `#170a35` | `#24104d` | – | – | unverändert |

Partikelfarben: Spore `#ff3fb4` (Tint), Reinheit `#f4c95a`, Funken `#ffb3f0`.

## Sound-Beschreibungen (`.ogg`, mono, 44,1 kHz; Pfad = Eventname aus `sounds.json`)

Neu für die Story (die übrigen Events wie bisher):

| Event | Beschreibung | Länge |
|---|---|---|
| `entity.virus_heart.ambient` | Tiefer, nasser Herzschlag (Lub-Dub), darunter leises Knistern | 1,2 s |
| `entity.virus_heart.pulse` | Starker Schlag mit Sub-Bass, kurzer hoher Kristall-Zischer als Druckwelle | 0,8 s |
| `entity.virus_heart.hurt` | Feuchtes Platzen + schrilles Glas-Kreischen, kurz | 0,4 s |
| `entity.virus_heart.death` | Anschwellendes Pochen → lautes Platzen → Kristallregen und tiefes Ausatmen | 3 s |
| `entity.shard_zombie.ambient` | Heiseres Stöhnen mit Glasklirren im Hintergrund | 1,5 s |
| `entity.shard_zombie.hurt` | Knurren + Splittern | 0,4 s |
| `entity.shard_zombie.death` | Langes Röcheln, am Ende klirrender Zerfall | 1,2 s |
| `entity.crystallized_cow.ambient` | Gedämpftes Muhen, verzerrt, mit Kristallklingeln | 1,5 s |
| `entity.crystallized_cow.hurt` | Kurzes verzerrtes Muhen + Klirren | 0,4 s |
| `entity.crystallized_cow.death` | Muhen bricht ab, Glas zersplittert | 1 s |
| `block.memory_crystal.vision` | Heller, hallender Sinuston-Akkord mit Rückwärts-Hall, wie ein Atemzug | 2,5 s |
| `block.purity_anchor.activate` | Warmer Glockenakkord, aufsteigend, sanfter Chor-Pad darunter | 3 s |
| `event.virus_outbreak` | Tiefes Horn + Herzschläge, dissonantes Kristallkreischen | 5 s |
| `event.virus_sealed` | Schwerer Gong, auflösende Obertöne, danach Stille | 4 s |
| `event.virus_dominated` | Dunkler Drone, verzerrtes Glockenspiel, Herzschlag verstummt | 5 s |
| `item.lore_book.open` | Papier-/Ledergeräusch, leises magisches Schimmern | 0,7 s |
| `item.purification_essence.use` | Gluckern, dann klarer Glockenton + Luftzug | 1,2 s |
| `story.chapter` | Kurzer, feierlicher Dreiklang (Harfe + Streicher-Pad) | 3 s |
| `music.infected` | Ambient-Loop: Drone in Moll, langsame Herzschläge, vereinzelte Glasklänge, 90–120 s | Loop |

## Story-Erweiterung 2: Sound-Beschreibungen (`.ogg`, mono, 44,1 kHz)

| Event | Beschreibung | Länge |
|---|---|---|
| `entity.crystallized_sheep.ambient` | Blöken, gedämpft, mit feinem Glasklingeln im Ausklang | 1,2 s |
| `entity.crystallized_sheep.hurt` | Kurzes Blöken + Splittern | 0,4 s |
| `entity.crystallized_sheep.death` | Blöken bricht ab, Kristallregen | 1 s |
| `entity.crystallized_pig.ambient` | Grunzen mit hohlem Kristallresonanzton | 1 s |
| `entity.crystallized_pig.hurt` | Quieken + Klirren | 0,4 s |
| `entity.crystallized_pig.death` | Quieken, dann zerspringendes Glas | 1 s |
| `entity.crystallized_villager.ambient` | Murmeln („hmm“), tief, mit leisem falschem Summton darunter | 1,5 s |
| `entity.crystallized_villager.hurt` | Erschrockenes „hrm“ + Splittern | 0,4 s |
| `entity.crystallized_villager.death` | Seufzer, der in einen Kristallton übergeht | 1,5 s |
| `entity.infected.cured` | Kristall bröckelt ab, klarer Glockenton, Ausatmen | 2 s |
| `block.decision_altar.rise` | Tiefes Grollen, Stein reibt, Akkord aus Gold- und Magentaklang | 4 s |
| `block.decision_altar.awaken` | Zwei Töne gleichzeitig: warme Glocke und dissonantes Glas, schwellen an | 2 s |
| `block.decision_altar.use` | Kern rastet ein: dumpfer Schlag, dann Hall | 1,5 s |
| `entity.virus_heart.phase` | Herz zieht sich zusammen (Knirschen), dann schwerer Schlag mit Sub-Bass | 1,6 s |
| `entity.virus_heart.tendrils` | Nasses Reißen aus dem Boden, mehrere peitschende Kristallschläge | 1,2 s |
| `entity.virus_heart.summon` | Schrei aus Glas und Fleisch, Sporenrauschen schwillt an | 2 s |
| `block.purity_anchor.grow` | Sanfter, aufsteigender Glockenton (Tonhöhe steigt mit dem Radius) | 1,5 s |

Neue Texturen: siehe [ASSETS.md](../../ASSETS.md), Abschnitt „Story-Erweiterung 2“ (Paletten wie oben: Virus, Reinheit Elfenbein/Gold/Himmelblau, Splitterweite Violett/Cyan).

## Blockbench / Aseprite – Feinschliff

* Blöcke: in Aseprite 4× Kachel-Vorschau (View → Tiled Mode) prüfen; die Adern wiederholen sich im 16-px-Raster, bei Bedarf 1–2 Adern pro Block von Hand variieren.
* Leuchtschichten: `_glow` ist eine eigene Ebene; Animations-Frames nur in der Helligkeit ändern, nie die Form.
* Virusherz: in Blockbench (Bedrock-Modell) die Tentakel-Bones `detail_tendril_*` gemeinsam posen; UVs sind geteilt, Textur einmal malen genügt. Glowmask als zweite Ebene pflegen.
* Shard-Zombie/Kuh: nutzen Vanilla-Modelle, deshalb nur aufgemalte Kristalle. Echte Geometrie wäre nur über ein eigenes Modell möglich.
* Overlay: Risse sind prozedural; für Feinkontrolle in Aseprite einzelne Äste löschen, die Mitte (≈140 px) frei lassen.

## Testliste (runClient)

1. Infizierte Blöcke setzen: Adern leuchten auch bei Dunkelheit, Gras/Laub pulsieren.
2. Laub: transparente Ränder sauber (Cutout), keine leuchtenden Pixel auf Transparenz.
3. Reinheitsanker: Kristall dreht Lichtband, Rune sichtbar, Goldrand.
4. Virusherz: Spawn, Idle-Schwanken, Angriff (Tentakel), Beschwörung, Treffer, Tod bleibt am Ende stehen.
5. Shard-Zombie/Kuh: Augen und Adern leuchten (Glowmask).
6. Cape (inkl. Elytra), Effekt-Icon im HUD, Spore-Partikel getintet, Breach-Overlay mit freier Bildmitte, Advancement-Hintergrund.

## Nach dem Ausbruch (Kapitel 7–9)

### Paletten der Virusstämme (`virus_strains.py`)

| Stamm | Rillen (dunkel → hell) | Leuchten (dunkel → hell) |
|---|---|---|
| Shardvirus | `#0d0412 #240a2c #481044 #7c1a68 #b8268f` | `#a3208a #e03cc0 #ff6fdc #ffb3f0 #fff0fb` |
| Glutbrand (Nether) | `#120403 #2a0905 #4a1206 #7a2408 #b3400c` | `#a3300c #e0601a #ff8a2a #ffc46a #fff2c8` |
| Leerenfäule (End) | `#06040c #120a20 #1e1236 #2a1d4c #3b2a66` | `#1f6e5c #2fa384 #5fd6ac #a8f5d6 #f0fff6` |

Nachhall: Geist-Rampe `#3a4a5e → #ffffff`, Leuchten `#3fb8c8 → #ffffff`, Alpha 80 %. Schwärenherz: Eiter `#160a04 → #cf9a2c`, Leuchten `#a05a10 → #fff0a0`.

### Sound-Beschreibungen (`.ogg`, mono, 44,1 kHz; Platzhalter = Vanilla-Klang aus `sounds.json`)

| Event | Beschreibung | Länge |
|---|---|---|
| `entity.rhea.voice` | Müdes Ausatmen, kurzes „hm“, ruhige tiefe Frauenstimme ohne Worte | 1,2 s |
| `entity.rhea.hurt` / `.death` | Scharfes Einatmen / schweres Ausatmen, Rüstung klappert | 0,4 / 1,2 s |
| `entity.veyl.voice` | Geflüster aus mehreren Lagen, Papierrascheln, ferner Hall | 1,8 s |
| `entity.veyl.hurt` / `.death` | Flackerndes Glas, Ton bricht kurz weg / langes Verklingen, letzte Seite raschelt | 0,5 / 2,5 s |
| `entity.ilsa.voice` | Summen einer Melodie, Werkzeug klirrt leise | 1,5 s |
| `entity.ilsa.hurt` / `.death` | Überraschter Laut / Seufzer, Fläschchen zerbricht | 0,4 / 1,2 s |
| `entity.kesh.voice` | Murmeln, darunter ein feiner falscher Kristallton | 1,4 s |
| `entity.kesh.hurt` / `.death` | Zischen + Splittern / Seufzer, der in einen reinen Ton übergeht | 0,4 / 1,5 s |
| `ui.dialogue.open` / `.close` | Weiches Aufschlagen von Pergament / Zuklappen | 0,4 s |
| `ui.dialogue.type` | Sehr kurzer, leiser Kristall-Klick (variiert in der Tonhöhe) | 0,05 s |
| `ui.dialogue.choice` | Heller Doppelklick | 0,15 s |
| `ui.quest.update` / `.complete` | Kleiner aufsteigender Glockenton / Dreiklang mit Glanz | 0,6 / 1,8 s |
| `story.epilogue` | Ruhiges Thema (Harfe, Streicher), endet offen | 8 s |
| `event.aftershock` | Tiefes Grollen von unten, Kristalle klirren nach | 2,5 s |
| `block.festering_node.hum` | Nasses Pulsieren mit dissonantem Summton | 2 s |
| `block.festering_node.purify` | Ton löst sich in eine reine Quinte auf, Glas rieselt | 2 s |
| `block.festering_node.bind` | Ton beugt sich nach unten, dumpfer Schlag | 1,5 s |
| `block.festering_node.scream` | Schrei aus Glas und Fleisch | 1,2 s |
| `item.keeper_tuning_fork.ring` | Klarer Stimmgabelton (A), Tonhöhe vom Code variiert | 1,5 s |
| `entity.crystallized_piglin.*` | Grunzen mit Glutknistern; Treffer: Splittern; Tod: Grunzen bricht in Funkenregen ab | 1 / 0,4 / 1 s |
| `entity.crystallized_enderman.*` | Enderman-Rauschen mit hohlem Kristallecho; Tod: rückwärts gesogenes Glas | 1,5 / 0,4 / 1,5 s |

### Testliste (runClient) – Nach dem Ausbruch

1. Welt mit Ende (`/shardline story vote seal` bzw. Entscheidung) → Kapitelkarte 7. Resonanzkompass in der Splitterweite auf „Überlebendenlager“ stellen, hinlaufen.
2. Lager: Palisade mit Laternen, Feuer, Zelte, Werkstatt; Rhea, Ilsa, Kesh stehen an ihrem Platz, schauen den Spieler an, schlafen nachts.
3. Rhea ansprechen: Dialogfenster (Porträt, Name, Schreibmaschine, Antworten 1–4, Verlauf L), Chronik-Tagebuch erhalten, `J` öffnet das Auftragsbuch (alle 4 Reiter, Mausrad, Tooltip bei langen Titeln). GUI-Skalierung 1–4 und kleines Fenster prüfen.
4. „Die erste Schwäre“: Kesh folgt, Schwäre erscheint, Stimmgabel horcht (Richtung), Schwäre anschlagen → Echosplitter; Kesh zu Rhea → Kapitel 8 im richtigen Zweig.
5. Ilsa: Handel öffnet sich aus dem Dialog; „Der Gezeichnete“ → Kesh wechselt auf die geheilte Textur.
6. Versiegelt: Anker-Netz, Hüter-Bibliothek finden, Veyl schwebt (durchscheinend, Augen leuchten), Nachhall erscheint, Bossleiste, Sieg → Epilog-Wahl.
7. Beherrscht: Gefolge, Stimmlose, Schwärenherz neben dem Lager (Gegengesang trifft Herrscher), Sieg → Epilog-Wahl.
8. Nachbeben (Config auf 1 Minute): Wackeln, violetter Rand, neue Schwäre in der Nähe.
9. Virus in der Tiefe: Höhle unter infizierter Oberfläche anschauen, Adern im Gestein. Nether/End besuchen: Glutbrand (orange, glimmt, Feuer) und Leerenfäule (blassgrün, Dunkelheit), kristallisierte Piglins und Endermen.

## Stand der Klänge

Eigene Klänge liegen unter `assets/shardline/sounds/` (Ogg Vorbis, 48 kHz; Mono für Klänge mit Position, Stereo nur für globale Ereignisse). Neue Lieferungen (WAV, 16 bit) übernimmt `python tools/assetgen/import_sounds.py <ordner>`: Stille kürzen, Spitzenpegel −1 dBFS, Ogg. Dazu die Datei in `SOUNDS` (Skript) und das Event in `ModSounds.FILES` eintragen, dann `runData`. `validate.py` und der GameTest `soundFiles` prüfen, dass jede eingetragene Datei existiert.

**Eigene Klänge (24 Events, 23 Dateien):** `entity.crystal_colossus.roar|slam`, `entity.shard_queen.cast|scream|teleport`, `entity.shard_zombie.ambient`, `entity.crystallized_cow.death`, `entity.virus_heart.pulse`, `block.shard_portal.travel` und `item.shard_key.use` (dieselbe Datei), `block.memory_crystal.vision`, `block.purity_anchor.activate`, `block.shard_ore.break` (neu, Abbauklang beider Shard-Erze), `item.lore_book.open`, `item.purification_essence.use`, `item.shard_fragment.pickup` (neu, beim Aufheben von Shard-Splittern), `ability.shard_call|crystal_wave`, `story.chapter`, `event.virus_outbreak|sealed|dominated`, `music.splitterweite|infected` (gestreamt).

**Noch Vanilla-Platzhalter (111 Events)** – Beschreibungen stehen weiter oben in diesem Dokument:

| Gruppe | Events |
|---|---|
| Bosse | `crystal_colossus.ambient|hurt|death|pillar|summon`, `shard_queen.ambient|hurt|death`, `virus_heart.ambient|hurt|death|phase|tendrils|summon` |
| Wesen der Weite | `shard_creeper.hurt|death|fuse`, `crystal_golem.ambient|hurt|death|angry`, `glow_moth.*`, `ash_walker.*`, `shard_scout.*` + `shoot`, `void_ray.*` + `flap`, `crystal_spider.*`, `shadow_fox.*` + `vanish`, `crystal_snail.*` + `hide`, `shattered_knight.*` + `charge`, `shard_trader.*` + `yes|no|trade` |
| Befallene | `shard_zombie.hurt|death`, `crystallized_cow.ambient|hurt`, `crystallized_sheep|pig|villager|piglin|enderman.ambient|hurt|death`, `infected.cured` |
| NPCs | `rhea|veyl|ilsa|kesh.voice|hurt|death` |
| Blöcke | `shard_portal.ambient`, `purity_anchor.grow`, `decision_altar.rise|awaken|use`, `festering_node.hum|purify|bind|scream` |
| Items, Fähigkeiten | `shard_projectile.shatter`, `crystal_shield.activate`, `colossus_gauntlet.slam`, `heart_of_the_expanse.blink`, `queens_shardblade.volley`, `keeper_tuning_fork.ring` |
| Oberfläche, Story | `ui.dialogue.open|close|type|choice`, `ui.quest.update|complete`, `story.epilogue`, `event.aftershock` |
