package eu.shardline.mod.virus;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Laesst in stark infizierten Chunks ein Virusherz entstehen. Ausgeloest, sobald die Zahl
 * infizierter Bloecke eines Chunks den Schwellwert erreicht - und nur, wenn ein Spieler in der
 * Naehe ist, kein anderes Herz in der Umgebung pulsiert und die Abklingzeit abgelaufen ist.
 */
public final class VirusHeartSpawner {
    private static final double PLAYER_RANGE = 64.0;
    private static final double OTHER_HEART_RANGE = 96.0;

    private VirusHeartSpawner() {
    }

    public static void onChunkInfectionChanged(ServerLevel level, ChunkPos chunkPos, int count) {
        int threshold = ShardlineConfig.HEART_SPAWN_THRESHOLD.get();
        if (count < threshold || count % threshold != 0) {
            return;
        }
        ShardlineWorldData data = StoryManager.data(level.getServer());
        long cooldown = ShardlineConfig.HEART_SPAWN_COOLDOWN_MINUTES.get() * 60L * 20L;
        long now = level.getGameTime();
        if (data.lastHeartSpawn() != 0L && now - data.lastHeartSpawn() < cooldown && now >= data.lastHeartSpawn()) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) {
            return;
        }
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15) + 1;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState ground = chunk.getBlockState(pos.below());
        if (!ground.isSolid() || level.isOutsideBuildHeight(pos)) {
            return;
        }
        if (!level.hasNearbyAlivePlayer(x + 0.5, y, z + 0.5, PLAYER_RANGE)) {
            return;
        }
        if (!level.getEntitiesOfClass(VirusHeartEntity.class, new AABB(pos).inflate(OTHER_HEART_RANGE)).isEmpty()) {
            return;
        }
        if (spawn(level, pos) != null) {
            data.setLastHeartSpawn(now);
        }
    }

    public static VirusHeartEntity spawn(ServerLevel level, BlockPos pos) {
        VirusHeartEntity heart = ModEntities.VIRUS_HEART.get().create(level);
        if (heart == null) {
            return null;
        }
        heart.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        heart.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        level.addFreshEntity(heart);
        level.sendParticles(ModParticles.VIRUS_SPORE.get(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 120, 1.5, 1.5, 1.5, 0.05);
        level.playSound(null, pos, ModSounds.VIRUS_HEART_PULSE.get(), SoundSource.HOSTILE, 4.0F, 0.5F);
        Component message = Component.translatable("message.shardline.virus_heart_awakens").withStyle(ChatFormatting.DARK_PURPLE);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(OTHER_HEART_RANGE))) {
            player.displayClientMessage(message, true);
        }
        return heart;
    }
}
