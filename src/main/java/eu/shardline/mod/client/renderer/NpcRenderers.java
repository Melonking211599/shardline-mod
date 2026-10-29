package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.boss.EchoQueenEntity;
import eu.shardline.mod.entity.boss.FesteringHeartEntity;
import eu.shardline.mod.entity.npc.KeshEntity;
import eu.shardline.mod.entity.npc.VeylEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Renderer fuer "Nach dem Ausbruch": Veyl durchscheinend (Alpha steckt in der Textur), Kesh mit Heil-Textur, Nachhall
 * (Koenigin-Modell, durchscheinende Geisttextur) und Schwaerenherz (Virusherz-Modell, eigene Textur).
 */
public final class NpcRenderers {
    private NpcRenderers() {
    }

    /** Geist: durchscheinend, ohne Schatten; Augen und Runen leuchten ueber die Glowmask. */
    public static class VeylRenderer extends ShardlineGeoRenderer<VeylEntity> {
        public VeylRenderer(EntityRendererProvider.Context context) {
            super(context, "veyl", 0.0F, true);
        }

        @Nullable
        @Override
        public RenderType getRenderType(VeylEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
            return RenderType.entityTranslucent(texture);
        }
    }

    /** Nachhall: Modell und Animationen der Koenigin, Textur echo_queen (+ Glowmask), durchscheinend. */
    public static class EchoQueenRenderer extends ShardlineGeoRenderer<EchoQueenEntity> {
        public EchoQueenRenderer(EntityRendererProvider.Context context) {
            super(context, new DefaultedEntityGeoModel<EchoQueenEntity>(Shardline.id("shard_queen"), true).withAltTexture(Shardline.id("echo_queen")),
                    0.0F, true);
        }

        @Nullable
        @Override
        public RenderType getRenderType(EchoQueenEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
            return RenderType.entityTranslucent(texture);
        }

        @Override
        protected float getDeathMaxRotation(EchoQueenEntity animatable) {
            return 0.0F;
        }
    }

    /** Schwaerenherz: Modell und Animationen des Virusherzens, Textur festering_heart; kippt beim Tod nicht um. */
    public static class FesteringHeartRenderer extends ShardlineGeoRenderer<FesteringHeartEntity> {
        public FesteringHeartRenderer(EntityRendererProvider.Context context) {
            super(context, new DefaultedEntityGeoModel<FesteringHeartEntity>(Shardline.id("virus_heart"), true)
                    .withAltTexture(Shardline.id("festering_heart")), 1.2F, true);
        }

        @Override
        protected float getDeathMaxRotation(FesteringHeartEntity animatable) {
            return 0.0F;
        }
    }

    /**
     * Kesh: textures/entity/kesh.png mit leuchtenden Kristallen (kesh_glowmask.png), nach der Heilung kesh_healed.png
     * ohne Leuchtschicht - die Kristalle sind erloschen, und GeckoLib stuerzt bei einer leeren Glowmask ab.
     */
    public static class KeshRenderer extends ShardlineGeoRenderer<KeshEntity> {
        private static final ResourceLocation HEALED = Shardline.id("textures/entity/kesh_healed.png");

        public KeshRenderer(EntityRendererProvider.Context context) {
            super(context, new DefaultedEntityGeoModel<KeshEntity>(Shardline.id("kesh"), true) {
                @Override
                public ResourceLocation getTextureResource(KeshEntity animatable) {
                    return animatable.isHealed() ? HEALED : super.getTextureResource(animatable);
                }
            }, 0.5F, false);
            this.addRenderLayer(new AutoGlowingGeoLayer<>(this) {
                @Override
                public void render(PoseStack poseStack, KeshEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                                   MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight,
                                   int packedOverlay) {
                    if (!animatable.isHealed()) {
                        super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                    }
                }
            });
        }
    }
}
