---
tags: [shardline, spielinhalte, blöcke, items]
---

# Blöcke und Items

Alle Blöcke und Items stehen im Kreativ-Tab **„Shardline“**: zuerst die Blöcke, dann die Items, zuletzt die Spawn-Eier. Ausrüstung steht in [[Ausrüstung]], Rezepte in [[Rezepte]].

## Gestein und Erze

| Block | Härte | Werkzeug | Licht | Drop / Besonderheit |
|---|---|---|---|---|
| Splitterstein | 1,8 | Spitzhacke | – | sich selbst; Grundgestein der [[Splitterweite]] |
| Splitterstein-Ziegel | 2,0 | Spitzhacke | – | auch als Treppe, Stufe, Mauer |
| Rissige Splitterstein-Ziegel | 2,0 | Spitzhacke | – | entstehen durch Schmelzen von Ziegeln |
| Shard-Erz | 3,0 | **Eisen**-Spitzhacke | 3 | 1 Roher Shard (Glück wirkt), 2–5 XP |
| Tiefenschiefer-Shard-Erz | 4,5 | **Eisen**-Spitzhacke | 3 | wie oben; kommt in der Oberwelt vor |
| Shard-Block | 5,0 | **Eisen**-Spitzhacke | – | Speicher für 9 Barren, Leuchtfeuer-Sockel |
| Leuchtkristall | 1,5 | Spitzhacke | **15** | sich selbst |
| Leuchtkristall-Cluster | 1,5 | Spitzhacke | 8 | mit Behutsamkeit sich selbst, sonst 1–3 Shard-Splitter (Glück wirkt); lässt sich an jede Seite setzen |

## Oberflächen und Kristallholz

| Block | Härte | Werkzeug | Licht | Besonderheit |
|---|---|---|---|---|
| Kristallmoos | 0,8 | Schaufel | – | Oberfläche von Kristallwald und Höhlen |
| Ascheboden | 0,6 | Schaufel | – | Oberfläche der Aschefelder |
| Kristallholzstamm | 2,0 | Axt | – | drehbar wie ein Stamm, zählt als Holz (Tag `logs`), Brennstoff |
| Kristallholzbretter | 2,0 | Axt | – | zählen als Bretter (Werkbank, Stöcke usw.), Brennstoff |
| Kristallblätter | 0,2 | Hacke/Schere | 4 | zerfallen ohne Stamm, lassen Setzlinge fallen |
| Kristallsetzling | sofort | – | 5 | wächst zu einem Kristallbaum mit Kirschbaum-Form, Knochenmehl wirkt |

## Leuchtende Pflanzen

Sie wachsen auf **Kristallmoos, Ascheboden, Splitterstein, Leuchtkristall, Moosblöcken und allen Erdarten** (Tag `shard_plant_soil`) und funkeln sichtbar.

