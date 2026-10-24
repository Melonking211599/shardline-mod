package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.npc.KeshEntity;
import eu.shardline.mod.entity.npc.VeylEntity;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** Renderer der NPCs mit Sonderwuenschen: Veyl durchscheinend (Alpha steckt in der Textur), Kesh mit Heil-Textur. */
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

    /** Kesh: textures/entity/kesh.png, nach der Heilung kesh_healed.png (Glowmask folgt automatisch dem Namen). */
    public static class KeshRenderer extends ShardlineGeoRenderer<KeshEntity> {
        private static final ResourceLocation HEALED = Shardline.id("textures/entity/kesh_healed.png");

        public KeshRenderer(EntityRendererProvider.Context context) {
            super(context, new DefaultedEntityGeoModel<KeshEntity>(Shardline.id("kesh"), true) {
                @Override
                public ResourceLocation getTextureResource(KeshEntity animatable) {
                    return animatable.isHealed() ? HEALED : super.getTextureResource(animatable);
                }
            }, 0.5F, true);
        }
    }
}
