package eu.shardline.mod.gametest;

import com.mojang.datafixers.util.Pair;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.world.dimension.ModDimensions;
import eu.shardline.mod.world.noise.ModNoiseSettings;
import eu.shardline.mod.world.portal.ShardPortalShape;
import eu.shardline.mod.world.structure.ColossusArenaPiece;
import eu.shardline.mod.world.structure.ModStructures;
import eu.shardline.mod.world.structure.QueenArenaPiece;
import eu.shardline.mod.world.structure.RuinTowerPiece;
import eu.shardline.mod.world.structure.ShardlinePiece;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
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

    /** Alle drei Bauwerke einmal vollstaendig errichten und Schluesselbloecke pruefen. */
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
                ModEntities.QUEEN_CLONE.get())) {
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
}
