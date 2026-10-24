package eu.shardline.mod.gametest;

import com.mojang.datafixers.util.Pair;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModEnchantments;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.mob.CrystalSnailEntity;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.ending.EndingManager;
import eu.shardline.mod.story.lore.LoreBooks;
import eu.shardline.mod.story.lore.Visions;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.PurityAnchors;
import eu.shardline.mod.virus.VirusSpreadManager;
import eu.shardline.mod.world.dimension.ModDimensions;
import eu.shardline.mod.world.noise.ModNoiseSettings;
import eu.shardline.mod.world.portal.ShardPortalShape;
import eu.shardline.mod.world.structure.AshCryptPiece;
import eu.shardline.mod.world.structure.ColossusArenaPiece;
import eu.shardline.mod.world.structure.CrystalGeodePiece;
import eu.shardline.mod.world.structure.KeeperShrinePiece;
import eu.shardline.mod.world.structure.ModStructures;
import eu.shardline.mod.world.structure.QueenArenaPiece;
import eu.shardline.mod.world.structure.RuinTowerPiece;
import eu.shardline.mod.world.structure.ShardCraterPiece;
import eu.shardline.mod.world.structure.ShardlinePiece;
import eu.shardline.mod.world.structure.TraderOutpostPiece;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Automatische Rauchtests fuer die Entwicklungsumgebung: {@code gradlew runGameTestServer}.
 * Der GameTest-Server laedt keine Mod-Dimensionen; Gelaende wird daher ueber den Chunk-Generator
 * berechnet und die Bauwerke werden direkt in der Testwelt errichtet.
 */
@GameTestHolder(Shardline.MODID)
@PrefixGameTestTemplate(false)
public final class ShardlineGameTests {
    private static final String EMPTY = "test/empty";

    private ShardlineGameTests() {
    }

