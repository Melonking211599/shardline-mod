"""Prueft die Assets gegen ASSETS.md: Dateien vorhanden, Groessen, UV-Grenzen, Animations-Bones. Aufruf: python tools/assetgen/validate.py"""
import json, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from PIL import Image
from common import ANIM, ASSETS, GEO, RESOURCE_ROOT, TEX
from uvlib import box_extent, box_rects

errors = []
def need(path, size=None):
    if not path.exists():
        errors.append(f"fehlt: {path}"); return
    if size:
        w, h = Image.open(path).size
        if (w, h) != size: errors.append(f"Groesse {w}x{h} statt {size}: {path.name}")

ENT = {"shard_creeper": 64, "crystal_golem": 128, "glow_moth": 64, "ash_walker": 64, "shard_scout": 64, "void_ray": 128,
       "crystal_spider": 64, "shard_trader": 64, "shadow_fox": 64, "shattered_knight": 64, "crystal_colossus": 256,
       "shard_queen": 128, "crystal_pillar": 64, "crystal_snail": 64, "rhea": 64,
       "veyl": 64, "ilsa": 64, "kesh": 64}
GLOW = {"shard_creeper", "crystal_golem", "glow_moth", "shard_scout", "void_ray", "crystal_spider", "shadow_fox",
        "shattered_knight", "crystal_colossus", "shard_queen", "crystal_pillar", "crystal_snail", "rhea",
        "veyl", "ilsa", "kesh"}
