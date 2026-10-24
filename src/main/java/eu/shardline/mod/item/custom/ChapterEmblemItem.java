package eu.shardline.mod.item.custom;

import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.StoryChapter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Kapitel-Emblem: nur ein Symbol (Advancement-Icons des Chronik-Tabs, Kapitelkarte). Welches Kapitel, steht in der
 * Komponente {@code shardline:chapter}; das Item-Modell waehlt die Textur ueber die Eigenschaft {@code shardline:chapter}.
 * Nicht im Kreativ-Tab und nicht herstellbar.
 */
public class ChapterEmblemItem extends Item {
    public ChapterEmblemItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(StoryChapter chapter) {
        ItemStack stack = new ItemStack(ModItems.CHAPTER_EMBLEM.get());
        stack.set(ModDataComponents.CHAPTER.get(), chapter.number());
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        StoryChapter chapter = StoryChapter.byNumber(stack.getOrDefault(ModDataComponents.CHAPTER.get(), 0));
        return chapter != null ? Component.translatable("item.shardline.chapter_emblem.named", chapter.label(), chapter.title())
                : super.getName(stack);
    }
}
