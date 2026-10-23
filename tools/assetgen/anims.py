"""Ergaenzt fehlende Animationen in den bestehenden .animation.json (nichts Vorhandenes wird veraendert).

Hinzugefuegt werden: death fuer alle Wesen, idle/attack wo sie fehlen, sowie die im Design-Dokument genannten
Boss-Namen als Alias (stomp, summon_pillars, phase_transition, projectile_barrage, clone_summon, void_zone).
Der Java-Code nutzt weiterhin die bisherigen Namen; die zusaetzlichen Clips stoeren nicht.
"""
from __future__ import annotations

import copy
import json

from common import ANIM


def load(name):
    p = ANIM / f"{name}.animation.json"
    return p, json.loads(p.read_text(encoding="utf-8"))


def save(p, d):
    p.write_text(json.dumps(d, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")


def sin(speed, amp, off=0):
    return f"math.sin(query.anim_time * {speed}) * {amp}" + (f" + {off}" if off else "")


def kf(**frames):
    return {k.replace("t", "", 1).replace("_", "."): v for k, v in frames.items()}


def fall(length, rot, pos, extra=None):
    bones = {"root": {"rotation": {"0.0": [0, 0, 0], f"{length * 0.7:.2f}": [r * 0.9 for r in rot], f"{length:.2f}": rot},
                     "position": {"0.0": [0, 0, 0], f"{length * 0.7:.2f}": [p * 0.9 for p in pos], f"{length:.2f}": pos}}}
    if extra:
        bones.update(extra)
    return {"loop": "hold_on_last_frame", "animation_length": length, "bones": bones}


def shrink(length, bone):
    return {bone: {"scale": {"0.0": [1, 1, 1], f"{length * 0.5:.2f}": [1.2, 1.2, 1.2], f"{length:.2f}": [0, 0, 0]}}}


def hover_idle(bone, amp=0.6, spd=180):
    return {"loop": True, "animation_length": 2, "bones": {bone: {"position": [0, sin(spd, amp), 0]}}}


def strike(bone, rot, length=0.4):
    return {"loop": False, "animation_length": length, "bones": {bone: {"rotation": {
        "0.0": [0, 0, 0], f"{length * 0.4:.2f}": rot, f"{length:.2f}": [0, 0, 0]}}}}


ADD = {
    "shard_creeper": {"death": fall(0.8, [0, 0, 90], [4, 0, 0], shrink(0.8, "crystals")), "attack": None},
    "crystal_golem": {"death": fall(1.2, [-80, 0, 0], [0, 0, 6])},
    "glow_moth": {"death": fall(0.8, [0, 0, 160], [0, -3, 0]), "idle": hover_idle("body", 0.4, 360),
                  "attack": strike("body", [-25, 0, 0], 0.3)},
    "ash_walker": {"death": fall(1.0, [0, 0, 90], [6, 0, 0])},
    "shard_scout": {"death": fall(0.9, [30, 0, 90], [0, -6, 0], shrink(0.9, "orbit")), "idle": hover_idle("core", 0.5, 240),
                    "attack": None},
    "void_ray": {"death": fall(1.2, [20, 0, 70], [0, -8, 0]), "idle": hover_idle("body", 0.8, 180),
                 "attack": strike("head", [30, 0, 0], 0.4)},
    "crystal_spider": {"death": fall(0.9, [0, 0, 180], [0, 2, 0])},
    "shard_trader": {"death": fall(1.0, [0, 0, 90], [6, 0, 0]), "attack": strike("arms", [-40, 0, 0], 0.4)},
    "shadow_fox": {"death": fall(0.9, [0, 0, 90], [3, 0, 0])},
    "shattered_knight": {"death": fall(1.2, [-80, 0, 0], [0, 0, 6], shrink(1.2, "shoulder_crystals"))},
    "crystal_pillar": {"death": {"loop": "hold_on_last_frame", "animation_length": 0.5, "bones": {
        "crystals": {"scale": {"0.0": [1, 1, 1], "0.5": [0.6, 0.0, 0.6]}}}}},
    "crystal_colossus": {"death": fall(3.0, [-85, 0, 0], [0, 0, 14], {**shrink(3.0, "crystals"), "head": {"rotation": {
        "0.0": [0, 0, 0], "3.00": [-25, 0, 8]}}}),
                         "stomp": "slam", "summon_pillars": "pillar", "phase_transition": "roar"},
    "shard_queen": {"death": fall(2.0, [55, 0, 25], [0, -26, 0], {**shrink(2.0, "halo"), "left_wing": {"rotation": {
        "0.0": [0, 0, 0], "2.00": [0, -40, 0]}}, "right_wing": {"rotation": {"0.0": [0, 0, 0], "2.00": [0, 40, 0]}}}),
                    "idle": hover_idle("root", 1.0, 120), "projectile_barrage": "cast", "clone_summon": "summon",
                    "phase_transition": "scream",
                    "void_zone": {"loop": False, "animation_length": 1.4, "bones": {
                        "right_arm": {"rotation": {"0.0": [0, 0, 0], "0.50": [-80, 0, -30], "1.40": [0, 0, 0]}},
                        "left_arm": {"rotation": {"0.0": [0, 0, 0], "0.50": [-80, 0, 30], "1.40": [0, 0, 0]}},
                        "skirt_lower": {"scale": {"0.0": [1, 1, 1], "0.70": [1.25, 1, 1.25], "1.40": [1, 1, 1]}}}}},
}

# Wo "attack" fehlt und None steht, wird die vorhandene Aktionsanimation als Alias genutzt
ALIAS_FOR_NONE = {"shard_creeper": "swell", "shard_scout": "shoot"}


def main():
    n = 0
    for name, clips in ADD.items():
        p, d = load(name)
        anims = d["animations"]
        for clip, val in clips.items():
            if clip in anims:
                continue
            if val is None:
                val = ALIAS_FOR_NONE.get(name)
            if isinstance(val, str):
                if val not in anims:
                    continue
                val = copy.deepcopy(anims[val])
            anims[clip] = val
            n += 1
        save(p, d)
    print(f"anims: {n} Clips ergaenzt")


if __name__ == "__main__":
    main()
