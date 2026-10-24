package eu.shardline.mod.virus;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.effect.ModEffects;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import eu.shardline.mod.entity.infected.InfectedMob;
import eu.shardline.mod.story.StoryManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Spielereignisse rund um den Virus: Immunitaet gegen Splitterfieber und fliehende Dorfbewohner.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class VirusEvents {
    private VirusEvents() {
    }

    @SubscribeEvent
    static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!event.getEffectInstance().is(ModEffects.SHARD_FEVER) || event.getEntity().level().isClientSide) {
            return;
        }
        if (VirusEffects.isImmune(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        } else if (event.getEntity().hasEffect(ModEffects.IMMUNITY)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            if (event.getEntity() instanceof ServerPlayer player) {
                StoryManager.award(player, "story/immunity");
            }
        }
    }

    /** Dorfbewohner und Haendler fliehen vor infizierten Mobs und Virusherzen. */
    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractVillager villager)
                || !ShardlineConfig.VILLAGERS_FLEE.get()) {
            return;
        }
        villager.goalSelector.addGoal(1, new AvoidEntityGoal<>(villager, LivingEntity.class, 10.0F, 0.6, 0.75,
                entity -> entity instanceof InfectedMob infected && !infected.isTamed() || entity instanceof VirusHeartEntity));
    }
}
