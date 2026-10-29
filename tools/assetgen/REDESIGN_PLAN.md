# Shardline – Redesign: Audit und Plan (Stand vor dem Umbau)

Bilder zum Ist-Zustand: `tools/assetgen/redesign_before_after/before/`. Vergleichsbilder mit Netherite entstehen nur lokal aus dem Minecraft-Client-Jar und werden nicht ins Repository kopiert.

## 1. Audit: was an der letzten Runde nicht gut genug ist

| Bereich | Menge | Befund (am Bild und gemessen) |
|---|---|---|
| Blöcke | 148 Dateien, 81 volle Kacheln gemessen | Shardstone, Erze, Aschestein und infizierte Blöcke zeigen bei 3×3 ein **Stempelraster**: pro Kachel dieselbe Form an derselben Stelle (`blocks_a.png`, `blocks_inf.png`). Infizierte Adern sind Einzelstempel und laufen nicht über die Kachelgrenze, also nicht „gewachsen". Infiziertes Laub und infizierte Grasseiten haben 16–19 % Einzelpixel (Streurauschen), Kristallmoos und -laub wirken im Kontaktbogen fleckig (dort nicht gemessen, weil Cutout). Ziegel, Planken und Säulen sind absichtlich periodisch (Messwert 50–100 %), das ist in Ordnung. |
| Items | 73 Dateien, davon 23 Werkzeuge, Waffen und Rüstungen | Schwert und Spitzhacke sind brauchbar. **Rüstungsicons sind gefüllte Blobs** ohne Binnenzeichnung (kein V-Ausschnitt, kein Gürtel, keine Kappen), Axtköpfe sind glatte Keile. Grundton ist hell-pastellig, Netherite dagegen dunkel mit hellem Rand (`vs_netherite_items.png`, nur lokal). Colossus-Formen sind eine Umfärbung der Shard-Formen. |
| Rüstung am Körper | 2 Sets | Hybrid aus schlankem Vanilla-Modell (Aufblähung 0,55 / 0,3) und angesetzten Zusatzwürfeln (`ArmorExtraModels`, `ArmorExtrasLayer`). Ergebnis: **Kastenrumpf mit Antennen-Kristallen** (`armor_body.png`). Die Form bleibt quadratisch, weil nur Quader dazukommen. |
| Shard Queen | 16 Bones, 36 Würfel, 128×128 | Zwei **flache Flügelplatten** (16×24×1) bestimmen die Silhouette, die Schleppe ist ein Rockstück mit vier Zapfen, der Hinterkopf eine flache dunkle Fläche (`queen_front_back.png`). Wirkt kastig, nicht „glasartig und tragisch". |
| Wesen und Bosse | 19 Modelle | Modelle bleiben. **Colossus ist der stärkste Bestand** (dunkler Stein, Cyan nur an Runen und Kern, Vorder- und Rückseite verschieden) und bekommt nur Feinschliff. Das **Virusherz** ist flach hellrosa mit Kristallbalken und Streurauschen auf der Rückseite, es passt nicht zu „Schwarzviolett/Magenta" (`bosses.png`, `colossus_front_back.png`, `virus_heart_front_back.png`). Die übrigen Wesen sind noch nicht im Detail bewertet, das folgt in Kategorie 5. |
| Bossleisten | 5 Bosse, 3 Designs | Siehe Abschnitt 3. Leiste (200×12, Rahmen 232×28) ist deutlich größer als Vanilla (182×5). |

Messung der Kachelwiederholung (Verschiebung ≥ 4 px, Zufallsübereinstimmung abgezogen): Median 15 %, 25 Kacheln über 25 %, 11 über 40 %. Das Maß erfasst Innen-Periodik, nicht das Stempelraster über Kachelgrenzen. Für das Raster kommt in `validate.py` eine Merkmalsverteilung dazu (Streuung der Akzentpixel über die Kachel) und der 3×3-Kontaktbogen bleibt Pflichtbild.

## 2. Stilbibel

Steht als Kurzfassung (8 Zeilen) am Anfang von [STYLEBIBEL.md](STYLEBIBEL.md).

## 3. Bossbar-Audit

Gefunden mit Suche nach `ServerBossEvent`, `CustomBossEvent`, `BossEvent`, `bossbar` in `src` (Java und Daten). Es gibt keine Bossbar in Story- oder Virus-Logik und keine per Datapack.

