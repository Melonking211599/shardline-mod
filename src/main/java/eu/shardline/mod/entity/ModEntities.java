package eu.shardline.mod.entity;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.boss.CrystalColossusEntity;
import eu.shardline.mod.entity.boss.QueenCloneEntity;
import eu.shardline.mod.entity.boss.ShardQueenEntity;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import eu.shardline.mod.entity.infected.CrystallizedCowEntity;
import eu.shardline.mod.entity.infected.ShardZombieEntity;
import eu.shardline.mod.entity.misc.CrystalPillarEntity;
import eu.shardline.mod.entity.misc.VoidZoneEntity;
import eu.shardline.mod.entity.mob.AshWalkerEntity;
import eu.shardline.mod.entity.mob.CrystalGolemEntity;
import eu.shardline.mod.entity.mob.CrystalSpiderEntity;
import eu.shardline.mod.entity.mob.GlowMothEntity;
import eu.shardline.mod.entity.mob.ShadowFoxEntity;
import eu.shardline.mod.entity.mob.ShardCreeperEntity;
import eu.shardline.mod.entity.mob.ShardScoutEntity;
import eu.shardline.mod.entity.mob.ShardTraderEntity;
import eu.shardline.mod.entity.mob.ShatteredKnightEntity;
import eu.shardline.mod.entity.mob.VoidRayEntity;
import eu.shardline.mod.entity.projectile.ShardProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Shardline.MODID);

    // --- Mobs ---
    public static final DeferredHolder<EntityType<?>, EntityType<ShardCreeperEntity>> SHARD_CREEPER = register("shard_creeper",
            EntityType.Builder.of(ShardCreeperEntity::new, MobCategory.MONSTER).sized(0.6F, 1.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalGolemEntity>> CRYSTAL_GOLEM = register("crystal_golem",
            EntityType.Builder.of(CrystalGolemEntity::new, MobCategory.CREATURE).sized(1.4F, 2.7F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<GlowMothEntity>> GLOW_MOTH = register("glow_moth",
            EntityType.Builder.of(GlowMothEntity::new, MobCategory.AMBIENT).sized(0.5F, 0.5F).eyeHeight(0.3F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<AshWalkerEntity>> ASH_WALKER = register("ash_walker",
            EntityType.Builder.of(AshWalkerEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).fireImmune().clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<ShardScoutEntity>> SHARD_SCOUT = register("shard_scout",
            EntityType.Builder.of(ShardScoutEntity::new, MobCategory.MONSTER).sized(0.7F, 0.9F).eyeHeight(0.45F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<VoidRayEntity>> VOID_RAY = register("void_ray",
            EntityType.Builder.of(VoidRayEntity::new, MobCategory.CREATURE).sized(2.4F, 0.6F).eyeHeight(0.4F)
                    .passengerAttachments(0.55F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalSpiderEntity>> CRYSTAL_SPIDER = register("crystal_spider",
            EntityType.Builder.of(CrystalSpiderEntity::new, MobCategory.MONSTER).sized(1.4F, 0.9F).eyeHeight(0.65F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<ShardTraderEntity>> SHARD_TRADER = register("shard_trader",
            EntityType.Builder.of(ShardTraderEntity::new, MobCategory.CREATURE).sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<ShadowFoxEntity>> SHADOW_FOX = register("shadow_fox",
            EntityType.Builder.of(ShadowFoxEntity::new, MobCategory.MONSTER).sized(0.6F, 0.7F).eyeHeight(0.4F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<ShatteredKnightEntity>> SHATTERED_KNIGHT = register("shattered_knight",
            EntityType.Builder.of(ShatteredKnightEntity::new, MobCategory.MONSTER).sized(0.7F, 2.2F).eyeHeight(1.9F).clientTrackingRange(8));

    // --- Bosse ---
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalColossusEntity>> CRYSTAL_COLOSSUS = register("crystal_colossus",
            EntityType.Builder.of(CrystalColossusEntity::new, MobCategory.MONSTER).sized(2.2F, 4.4F).eyeHeight(3.8F)
                    .fireImmune().clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<ShardQueenEntity>> SHARD_QUEEN = register("shard_queen",
            EntityType.Builder.of(ShardQueenEntity::new, MobCategory.MONSTER).sized(1.2F, 3.0F).eyeHeight(2.6F)
                    .fireImmune().clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<QueenCloneEntity>> QUEEN_CLONE = register("queen_clone",
            EntityType.Builder.of(QueenCloneEntity::new, MobCategory.MONSTER).sized(1.0F, 2.6F).eyeHeight(2.2F)
                    .fireImmune().clientTrackingRange(10));

    // --- Infizierte (Shardvirus) ---
    public static final DeferredHolder<EntityType<?>, EntityType<ShardZombieEntity>> SHARD_ZOMBIE = register("shard_zombie",
            EntityType.Builder.of(ShardZombieEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F)
                    .passengerAttachments(2.0125F).ridingOffset(-0.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<CrystallizedCowEntity>> CRYSTALLIZED_COW = register("crystallized_cow",
            EntityType.Builder.of(CrystallizedCowEntity::new, MobCategory.CREATURE).sized(0.9F, 1.4F).eyeHeight(1.3F)
                    .passengerAttachments(1.36875F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<VirusHeartEntity>> VIRUS_HEART = register("virus_heart",
            EntityType.Builder.of(VirusHeartEntity::new, MobCategory.MONSTER).sized(1.8F, 2.4F).eyeHeight(1.6F)
                    .fireImmune().clientTrackingRange(10));

    // --- Projektile & Effekte ---
    public static final DeferredHolder<EntityType<?>, EntityType<ShardProjectileEntity>> SHARD_PROJECTILE = register("shard_projectile",
            EntityType.Builder.<ShardProjectileEntity>of(ShardProjectileEntity::new, MobCategory.MISC).sized(0.3F, 0.3F)
                    .clientTrackingRange(6).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalPillarEntity>> CRYSTAL_PILLAR = register("crystal_pillar",
            EntityType.Builder.<CrystalPillarEntity>of(CrystalPillarEntity::new, MobCategory.MISC).sized(1.0F, 2.5F)
                    .clientTrackingRange(6).updateInterval(2));
    public static final DeferredHolder<EntityType<?>, EntityType<VoidZoneEntity>> VOID_ZONE = register("void_zone",
            EntityType.Builder.<VoidZoneEntity>of(VoidZoneEntity::new, MobCategory.MISC).sized(3.0F, 0.5F).fireImmune()
                    .clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));

    private ModEntities() {
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(Shardline.MODID + ":" + name));
    }
}
