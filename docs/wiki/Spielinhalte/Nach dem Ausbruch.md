---
tags: [shardline, spielinhalte, story, aftermath]
---

# Nach dem Ausbruch

Kapitel 7–9 der Story: Überlebendenlager, NPCs mit Dialogen, Aufträge, Schwären, zwei neue Endkämpfe und Epiloge – dazu die Erweiterung des Shardvirus in die Tiefe, den Nether und das End. Vorgeschichte: [[Story und Shardvirus]]. Bauwerke: [[Strukturen#Überlebendenlager]], [[Strukturen#Hüter-Bibliothek]]. Bosse: [[Bosse#Der Nachhall]], [[Bosse#Das Schwärenherz]].

Nach der Endentscheidung geht die Geschichte weiter: Die Weite bebt nach, Flüchtlinge aus der Oberwelt haben in der Splitterweite ein Lager aufgeschlagen, und die Wunde „singt“. Kapitel 7 beginnt, sobald die Welt ein Ende hat (Anmeldung, Dimensionswechsel oder direkt nach der Entscheidung). Ab Kapitel 8 läuft die Geschichte in zwei Zweigen, benannt wie die Enden im Code: `seal` (versiegelt) und `dominate` (beherrscht). Die Endkämpfe ändern das Weltende nie; die Entscheidungen in Kapitel 9 setzen nur den **Epilog** des Spielers.

## Ablauf

| Kapitel | versiegelt (`seal`) | beherrscht (`dominate`) |
|---|---|---|
| 7 Das Echo der Wunde | Lager finden, Rhea treffen (Chronik-Tagebuch), „Holz für das Lager“, „Die erste Schwäre“ mit Kesh → Kapitel 8 | gleich |
| 8 Wiederaufbau / Herrschaft | „Das Anker-Netz“ (Rhea), „Stimmen des Archivs“ (Veyl in der Hüter-Bibliothek) | „Gefolge“ (Rhea), „Schonen oder benutzen“ (Ilsa, Entscheidung über die Stimmlosen) |
| 9 Der letzte Ton | Veyl erweckt den **Nachhall** der Königin; nach dem Sieg Epilog `stillness` oder `chronicle` | Ilsa: neben dem Lager wächst das **Schwärenherz**; nach dem Sieg Epilog `truce` oder `dominion` |

Zweigübergreifend: „Der Gezeichnete“ (Kesh heilen), „Ritter ohne Herrn“, wiederholbar „Echoernte“ und „Nachtwache“. Nach dem Epilog reden alle NPCs anders (Epilog-Zeilen); `story.shardline.next.epilogue` ersetzt das nächste Ziel.

## Figuren

| NPC | Rolle | Ort |
|---|---|---|
| Rhea Tannwald (`rhea`) | Lagerleiterin, frühere Hauptfrau der Wache von Birkenfurt; vergibt die meisten Aufträge, bleibt im beherrschten Ende neutral | Überlebendenlager, am Feuer |
| Ilsa Brandt (`ilsa`) | Schmiedin und Alchemistin, Händlerin (Shard-Splitter, füllt sich täglich auf); im beherrschten Ende Anführerin der Stimmlosen | Lager, Werkstatt |
| Kesh (`kesh`) | der Gezeichnete, hört den Ton und führt zu Schwären; folgt dem Spieler (Begleitschritt), lässt sich heilen (eigene Textur) | Lager, Rand |
| Veyl (`veyl`) | Geist des letzten Hüter-Archivars, an die Bibliothek gebunden, schwebt, schläft nicht, unverwundbar | Hüter-Bibliothek |

Alle NPCs: Rechtsklick öffnet den Dialog, im Lagerumkreis (`aftermath.campRadius`) unverwundbar, außerhalb fliehen sie vor Monstern und brechen bei tödlichem Schaden zusammen statt zu sterben (nur `/kill` tötet). Nachts gehen sie zu ihrem Platz und schlafen (`aftermath.npcSleep`), ansprechbar dann nur schleichend. Animationen: idle, walk, talk, hurt, sleep, death (hält den letzten Frame).

## Strukturen

- **Überlebendenlager** (`shardline:survivor_camp`, Kristallwald und Aschefelder, Abstand 44/16 Chunks): runde Lichtung mit Palisade, Lagerfeuer, drei Zelten mit Betten, Ilsas Werkstatt (Amboss, Braustand), Vorratstruhe (`chests/survivor_camp`); Rhea, Ilsa und Kesh erscheinen mit Heimatpunkt. Keine Monster-Spawns im Lager.
- **Hüter-Bibliothek** (`shardline:keeper_library`, ganze Splitterweite, Abstand 40/14): Saal mit Säulen, Kristallglasfenstern, eingestürztem Runendach, Regalreihen, Lesepulten und Archivtruhe (`chests/keeper_library`, Lore-Bücher, Echosplitter); Veyl schwebt in der Mitte.
- Beide sind Ziele des **Resonanzkompasses** (Schleichen + Rechtsklick wechselt das Ziel). In alten Welten entstehen sie nur in neuen Chunks; zum Testen `/place structure shardline:survivor_camp`.

## Schwären und Nachbeben

- **Nachbeben:** Alle `aftermath.aftershockMinutes` (8) bebt die Weite für Spieler ab Kapitel 7 (Klang, kurzes Wackeln, Meldung). Dabei entsteht 24–48 Blöcke entfernt eine **Schwäre** (`shardline:festering_node`), solange weniger als `aftermath.maxNodesNearPlayer` (3) im Umkreis von 96 Blöcken liegen. Gesetzt wird nur in geladenen Chunks; gezählt wird über eine gespeicherte Liste, nie über eine Weltsuche.
- **Versiegelte Welt:** eine Narbe, die sich nicht ausbreitet. **Beherrschte Welt:** der Ton singt gegen den Herrscher – die Schwäre streut Infektion und ruft Befallene („Gegengesang“), die auch Splitterherrscher angreifen und sich nicht zähmen lassen.
- **Zum Schweigen bringen** mit der **Stimmgabel der Hüter** (64 Benutzungen, Rezept: 2 Shard-Barren, Goldbarren, Stock) oder Reinigungsessenz: gereinigt (Radius 4) oder – als Herrscher in der beherrschten Welt – gebunden. Beides gibt 2–3 **Echosplitter** und zählt für Aufträge. Abbauen ohne Werkzeug: kein Drop, zwei Befallene erscheinen.
- Die Stimmgabel horcht bei Rechtsklick in die Luft nach der nächsten Schwäre (bis 96 Blöcke) und nennt Richtung und Entfernung.

## Endkämpfe

| Gegner | Grundlage | Besonderheiten |
|---|---|---|
| Nachhall (`shardline:echo_queen`) | Königin (Modell, Angriffe, Klone, Leerzonen), durchscheinende Geisttextur | Leben `aftermath.echoQueenHealth` (320); löst weder Ausbruch noch Entscheidungsaltar aus |
| Schwärenherz (`shardline:festering_heart`) | Virusherz (drei Phasen), Eiter-/Rost-Textur | Leben `aftermath.festeringHeartHealth` (320); greift Herrscher an, „Gegengesang“ alle 4 s gegen Herrscher (Schwäche, Langsamkeit, 2 Schaden); zählt nicht für den Kern des Bruchs |

Beide erscheinen über die Dialogaktion `summon_encounter` (nie doppelt im Umkreis von 96 Blöcken; das NPC kann sie erneut rufen, solange der Sieg fehlt). Der Sieg setzt bei allen Spielern im Umkreis von 48 Blöcken die Flagge `boss:echo_queen` bzw. `boss:festering_heart` und das gleichnamige Advancement.

## Aufträge

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

## Dialoge

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

## Auftragsbuch

Taste **J** (änderbar) oder das **Chronik-Tagebuch** (Rhea gibt es beim ersten Treffen; Rezept: Buch + Shard-Splitter). Reiter **Kapitel** (Titel, Zusammenfassung, nächstes Ziel, Epilog), **Aufträge** (Auftraggeber, Beschreibung, erledigte und aktueller Schritt mit Fortschritt), **Figuren** (Porträt, Beschreibung je nach Lage) und **Lore** (gelesene Seiten). Der Server schickt den Stand (`JournalPayload`) beim Öffnen und bei jeder Änderung; ein offenes Buch aktualisiert sich.

## Virus in der Tiefe, im Nether und im End

- **Tiefe:** Ein Block ohne Luftkontakt wird befallen, wenn ein Nachbar schon befallen ist, die nächste Oberfläche entlang einer Achse höchstens `virus.maxDepth` (6) Blöcke entfernt ist und der Block „porös“ ist (fester Anteil `virus.depthChance` = 0,35, aus der Position berechnet). So wachsen Adern ins Gestein statt voller Würfel. Kosten: höchstens 6 + 6 · maxDepth Blockabfragen pro Versuch, keine Chunk-Ladungen.
- **Stämme:** Shardvirus (Oberwelt, Splitterweite), **Glutbrand** (Nether: Ausbreitung × `virus.emberSpreadMultiplier` 1,25, setzt Lebewesen 2 s in Brand) und **Leerenfäule** (End: × `virus.voidSpreadMultiplier` 0,75, 3 s Dunkelheit). Wirkungen abschaltbar mit `virus.strainEffects`. Erlaubte Dimensionen: `virus.dimensions`.
- **Neue Blöcke:** `infected_deepslate` (Tiefenschiefer, Bruchtiefenschiefer, Tuff), `infected_planks` (alle Bretter, auch in Häusern), `infected_netherrack` (Netherrack, Magmablock; brennt ewig), `infected_blackstone` (Schwarzstein, Basalt), `infected_soul_soil` (Seelenerde, Seelensand), `infected_nylium` (beide Nezel), `infected_wart_block` (beide Warzenblöcke), `infected_end_stone` (Endstein, Endsteinziegel), `infected_purpur` (Purpur, Purpursäule). Tags `shardline:virus_infectable/<art>` per Datapack erweiterbar.
- **Neue Mobs:** Kristallisierter Piglin (aus Piglin, Piglin-Barbar, Zombifiziertem Piglin; feuerfest, Goldschwert) und Kristallisierter Enderman (greift ohne Blickkontakt an, springt zu fernen Zielen, Treffer geben Dunkelheit und Fieber). Beide zähmbar, reinigbar (`virus.netherEndMobsTransform`).
- **Aussaat:** neue Herde nahe Spielern im Nether und End alle `virus.netherEndSeedMinutes` (10) und beim Ausbruch.

## Konfiguration

| Abschnitt | Neue Schlüssel (Standard) |
|---|---|
| `dialogue` | `maxDistance` (8), `commandWhitelist` (`effect give @s `, `particle `, `playsound `), `questCheckTicks` (40) |
| `aftermath` | `campRadius` (32), `npcSleep` (true), `aftershockMinutes` (8), `maxNodesNearPlayer` (3), `echoQueenHealth` (320), `festeringHeartHealth` (320) |
| `virus` | `netherEndSeedMinutes` (10), `maxDepth` (6), `depthChance` (0.35), `surfaceOnly` (false), `dimensions` (Oberwelt, Nether, End, Splitterweite), `emberSpreadMultiplier` (1.25), `voidSpreadMultiplier` (0.75), `strainEffects` (true), `netherEndMobsTransform` (true) |
| Client `dialogue` | `typewriter` (true), `textSpeed` (45), `typeSound` (true), `autoAdvance` (false), `autoAdvanceSeconds` (4) |

## Befehle

| Befehl | Wirkung |
|---|---|
| `/shardline dialogue start <npc> <id>` | Dialog ohne NPC starten (Test) |
| `/shardline dialogue reload` | Dialoge und Aufträge neu laden, Fehler im Log |
| `/shardline quest list [spieler]` | Aufträge mit Schritt und Stand |
| `/shardline quest start\|complete\|reset <id> [spieler]` | Auftrag beginnen, aktuellen Schritt abschließen, zurücksetzen |

## Registry-Namen

- Wesen: `rhea`, `veyl`, `ilsa`, `kesh`, `crystallized_piglin`, `crystallized_enderman`, `echo_queen`, `festering_heart` (alle mit Spawn-Ei)
- Blöcke: `festering_node`, `infected_deepslate`, `infected_planks`, `infected_netherrack`, `infected_blackstone`, `infected_soul_soil`, `infected_nylium`, `infected_wart_block`, `infected_end_stone`, `infected_purpur`
- Items: `chronicle_journal`, `keeper_tuning_fork`, `echo_shard`
- Strukturen: `survivor_camp`, `keeper_library`; Loot: `chests/survivor_camp`, `chests/keeper_library`
- Advancements: `story/chapter_7`, `story/chapter_8_seal|dominate`, `story/chapter_9_seal|dominate`, `story/festering_node`, `story/echo_queen`, `story/festering_heart`, `story/epilogue_stillness|chronicle|truce|dominion`
- Netzwerk (Protokoll 5): `open_dialogue`, `dialogue_choice`, `close_dialogue`, `journal`, `open_journal`; Bildschirmeffekt `AFTERSHOCK`
- Taste: `key.shardline.journal` (J)

## Tests (Batches `shardline_aftermath`, `shardline_virus`)

`dialogueEngine`, `dialogueValidation`, `questFlow`, `aftermathChapters`, `aftermathSaveData`, `dialogueSessions`, `dialoguePayloadCodec`, `storyDataFiles` (alle Dateien ohne Fehler, alle Texte vorhanden), `rheaNpc`, `veylAndIlsa`, `keshEscort`, `virusStrainBlocks`, `virusDepth`, `virusStrainMobs`, `aftermathFlowSeal` (Kapitel 7 → 8 über die echten Dialoge), `aftermathBossesAndEpilogue`, `festeringNodeDominated`, `journalSync`, `aftermathStructures`.
