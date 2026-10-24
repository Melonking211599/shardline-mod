package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.StoryChapter;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Zweiter Advancement-Tab "Shardline: Chronik" fuer die Geschichte. Alle Kriterien sind
 * {@code minecraft:impossible} und werden vom Server ueber {@code StoryManager.award} vergeben.
 */
public class ModStoryAdvancements implements AdvancementProvider.AdvancementGenerator {
    private static final ResourceLocation BACKGROUND = Shardline.id("textures/gui/advancements/backgrounds/story.png");

    private Consumer<AdvancementHolder> saver;
    private ExistingFileHelper fileHelper;

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper fileHelper) {
        this.saver = saver;
        this.fileHelper = fileHelper;

        AdvancementHolder chapter1 = Advancement.Builder.advancement()
                .display(ModItems.SHARD_FRAGMENT.get(), title("chapter_1"), description("chapter_1"), BACKGROUND,
                        AdvancementType.TASK, true, true, false)
                .addCriterion("story", impossible())
                .save(saver, Shardline.id(StoryChapter.FIRST_SHARD.advancementId()), fileHelper);
        AdvancementHolder chapter2 = chapter(chapter1, StoryChapter.THE_GATE, ModBlocks.SHARD_PORTAL_FRAME.get());
        AdvancementHolder chapter3 = chapter(chapter2, StoryChapter.ECHOES_OF_THE_KEEPERS, ModBlocks.MEMORY_CRYSTAL.get());

        AdvancementHolder book = task(chapter3, "lore_book", ModItems.LORE_BOOK.get(), AdvancementType.TASK, 0);
        task(book, "all_lore_books", ModItems.LORE_BOOK.get(), AdvancementType.CHALLENGE, 250);
        AdvancementHolder vision = task(chapter3, "vision", ModBlocks.MEMORY_CRYSTAL.get(), AdvancementType.TASK, 0);
        task(vision, "all_visions", ModBlocks.MEMORY_CRYSTAL.get(), AdvancementType.CHALLENGE, 250);

        AdvancementHolder chapter4 = chapter(chapter3, StoryChapter.HEART_OF_THE_EXPANSE, ModItems.COLOSSUS_CORE.get());
        task(chapter4, "trader_warning", ModItems.SHARD_TRADER_SPAWN_EGG.get(), AdvancementType.TASK, 0);

        AdvancementHolder chapter5 = chapter(chapter4, StoryChapter.THE_BREACH, ModBlocks.INFECTED_STONE.get());
        AdvancementHolder chapter6 = chapter(chapter5, StoryChapter.CONTAINMENT, ModBlocks.PURITY_ANCHOR.get());
        task(chapter6, "purity_anchor", ModBlocks.PURITY_ANCHOR.get(), AdvancementType.TASK, 0);
        AdvancementHolder purification = task(chapter6, "purification", ModItems.PURIFICATION_ESSENCE.get(), AdvancementType.TASK, 0);
        task(purification, "cure_villager", ModItems.CRYSTALLIZED_VILLAGER_SPAWN_EGG.get(), AdvancementType.GOAL, 100);
        task(purification, "immunity", ModItems.PURIFICATION_ESSENCE.get(), AdvancementType.TASK, 0);
        AdvancementHolder heart = task(chapter6, "virus_heart", ModItems.VIRUS_HEART_SPAWN_EGG.get(), AdvancementType.GOAL, 100);
        AdvancementHolder core = task(heart, "breach_core", ModItems.BREACH_CORE.get(), AdvancementType.GOAL, 300);
        task(core, "decision_altar", ModBlocks.DECISION_ALTAR.get(), AdvancementType.TASK, 0);
        task(core, "ending_seal", ModBlocks.QUEEN_TROPHY.get(), AdvancementType.CHALLENGE, 1000);
        AdvancementHolder dominate = task(core, "ending_dominate", ModItems.SHARD_ZOMBIE_SPAWN_EGG.get(), AdvancementType.CHALLENGE, 1000);
        AdvancementHolder tame = task(dominate, "tame_infected", ModItems.CRYSTALLIZED_COW_SPAWN_EGG.get(), AdvancementType.TASK, 0);
        task(tame, "infected_herd", ModItems.CRYSTALLIZED_SHEEP_SPAWN_EGG.get(), AdvancementType.CHALLENGE, 250);
    }

    private AdvancementHolder chapter(AdvancementHolder parent, StoryChapter chapter, ItemLike icon) {
        return this.advancement(parent, "chapter_" + chapter.number(), icon, AdvancementType.GOAL, 0);
    }

    private AdvancementHolder task(AdvancementHolder parent, String id, ItemLike icon, AdvancementType type, int experience) {
        return this.advancement(parent, id, icon, type, experience);
    }

    private AdvancementHolder advancement(@Nullable AdvancementHolder parent, String id, ItemLike icon, AdvancementType type, int experience) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        if (parent != null) {
            builder.parent(parent);
        }
        builder.display(icon, title(id), description(id), null, type, true, true, false)
                .addCriterion("story", impossible());
        if (experience > 0) {
            builder.rewards(AdvancementRewards.Builder.experience(experience));
        }
        return builder.save(this.saver, Shardline.id("story/" + id), this.fileHelper);
    }

    private static Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }

    private static Component title(String id) {
        return Component.translatable("advancements.shardline.story." + id + ".title");
    }

    private static Component description(String id) {
        return Component.translatable("advancements.shardline.story." + id + ".description");
    }
}
