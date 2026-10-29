package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.client.story.StoryClientState;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.ending.EndingManager;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Entscheidungsaltar: waechst beim Bruch vor dem Thron der Koenigin aus dem Boden. Er erwacht nur,
 * wenn ein Spieler mit dem Kern des Bruchs in der Naehe ist (Zustand {@code active}: Leuchtschicht,
 * Licht, Partikel). Wer den Kern auf den Altar legt, oeffnet die Wahl "Versiegeln" oder "Beherrschen"
 * (dieselbe Logik wie am Thron, {@link EndingManager}). Unzerstoerbar wie die Erinnerungskristalle.
 */
public class DecisionAltarBlock extends Block {
    public static final MapCodec<DecisionAltarBlock> CODEC = simpleCodec(DecisionAltarBlock::new);
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    /** Wie nah ein Spieler mit dem Kern sein muss, damit der Altar erwacht. */
    public static final double WAKE_RADIUS = 8.0;
    private static final int CHECK_TICKS = 20;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0),
            Block.box(4.0, 3.0, 4.0, 12.0, 10.0, 12.0),
            Block.box(2.0, 10.0, 2.0, 14.0, 13.0, 14.0));

    public DecisionAltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            serverLevel.scheduleTick(pos, this, CHECK_TICKS);
        }
    }

    /** Jede Sekunde: Ist ein Spieler mit dem Kern des Bruchs in der Naehe? */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, CHECK_TICKS);
        boolean active = isCoreNearby(level, pos);
        if (active != state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_ALL);
            if (active) {
                level.playSound(null, pos, ModSounds.DECISION_ALTAR_AWAKEN.get(), SoundSource.BLOCKS, 1.2F, 1.0F);
                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(WAKE_RADIUS),
                        player -> player.getInventory().hasAnyMatching(stack -> stack.is(ModItems.BREACH_CORE.get())))) {
                    StoryManager.award(player, "story/decision_altar");
                }
            }
        }
    }

    public static boolean isCoreNearby(Level level, BlockPos pos) {
        return level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, WAKE_RADIUS,
                entity -> entity instanceof Player player && !player.isSpectator() && player.getInventory().hasAnyMatching(
                        stack -> stack.is(ModItems.BREACH_CORE.get()))) != null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.BREACH_CORE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            if (!state.getValue(ACTIVE)) {
                serverLevel.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
            }
            EndingManager.onCoreUsed(serverLevel, pos, serverPlayer, stack);
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 40, 0.4, 0.8, 0.4, 0.06);
            serverLevel.sendParticles(ModParticles.VIRUS_SPORE.get(), pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 40, 0.4, 0.8, 0.4, 0.04);
            serverLevel.playSound(null, pos, ModSounds.DECISION_ALTAR_USE.get(), SoundSource.BLOCKS, 1.5F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            StoryEnding ending = StoryManager.data(serverLevel.getServer()).ending();
            String key = switch (ending) {
                case SEALED -> "message.shardline.decision_altar.sealed";
                case DOMINATED -> "message.shardline.decision_altar.dominated";
                default -> "message.shardline.decision_altar.hint";
            };
            player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Partikel (nur Client): ruhend ein schwacher Hauch, erwacht zwei Spiralen - golden fuer das Siegel,
     * magenta fuer die Herrschaft. Nach der Entscheidung bleibt nur die Farbe des gewaehlten Wegs.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.9;
        double cz = pos.getZ() + 0.5;
        if (!state.getValue(ACTIVE)) {
            if (random.nextInt(4) == 0) {
                level.addParticle(ModParticles.VIRUS_SPORE.get(), cx + (random.nextDouble() - 0.5) * 0.6, cy, cz + (random.nextDouble() - 0.5) * 0.6,
                        0.0, 0.01, 0.0);
            }
            return;
        }
        StoryEnding ending = StoryClientState.ending();
        double t = (level.getGameTime() % 60) / 60.0 * Math.PI * 2.0;
        for (int i = 0; i < 2; i++) {
            boolean seal = i == 0;
            if (ending == StoryEnding.SEALED && !seal || ending == StoryEnding.DOMINATED && seal) {
                continue;
            }
            double a = t + (seal ? 0.0 : Math.PI);
            double r = 0.45;
            level.addParticle(seal ? ParticleTypes.END_ROD : ModParticles.VIRUS_SPORE.get(), cx + Math.cos(a) * r, cy + 0.15,
                    cz + Math.sin(a) * r, 0.0, 0.035, 0.0);
        }
        if (random.nextInt(3) == 0) {
            float a = random.nextFloat() * Mth.TWO_PI;
            level.addParticle(ParticleTypes.ENCHANT, cx + Mth.cos(a) * 1.4, cy + 0.6, cz + Mth.sin(a) * 1.4,
                    -Mth.cos(a) * 1.2, -0.3, -Mth.sin(a) * 1.2);
        }
    }

    // ------------------------------------------------------------------ Entstehen beim Bruch

    /** Position vor dem Thron: zwei Bloecke tiefer (Arenaboden) und fuenf nach Sueden, wo die Vision "Das Ritual" steht. */
    public static BlockPos spotBeforeThrone(BlockPos throne) {
        return throne.offset(0, -2, 5);
    }

    /**
     * Laesst den Altar beim Tod der Koenigin entstehen: vor dem Thron, sonst am Todesort auf dem Boden.
     * Gibt es im Umkreis von 12 Bloecken schon einen Altar (neue Arena, zweiter Kampf), passiert nichts.
     *
     * @return die Position des Altars oder {@code null}, wenn kein Platz gefunden wurde
     */
    @Nullable
    public static BlockPos raise(ServerLevel level, @Nullable BlockPos throne, BlockPos deathPos) {
        BlockPos center = throne != null ? throne : deathPos;
        BlockPos existing = findNearby(level, center, 12);
        if (existing != null) {
            return existing;
        }
        BlockPos target = throne != null ? spotBeforeThrone(throne)
                : level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, deathPos);
        BlockPos spot = findFreeSpot(level, target);
        if (spot == null) {
            return null;
        }
        level.setBlock(spot, ModBlocks.DECISION_ALTAR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.sendParticles(ParticleTypes.END_ROD, spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5, 60, 0.5, 1.2, 0.5, 0.08);
        level.sendParticles(ModParticles.VIRUS_SPORE.get(), spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5, 60, 0.5, 1.2, 0.5, 0.05);
        level.playSound(null, spot, ModSounds.DECISION_ALTAR_RISE.get(), SoundSource.BLOCKS, 2.5F, 0.9F);
        Component text = Component.translatable("message.shardline.decision_altar.risen").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(spot).inflate(96.0))) {
            player.sendSystemMessage(text);
        }
        return spot;
    }

    @Nullable
    public static BlockPos findNearby(Level level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))) {
            if (level.getBlockState(pos).is(ModBlocks.DECISION_ALTAR.get())) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** Sucht ab der Zielposition auf- und abwaerts einen Platz mit festem Boden und zwei Bloecken Luft. */
    @Nullable
    private static BlockPos findFreeSpot(ServerLevel level, BlockPos target) {
        for (int i = 0; i <= 6; i++) {
            for (int dy : i == 0 ? new int[] {0} : new int[] {i, -i}) {
                BlockPos pos = target.above(dy);
                if (level.getBlockState(pos).canBeReplaced() && level.getBlockState(pos.above()).canBeReplaced()
                        && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
                    return pos;
                }
            }
        }
        return null;
    }
}
