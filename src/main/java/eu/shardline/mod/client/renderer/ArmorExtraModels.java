package eu.shardline.mod.client.renderer;

import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

/**
 * Zusatzteile der Ruestungen. DATEI WIRD ERZEUGT von tools/assetgen/armor_extras.py, nicht von Hand aendern.
 * Gruppen: helmet, torso, right_shoulder, left_shoulder, right_knee, left_knee, right_boot, left_boot.
 * Texturen: textures/models/armor/<set>_extras.png (+ _glow.png), 64x64.
 */
public final class ArmorExtraModels {
    private ArmorExtraModels() {
    }

    public static LayerDefinition shard() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition helmet = root.addOrReplaceChild("helmet", CubeListBuilder.create(), PartPose.ZERO);
        helmet.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(8, 0).addBox(-1F, -7F, -1F, 2, 7, 2), PartPose.offsetAndRotation(0F, -8.5F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(38, 0).addBox(-1F, -5F, -1F, 2, 5, 2), PartPose.offsetAndRotation(0F, -8.05F, 2.5F, -0.6F, 0F, 0F));
        helmet.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(6, 11).addBox(-0.5F, -4F, -0.5F, 1, 4, 1), PartPose.offsetAndRotation(-2.6F, -8.05F, -1.5F, 0.3F, 0F, -0.35F));
        helmet.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(6, 11).addBox(-0.5F, -4F, -0.5F, 1, 4, 1), PartPose.offsetAndRotation(2.6F, -8.05F, -1.5F, 0.3F, 0F, 0.35F));
        helmet.addOrReplaceChild("c4", CubeListBuilder.create().texOffs(20, 11).addBox(-3.5F, -7.4F, -5.15F, 7, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c5", CubeListBuilder.create().texOffs(36, 11).addBox(-0.5F, -7.4F, -5.35F, 1, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c6", CubeListBuilder.create().texOffs(52, 0).addBox(-5.55F, -5.5F, -1.5F, 1, 3, 3), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c7", CubeListBuilder.create().texOffs(52, 0).addBox(4.55F, -5.5F, -1.5F, 1, 3, 3), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.ZERO);
        torso.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, -9F, -1F, 2, 9, 2), PartPose.offsetAndRotation(0F, 4F, 2.95F, -0.28F, 0F, 0F));
        torso.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(46, 0).addBox(-1F, -6F, -0.5F, 2, 6, 1), PartPose.offsetAndRotation(-2.55F, 5F, 2.95F, -0.2F, 0F, -0.35F));
        torso.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(46, 0).addBox(-1F, -6F, -0.5F, 2, 6, 1), PartPose.offsetAndRotation(2.55F, 5F, 2.95F, -0.2F, 0F, 0.35F));
        torso.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(10, 11).addBox(-1F, 2.5F, -3.4F, 2, 3, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition right_shoulder = root.addOrReplaceChild("right_shoulder", CubeListBuilder.create(), PartPose.ZERO);
        right_shoulder.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(16, 0).addBox(-4.6F, -3.45F, -3F, 5, 1, 6), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_shoulder.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(38, 0).addBox(-1F, -5F, -1F, 2, 5, 2), PartPose.offsetAndRotation(-3.8F, -3.35F, -1.5F, -0.15F, 0F, -0.45F));
        right_shoulder.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(16, 11).addBox(-0.5F, -3F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(-2.4F, -3.35F, 1.8F, -0.45F, 0F, -0.3F));
        PartDefinition left_shoulder = root.addOrReplaceChild("left_shoulder", CubeListBuilder.create(), PartPose.ZERO);
        left_shoulder.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(16, 0).addBox(-0.4F, -3.45F, -3F, 5, 1, 6), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_shoulder.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(38, 0).addBox(-1F, -5F, -1F, 2, 5, 2), PartPose.offsetAndRotation(3.8F, -3.35F, -1.5F, -0.15F, 0F, 0.45F));
        left_shoulder.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(16, 11).addBox(-0.5F, -3F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(2.4F, -3.35F, 1.8F, -0.45F, 0F, 0.3F));
        PartDefinition right_knee = root.addOrReplaceChild("right_knee", CubeListBuilder.create(), PartPose.ZERO);
        right_knee.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(0, 11).addBox(-1F, -4F, -0.5F, 2, 4, 1), PartPose.offsetAndRotation(-1.9F, 7F, -2.8F, 0.9F, 0F, 0F));
        PartDefinition left_knee = root.addOrReplaceChild("left_knee", CubeListBuilder.create(), PartPose.ZERO);
        left_knee.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(0, 11).addBox(-1F, -4F, -0.5F, 2, 4, 1), PartPose.offsetAndRotation(1.9F, 7F, -2.8F, 0.9F, 0F, 0F));
        PartDefinition right_boot = root.addOrReplaceChild("right_boot", CubeListBuilder.create(), PartPose.ZERO);
        right_boot.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(52, 0).addBox(-3.4F, 7.55F, -1.5F, 1, 3, 3), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_boot.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(16, 11).addBox(-0.5F, -3F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(-3.15F, 9.55F, 1.2F, -0.9F, 0F, -0.3F));
        PartDefinition left_boot = root.addOrReplaceChild("left_boot", CubeListBuilder.create(), PartPose.ZERO);
        left_boot.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(52, 0).addBox(2.4F, 7.55F, -1.5F, 1, 3, 3), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_boot.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(16, 11).addBox(-0.5F, -3F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(3.15F, 9.55F, 1.2F, -0.9F, 0F, 0.3F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition colossus() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition helmet = root.addOrReplaceChild("helmet", CubeListBuilder.create(), PartPose.ZERO);
        helmet.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(22, 18).addBox(-4.5F, -7.4F, -5.15F, 9, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(52, 18).addBox(-1F, -7.1F, -5.65F, 2, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(28, 0).addBox(-0.5F, -11.05F, -3.5F, 1, 3, 7), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        helmet.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(26, 10).addBox(-1F, -4F, -1F, 2, 4, 2), PartPose.offsetAndRotation(0F, -11.05F, -3.55F, 0.35F, 0F, 0F));
        helmet.addOrReplaceChild("c4", CubeListBuilder.create().texOffs(34, 10).addBox(-1F, -4F, -1F, 2, 4, 2), PartPose.offsetAndRotation(-5.15F, -6F, 0F, 0F, 0F, -0.9F));
        helmet.addOrReplaceChild("c5", CubeListBuilder.create().texOffs(0, 10).addBox(-1F, -6F, -1F, 2, 6, 2), PartPose.offsetAndRotation(-7.657F, -7.539F, 0F, 0F, 0F, -0.15F));
        helmet.addOrReplaceChild("c6", CubeListBuilder.create().texOffs(34, 10).addBox(-1F, -4F, -1F, 2, 4, 2), PartPose.offsetAndRotation(5.15F, -6F, 0F, 0F, 0F, 0.9F));
        helmet.addOrReplaceChild("c7", CubeListBuilder.create().texOffs(0, 10).addBox(-1F, -6F, -1F, 2, 6, 2), PartPose.offsetAndRotation(7.657F, -7.539F, 0F, 0F, 0F, 0.15F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.ZERO);
        torso.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(44, 0).addBox(-2F, 1.5F, 2.45F, 4, 7, 2), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        torso.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(56, 0).addBox(-1F, -7F, -1F, 2, 7, 2), PartPose.offsetAndRotation(-3.15F, 4.5F, 2.75F, -0.2F, 0F, -0.5F));
        torso.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(56, 0).addBox(-1F, -7F, -1F, 2, 7, 2), PartPose.offsetAndRotation(3.15F, 4.5F, 2.75F, -0.2F, 0F, 0.5F));
        torso.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(22, 18).addBox(-4.5F, 0.05F, -3.3F, 9, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        torso.addOrReplaceChild("c4", CubeListBuilder.create().texOffs(0, 18).addBox(-1.5F, 3F, -3.4F, 3, 3, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition right_shoulder = root.addOrReplaceChild("right_shoulder", CubeListBuilder.create(), PartPose.ZERO);
        right_shoulder.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(0, 0).addBox(-5.4F, -3.85F, -4F, 6, 2, 8), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_shoulder.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(8, 10).addBox(-4.4F, -4.85F, -2.5F, 4, 1, 5), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_shoulder.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(56, 10).addBox(-1F, -3F, -1F, 2, 3, 2), PartPose.offsetAndRotation(-4.35F, -3.85F, -2.55F, 0.3F, 0F, -0.55F));
        right_shoulder.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(56, 10).addBox(-1F, -3F, -1F, 2, 3, 2), PartPose.offsetAndRotation(-4.35F, -3.85F, 2.55F, -0.3F, 0F, -0.55F));
        PartDefinition left_shoulder = root.addOrReplaceChild("left_shoulder", CubeListBuilder.create(), PartPose.ZERO);
        left_shoulder.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, -3.85F, -4F, 6, 2, 8), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_shoulder.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(8, 10).addBox(0.4F, -4.85F, -2.5F, 4, 1, 5), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_shoulder.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(56, 10).addBox(-1F, -3F, -1F, 2, 3, 2), PartPose.offsetAndRotation(4.35F, -3.85F, -2.55F, 0.3F, 0F, 0.55F));
        left_shoulder.addOrReplaceChild("c3", CubeListBuilder.create().texOffs(56, 10).addBox(-1F, -3F, -1F, 2, 3, 2), PartPose.offsetAndRotation(4.35F, -3.85F, 2.55F, -0.3F, 0F, 0.55F));
        PartDefinition right_knee = root.addOrReplaceChild("right_knee", CubeListBuilder.create(), PartPose.ZERO);
        right_knee.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(8, 18).addBox(-2.5F, 5F, -3F, 3, 3, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_knee.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(16, 18).addBox(-1F, -3F, -0.5F, 2, 3, 1), PartPose.offsetAndRotation(-1F, 5.2F, -2.8F, 0.9F, 0F, 0F));
        PartDefinition left_knee = root.addOrReplaceChild("left_knee", CubeListBuilder.create(), PartPose.ZERO);
        left_knee.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(8, 18).addBox(-0.5F, 5F, -3F, 3, 3, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_knee.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(16, 18).addBox(-1F, -3F, -0.5F, 2, 3, 1), PartPose.offsetAndRotation(1F, 5.2F, -2.8F, 0.9F, 0F, 0F));
        PartDefinition right_boot = root.addOrReplaceChild("right_boot", CubeListBuilder.create(), PartPose.ZERO);
        right_boot.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(42, 10).addBox(-3.2F, 7.55F, -1.5F, 1, 3, 3), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_boot.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(42, 18).addBox(-2F, 9.55F, -3.35F, 4, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        right_boot.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(50, 10).addBox(-0.5F, -4F, -1F, 1, 4, 2), PartPose.offsetAndRotation(-3.35F, 9.55F, 1.4F, -0.8F, 0F, -0.25F));
        PartDefinition left_boot = root.addOrReplaceChild("left_boot", CubeListBuilder.create(), PartPose.ZERO);
        left_boot.addOrReplaceChild("c0", CubeListBuilder.create().texOffs(42, 10).addBox(2.2F, 7.55F, -1.5F, 1, 3, 3), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_boot.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(42, 18).addBox(-2F, 9.55F, -3.35F, 4, 1, 1), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        left_boot.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(50, 10).addBox(-0.5F, -4F, -1F, 1, 4, 2), PartPose.offsetAndRotation(3.35F, 9.55F, 1.4F, -0.8F, 0F, 0.25F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
