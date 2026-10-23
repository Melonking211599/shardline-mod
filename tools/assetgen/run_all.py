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
import blocks
import bosses
import effects_gui
import entities
import geo_prep
import gui
import infected_entities
import items
import story_items
import virus_blocks


def main():
    geo_prep.main()
    anims.main()
    blocks.main()
    items.main()
    armor.main()
    armor_extras.main()
    entities.main()
    bosses.main()
    gui.main()
    # Story-Erweiterung
    virus_blocks.main()
    story_items.main()
    infected_entities.main()
    effects_gui.main()
    print("Fertig.")


if __name__ == "__main__":
    main()
