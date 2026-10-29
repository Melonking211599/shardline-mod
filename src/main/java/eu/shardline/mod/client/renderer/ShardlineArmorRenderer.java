package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.item.custom.ShardlineArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Traegt die Shardline-Ruestungen als GeckoLib-Modell: geo/armor/&lt;set&gt;.geo.json mit den Bones armorHead, armorBody,
 * armorRightArm, armorLeftArm, armorRightLeg, armorLeftLeg, armorRightBoot und armorLeftBoot. Textur
 * textures/armor/&lt;set&gt;.png, die leuchtenden Pixel stehen in &lt;set&gt;_glowmask.png. Erzeugt von tools/assetgen/armor_models.py.
 */
public class ShardlineArmorRenderer extends GeoArmorRenderer<ShardlineArmorItem> {
    public ShardlineArmorRenderer() {
        super(new SetModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    private static final class SetModel extends GeoModel<ShardlineArmorItem> {
        private static String set(ShardlineArmorItem item) {
            return item.getSetType().name().toLowerCase(java.util.Locale.ROOT);
        }

        @Override
        public ResourceLocation getModelResource(ShardlineArmorItem item) {
            return Shardline.id("geo/armor/" + set(item) + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(ShardlineArmorItem item) {
            return Shardline.id("textures/armor/" + set(item) + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(ShardlineArmorItem item) {
            return Shardline.id("animations/armor/" + set(item) + ".animation.json");
        }
    }
}
