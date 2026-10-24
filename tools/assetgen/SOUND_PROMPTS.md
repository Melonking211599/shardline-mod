# Shardline – Prompts für die restlichen 111 Sounds

Für KI-Klanggeneratoren (z. B. ElevenLabs Sound Effects, Stable Audio). Die Prompts sind englisch, weil die Generatoren damit am besten arbeiten.

## So benutzen

1. **Stil-Präfix** (unten) vor jeden Prompt setzen.
2. Die **Länge** aus der Tabelle einstellen (Dauer-Feld des Generators).
3. Als **WAV, 16 bit** speichern. Der Dateiname ist die Spalte „Datei“, genau so, mit langem Gedankenstrich. Dann zählt `import_sounds.py` die Datei ohne Änderung mit.
4. Stille am Anfang und Ende ist egal, das Skript kürzt sie und pegelt auf −1 dBFS.
5. Bei Tönen, die oft laufen (`ambient`, `hurt`, `ui.*`), lohnen sich 2–3 Varianten. Dateiname dann mit `(2)`, `(3)` am Ende, ich trage sie als Varianten ein.
6. Wenn eine Datei nicht klingt, bitte nur den Prompt der Zeile ändern und neu erzeugen.

**Stil-Präfix (immer davor):**

> Dark fantasy video game sound effect, crystalline glass-like timbre, cold and eerie, dry with short natural reverb, clean recording, no music, no voice words, no background noise. 

**Für Wesen und Stimmen** zusätzlich: *“single isolated sound, no echo tail”*.
**Für Musik und Themen** gibt es keinen Präfix, sie stehen am Ende.

Mono genügt für alles außer den Zeilen mit „Stereo“.

---

## 1. Wesen der Weite

### Splitterkriecher (`shard_creeper`)

| Datei | Prompt | Länge |
|---|---|---|
| `Shard Creeper — Treffer.wav` | Short hiss and crack of a crystal creature being hit, glass chip, 0.3 seconds | 0,4 s |
| `Shard Creeper — Tod.wav` | Creature collapses into shattering glass shards, rattling tinkle, final soft hiss | 1 s |
| `Shard Creeper — Zünden.wav` | Rising crystal charging whine with crackling glass, building tension like a fuse, ends abruptly | 1,5 s |

### Kristallgolem (`crystal_golem`)

| Datei | Prompt | Länge |
|---|---|---|
| `Crystal Golem — Umgebung.wav` | Deep slow grinding of stone on crystal, low hollow rumble, faint chime | 2 s |
| `Crystal Golem — Treffer.wav` | Heavy stone crack and crystal clang, short | 0,5 s |
| `Crystal Golem — Tod.wav` | Massive stone creature crumbles, heavy rubble collapse with crystal shards falling and ringing | 2 s |
| `Crystal Golem — Wut.wav` | Angry deep roar made of grinding stone and resonating crystal, aggressive, rising pitch | 1,5 s |

### Leuchtmotte (`glow_moth`)

| Datei | Prompt | Länge |
|---|---|---|
| `Glow Moth — Umgebung.wav` | Tiny soft fluttering wings with a faint glassy chime, delicate and peaceful | 1 s |
| `Glow Moth — Treffer.wav` | Small startled squeak with a tiny chime, very short | 0,25 s |
| `Glow Moth — Tod.wav` | Tiny wings falter, a soft descending chime fades out | 0,8 s |

### Aschewandler (`ash_walker`)

| Datei | Prompt | Länge |
|---|---|---|
| `Ash Walker — Umgebung.wav` | Dry rasping breath and low groan of an ash-covered undead, powdery rustle | 1,5 s |
| `Ash Walker — Treffer.wav` | Short dry grunt with a puff of ash | 0,4 s |
| `Ash Walker — Tod.wav` | Long dry death rattle, body collapses into a pile of dust | 1,5 s |

### Shard-Späher (`shard_scout`)

| Datei | Prompt | Länge |
|---|---|---|
| `Shard Scout — Umgebung.wav` | Floating crystal eye hum, soft electric whirring with faint glass ping | 1,5 s |
| `Shard Scout — Treffer.wav` | Glass crack and electric squeal | 0,3 s |
| `Shard Scout — Tod.wav` | Floating crystal shatters, falling shards tinkle, humming fades | 1 s |
| `Shard Scout — Schuss.wav` | Sharp crystal shard projectile launch, quick whoosh with glass zing | 0,5 s |

### Void-Rochen (`void_ray`)

