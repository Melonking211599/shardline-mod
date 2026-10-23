package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.mob.VoidRayEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Void-Rochen: der Bone "saddle" ist nur sichtbar, wenn der Rochen gesattelt ist.
 */
public class VoidRayRenderer extends ShardlineGeoRenderer<VoidRayEntity> {
    public VoidRayRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<VoidRayEntity>(Shardline.id("void_ray"), false) {
            @Override
            public void setCustomAnimations(VoidRayEntity ray, long instanceId, AnimationState<VoidRayEntity> animationState) {
                super.setCustomAnimations(ray, instanceId, animationState);
                GeoBone saddle = this.getAnimationProcessor().getBone("saddle");
                if (saddle != null) {
                    saddle.setHidden(!ray.isSaddled());
                }
            }
        }, 0.8F, true);
    }
}
