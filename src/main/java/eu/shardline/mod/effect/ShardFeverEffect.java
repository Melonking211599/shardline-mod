package eu.shardline.mod.effect;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.core.ModParticles;
import java.util.Set;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

/**
 * Splitterfieber: langsamer Schaden durch Kristallisierung. Die verzerrte Sicht entsteht auf dem
 * Client ({@code client.story.StoryScreenEffects}). Heilbar mit der Reinigungsessenz und im
 * Umkreis eines Reinheitsankers; Milch nur, wenn in der Config erlaubt.
 */
public class ShardFeverEffect extends MobEffect {
    /** Eigene Heilart, die die Reinigungsessenz und der Reinheitsanker ausloesen. */
    public static final EffectCure PURIFICATION = EffectCure.get("shardline:purification");
    private static final int DAMAGE_INTERVAL = 60;

    public ShardFeverEffect() {
        super(MobEffectCategory.HARMFUL, 0xB04BFF);
    }

    /** Partikeltyp erst zur Laufzeit holen: Effekte werden vor den Partikeltypen registriert. */
    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance effect) {
        return ModParticles.VIRUS_SPORE.get();
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = Math.max(10, DAMAGE_INTERVAL >> amplifier);
        return duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        float damage = ShardlineConfig.FEVER_DAMAGE.get().floatValue();
        if (damage <= 0.0F) {
            return true;
        }
        if (!ShardlineConfig.FEVER_CAN_KILL.get()) {
            damage = Math.min(damage, entity.getHealth() - 1.0F);
            if (damage <= 0.0F) {
                return true;
            }
        }
        entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.SHARD_FEVER, null), damage);
        return true;
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.add(PURIFICATION);
        cures.add(EffectCures.PROTECTED_BY_TOTEM);
        if (ShardlineConfig.FEVER_CURED_BY_MILK.get()) {
            cures.add(EffectCures.MILK);
        }
    }
}
