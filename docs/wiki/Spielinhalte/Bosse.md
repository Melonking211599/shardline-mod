---
tags: [shardline, spielinhalte, bosse]
---

# Bosse

Fünf Bosse: die beiden Hauptbosse **Kristallkoloss** und **Splitterkönigin**, der Mini-Boss **Virusherz** nach dem Bruch und die zwei Endkämpfe aus [[Nach dem Ausbruch]], **Nachhall** und **Schwärenherz**.

| Boss | ID | HP (Standard) | Wo | Auslöser |
|---|---|---|---|---|
| [[#Der Kristallkoloss]] | `crystal_colossus` | 400 | Koloss-Arena | Erweckungskristall auf den Altar |
| [[#Die Splitterkönigin]] | `shard_queen` | 600 | Thron bei 0/0 | Kolosskern auf den Thron |
| [[#Das Virusherz]] | `virus_heart` | 200 | stark infizierte Chunks | spawnt von selbst |
| [[#Der Nachhall]] | `echo_queen` | 320 | Hüter-Bibliothek | Veyl, Kapitel 9 (versiegelt) |
| [[#Das Schwärenherz]] | `festering_heart` | 320 | neben dem Überlebendenlager | Ilsa, Kapitel 9 (beherrscht) |

Koloss und Königin haben eine **Bossleiste**, mehrere Phasen, eine eigene Arena (siehe [[Strukturen]]) und einzigartige Beute. Ihre Lebenspunkte und ihr Schaden lassen sich in der [[Konfiguration#Bosse]] einstellen. Beide sind an ihre Arena gebunden, verschwinden nie von selbst und können keine Portale benutzen. Koloss, Königin, Virusherz, Nachhall und Schwärenherz haben **eigene Bossleisten**, jede mit eigenem Design: Der Koloss hat einen Steinrahmen mit Schild am 50-%-Marker (zerbricht beim Wechsel zur Raserei, die Füllung wird zu Glut), die Königin einen Glasrahmen mit Krone, dessen Risse mit sinkendem Leben zunehmen (Rosa, Violett, Rot), das Virusherz einen Fleischrahmen mit Adern, die im Takt der Fieberwelle aufleuchten, der Nachhall eine durchscheinende Geisterleiste mit Runen und das Schwärenherz einen Eiterrahmen mit Tropfen. Phasenmarken, schimmernde Füllung und Pulsieren unter 25 % Leben bleiben. Wer die Vanilla-Leisten bevorzugt, stellt `customBossBars` in der Client-Config auf `false` ([[Konfiguration]]).

---

## Der Kristallkoloss

`shardline:crystal_colossus`, der Boss der mittleren Spielphase.

### Beschwörung

1. Finde eine **Koloss-Arena** im Kristallwald, etwa mit der Karte des [[Splitterhändler]]s.
2. Rechtsklicke den **Koloss-Altar** mit einem **Erweckungskristall**. Der Kristall wird verbraucht.
3. Der Koloss erscheint 7 Blöcke vom Altar entfernt, auf der dir abgewandten Seite. Begleitet wird das von einem Blitz (ohne Schaden), einem Brüllen und einer Meldung an alle Spieler im Umkreis von 64 Blöcken.
4. **Erwachen:** Etwa 2 Sekunden lang brüllt er und ist unverwundbar.

> [!note] Hinweise am Altar
> - Ein Rechtsklick ohne Kristall zeigt einen Hinweis.
> - Ist bereits ein Koloss in 48 Blöcken Umkreis wach, wird kein zweiter beschworen, und der Kristall bleibt erhalten.

### Werte

| | Standard |
|---|---|
| Lebenspunkte | 400 (`colossusHealth`) |
| Nahkampf | 18 (`colossusAttackDamage`) mit Rückstoß nach oben |
| Rüstung / Härte | 12 / 6 |
| Rückstoßresistenz | 100 % |
| Erfahrung | 200 |
| Größe | 2,2 × 4,4 Blöcke, steigt 1,5 Blöcke hohe Stufen hinauf |

### Phase 1 (100 % bis 50 % HP)

| Angriff | Auslöser | Wirkung | Abklingzeit |
|---|---|---|---|
| **Nahkampf** | Ziel in Reichweite | 18 Schaden | 1,2 s |
| **Bodenstampfer** | Ziel näher als ca. 5,5 Blöcke | Hebt die Arme und stampft: Flächenschaden bis 14 im Radius 6 (nach außen schwächer), Rückstoß und Schleudern nach oben, Schockwellen-Partikel | 8 s |
| **Kristallsäulen** | Ziel 6 bis 24 Blöcke entfernt | 4 bis 14 Säulen brechen nacheinander in einer Welle Richtung Ziel aus dem Boden, jede macht 10 Schaden und schleudert hoch. Vorher kündigen sie sich mit Funken an. | 10 s |

### Phase 2: Raserei (unter 50 % HP)

- **Übergang:** Er brüllt, eine Kristallexplosion folgt, die Bossleiste wird **rot** und heißt nun „Der Kristallkoloss – Raserei“. Alle Spieler in der Bossleiste erhalten eine Meldung.
- **Tempo:** 35 % schneller.
- **Diener:** Er ruft sofort und danach alle 20 Sekunden **3 Splitterkriecher** (`colossusMinionCount`), die direkt das Ziel angreifen.
- **Schnellere Angriffe:**
  - Nahkampf alle 0,8 s.
  - Bodenstampfer alle 4,5 s mit Radius 7.
  - Kristallsäulen alle 6 s, zusätzlich mit einem **Ring aus 6 Säulen** um das Ziel.

### Arena-Bindung und Fairness

- **Bindung:** Er bleibt im Umkreis von 28 Blöcken um den Altar. Weiter als 40 Blöcke entfernt teleportiert er zurück.
- **Rückzug:** Ziele, die sich mehr als 48 Blöcke vom Altar entfernen, gibt er auf.
- **Regeneration:** Hat er 10 Sekunden lang kein Ziel, heilt er 4 HP pro Sekunde.
- **Immunitäten:** Fallschaden sowie Splitter, Explosionen und Säulen, die nicht von Spielern stammen. Seine eigenen Kriecher verletzen ihn also nicht.

### Beute

| Item | Menge |
|---|---|
| Kolosskern | 3–4 |
| Koloss-Schmiedevorlage | 1 |
| Kolossfaust (Spezialitem) | 1 |
| Kolosstrophäe | 1 |
| Shard-Barren | 6–10 |
| Shard-Block | 1–2 |

Advancement: [[Advancements|Kolossbezwinger]] (+200 XP).

---

## Die Splitterkönigin

`shardline:shard_queen`, der Endboss. Sie fliegt.

### Beschwörung

1. Reise ins Zentrum der Splitterweite (X 8 / Z 8) zur **Königinnen-Arena**.
2. Rechtsklicke den **Thron der Splitterweite** (Y 115) mit einem **Kolosskern**. Der Kern wird verbraucht.
3. Die Königin erscheint 8 Blöcke über dem Thron, begleitet von Portalpartikeln, einem Schrei und einer Meldung an alle Spieler im Umkreis von 96 Blöcken.

Ist bereits eine Königin in 96 Blöcken Umkreis, passiert nichts, und der Kern bleibt erhalten.

### Werte

| | Standard |
|---|---|
| Lebenspunkte | 600 (`queenHealth`) |
| Projektilschaden | 8 (`queenProjectileDamage`) |
| Rüstung / Härte | 10 / 4 |
| Rückstoßresistenz | 100 % |
| Erfahrung | 500 |
| Bewegung | schwebt 5–10 Blöcke über dem Ziel, bleibt in der Arena (Radius 26) |

### Phase 1: Projektilhagel (über 66 % HP)

- **Salve:** 5 Kristallsplitter im Fächer auf das Ziel, alle 2,5 s. Nur mit Sichtlinie.
- **Splitterregen:** Über **jedem** Spieler in der Arena fallen 8 Splitter aus 12 bis 16 Blöcken Höhe, jeder mit 75 % des Projektilschadens. Alle 7 s.

### Phase 2: Teleportation und Klone (66 % bis 33 % HP)

- **Übergang:** Sie schreit, die Bossleiste wird **lila**, und es erscheint die Meldung „Die Königin zersplittert in Spiegelbilder!“
- **Teleport:** alle 5 s an einen freien Punkt in der Arena, 6 bis 12 Blöcke über dem Boden.
- **Klone:** sofort und danach alle 15 s **3 Spiegelbilder** (`queenCloneCount`), höchstens 6 gleichzeitig.
  - **Solange Klone leben, erleidet die Königin nur 50 % Schaden.**
  - Klone haben 20 HP, **zerspringen beim ersten Spielertreffer**, schießen alle 2,5 bis 4 s einen Splitter mit 3 Schaden und lösen sich nach 20 Sekunden auf. Sie lassen keine Beute fallen.
- **Salven:** weiterhin, alle 3 s.

### Phase 3: Void-Zonen (unter 33 % HP)

- **Übergang:** Die Bossleiste wird **rot**, Meldung „Die Leere öffnet sich unter euren Füßen!“, statt Funken fallen dunkle Void-Partikel.
- **Void-Zonen:** sofort und danach alle 8 s öffnet sich unter **jedem** Spieler eine Zone, höchstens 6 gleichzeitig.
  - Eine Zone wächst in 3 Sekunden von 1,5 auf 3,5 Blöcke Radius und bleibt 12 Sekunden.
  - Sie zieht Wesen zur Mitte, macht jede Sekunde 4 Schaden (`queenVoidZoneDamage`) **durch die Rüstung hindurch** und verursacht 3 s Verdorrung.
- **Salven:** 7 Splitter alle 1,8 s, Splitterregen alle 10 s, Teleport alle 8 s.

### Arena-Bindung und Fairness

- **Bindung:** Weiter als 44 Blöcke vom Thron entfernt teleportiert sie zurück.
- **Zurücksetzen:** Hat sie **30 Sekunden kein Ziel**, heilt sie vollständig, kehrt zu Phase 1 zurück und entfernt alle Klone und Void-Zonen.
- **Immunitäten:** Fall- und Ertrinkungsschaden sowie Splitter, Explosionen und Void-Zonen, die nicht von Spielern stammen.
- **Bei ihrem Tod:** Alle Klone und Void-Zonen verschwinden.

### Beute

| Item | Menge |
|---|---|
| Herz der Splitterweite (Spezialitem) | 1 |
| Splitterklinge der Königin (Waffe) | 1 |
| Königinnentrophäe | 1 |
| Shard-Block | 3–5 |
| Kolosskern | 1–2 |

Advancement: [[Advancements|Herz der Splitterweite]] (Herausforderung, +1000 XP).

> [!warning] Der Bruch
> Der Tod der Königin setzt den **Shardvirus** frei (solange `virus.enabled` aktiv ist und die Welt nicht schon versiegelt wurde): Der Himmel färbt sich violett, rund um den Todesort beginnt die Infektion, und vor dem Thron wächst der **Entscheidungsaltar**. Alles Weitere steht in [[Story und Shardvirus#Der Bruch]].

> [!tip] Taktik
> - **Phase 2:** Zuerst die Klone zerschlagen, ein Treffer genügt. Sonst halbiert sich der Schaden an der Königin.
> - **Phase 3:** In Bewegung bleiben. Die Zonen öffnen sich dort, wo du gerade stehst.
> - **Ausrüstung:** Das volle Shard-Set halbiert den Splitterschaden. Der Kristallschild des Koloss-Sets fängt Salven ab.

---

## Das Virusherz

`shardline:virus_heart`, ein unbewegliches Mini-Boss-Herz des Shardvirus. Drei besiegte Herzen ergeben den **Kern des Bruchs** für die Endentscheidung.

### Entstehen

Ein Herz entsteht in Chunks mit mindestens 160 infizierten Blöcken (`heartSpawnThreshold`), aber nur, wenn ein Spieler innerhalb von 64 Blöcken ist, im Umkreis von 96 Blöcken kein anderes Herz existiert und seit dem letzten Herz 8 Minuten vergangen sind (`heartSpawnCooldownMinutes`).

### Werte

| | Standard |
|---|---|
| Lebenspunkte | 200 (`remedies.virusHeartHealth`) |
| Rüstung | 8 |
| Erfahrung | 120 |
| Schwierigkeit | `remedies.virusHeartDifficulty` (EASY / NORMAL / HARD) |

### Phasen (bei 2/3 und 1/3 Leben)

1. **Splittersalven** und **Fieberwellen** (Radius 8, geben Splitterfieber).
2. Zusätzlich **Tentakelschläge**: Ein Sporenring markiert eine Sekunde vorher die Einschlagstelle.
3. Zusätzlich **Beschwörung** von Shard-Zombies und kristallisierten Tieren (Sporenwirbel zeigen die Stellen), schnellere Wellen.

Beim Phasenwechsel verhärtet sich das Herz kurz und ist unverwundbar; die Leiste färbt sich pink bzw. rot.

### Nach dem Sieg

- Reinigt einen Radius von 40 Blöcken (`heartPurifyRadius`).
- Jeder Spieler im Umkreis von 32 Blöcken zählt ein Herz; nach **drei Herzen** (`heartsForCore`) erhält er den **Kern des Bruchs**.
- Advancements „Herzstillstand“ und „Kern des Bruchs“.

---

## Der Nachhall

`shardline:echo_queen`, Endkampf des **versiegelten** Zweigs in Kapitel 9. Veyl erweckt in der Hüter-Bibliothek den Nachhall der Königin.

- **Grundlage:** Splitterkönigin (Modell, Angriffe, Klone, Void-Zonen) mit durchscheinender Geisttextur.
- **Leben:** 320 (`aftermath.echoQueenHealth`), 250 XP.
- **Unterschied zur Königin:** Löst weder den Ausbruch noch den Entscheidungsaltar aus und ändert das Weltende nicht.
- **Sieg:** Alle Spieler im Umkreis von 48 Blöcken erhalten die Flagge `boss:echo_queen` und das Advancement „Der letzte Nachhall“. Danach wählt man bei Veyl den Epilog **Stille** oder **Chronik**.

## Das Schwärenherz

`shardline:festering_heart`, Endkampf des **beherrschten** Zweigs in Kapitel 9. Auf Ilsas Ruf wächst neben dem Überlebendenlager ein Herz, das gegen den Herrscher singt.

- **Grundlage:** Virusherz (drei Phasen) mit Eiter- und Rost-Textur.
- **Leben:** 320 (`aftermath.festeringHeartHealth`).
- **Gegengesang:** Greift auch Splitterherrscher an und belegt sie alle 4 Sekunden mit Schwäche, Langsamkeit und 2 Schaden.
- Zählt **nicht** für den Kern des Bruchs.
- **Sieg:** Flagge `boss:festering_heart` und Advancement „Gegengesang“ für alle im Umkreis von 48 Blöcken. Danach wählt man bei Ilsa den Epilog **Waffenstillstand** oder **Alleinherrschaft**.

> [!note] Beschwörung über Dialoge
> Nachhall und Schwärenherz erscheinen über die Dialogaktion `summon_encounter`, nie doppelt im Umkreis von 96 Blöcken. Das NPC kann sie erneut rufen, solange der Sieg fehlt.