| Pflanze | Licht | Drop / Nutzen |
|---|---|---|
| Leuchtblume | 10 | sich selbst; zähmt und vermehrt [[Mobs#Leuchtmotte|Leuchtmotten]], ergibt hellblauen Farbstoff, zählt als Blume (Bienen, Seltsame Suppe mit Leuchten) |
| Splitterfarn | 3 | nur mit Schere; ersetzbar wie Gras |
| Leuchtkappe | 12 | sich selbst; wächst auf Höhlenböden |
| Leuchtbeerenstrauch | 6 | sich selbst + 1–3 **Leuchtbeeren** (Glück wirkt) |

## Portal und Bossblöcke

| Block | Eigenschaften |
|---|---|
| Splitterrahmen | Härte 25, **Diamant**-Spitzhacke, Explosionsresistenz 1200, Licht 4 → [[Splitterportal]] |
| Splitterportal | nicht abbaubar, erlischt, sobald der Rahmen beschädigt wird; Licht 11, kein Item |
| Koloss-Altar | **unzerstörbar**, Licht 9; nimmt den Erweckungskristall an → [[Bosse#Der Kristallkoloss]] |
| Thron der Splitterweite | **unzerstörbar**, Licht 9; nimmt den Kolosskern an → [[Bosse#Die Splitterkönigin]] |
| Kolosstrophäe / Königinnentrophäe | Härte 3, Spitzhacke, Licht 10, richtet sich zum Spieler aus, feuerfestes Item |

Altäre und Trophäen sind auch im Kreativmodus als Items erhältlich, etwa für eigene Arenen auf dem Server.

## Splitterhandwerk: Bauen und Licht

| Block | Besonderheit |
|---|---|
| Kristallholz-Treppe, -Stufe, -Zaun, -Zauntor, -Tür, -Falltür, -Knopf, -Druckplatte | wie Vanilla-Holz; Tür und Falltür aus Glas mit Kristallholzrahmen |
| Polierter Splitterstein (+ Treppe, Stufe) | aus Splitterstein im Ofen oder an der Steinsäge |
| Gemeißelte Splitterstein-Ziegel | aus 2 Ziegelstufen oder an der Steinsäge |
| Hüterruinen-Ziegel, Hüterruinen-Säule | heller Hüterstein aus Calcit; Bauteile der Hüter-Ruinen und der Hüter-Bibliothek |
| Hüter-Runenstufe | Stufe mit leuchtender Rune (Leuchtschicht) |
| Splitterlaterne | Licht 15, steht oder hängt |
| Kristallglas / Kristallglasscheibe | nur mit Behutsamkeit abbaubar |

## Story-Blöcke

| Block | Eigenschaften |
|---|---|
| Erinnerungskristall | **unzerstörbar**, `vision=0..7`; Rechtsklick zeigt eine Vision → [[Story und Shardvirus#Erinnerungskristalle (8 Visionen)]] |
| Entscheidungsaltar | **unzerstörbar**, vor dem Thron; nimmt den Kern des Bruchs an → [[Story und Shardvirus#Endentscheidung]] |
| Reinheitsanker | Block-Entity mit wachsender Reinheitszone (6 → 24 Blöcke in 5 Minuten) → [[Story und Shardvirus#Gegenmittel]] |
| Schwäre | entsteht bei Nachbeben ab Kapitel 7; mit Stimmgabel oder Essenz zum Schweigen bringen, gibt Echosplitter → [[Nach dem Ausbruch#Schwären und Nachbeben]] |

## Infizierte Blöcke

Alle infizierten Blöcke leuchten (Overlay mit voller Eigenhelligkeit, ohne Lichtupdates) und sind über die Tags `shardline:virus_infectable/<art>` per Datapack erweiterbar. Ohne Behutsamkeit fallen die Grundblöcke.

| Stamm | Blöcke |
|---|---|
| Shardvirus (Oberwelt, Splitterweite) | Infizierter Stein, Erde, Grasblock, Holz, Laub, Sand, **Tiefenschiefer**, **Bretter** |
| Glutbrand (Nether) | Glutbrand-Netherrack (brennt ewig), -Schwarzstein, -Seelenerde, -Nezel, -Warzenblock |
| Leerenfäule (End) | Leerenfäule-Endstein, -Purpur |

Details: [[Story und Shardvirus#Infizierte Blöcke]] und [[Nach dem Ausbruch#Virus in der Tiefe, im Nether und im End]].

## Technische Blöcke (ohne Item)

| Block | Zweck |
|---|---|
| Kristallnetz | entsteht durch die [[Mobs#Kristallspinne|Kristallspinne]], verlangsamt stark, zerfällt nach 8 Sekunden |
| Mottenlicht | unsichtbare Lichtquelle (12) der [[Mobs#Leuchtmotte|Leuchtmotte]], entfernt sich selbst |

## Rohstoffe und Drops

| Item | Herkunft | Verwendung |
|---|---|---|
| Shard-Splitter | Mobs, Kristallcluster, Roher Shard (1 → 4) | Portalrahmen, Schlüssel, Leuchtkristall, **Währung** beim [[Splitterhändler]] |
| Roher Shard | Shard-Erz | schmelzen → Shard-Barren |
| Shard-Barren | Ofen, Hochofen | Shard-Set, Schlüssel, Bogen, Erweckungskristall |
| Aschestaub | Aschewandler | Brennstoff (4 Items pro Stück), 2 → schwarzer Farbstoff |
| Späherauge | Shard-Späher | Kristallbogen |
| Void-Rochenhaut | Void-Rochen | Kristallbogen |
| Schattenfell | Schattenfuchs | 1 → Leder |
| Ritterwappen | Gesplitterter Ritter | Erweckungskristall |
| Leuchtbeere | Leuchtbeerenstrauch | Nahrung mit Nachtsicht, zähmt Void-Rochen |
| Kolosskern | Kristallkoloss | Koloss-Set, Beschwörung der Königin |
| Herz der Splitterweite | Splitterkönigin | Spezialitem, Reparatur der Splitterklinge |
| Kristallschleim | Kristallschnecke | Kristallglas, Kristallbonbon, Resonanzkompass |
| Echosplitter | Schwären, Archivtruhe der Hüter-Bibliothek | Aufträge in [[Nach dem Ausbruch]] |
| Kern des Bruchs | drei besiegte Virusherzen | Endentscheidung am Entscheidungsaltar |

## Story- und Spezialitems

| Item | Herkunft | Wirkung |
|---|---|---|
| Lore-Buch (12 Varianten) | Strukturtruhen, Splitterhändler (4 Seiten binden) | schreibgeschütztes Buch in der eigenen Sprache |
| Lose Lore-Seite | Truhen der Hüter-Ruinen, Türme, Grüfte, Geoden | Rechtsklick öffnet ein Pergament; der Händler kauft sie für 2 Splitter |
| Reinigungsessenz | Rezept | reinigt Radius 6, heilt Fieber, verwandelt Infizierte zurück, 2 min Immunität |
| Resonanzkompass | Rezept | zeigt Richtung und Entfernung zum nächsten Bauwerk; Schleichen + Rechtsklick wechselt das Ziel |
| Stimmgabel der Hüter | Rezept | 64 Benutzungen; bringt Schwären zum Schweigen, Rechtsklick in die Luft horcht nach der nächsten Schwäre (96 Blöcke) |
| Chronik-Tagebuch | Rhea beim ersten Treffen oder Rezept | öffnet das Auftragsbuch (wie Taste J) |
| Kapitel-Emblem | – | nur Symbol der Kapitel-Advancements, nicht herstellbar |

## Essen

| Item | Wirkung |
|---|---|
| Leuchtbeere | Nahrung mit Nachtsicht, zähmt Void-Rochen, lockt Kristallschnecken |
| Leuchtkappen-Eintopf | 6 Hunger, 60 s Nachtsicht |
| Kristallbonbon | schnell gegessen, 15 s Tempo, auch satt essbar |
| Leuchtbeeren-Törtchen | 8 Hunger, 6 s Regeneration |

## Kompatibilitäts-Tags

Für Modpacks gibt es gemeinsame Tags: `c:ores/shard`, `c:raw_materials/shard`, `c:ingots/shard`, `c:storage_blocks/shard` und `c:tools/bow` (Kristallbogen). Alle Werkzeuge und Rüstungen stecken in den passenden Vanilla-Tags, damit Verzauberungen und Rüstungsbesätze funktionieren.

Kompostierbar sind:
- Leuchtbeere, Splitterfarn, Kristallblätter, Kristallsetzling (30 %)
- Leuchtblume, Leuchtkappe (65 %)
