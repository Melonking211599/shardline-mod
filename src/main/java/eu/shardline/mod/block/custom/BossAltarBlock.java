package eu.shardline.mod.block.custom;

import eu.shardline.mod.core.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Gemeinsame Basis der Boss-Altaere. Unzerstoerbar, nimmt ein Beschwoerungsitem an
 * und ruft den zugehoerigen Boss herbei.
 */
public abstract class BossAltarBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(2.0, 4.0, 2.0, 14.0, 10.0, 14.0),
            Block.box(5.0, 10.0, 5.0, 11.0, 16.0, 11.0));

    protected BossAltarBlock(Properties properties) {
        super(properties);
    }

    /** Das Item, das den Boss beschwoert. */
    protected abstract Item summoningItem();

    /** Uebersetzungsschluessel des Hinweises ohne passendes Item. */
    protected abstract String hintKey();

    /**
     * Versucht den Boss zu beschwoeren.
     *
     * @return true, wenn der Boss erschienen ist und das Item verbraucht werden soll
     */
    protected abstract boolean summon(ServerLevel level, BlockPos altarPos, Player player);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(this.summoningItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (!this.summon(serverLevel, pos, player)) {
                return ItemInteractionResult.CONSUME;
            }
            stack.consume(1, player);
            serverLevel.sendParticles(ModParticles.SHARD_SPARKLE.get(), pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    80, 0.6, 0.8, 0.6, 0.15);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(this.hintKey()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ModParticles.SHARD_SPARKLE.get(),
                    pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.04, 0.0);
        }
    }

    /** Sendet allen Spielern im Umkreis eine Nachricht in der Aktionsleiste. */
    protected static void announce(ServerLevel level, BlockPos pos, Component message, double radius) {
        AABB area = new AABB(pos).inflate(radius);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.displayClientMessage(message, true);
        }
    }
}
