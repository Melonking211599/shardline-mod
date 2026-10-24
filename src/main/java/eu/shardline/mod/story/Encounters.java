package eu.shardline.mod.story;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.boss.EchoQueenEntity;
import eu.shardline.mod.entity.boss.FesteringHeartEntity;
import eu.shardline.mod.network.ScreenEffectPayload;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.PurityAnchors;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Begegnungen in Kapitel 7-9: Schwaeren, der Nachhall und das Schwaerenherz.
 * <ul>
 *   <li>Dialogaktion {@code summon_encounter} ({@link #summon}): ruft eine Begegnung beim NPC bzw. Spieler hervor.
 *   Bosse erscheinen nur, wenn im Umkreis von 96 Bloecken noch keiner lebt.</li>
 *   <li>Nachbeben: alle {@code aftermath.aftershockMinutes} bebt die Weite fuer Spieler ab Kapitel 7 (Klang, kurzes
 *   Wackeln); in 24-48 Bloecken entsteht eine Schwaere, solange weniger als {@code aftermath.maxNodesNearPlayer} in der
 *   Naehe sind. Gesetzt wird nur in geladenen Chunks; gezaehlt ueber die gespeicherte Liste, ohne Weltsuche.</li>
 *   <li>Sieg ueber einen Boss: alle Spieler im Umkreis von 48 Bloecken erhalten die Flagge {@code boss:<typ>} (fuer die
 *   Entscheidungsdialoge) und das Advancement {@code story/<typ>}.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class Encounters {
    public static final String FESTERING_NODE = "festering_node";
    public static final String ECHO_QUEEN = "echo_queen";
    public static final String FESTERING_HEART = "festering_heart";
    public static final Set<String> TYPES = Set.of(FESTERING_NODE, ECHO_QUEEN, FESTERING_HEART);
    private static final double BOSS_SEARCH = 96.0;
    private static final double CREDIT_RADIUS = 48.0;
    private static long nextAftershock = -1L;

    private Encounters() {
    }

    /**
     * Ruft eine Begegnung hervor.
     *
     * @param anchor NPC, bei dem die Begegnung beginnt (null: beim Spieler)
     * @return true, wenn etwas erschienen ist
     */
    public static boolean summon(String type, ServerPlayer player, @Nullable Entity anchor) {
        ServerLevel level = player.serverLevel();
        BlockPos center = anchor != null ? anchor.blockPosition() : player.blockPosition();
        RandomSource random = level.getRandom();
        return switch (type) {
            case FESTERING_NODE -> spawnNodeNear(level, center, 14, 26, random) != null;
            case ECHO_QUEEN -> {
                if (!level.getEntitiesOfClass(EchoQueenEntity.class, new AABB(center).inflate(BOSS_SEARCH)).isEmpty()) {
                    yield false;
                }
                EchoQueenEntity queen = ModEntities.ECHO_QUEEN.get().create(level);
                if (queen == null) {
                    yield false;
                }
                BlockPos spot = ring(center, 6, 9, random).above(4);
                queen.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                queen.setHome(center);
                queen.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.EVENT, null);
                level.addFreshEntity(queen);
                announce(level, center, "message.shardline.echo_queen.appears");
                yield true;
            }
            case FESTERING_HEART -> {
                if (!level.getEntitiesOfClass(FesteringHeartEntity.class, new AABB(center).inflate(BOSS_SEARCH)).isEmpty()) {
                    yield false;
                }
                FesteringHeartEntity heart = ModEntities.FESTERING_HEART.get().create(level);
                BlockPos ground = groundNear(level, ring(center, 10, 14, random));
                if (heart == null || ground == null) {
                    yield false;
                }
                heart.moveTo(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                heart.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), MobSpawnType.EVENT, null);
                level.addFreshEntity(heart);
                announce(level, center, "message.shardline.festering_heart.appears");
                yield true;
            }
            default -> false;
        };
    }

    private static void announce(ServerLevel level, BlockPos center, String key) {
        Component message = Component.translatable(key).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
        for (ServerPlayer near : level.getEntitiesOfClass(ServerPlayer.class, new AABB(center).inflate(CREDIT_RADIUS))) {
            near.sendSystemMessage(message);
            StoryManager.send(near, new ScreenEffectPayload(ScreenEffectPayload.AFTERSHOCK, 40));
        }
        level.playSound(null, center, ModSounds.AFTERSHOCK.get(), SoundSource.HOSTILE, 2.0F, 0.7F);
    }

    private static BlockPos ring(BlockPos center, int min, int max, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        int distance = min + random.nextInt(max - min + 1);
        return center.offset((int) Math.round(Math.cos(angle) * distance), 0, (int) Math.round(Math.sin(angle) * distance));
    }

    /**
     * Fester Boden mit Luft darueber nahe {@code around} (sucht 16 Bloecke auf und ab, laedt keine Chunks); null, wenn
     * nichts passt. Im Nether beginnt die Suche auf Spielerhoehe statt an der Decke.
     */
    @Nullable
    public static BlockPos groundNear(ServerLevel level, BlockPos around) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(around.getX() >> 4, around.getZ() >> 4);
        if (chunk == null) {
            return null;
        }
        int surface = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, around.getX() & 15, around.getZ() & 15);
        int top = Math.min(surface, around.getY() + 16);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(around.getX(), top, around.getZ());
        for (int i = 0; i < 32 && cursor.getY() > level.getMinBuildHeight(); i++) {
            BlockState state = chunk.getBlockState(cursor);
            BlockState above = chunk.getBlockState(cursor.above());
            if (state.isSolidRender(level, cursor) && above.isAir()) {
                return cursor.immutable();
            }
            cursor.move(0, -1, 0);
        }
        return null;
    }

    /** Setzt eine Schwaere in den Boden, {@code min}-{@code max} Bloecke vom Mittelpunkt (8 Versuche). */
    @Nullable
    public static BlockPos spawnNodeNear(ServerLevel level, BlockPos center, int min, int max, RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos ground = groundNear(level, ring(center, min, max, random).atY(center.getY()));
            if (ground == null || PurityAnchors.isProtected(level, ground)) {
                continue;
            }
            level.setBlock(ground, ModBlocks.FESTERING_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, ground, ModSounds.FESTERING_NODE_HUM.get(), SoundSource.BLOCKS, 1.5F, 0.6F);
            return ground;
        }
        return null;
    }

    /** Schwaeren im Umkreis (aus der gespeicherten Liste). */
    public static int nodesNear(ServerLevel level, BlockPos center, double radius) {
        int count = 0;
        for (long packed : StoryManager.data(level.getServer()).festeringNodes(level.dimension())) {
            if (BlockPos.of(packed).distSqr(center) <= radius * radius) {
                count++;
            }
        }
        return count;
    }

    /** Sieg ueber Nachhall oder Schwaerenherz. */
    public static void onBossDefeated(ServerLevel level, LivingEntity boss, @Nullable ServerPlayer killer, String type) {
        if (FESTERING_HEART.equals(type)) {
            Purification.purifySphere(level, boss.blockPosition(), ShardlineConfig.HEART_PURIFY_RADIUS.get(), null);
        }
        ShardlineWorldData data = StoryManager.data(level.getServer());
        for (ServerPlayer player : StoryEvents.creditedPlayers(level, boss, killer, CREDIT_RADIUS)) {
            data.addFlag(player.getUUID(), "boss:" + type);
            StoryManager.award(player, "story/" + type);
            StoryManager.send(player, new ScreenEffectPayload(ScreenEffectPayload.PURIFY, 40));
            player.sendSystemMessage(Component.translatable("message.shardline." + type + ".defeated").withStyle(ChatFormatting.GOLD));
        }
    }

    // ------------------------------------------------------------------ Nachbeben

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        int minutes = ShardlineConfig.AFTERSHOCK_MINUTES.get();
        if (minutes <= 0 || StoryManager.data(server).ending() == StoryEnding.NONE) {
            nextAftershock = -1L;
            return;
        }
        long now = server.getTickCount();
        if (nextAftershock < 0L) {
            nextAftershock = now + minutes * 1200L;
            return;
        }
        if (now < nextAftershock) {
            return;
        }
        nextAftershock = now + minutes * 1200L;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            aftershock(player);
        }
    }

    /** Ein Nachbeben fuer einen Spieler (ab Kapitel 7): Klang, Wackeln, eventuell eine neue Schwaere. */
    public static boolean aftershock(ServerPlayer player) {
        if (player.isSpectator() || StoryManager.data(player.server).chapter(player.getUUID()) < StoryChapter.ECHO_OF_THE_WOUND.number()) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        StoryManager.send(player, new ScreenEffectPayload(ScreenEffectPayload.AFTERSHOCK, 50));
        player.playNotifySound(ModSounds.AFTERSHOCK.get(), SoundSource.AMBIENT, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.3F);
        player.displayClientMessage(Component.translatable("message.shardline.aftershock").withStyle(ChatFormatting.DARK_PURPLE), true);
        if (nodesNear(level, player.blockPosition(), 96.0) >= ShardlineConfig.MAX_NODES_NEAR_PLAYER.get()) {
            return false;
        }
        return spawnNodeNear(level, player.blockPosition(), 24, 48, level.getRandom()) != null;
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        nextAftershock = -1L;
    }
}