    /** Gelaende und Biome: genug Inseln, genug Void, alle drei Biome vorhanden. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void terrainAndBiomes(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        NoiseBasedChunkGenerator generator = ModDimensions.createGenerator(
                registries.lookupOrThrow(Registries.BIOME), registries.lookupOrThrow(Registries.NOISE_SETTINGS));
        NoiseGeneratorSettings settings = registries.registryOrThrow(Registries.NOISE_SETTINGS).getOrThrow(ModNoiseSettings.SPLITTERWEITE);
        RandomState randomState = RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), 12345L);
        LevelHeightAccessor height = LevelHeightAccessor.create(0, 256);

        // Gleiche Pruefung wie bei der Chunk-Generierung: einheitliche Feature-Reihenfolge ueber alle Biome.
        try {
            FeatureSorter.buildFeaturesPerStep(List.copyOf(generator.getBiomeSource().possibleBiomes()),
                    biome -> biome.value().getGenerationSettings().features(), true);
        } catch (IllegalStateException exception) {
            helper.fail("Feature-Reihenfolge der Biome ist widerspruechlich: " + exception.getMessage());
            return;
        }

        int columns = 0;
        int solid = 0;
        int minTop = Integer.MAX_VALUE;
        int maxTop = Integer.MIN_VALUE;
        Map<String, Integer> biomes = new HashMap<>();
        for (int x = -1024; x <= 1024; x += 16) {
            for (int z = -1024; z <= 1024; z += 16) {
                columns++;
                int top = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, randomState);
                if (top > height.getMinBuildHeight() + 1) {
                    solid++;
                    minTop = Math.min(minTop, top);
                    maxTop = Math.max(maxTop, top);
                }
                Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(100),
                        QuartPos.fromBlock(z), randomState.sampler());
                biomes.merge(biome.unwrapKey().map(ResourceKey::location).map(Object::toString).orElse("?"), 1, Integer::sum);
            }
        }
        double coverage = solid / (double) columns;
        Shardline.LOGGER.info("[GameTest] Inselabdeckung {}% ({} von {} Spalten), Oberkante Y {}-{}, Biome {}",
                Math.round(coverage * 100), solid, columns, minTop, maxTop, biomes);
        helper.assertTrue(coverage > 0.15 && coverage < 0.85, "Inselabdeckung unplausibel: " + coverage);
        helper.assertTrue(biomes.size() == 3, "Nicht alle drei Biome gefunden: " + biomes);
        helper.succeed();
    }

    /** Alle Bauwerke einmal vollstaendig errichten und Schluesselbloecke pruefen. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void structurePieces(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        // Weit genug von anderen Tests entfernt bauen.
        BlockPos tower = new BlockPos(base.getX() + 200, 120, base.getZ());
        BlockPos arena = new BlockPos(base.getX() + 300, 120, base.getZ());
        BlockPos throne = new BlockPos(base.getX() + 400, QueenArenaPiece.FLOOR_Y, base.getZ());

        build(level, new RuinTowerPiece(tower, 1L));
        build(level, new ColossusArenaPiece(arena, 2L));
        build(level, new QueenArenaPiece(throne, 3L));

        expect(helper, level, tower, ModBlocks.SHARDSTONE_BRICKS.get(), ModBlocks.CRACKED_SHARDSTONE_BRICKS.get());
        expect(helper, level, tower.offset(2, 1, -1), Blocks.CHEST);
        expect(helper, level, arena.above(), ModBlocks.COLOSSUS_ALTAR.get());
        expect(helper, level, throne.above(3), ModBlocks.QUEEN_ALTAR.get());
        expect(helper, level, throne.offset(7, 1, 0), Blocks.CHEST);
        expect(helper, level, tower.offset(-2, 1, 1), ModBlocks.MEMORY_CRYSTAL.get());
        expect(helper, level, arena.offset(-10, 1, 10), ModBlocks.MEMORY_CRYSTAL.get());
        expect(helper, level, throne.offset(0, 1, 7), ModBlocks.MEMORY_CRYSTAL.get());
        expect(helper, level, throne.offset(0, 1, -7), ModBlocks.MEMORY_CRYSTAL.get());

        BlockPos shrine = new BlockPos(base.getX() + 500, 120, base.getZ());
        BlockPos outpost = new BlockPos(base.getX() + 560, 120, base.getZ());
        BlockPos crypt = new BlockPos(base.getX() + 620, 120, base.getZ());
        BlockPos geode = new BlockPos(base.getX() + 680, 120, base.getZ());
        BlockPos crater = new BlockPos(base.getX() + 740, 120, base.getZ());
        BlockPos keeperRuin = new BlockPos(base.getX() + 800, 120, base.getZ());
        build(level, new KeeperShrinePiece(shrine, 4L));
        build(level, new TraderOutpostPiece(outpost, 5L));
        build(level, new AshCryptPiece(crypt, 6L));
        build(level, new CrystalGeodePiece(geode, 7L));
        build(level, new ShardCraterPiece(crater, 8L));
        build(level, new eu.shardline.mod.world.structure.KeeperRuinPiece(keeperRuin, 9L));

        expect(helper, level, shrine.above(2), ModBlocks.MEMORY_CRYSTAL.get());
        expect(helper, level, shrine.offset(0, 1, 2), Blocks.CHEST);
        expect(helper, level, outpost.offset(2, 1, 2), Blocks.CHEST);
        expect(helper, level, outpost.offset(3, 1, 3), ModBlocks.CRYSTAL_LOG.get());
        expect(helper, level, outpost.offset(0, 5, 0), ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        expect(helper, level, crypt.offset(4, -7, 4), Blocks.CHEST);
        expect(helper, level, crypt.offset(-4, -7, 4), Blocks.CHEST);
        expect(helper, level, crypt.offset(0, -7, 4), ModBlocks.MEMORY_CRYSTAL.get());
        expect(helper, level, crypt.offset(0, -7, -4), Blocks.LADDER);
        expect(helper, level, crypt.offset(0, 0, -4), Blocks.LADDER);
        expect(helper, level, geode.offset(0, -1, 2), Blocks.CHEST);
        expect(helper, level, geode.offset(0, -6, 0), ModBlocks.SHARDSTONE.get(), ModBlocks.SHARD_ORE.get());
        expect(helper, level, geode.offset(0, 0, -7), Blocks.AIR);
        expect(helper, level, crater.offset(0, -3, 0), ModBlocks.SHARDSTONE.get(), ModBlocks.SHARD_ORE.get(), ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        expect(helper, level, crater.offset(0, -1, 0), ModBlocks.GLOW_CRYSTAL_CLUSTER.get());
        boolean craterChest = BlockPos.betweenClosedStream(crater.offset(-7, -4, -7), crater.offset(7, 1, 7))
                .anyMatch(pos -> level.getBlockState(pos).is(Blocks.CHEST));
        helper.assertTrue(craterChest, "Keine Truhe im Splitterkrater");
        expect(helper, level, eu.shardline.mod.world.structure.KeeperRuinPiece.crystalPos(keeperRuin), ModBlocks.MEMORY_CRYSTAL.get());
        expect(helper, level, eu.shardline.mod.world.structure.KeeperRuinPiece.chestPos(keeperRuin), Blocks.CHEST);
        expect(helper, level, keeperRuin.offset(eu.shardline.mod.world.structure.KeeperRuinPiece.HALF_W, 1,
                eu.shardline.mod.world.structure.KeeperRuinPiece.HALF_L), ModBlocks.KEEPER_RUIN_PILLAR.get());
        expect(helper, level, keeperRuin.offset(0, 0, 2), ModBlocks.KEEPER_RUNE_SLAB.get());
        // Die Truhe der Hueter-Ruinen enthaelt immer mindestens ein Lore-Buch.
        var table = level.getServer().reloadableRegistries().getLootTable(eu.shardline.mod.core.ModLootTables.KEEPER_RUIN_CHEST);
        boolean lore = table.getRandomItems(new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                        .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, keeperRuin.getCenter())
                        .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST))
                .stream().anyMatch(stack -> stack.is(eu.shardline.mod.item.ModItems.LORE_BOOK.get()));
        helper.assertTrue(lore, "Keine Lore-Buecher in der Truhe der Hueter-Ruinen");
        helper.succeed();
    }

    private static void build(ServerLevel level, ShardlinePiece piece) {
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(7L),
                piece.getBoundingBox(), new ChunkPos(piece.getBoundingBox().getCenter()), piece.getBoundingBox().getCenter());
    }

    private static void expect(GameTestHelper helper, ServerLevel level, BlockPos pos, Block... expected) {
        for (Block block : expected) {
            if (level.getBlockState(pos).is(block)) {
                return;
            }
        }
        helper.fail("Bei " + pos + " erwartet " + List.of(expected) + ", gefunden " + level.getBlockState(pos));
    }

    /** Rahmen erkennen und Portal oeffnen; mit geladener Splitterweite zusaetzlich das Zielportal pruefen. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void portal(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO).offset(6, 2, 6);
        Block frame = ModBlocks.SHARD_PORTAL_FRAME.get();
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                boolean edge = dx == -1 || dx == 2 || dy == -1 || dy == 3;
                overworld.setBlockAndUpdate(base.offset(dx, dy, 0), edge ? frame.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
        }
        ShardPortalShape.findEmptyPortalShape(overworld, base, Direction.Axis.X)
                .ifPresentOrElse(ShardPortalShape::createPortalBlocks, () -> helper.fail("Portalrahmen nicht erkannt"));
        helper.assertTrue(overworld.getBlockState(base).is(ModBlocks.SHARD_PORTAL.get()), "Portalblock wurde nicht gesetzt");
        helper.assertTrue(overworld.getBlockState(base.offset(1, 2, 0)).is(ModBlocks.SHARD_PORTAL.get()), "Portal nicht vollstaendig gefuellt");

        if (overworld.getServer().getLevel(ModDimensions.SPLITTERWEITE_LEVEL) != null) {
            Entity probe = EntityType.ARMOR_STAND.create(overworld);
            probe.moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
            DimensionTransition transition = ModBlocks.SHARD_PORTAL.get().getPortalDestination(overworld, probe, base);
            helper.assertTrue(transition != null, "Kein Portalziel berechnet");
            helper.assertTrue(transition.newLevel().dimension() == ModDimensions.SPLITTERWEITE_LEVEL, "Falsche Zieldimension");
            BlockPos target = BlockPos.containing(transition.pos());
            boolean portalNearby = BlockPos.betweenClosedStream(target.offset(-2, -1, -2), target.offset(2, 3, 2))
                    .anyMatch(pos -> transition.newLevel().getBlockState(pos).is(ModBlocks.SHARD_PORTAL.get()));
            helper.assertTrue(portalNearby, "Kein Zielportal in der Splitterweite bei " + target);
            Shardline.LOGGER.info("[GameTest] Portal Oberwelt {} -> Splitterweite {} (Zielportal vorhanden)", base, target);
        } else {
            Shardline.LOGGER.info("[GameTest] Splitterweite im GameTest-Server nicht geladen - Zielportal wird nicht geprueft");
        }
        helper.succeed();
    }

    /**
     * Nur in einer echten Welt (runServer, /test runall): Strukturen in der Splitterweite finden,
     * generieren und pruefen, dazu die Spawnregeln aller Mobs auf echtem Boden.
     */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void splitterweiteWorld(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(ModDimensions.SPLITTERWEITE_LEVEL);
        if (level == null) {
            Shardline.LOGGER.info("[GameTest] Splitterweite nicht geladen - Welttest uebersprungen");
            helper.succeed();
            return;
        }
        level.getChunk(0, 0);
        BlockPos floor = new BlockPos(8, QueenArenaPiece.FLOOR_Y, 8);
        expect(helper, level, floor, ModBlocks.SHARDSTONE_BRICKS.get(), ModBlocks.CRACKED_SHARDSTONE_BRICKS.get(), ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        expect(helper, level, floor.above(3), ModBlocks.QUEEN_ALTAR.get());

        BlockPos tower = locate(helper, level, ModStructures.RUIN_TOWER_KEY, ModBlocks.SHARDSTONE_BRICKS.get(),
                ModBlocks.CRACKED_SHARDSTONE_BRICKS.get());
        BlockPos altar = locate(helper, level, ModStructures.COLOSSUS_ARENA_KEY, ModBlocks.COLOSSUS_ALTAR.get());
        BlockPos shrine = locate(helper, level, ModStructures.KEEPER_SHRINE_KEY, ModBlocks.MEMORY_CRYSTAL.get());
        BlockPos outpost = locate(helper, level, ModStructures.TRADER_OUTPOST_KEY, ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        BlockPos crypt = locate(helper, level, ModStructures.ASH_CRYPT_KEY, ModBlocks.SHARDSTONE_BRICKS.get(),
                ModBlocks.CRACKED_SHARDSTONE_BRICKS.get());
        BlockPos geode = locate(helper, level, ModStructures.CRYSTAL_GEODE_KEY, ModBlocks.GLOW_CRYSTAL_BLOCK.get());
        Shardline.LOGGER.info("[GameTest] Splitterweite: Schrein bei {}, Handelsposten bei {}, Gruft bei {}, Geode bei {}",
                shrine, outpost, crypt, geode);
        Shardline.LOGGER.info("[GameTest] Splitterweite: Thron bei {}, Ruinen-Turm bei {}, Koloss-Altar bei {}", floor.above(3), tower, altar);

        // Spawnregeln: jeder Bodenmob muss auf dem Arenaboden, jeder Flieger in der Luft darueber spawnen koennen.
        BlockPos ground = altar.offset(6, 0, 6);
        BlockPos air = altar.above(8);
        RandomSource random = RandomSource.create(42L);
        for (EntityType<?> type : List.<EntityType<?>>of(ModEntities.SHARD_CREEPER.get(), ModEntities.ASH_WALKER.get(),
                ModEntities.CRYSTAL_SPIDER.get(), ModEntities.SHADOW_FOX.get(), ModEntities.CRYSTAL_GOLEM.get(),
                ModEntities.SHARD_TRADER.get(), ModEntities.SHATTERED_KNIGHT.get())) {
            helper.assertTrue(canSpawn(type, level, ground, random), EntityType.getKey(type) + " kann bei " + ground + " nicht spawnen");
        }
        for (EntityType<?> type : List.<EntityType<?>>of(ModEntities.SHARD_SCOUT.get(), ModEntities.VOID_RAY.get(),
                ModEntities.GLOW_MOTH.get())) {
            helper.assertTrue(canSpawn(type, level, air, random), EntityType.getKey(type) + " kann bei " + air + " nicht spawnen");
        }
        Shardline.LOGGER.info("[GameTest] Splitterweite: alle Spawnregeln erfuellbar");
        helper.succeed();
    }

    /**
     * Nur in einer echten Welt: erzeugt wie ein erkundender Spieler ein grosses Gebiet der Splitterweite
     * (Strukturen, Features, Spawns bei der Generierung). Faengt Haenger und Deadlocks in der Weltgenerierung ab.
     */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void chunkGenerationStress(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(ModDimensions.SPLITTERWEITE_LEVEL);
        if (level == null) {
            Shardline.LOGGER.info("[GameTest] Splitterweite nicht geladen - Generierungstest uebersprungen");
            helper.succeed();
            return;
        }
        int radius = 12;
        ChunkPos center = new ChunkPos(new BlockPos(1200, 0, 1200));
        long start = System.nanoTime();
        int generated = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.getChunk(center.x + dx, center.z + dz);
                generated++;
            }
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        Shardline.LOGGER.info("[GameTest] {} Chunks der Splitterweite in {} ms erzeugt ({} ms pro Chunk)",
                generated, millis, millis / Math.max(1, generated));
        helper.succeed();
    }

    private static boolean canSpawn(EntityType<?> type, ServerLevel level, BlockPos pos, RandomSource random) {
        for (int attempt = 0; attempt < 50; attempt++) {
            if (SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, random)) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos locate(GameTestHelper helper, ServerLevel level, ResourceKey<Structure> key, Block... expected) {
        Holder<Structure> structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(key);
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, HolderSet.direct(structure), BlockPos.ZERO, 64, false);
        if (found == null) {
            helper.fail("Struktur nicht gefunden: " + key.location());
            return BlockPos.ZERO;
        }
        // Gemeldet wird die Chunk-Ecke; die Bauwerke stehen in der Chunk-Mitte.
        ChunkPos chunk = new ChunkPos(found.getFirst());
        BlockPos pos = new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
        level.getChunk(chunk.x, chunk.z);
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos candidate = new BlockPos(pos.getX(), y, pos.getZ());
            if (java.util.Arrays.stream(expected).anyMatch(level.getBlockState(candidate)::is)) {
                return candidate;
            }
        }
        helper.fail("Struktur " + key.location() + " bei " + pos + " enthaelt keinen der Bloecke " + List.of(expected));
        return pos;
    }

