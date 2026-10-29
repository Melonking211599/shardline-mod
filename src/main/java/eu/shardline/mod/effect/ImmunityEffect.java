package eu.shardline.mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Immunitaet: Reinheit liegt wie ein Schutzfilm auf dem Koerper. Solange der Effekt wirkt, kann kein
 * Splitterfieber entstehen und Tiere kristallisieren nicht. Vergeben vom Reinheitsanker (solange man in
 * seiner Zone steht) und von der Reinigungsessenz. Die Pruefung steht in {@code virus.VirusEvents}.
 */
public class ImmunityEffect extends MobEffect {
    public ImmunityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4C95A);
    }
}
