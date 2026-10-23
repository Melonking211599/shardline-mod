package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.story.lore.Visions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Erinnerungskristall: Rechtsklick zeigt eine Vision aus der Vergangenheit der Splitterweite.
 * Welche Vision, bestimmt die Blockeigenschaft {@code vision} (wird von den Strukturen gesetzt).
 */
public class MemoryCrystalBlock extends Block {
    public static final MapCodec<MemoryCrystalBlock> CODEC = simpleCodec(MemoryCrystalBlock::new);
    public static final IntegerProperty VISION = IntegerProperty.create("vision", 0, Visions.COUNT - 1);
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
    private static final int COOLDOWN_TICKS = 100;
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    public MemoryCrystalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VISION, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VISION);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            long now = serverLevel.getGameTime();
            Long last = LAST_USE.get(player.getUUID());
            if (last != null && now - last < COOLDOWN_TICKS && now >= last) {
                return InteractionResult.CONSUME;
            }
            LAST_USE.put(player.getUUID(), now);
            Visions.show(serverPlayer, serverLevel, pos, state.getValue(VISION));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(ModParticles.SHARD_SPARKLE.get(),
                    pos.getX() + 0.25 + random.nextDouble() * 0.5, pos.getY() + 0.3 + random.nextDouble() * 0.8,
                    pos.getZ() + 0.25 + random.nextDouble() * 0.5, 0.0, 0.02, 0.0);
        }
    }
}
