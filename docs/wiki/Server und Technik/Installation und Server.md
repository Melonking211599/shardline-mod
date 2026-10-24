---
tags: [shardline, technik, server]
---

# Installation und Server

## Voraussetzungen

| Komponente | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.264 oder neuer |
| Java | 21 |
| GeckoLib (NeoForge 1.21.1) | 4.9.3 oder neuer, **Pflicht** |
| JEI oder EMI | optional, nur auf dem Client sinnvoll |

## Installation

1. NeoForge 1.21.1 installieren (Server und Clients).
2. `shardline-1.0.0.jar` und die GeckoLib-JAR in den Ordner `mods/` legen, **auf dem Server und bei jedem Spieler**.
3. Starten. Beim ersten Start entstehen die Konfigurationsdateien (siehe [[Konfiguration]]).

> [!warning] Server und Client
> Shardline fügt neue Blöcke, Items, Entities und eine Dimension hinzu. Das geht nur, wenn die Mod auf beiden Seiten installiert ist. Ein reiner Paper- oder Spigot-Server kann die Mod nicht laden. Clients ohne Mod können einem Server mit Shardline nicht beitreten, und umgekehrt.

## Betrieb im Netzwerk Shardline.eu

- **Server:** Jeder Backend-Server, der die Splitterweite anbieten soll, braucht NeoForge mit Shardline und GeckoLib.
- **Proxy:** Hängen die Server an einem Proxy wie Velocity, muss dieser modded Clients unterstützen.
- **Konfiguration:** `config/shardline-common.toml` gilt global auf dem Server: Boss-HP, Spawnraten, Story, Shardvirus, Ende usw. Die Client-Datei `shardline-client.toml` betrifft nur Darstellung (Nebel, Overlay, Atmosphäre, Bildschirmeffekte, Dialogfenster) und ist Sache jedes Spielers.
- **Story-Stand:** Kapitel, Aufträge, Flaggen, Ende und Virus-Zustand liegen pro Welt in `data/shardline_story.dat`. Auf mehreren Backend-Servern hat jede Welt ihren eigenen Stand.
- **Dialoge und Aufträge** lassen sich per Datapack anpassen (`data/shardline/dialogues`, `data/shardline/quests`) und mit `/shardline dialogue reload` neu laden.

## Netzwerk

Protokollversion **5**. Server und Client müssen dieselbe Version haben. Die gesamte Logik läuft auf dem Server; der Client zeigt nur an.

| Richtung | Pakete |
|---|---|
| Client → Server | `CrystalShieldPayload` (V), `AbilityPayload` (G, H), `EndingChoicePayload`, `TraderActionPayload`, `DialogueChoicePayload`, `CloseDialoguePayload`, `OpenJournalPayload` (J) |
| Server → Client | `StoryStatePayload`, `TitlesPayload`, `ScreenEffectPayload` (Bruch, Vision, Nachbeben …), `ChapterCardPayload`, `OpenEndingChoicePayload`, `TraderDialoguePayload`, `OpenLorePagePayload`, `OpenDialoguePayload`, `JournalPayload` |

Clients ohne einzelne Kanäle bekommen Händlertexte und Lore-Seiten ersatzweise im Chat.

## Neue Welt oder bestehende Welt?

- **Bestehende Welt:** Die Splitterweite ist eine eigene Dimension. Sie wird beim ersten Betreten generiert und funktioniert auch in bestehenden Welten.
- **Oberwelt:** Das **Shard-Erz in der Oberwelt** erscheint nur in **neu generierten Chunks**. Bereits erkundete Gebiete bleiben unverändert.
- **Zentrum:** Die Königinnen-Arena entsteht im Chunk 0/0 der Splitterweite, sobald jemand diesen Bereich lädt.
- **Neue Strukturen** (Hüter-Ruinen, Überlebendenlager, Hüter-Bibliothek, Splitterkrater) erscheinen nur in neu generierten Chunks.
- **Alte Stände:** Registry-Namen sind unverändert; Welten aus älteren Versionen laden weiter. Reinheitsanker aus älteren Welten haben sofort den vollen Radius, und der Thron nimmt den Kern des Bruchs weiterhin an, falls die Königin schon vor dem Entscheidungsaltar gefallen ist.

## Nützliche Befehle (Operator)

```text
/execute in shardline:splitterweite run tp @s 8 120 8
/locate structure shardline:survivor_camp
/locate structure shardline:keeper_library
/place structure shardline:survivor_camp
/locate structure shardline:ruin_tower
/locate structure shardline:colossus_arena
/summon shardline:crystal_colossus ~ ~ ~
/give @s shardline:shard_key
/give @s shardline:colossus_summoning_crystal
```

Mit `/summon` erzeugte Bosse haben keinen Altar als Heimat und sind daher nicht an eine Arena gebunden.

### Story, Virus, Dialoge

Alle außer `story status` und `story vote` brauchen Operator-Rechte (Stufe 2).

| Befehl | Wirkung |
|---|---|
| `/shardline story status` | eigener Fortschritt und nächstes Ziel (alle Spieler) |
| `/shardline story set <0-9> [spieler]` | Kapitel setzen |
| `/shardline story info [spieler]` | Kapitel, Bücher, Visionen, Herzen, Titel |
| `/shardline story vote <seal\|dominate>` | Stimme abgeben (alle Spieler) |
| `/shardline story ending reset` | Ende und Titel zurücksetzen |
| `/shardline lore give <buch\|all> [spieler]` | Lore-Bücher ausgeben |
| `/shardline virus status\|pause\|resume\|outbreak\|reset` | Virus-Zustand, anhalten, fortsetzen, Testausbruch, Zustand „infiziert“ aufheben |
| `/shardline virus purge <radius>` | im Radius reinigen (max. 256) |
| `/shardline dialogue start <npc> <id>` | Dialog ohne NPC starten |
| `/shardline dialogue reload` | Dialoge und Aufträge neu laden |
| `/shardline quest list [spieler]` | Aufträge mit Stand |
| `/shardline quest start\|complete\|reset <id> [spieler]` | Auftrag beginnen, Schritt abschließen, zurücksetzen |
