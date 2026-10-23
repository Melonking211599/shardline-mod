"""Bereitet die Modelle vor: behebt UV-Ueberlappungen und ergaenzt Detail-Quader der Bosse.

Laeuft vor entities.py (siehe run_all.py). Idempotent.
"""
from __future__ import annotations

import geofix


def fix_overlaps():
    # Kristallspinne: die Rueckenkristalle lagen im UV-Bereich des Kopfes
    geofix.relocate("crystal_spider", "crystals", [0, 1, 2])
    # Gesplitterter Ritter: Schwert (Parierstange + Klinge) ueberdeckte die Armtextur
    geofix.relocate("shattered_knight", "sword", [0, 1])


def colossus_details():
    """Mehr Detail fuer den Kristallkoloss: Brustplatte, Kern, Guertel, Schulterplatten, Krone, Knie, Faustdornen."""
    n = "crystal_colossus"
    geofix.add_details(n, "body", "detail_body", [0, 24, 0], [
        {"origin": [-12, 30, -12], "size": [24, 15, 3]},                       # Brustplatte
        {"origin": [-4, 34, -14], "size": [8, 8, 2]},                          # Kerngehaeuse (leuchtet)
        {"origin": [-16, 22, -10], "size": [32, 4, 20]},                       # Guertel
        {"origin": [-3, 14, -11], "size": [6, 8, 2]},                          # Guertelschnalle
        {"origin": [-10, 46, 8], "size": [20, 4, 3]},                          # Nackenplatte am Ruecken
    ])
    geofix.add_details(n, "head", "detail_head", [0, 48, -6], [
        {"origin": [-7, 56, -15], "size": [14, 2, 3]},                         # Brauenwulst
        {"origin": [-5, 47, -16], "size": [10, 3, 3]},                         # Kiefer
        {"origin": [-5, 60, -11], "size": [2, 8, 2], "share": "spike"},       # Krone links
        {"origin": [-1, 60, -13], "size": [2, 11, 2]},                         # Krone Mitte
        {"origin": [3, 60, -11], "size": [2, 8, 2], "share": "spike"},        # Krone rechts
    ])
    geofix.add_details(n, "right_arm", "detail_right_arm", [-20, 48, 0], [
        {"origin": [-29, 46, -8], "size": [18, 8, 16]},                        # Schulterplatte
        {"origin": [-27, 38, -6], "size": [14, 3, 12], "share": "armband"},  # Armreif oben
        {"origin": [-27, 24, -6], "size": [14, 3, 12], "share": "armband"},  # Armreif unten
    ])
    geofix.add_details(n, "left_arm", "detail_left_arm", [20, 48, 0], [
        {"origin": [11, 46, -8], "size": [18, 8, 16]},
        {"origin": [13, 38, -6], "size": [14, 3, 12], "share": "armband"},
        {"origin": [13, 24, -6], "size": [14, 3, 12], "share": "armband"},
    ])
    geofix.add_details(n, "right_fist", "detail_right_fist", [-20, 14, 0], [
        {"origin": [-25, 4, -9], "size": [3, 3, 3], "share": "knuckle"},
        {"origin": [-21, 4, -9], "size": [3, 3, 3], "share": "knuckle"},
        {"origin": [-17, 4, -9], "size": [3, 3, 3], "share": "knuckle"},
    ])
    geofix.add_details(n, "left_fist", "detail_left_fist", [20, 14, 0], [
        {"origin": [15, 4, -9], "size": [3, 3, 3], "share": "knuckle"},
        {"origin": [19, 4, -9], "size": [3, 3, 3], "share": "knuckle"},
        {"origin": [23, 4, -9], "size": [3, 3, 3], "share": "knuckle"},
    ])
    geofix.add_details(n, "right_leg", "detail_right_leg", [-8, 24, 0], [
        {"origin": [-14, 14, -7], "size": [12, 7, 3], "share": "knee"},        # Knieplatte
        {"origin": [-14, 0, -7], "size": [12, 3, 4], "share": "toe"},          # Stiefelkappe
    ])
    geofix.add_details(n, "left_leg", "detail_left_leg", [8, 24, 0], [
        {"origin": [2, 14, -7], "size": [12, 7, 3], "share": "knee"},
        {"origin": [2, 0, -7], "size": [12, 3, 4], "share": "toe"},
    ])


def queen_details():
    """Mehr Detail fuer die Splitterkoenigin: Haar, Kronenzacken, Schulterkristalle, Saumsplitter, Fluegelfedern."""
    n = "shard_queen"
    geofix.add_details(n, "head", "detail_head", [0, 30, 0], [
        {"origin": [-4.5, 22, 3.5], "size": [9, 14, 2]},                       # langes Haar hinten
        {"origin": [-5, 24, -3], "size": [1, 10, 6], "share": "hairside"},    # Haarstraehnen seitlich
        {"origin": [4, 24, -3], "size": [1, 10, 6], "share": "hairside"},
        {"origin": [-6, 36, -1], "size": [1, 5, 2], "share": "tiara"},        # Tiara-Zacken
        {"origin": [5, 36, -1], "size": [1, 5, 2], "share": "tiara"},
    ])
    geofix.add_details(n, "torso", "detail_torso", [0, 18, 0], [
        {"origin": [-8, 27, -2], "size": [3, 3, 4], "share": "shoulder"},     # Schulterkristalle
        {"origin": [5, 27, -2], "size": [3, 3, 4], "share": "shoulder"},
        {"origin": [-1.5, 22, -3.6], "size": [3, 3, 1]},                       # Korsett-Edelstein
        {"origin": [-4, 28.5, -3.4], "size": [8, 1.5, 6.8]},                   # Kragen
    ])
    geofix.add_details(n, "skirt_lower", "detail_skirt", [0, 8, 0], [
        {"origin": [-5, -3, -4], "size": [2, 4, 2], "share": "hem"},
        {"origin": [3, -3, -4], "size": [2, 4, 2], "share": "hem"},
        {"origin": [-5, -3, 2], "size": [2, 4, 2], "share": "hem"},
        {"origin": [3, -3, 2], "size": [2, 4, 2], "share": "hem"},
        {"origin": [-1, -5, -4], "size": [2, 6, 2]},
        {"origin": [-1, -3, 2], "size": [2, 4, 2], "share": "hem"},
    ])
    geofix.add_details(n, "right_wing", "detail_right_wing", [-4, 26, 3], [
        {"origin": [-27, 14, 3.2], "size": [6, 16, 1]},
        {"origin": [-25, 33, 3.2], "size": [5, 10, 1]},
        {"origin": [-23, 8, 3.2], "size": [5, 8, 1]},
    ])
    geofix.add_details(n, "left_wing", "detail_left_wing", [4, 26, 3], [
        {"origin": [21, 14, 3.2], "size": [6, 16, 1]},
        {"origin": [20, 33, 3.2], "size": [5, 10, 1]},
        {"origin": [18, 8, 3.2], "size": [5, 8, 1]},
    ])


def main():
    fix_overlaps()
    colossus_details()
    queen_details()
    print("geo_prep: UV-Korrekturen und Boss-Details angewendet")


if __name__ == "__main__":
    main()
