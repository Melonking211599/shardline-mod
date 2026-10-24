"""Uebernimmt gelieferte Soundeffekte (WAV) als .ogg in die Mod. Aufruf: python tools/assetgen/import_sounds.py <wav-ordner>

Je Datei aus SOUNDS:
  * Stille am Anfang bis auf 10 ms kuerzen (der Klang setzt sofort ein), am Ende bis auf 120 ms mit Ausblenden;
    "still" heisst 50 dB unter dem Spitzenpegel der Datei, damit leise gelieferte Dateien ihren Nachhall behalten,
  * Spitzenpegel auf -1 dBFS anheben bzw. absenken (zu leise gelieferte Dateien waeren im Spiel kaum hoerbar),
  * Mono fuer Klaenge mit Position (Minecraft daempft nur Mono-Klaenge mit der Entfernung), Stereo nur fuer globale
    Ereignisse, die per playNotifySound direkt beim Spieler erklingen,
  * Ogg Vorbis (libvorbis, Qualitaet 5, 48 kHz) nach assets/shardline/sounds/<ziel>.ogg.
Welche Events welche Datei nutzen, steht in core/ModSounds.java (sound(...)); sounds.json schreibt der Datengenerator.
"""
from __future__ import annotations

import subprocess
import sys
import tempfile
import wave
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/shardline/sounds"

# Lieferdatei -> (Zielpfad unter sounds/, mono)
SOUNDS = {
    "Crystal Colossus — Brüllen.wav": ("entity/crystal_colossus/roar", True),
    "Crystal Colossus — Stampfen.wav": ("entity/crystal_colossus/slam", True),
    "Crystal Wave — Angriff.wav": ("ability/crystal_wave", True),
    "Crystallized Cow — death.wav": ("entity/crystallized_cow/death", True),
    "Lore Book — Öffnen.wav": ("item/lore_book/open", True),
    "Memory Crystal — Vision.wav": ("block/memory_crystal/vision", True),
    "Purification Essence — Benutzung.wav": ("item/purification_essence/use", True),
    "Purity Anchor — Aktivierung.wav": ("block/purity_anchor/activate", True),
    "Shard Call — Fähigkeit.wav": ("ability/shard_call", True),
    "Shard Fragment — Aufheben.wav": ("item/shard_fragment/pickup", True),
    "Shard Ore — Abbauen.wav": ("block/shard_ore/break", True),
    "Shard Zombie — Geräusche.wav": ("entity/shard_zombie/ambient", True),
    "Splitter Portal — Öffnen oder Durchqueren.wav": ("block/shard_portal/travel", True),
    "Splitter Queen — Magie wirken.wav": ("entity/shard_queen/cast", True),
    "Splitter Queen — Schrei.wav": ("entity/shard_queen/scream", True),
    "Splitter Queen — Teleportation.wav": ("entity/shard_queen/teleport", True),
    "Story Chapter — Beginn.wav": ("story/chapter", False),
    "Virus Dominated — Die Macht übernehmen.wav": ("event/virus_dominated", False),
    "Virus Heart — Pulsieren.wav": ("entity/virus_heart/pulse", True),
    "Virus Outbreak — Der Ausbruch.wav": ("event/virus_outbreak", False),
    "Virus Sealed — Die Welt wird gerettet.wav": ("event/virus_sealed", False),
}
SILENCE_DB = -50.0
TARGET_PEAK_DB = -1.0


def load(path: Path):
    with wave.open(str(path)) as w:
        if w.getsampwidth() != 2:
            raise ValueError(f"{path.name}: nur 16-bit-PCM wird unterstuetzt")
        data = np.frombuffer(w.readframes(w.getnframes()), dtype="<i2").astype(np.float64) / 32768.0
        return data.reshape(-1, w.getnchannels()), w.getframerate()


def process(samples: np.ndarray, rate: int, mono: bool) -> np.ndarray:
    if mono and samples.shape[1] > 1:
        samples = samples.mean(axis=1, keepdims=True)
    level = np.abs(samples).max(axis=1)
    loud = np.nonzero(level > level.max() * 10 ** (SILENCE_DB / 20))[0]
    if len(loud):
        start = max(0, loud[0] - int(0.010 * rate))
        end = min(len(samples), loud[-1] + int(0.120 * rate))
        samples = samples[start:end].copy()
        fade = min(len(samples), int(0.120 * rate))
        samples[-fade:] *= np.linspace(1.0, 0.0, fade)[:, None]
    peak = np.abs(samples).max()
    if peak > 0:
        samples *= 10 ** (TARGET_PEAK_DB / 20) / peak
    return samples


def save_ogg(samples: np.ndarray, rate: int, target: Path):
    target.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        wav = Path(tmp) / "x.wav"
        with wave.open(str(wav), "wb") as w:
            w.setnchannels(samples.shape[1])
            w.setsampwidth(2)
            w.setframerate(rate)
            w.writeframes((np.clip(samples, -1.0, 1.0) * 32767).round().astype("<i2").tobytes())
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(wav), "-map_metadata", "-1",
                        "-c:a", "libvorbis", "-q:a", "5", "-fflags", "+bitexact", str(target)], check=True)


def main():
    source = Path(sys.argv[1])
    files = {p.name: p for p in source.rglob("*.wav")}
    missing = [name for name in SOUNDS if name not in files]
    if missing:
        sys.exit(f"fehlen im Ordner: {missing}")
    for name, (target, mono) in SOUNDS.items():
        samples, rate = load(files[name])
        out = process(samples, rate, mono)
        save_ogg(out, rate, OUT / f"{target}.ogg")
        print(f"{target:36s} {'mono' if mono else 'stereo'} {len(out) / rate:5.2f} s")
    unused = sorted(set(files) - set(SOUNDS))
    if unused:
        print("nicht zugeordnet:", unused)


if __name__ == "__main__":
    main()
