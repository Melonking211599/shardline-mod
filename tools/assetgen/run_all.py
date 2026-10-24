"""Erzeugt alle Shardline-Assets. Aufruf aus dem Projektordner:  python tools/assetgen/run_all.py

Reihenfolge: Modelle vorbereiten (UV-Korrekturen, Boss-Details), Animationen ergaenzen, dann alle Texturen.
Das Skript ist deterministisch und idempotent: zweimal ausfuehren ergibt identische Dateien.
Benoetigt: Python 3 und Pillow (pip install pillow). Ausgabe: src/main/resources/assets/shardline/
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import anims
import armor
import armor_extras
import dialogue_gui
import npc_assets
import fresh_blocks
import bosses
import bossbars
import effects_gui
import entities
import geo_prep
import gui
import infected_animals
import infected_entities
import snail
import story2_assets
import fresh_items
import fresh_update
import story_items
import fresh_virus
import virus_strains
import aftermath_assets


def main():
    geo_prep.main()
    anims.main()
    fresh_blocks.main()
    fresh_items.main()
    armor.main()
    armor_extras.main()
    entities.main()
    bosses.main()
    gui.main()
    bossbars.main()
    # Story-Erweiterung
    fresh_virus.main()
    story_items.main()
    infected_entities.main()
    effects_gui.main()
    # Splitterhandwerk
    fresh_update.main()
    snail.main()
    # Story-Erweiterung 2
    infected_animals.main()
    story2_assets.main()
    # Nach dem Ausbruch
    dialogue_gui.main()
    npc_assets.main()
    virus_strains.main()
    aftermath_assets.main()
    print("Fertig.")


if __name__ == "__main__":
    main()