| Boss | Klasse | Namensschlüssel | Vanilla-Farbe (Phase 1 → 2 → 3) | Overlay | Eigenes Design | Gerendert in |
|---|---|---|---|---|---|---|
| Kristallkoloss | `CrystalColossusEntity` | `entity.shardline.crystal_colossus`, ab 50 % `….enraged` | Lila → Rot | NOTCHED_10 | ja | `BossBars.java` (`CustomizeGuiOverlayEvent.BossEventProgress`) |
| Splitterkönigin | `ShardQueenEntity` | `entity.shardline.shard_queen` | Rosa → Lila → Rot | NOTCHED_6 | ja | `BossBars.java` |
| Virusherz | `VirusHeartEntity` | `entity.shardline.virus_heart` | Lila → Rosa → Rot | NOTCHED_10 | ja | `BossBars.java` |
| Nachhall (Echo) | `EchoQueenEntity` (erbt Queen) | `entity.shardline.echo_queen` | Blau | NOTCHED_6 | **nein, Vanilla** | – (nicht in `STYLES`) |
| Schwärenherz | `FesteringHeartEntity` (erbt Virusherz) | `entity.shardline.festering_heart` | Gelb | NOTCHED_10 | **nein, Vanilla** | – (nicht in `STYLES`) |

Weitere Befunde:

* Die Zuordnung läuft über den Übersetzungsschlüssel des Namens. Neue Bosse fallen still auf Vanilla zurück, es gibt keine Registry und keinen Abschalter in `ShardlineClientConfig`.
* Der Herzschlag-Puls (<25 % Leben) läuft auf freiem Takt, nicht am echten Puls des Virusherzens. Das Herz pulst unregelmäßig (Abklingzeit 100 bzw. 70 Ticks plus 0–40 Zufall), also braucht die Kopplung ein Signal vom Server (Abschnitt 5, Punkt 4).
* Texturen liegen als Atlas `gui/bossbar/<boss>.png` (256×64), im Auftrag stehen Einzeldateien `gui/boss_bar/<boss>_{background,fill,overlay}.png`.

## 4. Rüstung: Entscheidung A oder B

| Kriterium | A: nur Texturen (auf Vanilla-/Slim-Modell) | B: GeckoLib-Rüstungsmodell |
|---|---|---|
| Form | Nur Schattierung und Transparenzausschnitte auf Quadern. Abgeschrägte Schulter, Helmkamm und Beinschiene sind nicht möglich, ohne wieder Zusatzwürfel anzusetzen (genau der heutige Stand). | Echte Geometrie: gedrehte Platten, Schultern mit Überstand, Helmkamm, Stiefelspitzen. |
| Aufwand | Klein: Texturen neu, Java unverändert. | Mittel: 2 Modelle (je 8 Bones, rund 30–40 Würfel) mit Atlas 128×128 und Glowmaske, 1 Renderer, Item-Anpassung. |
| Risiko | Gering. | Mittel: Pivot-Ausrichtung muss im echten Client geprüft werden (mache ich mit dem Xvfb-Verfahren von der Kesh-Reparatur, 1. und 3. Person). |
| Verlust | – | Rüstungsbesätze (Trims) werden nicht mehr gezeigt, weil GeckoLib die Vanilla-Ebene vollständig ersetzt. Verzauberungsglanz bleibt (`getArmorFoilBuffer`). |

**Empfehlung: B.** Begründung: Das Problem „quadratisch" liegt in der Geometrie, nicht in der Textur. Geprüft in GeckoLib 4.9.3 (Bytecode): `GeoArmorRenderer`, `GeoRenderProvider`, Bones `armorHead`, `armorBody`, `armorRightArm`, `armorLeftArm`, `armorRightLeg`, `armorLeftLeg`, `armorRightBoot`, `armorLeftBoot`; ein Mixin ersetzt `HumanoidArmorLayer.renderArmorPiece` für `GeoItem`-Rüstung. GeckoLib warnt, wenn auf demselben Item zusätzlich `IClientItemExtensions` hängt, das muss also weg.

Java-Änderungen unter B (minimal):

