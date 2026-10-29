# Shardline – Story und Shardvirus

Die Story-Erweiterung erzählt, warum die Splitterweite zerbrochen ist. Am Ende entscheiden die Spieler, wie es mit dem Shardvirus weitergeht. Die gesamte Logik läuft auf dem Server. Der Client zeigt nur an, was der Server ihm mitteilt.

## Die Geschichte in Kürze

Einst war die Splitterweite **Aurelis**: ein einziger, endloser Kristall, zusammengehalten von einem Lied, der *Resonanz*. Tief unter dem Herzen entstand ein falscher Ton. Er vermehrte sich und ließ Stein, Tiere und Menschen kristallisieren. Das war der **Shardvirus**.

Die Hüter von Aurelis konnten ihn nicht zerstören. Deshalb nahm ihre geduldigste Hüterin, **Selvaine**, ihn in sich auf und wurde so selbst zum **Siegel**. Ein Kristallkoloss sollte den Weg zu ihr bewachen. Durch das Ritual zersprang Aurelis in tausend schwebende Inseln.

Jahrhunderte später ist Selvaine die gläserne **Splitterkönigin**. Sie greift jeden an, der sich nähert, aber nicht aus Bosheit: Sie schützt das Siegel. Wer sie tötet, setzt den Virus frei. Er kriecht durch die Portale bis in die Oberwelt.

Der Splitterhändler war früher selbst ein Hüter und warnt die Spieler. Nach dem Bruch müssen die Spieler den Virus eindämmen und drei Virusherzen zerstören. Der **Kern des Bruchs**, der dabei entsteht, wird zum Thron gebracht. Dort wird entschieden: **versiegeln** oder **beherrschen**.

## Kapitel

Der Fortschritt wird pro Spieler und pro Welt in `data/shardline_story.dat` (SavedData) gespeichert. Kapitel werden nie übersprungen. Wer ein späteres Kapitel erreicht, erhält auch alle vorherigen Advancements.

| Nr. | Kapitel | Ausgelöst durch |
|---|---|---|
| 1 | Der erste Splitter | Shard-Splitter oder Roh-Shard aufheben, Shard-Barren herstellen oder schmelzen |
| 2 | Das Tor | die Splitterweite betreten |
| 3 | Echos der Hüter | ein Lore-Buch lesen (ab Kapitel 2) oder einen Erinnerungskristall berühren |
| 4 | Das Herz der Weite | den Kristallkoloss besiegen (Spieler im Umkreis von 48 Blöcken) |
| 5 | Der Bruch | die Königin fällt (Umkreis 64), infizierten Boden betreten oder einen infizierten Mob töten |
| 6 | Eindämmung | Reinheitsanker setzen, Reinigungsessenz benutzen oder ein Virusherz besiegen (nach dem Bruch) |
| 7 | Das Echo der Wunde | die Welt hat ein Ende (Anmeldung, Dimensionswechsel oder Endentscheidung) |
| 8 | Wiederaufbau / Herrschaft | Rhea schließt „Die erste Schwäre“ ab; Zweig = Ende der Welt (`seal` / `dominate`) |
| 9 | Der letzte Ton: Nachhall / Gegengesang | Veyl (versiegelt) bzw. Ilsa (beherrscht), wenn die zwei Zweig-Aufträge erledigt sind |

