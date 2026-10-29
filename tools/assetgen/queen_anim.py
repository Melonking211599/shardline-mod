"""Animationen der Splitterkoenigin zum Rig aus queen_rig.py. Aufruf: python tools/assetgen/queen_anim.py

Keyframes sind hier mit positivem x = Arm/Kopf nach vorn notiert; `kf()` dreht das x-Vorzeichen fuer GeckoLib um.

Clip-Namen wie bisher: fly (Schleife, vom Code als Dauerbewegung genutzt) sowie cast, summon, teleport, scream (per
triggerAnim). Zusaetzlich idle, death (haelt den letzten Frame), hurt, projectile_barrage, clone_summon, phase_transition und
void_zone. Kopfneigungen laufen ueber den Bone `neck`, weil GeckoLib `head` jedes Bild auf das Blickziel setzt.
Dauerbewegung mit Molang (Schleife 12 s, damit Ringdrehungen ganze Umdrehungen ergeben), Aktionen mit Keyframes.
"""
from __future__ import annotations

import copy
import json

from common import ANIM


def sin(speed, phase=0, amp=1.0):
    """Molang-Sinus in Grad. Negative Ausschlaege bekommen das Minus vor den Ausdruck (kein `* -x`)."""
    ph = f"{phase:+g}" if phase else ""
    sign = "-" if amp < 0 else ""
    return f"{sign}math.sin(query.anim_time * {speed:g}{ph}) * {abs(amp):g}"


def loop_clip(amp=1.0, length=12):
    """Schwebende Dauerbewegung. amp skaliert alle Ausschlaege (idle = ruhigere Fassung)."""
    A = amp
    b = {}
    b["root"] = {"position": [0, sin(120, 0, 0.9 * A), 0]}
    b["body"] = {"rotation": [sin(120, 40, 1.5 * A), 0, sin(60, 0, 2.0 * A)]}
    b["torso"] = {"rotation": [sin(120, 90, 1.5 * A), sin(60, 0, 3.0 * A), 0]}
    b["neck"] = {"rotation": [sin(120, 130, 2.0 * A), sin(60, 30, 3.0 * A), sin(60, 0, 1.5 * A)]}
    b["crown"] = {"rotation": [0, sin(90, 0, 4.0 * A), 0]}
    b["core"] = {"scale": [f"1 + {sin(180, 0, 0.12 * A)}"] * 3}
    for sfx, sg in (("r", 1), ("l", -1)):
        b[f"veil_{sfx}"] = {"rotation": [sin(120, -60, 8.0 * A), 0, sin(120, 0, 5.0 * A * sg)]}
        b[f"shoulder_{sfx}"] = {"rotation": [0, 0, sin(120, 0, 2.5 * A * sg)]}
        b[f"upperarm_{sfx}"] = {"rotation": [sin(120, 60, 4.0 * A), 0, sin(120, 0, -3.0 * A * sg)]}
        b[f"forearm_{sfx}"] = {"rotation": [sin(120, 120, 6.0 * A), 0, 0]}
        b[f"hand_{sfx}"] = {"rotation": [sin(120, 160, 6.0 * A), 0, sin(240, 0, 3.0 * A * sg)]}
        b[f"back_{sfx}"] = {"rotation": [0, 0, sin(180, 0, 3.0 * A * sg)]}
    b["gown_front"] = {"rotation": [sin(120, 20, 3.0 * A), 0, 0]}
    b["gown_back"] = {"rotation": [sin(120, 20, -3.0 * A), 0, 0]}
    b["gown_left"] = {"rotation": [0, 0, sin(120, 0, 2.0 * A)]}
    b["gown_right"] = {"rotation": [0, 0, sin(120, 0, -2.0 * A)]}
    for i, (ph, amp_t) in enumerate(((-30, 6.0), (-80, 9.0), (-130, 12.0)), start=1):
        b[f"train_{i}"] = {"rotation": [sin(120, ph, amp_t * A), 0, 0]}
    # Ringe: Drehung in der eigenen Ebene (ganze Umdrehungen in 12 s), leichtes Taumeln der Neigung
    b["ring_1_spin"] = {"rotation": [0, "query.anim_time * 30", 0]}
    b["ring_2_spin"] = {"rotation": [0, "-query.anim_time * 60", 0]}
    b["ring_3_spin"] = {"rotation": [0, "query.anim_time * 90", 0]}
    b["ring_2"] = {"rotation": [sin(60, 0, 3.0 * A), 0, sin(90, 0, 3.0 * A)]}
    b["ring_3"] = {"rotation": [sin(90, 0, -3.0 * A), 0, sin(60, 0, 3.0 * A)]}
    return {"loop": True, "animation_length": length, "bones": b}


