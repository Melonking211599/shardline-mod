package eu.shardline.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.infected.CrystallizedVillagerEntity;
import eu.shardline.mod.entity.infected.InfectedMob;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Renderer der infizierten Mobs: Vanilla-Modelle mit eigener Textur und einer leuchtenden
 * Kristallschicht (textures/entity/&lt;name&gt;_glowmask.png). Gezaehmte Wesen tragen zusaetzlich einen
 * Kragen (textures/entity/&lt;name&gt;_collar.png im Layout des jeweiligen Modells).
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
            this.addLayer(new CollarLayer<>(this, Shardline.id("textures/entity/shard_zombie_collar.png")));
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
            this.addLayer(new CollarLayer<>(this, Shardline.id("textures/entity/crystallized_cow_collar.png")));
        }

        @Override
        public ResourceLocation getTextureLocation(Cow entity) {
            return TEXTURE;
        }
    }

    public static class CrystallizedPigRenderer extends PigRenderer {
        private static final ResourceLocation TEXTURE = Shardline.id("textures/entity/crystallized_pig.png");
        private static final RenderType GLOW = RenderType.eyes(Shardline.id("textures/entity/crystallized_pig_glowmask.png"));

        public CrystallizedPigRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.addLayer(new EyesLayer<Pig, PigModel<Pig>>(this) {
                @Override
                public RenderType renderType() {
                    return GLOW;
                }
            });
            this.addLayer(new CollarLayer<>(this, Shardline.id("textures/entity/crystallized_pig_collar.png")));
        }

        @Override
        public ResourceLocation getTextureLocation(Pig entity) {
            return TEXTURE;
        }
    }

    /** Schaf mit Kristallvlies statt eingefaerbter Wolle; der Kragen sitzt auf dem Vlies-Modell. */
    public static class CrystallizedSheepRenderer extends MobRenderer<Sheep, SheepModel<Sheep>> {
        private static final ResourceLocation TEXTURE = Shardline.id("textures/entity/crystallized_sheep.png");
        private static final RenderType GLOW = RenderType.eyes(Shardline.id("textures/entity/crystallized_sheep_glowmask.png"));

        public CrystallizedSheepRenderer(EntityRendererProvider.Context context) {
            super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
            this.addLayer(new EyesLayer<Sheep, SheepModel<Sheep>>(this) {
                @Override
                public RenderType renderType() {
                    return GLOW;
                }
            });
            this.addLayer(new FleeceLayer(this, context.getModelSet()));
        }

        @Override
        public ResourceLocation getTextureLocation(Sheep entity) {
            return TEXTURE;
        }
    }

    private static class FleeceLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
        private static final ResourceLocation FLEECE = Shardline.id("textures/entity/crystallized_sheep_fur.png");
        private static final RenderType FLEECE_GLOW = RenderType.eyes(Shardline.id("textures/entity/crystallized_sheep_fur_glowmask.png"));
        private static final ResourceLocation COLLAR = Shardline.id("textures/entity/crystallized_sheep_collar.png");
        private final SheepFurModel<Sheep> model;

        FleeceLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> parent, EntityModelSet models) {
            super(parent);
            this.model = new SheepFurModel<>(models.bakeLayer(ModelLayers.SHEEP_FUR));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Sheep sheep, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (sheep.isInvisible()) {
                return;
            }
            boolean collar = sheep instanceof InfectedMob infected && infected.hasCollar();
            if (sheep.isSheared() && !collar) {
                return;
            }
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            if (!sheep.isSheared()) {
                renderColoredCutoutModel(this.model, FLEECE, poseStack, buffer, packedLight, sheep, -1);
                this.model.renderToBuffer(poseStack, buffer.getBuffer(FLEECE_GLOW), 15728640, OverlayTexture.NO_OVERLAY);
            }
            if (collar) {
                renderColoredCutoutModel(this.model, COLLAR, poseStack, buffer, packedLight, sheep, -1);
            }
        }
    }

    public static class CrystallizedVillagerRenderer extends MobRenderer<CrystallizedVillagerEntity, VillagerModel<CrystallizedVillagerEntity>> {
        private static final ResourceLocation TEXTURE = Shardline.id("textures/entity/crystallized_villager.png");
        private static final RenderType GLOW = RenderType.eyes(Shardline.id("textures/entity/crystallized_villager_glowmask.png"));

        public CrystallizedVillagerRenderer(EntityRendererProvider.Context context) {
            super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
            this.addLayer(new EyesLayer<CrystallizedVillagerEntity, VillagerModel<CrystallizedVillagerEntity>>(this) {
                @Override
                public RenderType renderType() {
                    return GLOW;
                }
            });
            this.addLayer(new CollarLayer<>(this, Shardline.id("textures/entity/crystallized_villager_collar.png")));
        }

        @Override
        public ResourceLocation getTextureLocation(CrystallizedVillagerEntity entity) {
            return TEXTURE;
        }

        @Override
        protected void scale(CrystallizedVillagerEntity entity, PoseStack poseStack, float partialTick) {
            float scale = 0.9375F;
            if (entity.isBaby()) {
                scale *= 0.5F;
                this.shadowRadius = 0.25F;
            } else {
                this.shadowRadius = 0.5F;
            }
            poseStack.scale(scale, scale, scale);
        }
    }

    /** Kragen gezaehmter infizierter Wesen; die Textur liegt im Layout des Eltern-Modells. */
    public static class CollarLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        private final ResourceLocation texture;

        public CollarLayer(RenderLayerParent<T, M> parent, ResourceLocation texture) {
            super(parent);
            this.texture = texture;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity instanceof InfectedMob infected && infected.hasCollar() && !entity.isInvisible()) {
                renderColoredCutoutModel(this.getParentModel(), this.texture, poseStack, buffer, packedLight, entity, -1);
            }
        }
    }
}
