package eu.shardline.mod.item;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.story.lore.LoreBooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Kreativ-Tab "Shardline": erst Bloecke, dann Items, zuletzt Spawn-Eier.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Shardline.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHARDLINE = TABS.register("shardline", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.shardline"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(ModItems.SHARD_INGOT.get()))
            .displayItems((parameters, output) -> {
                ModBlocks.BLOCK_ITEMS.forEach(item -> output.accept(item.get()));
                ModItems.ITEMS.getEntries().forEach(holder -> {
                    Item item = holder.get();
                    if (item == ModItems.LORE_BOOK.get()) {
                        LoreBooks.ALL.forEach(book -> output.accept(LoreBooks.create(book)));
                    } else if (item == ModItems.LORE_PAGE.get()) {
                        LoreBooks.ALL.forEach(book -> output.accept(LoreBooks.page(book, 1)));
                    } else if (!(item instanceof BlockItem) && !(item instanceof SpawnEggItem)) {
                        output.accept(item);
                    }
                });
                ModItems.SPAWN_EGGS.forEach(egg -> output.accept(egg.get()));
            })
            .build());

    private ModCreativeTabs() {
    }
}
