package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Schlankere Ruestungsmodelle fuer die Shardline-Sets. Vanilla blaeht Ruestung um 1,0 (Hose 0,5) Pixel vom Koerper auf;
 * hier sind es 0,55 bzw. 0,3, damit die Ruestung enger anliegt und nicht so dick wirkt. Die Texturen (64x32) und das
 * UV-Layout bleiben die der normalen Ruestungslayer. Pose und Sichtbarkeit der Teile uebernimmt NeoForge vom Original.
 */
public final class SlimArmorModels {
    public static final ModelLayerLocation OUTER = new ModelLayerLocation(Shardline.id("slim_armor_outer"), "main");
    public static final ModelLayerLocation INNER = new ModelLayerLocation(Shardline.id("slim_armor_inner"), "main");

    private static HumanoidModel<LivingEntity> outerModel;
    private static HumanoidModel<LivingEntity> innerModel;

    private SlimArmorModels() {
    }

    public static LayerDefinition outerDefinition() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.55F), 0.0F), 64, 32);
    }

    public static LayerDefinition innerDefinition() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.3F), 0.0F), 64, 32);
    }

    /** Hose: inneres Modell, alles andere: aeusseres. Die Modelle werden einmal gebacken und beim Resource-Reload verworfen. */
    public static HumanoidModel<?> get(EquipmentSlot slot) {
        if (slot == EquipmentSlot.LEGS) {
            if (innerModel == null) {
                innerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(INNER));
            }
            return innerModel;
        }
        if (outerModel == null) {
            outerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(OUTER));
        }
        return outerModel;
    }

    public static void clear() {
        outerModel = null;
        innerModel = null;
    }
}