| Datei | Prompt | Länge |
|---|---|---|
| `Void Ray — Umgebung.wav` | Slow ethereal whale-like call, airy and deep, gentle glide | 2,5 s |
| `Void Ray — Treffer.wav` | Short startled airy cry | 0,5 s |
| `Void Ray — Tod.wav` | Long sad descending whale-like call fading into wind | 2,5 s |
| `Void Ray — Flügelschlag.wav` | One slow soft flap of huge membrane wings, airy whoosh | 0,7 s |

### Kristallspinne (`crystal_spider`)

| Datei | Prompt | Länge |
|---|---|---|
| `Crystal Spider — Umgebung.wav` | Clicking spider legs on stone with glassy ticks and a dry hiss | 1,2 s |
| `Crystal Spider — Treffer.wav` | Sharp spider screech with a glass crack | 0,3 s |
| `Crystal Spider — Tod.wav` | Shrill dying screech, legs curl with clicking, crystal shell cracks | 1 s |

### Splitterhändler (`shard_trader`)

Nur Laute ohne Worte, freundlich-geheimnisvoll, wie ein Murmeln.

| Datei | Prompt | Länge |
|---|---|---|
| `Shard Trader — Umgebung.wav` | Wise old traveler murmuring “hmm” with a faint crystal tinkle from a backpack, no words | 1,5 s |
| `Shard Trader — Treffer.wav` | Startled “oh!” grunt of an old man with a clink of crystals | 0,4 s |
| `Shard Trader — Tod.wav` | Sad sigh, backpack of crystals falls and clinks to the ground | 1,2 s |
| `Shard Trader — Ja.wav` | Pleased approving “mhm” with a bright small chime | 0,6 s |
| `Shard Trader — Nein.wav` | Disapproving “hmm-mm” with a dull clink | 0,6 s |
| `Shard Trader — Handel.wav` | Coins and crystal shards exchanged, bright coin clink with a crystalline chime | 0,8 s |

### Schattenfuchs (`shadow_fox`)

| Datei | Prompt | Länge |
|---|---|---|
| `Shadow Fox — Umgebung.wav` | Soft fox chirp, quiet and sly, with a faint shadowy whisper | 1 s |
| `Shadow Fox — Treffer.wav` | Short sharp fox yelp | 0,3 s |
| `Shadow Fox — Tod.wav` | Fox whimper that fades into silence with a dark airy breath | 1 s |
| `Shadow Fox — Verschwinden.wav` | Fast ghostly whoosh as something dissolves into shadow, sucked-in air, soft low thump | 0,8 s |

### Kristallschnecke (`crystal_snail`)

| Datei | Prompt | Länge |
|---|---|---|
| `Crystal Snail — Umgebung.wav` | Tiny wet squelching slither with a soft glassy tinkle | 1,2 s |
| `Crystal Snail — Treffer.wav` | Small squeaky squelch | 0,3 s |
| `Crystal Snail — Tod.wav` | Wet pop, shell cracks and tinkles | 0,8 s |
| `Crystal Snail — Verstecken.wav` | Quick snail pulling into crystal shell, wet slurp then a soft glass click | 0,5 s |

### Gesplitterter Ritter (`shattered_knight`)

| Datei | Prompt | Länge |
|---|---|---|
| `Shattered Knight — Umgebung.wav` | Slow heavy armor clinking with hollow breath, low murmur of a haunted knight | 1,8 s |
| `Shattered Knight — Treffer.wav` | Armor clang and a pained grunt, glass crack | 0,4 s |
| `Shattered Knight — Tod.wav` | Armored body collapses, metal and crystal shards clatter to the floor, hollow final groan | 1,8 s |
| `Shattered Knight — Ansturm.wav` | War cry of a charging knight, heavy armor rushing, crystal sword raised | 1 s |

---

## 2. Bosse

### Kristallkoloss (`crystal_colossus`, Brüllen und Stampfen sind schon da)

| Datei | Prompt | Länge |
|---|---|---|
| `Crystal Colossus — Umgebung.wav` | Giant crystal titan idle, deep slow breathing and grinding stone, ominous low rumble | 2,5 s |
| `Crystal Colossus — Treffer.wav` | Heavy impact on giant crystal body, deep clang and cracking stone | 0,6 s |
| `Crystal Colossus — Tod.wav` | Giant crystal titan falls apart, long thunderous collapse with shattering crystal tower and a final low groan | 4 s |
| `Crystal Colossus — Säule.wav` | Crystal pillar erupting from the ground, sharp growth crack with rising glass shards | 0,9 s |
| `Crystal Colossus — Rufen.wav` | Giant crystal titan calls minions, deep horn-like roar with crystal resonance, ominous | 2 s |

### Splitterkönigin (`shard_queen`, Schrei, Magie und Teleport sind schon da)

