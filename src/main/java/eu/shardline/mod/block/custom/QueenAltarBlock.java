package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.boss.ShardQueenEntity;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.ending.EndingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Thron der Splitterweite im Zentrum der Dimension. Ein Kolosskern ruft die Splitterkoenigin.
 */
public class QueenAltarBlock extends BossAltarBlock {
    public static final MapCodec<QueenAltarBlock> CODEC = simpleCodec(QueenAltarBlock::new);

    public QueenAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Der Kern des Bruchs startet am Thron die Endentscheidung. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModItems.BREACH_CORE.get())) {
            if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
                EndingManager.onCoreUsed(serverLevel, pos, serverPlayer, stack);
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 60, 0.6, 1.0, 0.6, 0.08);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected Item summoningItem() {
        return ModItems.COLOSSUS_CORE.get();
    }

    @Override
    protected String hintKey() {
        return "message.shardline.queen_altar_hint";
    }

    @Override
    protected boolean summon(ServerLevel level, BlockPos altarPos, Player player) {
        if (!level.getEntitiesOfClass(ShardQueenEntity.class, new AABB(altarPos).inflate(96.0)).isEmpty()) {
            player.displayClientMessage(Component.translatable("message.shardline.queen_already_present"), true);
            return false;
        }
        ShardQueenEntity queen = ModEntities.SHARD_QUEEN.get().create(level);
        if (queen == null) {
            return false;
        }
        queen.moveTo(altarPos.getX() + 0.5, altarPos.getY() + 8.0, altarPos.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
        queen.setHome(altarPos);
        queen.finalizeSpawn(level, level.getCurrentDifficultyAt(altarPos), MobSpawnType.EVENT, null);
        level.addFreshEntity(queen);

        level.sendParticles(ParticleTypes.REVERSE_PORTAL, altarPos.getX() + 0.5, altarPos.getY() + 8.0, altarPos.getZ() + 0.5,
                200, 1.5, 2.0, 1.5, 0.2);
        level.playSound(null, altarPos, ModSounds.QUEEN_SCREAM.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
        announce(level, altarPos, Component.translatable("message.shardline.queen_awakens"), 96.0);
        return true;
    }
}