def kf(table):
    """{bone: {property: {zeit: [x, y, z]}}} -> Bedrock-Keyframes mit Zeitschluesseln als Text."""
    out = {}
    for bone, props in table.items():
        out[bone] = {}
        for prop, frames in props.items():
            if prop == "rotation":      # Eingabe in Vorschau-Konvention (positives x = Arm nach vorn), Spiel: x umgekehrt
                out[bone][prop] = {f"{t:.2f}": [-v[0] + 0.0, v[1], v[2]] for t, v in frames.items()}
            else:
                out[bone][prop] = {f"{t:.2f}": list(v) for t, v in frames.items()}
    return out


def both(table_r, extra=None):
    """Spiegelt Eintraege fuer rechts (_r) auf links (_l): z-Drehung wechselt das Vorzeichen."""
    out = {}
    for bone, props in table_r.items():
        out[bone] = props
        left = {}
        for prop, frames in props.items():
            if prop == "rotation":
                left[prop] = {t: (v[0], -v[1], -v[2]) for t, v in frames.items()}
            else:
                left[prop] = dict(frames)
        out[bone[:-2] + "_l"] = left
    if extra:
        out.update(extra)
    return out


Z = (0, 0, 0)
ONE = (1, 1, 1)


def scale_k(t0, t1, v, t2):
    return {0.0: ONE, t0: (v, v, v), t1: (v, v, v), t2: ONE}


