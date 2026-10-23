package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Shardline.MODID);

    /** Funkelnde Kristallsplitter (Portal, Shard-Erz, Kristallschild). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHARD_SPARKLE =
            PARTICLE_TYPES.register("shard_sparkle", () -> new SimpleParticleType(false));
    /** Dunkle Void-Partikel (Void-Zonen der Koenigin, Void-Rochen). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_MOTE =
            PARTICLE_TYPES.register("void_mote", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}
