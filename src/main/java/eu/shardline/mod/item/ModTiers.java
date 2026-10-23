package eu.shardline.mod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * Werkzeugstufen. Vergleich (Haltbarkeit / Abbaugeschwindigkeit / Schadensbonus / Verzauberbarkeit):
 * Eisen 250/6/2/14 - Shard 900/7/2.5/16 - Diamant 1561/8/3/10 - Netherit 2031/9/4/15 - Koloss 2800/10/5/18.
 */
public final class ModTiers {
    /** Baut alles ab, was Eisen abbaut. */
    public static final Tier SHARD = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,
            900, 7.0F, 2.5F, 16, () -> Ingredient.of(ModItems.SHARD_INGOT.get()));

    /** Baut alles ab, was Netherit abbaut. */
    public static final Tier COLOSSUS = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2800, 10.0F, 5.0F, 18, () -> Ingredient.of(ModItems.COLOSSUS_CORE.get()));

    /** Stufe der Splitterklinge der Koenigin. */
    public static final Tier QUEEN = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            3600, 10.0F, 6.0F, 22, () -> Ingredient.of(ModItems.HEART_OF_THE_EXPANSE.get()));

    private ModTiers() {
    }
}
