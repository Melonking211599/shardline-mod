---
tags: [shardline, spielinhalte, ausrüstung]
---

# Ausrüstung

Zwei Rüstungssets mit Werkzeugen, ein Bogen und drei Spezialitems von den Bossen. Die Rezepte stehen in [[Rezepte]].

## Vergleich mit Vanilla

| Rüstung | Helm | Brust | Hose | Stiefel | Summe | Härte | Rückstoß |
|---|---|---|---|---|---|---|---|
| Eisen | 2 | 6 | 5 | 2 | 15 | 0 | – |
| **Shard** | 3 | 7 | 5 | 2 | **17** | 1 | – |
| Diamant | 3 | 8 | 6 | 3 | 20 | 2 | – |
| Netherit | 3 | 8 | 6 | 3 | 20 | 3 | 10 % je Teil |
| **Koloss** | 4 | 9 | 7 | 4 | **24** | 4 | 15 % je Teil |

| Werkzeugstufe | Haltbarkeit | Abbautempo | Schadensbonus | Verzauberbarkeit | Abbaustufe |
|---|---|---|---|---|---|
| Eisen | 250 | 6 | 2 | 14 | Eisen |
| **Shard** | 900 | 7 | 2,5 | 16 | Eisen |
| Diamant | 1561 | 8 | 3 | 10 | Diamant |
| Netherit | 2031 | 9 | 4 | 15 | Netherit |
| **Koloss** | 2800 | 10 | 5 | 18 | Netherit |

---

## Shard-Set

Es wird aus **Shard-Barren** hergestellt und liegt zwischen Eisen und Diamant. Reparieren lässt es sich ebenfalls mit Shard-Barren.

| Teil | Rüstung | Haltbarkeit |
|---|---|---|
| Shard-Helm | 3 | 264 |
| Shard-Brustplatte | 7 | 384 |
| Shard-Hose | 5 | 360 |
| Shard-Stiefel | 2 | 312 |

**Werkzeuge:**
- Shard-Schwert: 6,5 Schaden, 1,6 Angriffe pro Sekunde
- Shard-Spitzhacke: 4,5 Schaden
- Shard-Axt: 9,5 Schaden, 0,95 Angriffe pro Sekunde
- Shard-Schaufel: 5 Schaden

> [!success] Set-Bonus (alle 4 Teile)
> - **Nachtsicht** in der [[Splitterweite]], dauerhaft, solange das Set getragen wird.
> - **−50 % Schaden durch Splitter-Projektile**: Shard-Späher, Splitterkriecher, Splitterkönigin, Klone (`shardProjectileResistance`).

## Koloss-Set

Es entsteht durch **Aufwertung von Netheritausrüstung** am Schmiedetisch: Koloss-Schmiedevorlage + Netherit-Teil + **Kolosskern**. Das Set ist feuerfest wie Netherit, lässt sich verzaubern (Wert 18) und mit Kolosskernen reparieren.

| Teil | Rüstung | Haltbarkeit |
|---|---|---|
| Kolosshelm | 4 | 495 |
| Kolossbrustplatte | 9 | 720 |
| Kolosshose | 7 | 675 |
| Kolossstiefel | 4 | 585 |

**Werkzeuge:**
- Kolossschwert: 9 Schaden, 1,6 Angriffe pro Sekunde
- Kolossspitzhacke: 7 Schaden
- Kolossaxt: 11 Schaden, 1,0 Angriffe pro Sekunde
- Kolossschaufel: 7,5 Schaden

> [!success] Set-Bonus (alle 4 Teile)
> - **Rückstoßimmunität** (100 %)
> - **+4 Herzen** maximale Gesundheit (`colossusBonusHearts`)
> - **Kristallschild** per Tastendruck, siehe unten
>
> Die Boni gelten nur, solange alle vier Teile getragen werden. Legt man ein Teil ab, sinkt die Gesundheit sofort wieder auf den Normalwert.

### Kristallschild

| | Standard |
|---|---|
| Taste | **V** (änderbar unter Steuerung → Shardline) |
| Wirkung | **8 Absorptionsherzen** für **6 Sekunden** |
| Rückstoßwelle | Gegner im Umkreis von 4 Blöcken werden weggestoßen, eigene Haustiere nicht |
| Abklingzeit | 60 Sekunden; bleibt nach Tod und Neuanmeldung erhalten |
| Effekte | Kristallfunken rund um den Spieler, Kristallklang, Meldung in der Aktionsleiste |

- **Abklingzeit:** Drückt man die Taste zu früh, nennt die Aktionsleiste die Restzeit in Sekunden.
- **Ohne volles Set:** Es erscheint ein Hinweis, und es passiert nichts.
- **Server:** Die Taste schickt nur eine Anfrage an den Server. Der Server prüft Set und Abklingzeit selbst, Manipulation über den Client ist damit ausgeschlossen.

