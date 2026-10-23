package eu.shardline.mod.effect;

import eu.shardline.mod.Shardline;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Statuseffekte der Mod. Symbol: textures/mob_effect/&lt;name&gt;.png
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Shardline.MODID);

    public static final DeferredHolder<MobEffect, ShardFeverEffect> SHARD_FEVER = MOB_EFFECTS.register("shard_fever", ShardFeverEffect::new);

    private ModEffects() {
    }
}
