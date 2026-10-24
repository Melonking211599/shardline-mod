---
tags: [shardline, spielinhalte, mobs]
---

# Mobs

Elf Wesen der Splitterweite, gemischt aus friedlichen, neutralen und feindlichen, dazu sieben [[#Infizierte Wesen|infizierte Wesen]] des Shardvirus und vier [[#NPCs nach dem Ausbruch|NPCs]]. Die Bosse stehen in [[Bosse]]. Alle sind mit GeckoLib animiert und haben ein eigenes Spawn-Ei im Kreativ-Tab „Shardline“. Schadenswerte gelten für die Standard-[[Konfiguration]] (`mobDamageMultiplier` = 1,0). HP und Schaden zählen in Lebenspunkten, 2 Punkte sind 1 Herz.

## Übersicht

| Mob | Verhalten | HP | Schaden | Oberwelt |
|---|---|---|---|---|
| [[#Splitterkriecher]] | feindlich | 20 | Explosion + 10 × 4 | ja, nachts |
| [[#Kristallgolem]] | neutral | 60 | 10 | nein |
| [[#Leuchtmotte]] | friedlich, zähmbar | 6 | – | ja, nachts |
| [[#Aschewandler]] | feindlich | 26 | 5 + Blindheit | ja, nachts |
| [[#Shard-Späher]] | feindlich, fliegend | 18 | 5 (Projektil) | nein |
| [[#Void-Rochen]] | friedlich, reitbar | 30 | – | nein |
| [[#Kristallspinne]] | feindlich | 20 | 4 + Netz | ja, nachts |
| [[#Splitterhändler]] | NPC-Händler | 24 | – | nein |
| [[#Schattenfuchs]] | neutral | 16 | 4 (Hinterhalt 6) | ja, nachts |
| [[#Gesplitterter Ritter]] | Elite, feindlich | 80 | 12 (Ansturm 15) | nein |
| [[#Kristallschnecke]] | friedlich | 10 | – | nein |

---

## Splitterkriecher

`shardline:shard_creeper`: ein Creeper aus Kristall, der in Splitter zerspringt.

- **Verhalten:** Er schleicht sich an. Ab 3 Blöcken Abstand und mit Sichtkontakt lädt er sich 1,5 Sekunden lang auf: Er bläht sich auf, blinkt weiß, und seine Kristalle wachsen. Entfernt sich das Ziel auf mehr als 7 Blöcke, bricht er ab.
- **Explosion:** Stärke 1,5, zerstört standardmäßig **keine** Blöcke (`creeperBreaksBlocks`). Dazu schleudert er **10 Kristallsplitter** fächerförmig nach oben und außen, jeder macht 4 Schaden vom Typ Splitter.
- **Werte:** 20 HP, Geschwindigkeit 0,25, kein Nahkampf, 6 XP
- **Drops:** 1–3 Shard-Splitter, 0–2 Schwarzpulver (+ Plünderung)
- **Sonstiges:** In Phase 2 ruft der [[Bosse#Der Kristallkoloss|Kristallkoloss]] ihn als Diener herbei.

## Kristallgolem

`shardline:crystal_golem`: der Wächter der Kristallwälder.

- **Verhalten:** Er ist neutral. Er wird wütend, wenn man ihn angreift **oder in seiner Nähe Bergbau betreibt**.
  - Auslöser sind Splitterstein, Shard-Erz, Leuchtkristall, Kristallcluster und Splitterstein-Ziegel, abgebaut im Umkreis von 12 Blöcken und in seiner Sichtlinie.
  - Wütend färbt er sich rot (eigene Textur) und schreit. Die Wut hält 20–39 Sekunden an.
- **Angriff:** Sein Schlag schleudert das Ziel in die Luft.
- **Werte:** 60 HP, 10 Schaden, Rüstung 6, Rückstoßresistenz 0,8, Geschwindigkeit 0,22, 12 XP
- **Drops:** 2–5 Shard-Splitter, 1–3 Splitterstein, 0–1 Roher Shard
- **Sonstiges:** Er verschwindet nie von selbst. Spieler im Kreativ- oder Zuschauermodus provozieren ihn nicht.

## Leuchtmotte

`shardline:glow_moth`: eine lebende Lichtquelle.

- **Verhalten:** Sie fliegt friedlich umher, landet gelegentlich und flieht, wenn sie verletzt wird.
- **Licht:** Alle 5 Ticks setzt sie an ihrer Position einen unsichtbaren Lichtblock (Lichtstufe 12). Der Block entfernt sich selbst, sobald keine Motte mehr in der Nähe ist.
- **Zähmen:** mit einer **Leuchtblume**, Chance 1/3 pro Blume. Gezähmt folgt sie ihrem Besitzer, ein Rechtsklick lässt sie sitzen oder aufstehen. Mit Leuchtblumen lässt sie sich auch züchten.
- **Werte:** 6 HP, Fluggeschwindigkeit 0,6, kein Fallschaden
- **Drops:** 0–1 Glowstonestaub
- **Sonstiges:** Gezähmte Motten verschwinden nie. Ihr Körper leuchtet über eine Leuchtmaske.

## Aschewandler

`shardline:ash_walker`: wandelnde Asche aus den Aschefeldern.

- **Verhalten:** ein Nahkämpfer im Zombie-Stil. Er greift Spieler und Eisengolems an und zieht eine Spur weißer Asche hinter sich her.
- **Besonderheit:** Jeder Treffer verursacht **3 Sekunden Blindheit** (`ashWalkerBlindnessSeconds`). Er ist immun gegen Feuer.
- **Werte:** 26 HP, 5 Schaden, Rüstung 2, Geschwindigkeit 0,26, 7 XP
- **Drops:** 1–3 Aschestaub, 0–1 Knochen; 5 % Roher Shard, nur bei Tötung durch einen Spieler (+2 % je Plünderungsstufe)

## Shard-Späher

`shardline:shard_scout`: ein schwebender Kristall mit Auge.

- **Verhalten:** Er fliegt und hält Abstand. Alle 1,5 bis 2,5 Sekunden schießt er ein **Splitter-Projektil** auf Ziele bis 18 Blöcke Entfernung. Um ihn kreisen vier Kristallsplitter.
- **Werte:** 18 HP, Projektilschaden 5 (`scoutProjectileDamage`), kein Fallschaden, 8 XP
- **Drops:** 1–2 Shard-Splitter; 33 % **Späherauge**, nur bei Tötung durch einen Spieler (+5 % je Plünderungsstufe)
- **Sonstiges:** Das Späherauge braucht man für den [[Ausrüstung#Kristallbogen|Kristallbogen]]. Das volle Shard-Set halbiert den Projektilschaden.

## Void-Rochen

`shardline:void_ray`: ein Mantarochen, der über dem Void gleitet.

- **Verhalten:** Er gleitet in weiten Bahnen von 16 bis 40 Blöcken zwischen Y 50 und 150, oft über das offene Void. Er ist friedlich und folgt Spielern mit Leuchtbeeren.
- **Zähmen:** mit einer **Leuchtbeere**, Chance 1/4. Mit Leuchtbeeren lässt er sich auch züchten.
- **Reiten:**
  1. Den gezähmten Rochen mit einem Sattel ausrüsten (Rechtsklick mit Sattel). Der Sattel ist dann am Modell sichtbar.
  2. Rechtsklick ohne Schleichen setzt dich auf.
  3. Gesteuert wird mit **W**, die Blickrichtung lenkt auch nach oben und unten. Höchstgeschwindigkeit etwa 11 Blöcke pro Sekunde.
- **Werte:** 30 HP, Fluggeschwindigkeit 0,5, kein Fallschaden
- **Drops:** 1–2 Void-Rochenhaut, 0–1 Phantommembran; ein gesattelter Rochen lässt außerdem den Sattel fallen
- **Sonstiges:** Gezähmte Rochen verschwinden nie.

## Kristallspinne

`shardline:crystal_spider`: eine Spinne mit Kristallpanzer.

- **Verhalten:** wie eine Spinne. Sie klettert Wände hoch, springt an und greift bei hellem Licht nicht von selbst an.
- **Kristallnetz:** Bei Treffern setzt sie mit 30 % Chance ein **Kristallnetz** an die Füße des Ziels (`spiderWebChance`). Es verlangsamt sehr stark (Langsamkeit II) und zerfällt nach 8 Sekunden. Zusätzlich bekommt das Ziel 3 Sekunden Langsamkeit II. Ihre eigenen Netze bremsen die Spinne nicht.
- **Werte:** 20 HP, 4 Schaden, Geschwindigkeit 0,3, 6 XP
- **Drops:** 0–2 Faden, 0–2 Shard-Splitter; 33 % Spinnenauge, nur bei Tötung durch einen Spieler

## Splitterhändler

`shardline:shard_trader`: ein reisender Händler mit kristallbeladenem Rucksack.

- **Verhalten:** Rechtsklick öffnet das normale Handelsfenster. Bezahlt wird mit **Shard-Splittern**. Er meidet Kriecher, Aschewandler, Kristallspinnen und Ritter und flieht, wenn er verletzt wird.
- **Werte:** 24 HP, verschwindet nie von selbst
- **Drops:** 2–4 Shard-Splitter
- **Fundorte:** selten im Kristallwald und in jedem vierten [[Strukturen#Ruinen-Turm|Ruinen-Turm]]
- **Handelsangebote:** siehe [[Splitterhändler]]

## Schattenfuchs

`shardline:shadow_fox`: ein schwarzer Fuchs, der mit den Schatten verschmilzt.

- **Verhalten:** Er ist neutral und greift nur an, wenn er provoziert wird. Wird ein Fuchs angegriffen, alarmiert er die Füchse in der Nähe. Er jagt Hühner und Kaninchen.
- **Verschwinden:** Sobald er ein Ziel hat, wird er **6 Sekunden unsichtbar** und 30 % schneller (Abklingzeit 20 Sekunden). Im Dunkeln verschwindet er manchmal auch ohne Ziel für 4 Sekunden.
- **Schattenbiss:** Der erste Biss aus der Unsichtbarkeit macht **50 % mehr Schaden** und macht ihn wieder sichtbar.
- **Werte:** 16 HP, 4 Schaden, Geschwindigkeit 0,33, 5 XP
- **Drops:** 50 % Schattenfell, nur bei Tötung durch einen Spieler (+10 % je Plünderungsstufe)

## Gesplitterter Ritter

`shardline:shattered_knight`: ein Elite-Wächter der Ruinen.

- **Verhalten:** ein Nahkämpfer mit Kristallschwert und schwerer Panzerung.
- **Ansturm:** Steht das Ziel 5 bis 12 Blöcke entfernt und ist in Sicht, holt er 0,6 Sekunden lang aus und stürmt dann los. Ein Treffer macht 125 % Schaden mit starkem Rückstoß. Abklingzeit 7 Sekunden.
- **Wache:** Ritter, die mit einem Turm erzeugt werden, bleiben dauerhaft und kehren in 16 Blöcken Umkreis zu ihrem Turm zurück.
- **Werte:** 80 HP, 12 Schaden, Rüstung 10, Rüstungshärte 4, Rückstoßresistenz 0,6, Geschwindigkeit 0,28, 25 XP
- **Drops:** **Ritterwappen (100 %)**, 1–2 Shard-Barren; 8 % Shard-Schwert, nur bei Tötung durch einen Spieler

## Kristallschnecke

`shardline:crystal_snail`: ein langsames, friedliches Wesen mit Kristallhaus.

- **Verhalten:** Sie kriecht durch Kristallwald und Leuchtende Höhlen und folgt Spielern mit **Leuchtbeeren**.
- **Kristallschleim:** Alle 5 bis 10 Minuten hinterlässt sie einen **Kristallschleim**, Zutat für Kristallglas, Kristallbonbons und den Resonanzkompass (siehe [[Rezepte#Splitterhandwerk]]).
- **Rückzug:** Bei einem Treffer zieht sie sich 5 Sekunden in ihr Haus zurück und nimmt dann nur ein Fünftel des Schadens.
- **Werte:** 10 HP, Rüstung 4, Geschwindigkeit 0,12, 2 XP
- Advancement „Langsam, aber glänzend“.

---

## Infizierte Wesen

Wesen, die der Shardvirus kristallisiert hat. Wie sie entstehen, anstecken und gereinigt werden, steht in [[Story und Shardvirus#Infizierte Mobs]]. Splitterherrscher zähmen sie mit einem Shard-Splitter; gezähmte tragen ein magentafarbenes Halsband.

| Wesen | ID | HP | Schaden | Rüstung | Entsteht aus | Besonderheit |
|---|---|---|---|---|---|---|
| Shard-Zombie | `shard_zombie` | 26 | 4 | 4 | Zombie, Wüstenzombie, von ihm getötete Dorfbewohner | brennt nicht, steckt mit Splitterfieber an, setzt Infektionsstellen |
| Kristallisierte Kuh | `crystallized_cow` | 14 | – | 4 | Kuh | doppelt so viele Infektionsstellen, keine Milch, 30 % Fieber bei Nahkampftreffer |
| Kristallisiertes Schaf | `crystallized_sheep` | 12 | – | 3 | Schaf | Vlies aus Viruskristallen: Scheren gibt Amethyst, manchmal Shard-Splitter |
| Kristallisiertes Schwein | `crystallized_pig` | 12 | – | 3 | Schwein | frisst Amethyst, lässt sich satteln |
| Kristallisierter Dorfbewohner | `crystallized_villager` | 20 | – | 4 | Dorfbewohner | murmelt Bruchstücke des falschen Tons; behält Beruf und Angebote für die Heilung |
| Kristallisierter Piglin | `crystallized_piglin` | 24 | 5 | 2 | Piglin, Piglin-Barbar, Zombifizierter Piglin | Nether, feuerfest, Goldschwert |
| Kristallisierter Enderman | `crystallized_enderman` | 44 | 6 | – | Enderman | End, greift ohne Blickkontakt an, springt zu fernen Zielen, Treffer geben Dunkelheit und Fieber |

Befallene, die eine **Schwäre** in der beherrschten Welt ruft („Gegengesang“), greifen auch Splitterherrscher an und lassen sich nicht zähmen (siehe [[Nach dem Ausbruch#Schwären und Nachbeben]]).

---

## NPCs nach dem Ausbruch

Vier Figuren für Kapitel 7–9 (Details in [[Nach dem Ausbruch#Figuren]]). Alle haben 30 HP, sprechen über das Dialogfenster (Rechtsklick), sind im Lagerumkreis unverwundbar, fliehen außerhalb vor Monstern und brechen bei tödlichem Schaden zusammen statt zu sterben (nur `/kill` tötet). Nachts schlafen sie an ihrem Platz.

| NPC | ID | Rolle | Ort |
|---|---|---|---|
| Rhea Tannwald | `rhea` | Lagerleiterin, vergibt die meisten Aufträge | Überlebendenlager, am Feuer |
| Ilsa Brandt | `ilsa` | Schmiedin, Alchemistin, Händlerin; im beherrschten Ende Anführerin der Stimmlosen | Lager, Werkstatt |
| Kesh | `kesh` | der Gezeichnete, hört den Ton, führt zu Schwären, folgt dem Spieler, lässt sich heilen | Lager, Rand |
| Veyl | `veyl` | Geist des letzten Hüter-Archivars; schwebt, schläft nicht, unverwundbar | Hüter-Bibliothek |

---

## Spawn-Übersicht

Spalten: Gewicht und Gruppengröße je Biom. Die Kategorie bestimmt die Spawnregeln von Minecraft: Monster spawnen laufend, Kreaturen vor allem bei der Generierung, Ambient wie Fledermäuse.

| Mob | Kategorie | Kristallwald | Aschefelder | Leuchtende Höhlen | Oberwelt (nachts) |
|---|---|---|---|---|---|
| Splitterkriecher | Monster | 40 (1–2) | 30 (1–2) | 20 (1–2) | 8 (1) |
| Kristallspinne | Monster | 20 (1–2) | – | 40 (1–2) | 6 (1) |
| Schattenfuchs | Monster | 15 (1–2) | 20 (1–2) | – | 6 (1) |
| Shard-Späher | Monster | 15 (1) | 15 (1) | 15 (1) | – |
| Aschewandler | Monster | – | 50 (1–3) | 10 (1–2) | 8 (1–2) |
| Gesplitterter Ritter | Monster | 3 (1) | 3 (1) | – | – |
| Kristallgolem | Kreatur | 6 (1) | – | 8 (1) | – |
| Void-Rochen | Kreatur | 4 (1–2) | 4 (1–2) | 3 (1) | – |
| Splitterhändler | Kreatur | 1 (1) | – | – | – |
| Leuchtmotte | Ambient | 12 (2–3) | 4 (1–2) | 20 (2–4) | 6 (1–2) |
| Kristallschnecke | Kreatur | 10 (1–2) | – | 14 (1–3) | – |

**Regeln:**
- **Splitterweite:** Feindliche Mobs spawnen **unabhängig vom Licht**, nur nicht auf der Schwierigkeit Friedlich. Bodenmobs brauchen einen festen Block, aber kein Laub. Flieger brauchen zwei Blöcke Luft.
- **Oberwelt:** nur nachts, nur bei ausreichender Dunkelheit wie bei normalen Monstern, nie auf Pilzinseln.
- **Spawnchance:** Jeder natürliche Spawnversuch muss zusätzlich die Spawnchance des Mobs bestehen (`spawnChances`). In der Oberwelt gilt außerdem der Faktor `overworldSpawnChance` (0,35). Spawner und Spawn-Eier ignorieren beides.
- **Ruinen-Türme:** Innerhalb eines Turms spawnen nur Gesplitterte Ritter.
- **Überlebendenlager:** Im Lager spawnen keine Monster.
- **Infizierte Wesen und NPCs** spawnen nicht natürlich: Infizierte entstehen durch Verwandlung, Virusherzen und Schwären, NPCs mit ihren Strukturen. Alle haben Spawn-Eier.

Alle Werte lassen sich in der [[Konfiguration#Spawns]] anpassen.
