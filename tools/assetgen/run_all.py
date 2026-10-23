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
import blocks
import bosses
import entities
import geo_prep
import gui
import items


def main():
    geo_prep.main()
    anims.main()
    blocks.main()
    items.main()
    armor.main()
    entities.main()
    bosses.main()
    gui.main()
    print("Fertig.")


if __name__ == "__main__":
    main()
