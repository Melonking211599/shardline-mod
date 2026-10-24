package eu.shardline.mod.entity;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.boss.CrystalColossusEntity;
import eu.shardline.mod.entity.boss.QueenCloneEntity;
import eu.shardline.mod.entity.boss.ShardQueenEntity;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import eu.shardline.mod.entity.infected.CrystallizedCowEntity;
import eu.shardline.mod.entity.infected.CrystallizedPigEntity;
import eu.shardline.mod.entity.infected.CrystallizedSheepEntity;
import eu.shardline.mod.entity.infected.CrystallizedVillagerEntity;
import eu.shardline.mod.entity.infected.ShardZombieEntity;
import eu.shardline.mod.entity.mob.AshWalkerEntity;
import eu.shardline.mod.entity.mob.CrystalGolemEntity;
import eu.shardline.mod.entity.mob.CrystalSnailEntity;
import eu.shardline.mod.entity.mob.CrystalSpiderEntity;
import eu.shardline.mod.entity.mob.GlowMothEntity;
import eu.shardline.mod.entity.mob.ShadowFoxEntity;
import eu.shardline.mod.entity.mob.ShardCreeperEntity;
import eu.shardline.mod.entity.mob.ShardScoutEntity;
import eu.shardline.mod.entity.mob.ShardTraderEntity;
import eu.shardline.mod.entity.mob.ShatteredKnightEntity;
import eu.shardline.mod.entity.mob.VoidRayEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Attribute und Spawnplatzierungen aller Shardline-Mobs (Mod-Event-Bus).
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class ModEntityEvents {
    private ModEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SHARD_CREEPER.get(), ShardCreeperEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_GOLEM.get(), CrystalGolemEntity.createAttributes().build());
        event.put(ModEntities.GLOW_MOTH.get(), GlowMothEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_SNAIL.get(), CrystalSnailEntity.createAttributes().build());
        event.put(ModEntities.RHEA.get(), eu.shardline.mod.entity.npc.AftermathNpcEntity.createAttributes().build());
        event.put(ModEntities.VEYL.get(), eu.shardline.mod.entity.npc.AftermathNpcEntity.createAttributes().build());
        event.put(ModEntities.ILSA.get(), eu.shardline.mod.entity.npc.AftermathNpcEntity.createAttributes().build());
        event.put(ModEntities.KESH.get(), eu.shardline.mod.entity.npc.AftermathNpcEntity.createAttributes().build());
        event.put(ModEntities.ASH_WALKER.get(), AshWalkerEntity.createAttributes().build());
        event.put(ModEntities.SHARD_SCOUT.get(), ShardScoutEntity.createAttributes().build());
        event.put(ModEntities.VOID_RAY.get(), VoidRayEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_SPIDER.get(), CrystalSpiderEntity.createAttributes().build());
        event.put(ModEntities.SHARD_TRADER.get(), ShardTraderEntity.createAttributes().build());
        event.put(ModEntities.SHADOW_FOX.get(), ShadowFoxEntity.createAttributes().build());
        event.put(ModEntities.SHATTERED_KNIGHT.get(), ShatteredKnightEntity.createAttributes().build());
        event.put(ModEntities.CRYSTAL_COLOSSUS.get(), CrystalColossusEntity.createAttributes().build());
        event.put(ModEntities.SHARD_QUEEN.get(), ShardQueenEntity.createAttributes().build());
        event.put(ModEntities.QUEEN_CLONE.get(), QueenCloneEntity.createAttributes().build());
        event.put(ModEntities.SHARD_ZOMBIE.get(), ShardZombieEntity.createAttributes().build());
        event.put(ModEntities.CRYSTALLIZED_PIGLIN.get(), eu.shardline.mod.entity.infected.CrystallizedPiglinEntity.createAttributes().build());
        event.put(ModEntities.CRYSTALLIZED_ENDERMAN.get(), eu.shardline.mod.entity.infected.CrystallizedEndermanEntity.createAttributes().build());
        event.put(ModEntities.CRYSTALLIZED_COW.get(), CrystallizedCowEntity.createAttributes().build());
        event.put(ModEntities.CRYSTALLIZED_SHEEP.get(), CrystallizedSheepEntity.createAttributes().build());
        event.put(ModEntities.CRYSTALLIZED_PIG.get(), CrystallizedPigEntity.createAttributes().build());
        event.put(ModEntities.CRYSTALLIZED_VILLAGER.get(), CrystallizedVillagerEntity.createAttributes().build());
        event.put(ModEntities.VIRUS_HEART.get(), VirusHeartEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        RegisterSpawnPlacementsEvent.Operation replace = RegisterSpawnPlacementsEvent.Operation.REPLACE;
        Heightmap.Types ground = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;

        event.register(ModEntities.SHARD_CREEPER.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.hostileGround("shard_creeper", type, level, spawnType, pos, random), replace);
        event.register(ModEntities.ASH_WALKER.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.hostileGround("ash_walker", type, level, spawnType, pos, random), replace);
        event.register(ModEntities.CRYSTAL_SPIDER.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.hostileGround("crystal_spider", type, level, spawnType, pos, random), replace);
        event.register(ModEntities.SHADOW_FOX.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.hostileGround("shadow_fox", type, level, spawnType, pos, random), replace);

        event.register(ModEntities.CRYSTAL_GOLEM.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.splitterweiteGround("crystal_golem", type, level, spawnType, pos, random), replace);
        event.register(ModEntities.CRYSTAL_SNAIL.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.splitterweiteGround("crystal_snail", type, level, spawnType, pos, random), replace);
        event.register(ModEntities.SHARD_TRADER.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.splitterweiteGround("shard_trader", type, level, spawnType, pos, random), replace);
        event.register(ModEntities.SHATTERED_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (type, level, spawnType, pos, random) -> ModSpawnRules.splitterweiteHostile("shattered_knight", type, level, spawnType, pos, random), replace);

        event.register(ModEntities.SHARD_SCOUT.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
                (type, level, spawnType, pos, random) -> ModSpawnRules.splitterweiteAir("shard_scout", true, level, spawnType, pos, random), replace);
        event.register(ModEntities.VOID_RAY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
                (type, level, spawnType, pos, random) -> ModSpawnRules.splitterweiteAir("void_ray", false, level, spawnType, pos, random), replace);
        event.register(ModEntities.GLOW_MOTH.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING,
                ModSpawnRules::glowMoth, replace);
    }
}
