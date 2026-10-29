---
tags: [shardline, spielinhalte, portal]
---

# Splitterportal

Das Portal verbindet Oberwelt und [[Splitterweite]]. Es funktioniert wie ein Netherportal, nutzt aber eigene Blöcke und eigene Effekte.

## Bau

1. Lege einen rechteckigen Rahmen aus **Splitterrahmen**-Blöcken, senkrecht wie beim Netherportal.
   - Die Innenfläche ist mindestens 2 × 3 und höchstens 21 × 21 Blöcke groß.
   - Die Ecken sind optional. Das kleinste Portal braucht 10 Rahmen.
2. Klicke mit dem **Shard-Schlüssel** von innen auf einen Rahmenblock, am einfachsten auf die obere Fläche des unteren Rahmens.
3. Das Portal füllt sich, und der Schlüssel verliert 1 Haltbarkeit. Er hat insgesamt 8 Ladungen.

> [!warning] Unvollständiger Rahmen
> Ist der Rahmen nicht geschlossen, erscheint in der Aktionsleiste: „Der Splitterrahmen ist nicht vollständig.“ Zerstört man später einen Rahmenblock, erlischt das Portal.

## Material

| Block | Rezept | Eigenschaften |
|---|---|---|
| Splitterrahmen | 4 Shard-Splitter in Kreuzform um 1 Obsidian | Härte 25 wie Obsidian, braucht eine **Diamant-Spitzhacke**, Explosionsresistenz 1200, leuchtet schwach (4) |
| Shard-Schlüssel | 2 Shard-Barren + 2 Shard-Splitter + 1 Enderauge | 8 Haltbarkeit; auch beim [[Splitterhändler]] erhältlich |

## Reise

- **Wartezeit:** wie beim Netherportal, also 4 Sekunden im Überlebensmodus (Spielregel `playersNetherPortalDefaultDelay`) und sofort im Kreativmodus.
- **Maßstab:** Oberwelt-Koordinaten werden durch 4 geteilt, Splitterweite-Koordinaten mit 4 multipliziert.
- **Zielportal:** In einem Umkreis von 48 Blöcken um den Zielpunkt wird ein vorhandenes Splitterportal gesucht.
- **Neues Portal:** Gibt es keins, wird in einem Umkreis von 16 Blöcken ein Platz mit festem Boden gesucht und dort ein 4 × 5-Rahmen samt Portal gebaut.
- **Notplattform:** Findet sich kein Boden, etwa über dem Void, entsteht eine schwebende Plattform.
  - In der Splitterweite besteht sie aus 5 × 4 Splitterstein-Ziegeln auf Höhe Y 96 bis 140.
  - In der Oberwelt besteht sie aus Obsidian.
- Auch Tiere, Items und andere Entities reisen mit. **Bosse** können das Portal nicht benutzen.

## Eigener Portaleffekt

| Wann | Effekt |
|---|---|
| Im Portal stehen | violetter, pulsierender Kristallschleier über dem Bildschirm (abschaltbar in der [[Konfiguration#Client]]) |
| Am Portal | Kristallfunkeln, umgekehrte Portalpartikel und ein eigener Summton |
| Bei der Ankunft | Kristallklang, ein Wirbel aus Funken und End-Stab-Partikeln |
| Nach der Ankunft | **10 Sekunden Sanfter Fall**, als Schutz vor dem Sturz ins Void |

Die Dauer von Sanfter Fall steht als `portalSlowFallingSeconds` in der [[Konfiguration]].

> [!warning] Portale und der Shardvirus
> Nach dem Bruch infizieren Splitterportale **außerhalb** der Splitterweite ihre Umgebung (Radius `virus.portalSeedRadius` = 6, Chance `portalSeedChance` = 0,75 pro Random Tick). Ein Reinheitsanker neben dem Portal verhindert das. Siehe [[Story und Shardvirus#Ausbreitung]].
