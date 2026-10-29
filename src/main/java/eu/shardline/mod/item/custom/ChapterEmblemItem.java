package eu.shardline.mod.item.custom;

import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
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
        return of(chapter, StoryEnding.NONE);
    }

    /** Emblem im Zweig (Kapitel 8 und 9 haben je eines pro Ende, siehe {@link StoryChapter#emblemValue}). */
    public static ItemStack of(StoryChapter chapter, StoryEnding branch) {
        ItemStack stack = new ItemStack(ModItems.CHAPTER_EMBLEM.get());
        stack.set(ModDataComponents.CHAPTER.get(), chapter.emblemValue(branch));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        int value = stack.getOrDefault(ModDataComponents.CHAPTER.get(), 0);
        StoryChapter chapter = StoryChapter.byEmblemValue(value);
        return chapter != null ? Component.translatable("item.shardline.chapter_emblem.named", chapter.label(),
                chapter.title(StoryChapter.branchOfEmblemValue(value))) : super.getName(stack);
    }
}
