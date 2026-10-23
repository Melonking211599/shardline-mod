package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.boss.QueenCloneEntity;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.util.Color;

/**
 * Klon der Splitterkoenigin: nutzt Modell und Animationen der Koenigin, aber durchscheinend
 * und mit eigener Textur (textures/entity/queen_clone.png).
 */
public class QueenCloneRenderer extends ShardlineGeoRenderer<QueenCloneEntity> {
    public QueenCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<QueenCloneEntity>(Shardline.id("shard_queen"), true)
                .withAltTexture(Shardline.id("queen_clone")), 0.5F, false);
    }

    @Nullable
    @Override
    public RenderType getRenderType(QueenCloneEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public Color getRenderColor(QueenCloneEntity animatable, float partialTick, int packedLight) {
        return Color.ofARGB(150, 255, 255, 255);
    }
}