Kapitel 7–9 beschreibt der Abschnitt [Nach dem Ausbruch](#nach-dem-ausbruch-kapitel-79).

Beim Erreichen eines Kapitels erscheint eine **Kapitelkarte**: ein Banner mit dem Kapitel-Emblem, „Kapitel 3“, dem Titel („Echos der Hüter“) und der Zusammenfassung. Sie blendet sich ein, steht einige Sekunden und blendet sich wieder aus (`ChapterCardPayload`, abschaltbar mit `chapterCard` in der Client-Config, dann wie früher als Titel). Dazu kommen eine Chatzeile und ein Sound. Dazu kommt das Advancement im eigenen Tab **Shardline: Chronik** (`shardline:story/…`). Jedes Kapitel-Advancement hat ein eigenes Emblem als Icon (`shardline:chapter_emblem`, nur als Symbol, nicht herstellbar). `/shardline story status` zeigt jedem Spieler seinen Fortschritt und das nächste Ziel. Alle Story-Advancements nutzen `minecraft:impossible` und werden vom Server vergeben.

## Erzählung im Spiel

### Lore-Bücher (12)

Die Bücher sind schreibgeschützt. Ihr Text steht in den Sprachdateien, deshalb liest jeder Spieler sie in seiner eigenen Sprache. Item: `shardline:lore_book` mit der Komponente `shardline:lore_book` (Buch-ID).

| Nr. | ID | Titel | Autor | Fundort |
|---|---|---|---|---|
| 1 | `wanderer_notes` | Notizen eines Wanderers | ein namenloser Wanderer | Ruinen-Turm (Truhe), Splitterkrater, Handelsposten |
| 2 | `crystal_kingdom` | Das Kristallreich Aurelis | Chronist Veyl | Ruinen-Turm (Truhe), Hüter-Schrein |
| 3 | `keepers_oath` | Der Eid der Hüter | Orden der Hüter | Ruinen-Turm (Truhe), Hüter-Schrein |
| 4 | `the_resonance` | Über die Resonanz | Magister Orrin | Ruinen-Turm (Truhe), Kristallgeode |
| 5 | `first_infection` | Die erste Ansteckung | Heilerin Maelis | Ruinen-Turm (Truhe), Aschegruft, Kristallgeode |
| 6 | `the_colossus` | Bauplan des Wächters | Schmiedemeister Duran | Turm-Schatz, Koloss-Arena |
| 7 | `the_last_knights` | Die letzten Ritter | Hauptmann Kael | Turm-Schatz, Koloss-Arena, Aschegruft |
| 8 | `queen_selvaine` | Selvaine, die Glasgekrönte | Chronist Veyl | Turm-Schatz, Hüter-Schrein |
| 9 | `the_seal` | Das Siegel | Selvaine | Königinnen-Arena |
| 10 | `the_shattering` | Der Tag des Zerspringens | unbekannt | Turm-Schatz, Königinnen-Arena, Aschegruft |
| 11 | `traders_ledger` | Kassenbuch des Händlers | der Splitterhändler | Ruinen-Turm (Truhe), Handelsposten |
| 12 | `the_breach_core` | Vom Kern des Bruchs | Magister Orrin | Koloss-Arena, Königinnen-Arena |

Das erste Lesen eines Buchs zählt mit, zum Beispiel „Lore-Buch gelesen (5/12)“. Wer alle zwölf gelesen hat, erhält ein Advancement. Mit `/shardline lore give <buch|all>` lassen sich Bücher ausgeben.

### Erinnerungskristalle (8 Visionen)

Der Block `shardline:memory_crystal[vision=0..7]` ist unzerstörbar. Ein Rechtsklick zeigt eine Vision mit Titel und Untertitel, drei Chatzeilen nacheinander, einem Partikelwirbel, einem Sound und einem Bildschirmschleier. Danach gilt eine Abklingzeit von 5 Sekunden pro Spieler.

| Vision | Titel | Fundort |
|---|---|---|
| 0, 1, 2, 4, 6 | Aurelis, Der falsche Ton, Der Rat der Hüter, Das Zerspringen, Die letzten Ritter | Erdgeschoss der Ruinen-Türme und Hüter-Schreine (eine zufällige pro Bauwerk) |
| 4 oder 6 | Das Zerspringen, Die letzten Ritter | Aschegruft |
| 5 | Der Wächter | Koloss-Arena |
| 3 | Das Ritual | Königinnen-Arena, vor dem Thron |
| 7 | Die Warnung | Königinnen-Arena, hinter dem Thron |

### Der Splitterhändler als Erzähler

Ein Rechtsklick auf den Händler öffnet ein **Dialogfenster** mit seinem Porträt. Der Text erscheint Zeile für Zeile, ein Klick zeigt ihn sofort ganz. Darunter liegen vier Knöpfe: **Handeln**, **Lore-Seiten**, **Erzähl mehr** und **Tschüss**.
- Spricht ein Spieler den Händler in einem neuen Story-Abschnitt zum ersten Mal an, erzählt der Händler vier Zeilen Einführung. Danach sagt er jeweils eine kurze Bemerkung.
- **Erzähl mehr** liefert drei weitere Zeilen zum aktuellen Abschnitt.
- Mit Schleichen und Rechtsklick handelt man direkt, ohne Dialog.

Es gibt neun Abschnitte: Kapitel 0 bis 6, „versiegelt“ und „beherrscht“. **In Kapitel 4 warnt er vor der Königin** (Advancement „Ungehörte Warnung“). Was gesagt wird und was ein Knopf bewirkt, entscheidet der Server (`TraderDialoguePayload`, `TraderActionPayload`). Clients ohne den Kanal bekommen die Einführung wie früher im Chat.

**Lose Lore-Seiten** (`shardline:lore_page`) liegen in den Truhen der Hüter-Ruinen, Ruinen-Türme, Aschegrüfte und Kristallgeoden. Jede Seite gehört zu einem der zwölf Bücher. Ein Rechtsklick öffnet sie in einem eigenen Lesefenster (Pergamentblatt mit Buchtitel und Seitenzahl). Clients ohne den Kanal lesen sie wie früher im Chat.
- Der Händler kauft lose Seiten für je 2 Shard-Splitter (`story.lorePagePrice`).
- **Vier Seiten desselben Buchs** bindet er zum ganzen Buch, noch ungelesene Bücher zuerst (Advancement „Neu gebunden“).

## Der Bruch

Stirbt die Königin, passiert Folgendes, solange `virus.enabled` aktiv ist und das Ende nicht „versiegelt“ lautet:

- Der Weltzustand wird auf **infiziert** gesetzt.
- Alle Spieler erhalten eine Chatnachricht, einen Titel („DER BRUCH“) und einen Sound.
- Auf dem Bildschirm erscheinen ein Blitz, Risse und eine violette Vignette, dazu wackelt die Kamera.
- Der Himmel in allen Dimensionen mit Tageslicht färbt sich violett.
- Rund um den Todesort beginnt sofort die Infektion.
- In der Oberwelt entstehen sofort Herde in der Nähe jedes Spielers dort und am Weltspawn.
- Danach setzt der Virus alle `virus.overworldSeedMinutes` Minuten (Standard 4, 0 = aus) einen neuen Herd 24 bis 64 Blöcke neben einem zufälligen Spieler in der Oberwelt. Reinheitsanker schützen auch hier.

Ist das Ende bereits „versiegelt“, meldet der Server nur, dass das Siegel hält.

## Shardvirus

### Ausbreitung

- **Quellen:** Infizierte Blöcke breiten sich bei Random Ticks aus.
  - Freiliegendes **Shard-Erz** infiziert seine Nachbarn.
  - **Splitterportale außerhalb der Splitterweite** infizieren ihre Umgebung (Radius 6).
  - **Infizierte Mobs** infizieren den Boden unter sich.
- **Oberflächen zuerst:** Befallen werden vor allem Blöcke, die Luft berühren (Oberflächen, Höhlenwände). In die Tiefe frisst sich der Virus nur Block für Block und nur in „porösem“ Gestein (siehe [Virus in der Tiefe](#virus-in-der-tiefe-im-nether-und-im-end)); `virus.surfaceOnly` stellt das alte Verhalten her.
- **Reichweite:** Ohne Grenze (`maxRange = 0`), optional begrenzt auf einen Abstand zum nächsten Infektionsherd.
- **Stoppen:**
  - per Befehl: `/shardline virus pause`, `/shardline virus purge`
  - per Config: `virus.spreadPaused`
  - im Spiel: Reinheitsanker, Reinigungsessenz, Virusherzen besiegen oder das Ende „versiegeln“

**Performance:** Alle Quellen schlagen nur Positionen vor. Diese landen in einer Warteschlange pro Dimension, gruppiert nach Chunk.
- **Pro Tick:** Am Tickende werden höchstens `maxBlocksPerTick` Positionen (Standard 80) Chunk für Chunk abgearbeitet. Positionen in inzwischen entladenen Chunks werden verworfen.
- **Keine Chunk-Ladungen:** Gelesen wird nur über `getChunkNow`, es wird also nie ein Chunk geladen.
- **Nur in Spielernähe:** Random Ticks gibt es nur in geladenen Chunks in Spielernähe.
- **Große Reinigungen:** Sie laufen als Hintergrundauftrag, pro Tick werden höchstens 12 Chunk-Abschnitte bearbeitet. Abschnitte ohne infizierte Blöcke werden über die Palette übersprungen.

### Infizierte Blöcke

Alle infizierten Blöcke haben eine leuchtende Overlay-Schicht mit voller Eigenhelligkeit. Sie leuchten im Dunkeln, geben aber kein Licht ab und lösen so keine Lichtupdates aus. Welche Blöcke befallen werden, regeln die Block-Tags `shardline:virus_infectable/*`, die sich per Datapack erweitern lassen.

| Infiziert | Entsteht aus (Tag) | Gereinigt in der Oberwelt / Splitterweite | Drop ohne Behutsamkeit |
|---|---|---|---|
| Infizierter Stein | `stone`: Oberwelt-Gestein, Bruchstein, Tiefenschiefer, Splitterstein | Stein / Splitterstein | Bruchstein |
| Infizierte Erde | `dirt`: Erde, Grobe Erde, Wurzelerde, Schlamm, Pfad, Ackerboden | Erde | Erde |
| Infizierter Grasblock | `grass`: Gras, Myzel, Podsol, Moos, Kristallmoos | Grasblock / Kristallmoos | Erde |
| Infiziertes Holz | `log`: natürliche Stämme, Kristallholz | Eichenstamm / Kristallholzstamm | Eichenstamm |
| Infiziertes Laub | `leaves`: alle Blätter | Eichenlaub / Kristallblätter (dauerhaft) | 0–2 Stöcke |
| Infizierter Sand | `sand`: Sand, Roter Sand, Kies, Ascheboden | Sand / Ascheboden | Sand |

### Splitterfieber

Der Effekt `shardline:shard_fever` verursacht alle 3 Sekunden 1 Schaden. Standardmäßig tötet er nicht, mindestens ein halbes Herz bleibt übrig.

Dazu kommt eine verzerrte Sicht: Die Kamera schwankt, das Sichtfeld pulsiert und eine violette Vignette erscheint.

**Ansteckung:**
- infizierter Boden (4 % pro Sekunde)
- Schläge von Shard-Zombies
- Splitter einer kristallisierten Kuh
- die Fieberwelle eines Virusherzens

**Heilung:**
- Reinigungsessenz
- Nähe zu einem Reinheitsanker
- Totem
- Milch nur, wenn `feverCuredByMilk` aktiv ist

Splitterherrscher und infizierte Wesen sind immun.

### Infizierte Mobs

| Mob | Verhalten |
|---|---|
| **Shard-Zombie** | Kristallisierter Zombie: brennt nicht in der Sonne und steckt mit Splitterfieber an. Von ihm getötete Dorfbewohner werden zu Shard-Zombies (schwer 100 %, normal 50 %, leicht 25 %). Setzt neue Infektionsstellen. |
| **Kristallisierte Kuh** | Friedlicher Träger: setzt doppelt so oft Infektionsstellen, gibt keine Milch. Wer sie im Nahkampf trifft, bekommt zu 30 % Fieber. |
| **Kristallisiertes Schaf** | Träger mit einem Vlies aus Viruskristallen. Scheren bringt Amethyst und manchmal Shard-Splitter, das Vlies wächst durch Grasfressen nach. Treffer: 25 % Fieber. |
| **Kristallisiertes Schwein** | Träger, frisst Amethyst, lässt sich satteln. Blitze verwandeln es nicht. Treffer: 25 % Fieber. |
| **Kristallisierter Dorfbewohner** | Handelt nicht mehr, wandert umher und murmelt Bruchstücke des falschen Tons (Rechtsklick). Er behält Beruf, Stufe, Angebote und Ruf des Dorfbewohners, der er war. Treffer: 30 % Fieber. |

Zombies, Wüstenzombies, Kühe, Schafe, Schweine und Dorfbewohner, die auf infiziertem Boden stehen, kristallisieren mit 2 % pro Sekunde. Dorfbewohner werden dabei zu **kristallisierten Dorfbewohnern** (früher Shard-Zombies). Wen ein Shard-Zombie tötet, der wird weiterhin zum Shard-Zombie. Tiere lassen sich mit `virus.animalsTransform` ausnehmen. Wer **Immunität** hat, kristallisiert nicht. **Dorfbewohner und Händler fliehen** vor wilden infizierten Mobs und Virusherzen.

Eine Reinigungsessenz verwandelt alle infizierten Tiere und Shard-Zombies zurück. Ein kristallisierter Dorfbewohner bekommt dabei seinen alten Beruf samt Angeboten zurück (Advancement „Eine Stimme kehrt zurück“).

Splitterherrscher zähmen alle vier infizierten Tiere mit einem Shard-Splitter. Gezähmte infizierte Wesen tragen ein **Halsband** in Magenta mit Kristallnieten, auch Kristall-Kühe und Shard-Zombies. Wer eine Kuh, ein Schaf, ein Schwein und einen Dorfbewohner gezähmt hat, erhält „Kristallherde“.

### Atmosphäre infizierter Gebiete (Client)

Der Client prüft alle halbe Sekunde 75 Blöcke in der Umgebung. Je stärker sie befallen sind, desto deutlicher werden die Effekte:
- violetter Nebel (Sichtweite bis 28 Blöcke)
- eigene Musik (`shardline:music.infected`)
- leuchtende Sporen-Partikel über infizierten Blöcken

Alles lässt sich in `shardline-client.toml` abschalten.

## Gegenmittel

| | |
|---|---|
| **Reinigungsessenz** | Herz der Splitterweite, 3 Leuchtblumen, Shard-Barren und Glasflasche ergeben 3 Essenzen. **Das Herz bleibt erhalten**, es dient als Katalysator. Rechtsklick in die Luft wirkt um dich herum, Rechtsklick auf einen Block wirkt dort (Radius 6). Die Essenz reinigt Blöcke, heilt Fieber, verwandelt infizierte Mobs zurück und gibt allen im Radius **2 Minuten Immunität**. |
| **Reinheitsanker** | Rezept: Essenz oben, darunter Leuchtkristall, Shard-Block, Leuchtkristall, unten 3 Splitterstein-Ziegel. Seine **Reinheitszone** beginnt bei 6 Blöcken und wächst in 5 Minuten auf 24 Blöcke. In der Zone gibt es **keine Ausbreitung**, jeder neu hinzukommende Ring wird gründlich gereinigt, und alle zwei Sekunden reinigt der Anker zusätzlich 24 zufällige Positionen. Lebewesen in der Zone werden vom Fieber geheilt und erhalten Immunität (15 s, ständig aufgefrischt). Goldene Funken und eine goldene Linie am Boden zeigen den Rand. Anker aus älteren Welten haben sofort den vollen Radius. |
| **Virusherz** (Mini-Boss) | Entsteht in Chunks mit mindestens 160 infizierten Blöcken, aber nur, wenn ein Spieler innerhalb von 64 Blöcken ist, im Umkreis von 96 Blöcken kein anderes Herz existiert und seit dem letzten Herz 8 Minuten vergangen sind. Es ist unbeweglich, hat 200 HP und eine Bossleiste mit Phasenmarken. Sein Tod reinigt einen Radius von 40 Blöcken. **Drei Phasen** (bei 2/3 und 1/3 Leben):<br>1. Splittersalven und Fieberwellen (Radius 8).<br>2. Zusätzlich **Tentakelschläge**: Ein Sporenring markiert eine Sekunde vorher die Einschlagstelle.<br>3. Zusätzlich **Beschwörung** von Shard-Zombies und kristallisierten Tieren (Sporenwirbel zeigen die Stellen), schnellere Wellen.<br>Beim Phasenwechsel verhärtet sich das Herz kurz und ist unverwundbar, die Leiste färbt sich pink bzw. rot. Schwierigkeit: `remedies.virusHeartDifficulty`. |
| **Immunität** (Effekt) | Splitterfieber kann nicht ausbrechen und Tiere kristallisieren nicht. Gibt es durch Essenz und Anker. Advancement „Unberührt“. |

Jeder Spieler im Umkreis von 32 Blöcken zählt ein besiegtes Herz. **Nach drei Herzen erhält er den Kern des Bruchs.**

## Endentscheidung

Wenn die Königin fällt, wächst vor ihrem Thron der **Entscheidungsaltar** aus dem Stein (fünf Blöcke vor dem Thron, auf dem Arenaboden). Neue Thron-Arenen haben ihn von Anfang an.
- Der Altar ist unzerstörbar und ruht, bis ein Spieler mit dem Kern des Bruchs näher als 8 Blöcke kommt. Dann erwacht er: Leuchtschicht, helles Licht, goldene und magentafarbene Spiralen (Advancement „Am Scheideweg“).
- Lege den **Kern des Bruchs** auf den Altar. Dafür ist Kapitel 6 nötig. Danach öffnet sich das Entscheidungsfenster: zwei Karten, links „Versiegeln“ (Elfenbein/Gold), rechts „Beherrschen“ (Schwarzviolett/Magenta). Die Karte unter der Maus leuchtet auf, ein Klick wählt.
- Der Thron selbst nimmt den Kern weiterhin an, damit Welten funktionieren, in denen die Königin schon vor diesem Update gefallen ist.

- **`ending.mode = VOTE`** (Standard): Die Wahl eröffnet eine Abstimmung (120 s) unter allen Online-Spielern mit Kapitel 6. Sie erhalten dasselbe Fenster und klickbare Chat-Buttons (`/shardline story vote seal|dominate`). Die Abstimmung endet, sobald alle abgestimmt haben, spätestens nach Ablauf der Zeit. Bei Gleichstand wird versiegelt. Den Titel erhalten alle, die für das Ergebnis gestimmt haben.
- **`ending.mode = FIRST_PLAYER`:** Der erste Spieler entscheidet allein.

| | Versiegeln | Beherrschen |
|---|---|---|
| Virus | stoppt weltweit; befallenes Land bleibt als Mahnmal | breitet sich weiter aus |
| Titel | **[Hüter]** als Namenspräfix (Scoreboard-Team, Chat, Tabliste, Namensschild) | **[Splitterherrscher]** |
| Belohnung | Umhang der Hüter (eigener Render-Layer, für alle Spieler sichtbar) | Immunität gegen Fieber, infizierte Mobs per Shard-Splitter zähmen, dazu drei Fähigkeiten |

Fähigkeiten des Splitterherrschers:
- **Splitterruf** (Taste G): Holt bis zu 3 gezähmte infizierte Diener aus bis zu 128 Blöcken heran. Fehlende werden als Shard-Zombies für 60 s beschworen. Dazu gibt es 10 s Stärke. Abklingzeit 90 s.
- **Kristallwelle** (Taste H): Splitterwelle im Radius 6 mit 8 Schaden, Rückstoß und 4 s Fieber. Abklingzeit 45 s.
- **Virus-Lauf** (passiv): Tempo II und Regeneration I auf infiziertem Boden.
- **Gefolgschaft** (passiv): Die Wesen der Splitterweite (Tag `shardline:obeys_ruler`: Splitterkriecher, Kristallgolem, Aschewandler, Splitterspäher, Kristallspinne, Schattenfuchs, Gesplitterter Ritter, Void-Rochen, Klone der Königin) greifen Splitterherrscher nicht an und lassen von ihnen ab. Bosse bleiben feindlich.

Wer **später** einen Kern auf den Altar oder den Thron legt, schließt sich dem gewählten Weg an und bekommt dessen Titel.

## Befehle

Alle Befehle außer `story status` und `story vote` brauchen Operator-Rechte (Stufe 2).

| Befehl | Wirkung |
|---|---|
| `/shardline story set <0-9> [spieler]` | Kapitel setzen (ab 8 mit dem Zweig des Weltendes) |
| `/shardline story status` | eigener Fortschritt: Kapitelleiste, aktuelles Kapitel, nächstes Ziel, Bücher/Visionen/Herzen, Ende (alle Spieler) |
| `/shardline story info [spieler]` | Kapitel, Bücher, Visionen, Virusherzen, Titel |
| `/shardline story vote <seal\|dominate>` | Stimme abgeben (alle Spieler) |
| `/shardline story ending reset` | Ende und Titel zurücksetzen |
| `/shardline lore give <buch\|all> [spieler]` | Lore-Bücher ausgeben |
| `/shardline virus status` | Zustand, Statistik, Warteschlange, Anker, Herzen |
| `/shardline virus pause` / `resume` | Ausbreitung anhalten / fortsetzen |
| `/shardline virus purge <radius>` | im Radius um dich reinigen (max. 256, als Hintergrundauftrag) |
| `/shardline virus outbreak` | Ausbruch an der eigenen Position (Test) |
| `/shardline virus reset` | Weltzustand „infiziert“ aufheben (Blöcke bleiben) |

## Konfiguration

`config/shardline-common.toml`, neue Abschnitte:

| Abschnitt | Schlüssel (Standard) |
|---|---|
| `story` | `chapterTitles` (true), `traderDialogue` (true), `dialogueLineDelayTicks` (50), `lorePagePrice` (2) |
| `virus` | `enabled` (true), `spreadPaused` (false), `spreadChance` (0.75), `spreadAttempts` (2), `spreadRange` (2), `maxRange` (0 = unbegrenzt), `overworldSeedMinutes` (4, 0 = aus), `maxBlocksPerTick` (80), `queueLimit` (8192), `oreSeedChance` (0.2), `portalSeedChance` (0.75), `portalSeedRadius` (6), `infectedMobSeedChance` (0.065), `mobConversionChance` (0.02), `villagersFlee` (true), `villagersTransform` (true), `animalsTransform` (true), `feverChanceOnInfectedBlock` (0.04), `feverDurationSeconds` (30), `feverDamage` (1.0), `feverCanKill` (false), `feverCuredByMilk` (false) |
| `remedies` | `anchorRadius` (24), `anchorPurifyPerPulse` (24), `anchorStartRadius` (6), `anchorGrowSeconds` (300), `essenceRadius` (6), `immunityEssenceSeconds` (120), `immunityAnchorSeconds` (15), `heartPurifyRadius` (40), `virusHeartHealth` (200), `virusHeartDifficulty` (EASY / NORMAL / HARD), `heartSpawnThreshold` (160), `heartSpawnCooldownMinutes` (8), `heartsForCore` (3) |
| `ending` | `mode` (VOTE / FIRST_PLAYER), `voteDurationSeconds` (120), `titlePrefix` (true) |
| `abilities` | `shardCallCooldownSeconds` (90), `shardCallMaxMobs` (3), `shardCallStrengthSeconds` (10), `crystalWaveCooldownSeconds` (45), `crystalWaveRadius` (6), `crystalWaveDamage` (8), `crystalWaveFeverSeconds` (4), `virusRun` (true) |

`config/shardline-client.toml`: `infectionFog`, `infectionMusic`, `infectedSky`, `screenShake`, `feverDistortion`, `chapterCard` (alle standardmäßig true).

Neue Schlüssel für Kapitel 7–9 und die Virus-Erweiterung: siehe [Nach dem Ausbruch → Konfiguration](#konfiguration-1).

## Neue Inhalte im Überblick

- **Blöcke:**
  - Story: `memory_crystal`, `purity_anchor`
  - Infiziert: `infected_stone`, `infected_dirt`, `infected_grass`, `infected_log`, `infected_leaves`, `infected_sand`
- **Items:**
  - `lore_book` (12 Varianten), `purification_essence`, `breach_core`
  - Spawn-Eier: `shard_zombie_spawn_egg`, `crystallized_cow_spawn_egg`, `virus_heart_spawn_egg`
- **Wesen:** `shard_zombie`, `crystallized_cow`, `virus_heart`
- **Effekt:** `shard_fever`. Schadenstypen: `shard_fever`, `virus_pulse`.
- **Partikel:** `virus_spore`
- **Sounds:**
  - Story und Ereignisse: `story.chapter`, `block.memory_crystal.vision`, `item.lore_book.open`, `event.virus_outbreak`, `event.virus_sealed`, `event.virus_dominated`
  - Gegenmittel: `block.purity_anchor.activate`, `item.purification_essence.use`
  - Mobs: `entity.shard_zombie.*`, `entity.crystallized_cow.*`, `entity.virus_heart.*` (inkl. `pulse`)
  - Fähigkeiten: `ability.shard_call`, `ability.crystal_wave`
  - Musik: `music.infected`
- **Datenkomponente:** `shardline:lore_book`. Attachments: `shard_call_ready_at`, `crystal_wave_ready_at`.
- **Block-Tags:** `shardline:virus_infectable/{stone,dirt,grass,log,leaves,sand}`, `shardline:infected`
- **Tasten:** Splitterruf (G), Kristallwelle (H)

## Neue Klassen

| Paket | Klassen |
|---|---|
| `story` | `StoryChapter`, `StoryEnding`, `ShardlineWorldData` (SavedData), `StoryManager`, `StoryEvents`, `StoryScheduler` |
| `story.lore` | `LoreBooks`, `TraderDialogue`, `Visions` |
| `story.ending` | `EndingManager`, `RulerAbilities` |
| `virus` | `Infected`, `InfectionMap`, `VirusSpreadManager`, `Purification`, `PurityAnchors`, `VirusEffects`, `VirusOutbreak`, `VirusHeartSpawner`, `VirusEvents` |
| `effect` | `ModEffects`, `ShardFeverEffect` |
| `entity.infected` | `InfectedMob`, `InfectedMobs`, `ShardZombieEntity`, `CrystallizedCowEntity` |
| `entity.boss` | `VirusHeartEntity` |
| `block.custom` | `MemoryCrystalBlock`, `InfectedBlock`, `InfectedPillarBlock`, `PurityAnchorBlock`, `ShardOreBlock` |
| `item.custom` | `LoreBookItem`, `PurificationEssenceItem`, `BreachCoreItem` |
| `command` | `ShardlineCommands` |
| `core` | `ModDataComponents` |
| `network` | `StoryStatePayload`, `TitlesPayload`, `ScreenEffectPayload`, `OpenEndingChoicePayload`, `EndingChoicePayload`, `AbilityPayload` (Protokoll 2) |
| `client.story` | `StoryClientState`, `ClientStoryHandler`, `StoryScreenEffects`, `InfectionAtmosphere`, `EndingChoiceScreen` |
| `client.renderer` | `KeeperCapeLayer`, `InfectedRenderers` |
| `datagen` | `ModStoryAdvancements` |

Angepasst wurden:
- **Registries und Setup:** `Shardline`, `ModBlocks`, `ModItems`, `ModEntities`, `ModEntityEvents`, `ModSounds`, `ModParticles`, `ModAttachments`, `ModDamageTypes`, `ModTags`, `ModCreativeTabs`, `ModNetworking`
- **Config:** `ShardlineConfig`, `ShardlineClientConfig`
- **Blöcke und Items:** `ShardPortalBlock` (Random Ticks), `QueenAltarBlock` (Kern des Bruchs), `HeartOfTheExpanseItem` (Katalysator)
- **Strukturen:** Ruinen-Turm und beide Arenen (Erinnerungskristalle)
- **Client:** `ShardlineClient`, `ClientGameEvents`, `KeyBindings`, `ShardParticle`
- **Datagen:** alle Datengeneratoren
- **Tests:** GameTests

Die bestehenden Registry-Namen sind unverändert. Alte Welten laden weiter.

## Tests

`gradlew runGameTestServer` führt 23 Tests aus (Stand Story-Erweiterung 2). Aus der ersten Story-Erweiterung stammen:
- Infektion, Reinheitsanker und Reinigung
- Warteschlange in Chunk-Stapeln und Pause
- Ausbreitung per Random Tick
- SavedData speichern und laden
- vollständige Sprachdateien für alle Story-Texte
- Lore-Buch-Items
- Ende „versiegeln“ stoppt den Virus

Die Strukturtests prüfen zusätzlich die Erinnerungskristalle.

Neu in der Story-Erweiterung 2: `infectedAnimals`, `decisionAltar`, `immunity`, `virusHeartPhases`, `lorePageTrading`, `chapterDisplay`, `purityZone`, `rulerAllegiance`, dazu die Hüter-Ruinen im Strukturtest.

Für den Server-Rauchtest gilt: `run/world` vorher löschen. Eine Welt, die der GameTest-Server angelegt hat, hat eine falsch eingerichtete Splitterweite.

## Story-Erweiterung 2

### Hüter-Ruinen

Die Ruinen eines Versammlungssaals der Hüter stehen in allen Biomen der Splitterweite (`shardline:keeper_ruin`, Abstand 28 Chunks).
- **Bau:** Mauern aus hellem Hüterstein, nach Süden zu eingestürzt, an den Ecken und Seiten Säulen.
- **Innen:** Ein Runenpfad führt zum Podest am Nordende. Dort stehen ein Erinnerungskristall (Aurelis, Der falsche Ton oder meist Der Rat der Hüter) und eine Truhe mit mindestens einem Lore-Buch und mehreren losen Seiten.
- **Neue Blöcke:** `keeper_ruin_bricks`, `keeper_ruin_pillar`, `keeper_rune_slab` (Rune mit Leuchtschicht). Hergestellt werden sie aus Calcit, die Runenstufe zusätzlich mit Glowstone-Staub.
- Advancement „Wo die Hüter tagten“. Der Resonanzkompass kennt die Ruinen als Ziel.

### Neue Advancements

| Tab | Advancement |
|---|---|
| Shardline | Wo die Hüter tagten (Hüter-Ruinen gefunden) |
| Chronik | Am Scheideweg, Unberührt, Eine Stimme kehrt zurück, Kristallherde, Neu gebunden |

### Neue Registry-Namen

- **Wesen:** `crystallized_sheep`, `crystallized_pig`, `crystallized_villager` (+ Spawn-Eier)
- **Blöcke:** `decision_altar`, `keeper_ruin_bricks`, `keeper_ruin_pillar`, `keeper_rune_slab`
- **Items:** `lore_page`, `chapter_emblem`
- **Effekt:** `immunity`
- **Partikel:** `purity_mote`
- **Block-Entity:** `purity_anchor`
- **Struktur:** `keeper_ruin` (Set `keeper_ruins`, Biome-Tag `has_structure/keeper_ruin`)
- **Datenkomponenten:** `lore_page`, `chapter`
- **Netzwerk** (Protokoll 4): `chapter_card`, `trader_dialogue`, `trader_action`, `open_lore_page`

## Nach dem Ausbruch (Kapitel 7–9)

Nach der Endentscheidung geht die Geschichte weiter: Die Weite bebt nach, Flüchtlinge aus der Oberwelt haben in der Splitterweite ein Lager aufgeschlagen, und die Wunde „singt“. Kapitel 7 beginnt, sobald die Welt ein Ende hat (Anmeldung, Dimensionswechsel oder direkt nach der Entscheidung). Ab Kapitel 8 läuft die Geschichte in zwei Zweigen, benannt wie die Enden im Code: `seal` (versiegelt) und `dominate` (beherrscht). Die Endkämpfe ändern das Weltende nie; die Entscheidungen in Kapitel 9 setzen nur den **Epilog** des Spielers.

### Ablauf

| Kapitel | versiegelt (`seal`) | beherrscht (`dominate`) |
|---|---|---|
| 7 Das Echo der Wunde | Lager finden, Rhea treffen (Chronik-Tagebuch), „Holz für das Lager“, „Die erste Schwäre“ mit Kesh → Kapitel 8 | gleich |
| 8 Wiederaufbau / Herrschaft | „Das Anker-Netz“ (Rhea), „Stimmen des Archivs“ (Veyl in der Hüter-Bibliothek) | „Gefolge“ (Rhea), „Schonen oder benutzen“ (Ilsa, Entscheidung über die Stimmlosen) |
| 9 Der letzte Ton | Veyl erweckt den **Nachhall** der Königin; nach dem Sieg Epilog `stillness` oder `chronicle` | Ilsa: neben dem Lager wächst das **Schwärenherz**; nach dem Sieg Epilog `truce` oder `dominion` |

Zweigübergreifend: „Der Gezeichnete“ (Kesh heilen), „Ritter ohne Herrn“, wiederholbar „Echoernte“ und „Nachtwache“. Nach dem Epilog reden alle NPCs anders (Epilog-Zeilen); `story.shardline.next.epilogue` ersetzt das nächste Ziel.

### Figuren

| NPC | Rolle | Ort |
|---|---|---|
| Rhea Tannwald (`rhea`) | Lagerleiterin, frühere Hauptfrau der Wache von Birkenfurt; vergibt die meisten Aufträge, bleibt im beherrschten Ende neutral | Überlebendenlager, am Feuer |
| Ilsa Brandt (`ilsa`) | Schmiedin und Alchemistin, Händlerin (Shard-Splitter, füllt sich täglich auf); im beherrschten Ende Anführerin der Stimmlosen | Lager, Werkstatt |
| Kesh (`kesh`) | der Gezeichnete, hört den Ton und führt zu Schwären; folgt dem Spieler (Begleitschritt), lässt sich heilen (eigene Textur) | Lager, Rand |
| Veyl (`veyl`) | Geist des letzten Hüter-Archivars, an die Bibliothek gebunden, schwebt, schläft nicht, unverwundbar | Hüter-Bibliothek |

Alle NPCs: Rechtsklick öffnet den Dialog, im Lagerumkreis (`aftermath.campRadius`) unverwundbar, außerhalb fliehen sie vor Monstern und brechen bei tödlichem Schaden zusammen statt zu sterben (nur `/kill` tötet). Nachts gehen sie zu ihrem Platz und schlafen (`aftermath.npcSleep`), ansprechbar dann nur schleichend. Animationen: idle, walk, talk, hurt, sleep, death (hält den letzten Frame).

### Strukturen

- **Überlebendenlager** (`shardline:survivor_camp`, Kristallwald und Aschefelder, Abstand 44/16 Chunks): runde Lichtung mit Palisade, Lagerfeuer, drei Zelten mit Betten, Ilsas Werkstatt (Amboss, Braustand), Vorratstruhe (`chests/survivor_camp`); Rhea, Ilsa und Kesh erscheinen mit Heimatpunkt. Keine Monster-Spawns im Lager.
- **Hüter-Bibliothek** (`shardline:keeper_library`, ganze Splitterweite, Abstand 40/14): Saal mit Säulen, Kristallglasfenstern, eingestürztem Runendach, Regalreihen, Lesepulten und Archivtruhe (`chests/keeper_library`, Lore-Bücher, Echosplitter); Veyl schwebt in der Mitte.
- Beide sind Ziele des **Resonanzkompasses** (Schleichen + Rechtsklick wechselt das Ziel). In alten Welten entstehen sie nur in neuen Chunks; zum Testen `/place structure shardline:survivor_camp`.

### Schwären und Nachbeben

- **Nachbeben:** Alle `aftermath.aftershockMinutes` (8) bebt die Weite für Spieler ab Kapitel 7 (Klang, kurzes Wackeln, Meldung). Dabei entsteht 24–48 Blöcke entfernt eine **Schwäre** (`shardline:festering_node`), solange weniger als `aftermath.maxNodesNearPlayer` (3) im Umkreis von 96 Blöcken liegen. Gesetzt wird nur in geladenen Chunks; gezählt wird über eine gespeicherte Liste, nie über eine Weltsuche.
- **Versiegelte Welt:** eine Narbe, die sich nicht ausbreitet. **Beherrschte Welt:** der Ton singt gegen den Herrscher – die Schwäre streut Infektion und ruft Befallene („Gegengesang“), die auch Splitterherrscher angreifen und sich nicht zähmen lassen.
- **Zum Schweigen bringen** mit der **Stimmgabel der Hüter** (64 Benutzungen, Rezept: 2 Shard-Barren, Goldbarren, Stock) oder Reinigungsessenz: gereinigt (Radius 4) oder – als Herrscher in der beherrschten Welt – gebunden. Beides gibt 2–3 **Echosplitter** und zählt für Aufträge. Abbauen ohne Werkzeug: kein Drop, zwei Befallene erscheinen.
- Die Stimmgabel horcht bei Rechtsklick in die Luft nach der nächsten Schwäre (bis 96 Blöcke) und nennt Richtung und Entfernung.

### Endkämpfe

| Gegner | Grundlage | Besonderheiten |
|---|---|---|
| Nachhall (`shardline:echo_queen`) | Königin (Modell, Angriffe, Klone, Leerzonen), durchscheinende Geisttextur | Leben `aftermath.echoQueenHealth` (320); löst weder Ausbruch noch Entscheidungsaltar aus |
| Schwärenherz (`shardline:festering_heart`) | Virusherz (drei Phasen), Eiter-/Rost-Textur | Leben `aftermath.festeringHeartHealth` (320); greift Herrscher an, „Gegengesang“ alle 4 s gegen Herrscher (Schwäche, Langsamkeit, 2 Schaden); zählt nicht für den Kern des Bruchs |

Beide erscheinen über die Dialogaktion `summon_encounter` (nie doppelt im Umkreis von 96 Blöcken; das NPC kann sie erneut rufen, solange der Sieg fehlt). Der Sieg setzt bei allen Spielern im Umkreis von 48 Blöcken die Flagge `boss:echo_queen` bzw. `boss:festering_heart` und das gleichnamige Advancement.

### Aufträge

Dateien: `data/shardline/quests/<id>.json`, Texte `quest.shardline.<id>.title|description|step.<n>`.

| ID | Kapitel / Zweig | Auftraggeber | Schritte |
|---|---|---|---|
| `camp_wood` | 7 | Rhea | 32 Kristallholzbretter, 4 Splitterlaternen, abgeben |
| `first_festering` | 7 | Rhea | Kesh sprechen, Schwäre zum Schweigen bringen, Kesh zu Rhea begleiten, berichten (→ Kapitel 8) |
| `the_marked` | 7 | Kesh | 1 Reinigungsessenz, 3 Echosplitter, Ilsa braut, Kesh trinkt (geheilt) |
| `echo_harvest` (wiederholbar) | 7 | Ilsa | 5 Echosplitter, abgeben |
| `knights_without_lord` | 8 | Rhea | 5 Gesplitterte Ritter besiegen, berichten |
| `night_watch` (wiederholbar) | 8 | Rhea | 8 Angreifer (Tag `shardline:night_watch_targets`), berichten |
| `anchor_net` | 8 `seal` | Rhea | 3 Reinheitsanker setzen, berichten |
| `archive_voices` | 8 `seal` | Veyl | 3 Lore-Seiten/Bücher lesen, 2 Echosplitter, abgeben |
| `following` | 8 `dominate` | Rhea | 3 Befallene zähmen, berichten |
| `spare_or_use` | 8 `dominate` | Ilsa | 3 Echosplitter, Entscheidung (`voiceless:spared` / `voiceless:used`) |

Format:

```json
{
  "chapter_min": 8, "branch": "seal", "giver": "rhea", "repeatable": false,
  "steps": [{"type": "place_anchor", "target": "shardline:purity_anchor", "count": 3}, {"type": "talk", "target": "rhea"}],
  "rewards": [{"item": "shardline:shard_ingot", "count": 3}], "xp": 160, "flags": ["camp:anchors"]
}
```

Schritttypen: `collect` (Inventar, geprüft alle `dialogue.questCheckTicks`), `kill` (Wesen-ID oder `#tag`), `visit_structure`, `talk` (nur über Dialogaktion `complete_quest_step`), `purify_node`, `bind_node`, `place_anchor`, `tame` (`any` oder Wesen), `read_lore` (`any` oder Buch-ID; jede Seite zählt einmal, ein ganzes Buch einmal), `escort` (Kesh bei Rhea), `custom` (`shardline:festering_node`). Abschluss setzt `quest_done:<id>`, Belohnungen und `flags`.

### Dialoge

Dateien: `data/shardline/dialogues/<npc>.json`, geladen bei `/reload` oder `/shardline dialogue reload`; der Server prüft beim Laden jede Datei und meldet Fehler im Log, eine kaputte Datei schaltet nur sich selbst ab.

```json
{
  "speaker": "rhea",
  "start": [{"if": [{"quest_step": {"quest": "camp_wood", "step": 3}}], "node": "wood_turn_in"}, {"node": "idle"}],
  "nodes": {
    "wood_turn_in": {
      "portrait": "neutral",
      "text": ["dialogue.shardline.rhea.wood_turn_in.1"],
      "choices": [
        {"text": "…c1", "next": "wood_thanks", "if": [{"has_item": {"item": "shardline:crystal_planks", "count": 32}}],
         "do": [{"take_item": {"item": "shardline:crystal_planks", "count": 32}}, {"complete_quest_step": "camp_wood"}],
         "show_locked": true, "locked_hint": "…locked1"},
        {"text": "…c2", "next": "END"}
      ]
    }
  }
}
```

- `start`: der erste Eintrag, dessen Bedingungen gelten (ein Eintrag ohne `if` ist Pflicht). Knoten: `portrait` (`neutral`, `happy`, `sad`, `angry`), `speaker` (optional), `text` (Sprachschlüssel, je eine Seite), `do` (Aktionen beim Betreten), `choices` (höchstens 4) oder `next`; `END` beendet.
- **Bedingungen:** `chapter_min`, `chapter_max`, `ending`, `branch`, `title` (`keeper`/`ruler`/`none`), `has_item`, `flag`, `not_flag`, `quest_active`, `quest_done`, `quest_new` (kann angenommen werden), `quest_step` (1-basiert), `epilogue` (`""` = noch keiner), `any`, `not`. Mehrere in einer Liste: alle müssen gelten.
- **Aktionen:** `give_item`, `take_item` (alles oder nichts), `set_flag`, `clear_flag`, `advance_chapter` (nur +1), `start_quest`, `complete_quest_step`, `open_trade`, `play_sound`, `run_command_safe`, `set_epilogue`, `give_xp`, `npc_state` (`follow`, `stay`, `healed`), `summon_encounter` (`festering_node`, `echo_queen`, `festering_heart`).
- `run_command_safe` führt nur Befehle aus, die mit einem Eintrag aus `dialogue.commandWhitelist` beginnen, ohne `/`, ohne Verkettung, ohne `execute` und nur mit dem Ziel `@s`.
- Der Server ist maßgeblich: Jede Antwort wird gegen Sitzung, Abstand (`dialogue.maxDistance`), lebendes NPC und die Bedingungen erneut geprüft; ungültige Antworten schicken den aktuellen Stand zurück. Ein Spieler hat höchstens einen Dialog; Schaden, Tod, Abmelden, Dimensionswechsel oder zu großer Abstand beenden ihn.
- Dialogfenster: Porträt mit Stimmung, Namensschild, Schreibmaschinentext (Client-Config `dialogue.*`), gesperrte Antworten mit Hinweis, Verlauf (L), Tasten Enter/Leertaste, 1–4, Pfeile, Esc; passt sich an GUI-Skalierung und kleine Fenster an.

### Auftragsbuch

Taste **J** (änderbar) oder das **Chronik-Tagebuch** (Rhea gibt es beim ersten Treffen; Rezept: Buch + Shard-Splitter). Reiter **Kapitel** (Titel, Zusammenfassung, nächstes Ziel, Epilog), **Aufträge** (Auftraggeber, Beschreibung, erledigte und aktueller Schritt mit Fortschritt), **Figuren** (Porträt, Beschreibung je nach Lage) und **Lore** (gelesene Seiten). Der Server schickt den Stand (`JournalPayload`) beim Öffnen und bei jeder Änderung; ein offenes Buch aktualisiert sich.

### Virus in der Tiefe, im Nether und im End

- **Tiefe:** Ein Block ohne Luftkontakt wird befallen, wenn ein Nachbar schon befallen ist, die nächste Oberfläche entlang einer Achse höchstens `virus.maxDepth` (6) Blöcke entfernt ist und der Block „porös“ ist (fester Anteil `virus.depthChance` = 0,35, aus der Position berechnet). So wachsen Adern ins Gestein statt voller Würfel. Kosten: höchstens 6 + 6 · maxDepth Blockabfragen pro Versuch, keine Chunk-Ladungen.
- **Stämme:** Shardvirus (Oberwelt, Splitterweite), **Glutbrand** (Nether: Ausbreitung × `virus.emberSpreadMultiplier` 1,25, setzt Lebewesen 2 s in Brand) und **Leerenfäule** (End: × `virus.voidSpreadMultiplier` 0,75, 3 s Dunkelheit). Wirkungen abschaltbar mit `virus.strainEffects`. Erlaubte Dimensionen: `virus.dimensions`.
- **Neue Blöcke:** `infected_deepslate` (Tiefenschiefer, Bruchtiefenschiefer, Tuff), `infected_planks` (alle Bretter, auch in Häusern), `infected_netherrack` (Netherrack, Magmablock; brennt ewig), `infected_blackstone` (Schwarzstein, Basalt), `infected_soul_soil` (Seelenerde, Seelensand), `infected_nylium` (beide Nezel), `infected_wart_block` (beide Warzenblöcke), `infected_end_stone` (Endstein, Endsteinziegel), `infected_purpur` (Purpur, Purpursäule). Tags `shardline:virus_infectable/<art>` per Datapack erweiterbar.
- **Neue Mobs:** Kristallisierter Piglin (aus Piglin, Piglin-Barbar, Zombifiziertem Piglin; feuerfest, Goldschwert) und Kristallisierter Enderman (greift ohne Blickkontakt an, springt zu fernen Zielen, Treffer geben Dunkelheit und Fieber). Beide zähmbar, reinigbar (`virus.netherEndMobsTransform`).
- **Aussaat:** neue Herde nahe Spielern im Nether und End alle `virus.netherEndSeedMinutes` (10) und beim Ausbruch.

### Konfiguration

| Abschnitt | Neue Schlüssel (Standard) |
|---|---|
| `dialogue` | `maxDistance` (8), `commandWhitelist` (`effect give @s `, `particle `, `playsound `), `questCheckTicks` (40) |
| `aftermath` | `campRadius` (32), `npcSleep` (true), `aftershockMinutes` (8), `maxNodesNearPlayer` (3), `echoQueenHealth` (320), `festeringHeartHealth` (320) |
| `virus` | `netherEndSeedMinutes` (10), `maxDepth` (6), `depthChance` (0.35), `surfaceOnly` (false), `dimensions` (Oberwelt, Nether, End, Splitterweite), `emberSpreadMultiplier` (1.25), `voidSpreadMultiplier` (0.75), `strainEffects` (true), `netherEndMobsTransform` (true) |
| Client `dialogue` | `typewriter` (true), `textSpeed` (45), `typeSound` (true), `autoAdvance` (false), `autoAdvanceSeconds` (4) |

### Befehle

| Befehl | Wirkung |
|---|---|
| `/shardline dialogue start <npc> <id>` | Dialog ohne NPC starten (Test) |
| `/shardline dialogue reload` | Dialoge und Aufträge neu laden, Fehler im Log |
| `/shardline quest list [spieler]` | Aufträge mit Schritt und Stand |
| `/shardline quest start\|complete\|reset <id> [spieler]` | Auftrag beginnen, aktuellen Schritt abschließen, zurücksetzen |

### Registry-Namen

- Wesen: `rhea`, `veyl`, `ilsa`, `kesh`, `crystallized_piglin`, `crystallized_enderman`, `echo_queen`, `festering_heart` (alle mit Spawn-Ei)
- Blöcke: `festering_node`, `infected_deepslate`, `infected_planks`, `infected_netherrack`, `infected_blackstone`, `infected_soul_soil`, `infected_nylium`, `infected_wart_block`, `infected_end_stone`, `infected_purpur`
- Items: `chronicle_journal`, `keeper_tuning_fork`, `echo_shard`
- Strukturen: `survivor_camp`, `keeper_library`; Loot: `chests/survivor_camp`, `chests/keeper_library`
- Advancements: `story/chapter_7`, `story/chapter_8_seal|dominate`, `story/chapter_9_seal|dominate`, `story/festering_node`, `story/echo_queen`, `story/festering_heart`, `story/epilogue_stillness|chronicle|truce|dominion`
- Netzwerk (Protokoll 5): `open_dialogue`, `dialogue_choice`, `close_dialogue`, `journal`, `open_journal`; Bildschirmeffekt `AFTERSHOCK`
- Taste: `key.shardline.journal` (J)

### Tests (Batches `shardline_aftermath`, `shardline_virus`)

`dialogueEngine`, `dialogueValidation`, `questFlow`, `aftermathChapters`, `aftermathSaveData`, `dialogueSessions`, `dialoguePayloadCodec`, `storyDataFiles` (alle Dateien ohne Fehler, alle Texte vorhanden), `rheaNpc`, `veylAndIlsa`, `keshEscort`, `virusStrainBlocks`, `virusDepth`, `virusStrainMobs`, `aftermathFlowSeal` (Kapitel 7 → 8 über die echten Dialoge), `aftermathBossesAndEpilogue`, `festeringNodeDominated`, `journalSync`, `aftermathStructures`.