Alle Werte stehen in der [[Konfiguration#Ausrüstung]].

---

## Kristallbogen

| | |
|---|---|
| Rezept | Bogen + 2 Shard-Barren + Späherauge + Void-Rochenhaut (formlos) |
| Haltbarkeit | 640, Reparatur mit Shard-Barren |
| Wirkung | Pfeile machen **+1,5 Grundschaden**; normale Pfeile und Trankpfeile lassen das Ziel **5 Sekunden leuchten** |
| Verzauberungen | wie ein Bogen |

## Spezialitems der Bosse

### Kolossfaust (Kristallkoloss)

Ein Schlagring, der als Waffe und als Fähigkeit funktioniert.

- **Als Waffe:** 10 Angriffsschaden, 1,0 Angriffe pro Sekunde, +20 % Rückstoßresistenz in der Hand.
- **Rechtsklick (Bodenstampfer):**
  - Alle Gegner im Umkreis von 5 Blöcken erleiden bis zu **10 Schaden**, nach außen schwächer.
  - Sie werden weggestoßen und in die Luft geschleudert.
  - Eigene Haustiere und Verbündete bleiben verschont.
- **Abklingzeit:** 10 Sekunden. Jede Nutzung kostet 1 Haltbarkeit (insgesamt 500).
- **Reparatur:** mit Kolosskernen. Feuerfest.

### Herz der Splitterweite (Splitterkönigin)

- **Rechtsklick (Void-Sprung):** Teleportiert dich **bis zu 12 Blöcke** in Blickrichtung. Vor Wänden stoppt der Sprung und weicht bei Bedarf bis zu 3 Blöcke nach oben aus.
- **Schutz:** Danach 3 Sekunden Sanfter Fall, der Fallschaden wird zurückgesetzt.
- **Abklingzeit:** 8 Sekunden. Es gibt keine Haltbarkeit, das Herz ist nicht stapelbar und feuerfest.

### Splitterklinge der Königin (Splitterkönigin)

| | |
|---|---|
| Schaden | 11, 1,8 Angriffe pro Sekunde |
| Haltbarkeit | 3600 |
| Treffer | Ziel leuchtet 3 Sekunden und erhält 2 Sekunden Langsamkeit II |
| Rechtsklick | **Splittersalve**: 5 Kristallsplitter im Fächer, je 6 Schaden, kostet 2 Haltbarkeit, 3 Sekunden Abklingzeit |
| Reparatur | mit dem Herz der Splitterweite |

Die Salve trifft weder dich noch deine Haustiere.

---

## Weitere Fortschritts-Items

| Item | Zweck |
|---|---|
| Shard-Schlüssel | öffnet das [[Splitterportal]], 8 Ladungen |
| Erweckungskristall | beschwört den Kristallkoloss am Koloss-Altar |
| Kern des Bruchs | aus drei Virusherzen; Endentscheidung am Entscheidungsaltar |
| Stimmgabel der Hüter | bringt Schwären zum Schweigen (siehe [[Nach dem Ausbruch]]) |
| Resonanzkompass | findet Bauwerke (siehe [[Blöcke und Items#Story- und Spezialitems]]) |
| Kolosskern | Material für das Koloss-Set und Beschwörung der Splitterkönigin |
| Koloss-Schmiedevorlage | Aufwertung Netherit → Koloss; vervielfältigen: 7 Diamanten + Vorlage + Shard-Block ergeben 2 Vorlagen |
| Leuchtbeere | Nahrung (3 Hungerpunkte, Sättigung 0,4, jederzeit essbar) mit **15 Sekunden Nachtsicht**; zähmt Void-Rochen |

## Verzauberungen

Erhältlich am Zaubertisch, im Handel und als Beute.

| Verzauberung | Stufen | Für | Wirkung | Schließt aus |
|---|---|---|---|---|
| **Splitterbann** | I–V | Schwert, Axt | +2,5 Schaden je Stufe gegen Splitterwesen, Bosse und Infizierte | Schärfe, Bann, Nemesis der Gliederfüßer |
| **Kristallwehr** | I–IV | Rüstung | Schutz gegen Splitterschaden: Shard-Geschosse, Void-Zonen, Kristallsäulen, Splitterfieber, Viruspulse | andere Schutz-Verzauberungen |

## Belohnungen der Endentscheidung

| Weg | Belohnung |
|---|---|
| Versiegeln | Titel **[Hüter]**, **Umhang der Hüter** (eigener Render-Layer, für alle sichtbar) |
| Beherrschen | Titel **[Splitterherrscher]**, Immunität gegen Fieber, Befallene zähmen, **Splitterruf** (Taste G), **Kristallwelle** (Taste H), Virus-Lauf und Gefolgschaft (passiv) |

Details: [[Story und Shardvirus#Endentscheidung]].
