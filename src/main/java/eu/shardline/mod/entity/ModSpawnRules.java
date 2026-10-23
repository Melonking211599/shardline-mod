package eu.shardline.mod.entity;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.Tags;

/**
 * Spawnbedingungen der Shardline-Mobs. In der Splitterweite gelten eigene Regeln (die Dimension ist
 * dauerhaft dunkel), in der Oberwelt spawnen ausgewaehlte Mobs nur selten und nachts.
 */
public final class ModSpawnRules {
    private ModSpawnRules() {
    }

    private static boolean inSplitterweite(ServerLevelAccessor level) {
        return level.getLevel().dimension() == ModDimensions.SPLITTERWEITE_LEVEL;
    }

    /** Wuerfelt die konfigurierbare Spawnchance; Spawner und Spawn-Eier sind nicht betroffen. */
    private static boolean rollChance(String mobId, ServerLevelAccessor level, MobSpawnType spawnType, RandomSource random) {
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return true;
        }
        double chance = ShardlineConfig.spawnChance(mobId);
        if (!inSplitterweite(level)) {
            if (!ShardlineConfig.OVERWORLD_SPAWNS.get()) {
                return false;
            }
            chance *= ShardlineConfig.OVERWORLD_SPAWN_CHANCE.get();
        }
        return random.nextDouble() < chance;
    }

    private static boolean overworldNight(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return level.getLevel().isNight()
                && !level.getBiome(pos).is(Tags.Biomes.IS_MUSHROOM)
                && Monster.isDarkEnoughToSpawn(level, pos, random);
    }

    /** Feindliche Bodenmobs: Splitterweite jederzeit, Oberwelt nur nachts im Dunkeln. */
    public static <T extends Mob> boolean hostileGround(String mobId, EntityType<T> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                                         BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) {
            return false;
        }
        if (!inSplitterweite(level) && !overworldNight(level, pos, random)) {
            return false;
        }
        return rollChance(mobId, level, spawnType, random);
    }

    /** Mobs, die nur in der Splitterweite auf festem Boden spawnen. */
    public static <T extends Mob> boolean splitterweiteGround(String mobId, EntityType<T> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                                               BlockPos pos, RandomSource random) {
        if (!inSplitterweite(level) && spawnType == MobSpawnType.NATURAL) {
            return false;
        }
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random)
                && !level.getBlockState(pos.below()).is(BlockTags.LEAVES)
                && rollChance(mobId, level, spawnType, random);
    }

    /** Feindliche Mobs, die nur in der Splitterweite spawnen (z. B. Gesplitterter Ritter). */
    public static <T extends Mob> boolean splitterweiteHostile(String mobId, EntityType<T> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                                                BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && splitterweiteGround(mobId, type, level, spawnType, pos, random);
    }

    /** Flieger der Splitterweite (Shard-Spaeher, Void-Rochen): brauchen nur freien Luftraum. */
    public static <T extends Mob> boolean splitterweiteAir(String mobId, boolean hostile, ServerLevelAccessor level, MobSpawnType spawnType,
                                                            BlockPos pos, RandomSource random) {
        if (hostile && level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!inSplitterweite(level) && spawnType == MobSpawnType.NATURAL) {
            return false;
        }
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && rollChance(mobId, level, spawnType, random);
    }

    /** Leuchtmotte: Splitterweite jederzeit, Oberwelt nur nachts. */
    public static <T extends Mob> boolean glowMoth(EntityType<T> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                                    BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        if (!inSplitterweite(level) && !(level.getLevel().isNight() && !level.getBiome(pos).is(Tags.Biomes.IS_MUSHROOM))) {
            return false;
        }
        return rollChance("glow_moth", level, spawnType, random);
    }
}
