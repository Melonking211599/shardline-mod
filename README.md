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

**Server-Rauchtest mit echter Splitterweite:** Kopiere `tools/devtest_datapack` nach `run/world/datapacks/shardline_devtest` und starte `gradlew runServer`. Der Server führt alle GameTests in einer echten Welt aus, schreibt Ergebnisse mit `[GameTest]` ins Log und stoppt nach 60 Sekunden. Danach löschst du `run/world`, sonst stoppt jeder weitere Start ebenfalls. Fehlgeschlagene Tests erscheinen als `ERROR … failed`. Die EULA in `run/eula.txt` ist für diese lokale Testumgebung bereits akzeptiert.

Abhängigkeiten:
- **Pflicht:** GeckoLib (4.9.3 oder neuer)
- **Optional:** JEI oder EMI. Beide zeigen die Rezepte und zusätzlich Infoseiten.

Einstellungen liegen in `config/shardline-common.toml` (Spawnraten, Boss-HP, Schaden, Set-Boni) und in `config/shardline-client.toml` (Nebel, Portal-Overlay).

## Fortschrittspfad

1. **Oberwelt:** Tief im Tiefenschiefer findest du selten Shard-Erz. Nachts spawnen Splitterkriecher, Aschewandler, Kristallspinnen, Schattenfüchse und Leuchtmotten.
2. **Portal:** Der Splitterrahmen kostet je Block 1 Obsidian und 4 Shard-Splitter. Du baust ihn wie ein Netherportal und aktivierst ihn mit dem Shard-Schlüssel.
3. **Shard-Set:** Es wird aus Shard-Barren hergestellt. Set-Bonus: Nachtsicht in der Splitterweite und 50 % weniger Splitterschaden.
4. **Kristallkoloss:** Gesplitterte Ritter in den Ruinen-Türmen lassen Ritterwappen fallen, daraus entsteht der Erweckungskristall. Diesen setzt du auf den Altar einer Kristallwald-Arena. Die Karte zur Arena verkauft der Splitterhändler.
5. **Kolossrüstung:** Netherit-Teil, Koloss-Schmiedevorlage und Kolosskern ergeben am Schmiedetisch ein Kolossteil. Set-Bonus: Rückstoßimmunität, 4 zusätzliche Herzen und der Kristallschild auf Taste V.
6. **Splitterkönigin:** Lege einen Kolosskern auf den Thron im Zentrum der Splitterweite bei den Koordinaten 0 / 0.

Eine Liste aller Texturen und Modelle steht in [ASSETS.md](ASSETS.md).
