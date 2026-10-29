# Shardline

NeoForge-Mod für Minecraft 1.21.1, entwickelt für das Netzwerk Shardline.eu. Die Mod muss auf dem Server **und** auf jedem Client installiert sein. Ein Paper- oder Spigot-Server reicht nicht aus.

## Bauen und Testen

Du brauchst ein JDK 21. Im Ordner `.tools/jdk-21` liegt ein portables JDK, alternativ setzt du `JAVA_HOME` auf eine eigene Installation.

| Befehl | Zweck |
|---|---|
| `gradlew build` | baut die Mod nach `build/libs/shardline-<version>.jar` |
| `gradlew runData` | erzeugt Modelle, Loot, Rezepte, Tags, Advancements und Worldgen neu |
| `gradlew runGameTestServer` | führt die automatischen Rauchtests aus |
| `gradlew runClient` / `gradlew runServer` | startet Client bzw. Server in der Entwicklungsumgebung |

**Server-Rauchtest mit echter Splitterweite:** Lösche zuerst `run/world`, denn eine vom GameTest-Server angelegte Welt hat eine falsch eingerichtete Splitterweite. Kopiere dann `tools/devtest_datapack` nach `run/world/datapacks/shardline_devtest` und starte `gradlew runServer`. Der Server führt alle GameTests in einer echten Welt aus, schreibt Ergebnisse mit `[GameTest]` ins Log und stoppt nach 60 Sekunden. Danach löschst du `run/world`, sonst stoppt jeder weitere Start ebenfalls. Fehlgeschlagene Tests erscheinen als `ERROR … failed`. Die EULA in `run/eula.txt` ist für diese lokale Testumgebung bereits akzeptiert.

Abhängigkeiten:
- **Pflicht:** GeckoLib (4.9.3 oder neuer)
- **Optional:** JEI oder EMI. Beide zeigen die Rezepte und zusätzlich Infoseiten.

Einstellungen liegen in `config/shardline-common.toml` (Spawnraten, Boss-HP, Schaden, Set-Boni, Story, Shardvirus, Ende, Fähigkeiten) und in `config/shardline-client.toml` (Nebel, Portal-Overlay, Virus-Atmosphäre, Bildschirmeffekte).

## Fortschrittspfad

1. **Oberwelt:** Tief im Tiefenschiefer findest du selten Shard-Erz. Nachts spawnen Splitterkriecher, Aschewandler, Kristallspinnen, Schattenfüchse und Leuchtmotten.
2. **Portal:** Der Splitterrahmen kostet je Block 1 Obsidian und 4 Shard-Splitter. Du baust ihn wie ein Netherportal und aktivierst ihn mit dem Shard-Schlüssel.
3. **Shard-Set:** Es wird aus Shard-Barren hergestellt. Set-Bonus: Nachtsicht in der Splitterweite und 50 % weniger Splitterschaden.
4. **Kristallkoloss:** Gesplitterte Ritter in den Ruinen-Türmen lassen Ritterwappen fallen, daraus entsteht der Erweckungskristall. Diesen setzt du auf den Altar einer Kristallwald-Arena. Die Karte zur Arena verkauft der Splitterhändler.
5. **Kolossrüstung:** Netherit-Teil, Koloss-Schmiedevorlage und Kolosskern ergeben am Schmiedetisch ein Kolossteil. Set-Bonus: Rückstoßimmunität, 4 zusätzliche Herzen und der Kristallschild auf Taste V.
6. **Splitterkönigin:** Lege einen Kolosskern auf den Thron im Zentrum der Splitterweite bei den Koordinaten 0 / 0. Sie schwebt meist außer Reichweite, stürzt sich aber regelmäßig herab (Schrei und Hinweis über der Hotbar). Dann ist sie im Nahkampf zu treffen. Sie heilt sich erst, wenn niemand mehr in der Arena ist.
7. **Der Bruch und das Ende:** Der Tod der Königin setzt den Shardvirus frei, vor dem Thron wächst der Entscheidungsaltar. Danach gilt es, ihn einzudämmen (Reinheitsanker mit wachsender Zone, Reinigungsessenz und Immunität), drei Virusherzen mit je drei Phasen zu zerstören und am Altar zu entscheiden: versiegeln oder beherrschen. Den eigenen Stand zeigt `/shardline story status`. Alles dazu steht in [STORY.md](STORY.md).

## Strukturen

