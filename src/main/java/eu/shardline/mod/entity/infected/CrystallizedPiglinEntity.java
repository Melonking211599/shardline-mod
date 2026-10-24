package eu.shardline.mod.entity.infected;

import eu.shardline.mod.core.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Kristallisierter Piglin (Glutbrand): ein vom Nether-Stamm befallener Piglin. Verhaelt sich wie ein Shard-Zombie
 * (jagt Spieler und Dorfbewohner, steckt mit Splitterfieber an, streut Infektion, zaehmbar durch Splitterherrscher),
 * ist aber feuerfest, schneller und traegt ein Goldschwert. Entsteht aus Piglins, Piglin-Barbaren und
 * Zombifizierten Piglins auf befallenem Boden; die Reinigungsessenz macht ihn wieder zum Piglin.
 */
public class CrystallizedPiglinEntity extends ShardZombieEntity {
    public CrystallizedPiglinEntity(EntityType<? extends ShardZombieEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ShardZombieEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.6F) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        }
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRYSTALLIZED_PIGLIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRYSTALLIZED_PIGLIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYSTALLIZED_PIGLIN_DEATH.get();
    }
}
