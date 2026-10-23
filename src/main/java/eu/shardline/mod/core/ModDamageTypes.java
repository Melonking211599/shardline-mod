package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Eigene Schadenstypen. Die JSON-Dateien werden per Datengenerator (ModWorldGenProvider) erzeugt.
 */
public final class ModDamageTypes {
    /** Splitter-Projektile aller Art. Das Shard-Set reduziert diesen Schaden. */
    public static final ResourceKey<DamageType> SHARD = ResourceKey.create(Registries.DAMAGE_TYPE, Shardline.id("shard"));
    /** Schaden in einer Void-Zone der Splitterkoenigin. */
    public static final ResourceKey<DamageType> VOID_ZONE = ResourceKey.create(Registries.DAMAGE_TYPE, Shardline.id("void_zone"));
    /** Kristallsaeulen des Kolosses. */
    public static final ResourceKey<DamageType> CRYSTAL_PILLAR = ResourceKey.create(Registries.DAMAGE_TYPE, Shardline.id("crystal_pillar"));

    private ModDamageTypes() {
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(SHARD, new DamageType("shardline.shard", 0.1F));
        context.register(VOID_ZONE, new DamageType("shardline.void_zone", 0.0F));
        context.register(CRYSTAL_PILLAR, new DamageType("shardline.crystal_pillar", 0.1F));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity direct, @Nullable Entity causing) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), direct, causing);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity causing) {
        return source(level, type, causing, causing);
    }
}
