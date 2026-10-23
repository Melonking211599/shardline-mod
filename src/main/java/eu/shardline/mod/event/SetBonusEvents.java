package eu.shardline.mod.event;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModDamageTypes;
import eu.shardline.mod.item.custom.ShardlineArmorItem;
import eu.shardline.mod.world.dimension.ModDimensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Set-Boni der beiden Ruestungssets.
 * <ul>
 *     <li>Shard-Set: Nachtsicht in der Splitterweite, weniger Schaden durch Splitter-Projektile.</li>
 *     <li>Koloss-Set: Rueckstossimmunitaet, Bonusherzen; der Kristallschild liegt in {@link CrystalShieldHandler}.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class SetBonusEvents {
    private static final ResourceLocation COLOSSUS_HEALTH_ID = Shardline.id("colossus_set_health");
    private static final ResourceLocation COLOSSUS_KNOCKBACK_ID = Shardline.id("colossus_set_knockback");
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private SetBonusEvents() {
    }

    /** Prueft, ob alle vier Ruestungsteile zum angegebenen Set gehoeren. */
    public static boolean wearsFullSet(LivingEntity entity, ShardlineArmorItem.SetType set) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ShardlineArmorItem armor) || armor.getSetType() != set) {
                return false;
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % 10 != 0) {
            return;
        }
        // Shard-Set: Nachtsicht in der Splitterweite.
        if (wearsFullSet(player, ShardlineArmorItem.SetType.SHARD) && player.level().dimension() == ModDimensions.SPLITTERWEITE_LEVEL) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            if (current == null || current.getDuration() < 220) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));
            }
        }
        // Koloss-Set: Attribute nur bei vollem Set.
        boolean colossus = wearsFullSet(player, ShardlineArmorItem.SetType.COLOSSUS);
        updateModifier(player.getAttribute(Attributes.MAX_HEALTH), COLOSSUS_HEALTH_ID,
                colossus ? ShardlineConfig.COLOSSUS_BONUS_HEARTS.get() * 2.0 : 0.0);
        updateModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), COLOSSUS_KNOCKBACK_ID, colossus ? 1.0 : 0.0);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void updateModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(id);
        if (amount <= 0.0) {
            if (existing != null) {
                attribute.removeModifier(id);
            }
        } else if (existing == null || existing.amount() != amount) {
            attribute.removeModifier(id);
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().is(ModDamageTypes.SHARD) && wearsFullSet(entity, ShardlineArmorItem.SetType.SHARD)) {
            float reduction = ShardlineConfig.SHARD_PROJECTILE_RESISTANCE.get().floatValue();
            event.setAmount(event.getAmount() * (1.0F - reduction));
        }
    }
}