| Datei | Prompt | Länge |
|---|---|---|
| `Splitter Queen — Umgebung.wav` | Ethereal glass queen idle, airy choir-like hum with soft glass chimes, regal and sad | 2,5 s |
| `Splitter Queen — Treffer.wav` | Glass cracking with a pained ethereal gasp | 0,5 s |
| `Splitter Queen — Tod.wav` | Glass queen shatters, long descending ethereal wail, huge glass crash, then silence | 4 s |

### Virusherz (`virus_heart`, Puls ist schon da)

| Datei | Prompt | Länge |
|---|---|---|
| `Virus Heart — Umgebung.wav` | Deep wet heartbeat thump-thump with faint crystal crackle underneath | 1,2 s |
| `Virus Heart — Treffer.wav` | Wet squelching burst with shrill glass screech | 0,4 s |
| `Virus Heart — Tod.wav` | Heartbeat swells, loud wet burst, crystal rain and a deep exhale | 3 s |
| `Virus Heart — Phase.wav` | Heart contracts with creaking crunch, then a heavy deep sub-bass thud | 1,6 s |
| `Virus Heart — Tentakel.wav` | Wet ripping from the ground, several whipping crystal lashes | 1,2 s |
| `Virus Heart — Beschwörung.wav` | Scream of glass and flesh with a swelling hiss of spores | 2 s |

### Projektil

| Datei | Prompt | Länge |
|---|---|---|
| `Shard Projectile — Zerspringen.wav` | Small crystal shard hits a surface and shatters, quick glass crack and tinkle | 0,4 s |

---

## 3. Befallene (Shardvirus)

Alle Befallenen klingen wie das normale Tier, aber verzerrt, mit Kristallklirren.

| Datei | Prompt | Länge |
|---|---|---|
| `Shard Zombie — Treffer.wav` | Zombie growl cut by a glass crack, short | 0,4 s |
| `Shard Zombie — Tod.wav` | Long rasping death groan ending in a shattering decay of glass | 1,2 s |
| `Crystallized Cow — Umgebung.wav` | Muffled distorted cow moo with crystal chimes | 1,5 s |
| `Crystallized Cow — Treffer.wav` | Short distorted moo with glass clink | 0,4 s |
| `Crystallized Sheep — Umgebung.wav` | Muffled sheep bleat with fine glass tinkle at the end | 1,2 s |
| `Crystallized Sheep — Treffer.wav` | Short bleat with splintering glass | 0,4 s |
| `Crystallized Sheep — Tod.wav` | Bleat cuts off, crystal rain falls | 1 s |
| `Crystallized Pig — Umgebung.wav` | Pig grunt with a hollow crystal resonance | 1 s |
| `Crystallized Pig — Treffer.wav` | Pig squeal with a glass clink | 0,4 s |
| `Crystallized Pig — Tod.wav` | Pig squeal, then shattering glass | 1 s |
| `Crystallized Villager — Umgebung.wav` | Deep mumbling “hmm” with a faint wrong dissonant hum underneath | 1,5 s |
| `Crystallized Villager — Treffer.wav` | Frightened “hrm” grunt and glass splinter | 0,4 s |
| `Crystallized Villager — Tod.wav` | Sigh that turns into a pure crystal tone | 1,5 s |
| `Crystallized Piglin — Umgebung.wav` | Piglin grunt with crackling embers | 1 s |
| `Crystallized Piglin — Treffer.wav` | Short piglin squeal with glass splinter | 0,4 s |
| `Crystallized Piglin — Tod.wav` | Grunt breaks off into a rain of sparks and glass | 1 s |
| `Crystallized Enderman — Umgebung.wav` | Enderman-like warbling drone with hollow crystal echo | 1,5 s |
| `Crystallized Enderman — Treffer.wav` | Short distorted enderman screech with glass crack | 0,4 s |
| `Crystallized Enderman — Tod.wav` | Distorted wail, glass sucked backwards, reversed shatter | 1,5 s |
| `Infected — Geheilt.wav` | Crystal crumbling off, a clear bell tone, a relieved exhale | 2 s |

---

## 4. Figuren (NPCs, ohne Worte)

Nur Atem und Laute. Keine verständliche Sprache.

