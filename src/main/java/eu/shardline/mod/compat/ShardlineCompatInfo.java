package eu.shardline.mod.compat;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.item.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;

/**
 * Infoseiten fuer JEI und EMI: erklaeren Mechaniken, die kein normales Rezept haben
 * (Portalbau, Boss-Beschwoerung, Zaehmen ...). Texte stehen in den Sprachdateien unter "jei.shardline.*".
 */
public final class ShardlineCompatInfo {
    public record Entry(String name, Supplier<? extends ItemLike> item, String translationKey) {
    }

    public static final List<Entry> ENTRIES = List.of(
            new Entry("shard_portal_frame", ModBlocks.SHARD_PORTAL_FRAME, "jei.shardline.portal"),
            new Entry("shard_key", ModItems.SHARD_KEY, "jei.shardline.portal"),
            new Entry("raw_shard", ModItems.RAW_SHARD, "jei.shardline.shard_ore"),
            new Entry("knight_crest", ModItems.KNIGHT_CREST, "jei.shardline.knight_crest"),
            new Entry("colossus_summoning_crystal", ModItems.COLOSSUS_SUMMONING_CRYSTAL, "jei.shardline.colossus_summon"),
            new Entry("colossus_altar", ModBlocks.COLOSSUS_ALTAR, "jei.shardline.colossus_summon"),
            new Entry("colossus_core", ModItems.COLOSSUS_CORE, "jei.shardline.queen_summon"),
            new Entry("queen_altar", ModBlocks.QUEEN_ALTAR, "jei.shardline.queen_summon"),
            new Entry("colossus_upgrade_smithing_template", ModItems.COLOSSUS_UPGRADE_SMITHING_TEMPLATE, "jei.shardline.colossus_template"),
            new Entry("glowbloom", ModBlocks.GLOWBLOOM, "jei.shardline.glow_moth"),
            new Entry("luminous_berry", ModItems.LUMINOUS_BERRY, "jei.shardline.void_ray"),
            new Entry("heart_of_the_expanse", ModItems.HEART_OF_THE_EXPANSE, "jei.shardline.heart"),
            new Entry("colossus_helmet", ModItems.COLOSSUS_HELMET, "jei.shardline.crystal_shield"));

    private ShardlineCompatInfo() {
    }
}
