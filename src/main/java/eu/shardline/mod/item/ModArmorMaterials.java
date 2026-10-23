package eu.shardline.mod.item;

import eu.shardline.mod.Shardline;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ruestungsmaterialien. Werte zum Vergleich (Helm/Brust/Hose/Stiefel, Haerte):
 * Eisen 2/6/5/2 (0) - Shard 3/7/5/2 (1.0) - Diamant 3/8/6/3 (2.0) - Netherit 3/8/6/3 (3.0) - Koloss 4/9/7/4 (4.0).
 * Texturen: assets/shardline/textures/models/armor/{shard,colossus}_layer_{1,2}.png
 */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Shardline.MODID);

    /** Haltbarkeitsfaktor (Eisen 15, Diamant 33, Netherit 37). */
    public static final int SHARD_DURABILITY = 24;
    public static final int COLOSSUS_DURABILITY = 45;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SHARD = ARMOR_MATERIALS.register("shard", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 7);
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.BODY, 7);
            }),
            12,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(ModItems.SHARD_INGOT.get()),
            List.of(new ArmorMaterial.Layer(Shardline.id("shard"))),
            1.0F,
            0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COLOSSUS = ARMOR_MATERIALS.register("colossus", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 4);
                map.put(ArmorItem.Type.LEGGINGS, 7);
                map.put(ArmorItem.Type.CHESTPLATE, 9);
                map.put(ArmorItem.Type.HELMET, 4);
                map.put(ArmorItem.Type.BODY, 12);
            }),
            18,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(ModItems.COLOSSUS_CORE.get()),
            List.of(new ArmorMaterial.Layer(Shardline.id("colossus"))),
            4.0F,
            0.15F));

    private ModArmorMaterials() {
    }
}