def clips():
    c = {"fly": loop_clip(1.0), "idle": loop_clip(0.45)}

    c["cast"] = {"loop": False, "animation_length": 0.6, "bones": kf(both({
        "upperarm_r": {"rotation": {0.0: Z, 0.15: (80, 0, -6), 0.6: Z}},
        "forearm_r": {"rotation": {0.0: Z, 0.15: (14, 0, 0), 0.6: Z}},
        "hand_r": {"rotation": {0.0: Z, 0.15: (-20, 0, 0), 0.6: Z}},
    }, {
        "torso": {"rotation": {0.0: Z, 0.15: (-10, 0, 0), 0.6: Z}},
        "neck": {"rotation": {0.0: Z, 0.15: (6, 0, 0), 0.6: Z}},
        "core": {"scale": {0.0: ONE, 0.15: (1.8, 1.8, 1.8), 0.6: ONE}},
    }))}
    c["summon"] = {"loop": False, "animation_length": 1.2, "bones": kf(both({
        "upperarm_r": {"rotation": {0.0: Z, 0.4: (150, 0, -25), 0.9: (150, 0, -25), 1.2: Z}},
        "forearm_r": {"rotation": {0.0: Z, 0.4: (10, 0, 0), 0.9: (10, 0, 0), 1.2: Z}},
    }, {
        "torso": {"rotation": {0.0: Z, 0.4: (8, 0, 0), 0.9: (8, 0, 0), 1.2: Z}},
        "neck": {"rotation": {0.0: Z, 0.4: (16, 0, 0), 0.9: (16, 0, 0), 1.2: Z}},
        "core": {"scale": {0.0: ONE, 0.45: (1.9, 1.9, 1.9), 0.8: (1.4, 1.4, 1.4), 1.2: ONE}},
        "crown": {"scale": {0.0: ONE, 0.4: (1.0, 1.15, 1.0), 0.9: (1.0, 1.15, 1.0), 1.2: ONE}},
        "ring_1": {"scale": scale_k(0.4, 0.9, 1.3, 1.2)},
        "ring_2": {"scale": scale_k(0.4, 0.9, 1.3, 1.2)},
        "ring_3": {"scale": scale_k(0.4, 0.9, 1.3, 1.2)},
    }))}
    c["teleport"] = {"loop": False, "animation_length": 0.5, "bones": kf({
        "body": {"scale": {0.0: ONE, 0.2: (0.35, 1.2, 0.35), 0.5: ONE}},
        "ring_1": {"scale": {0.0: ONE, 0.2: (0.4, 0.4, 0.4), 0.5: ONE}},
        "ring_2": {"scale": {0.0: ONE, 0.2: (0.4, 0.4, 0.4), 0.5: ONE}},
        "ring_3": {"scale": {0.0: ONE, 0.2: (0.4, 0.4, 0.4), 0.5: ONE}},
        "core": {"scale": {0.0: ONE, 0.2: (0.3, 0.3, 0.3), 0.5: ONE}},
    })}
    c["scream"] = {"loop": False, "animation_length": 1.5, "bones": kf(both({
        "upperarm_r": {"rotation": {0.0: Z, 0.3: (20, 0, -75), 1.2: (20, 0, -75), 1.5: Z}},
        "forearm_r": {"rotation": {0.0: Z, 0.3: (-10, 0, 0), 1.2: (-10, 0, 0), 1.5: Z}},
    }, {
        "neck": {"rotation": {0.0: Z, 0.3: (32, 0, 0), 1.2: (32, 0, 0), 1.5: Z}},
        "torso": {"rotation": {0.0: Z, 0.3: (14, 0, 0), 1.2: (14, 0, 0), 1.5: Z}},
        "crown": {"scale": {0.0: ONE, 0.3: (1.25, 1.25, 1.25), 1.2: (1.25, 1.25, 1.25), 1.5: ONE}},
        "core": {"scale": {0.0: ONE, 0.3: (2.1, 2.1, 2.1), 1.2: (2.1, 2.1, 2.1), 1.5: ONE}},
        "ring_1": {"scale": scale_k(0.4, 1.1, 1.5, 1.5)},
        "ring_2": {"scale": scale_k(0.4, 1.1, 1.5, 1.5)},
        "ring_3": {"scale": scale_k(0.4, 1.1, 1.5, 1.5)},
    }))}
    c["hurt"] = {"loop": False, "animation_length": 0.4, "bones": kf({
        "root": {"rotation": {0.0: Z, 0.1: (8, 0, 0), 0.4: Z}},
        "neck": {"rotation": {0.0: Z, 0.1: (14, 0, 6), 0.4: Z}},
        "ring_1": {"scale": {0.0: ONE, 0.1: (1.15, 1.15, 1.15), 0.4: ONE}},
        "ring_2": {"scale": {0.0: ONE, 0.1: (1.15, 1.15, 1.15), 0.4: ONE}},
        "core": {"scale": {0.0: ONE, 0.1: (0.6, 0.6, 0.6), 0.4: ONE}},
    })}
    c["death"] = {"loop": "hold_on_last_frame", "animation_length": 2.0, "bones": kf(both({
        "upperarm_r": {"rotation": {0.0: Z, 2.0: (-10, 0, -14)}},
    }, {
        "root": {"rotation": {0.0: Z, 2.0: (32, 0, 8)}, "position": {0.0: Z, 2.0: (0, -9, 0)}},
        "neck": {"rotation": {0.0: Z, 2.0: (40, 0, 0)}},
        "core": {"scale": {0.0: ONE, 0.6: (1.6, 1.6, 1.6), 2.0: (0.15, 0.15, 0.15)}},
        "ring_1": {"scale": {0.0: ONE, 2.0: (0.3, 0.3, 0.3)}},
        "ring_2": {"scale": {0.0: ONE, 2.0: (0.3, 0.3, 0.3)}},
        "ring_3": {"scale": {0.0: ONE, 2.0: (0.3, 0.3, 0.3)}},
    }))}
    c["void_zone"] = {"loop": False, "animation_length": 1.4, "bones": kf(both({
        "upperarm_r": {"rotation": {0.0: Z, 0.5: (70, 0, -22), 0.9: (70, 0, -22), 1.4: Z}},
    }, {
        "gown_front": {"rotation": {0.0: Z, 0.7: (16, 0, 0), 1.4: Z}},
        "gown_back": {"rotation": {0.0: Z, 0.7: (-16, 0, 0), 1.4: Z}},
        "gown_left": {"rotation": {0.0: Z, 0.7: (0, 0, 16), 1.4: Z}},
        "gown_right": {"rotation": {0.0: Z, 0.7: (0, 0, -16), 1.4: Z}},
        "torso": {"rotation": {0.0: Z, 0.5: (-8, 0, 0), 1.4: Z}},
    }))}
    c["projectile_barrage"] = copy.deepcopy(c["cast"])
    c["clone_summon"] = copy.deepcopy(c["summon"])
    c["phase_transition"] = copy.deepcopy(c["scream"])
    return c


def main():
    data = {"format_version": "1.8.0", "animations": clips()}
    (ANIM / "shard_queen.animation.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(f"queen_anim: {len(data['animations'])} Clips ({', '.join(data['animations'])})")


if __name__ == "__main__":
    main()