1. `item/custom/ShardlineArmorItem.java`: implementiert `GeoItem` (Instanz-Cache, keine Controller) und liefert in `createGeoRenderer` den neuen Renderer.
2. **Neu** `client/renderer/ShardlineArmorRenderer.java`: `GeoArmorRenderer<ShardlineArmorItem>` mit Modell `geo/armor/<set>.geo.json`, Textur `textures/armor/<set>.png`, `AutoGlowingGeoLayer` für `<set>_glowmask.png`.
3. `client/ShardlineClient.java`: `IClientItemExtensions`-Eintrag, Layer-Registrierung der Zusatzteile und `SlimArmorModels`-Reload-Listener entfernen, `addLayers` ohne `ArmorExtrasLayer`.
4. **Löschen** (Teil des Redesigns): `SlimArmorModels.java`, `ArmorExtrasLayer.java`, `ArmorExtraModels.java`, `tools/assetgen/armor_extras.py`.

Die Vanilla-Ebenen `models/armor/<set>_layer_1/2.png` (64×32) bleiben als Rückfall (andere Mods, die direkt die Vanilla-Ebene ansprechen) und entstehen aus derselben Palette und Formensprache. Unter B sieht sie der Spieler normalerweise nicht.

Entwurf der Sets (Netherite-Formensprache, eigenständig durch Material):

* **Shard (dunkler Violettstahl, Kristallinlays):** Helm mit T-Visier, schrägen Wangenplatten und einem nach hinten gebogenen Kamm aus 5 Kristallen. Brust mit V-förmig geteilten Platten, Kristallinlay in der Mitte, dreilagigen Schulterplatten, Kragen mit drei Zacken. Hose mit Hüftlamellen, abgeschrägten Knieschalen, Schienen. Stiefel mit Spitzkappe und Stulpe. Rückenkristalle kleiner und angelegt, nicht antennenhaft.
* **Colossus (schwerer Steinstahl, cyanfarbener Kern):** Visierhelm mit flacher Kronenplatte und Kern-Edelstein, breite Pauldrons aus geschichteten Steinplatten, Kern in einer Mulde auf der Brust, breiter Gürtel mit Schnalle, schwere Knieplatten, Stiefel mit Stahlkappe. Cyan nur am Kern und an Runen, der Rest ist dunkles Schiefer.

Tools und Waffen: gleiche Formensprache, Grundton dunkel, Kante hell, Kopf und Griff getrennt, Griffwicklung, Nietenreihe, Kristalleinsatz. `crystal_bow` (3 Zugstufen), `colossus_gauntlet`, `queens_shardblade`, `heart_of_the_expanse` bekommen eigene Silhouetten.

## 5. Shard Queen: Konzept

Leitbild: Selvaine, die Letzte der Hüter, als Siegel. Glas, hoch, still, tragisch. Alte Flügelplatten und `halo` entfallen (der Auftrag nennt sie nicht), kleine Rückensplitter erinnern an gebrochene Flügel.

```
  Vorderansicht, maßstäblich (1 Zeichen = 1 px breit, 1 Zeile = 2 px hoch, Gesamthöhe 46 px)

                      ^                   Krone: 7 Zacken (Mitte 6 px über dem Kopf, außen 3 px), Spitzen leuchten
                  ^ ^ | ^ ^
                ^ | | | | | ^
                | |_|_|_|_| |
                   |     |
                   |o   o|                Kopf 6×7×6: zwei leuchtende Augen, eine eisblaue Träne unter dem linken Auge
             / /   |, -  |   \ \          Schulterkristalle: je 2 Splitter, nach außen geneigt
              / /   _____   \ \
            . ///=.=     =.=\\\ .
          . o  |  | ( ^ ) |  |  o .       Splitterring 2 (r 12 px, 10 Splitter): o = vorn, . = hinten
               |  o (<*>) o  |            Brustmulde mit violettem Kern, leuchtet und pulsiert
             . |. | ( * ) | .| .
               |o | ( o ) | o|
               |  ---------  |.           Splitterring 1 (r 9 px, 8 Splitter) auf Taillenhöhe
       . .     |  //:   :\\  |     . .    lange Arme: Oberarm 8 + Unterarm 9 px, Hand mit 4 Kristallfingern
         o    o| //   o   \\ |o    o      Splitterring 3 (r 15 px, 12 Splitter), geneigt
               |// :   :   \\|
             '\|/ :   :   : \|/'          Rock: schmale Glasplatten, unten gezackt
               /             \
              /  :   :   :   :\
             /  v  v: v :v  v  \
                   '                      Schleppe: 4 Splitter-Segmente, hängt nach hinten (siehe Bones)
               '       '   '
              .  .   .   .    .
```

