package eu.shardline.mod.story.ending;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.story.StoryManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Ende "Beherrschen": Die Wesen der Splitterweite erkennen die Splitterherrscher an. Alle Typen im Tag
 * {@code shardline:obeys_ruler} (Splitterkriecher, Kristallgolem, Aschewandler, Splitterspaeher, Kristallspinne,
 * Schattenfuchs, Gesplitterter Ritter, Void-Rochen, Klone der Koenigin) nehmen keinen Splitterherrscher als Ziel und
 * fuegen ihm keinen Schaden zu - auch nicht mit Geschossen oder Explosionen. Die Bosse bleiben feindlich.
 * Infizierte Mobs und Virusherzen waren schon vorher friedlich zu Herrschern.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class RulerAllegiance {
    private RulerAllegiance() {
    }

    /** Ob dieses Wesen einem Splitterherrscher gehorcht. */
    public static boolean obeys(Entity entity, LivingEntity target) {
        return entity.getType().is(ModTags.EntityTypes.OBEYS_RULER) && target instanceof ServerPlayer player && StoryManager.isRuler(player);
    }

    @SubscribeEvent
    static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target != null && !event.getEntity().level().isClientSide && obeys(event.getEntity(), target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && !event.getEntity().level().isClientSide && obeys(attacker, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** Wesen, die schon ein Ziel hatten, bevor der Spieler Herrscher wurde, lassen einmal pro Sekunde davon ab. */
    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && mob.tickCount % 20 == 0 && !mob.level().isClientSide && mob.getTarget() != null
                && obeys(mob, mob.getTarget())) {
            mob.setTarget(null);
        }
    }
}