    /** Alle Wesen erzeugen und 10 Sekunden ticken lassen. */
    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void mobsTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).offset(0, 0, 40);
        List<Entity> spawned = new ArrayList<>();
        int index = 0;
        for (EntityType<?> type : List.<EntityType<?>>of(
                ModEntities.SHARD_CREEPER.get(), ModEntities.CRYSTAL_GOLEM.get(), ModEntities.GLOW_MOTH.get(),
                ModEntities.ASH_WALKER.get(), ModEntities.SHARD_SCOUT.get(), ModEntities.VOID_RAY.get(),
                ModEntities.CRYSTAL_SPIDER.get(), ModEntities.SHARD_TRADER.get(), ModEntities.SHADOW_FOX.get(),
                ModEntities.SHATTERED_KNIGHT.get(), ModEntities.CRYSTAL_COLOSSUS.get(), ModEntities.SHARD_QUEEN.get(),
                ModEntities.QUEEN_CLONE.get(), ModEntities.SHARD_ZOMBIE.get(), ModEntities.CRYSTALLIZED_COW.get(),
                ModEntities.VIRUS_HEART.get(), ModEntities.CRYSTAL_SNAIL.get(), ModEntities.CRYSTALLIZED_SHEEP.get(),
                ModEntities.CRYSTALLIZED_PIG.get(), ModEntities.CRYSTALLIZED_VILLAGER.get())) {
            // Mit Abstand erzeugen, damit grosse Wesen kleinere nicht in Bloecke schieben.
            BlockPos spawn = origin.offset((index % 5) * 8, 0, (index / 5) * 8);
            index++;
            Entity entity = type.spawn(level, spawn, MobSpawnType.COMMAND);
            helper.assertTrue(entity != null, "Konnte " + EntityType.getKey(type) + " nicht erzeugen");
            spawned.add(entity);
        }
        helper.runAfterDelay(200, () -> {
            for (Entity entity : spawned) {
                String cause = entity instanceof net.minecraft.world.entity.LivingEntity living && living.getLastDamageSource() != null
                        ? living.getLastDamageSource().getMsgId() : "-";
                helper.assertTrue(entity.isAlive(), EntityType.getKey(entity.getType()) + " ist nach 10 s nicht mehr am Leben (Grund: "
                        + entity.getRemovalReason() + ", letzter Schaden: " + cause + ")");
                entity.discard();
            }
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------ Splitterhandwerk

    /** Kristallschnecke: zieht sich bei Treffern ins Haus zurueck, nimmt dann deutlich weniger Schaden und kommt wieder heraus. */
    @GameTest(template = EMPTY, timeoutTicks = 300, batch = "shardline_snail")
    public static void crystalSnailHides(GameTestHelper helper) {
        // Freiraum schaffen: Bauwerke anderer Tests koennen in die Testflaeche ragen (sonst Erstickungsschaden).
        for (BlockPos pos : BlockPos.betweenClosed(-2, 0, -2, 4, 4, 4)) {
            helper.setBlock(pos, pos.getY() == 0 ? Blocks.STONE : Blocks.AIR);
        }
        Runnable release = forceArea(helper, 4);
        CrystalSnailEntity snail = helper.spawn(ModEntities.CRYSTAL_SNAIL.get(), new BlockPos(1, 1, 1));
        helper.assertFalse(snail.isHiding(), "Schnecke darf anfangs nicht versteckt sein");
        float[] firstHit = new float[1];
        helper.startSequence()
                .thenIdle(2)
                .thenExecute(() -> {
                    float before = snail.getHealth();
                    snail.invulnerableTime = 0;
                    snail.hurt(helper.getLevel().damageSources().generic(), 4.0F);
                    firstHit[0] = before - snail.getHealth();
                    helper.assertTrue(firstHit[0] > 0.0F, "Erster Treffer hat keinen Schaden gemacht");
                    helper.assertTrue(snail.isHiding(), "Schnecke versteckt sich nach einem Treffer nicht");
                })
                .thenIdle(23)
                .thenExecute(() -> {
                    float before = snail.getHealth();
                    snail.invulnerableTime = 0;
                    snail.hurt(helper.getLevel().damageSources().generic(), 4.0F);
                    float secondHit = before - snail.getHealth();
                    helper.assertTrue(secondHit < firstHit[0] * 0.5F, "Versteckte Schnecke nimmt zu viel Schaden: " + secondHit + " statt < " + firstHit[0] * 0.5F);
                    helper.assertTrue(snail.isHiding(), "Schnecke sollte noch versteckt sein");
                })
                .thenWaitUntil(() -> helper.assertFalse(snail.isHiding(), "Schnecke kommt nicht wieder aus dem Haus"))
                .thenExecute(() -> {
                    helper.assertTrue(snail.isAlive(), "Schnecke ist gestorben");
                    snail.discard();
                    release.run();
                })
                .thenSucceed();
    }


    /** Splitterbann trifft nur Splitterwesen, Kristallwehr schuetzt nur vor Splitterschaden. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void shardEnchantments(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        net.minecraft.core.Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> bane = registry.getHolderOrThrow(ModEnchantments.SHARD_BANE);
        Holder<Enchantment> ward = registry.getHolderOrThrow(ModEnchantments.CRYSTAL_WARD);

        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.enchant(bane, 3);
        Entity zombie = ModEntities.SHARD_ZOMBIE.get().create(level);
        Entity cow = EntityType.COW.create(level);
        net.minecraft.world.damagesource.DamageSource generic = level.damageSources().generic();
        float vsZombie = EnchantmentHelper.modifyDamage(level, sword, zombie, generic, 5.0F);
        float vsCow = EnchantmentHelper.modifyDamage(level, sword, cow, generic, 5.0F);
        helper.assertTrue(Math.abs(vsZombie - 12.5F) < 0.01F, "Splitterbann III gegen Splitterzombie: " + vsZombie + " statt 12.5");
        helper.assertTrue(Math.abs(vsCow - 5.0F) < 0.01F, "Splitterbann darf normale Tiere nicht staerker treffen: " + vsCow);

        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
        chest.enchant(ward, 4);
        stand.setItemSlot(EquipmentSlot.CHEST, chest);
        float vsShard = EnchantmentHelper.getDamageProtection(level, stand, ModDamageTypes.source(level, ModDamageTypes.SHARD, null));
        float vsGeneric = EnchantmentHelper.getDamageProtection(level, stand, generic);
        helper.assertTrue(Math.abs(vsShard - 8.0F) < 0.01F, "Kristallwehr IV gegen Splitterschaden: " + vsShard + " statt 8");
        helper.assertTrue(vsGeneric == 0.0F, "Kristallwehr darf nicht vor normalem Schaden schuetzen: " + vsGeneric);
        zombie.discard();
        cow.discard();
        stand.discard();
        helper.succeed();
    }

    /** Neue Rezepte sind geladen und jedes Item, Wesen, Bauwerk und jede Verzauberung der Mod hat einen Namen. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void craftingContentComplete(GameTestHelper helper) {
        net.minecraft.world.item.crafting.RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String id : List.of("crystal_stairs", "crystal_door", "crystal_trapdoor", "crystal_fence_gate", "polished_shardstone",
                "chiseled_shardstone_bricks", "shard_lantern", "crystal_glass", "crystal_glass_pane", "glowcap_stew", "crystal_candy",
                "berry_tart", "resonance_compass")) {
            helper.assertTrue(recipes.byKey(Shardline.id(id)).isPresent(), "Rezept fehlt: " + id);
        }
        Language language = Language.getInstance();
        List<String> keys = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Shardline.MODID)) {
                keys.add(item.getDescriptionId());
            }
        }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (EntityType.getKey(type).getNamespace().equals(Shardline.MODID)) {
                keys.add(type.getDescriptionId());
            }
        }
        for (ResourceKey<Structure> key : List.of(ModStructures.SHARD_CRATER_KEY, ModStructures.RUIN_TOWER_KEY, ModStructures.KEEPER_SHRINE_KEY,
                ModStructures.TRADER_OUTPOST_KEY, ModStructures.ASH_CRYPT_KEY, ModStructures.CRYSTAL_GEODE_KEY,
                ModStructures.COLOSSUS_ARENA_KEY, ModStructures.QUEEN_ARENA_KEY, ModStructures.KEEPER_RUIN_KEY)) {
            keys.add("structure.shardline." + key.location().getPath());
        }
        keys.add("enchantment.shardline.shard_bane");
        keys.add("enchantment.shardline.crystal_ward");
        for (String dir : List.of("n", "ne", "e", "se", "s", "sw", "w", "nw")) {
            keys.add("item.shardline.resonance_compass.dir." + dir);
        }
        List<String> missing = keys.stream().filter(key -> !language.has(key)).toList();
        helper.assertTrue(missing.isEmpty(), "Fehlende Namen: " + missing);
        helper.succeed();
    }

    // ------------------------------------------------------------------ Story-Erweiterung 2

    /**
     * Haelt die Chunks der Testflaeche geladen und tickend. Die Vorlage test/empty ist nur 1x1x1 gross; Wesen ausserhalb
     * ihres Chunks ticken sonst je nach Lage der Testposition nicht. @return gibt die Chunks wieder frei
     */
    private static Runnable forceArea(GameTestHelper helper, int size) {
        return forceArea(helper, new BlockPos(-1, 0, -1), new BlockPos(size, 0, size));
    }

    private static Runnable forceArea(GameTestHelper helper, BlockPos fromRelative, BlockPos toRelative) {
        ServerLevel level = helper.getLevel();
        BlockPos a = helper.absolutePos(fromRelative);
        BlockPos b = helper.absolutePos(toRelative);
        List<ChunkPos> forced = new ArrayList<>();
        for (int cx = Math.min(a.getX(), b.getX()) >> 4; cx <= Math.max(a.getX(), b.getX()) >> 4; cx++) {
            for (int cz = Math.min(a.getZ(), b.getZ()) >> 4; cz <= Math.max(a.getZ(), b.getZ()) >> 4; cz++) {
                if (level.setChunkForced(cx, cz, true)) {
                    forced.add(new ChunkPos(cx, cz));
                }
            }
        }
        return () -> forced.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
    }

    /** Freie Testflaeche mit Steinboden (Bauwerke anderer Tests koennen hineinragen). */
    private static void clearArea(GameTestHelper helper, int size, int height) {
        for (BlockPos pos : BlockPos.betweenClosed(-1, 0, -1, size, height, size)) {
            helper.setBlock(pos, pos.getY() == 0 ? Blocks.STONE : Blocks.AIR);
        }
    }

    /** Schaf, Schwein und Dorfbewohner kristallisieren; Kragen nach dem Zaehmen; Reinigung gibt dem Dorfbewohner den Beruf zurueck. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void infectedAnimals(GameTestHelper helper) {
        clearArea(helper, 8, 4);
        ServerLevel level = helper.getLevel();
        net.minecraft.world.entity.animal.Sheep sheep = helper.spawn(EntityType.SHEEP, new BlockPos(1, 1, 1));
        net.minecraft.world.entity.animal.Pig pig = helper.spawn(EntityType.PIG, new BlockPos(4, 1, 1));
        net.minecraft.world.entity.npc.Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(7, 1, 1));
        villager.setVillagerData(villager.getVillagerData().setProfession(net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN).setLevel(2));

        var crystalSheep = eu.shardline.mod.virus.VirusEffects.convert(level, sheep, ModEntities.CRYSTALLIZED_SHEEP.get());
        var crystalPig = eu.shardline.mod.virus.VirusEffects.convert(level, pig, ModEntities.CRYSTALLIZED_PIG.get());
        var crystalVillager = eu.shardline.mod.entity.infected.CrystallizedVillagerEntity.infect(level, villager);
        helper.assertTrue(crystalSheep != null && crystalPig != null && crystalVillager != null, "Kristallisierung fehlgeschlagen");
        helper.assertTrue(crystalVillager.remembersVillager(), "Dorfbewohner-Daten wurden nicht gespeichert");

        helper.assertFalse(crystalSheep.hasCollar(), "Wildes Schaf darf keinen Kragen haben");
        crystalSheep.setOwnerUUID(java.util.UUID.randomUUID());
        helper.assertTrue(crystalSheep.hasCollar(), "Gezaehmtes Schaf zeigt keinen Kragen");
        net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
        crystalSheep.addAdditionalSaveData(saved);
        var reloaded = ModEntities.CRYSTALLIZED_SHEEP.get().create(level);
        reloaded.readAdditionalSaveData(saved);
        helper.assertTrue(reloaded.hasCollar() && reloaded.isTamed(), "Besitzer/Kragen wird nicht gespeichert");

        crystalSheep.shear(net.minecraft.sounds.SoundSource.PLAYERS);
        helper.assertTrue(crystalSheep.isSheared(), "Kristallschaf laesst sich nicht scheren");
        boolean amethyst = !level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, crystalSheep.getBoundingBox().inflate(3.0),
                item -> item.getItem().is(Items.AMETHYST_SHARD)).isEmpty();
        helper.assertTrue(amethyst, "Scheren liefert keinen Amethyst");

        var cured = eu.shardline.mod.virus.VirusEffects.purifyMob(level, crystalVillager);
        helper.assertTrue(cured instanceof net.minecraft.world.entity.npc.Villager, "Reinigung liefert keinen Dorfbewohner");
        var data = ((net.minecraft.world.entity.npc.Villager) cured).getVillagerData();
        helper.assertTrue(data.getProfession() == net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN && data.getLevel() == 2,
                "Beruf ging bei der Reinigung verloren: " + data.getProfession() + " " + data.getLevel());
        helper.assertTrue(eu.shardline.mod.virus.VirusEffects.purifyMob(level, crystalPig) instanceof net.minecraft.world.entity.animal.Pig,
                "Schwein wird nicht gereinigt");
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO)).inflate(12.0),
                entity -> !(entity instanceof net.minecraft.world.entity.player.Player))) {
            entity.discard();
        }
        helper.succeed();
    }

    /** Der Entscheidungsaltar entsteht vor dem Thron, nur einmal, und ruht ohne Kern in der Naehe. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void decisionAltar(GameTestHelper helper) {
        clearArea(helper, 8, 6);
        ServerLevel level = helper.getLevel();
        BlockPos throne = helper.absolutePos(new BlockPos(3, 3, 0));
        level.setBlockAndUpdate(throne, ModBlocks.QUEEN_ALTAR.get().defaultBlockState());
        BlockPos altar = eu.shardline.mod.block.custom.DecisionAltarBlock.raise(level, throne, throne);
        BlockPos expected = eu.shardline.mod.block.custom.DecisionAltarBlock.spotBeforeThrone(throne);
        helper.assertTrue(expected.equals(altar), "Altar nicht vor dem Thron: " + altar + " statt " + expected);
        helper.assertTrue(level.getBlockState(expected).is(ModBlocks.DECISION_ALTAR.get()), "Altarblock fehlt");
        helper.assertTrue(expected.equals(eu.shardline.mod.block.custom.DecisionAltarBlock.raise(level, throne, throne)),
                "Zweiter Aufruf darf keinen weiteren Altar bauen");
        helper.assertFalse(eu.shardline.mod.block.custom.DecisionAltarBlock.isCoreNearby(level, expected), "Ohne Kern darf der Altar nicht erwachen");
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(expected);
        helper.assertTrue(state.getLightEmission() == 4, "Ruhender Altar sollte schwach leuchten");
        helper.assertTrue(state.setValue(eu.shardline.mod.block.custom.DecisionAltarBlock.ACTIVE, true).getLightEmission() == 12,
                "Erwachter Altar sollte hell leuchten");
        helper.succeed();
    }

    /** Immunitaet blockt Splitterfieber; der Reinheitsanker verteilt sie in seiner Zone. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void immunity(GameTestHelper helper) {
        clearArea(helper, 6, 3);
        ServerLevel level = helper.getLevel();
        net.minecraft.world.entity.animal.Pig shielded = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
        net.minecraft.world.entity.animal.Pig exposed = helper.spawn(EntityType.PIG, new BlockPos(5, 1, 5));
        eu.shardline.mod.virus.VirusEffects.giveImmunity(shielded, 200, false);
        eu.shardline.mod.virus.VirusEffects.giveFever(shielded, 200);
        eu.shardline.mod.virus.VirusEffects.giveFever(exposed, 200);
        helper.assertFalse(shielded.hasEffect(eu.shardline.mod.effect.ModEffects.SHARD_FEVER), "Immunitaet hat das Fieber nicht verhindert");
        helper.assertTrue(exposed.hasEffect(eu.shardline.mod.effect.ModEffects.SHARD_FEVER), "Ohne Immunitaet muss das Fieber wirken");

        BlockPos anchor = helper.absolutePos(new BlockPos(3, 1, 3));
        level.setBlockAndUpdate(anchor, ModBlocks.PURITY_ANCHOR.get().defaultBlockState());
        try {
            level.getBlockState(anchor).tick(level, anchor, level.getRandom());
            helper.assertTrue(exposed.hasEffect(eu.shardline.mod.effect.ModEffects.IMMUNITY), "Anker verteilt keine Immunitaet");
            helper.assertFalse(exposed.hasEffect(eu.shardline.mod.effect.ModEffects.SHARD_FEVER), "Anker heilt das Fieber nicht");
        } finally {
            level.setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());
            shielded.discard();
            exposed.discard();
        }
        helper.succeed();
    }

    /** Virusherz: Phasen nach Lebenspunkten, Verhaertung beim Phasenwechsel (unverwundbar), danach wieder verwundbar. */
    @GameTest(template = EMPTY, timeoutTicks = 240, batch = "shardline_heart")
    public static void virusHeartPhases(GameTestHelper helper) {
        clearArea(helper, 6, 5);
        Runnable release = forceArea(helper, 6);
        var heart = helper.spawn(ModEntities.VIRUS_HEART.get(), new BlockPos(3, 1, 3));
        helper.assertTrue(eu.shardline.mod.entity.boss.VirusHeartEntity.phaseFor(1.0F) == 1
                && eu.shardline.mod.entity.boss.VirusHeartEntity.phaseFor(0.6F) == 2
                && eu.shardline.mod.entity.boss.VirusHeartEntity.phaseFor(0.2F) == 3, "Phasengrenzen falsch");
        helper.assertTrue(heart.getPhase() == 1, "Neues Herz muss in Phase 1 sein");
        heart.setHealth(heart.getMaxHealth() * 0.6F);
        // Warten statt fester Verzoegerung: Der Chunk einer neuen Batch tickt nicht sofort.
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(heart.getPhase() == 2, "Phase 2 wurde nicht erreicht"))
                .thenExecute(() -> {
                    heart.invulnerableTime = 0;
                    helper.assertFalse(heart.hurt(helper.getLevel().damageSources().generic(), 4.0F),
                            "Beim Phasenwechsel muss das Herz unverwundbar sein");
                })
                .thenIdle(40)
                .thenExecute(() -> {
                    heart.invulnerableTime = 0;
                    helper.assertTrue(heart.hurt(helper.getLevel().damageSources().generic(), 4.0F),
                            "Nach dem Phasenwechsel muss das Herz verwundbar sein");
                    heart.setHealth(heart.getMaxHealth() * 0.2F);
                })
                .thenWaitUntil(() -> helper.assertTrue(heart.getPhase() == 3, "Phase 3 wurde nicht erreicht"))
                .thenExecute(() -> {
                    heart.discard();
                    release.run();
                })
                .thenSucceed();
    }

    /** Lose Lore-Seiten: Verkauf gegen Splitter und Binden von vier Seiten desselben Buchs. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void lorePageTrading(GameTestHelper helper) {
        net.minecraft.world.entity.player.Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        LoreBooks.LoreBook seal = LoreBooks.byId("the_seal");
        LoreBooks.LoreBook oath = LoreBooks.byId("keepers_oath");
        for (int page = 1; page <= 4; page++) {
            player.getInventory().add(LoreBooks.page(seal, page));
        }
        player.getInventory().add(LoreBooks.page(oath, 2));
        helper.assertTrue(eu.shardline.mod.story.lore.TraderDialogue.countPages(player) == 5, "Seiten falsch gezaehlt");
        helper.assertTrue(eu.shardline.mod.story.lore.TraderDialogue.bindableBooks(player).equals(List.of(seal)), "Bindbares Buch nicht erkannt");
        LoreBooks.LoreBook bound = eu.shardline.mod.story.lore.TraderDialogue.bindBook(player);
        helper.assertTrue(bound == seal, "Falsches Buch gebunden");
        helper.assertTrue(player.getInventory().contains(stack -> stack.is(eu.shardline.mod.item.ModItems.LORE_BOOK.get())
                && "the_seal".equals(stack.get(eu.shardline.mod.core.ModDataComponents.LORE_BOOK.get()))), "Gebundenes Buch fehlt im Inventar");
        helper.assertTrue(eu.shardline.mod.story.lore.TraderDialogue.countPages(player) == 1, "Gebundene Seiten wurden nicht entfernt");
        int sold = eu.shardline.mod.story.lore.TraderDialogue.sellPages(player);
        int fragments = player.getInventory().countItem(eu.shardline.mod.item.ModItems.SHARD_FRAGMENT.get());
        helper.assertTrue(sold == 1 && fragments == eu.shardline.mod.config.ShardlineConfig.LORE_PAGE_PRICE.get(),
                "Verkauf falsch: " + sold + " Seiten, " + fragments + " Splitter");
        helper.assertTrue(eu.shardline.mod.story.lore.TraderDialogue.bindBook(player) == null, "Ohne Seiten darf nichts gebunden werden");
        helper.succeed();
    }

    /** Kapitel sichtbar: Status-Ausgabe, eigene Embleme als Icons der Kapitel-Advancements, Texte fuer jedes Ziel. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void chapterDisplay(GameTestHelper helper) {
        net.minecraft.server.MinecraftServer server = helper.getLevel().getServer();
        List<net.minecraft.network.chat.Component> lines = eu.shardline.mod.command.ShardlineCommands.statusLines(server, java.util.UUID.randomUUID());
        helper.assertTrue(lines.size() >= 5, "Status hat zu wenige Zeilen: " + lines.size());
        Language language = Language.getInstance();
        for (int chapter = 0; chapter <= StoryChapter.MAX; chapter++) {
            helper.assertTrue(language.has("story.shardline.next." + chapter), "Ziel-Text fehlt fuer Kapitel " + chapter);
        }
        for (StoryChapter chapter : StoryChapter.values()) {
            var holder = server.getAdvancements().get(Shardline.id(chapter.advancementId()));
            helper.assertTrue(holder != null && holder.value().display().isPresent(), "Advancement fehlt: " + chapter.advancementId());
            net.minecraft.world.item.ItemStack icon = holder.value().display().get().getIcon();
            helper.assertTrue(icon.is(eu.shardline.mod.item.ModItems.CHAPTER_EMBLEM.get())
                    && icon.getOrDefault(eu.shardline.mod.core.ModDataComponents.CHAPTER.get(), 0) == chapter.number(),
                    "Kapitel " + chapter.number() + " hat nicht das eigene Emblem als Icon");
        }
        helper.succeed();
    }

    /** Reinheitszone: wachsender Radius (alte Anker voll), Schutz nach aktuellem Radius, Reinigung nur der neuen Schale. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void purityZone(GameTestHelper helper) {
        helper.assertTrue(Math.abs(PurityAnchors.radius(0L, 3000L, 6, 24, 6000L) - 15.0) < 1.0E-6, "Halbe Wachstumszeit = halber Weg");
        helper.assertTrue(PurityAnchors.radius(-1L, 3000L, 6, 24, 6000L) == 24.0, "Alte Anker ohne Zeitstempel haben den vollen Radius");
        helper.assertTrue(PurityAnchors.radius(0L, 99999L, 6, 24, 6000L) == 24.0, "Radius waechst nicht ueber das Maximum");

        ServerLevel level = helper.getLevel();
        ShardlineWorldData data = StoryManager.data(level.getServer());
        Runnable release = forceArea(helper, 18);
        BlockPos center = helper.absolutePos(new BlockPos(9, 1, 9));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -1, -8), center.offset(8, 2, 8))) {
            level.setBlock(pos, pos.getY() == center.getY() - 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(center, ModBlocks.PURITY_ANCHOR.get().defaultBlockState(), 3);
        try {
            int start = eu.shardline.mod.config.ShardlineConfig.ANCHOR_START_RADIUS.get();
            int max = eu.shardline.mod.config.ShardlineConfig.ANCHOR_RADIUS.get();
            long grow = eu.shardline.mod.config.ShardlineConfig.ANCHOR_GROW_SECONDS.get() * 20L;
            // Frisch gesetzt: Startradius (die Halbzeit prueft oben die Formel; die Spielzeit einer Testwelt ist dafuer zu klein)
            double radius = PurityAnchors.radius(level, center);
            helper.assertTrue(grow <= 0 ? radius == Math.max(start, max) : Math.abs(radius - start) < 0.5, "Startradius falsch: " + radius);
            helper.assertTrue(PurityAnchors.isProtected(level, center.offset((int) radius - 1, 0, 0)), "Innerhalb der Zone muss geschuetzt sein");
            helper.assertFalse(PurityAnchors.isProtected(level, center.offset((int) radius + 2, 0, 0)), "Ausserhalb der Zone darf kein Schutz sein");

            level.getBlockState(center).tick(level, center, level.getRandom());
            helper.assertTrue(level.getBlockEntity(center) instanceof eu.shardline.mod.block.entity.PurityAnchorBlockEntity anchor
                    && Math.abs(anchor.radius(level.getGameTime()) - radius) < 0.5, "Block-Entity kennt den Radius nicht");

            // Schale 4..8: der Block bei 3 bleibt (schon sauber gerechnet), der bei 6 wird gereinigt
            BlockPos inner = center.offset(3, -1, 0);
            BlockPos outer = center.offset(6, -1, 0);
            level.setBlock(inner, ModBlocks.INFECTED_STONE.get().defaultBlockState(), 2);
            level.setBlock(outer, ModBlocks.INFECTED_STONE.get().defaultBlockState(), 2);
            int[] purified = {-1};
            Purification.purifyShell(level, center, 4, 8, count -> purified[0] = count);
            helper.assertTrue(purified[0] == 1, "Schale sollte genau einen Block reinigen, waren " + purified[0]);
            helper.assertTrue(level.getBlockState(inner).is(ModBlocks.INFECTED_STONE.get()), "Innerer Block durfte nicht gereinigt werden");
            helper.assertFalse(level.getBlockState(outer).is(ModBlocks.INFECTED_STONE.get()), "Aeusserer Block wurde nicht gereinigt");
        } finally {
            level.setBlock(center, Blocks.AIR.defaultBlockState(), 3);
            release.run();
        }
        helper.assertFalse(data.anchors(level.dimension()).contains(center.asLong()), "Anker wurde nicht abgemeldet");
        helper.assertTrue(data.anchorSince(level.dimension(), center.asLong()) == -1L, "Setzzeit wurde nicht entfernt");
        helper.succeed();
    }

    // ------------------------------------------------------------------ Story & Shardvirus

    /**
     * Infektion, Schutz durch den Reinheitsanker und Reinigung. Veraendert den globalen
     * Virus-Zustand und laeuft deshalb in einer eigenen Batch; der Zustand wird danach wiederhergestellt.
     */
    @GameTest(template = EMPTY, timeoutTicks = 100, batch = "shardline_virus")
    public static void virusSpreadAnchorPurify(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ShardlineWorldData data = StoryManager.data(level.getServer());
        boolean wasInfected = data.isInfected();
        long outbreakTime = data.outbreakTime();
        boolean wasPaused = data.isPaused();
        StoryEnding ending = data.ending();
        data.setInfected(true, level.getGameTime());
        data.setPaused(false);
        data.setEnding(StoryEnding.NONE);
        BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).offset(0, 0, -60);
        try {
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, 0, -4), origin.offset(4, 3, 4))) {
                level.setBlock(pos, pos.getY() == origin.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            }
            level.setBlock(origin, ModBlocks.INFECTED_STONE.get().defaultBlockState(), 2);
            level.setBlock(origin.west(), Blocks.GRASS_BLOCK.defaultBlockState(), 2);

            helper.assertTrue(StoryManager.isSpreading(level.getServer()), "Virus sollte sich ausbreiten");
            helper.assertTrue(VirusSpreadManager.tryInfect(level, origin.east()), "Stein neben der Infektion wurde nicht befallen");
            helper.assertTrue(level.getBlockState(origin.east()).is(ModBlocks.INFECTED_STONE.get()), "Falscher Block nach Infektion von Stein");
            helper.assertTrue(VirusSpreadManager.tryInfect(level, origin.west()), "Grasblock wurde nicht befallen");
            helper.assertTrue(level.getBlockState(origin.west()).is(ModBlocks.INFECTED_GRASS.get()), "Falscher Block nach Infektion von Gras");
            helper.assertFalse(VirusSpreadManager.tryInfect(level, origin.above()), "Luft darf nicht infiziert werden");

            BlockPos anchor = origin.offset(3, 1, 3);
            level.setBlock(anchor, ModBlocks.PURITY_ANCHOR.get().defaultBlockState(), 3);
            helper.assertTrue(data.anchors(level.dimension()).contains(anchor.asLong()), "Reinheitsanker wurde nicht registriert");
            helper.assertTrue(PurityAnchors.isProtected(level, origin.north()), "Position sollte im Schutzradius liegen");
            helper.assertFalse(VirusSpreadManager.tryInfect(level, origin.north()), "Reinheitsanker hat die Infektion nicht verhindert");

            int[] purified = {-1};
            Purification.purifySphere(level, origin, 3, count -> purified[0] = count);
            helper.assertTrue(purified[0] == 3, "Es sollten 3 Bloecke gereinigt werden, waren " + purified[0]);
            helper.assertTrue(level.getBlockState(origin).is(Blocks.STONE), "Infizierter Stein wurde nicht zu Stein");
            helper.assertTrue(level.getBlockState(origin.west()).is(Blocks.GRASS_BLOCK), "Infiziertes Gras wurde nicht zu Gras");

            level.setBlock(anchor, Blocks.AIR.defaultBlockState(), 3);
            helper.assertFalse(data.anchors(level.dimension()).contains(anchor.asLong()), "Reinheitsanker wurde nicht abgemeldet");
            helper.succeed();
        } finally {
            data.setInfected(wasInfected, outbreakTime);
            data.setPaused(wasPaused);
            data.setEnding(ending);
            VirusSpreadManager.clearQueues();
        }
    }

    /** Vorgeschlagene Positionen werden am Tickende stapelweise umgewandelt, Pause leert die Warteschlange. */
    @GameTest(template = EMPTY, timeoutTicks = 100, batch = "shardline_virus_queue")
    public static void virusQueueProcessing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ShardlineWorldData data = StoryManager.data(level.getServer());
        boolean wasInfected = data.isInfected();
        long outbreakTime = data.outbreakTime();
        boolean wasPaused = data.isPaused();
        StoryEnding ending = data.ending();
        data.setInfected(true, level.getGameTime());
        data.setPaused(false);
        data.setEnding(StoryEnding.NONE);
        BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).offset(0, 0, -90);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, 0, -3), origin.offset(3, 2, 3))) {
            level.setBlock(pos, pos.getY() == origin.getY() ? Blocks.DIRT.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VirusSpreadManager.queueInfection(level, origin.relative(direction));
        }
        helper.assertTrue(VirusSpreadManager.queuedCount(level) >= 4, "Warteschlange wurde nicht gefuellt");
        helper.runAfterDelay(3, () -> {
            try {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    helper.assertTrue(level.getBlockState(origin.relative(direction)).is(ModBlocks.INFECTED_DIRT.get()),
                            "Wartende Position " + direction + " wurde nicht infiziert");
                }
                data.setPaused(true);
                VirusSpreadManager.queueInfection(level, origin);
                helper.assertFalse(StoryManager.isSpreading(level.getServer()), "Pause wirkt nicht");
                helper.succeed();
            } finally {
                Purification.purifySphere(level, origin, 3, null);
                data.setInfected(wasInfected, outbreakTime);
                data.setPaused(wasPaused);
                data.setEnding(ending);
                VirusSpreadManager.clearQueues();
            }
        });
    }

    /**
     * Kette Random Tick -&gt; Warteschlange -&gt; Umwandlung: Ein infizierter Block bekommt Random Ticks
     * und befaellt darueber seine Nachbarn. (Ob Minecraft den Block tickt, regelt
     * {@code randomTicks()} in den Blockeigenschaften; das wird hier mitgeprueft.)
     */
    @GameTest(template = EMPTY, timeoutTicks = 100, batch = "shardline_virus_ticks")
    public static void virusRandomTickSpread(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ShardlineWorldData data = StoryManager.data(level.getServer());
        boolean wasInfected = data.isInfected();
        long outbreakTime = data.outbreakTime();
        boolean wasPaused = data.isPaused();
        StoryEnding ending = data.ending();
        data.setInfected(true, level.getGameTime());
        data.setPaused(false);
        data.setEnding(StoryEnding.NONE);

        BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).offset(0, 0, -120);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, 0, -5), origin.offset(5, 3, 5))) {
            level.setBlock(pos, pos.getY() == origin.getY() ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(origin, ModBlocks.INFECTED_GRASS.get().defaultBlockState(), 2);
        for (Block block : List.of(ModBlocks.INFECTED_STONE.get(), ModBlocks.INFECTED_DIRT.get(), ModBlocks.INFECTED_GRASS.get(),
                ModBlocks.INFECTED_LOG.get(), ModBlocks.INFECTED_LEAVES.get(), ModBlocks.INFECTED_SAND.get(), ModBlocks.SHARD_ORE.get(),
                ModBlocks.SHARD_PORTAL.get())) {
            helper.assertTrue(block.defaultBlockState().isRandomlyTicking(), block + " bekommt keine Random Ticks");
        }
        for (int i = 0; i < 300; i++) {
            level.getBlockState(origin).randomTick(level, origin, level.getRandom());
        }

        helper.runAfterDelay(3, () -> {
            int infected = 0;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, 0, -5), origin.offset(5, 0, 5))) {
                if (level.getBlockState(pos).is(ModBlocks.INFECTED_GRASS.get())) {
                    infected++;
                }
            }
            Purification.purifySphere(level, origin, 8, null);
            data.setInfected(wasInfected, outbreakTime);
            data.setPaused(wasPaused);
            data.setEnding(ending);
            VirusSpreadManager.clearQueues();
            Shardline.LOGGER.info("[GameTest] Virus-Ausbreitung: {} infizierte Grasbloecke nach 300 Random Ticks", infected);
            helper.assertTrue(infected >= 4, "Virus hat sich per Random Tick kaum ausgebreitet: " + infected);
            helper.succeed();
        });
    }

    /** Story-Zustand ueberlebt Speichern und Laden. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void storyDataRoundTrip(GameTestHelper helper) {
        ShardlineWorldData data = new ShardlineWorldData();
        java.util.UUID player = java.util.UUID.randomUUID();
        BlockPos anchor = new BlockPos(10, 64, -20);
        data.setChapter(player, 5);
        data.addFlag(player, "book:the_seal");
        data.addHeartDefeated(player);
        data.setInfected(true, 1234L);
        data.setPaused(true);
        data.addAnchor(Level.OVERWORLD, anchor);
        data.addHerd(Level.OVERWORLD, anchor, 1);
        data.changeChunkInfection(Level.OVERWORLD, new ChunkPos(anchor), 42);
        data.setEnding(StoryEnding.DOMINATED);
        data.addRuler(player);

        ShardlineWorldData loaded = ShardlineWorldData.load(data.save(new net.minecraft.nbt.CompoundTag(), helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(loaded.chapter(player) == 5, "Kapitel nicht gespeichert");
        helper.assertTrue(loaded.hasFlag(player, "book:the_seal"), "Flag nicht gespeichert");
        helper.assertTrue(loaded.heartsDefeated(player) == 1, "Virusherzen nicht gespeichert");
        helper.assertTrue(loaded.isInfected() && loaded.outbreakTime() == 1234L && loaded.isPaused(), "Virus-Zustand nicht gespeichert");
        helper.assertTrue(loaded.anchors(Level.OVERWORLD).contains(anchor.asLong()), "Anker nicht gespeichert");
        helper.assertTrue(loaded.herds(Level.OVERWORLD).size() == 1, "Herd nicht gespeichert");
        helper.assertTrue(loaded.totalInfectedBlocks() == 42, "Chunk-Statistik nicht gespeichert");
        helper.assertTrue(loaded.ending() == StoryEnding.DOMINATED && loaded.isRuler(player), "Ende/Titel nicht gespeichert");
        helper.succeed();
    }

    /** Jeder Text, den die Story benutzt, steht in der Sprachdatei (Server-Sprache en_us). */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void storyTexts(GameTestHelper helper) {
        Language language = Language.getInstance();
        List<String> keys = new ArrayList<>();
        for (StoryChapter chapter : StoryChapter.values()) {
            keys.add("story.shardline.chapter." + chapter.id());
            keys.add("story.shardline.chapter." + chapter.id() + ".summary");
            keys.add("advancements.shardline.story.chapter_" + chapter.number() + ".title");
        }
        for (StoryEnding ending : StoryEnding.values()) {
            keys.add("story.shardline.ending." + ending.id());
        }
        for (LoreBooks.LoreBook book : LoreBooks.ALL) {
            keys.add("lore.shardline." + book.id() + ".title");
            keys.add("lore.shardline." + book.id() + ".author");
            for (int page = 1; page <= book.pages(); page++) {
                keys.add("lore.shardline." + book.id() + ".page." + page);
            }
        }
        for (int vision = 0; vision < Visions.COUNT; vision++) {
            keys.add("vision.shardline." + vision + ".title");
            keys.add("vision.shardline." + vision + ".subtitle");
            for (int line = 1; line <= 3; line++) {
                keys.add("vision.shardline." + vision + ".line." + line);
            }
        }
        List<String> stages = new ArrayList<>(List.of("sealed", "dominated"));
        for (int chapter = 0; chapter <= StoryChapter.MAX; chapter++) {
            stages.add("chapter_" + chapter);
        }
        for (String stage : stages) {
            for (int line = 1; line <= 4; line++) {
                keys.add("dialogue.shardline.trader." + stage + ".intro." + line);
            }
            for (int line = 1; line <= 3; line++) {
                keys.add("dialogue.shardline.trader." + stage + ".idle." + line);
                keys.add("dialogue.shardline.trader." + stage + ".more." + line);
            }
        }
        List<String> missing = keys.stream().filter(key -> !language.has(key)).toList();
        helper.assertTrue(missing.isEmpty(), "Fehlende Texte: " + missing);
        helper.assertTrue(LoreBooks.ALL.size() >= 12, "Es muessen mindestens 12 Lore-Buecher sein");
        helper.succeed();
    }

    /** Lore-Buecher tragen ihren Text als schreibgeschuetzten Buchinhalt. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void loreBookItems(GameTestHelper helper) {
        for (LoreBooks.LoreBook book : LoreBooks.ALL) {
            net.minecraft.world.item.ItemStack stack = LoreBooks.create(book);
            net.minecraft.world.item.component.WrittenBookContent content = stack.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);
            helper.assertTrue(content != null && content.pages().size() == book.pages(), "Buchinhalt fehlt fuer " + book.id());
            helper.assertTrue(book.id().equals(stack.get(eu.shardline.mod.core.ModDataComponents.LORE_BOOK.get())), "Buch-ID fehlt fuer " + book.id());
        }
        helper.succeed();
    }

    /** Ende "Versiegeln": Virus stoppt, Zuruecksetzen stellt den Zustand wieder her. */
    @GameTest(template = EMPTY, timeoutTicks = 20, batch = "shardline_ending")
    public static void endingSealStopsVirus(GameTestHelper helper) {
        net.minecraft.server.MinecraftServer server = helper.getLevel().getServer();
        ShardlineWorldData data = StoryManager.data(server);
        boolean wasInfected = data.isInfected();
        long outbreakTime = data.outbreakTime();
        StoryEnding ending = data.ending();
        try {
            data.setInfected(true, 1L);
            data.setEnding(StoryEnding.NONE);
            helper.assertTrue(StoryManager.isVirusActive(server), "Virus sollte aktiv sein");
            EndingManager.resolve(server, StoryEnding.SEALED, java.util.Set.of());
            helper.assertTrue(data.ending() == StoryEnding.SEALED, "Ende wurde nicht gesetzt");
            helper.assertFalse(StoryManager.isVirusActive(server), "Versiegelter Virus darf nicht aktiv sein");
            EndingManager.reset(server);
            helper.assertTrue(data.ending() == StoryEnding.NONE, "Ende wurde nicht zurueckgesetzt");
            helper.succeed();
        } finally {
            data.setInfected(wasInfected, outbreakTime);
            data.setEnding(ending);
        }
    }
}