| Gruppe | Bones (Eltern → Kinder) | Würfel (ca.) |
|---|---|---|
| Wurzel und Becken | `root` → `body` (Wippen) → `waist` | 4 |
| Rock | `gown_front`, `gown_back`, `gown_left`, `gown_right` | 16 |
| Schleppe | `train_1` → `train_2` → `train_3` → `train_4` (Kette, jedes Segment wippt phasenversetzt) | 20 |
| Oberkörper | `torso`, `ribs_left`, `ribs_right`, `collar` | 14 |
| Brustkern | `core` (zwei verschränkte, gedrehte Würfel plus 4 Satelliten, pulsiert über Skalierung) | 8 |
| Kopf | `neck`, `head` (Auto-Blick, Name bleibt), `crown` (7 Zacken), `veil_left`, `veil_right` (Kristallsträhnen) | 22 |
| Arme | `shoulder_l/r`, `upperarm_l/r`, `forearm_l/r`, `hand_l/r` (je 4 Finger) | 26 |
| Rücken | `back_shards_l/r` (3 Splitter je Seite, Rest der Flügel) | 6 |
| Ringe | `ring_1`, `ring_2`, `ring_3` (8 / 10 / 12 Splitter, drehen über Molang) | 30 |

Rund 36 Bones und 145 Würfel (bisher 16 Bones, 36 Würfel). Texturgröße bleibt **128×128**, weil `queen_clone` und `echo_queen` umgefärbt aus derselben Datei entstehen und die Würfel klein sind (Box-UV-Bedarf grob 6 000–7 000 von 16 384 Pixeln; ein deterministischer Packer im Generator vergibt die UV-Plätze, `validate.py` prüft sie). Reicht der Platz nach dem Bau nicht, wechsle ich auf 256×256 und sage es. Glaskörper in Violett mit Eisblau als zweiter Akzentfarbe, Glowmaske nur an Kern, Augen, Kronenspitzen, Adern und Ringsplittern.

Animationen (Clip-Namen aus dem Code, alle bleiben):

| Clip | Verwendet von | Inhalt neu |
|---|---|---|
| `fly` (Schleife, 12 s) | Dauer-Controller von Queen, Klon, Echo | Ringe drehen per Molang (`query.anim_time`), Körper wippt, Schleppe wellt sich phasenversetzt, Arme schweben, Kern pulsiert |
| `cast` (0,6 s) | Queen, Klon, Echo | Arme stoßen nach vorn, Oberkörper lehnt, Kern blitzt |
| `summon` (1,2 s) | Queen | Arme hoch, Ringe weiten sich, Kern pulsiert stark |
| `teleport` (0,5 s) | Queen | Wurzel verdichtet sich und kippt, Ringe ziehen sich zusammen |
| `scream` (1,5 s) | Queen (Phasenwechsel) | Kopf zurück, Arme gespreizt, Rücken gewölbt, Ringe sprengen nach außen, Krone spreizt sich |
| `idle`, `death` (hält letzten Frame), `projectile_barrage`, `clone_summon`, `phase_transition`, `void_zone` | werden vom Code nicht ausgelöst (registriert sind nur `cast`, `summon`, `teleport`, `scream`), stehen aber in der Datei | bleiben erhalten und werden zum neuen Rig neu geschrieben |
| `hurt` (neu, 0,4 s) | – | gibt es bisher nicht; ich liefere den Clip mit, ausgelöst wird er nur mit der optionalen Java-Änderung (Entscheidung 5) |

Hitbox 1,2 × 3,0 und Augenhöhe 2,6 bleiben (Krone endet bei 2,9 Blöcken). Schattenradius der Queen sinkt von 0,8 auf etwa 0,6, weil der Körper schlanker ist; Klon (1,0 × 2,6, Schatten 0,5) und Echo (Schatten 0,0) bleiben. Ringe ragen bis 14 px, das ist breiter als die Hitbox und rein optisch.

