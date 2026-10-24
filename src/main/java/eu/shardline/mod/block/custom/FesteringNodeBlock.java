package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.infected.InfectedMobs;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.quest.Quests;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.VirusSpreadManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Schwaere: eine Stelle, an der die Wunde der Weite nachhallt (Kapitel 7-9). Entsteht bei Nachbeben und durch Dialoge.
 * <ul>
 *   <li>Versiegelte Welt: eine Narbe. Sie breitet sich nicht aus und singt nur leise.</li>
 *   <li>Beherrschte Welt: Der Ton singt gegen den Herrscher. Die Schwaere streut Infektion und ruft Befallene, die
 *   auch Splitterherrscher angreifen ("Gegengesang").</li>
 * </ul>
 * Mit der Stimmgabel der Hueter oder Reinigungsessenz wird sie zum Schweigen gebracht: gereinigt (alle ausser einem
 * Herrscher in der beherrschten Welt) oder gebunden (Herrscher). Beides gibt Echosplitter und zaehlt fuer Auftraege.
 * Wer sie einfach abbaut, bekommt nichts und weckt zwei Befallene.
 */
public class FesteringNodeBlock extends Block {
    public static final MapCodec<FesteringNodeBlock> CODEC = simpleCodec(FesteringNodeBlock::new);
    /** Ziel fuer Auftragsschritte vom Typ "custom" (gereinigt oder gebunden). */
    public static final String QUEST_TARGET = "shardline:festering_node";
    private static final int PURIFY_RADIUS = 4;

    public FesteringNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            StoryManager.data(serverLevel.getServer()).addFesteringNode(serverLevel.dimension(), pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !newState.is(this)) {
            StoryManager.data(serverLevel.getServer()).removeFesteringNode(serverLevel.dimension(), pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        boolean fork = stack.is(ModItems.KEEPER_TUNING_FORK.get());
        if (!fork && !stack.is(ModItems.PURIFICATION_ESSENCE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            silence(serverLevel, pos, serverPlayer);
            if (fork) {
                stack.hurtAndBreak(1, serverPlayer, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            } else {
                stack.consume(1, player);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Bringt die Schwaere zum Schweigen: reinigen oder (Herrscher in der beherrschten Welt) binden. */
    public static void silence(ServerLevel level, BlockPos pos, ServerPlayer player) {
        ShardlineWorldData data = StoryManager.data(level.getServer());
        boolean bind = data.ending() == StoryEnding.DOMINATED && StoryManager.isRuler(player);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (bind) {
            level.sendParticles(ModParticles.VIRUS_SPORE.get(), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 40, 0.4, 0.4, 0.4, 0.02);
            level.playSound(null, pos, ModSounds.FESTERING_NODE_BIND.get(), SoundSource.BLOCKS, 1.0F, 0.8F);
        } else {
            Purification.purifySphere(level, pos, PURIFY_RADIUS, null);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 30, 0.4, 0.5, 0.4, 0.05);
            level.playSound(null, pos, ModSounds.FESTERING_NODE_PURIFY.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
        }
        Block.popResource(level, pos, new ItemStack(ModItems.ECHO_SHARD.get(), 2 + level.getRandom().nextInt(2)));
        player.displayClientMessage(Component.translatable(bind ? "message.shardline.festering_node.bound" : "message.shardline.festering_node.purified")
                .withStyle(bind ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA), true);
        StoryManager.award(player, "story/festering_node");
        Quests.trigger(player, bind ? "bind_node" : "purify_node", QUEST_TARGET);
        Quests.trigger(player, "custom", QUEST_TARGET);
    }

    /** Abbauen ohne Stimmgabel: der Ton schreit, zwei Befallene erscheinen. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel && !player.isCreative()) {
            for (int i = 0; i < 2; i++) {
                summonCounterSong(serverLevel, pos, serverLevel.getRandom());
            }
            serverLevel.playSound(null, pos, ModSounds.FESTERING_NODE_SCREAM.get(), SoundSource.BLOCKS, 1.5F, 0.7F);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Beherrschte Welt: streut Infektion und ruft gelegentlich Befallene, die auch den Herrscher angreifen. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (StoryManager.data(level.getServer()).ending() != StoryEnding.DOMINATED) {
            return;
        }
        VirusSpreadManager.spreadFrom(level, pos, random);
        if (random.nextInt(4) == 0 && level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(10.0),
                mob -> InfectedMobs.isCounterSong(mob)).size() < 3) {
            summonCounterSong(level, pos, random);
        }
    }

    private static void summonCounterSong(ServerLevel level, BlockPos pos, RandomSource random) {
        Mob mob = (level.dimension() == Level.NETHER ? ModEntities.CRYSTALLIZED_PIGLIN.get() : ModEntities.SHARD_ZOMBIE.get()).create(level);
        if (mob == null) {
            return;
        }
        BlockPos spot = pos.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
        while (spot.getY() < pos.getY() + 4 && !level.getBlockState(spot).isAir()) {
            spot = spot.above();
        }
        mob.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.MOB_SUMMONED, null);
        InfectedMobs.markCounterSong(mob);
        level.addFreshEntity(mob);
        level.sendParticles(ModParticles.VIRUS_SPORE.get(), mob.getX(), mob.getY() + 1.0, mob.getZ(), 20, 0.3, 0.5, 0.3, 0.03);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ModParticles.VIRUS_SPORE.get(), pos.getX() + random.nextDouble(), pos.getY() + 1.05,
                    pos.getZ() + random.nextDouble(), 0.0, 0.03, 0.0);
        }
        if (random.nextInt(60) == 0) {
            level.playLocalSound(pos, ModSounds.FESTERING_NODE_HUM.get(), SoundSource.BLOCKS, 0.6F, 0.8F + random.nextFloat() * 0.3F, false);
        }
    }
}
