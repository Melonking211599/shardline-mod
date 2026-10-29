package eu.shardline.mod.item.custom;

import eu.shardline.mod.client.renderer.ShardlineArmorRenderer;
import eu.shardline.mod.config.ShardlineConfig;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Ruestungsteil eines Shardline-Sets. Zeigt den Set-Bonus im Tooltip an; die Wirkung selbst
 * liegt in {@link eu.shardline.mod.event.SetBonusEvents}. Getragen wird die Ruestung als GeckoLib-Modell
 * (geo/armor/&lt;set&gt;.geo.json, siehe {@link ShardlineArmorRenderer}); die Vanilla-Ebenen unter
 * textures/models/armor sind nur der Rueckfall.
 */
public class ShardlineArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public enum SetType {
        SHARD, COLOSSUS
    }

    private final SetType setType;

    public ShardlineArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, SetType setType) {
        super(material, type, properties);
        this.setType = setType;
    }

    public SetType getSetType() {
        return this.setType;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ShardlineArmorRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T entity, ItemStack stack,
                    @Nullable EquipmentSlot slot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) {
                    this.renderer = new ShardlineArmorRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.shardline.set_bonus").withStyle(ChatFormatting.GOLD));
        if (this.setType == SetType.SHARD) {
            tooltip.add(Component.translatable("tooltip.shardline.shard_set.night_vision").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.shardline.shard_set.resistance",
                    Math.round(safeResistance() * 100)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.shardline.colossus_set.knockback").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.shardline.colossus_set.hearts", safeBonusHearts()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.shardline.colossus_set.shield",
                    Component.keybind("key.shardline.crystal_shield")).withStyle(ChatFormatting.GRAY));
        }
    }

    private static double safeResistance() {
        return ShardlineConfig.SPEC.isLoaded() ? ShardlineConfig.SHARD_PROJECTILE_RESISTANCE.get() : 0.5;
    }

    private static int safeBonusHearts() {
        return ShardlineConfig.SPEC.isLoaded() ? ShardlineConfig.COLOSSUS_BONUS_HEARTS.get() : 4;
    }
}