| Datei | Prompt | Länge |
|---|---|---|
| `Rhea — Stimme.wav` | Tired exhale and a short “hm” of a calm low woman’s voice, no words | 1,2 s |
| `Rhea — Treffer.wav` | Sharp intake of breath, light armor rattle | 0,4 s |
| `Rhea — Tod.wav` | Heavy exhale, armor clatters to the ground | 1,2 s |
| `Veyl — Stimme.wav` | Layered whisper, ghostly, paper rustling, distant reverb, no clear words | 1,8 s |
| `Veyl — Treffer.wav` | Flickering glass, tone cuts out briefly | 0,5 s |
| `Veyl — Tod.wav` | Long fading ghostly tone, a last page rustles | 2,5 s |
| `Ilsa — Stimme.wav` | Humming a quiet melody, tools clink softly | 1,5 s |
| `Ilsa — Treffer.wav` | Surprised short gasp | 0,4 s |
| `Ilsa — Tod.wav` | Sigh, a glass vial breaks | 1,2 s |
| `Kesh — Stimme.wav` | Low murmuring with a thin wrong crystal tone beneath it | 1,4 s |
| `Kesh — Treffer.wav` | Hiss and glass splinter | 0,4 s |
| `Kesh — Tod.wav` | Sigh that turns into one pure tone | 1,5 s |

---

## 5. Blöcke, Items, Fähigkeiten

| Datei | Prompt | Länge |
|---|---|---|
| `Splitter Portal — Umgebung.wav` | Portal ambient loop, low shimmering hum with faint crystal glints, seamless loop | 3 s |
| `Reinheitsanker — Wachsen.wav` | Gentle rising bell tone, soft and warm, pitch climbs slowly | 1,5 s |
| `Entscheidungsaltar — Erheben.wav` | Deep rumble, stone grinding, then a chord of golden and magenta crystal tones | 4 s |
| `Entscheidungsaltar — Erwachen.wav` | Two tones at once: warm bell and dissonant glass, swelling up | 2 s |
| `Entscheidungsaltar — Benutzen.wav` | Core clicks into place, dull heavy thud, then reverb | 1,5 s |
| `Schwäre — Summen.wav` | Wet pulsing with a dissonant humming tone, unsettling | 2 s |
| `Schwäre — Reinigen.wav` | Dissonant tone resolves into a pure fifth, glass trickles down | 2 s |
| `Schwäre — Binden.wav` | Tone bends downward, dull thump | 1,5 s |
| `Schwäre — Schrei.wav` | Scream of glass and flesh | 1,2 s |
| `Stimmgabel — Klang.wav` | Clear tuning fork tone in A (440 Hz), long ring-out | 1,5 s |
| `Kristallschild — Aktivieren.wav` | Magic crystal shield forming, bright rising shimmer then a solid glassy lock-in | 1 s |
| `Kolossfaust — Schlag.wav` | Heavy crystal gauntlet ground slam, deep boom with shockwave and glass crack | 1 s |
| `Herz der Splitterweite — Sprung.wav` | Short teleport blink, quick whoosh with a glass-bell chime and a soft pop | 0,6 s |
| `Splitterklinge — Salve.wav` | Sword swing releasing fan of crystal shards, whoosh and multiple glass zings | 0,9 s |

---

## 6. Oberfläche und Story

| Datei | Prompt | Länge |
|---|---|---|
| `UI Dialog — Öffnen.wav` | Soft parchment unfolding, quiet paper flap | 0,4 s |
| `UI Dialog — Schließen.wav` | Soft parchment closing | 0,4 s |
| `UI Dialog — Tippen.wav` | Very short tiny quiet crystal click, 50 ms, soft | 0,05 s |
| `UI Dialog — Wahl.wav` | Bright light double click, small | 0,15 s |
| `UI Auftrag — Update.wav` | Small rising bell chime, short | 0,6 s |
| `UI Auftrag — Fertig.wav` | Rising three-note bell chord with a shiny glow | 1,8 s |
| `Nachbeben.wav` | Deep rumble from below, a sub-bass quake, crystals ring afterwards | 2,5 s |

**Kein Präfix, Stereo, mehrstimmig:**

| Datei | Prompt | Länge |
|---|---|---|
| `Story Epilog — Thema.wav` | Calm cinematic theme, soft harp and warm string pad, hopeful but melancholic, ends open and unresolved, slow, no drums | 8 s |

---

## Zusammenfassung

| Gruppe | Dateien |
|---|---|
| Wesen der Weite | 42 |
| Bosse und Projektil | 15 |
| Befallene | 20 |
| Figuren | 12 |
| Blöcke, Items, Fähigkeiten | 14 |
| Oberfläche und Story | 8 |
| **Summe** | **111** |

Eine Datei pro Event. Die Dateinamen nennen Wesen und Aktion auf Englisch/Deutsch gemischt; die Zuordnung zu den Events (`entity.shard_creeper.hurt` usw.) trage ich beim Einbauen in `import_sounds.py` und `ModSounds.FILES` ein.
