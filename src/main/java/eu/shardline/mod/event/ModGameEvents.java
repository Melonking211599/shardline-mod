package eu.shardline.mod.event;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.entity.mob.CrystalGolemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Allgemeine Spiel-Events.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class ModGameEvents {
    private ModGameEvents() {
    }

    /** Bergbau in der Naehe eines Kristallgolems macht ihn wuetend. */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(event.getLevel() instanceof Level level) || level.isClientSide || !event.getState().is(ModTags.Blocks.ANGERS_CRYSTAL_GOLEM)) {
            return;
        }
        double radius = ShardlineConfig.GOLEM_AGGRO_RADIUS.get();
        AABB area = new AABB(event.getPos()).inflate(radius);
        for (CrystalGolemEntity golem : level.getEntitiesOfClass(CrystalGolemEntity.class, area, golem -> golem.isAlive() && golem.hasLineOfSight(player))) {
            golem.provokeBy(player);
        }
    }
}
