package eu.shardline.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import eu.shardline.mod.entity.mob.ShardCreeperEntity;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;

/**
 * Splitterkriecher: blaeht sich beim Aufladen auf und blitzt weiss, wie der Vanilla-Creeper.
 */
public class ShardCreeperRenderer extends ShardlineGeoRenderer<ShardCreeperEntity> {
    public ShardCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, "shard_creeper", 0.5F, true);
    }

    @Override
    public void preRender(PoseStack poseStack, ShardCreeperEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (isReRender) {
            return;
        }
        float swelling = animatable.getSwelling(partialTick);
        float wobble = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float horizontal = (1.0F + swelling * 0.4F) * wobble;
        float vertical = (1.0F + swelling * 0.1F) / wobble;
        poseStack.scale(horizontal, vertical, horizontal);
    }

    @Override
    public int getPackedOverlay(ShardCreeperEntity animatable, float u, float partialTick) {
        float swelling = animatable.getSwelling(partialTick);
        float white = (int) (swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
        return super.getPackedOverlay(animatable, white, partialTick);
    }
}
