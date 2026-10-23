package eu.shardline.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.item.custom.ShardlineArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

/**
 * Zusatzteile der Shardline-Ruestungen: Kristallkamm am Helm, Rueckenkristalle an der Brustplatte, Schulterpanzer,
 * Knieschuetzer an der Hose und Flossen an den Stiefeln. Die Teile haengen an den Koerperteilen des Traegers und
 * bewegen sich mit (Schleichen, Laufen, Armschwung). Modelle: {@link ArmorExtraModels}, Texturen:
 * textures/models/armor/&lt;set&gt;_extras.png mit leuchtender Schicht &lt;set&gt;_extras_glow.png.
 */
public class ArmorExtrasLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public static final ModelLayerLocation SHARD_LAYER = new ModelLayerLocation(Shardline.id("shard_armor_extras"), "main");
    public static final ModelLayerLocation COLOSSUS_LAYER = new ModelLayerLocation(Shardline.id("colossus_armor_extras"), "main");

    private record Extras(ModelPart root, RenderType solid, RenderType glow) {
        private static Extras of(ModelPart root, String set) {
            ResourceLocation tex = Shardline.id("textures/models/armor/" + set + "_extras.png");
            ResourceLocation glow = Shardline.id("textures/models/armor/" + set + "_extras_glow.png");
            return new Extras(root, RenderType.entityCutoutNoCull(tex), RenderType.eyes(glow));
        }
    }

    private final Extras shard;
    private final Extras colossus;

    public ArmorExtrasLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        super(parent);
        this.shard = Extras.of(models.bakeLayer(SHARD_LAYER), "shard");
        this.colossus = Extras.of(models.bakeLayer(COLOSSUS_LAYER), "colossus");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        M model = this.getParentModel();
        Extras helmet = piece(entity, EquipmentSlot.HEAD, ArmorItem.Type.HELMET);
        if (helmet != null) {
            draw(helmet, "helmet", model.head, poseStack, buffer, packedLight);
        }
        Extras chest = piece(entity, EquipmentSlot.CHEST, ArmorItem.Type.CHESTPLATE);
        if (chest != null) {
            draw(chest, "torso", model.body, poseStack, buffer, packedLight);
            draw(chest, "right_shoulder", model.rightArm, poseStack, buffer, packedLight);
            draw(chest, "left_shoulder", model.leftArm, poseStack, buffer, packedLight);
        }
        Extras legs = piece(entity, EquipmentSlot.LEGS, ArmorItem.Type.LEGGINGS);
        if (legs != null) {
            draw(legs, "right_knee", model.rightLeg, poseStack, buffer, packedLight);
            draw(legs, "left_knee", model.leftLeg, poseStack, buffer, packedLight);
        }
        Extras boots = piece(entity, EquipmentSlot.FEET, ArmorItem.Type.BOOTS);
        if (boots != null) {
            draw(boots, "right_boot", model.rightLeg, poseStack, buffer, packedLight);
            draw(boots, "left_boot", model.leftLeg, poseStack, buffer, packedLight);
        }
    }

    /** Zusatzteile fuer das getragene Ruestungsteil oder null, wenn dort kein Shardline-Teil dieser Art sitzt. */
    private Extras piece(T entity, EquipmentSlot slot, ArmorItem.Type type) {
        if (entity.getItemBySlot(slot).getItem() instanceof ShardlineArmorItem armor && armor.getType() == type) {
            return armor.getSetType() == ShardlineArmorItem.SetType.SHARD ? this.shard : this.colossus;
        }
        return null;
    }

    private static void draw(Extras extras, String group, ModelPart anchor, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ModelPart part = extras.root().getChild(group);
        poseStack.pushPose();
        anchor.translateAndRotate(poseStack);
        part.render(poseStack, buffer.getBuffer(extras.solid()), packedLight, OverlayTexture.NO_OVERLAY);
        part.render(poseStack, buffer.getBuffer(extras.glow()), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