**Umsetzung (Stand nach Redesign 2):** 33 Bones, 127 Würfel, UV-Bedarf 74 von 128 Zeilen (128×128 reicht). Abweichungen vom Plan: Die Ringe sind astrolabartig geneigt (Hüftring waagerecht, zwei geneigte Ringe, deren vordere Bögen tief liegen, damit Gesicht und Kern frei bleiben; sie drehen mit 30, −60 und 90 °/s auf eigenen Bones `ring_N_spin`), Kopfneigungen laufen über den zusätzlichen Bone `neck`, weil GeckoLib `head` jedes Bild zum Blickziel dreht. `hurt` läuft auf einem eigenen Controller (`hurt`), damit ein Treffer keinen Zauber abbricht; `death` und `hurt` werden jetzt von `ShardQueenEntity` ausgelöst (Entscheidung 5), der Körper bleibt 44 Ticks stehen und kippt nicht um (`getDeathMaxRotation` = 0 für Queen und Echo). Schatten der Queen 0,55, Klon 0,4. Wichtige Messung aus dem echten Client: Bone-Drehungen laufen in x umgekehrt zu Würfel-Drehungen (steht im Kopf von `queen_rig.py`).

## 6. Bossbars: Umsetzungsplan

1. Eine Design-Tabelle (Schlüssel des Namens → Design) in `BossBars.java` mit öffentlicher `register(...)`-Methode statt der festen `Map`; Fallback auf Vanilla nur für fremde Leisten.
2. Texturen je Boss: `gui/boss_bar/<boss>_background.png`, `_fill.png` (Streifen, wird per Clipping gekürzt), `_overlay.png` (Rahmen, Marken). Leiste 182 px breit wie Vanilla, Rahmen mit Kappen links und rechts. Sonderteile: Queen `_cracks.png` (drei Risstufen), Virusherz `_veins.png`. Atlas `gui/bossbar/*.png` entfällt.
3. Neue Designs für Echo (Geist: blassblau, durchscheinend, Runenrand) und Schwärenherz (Eiter: Ocker/Orange, wuchernder Rahmen). Colossus bekommt ein Schildsymbol am Phasenmarker, Queen die Risse, Virusherz Adern.
4. Herzschlag-Kopplung: `VirusHeartEntity.pulse()` sendet zusätzlich ein Entity-Event; der Client merkt sich den Zeitpunkt und pulsiert die Füllung danach. Das sind wenige Zeilen Java in `VirusHeartEntity`. Ohne diese Änderung bliebe nur ein freier Takt.
5. `ShardlineClientConfig.customBossBars` (Standard an), Lang DE und EN.
6. Vorschau-Skript rendert jede Leiste bei 100 %, 50 % und 10 % (auch GUI-Skalierung 1–4).

**Umsetzung (Stand nach Redesign 3):** Alle fünf Bosse haben ein eigenes Design (`bossbars.py`, Dateien unter `gui/boss_bar/`, Atlas entfernt). Abweichungen vom Plan: Der Rahmen ist 214 × 32 px (4 px mehr Platz oben für Kronen und Dornen, das Fenster liegt bei y 12); Füllung und Rahmen haben **eine Zeile je Phase** statt eines Farbtons per Multiplikation, weil Cyan × Rot nie Rot ergibt (Koloss: Cyan → Glut, Königin: Rosa → Violett → Rot, Virusherz: Violett → Rosa → Rot). Die Marken sind weiter feine Linien, der Koloss bekommt am 50-%-Marker ein Schild, das zerbricht. Herzschlag: `VirusHeartEntity.pulse()` sendet das Entity-Event 100, `BossBars` liest `VirusHeartEntity.lastPulseMillis()`; im echten Client sprangen die Adern im Abstand der Fieberwellen (Messung der Helligkeit pro Bild). Ein Konfigschalter `customBossBars` (Standard an) stellt auf Vanilla um.

**Umsetzung (Stand nach Redesign 4), ehrliche Kritik je Kategorie:**