for n, s in ENT.items():
    need(TEX / "entity" / f"{n}.png", (s, s))
    if n in GLOW: need(TEX / "entity" / f"{n}_glowmask.png", (s, s))
    g = json.loads((GEO / f"{n}.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
    if g["description"]["texture_width"] != s: errors.append(f"geo-Breite {n}")
    bones = {b["name"] for b in g["bones"]}
    for b in g["bones"]:
        if b.get("parent") and b["parent"] not in bones: errors.append(f"{n}: Parent fehlt {b['parent']}")
        for c in b.get("cubes", []):
            e = box_extent(c["uv"][0], c["uv"][1], *c["size"])
            if e[0] > s or e[1] > s: errors.append(f"{n}/{b['name']}: UV ausserhalb {e}")
    a = json.loads((ANIM / f"{n}.animation.json").read_text(encoding="utf-8"))["animations"]
    for clip, v in a.items():
        for bn in v.get("bones", {}):
            if bn not in bones: errors.append(f"{n}/{clip}: Bone {bn} nicht im Modell")
    for need_clip in ("idle", "walk", "fly", "death"):
        pass
need(TEX / "entity" / "crystal_golem_angry.png", (128, 128)); need(TEX / "entity" / "queen_clone.png", (128, 128))
blocks = """shardstone shardstone_bricks cracked_shardstone_bricks shard_ore deepslate_shard_ore shard_block glow_crystal_block
glow_crystal_cluster crystal_moss crystal_moss_side ash_block crystal_log crystal_log_top crystal_planks crystal_leaves
crystal_sapling glowbloom shard_fern glowcap luminous_berry_bush crystal_web shard_portal_frame colossus_altar
colossus_altar_top queen_altar queen_altar_top colossus_trophy queen_trophy""".split()
for b in blocks: need(TEX / "block" / f"{b}.png", (16, 16))
need(TEX / "block" / "shard_portal.png", (16, 128)); need(TEX / "block" / "shard_portal.png.mcmeta")
items = """shard_fragment raw_shard shard_ingot ash_dust scout_eye void_ray_hide shadow_pelt knight_crest luminous_berry shard_key
colossus_summoning_crystal colossus_core colossus_upgrade_smithing_template heart_of_the_expanse queens_shardblade
colossus_gauntlet crystal_bow crystal_bow_pulling_0 crystal_bow_pulling_1 crystal_bow_pulling_2""".split()
for s in ("shard", "colossus"):
    for p in ("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel"): items.append(f"{s}_{p}")
for i in items: need(TEX / "item" / f"{i}.png", (16, 16))
for s in ("shard", "colossus"):
    for l in (1, 2): need(TEX / "models/armor" / f"{s}_layer_{l}.png", (64, 32))
for i in range(4):
    need(TEX / "particle" / f"shard_sparkle_{i}.png", (8, 8)); need(TEX / "particle" / f"void_mote_{i}.png", (8, 8))
need(TEX / "environment/splitterweite_sky.png", (128, 128)); need(TEX / "environment/splitterweite_moon.png", (64, 64))
need(TEX / "misc/shard_portal_overlay.png", (256, 256)); need(TEX / "gui/advancements/backgrounds/shardline.png", (16, 16))
need(RESOURCE_ROOT / "shardline_logo.png", (256, 128))

# ---- Bossleisten: je Boss Hintergrund 182x10, Fuellung 198 x (10*Phasen), Rahmen 214 x (32*Phasen); Sonderteile
for boss, phases in (("crystal_colossus", 2), ("shard_queen", 3), ("virus_heart", 3), ("echo_queen", 1), ("festering_heart", 1)):
    need(TEX / "gui/boss_bar" / f"{boss}_background.png", (182, 10))
    need(TEX / "gui/boss_bar" / f"{boss}_fill.png", (198, 10 * phases))
    need(TEX / "gui/boss_bar" / f"{boss}_overlay.png", (214, 32 * phases))
need(TEX / "gui/boss_bar/shard_queen_cracks.png", (182, 30))
need(TEX / "gui/boss_bar/virus_heart_veins.png", (182, 10)); need(TEX / "gui/boss_bar/festering_heart_veins.png", (182, 10))
need(TEX / "gui/boss_bar/crystal_colossus_shield.png", (20, 10))
for boss in ("crystal_colossus", "shard_queen", "virus_heart", "echo_queen", "festering_heart"):   # Fenster im Rahmen bleibt frei
    im = Image.open(TEX / "gui/boss_bar" / f"{boss}_overlay.png").convert("RGBA")
    for y in range(12, 22):
        for x in range(16, 198):
            if im.getpixel((x, y))[3]:
                errors.append(f"{boss}_overlay.png: Fenster nicht frei bei {x},{y}")
                break

# ---- Story-Erweiterung
story_blocks = "infected_stone infected_dirt infected_grass_top infected_grass_side infected_log infected_log_top infected_leaves infected_sand".split()
for b in story_blocks:
    need(TEX / "block" / f"{b}.png", (16, 16))
    need(TEX / "block" / f"{b}_glow.png", (16, 64) if b in ("infected_grass_top", "infected_grass_side", "infected_leaves") else (16, 16))
for b in ("infected_grass_top", "infected_grass_side", "infected_leaves"):
    need(TEX / "block" / f"{b}_glow.png.mcmeta")
for b in ("memory_crystal", "purity_anchor"): need(TEX / "block" / f"{b}.png", (16, 16))
need(TEX / "block" / "purity_anchor_crystal.png"); need(TEX / "block" / "purity_anchor_crystal.png.mcmeta")
for i in ("lore_book", "purification_essence", "breach_core"): need(TEX / "item" / f"{i}.png", (16, 16))
need(TEX / "entity" / "keeper_cape.png", (64, 32))
need(TEX / "entity" / "virus_heart.png", (128, 128)); need(TEX / "entity" / "virus_heart_glowmask.png", (128, 128))
need(TEX / "entity" / "shard_zombie.png", (64, 64)); need(TEX / "entity" / "shard_zombie_glowmask.png", (64, 64))
need(TEX / "entity" / "crystallized_cow.png", (64, 32)); need(TEX / "entity" / "crystallized_cow_glowmask.png", (64, 32))
need(TEX / "mob_effect" / "shard_fever.png", (18, 18))
for i in range(4): need(TEX / "particle" / f"virus_spore_{i}.png", (8, 8))
need(TEX / "misc" / "breach_overlay.png", (256, 256)); need(TEX / "gui/advancements/backgrounds/story.png", (16, 16))
hg = json.loads((GEO / "virus_heart.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
hb = {b["name"] for b in hg["bones"]}
ha = json.loads((ANIM / "virus_heart.animation.json").read_text(encoding="utf-8"))["animations"]
for clip in ("idle", "spawn", "attack_tendrils", "summon_infected", "hurt", "death"):
    if clip not in ha: errors.append(f"virus_heart: Clip {clip} fehlt")

# ---- Splitterhandwerk
craft_blocks = """polished_shardstone chiseled_shardstone_bricks crystal_door_top crystal_door_bottom crystal_trapdoor crystal_glass
crystal_glass_pane_top shard_lantern""".split()
for b in craft_blocks: need(TEX / "block" / f"{b}.png", (16, 16))
craft_items = "crystal_door shard_lantern crystal_slime glowcap_stew crystal_candy berry_tart resonance_compass".split()
for i in craft_items: need(TEX / "item" / f"{i}.png", (16, 16))
blocks += craft_blocks
items += craft_items

# ---- Story-Erweiterung 2
for n, size in (("crystallized_sheep", (64, 32)), ("crystallized_sheep_fur", (64, 32)), ("crystallized_pig", (64, 32)),
                ("crystallized_villager", (64, 64))):
    need(TEX / "entity" / f"{n}.png", size); need(TEX / "entity" / f"{n}_glowmask.png", size)
for n, size in (("crystallized_cow", (64, 32)), ("crystallized_sheep", (64, 32)), ("crystallized_pig", (64, 32)),
                ("crystallized_villager", (64, 64)), ("shard_zombie", (64, 64))):
    need(TEX / "entity" / f"{n}_collar.png", size)
# ---- Nach dem Ausbruch: Virusstaemme (virus_strains.py)
import virus_strains
for name, (_fn, variants, animate) in virus_strains.BLOCKS.items():
    for suffix in [""] + [f"_v{n}" for n in range(2, variants + 1)]:
        need(TEX / "block" / f"{name}{suffix}.png", (16, 16))
        need(TEX / "block" / f"{name}{suffix}_glow.png", (16, 64) if animate else (16, 16))
        if animate: need(TEX / "block" / f"{name}{suffix}_glow.png.mcmeta")
for n, size in (("crystallized_piglin", (64, 64)), ("crystallized_enderman", (64, 32))):
    for part in ("", "_glowmask", "_collar"):
        need(TEX / "entity" / f"{n}{part}.png", size)
# ---- Nach dem Ausbruch: Kapitel 7-9 (aftermath_assets.py)
for i in ("echo_shard", "keeper_tuning_fork", "chronicle_journal"):
    need(TEX / "item" / f"{i}.png", (16, 16)); items.append(i)
for key in ("7", "8_seal", "8_dominate", "9_seal", "9_dominate"):
    need(TEX / "item" / f"chapter_emblem_{key}.png", (16, 16)); items.append(f"chapter_emblem_{key}")
need(TEX / "block" / "festering_node.png", (16, 16)); need(TEX / "block" / "festering_node_glow.png", (16, 64))
need(TEX / "block" / "festering_node_glow.png.mcmeta")
for n, size in (("echo_queen", (128, 128)), ("festering_heart", (128, 128))):
    need(TEX / "entity" / f"{n}.png", size); need(TEX / "entity" / f"{n}_glowmask.png", size)
story2_blocks = """decision_altar_side decision_altar_side_glow decision_altar_top decision_altar_top_glow decision_altar_bottom
keeper_ruin_bricks keeper_ruin_pillar keeper_ruin_pillar_top keeper_rune_slab keeper_rune_slab_glow keeper_rune_slab_side""".split()
for b in story2_blocks: need(TEX / "block" / f"{b}.png", (16, 16))
blocks += story2_blocks
need(TEX / "mob_effect" / "immunity.png", (18, 18))
need(TEX / "item" / "lore_page.png", (16, 16)); items.append("lore_page")
need(TEX / "gui" / "trader_dialogue.png", (256, 128))
for n in range(1, 7):
    need(TEX / "item" / f"chapter_emblem_{n}.png", (16, 16)); items.append(f"chapter_emblem_{n}")
need(TEX / "gui" / "chapter_card.png", (256, 128))
need(TEX / "gui" / "lore_page.png", (256, 256))
need(TEX / "gui" / "ending_choice.png", (512, 256))
for i in range(4): need(TEX / "particle" / f"purity_mote_{i}.png", (8, 8))
need(RESOURCE_ROOT / "assets" / "shardline" / "particles" / "purity_mote.json")
# ---- Nach dem Ausbruch: Dialog- und Journal-GUI, Porträts
for name, size in (("dialogue/frame", (32, 32)), ("dialogue/portrait_frame", (80, 80)), ("dialogue/nameplate", (48, 16)),
                   ("dialogue/button", (64, 80)), ("dialogue/icons", (32, 16)), ("journal/background", (256, 192)),
                   ("journal/tabs", (128, 56)), ("portraits/unknown", (64, 64))):
    need(TEX / "gui" / f"{name}.png", size)
for portrait in sorted((TEX / "gui" / "portraits").glob("*.png")):
    need(portrait, (64, 64))
need(TEX / "entity" / "kesh_healed.png", (64, 64))
# Leere Glowmasks lassen GeckoLib beim ersten Rendern abstuerzen ("must have at least one pixel")
for mask in sorted((TEX / "entity").glob("*_glowmask.png")):
    if Image.open(mask).convert("RGBA").getchannel("A").getbbox() is None:
        errors.append(f"{mask.name}: Glowmask ist leer")
for npc in ("rhea", "veyl", "ilsa", "kesh"):
    for pose in ("neutral", "happy", "sad", "angry"):
        need(TEX / "gui" / "portraits" / f"{npc}_{pose}.png", (64, 64))
    clips = json.loads((ANIM / f"{npc}.animation.json").read_text(encoding="utf-8"))["animations"]
    for clip in ("idle", "walk", "talk", "hurt", "sleep", "death"):
        if clip not in clips: errors.append(f"{npc}: Animation {clip} fehlt")
    if clips.get("death", {}).get("loop") != "hold_on_last_frame": errors.append(f"{npc}: death muss den letzten Frame halten")

# ---- Polish-Pruefungen: Entity-Texturen und Kachelbloecke
def opaque(img):
    px = img.convert("RGBA").load()
    return {(x, y) for y in range(img.height) for x in range(img.width) if px[x, y][3] > 0}


for n in ENT:
    g = json.loads((GEO / f"{n}.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
    inside = set()
    for b in g["bones"]:
        for c in b.get("cubes", []):
            w, h, d = (int(round(v)) for v in c["size"])
            u, v = int(c["uv"][0]), int(c["uv"][1])
            for (x0, y0, fw, fh) in box_rects(u, v, w, h, d).values():
                inside |= {(x, y) for y in range(y0, y0 + fh) for x in range(x0, x0 + fw)}
    base = Image.open(TEX / "entity" / f"{n}.png")
    stray = opaque(base) - inside
    if stray: errors.append(f"{n}: {len(stray)} bemalte Pixel ausserhalb der UV-Boxen")
    gm = TEX / "entity" / f"{n}_glowmask.png"
    if gm.exists():
        glow = Image.open(gm).convert("RGBA")
        missing = opaque(glow) - opaque(base)
        if missing: errors.append(f"{n}: Glowmaske leuchtet auf {len(missing)} Pixeln ohne Basistextur")
        bp, gp = base.convert("RGBA").load(), glow.load()
        off = sum(1 for (x, y) in opaque(glow) if bp[x, y][:3] != gp[x, y][:3])
        if off: errors.append(f"{n}: Glowmaske weicht auf {off} Pixeln farblich von der Basistextur ab")
for name in ("shardstone", "crystal_moss", "ash_block", "crystal_leaves", "shard_ore", "deepslate_shard_ore"):
    for suffix in ("_v2", "_v3"):
        f = TEX / "block" / f"{name}{suffix}.png"
        if f.exists(): need(f, (16, 16))
alpha = Image.open(TEX / "block" / "crystal_leaves.png").convert("RGBA")
holes = alpha.getchannel("A").histogram()[0]
if holes < 12: errors.append("crystal_leaves: zu wenige Luecken fuer cutout_mipped")
def seam_ratio(path):
    """Farbsprung ueber den Kachelrand geteilt durch den mittleren Sprung benachbarter Pixel im Inneren (<= 1: unsichtbar)."""
    im = Image.open(path).convert("RGBA")
    if im.height > im.width:
        im = im.crop((0, 0, im.width, im.width))
    px, n = im.load(), im.width
    d = lambda a, b: (0 if a[3] == b[3] == 0 else 150 if a[3] == 0 or b[3] == 0 else sum(abs(a[i] - b[i]) for i in range(3)))
    inner = (sum(d(px[x, y], px[x + 1, y]) for y in range(n) for x in range(n - 1)) / (n * (n - 1))
             + sum(d(px[x, y], px[x, y + 1]) for y in range(n - 1) for x in range(n)) / ((n - 1) * n)) / 2
    edge = (sum(d(px[n - 1, y], px[0, y]) for y in range(n)) / n + sum(d(px[x, n - 1], px[x, 0]) for x in range(n)) / n) / 2
    return edge / max(inner, 1.0)


for fam in ("shardstone", "shard_ore", "deepslate_shard_ore", "crystal_moss", "ash_block", "crystal_leaves", "infected_stone",
            "infected_dirt", "infected_grass_top", "infected_sand", "infected_leaves", "infected_deepslate", "infected_netherrack",
            "infected_end_stone", "infected_blackstone", "infected_soul_soil", "infected_wart_block"):
    for suffix in ("", "_v2", "_v3"):
        f = TEX / "block" / f"{fam}{suffix}.png"
        if f.exists() and seam_ratio(f) > (1.6 if "ore" in fam else 1.3):    # Erze: harte Einzelmerkmale, daher grosszuegiger
            errors.append(f"{fam}{suffix}: sichtbare Kachelnaht (Verhaeltnis {seam_ratio(f):.2f})")
for suffix in ("", "_v2", "_v3"):
    leaf, glow = TEX / "block" / f"infected_leaves{suffix}.png", TEX / "block" / f"infected_leaves{suffix}_glow.png"
    if leaf.exists() and glow.exists():
        lp, gi = Image.open(leaf).convert("RGBA").load(), Image.open(glow).convert("RGBA")
        gp = gi.load()
        bad = sum(1 for y in range(gi.height) for x in range(gi.width) if gp[x, y][3] and lp[x, y % 16][3] == 0)
        if bad: errors.append(f"infected_leaves{suffix}: {bad} Leuchtpixel auf transparentem Laub")

# ---- Ruestungsmodelle (GeckoLib, armor_models.py): Bones mit Standardpivots, UV-Boxen ohne Ueberlappung, Glowmaske
from armorkit import PIVOTS as ARMOR_PIVOTS
for s in ("shard", "colossus"):
    gp = ASSETS / "geo" / "armor" / f"{s}.geo.json"
    need(gp); need(ASSETS / "animations" / "armor" / f"{s}.animation.json")
    for layer in (1, 2): need(TEX / "models/armor" / f"{s}_layer_{layer}.png", (64, 32))
    if not gp.exists(): continue
    g = json.loads(gp.read_text(encoding="utf-8"))["minecraft:geometry"][0]
    W, H = int(g["description"]["texture_width"]), int(g["description"]["texture_height"])
    need(TEX / "armor" / f"{s}.png", (W, H)); need(TEX / "armor" / f"{s}_glowmask.png", (W, H))
    names = [b["name"] for b in g["bones"]]
    if sorted(names) != sorted(ARMOR_PIVOTS): errors.append(f"armor/{s}: Bones {names} statt {sorted(ARMOR_PIVOTS)}")
    owner, claimed = {}, set()
    for b in g["bones"]:
        if tuple(b["pivot"]) != tuple(ARMOR_PIVOTS.get(b["name"], ())): errors.append(f"armor/{s}/{b['name']}: Pivot {b['pivot']} statt {list(ARMOR_PIVOTS.get(b['name'], []))}")
        for i, c in enumerate(b.get("cubes", [])):
            if any(abs(v - round(v)) > 1e-6 for v in c["size"]): errors.append(f"armor/{s}/{b['name']}#{i}: Groesse nicht ganzzahlig")
            w, h, d = (int(round(v)) for v in c["size"])
            u, vv = int(c["uv"][0]), int(c["uv"][1])
            ext = box_extent(u, vv, w, h, d)
            if ext[0] > W or ext[1] > H: errors.append(f"armor/{s}/{b['name']}#{i}: UV ausserhalb {ext} von {W}x{H}")
            for (x0, y0, fw, fh) in box_rects(u, vv, w, h, d).values():
                for y in range(y0, y0 + fh):
                    for x in range(x0, x0 + fw):
                        if (x, y) in owner and owner[(x, y)] != (b["name"], i): errors.append(f"armor/{s}: UV ueberlappt bei {(x, y)} ({b['name']}#{i} und {owner[(x, y)]})"); break
                        owner[(x, y)] = (b["name"], i)
    base = Image.open(TEX / "armor" / f"{s}.png").convert("RGBA")
    stray = opaque(base) - set(owner)
    if stray: errors.append(f"armor/{s}: {len(stray)} bemalte Pixel ausserhalb der UV-Boxen")
    glow = Image.open(TEX / "armor" / f"{s}_glowmask.png").convert("RGBA")
    if glow.getchannel("A").getbbox() is None: errors.append(f"armor/{s}: Glowmaske ist leer")
    bp, gpx = base.load(), glow.load()
    off = [(x, y) for (x, y) in opaque(glow) if bp[x, y] != gpx[x, y]]
    if off: errors.append(f"armor/{s}: Glowmaske weicht auf {len(off)} Pixeln von der Basistextur ab")

# ---- Eigene Klaenge: jede in sounds.json genannte Datei muss als .ogg existieren
sounds_json = RESOURCE_ROOT.parent.parent / "generated" / "resources" / "assets" / "shardline" / "sounds.json"
if sounds_json.exists():
    for event, definition in json.loads(sounds_json.read_text(encoding="utf-8")).items():
        for sound in definition.get("sounds", []):
            name = sound if isinstance(sound, str) else sound.get("name", "")
            if (isinstance(sound, str) or sound.get("type", "file") == "file") and name.startswith("shardline:"):
                need(RESOURCE_ROOT / "assets" / "shardline" / "sounds" / (name.split(":", 1)[1] + ".ogg"))

print("\n".join(errors) if errors else f"OK: alle Pruefungen bestanden ({len(ENT)} Wesen, {len(blocks)+1} Bloecke, {len(items)} Items)")
sys.exit(1 if errors else 0)
