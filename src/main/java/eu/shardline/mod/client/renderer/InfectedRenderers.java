package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Renderer der infizierten Mobs: Vanilla-Modelle mit eigener Textur und einer leuchtenden
 * Kristallschicht (textures/entity/&lt;name&gt;_glowmask.png).
 */
public final class InfectedRenderers {
    private InfectedRenderers() {
    }

    public static class ShardZombieRenderer extends ZombieRenderer {
        private static final ResourceLocation TEXTURE = Shardline.id("textures/entity/shard_zombie.png");
        private static final RenderType GLOW = RenderType.eyes(Shardline.id("textures/entity/shard_zombie_glowmask.png"));

        public ShardZombieRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.addLayer(new EyesLayer<Zombie, ZombieModel<Zombie>>(this) {
                @Override
                public RenderType renderType() {
                    return GLOW;
                }
            });
        }

        @Override
        public ResourceLocation getTextureLocation(Zombie entity) {
            return TEXTURE;
        }
    }

    public static class CrystallizedCowRenderer extends CowRenderer {
        private static final ResourceLocation TEXTURE = Shardline.id("textures/entity/crystallized_cow.png");
        private static final RenderType GLOW = RenderType.eyes(Shardline.id("textures/entity/crystallized_cow_glowmask.png"));

        public CrystallizedCowRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.addLayer(new EyesLayer<Cow, CowModel<Cow>>(this) {
                @Override
                public RenderType renderType() {
                    return GLOW;
                }
            });
        }

        @Override
        public ResourceLocation getTextureLocation(Cow entity) {
            return TEXTURE;
        }
    }
}