| Struktur | Wo | Inhalt |
|---|---|---|
| Splitterkrater | Oberwelt: Ebenen, Savannen, Wüsten, Tafelberge, Wiesen | Mulde aus Asche mit einem Brocken voller Shard-Erz, verstreute Portalrahmen-Blöcke, Truhe eines Wanderers (selten Shard-Schlüssel) |
| Ruinen-Turm | Splitterweite, alle Biome | 3 Stockwerke, Truhen, Gesplitterte Ritter, Erinnerungskristall, manchmal ein gefangener Händler |
| Hüter-Schrein | Kristallwälder | Säulenkreis mit Erinnerungskristall, Opfertruhe, Leuchtmotten |
| Handelsposten | Splitterweite, alle Biome | Kristallholz-Hütte mit Splitterhändler und Vorratstruhe |
| Aschegruft | Aschefelder | Vergrabene Grabkammer: Schacht mit Leiter, Sarkophag, 2 Truhen, Aschewandler und Kristallspinne |
| Kristallgeode | Leuchtende Höhlen | Halb vergrabene Hohlkugel aus Leuchtkristall mit Shard-Erz, Kristallspinnen und Truhe |
| Koloss-Arena | Kristallwälder | Altar für den Kristallkoloss |
| Hüter-Ruinen | Splitterweite, alle Biome | Eingestürzter Hütersaal mit Runenpfad, Erinnerungskristall und Lore-Truhe (Bücher und lose Seiten) |
| Thron der Königin | Zentrum (0 / 0) | Arena der Splitterkönigin; davor der Entscheidungsaltar |

## Splitterhandwerk

Bauen, Licht, Essen und Werkzeuge rund um die Splitterweite.

| Inhalt | Herstellung | Hinweis |
|---|---|---|
| Kristallholz-Treppe, -Stufe, -Zaun, -Zauntor, -Tür, -Falltür, -Knopf, -Druckplatte | wie Vanilla-Holz aus Kristallholzbrettern | Tür und Falltür aus Glas mit Kristallholzrahmen |
| Polierter Splitterstein (+ Treppe, Stufe) | Splitterstein im Ofen oder an der Steinsäge | |
| Gemeißelte Splitterstein-Ziegel | 2 Splitterstein-Ziegelstufen übereinander oder Steinsäge | |
| Splitterlaterne | 8 Eisennuggets um einen Shard-Splitter | Licht 15, steht oder hängt |
| Kristallglas (8) / Kristallglasscheibe (16) | 8 Glas um Kristallschleim / 6 Kristallglas | nur mit Behutsamkeit abbaubar |
| Leuchtkappen-Eintopf | Schüssel, 2 Leuchtkappen, brauner Pilz | 6 Hunger, 60 s Nachtsicht |
| Kristallbonbon (3) | 2 Zucker, Kristallschleim | schnell gegessen, 15 s Tempo, auch satt essbar |
| Leuchtbeeren-Törtchen | 3 Leuchtbeeren, Weizen, Zucker, Ei | 8 Hunger, 6 s Regeneration |
| Resonanzkompass | Shard-Splitter, 2 Shard-Barren, Kompass, Kristallschleim | siehe unten |

**Kristallschnecke:** friedliches Wesen im Kristallwald und in den Leuchtenden Höhlen. Sie folgt Leuchtbeeren und hinterlässt alle 5 bis 10 Minuten einen Kristallschleim. Bei einem Treffer zieht sie sich 5 Sekunden in ihr Haus zurück und nimmt dann nur ein Fünftel des Schadens.

**Resonanzkompass:** Rechtsklick sucht das nächste Bauwerk des gewählten Typs und zeigt Richtung und Entfernung an; eine Lichtspur weist kurz den Weg. Schleichen und Rechtsklick wechselt das Ziel. In der Splitterweite stehen alle Bauwerke der Weite zur Wahl, in der Oberwelt der Splitterkrater.

**Verzauberungen** (Zaubertisch, Handel und Beute):
- **Splitterbann I–V** (Schwert und Axt): +2,5 Schaden je Stufe gegen Splitterwesen, Bosse und Infizierte. Schließt Schärfe, Bann und Nemesis der Gliederfüßer aus.
- **Kristallwehr I–IV** (Rüstung): Schutz gegen Splitterschaden, also Shard-Geschosse, Leerzonen, Kristallsäulen, Splitterfieber und Viruspulse. Schließt die anderen Schutz-Verzauberungen aus.

Neue Advancements im Tab „Shardline“: *Langsam, aber glänzend*, *Echo im Stein*, *Kristallhandwerker* und *Leuchten von innen*.

Eine Liste aller Texturen und Modelle steht in [ASSETS.md](ASSETS.md).
