# Konzept „Nach dem Ausbruch“ (bestätigt)

Kapitel 7–9 nach der Endentscheidung, datengetriebene Dialoge und Aufträge, vier NPCs, Virus-Erweiterung.

## Feste Entscheidungen
1. Kapitel 7 beginnt erst, wenn die Welt ein Ende hat (Login, Dimensionswechsel, Endentscheidung).
2. Schwären im versiegelten Ende breiten sich nicht aus (Nachhall-Narben); im beherrschten Ende singt dort der Ton gegen den Herrscher.
3. Die Endkämpfe ändern das Weltende nie; 9a/9b-Entscheidungen setzen nur den Epilog des Spielers.
4. Veyl ist ein an die Hüter-Bibliothek gebundener Geist.
5. Im beherrschten Ende führt Ilsa den Widerstand („Die Stimmlosen“), Rhea bleibt neutral.
6. Der Splitterhändler bleibt beim bestehenden Dialogsystem.
7. Netzwerkprotokoll 5.
8. Zweige heißen wie die Enden im Code: `seal` und `dominate` (Advancements `story/chapter_8_seal` usw.).

## Kapitel
| Nr. | Titel | Zweig |
|---|---|---|
| 7 | Das Echo der Wunde | beide |
| 8 | Wiederaufbau / Herrschaft | seal / dominate |
| 9 | Der letzte Ton: Nachhall / Gegengesang | seal / dominate |
Epiloge: `stillness`, `chronicle` (seal), `truce`, `dominion` (dominate).

## Figuren
Rhea Tannwald (Lagerleiterin), Veyl (Chronist, Geist), Ilsa Brandt (Schmiedin/Alchemistin, Widerstand im beherrschten Ende), Kesh (der Gezeichnete).

## Aufträge
camp_wood, first_festering, the_marked, archive_voices (seal), anchor_net (seal), knights_without_lord, following (dominate), spare_or_use (dominate), echo_harvest (wiederholbar), night_watch (wiederholbar).

## Neue Inhalte
Items `chronicle_journal`, `keeper_tuning_fork`, `echo_shard`; Block `festering_node`; Strukturen `survivor_camp`, `keeper_library`;
Virus: Tiefe (`virus.maxDepth`, `virus.depthChance`, `virus.surfaceOnly`), Stämme Glutbrand (Nether) und Leerenfäule (End), infizierte Blöcke
`infected_deepslate`, `infected_planks`, `infected_netherrack`, `infected_blackstone`, `infected_soul_soil`, `infected_nylium`,
`infected_wart_block`, `infected_end_stone`, `infected_purpur`, Mobs `crystallized_piglin`, `crystallized_enderman`.

## Dialogformat
Siehe STORY.md, Abschnitt „Dialoge“ (wird mit Block 9 vervollständigt). Bedingungen: chapter_min, chapter_max, ending, branch, title,
has_item, flag, not_flag, quest_active, quest_done, quest_new, quest_step, epilogue, any, not. Aktionen: give_item, take_item, set_flag,
clear_flag, advance_chapter, start_quest, complete_quest_step, open_trade, play_sound, run_command_safe, set_epilogue, give_xp.
