---
tags: [shardline, verschiedenes, todo]
stand: 2026-10-24
---

# ToDos

## Erledigt

- [x] Virus soll nicht nur die Oberfläche infizieren → Tiefe mit „porösem“ Gestein (`virus.maxDepth`, `virus.depthChance`)
- [x] Mehr Virusvarianten und Blöcke → Stämme Glutbrand und Leerenfäule, neun neue infizierte Blöcke, kristallisierter Piglin und Enderman
- [x] Virus breitet sich auch im Nether und End aus (`virus.dimensions`, `virus.netherEndSeedMinutes`)
- [x] Kapitel 7–9 mit NPCs, Dialogen, Aufträgen, Auftragsbuch, Endkämpfen und Epilogen → [[Nach dem Ausbruch]]
- [x] Strukturen Überlebendenlager und Hüter-Bibliothek
- [x] 21 eigene Soundeffekte eingebaut (22 Events)
- [x] Musik der Splitterweite (`music.splitterweite`)
- [x] Doku-Vault aktualisiert (Stand 24.10.2026)

## Offen

- [ ] Restliche Sounds liefern (siehe unten)
- [ ] Musik: `music.infected` (Stereo-Loop, 90–180 s)
- [ ] Im Spiel testen, was die GameTests nicht abdecken: Fenster (Dialog, Auftragsbuch, Entscheidung), Kapitelkarte, Kämpfe gegen Nachhall und Schwärenherz, Schlaf der NPCs, Virus im Nether und End
- [ ] Englische Texte der neuen Kapitel gegenlesen
- [ ] Balance: Ausbreitungstempo des Virus und Leben der neuen Bosse auf dem Server prüfen

## Fehlende Sounds

Diese 112 Events nutzen noch Vanilla-Platzhalter. Beschreibungen, wie sie klingen sollen, stehen im Projekt in `tools/assetgen/STORY_ASSETS.md`.

| Gruppe | Events |
|---|---|
| Bosse | `crystal_colossus.ambient\|hurt\|death\|pillar\|summon`, `shard_queen.ambient\|hurt\|death`, `virus_heart.ambient\|hurt\|death\|phase\|tendrils\|summon` |
| Wesen der Weite | `shard_creeper.hurt\|death\|fuse`, `crystal_golem.ambient\|hurt\|death\|angry`, `glow_moth.*`, `ash_walker.*`, `shard_scout.*` + `shoot`, `void_ray.*` + `flap`, `crystal_spider.*`, `shadow_fox.*` + `vanish`, `crystal_snail.*` + `hide`, `shattered_knight.*` + `charge`, `shard_trader.*` + `yes\|no\|trade` |
| Befallene | `shard_zombie.hurt\|death`, `crystallized_cow.ambient\|hurt`, `crystallized_sheep\|pig\|villager\|piglin\|enderman.ambient\|hurt\|death`, `infected.cured` |
| NPCs | `rhea\|veyl\|ilsa\|kesh.voice\|hurt\|death` |
| Blöcke | `shard_portal.ambient`, `purity_anchor.grow`, `decision_altar.rise\|awaken\|use`, `festering_node.hum\|purify\|bind\|scream` |
| Items, Fähigkeiten | `shard_projectile.shatter`, `crystal_shield.activate`, `colossus_gauntlet.slam`, `heart_of_the_expanse.blink`, `queens_shardblade.volley`, `keeper_tuning_fork.ring` |
| Oberfläche, Story | `ui.dialogue.open\|close\|type\|choice`, `ui.quest.update\|complete`, `story.epilogue`, `event.aftershock` |
| Musik (Loops, Stereo, 90–180 s) | `music.infected` |
