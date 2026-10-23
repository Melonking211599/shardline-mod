package eu.shardline.mod.block.custom;

import com.mojang.serialization.MapCodec;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.boss.CrystalColossusEntity;
import eu.shardline.mod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Koloss-Altar im Zentrum der Kristallwald-Arena. Ein Erweckungskristall ruft den Kristallkoloss.
 */
public class ColossusAltarBlock extends BossAltarBlock {
    public static final MapCodec<ColossusAltarBlock> CODEC = simpleCodec(ColossusAltarBlock::new);
    private static final double SPAWN_DISTANCE = 7.0;

    public ColossusAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected Item summoningItem() {
        return ModItems.COLOSSUS_SUMMONING_CRYSTAL.get();
    }

    @Override
    protected String hintKey() {
        return "message.shardline.colossus_altar_hint";
    }

    @Override
    protected boolean summon(ServerLevel level, BlockPos altarPos, Player player) {
        if (!level.getEntitiesOfClass(CrystalColossusEntity.class, new AABB(altarPos).inflate(48.0)).isEmpty()) {
            player.displayClientMessage(Component.translatable("message.shardline.colossus_already_awake"), true);
            return false;
        }
        CrystalColossusEntity colossus = ModEntities.CRYSTAL_COLOSSUS.get().create(level);
        if (colossus == null) {
            return false;
        }
        // Der Koloss erscheint auf der dem Spieler gegenueberliegenden Seite des Altars.
        Vec3 center = Vec3.atBottomCenterOf(altarPos);
        Vec3 away = center.subtract(player.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : away.normalize();
        Vec3 spawn = center.add(away.scale(SPAWN_DISTANCE));
        float yaw = (float) (Mth.atan2(-away.x, away.z) * Mth.RAD_TO_DEG) + 180.0F;
        colossus.moveTo(spawn.x, altarPos.getY(), spawn.z, yaw, 0.0F);
        colossus.setHome(altarPos);
        colossus.finalizeSpawn(level, level.getCurrentDifficultyAt(altarPos), MobSpawnType.EVENT, null);
        level.addFreshEntity(colossus);
        colossus.playAwakening();

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(spawn.x, altarPos.getY(), spawn.z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.playSound(null, altarPos, ModSounds.COLOSSUS_ROAR.get(), SoundSource.HOSTILE, 3.0F, 0.8F);
        announce(level, altarPos, Component.translatable("message.shardline.colossus_awakens"), 64.0);
        return true;
    }
}