* **Blöcke.** Ursache des „Stempelrasters": Alle Varianten einer Familie nutzten dasselbe 4×4-Fleckengitter, im Zufallsfeld (`tilecheck.py field`, so wie im Spiel gemischt) entstand ein Gitter aus gleichen Platten. Jetzt hat jede Variante ein anderes Gitter (4×4, 3×5, 5×3) plus Verzerrung (`pixel.field(warp=…)`); Splitterstein, Erze, Moos, Asche und Laub wirken im Feld wie gewachsener Stein. Rest: Die Zahl der Varianten (3) bleibt klein, auf sehr großen Flächen bleibt die 16-px-Wiederholung sichtbar (wie bei Vanilla-Stein); Ascheglut sitzt noch etwas gleichmäßig.
* **Befallene Blöcke.** Aderportale statt Einzelstempel (siehe ASSETS.md): Adern laufen über Kachel-, Varianten- und Blockgrenzen. Beim Test im echten Client fiel außerdem ein Fehler auf, den die Texturen nie zeigten: Die Blockmodelle nutzten für die Leuchtschicht den Render-Typ `solid`, dadurch waren alle befallenen Steinblöcke im Spiel schwarz mit leuchtenden Adern. Jetzt `cutout`. Rest: Das Adernetz ist ein sichtbares 16-px-Gitter, wenn man von oben auf große Flächen schaut; es liest sich als „aufgebrochener Boden mit leuchtenden Wurzeln", ist aber regelmäßiger als echte Wurzeln. Mehr Unregelmäßigkeit würde die Kachelgrenzen-Kontinuität kosten.
* **Wesen und Bosse.** Kristalle hatten Diagonalraster (Kristallsäule wie ein Schachbrett); `ent.M_crystal` malt jetzt drei senkrechte Facetten, Säule, Spinne, Koloss, Ritter und Händler wirken klarer. Das Virusherz ist dunkler (Magenta statt Rosa), nur die Kanten und der Kern leuchten. Rest: Das Herz bleibt die rosafarbenste Figur im Set; der Glühfalter ist in der Vorschau nur ein Streifen, weil die Flügel dünne Platten sind (nicht bewertet).
* **Items.** Kontaktbogen angesehen (35 Items auf Inventarfeld): einheitliche dunkle Konturen, klare Silhouetten, keine Änderungen nötig. Schwächste Stücke: `crystal_door` und `shard_lantern` (klein, wenig Binnenzeichnung).
* **GUI, Effekte, Partikel.** Kontaktbogen angesehen: Violett mit Gold durchgängig, passt zu den neuen Bossleisten; keine Änderungen.

## 7. Reihenfolge, Prüfungen, Abbruchregel

Reihenfolge wie im Auftrag: Rüstung und Tools → Queen → Bossbars → restliche Kategorien (Blöcke, Wesen und Bosse, Infiziertes, Items, GUI/Effekte/Partikel) → Gesamtvalidierung. Nach jeder Kategorie Kontaktbogen und zwei bis drei Sätze Kritik, einmal nachbessern. Wird etwas nach zwei Versuchen nicht klar besser, vereinfache ich es oder frage nach. Alles bleibt deterministisch (feste Seeds, `run_all.py` zweimal ergibt identische Dateien).

Prüfungen am Ende: `./gradlew build`, `runData`, `runGameTestServer`, `python tools/assetgen/validate.py`, dazu ein echter Client-Lauf unter Xvfb für Rüstung (1. und 3. Person) und Queen.

## 8. Entscheidungen, die ich von dir brauche

1. **Rüstung B** (GeckoLib) statt A. Preis: keine Rüstungsbesätze mehr an Shard- und Colossus-Rüstung.
2. **Queen ohne Flügel**, mit Ringen, Krone, Schleppe und Rückensplittern.
3. **Bossbars:** Einzeldateien `gui/boss_bar/…` und 182 px Breite statt Atlas und 200 px; die Herzschlag-Kopplung erlaubt eine kleine Java-Änderung in `VirusHeartEntity`.
4. **Colossus-Set** wird dunkler Steinstahl mit cyanfarbenem Kern statt durchgehend Türkis. Das verändert den Gesamteindruck deutlich, entspricht aber der Stilbibel.
5. Optional: Soll `ShardQueenEntity` die Clips `hurt` (neu) und `death` (existiert, wird nie gespielt) auch auslösen? Das wäre eine kleine Java-Änderung in der Queen-Klasse (Controller-Registrierung plus `triggerAnim`). Ohne Zuruf bleibt Java dort unberührt.
