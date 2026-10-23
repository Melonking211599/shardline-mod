package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.mob.CrystalGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Kristallgolem: wechselt die Textur (rote Kristalle), sobald er wuetend ist.
 */
public class CrystalGolemRenderer extends ShardlineGeoRenderer<CrystalGolemEntity> {
    private static final ResourceLocation ANGRY_TEXTURE = Shardline.id("textures/entity/crystal_golem_angry.png");

    public CrystalGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<CrystalGolemEntity>(Shardline.id("crystal_golem"), true) {
            @Override
            public ResourceLocation getTextureResource(CrystalGolemEntity golem) {
                return golem.isAngryVisual() ? ANGRY_TEXTURE : super.getTextureResource(golem);
            }
        }, 1.0F, true);
    }
}
