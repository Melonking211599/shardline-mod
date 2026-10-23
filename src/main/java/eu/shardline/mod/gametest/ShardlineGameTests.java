package eu.shardline.mod.gametest;

import com.mojang.datafixers.util.Pair;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.entity.ModEntities;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
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
        build(level, new KeeperShrinePiece(shrine, 4L));
        build(level, new TraderOutpostPiece(outpost, 5L));
        build(level, new AshCryptPiece(crypt, 6L));
        build(level, new CrystalGeodePiece(geode, 7L));
        build(level, new ShardCraterPiece(crater, 8L));

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
                ModEntities.VIRUS_HEART.get())) {
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
