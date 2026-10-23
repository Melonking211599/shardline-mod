package eu.shardline.mod.item.custom;

import eu.shardline.mod.Shardline;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

/**
 * Koloss-Schmiedevorlage: wertet Netherit-Ausruestung mit einem Kolosskern zur Koloss-Ausruestung auf.
 */
public final class ModSmithingTemplates {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    private static final ResourceLocation EMPTY_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_PICKAXE = ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_AXE = ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SHOVEL = ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_CORE = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");

    private ModSmithingTemplates() {
    }

    public static SmithingTemplateItem createColossusUpgradeTemplate() {
        return new SmithingTemplateItem(
                Component.translatable(Util.makeDescriptionId("item", Shardline.id("smithing_template.colossus_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT),
                Component.translatable(Util.makeDescriptionId("item", Shardline.id("smithing_template.colossus_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT),
                Component.translatable(Util.makeDescriptionId("upgrade", Shardline.id("colossus_upgrade"))).withStyle(TITLE_FORMAT),
                Component.translatable(Util.makeDescriptionId("item", Shardline.id("smithing_template.colossus_upgrade.base_slot_description"))),
                Component.translatable(Util.makeDescriptionId("item", Shardline.id("smithing_template.colossus_upgrade.additions_slot_description"))),
                List.of(EMPTY_HELMET, EMPTY_SWORD, EMPTY_CHESTPLATE, EMPTY_PICKAXE, EMPTY_LEGGINGS, EMPTY_AXE, EMPTY_BOOTS, EMPTY_SHOVEL),
                List.of(EMPTY_CORE));
    }
}
