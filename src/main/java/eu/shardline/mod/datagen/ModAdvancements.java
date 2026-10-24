package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.world.dimension.ModDimensions;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Eigener Advancement-Tab "Shardline" mit dem kompletten Fortschrittspfad:
 * Oberwelt -&gt; Portal -&gt; Shard-Set -&gt; Koloss -&gt; Koloss-Set -&gt; Koenigin.
 */
public class ModAdvancements implements AdvancementProvider.AdvancementGenerator {
    private static final ResourceLocation BACKGROUND = Shardline.id("textures/gui/advancements/backgrounds/shardline.png");

    private Consumer<AdvancementHolder> saver;
    private ExistingFileHelper fileHelper;

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper fileHelper) {
        this.saver = saver;
        this.fileHelper = fileHelper;

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.SHARD_FRAGMENT.get(), title("root"), description("root"), BACKGROUND,
                        AdvancementType.TASK, false, false, false)
                .addCriterion("shard_fragment", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SHARD_FRAGMENT.get()))
                .addCriterion("raw_shard", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RAW_SHARD.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, Shardline.id("root"), fileHelper);

        AdvancementHolder ingot = task(root, "shard_ingot", ModItems.SHARD_INGOT.get(), AdvancementType.TASK,
                "shard_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SHARD_INGOT.get()));
        AdvancementHolder key = task(ingot, "portal_key", ModItems.SHARD_KEY.get(), AdvancementType.TASK,
                "shard_key", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SHARD_KEY.get()));
        AdvancementHolder enter = task(key, "enter_splitterweite", ModBlocks.SHARD_PORTAL_FRAME.get(), AdvancementType.GOAL,
                "entered", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(ModDimensions.SPLITTERWEITE_LEVEL));
        AdvancementHolder shardArmor = task(ingot, "shard_armor", ModItems.SHARD_CHESTPLATE.get(), AdvancementType.TASK,
                "shard_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SHARD_HELMET.get(), ModItems.SHARD_CHESTPLATE.get(),
                        ModItems.SHARD_LEGGINGS.get(), ModItems.SHARD_BOOTS.get()));

        task(enter, "tame_glow_moth", ModBlocks.GLOWBLOOM.get(), AdvancementType.TASK,
                "tamed", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(ModEntities.GLOW_MOTH.get())));
        task(enter, "ride_void_ray", ModItems.VOID_RAY_HIDE.get(), AdvancementType.TASK,
                "riding", StartRidingTrigger.TriggerInstance.playerStartsRiding(EntityPredicate.Builder.entity()
                        .vehicle(EntityPredicate.Builder.entity().of(ModEntities.VOID_RAY.get()))));
        task(enter, "trade_shard_trader", ModItems.SHARD_FRAGMENT.get(), AdvancementType.TASK,
                "traded", TradeTrigger.TriggerInstance.tradedWithVillager(EntityPredicate.Builder.entity().of(ModEntities.SHARD_TRADER.get())));
        task(enter, "crystal_bow", ModItems.CRYSTAL_BOW.get(), AdvancementType.TASK,
                "crystal_bow", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CRYSTAL_BOW.get()));

        // Splitterhandwerk
        AdvancementHolder slime = task(enter, "crystal_slime", ModItems.CRYSTAL_SLIME.get(), AdvancementType.TASK,
                "crystal_slime", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CRYSTAL_SLIME.get()));
        task(slime, "resonance_compass", ModItems.RESONANCE_COMPASS.get(), AdvancementType.TASK,
                "resonance_compass", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.RESONANCE_COMPASS.get()));
        task(slime, "crystal_craftsman", ModBlocks.SHARD_LANTERN.get(), AdvancementType.GOAL,
                "crystal_craftsman", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.CRYSTAL_DOOR.get(),
                        ModBlocks.CRYSTAL_GLASS.get(), ModBlocks.SHARD_LANTERN.get(), ModBlocks.CHISELED_SHARDSTONE_BRICKS.get()));
        task(enter, "glowcap_stew", ModItems.GLOWCAP_STEW.get(), AdvancementType.TASK,
                "eaten", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.GLOWCAP_STEW.get()));

        AdvancementHolder knight = task(shardArmor, "kill_shattered_knight", ModItems.KNIGHT_CREST.get(), AdvancementType.TASK,
                "killed", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(ModEntities.SHATTERED_KNIGHT.get())));
        AdvancementHolder summonColossus = task(knight, "summon_colossus", ModItems.COLOSSUS_SUMMONING_CRYSTAL.get(), AdvancementType.TASK,
                "summoned", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                        LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(ModBlocks.COLOSSUS_ALTAR.get())),
                        ItemPredicate.Builder.item().of(ModItems.COLOSSUS_SUMMONING_CRYSTAL.get())));
        AdvancementHolder killColossus = advancement(summonColossus, "kill_colossus", ModBlocks.COLOSSUS_TROPHY.get(), AdvancementType.GOAL, 200,
                "killed", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(ModEntities.CRYSTAL_COLOSSUS.get())));
        AdvancementHolder colossusArmor = advancement(killColossus, "colossus_armor", ModItems.COLOSSUS_CHESTPLATE.get(), AdvancementType.GOAL, 100,
                "colossus_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.COLOSSUS_HELMET.get(), ModItems.COLOSSUS_CHESTPLATE.get(),
                        ModItems.COLOSSUS_LEGGINGS.get(), ModItems.COLOSSUS_BOOTS.get()));
        AdvancementHolder summonQueen = task(colossusArmor, "summon_queen", ModItems.COLOSSUS_CORE.get(), AdvancementType.TASK,
                "summoned", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                        LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(ModBlocks.QUEEN_ALTAR.get())),
                        ItemPredicate.Builder.item().of(ModItems.COLOSSUS_CORE.get())));
        advancement(summonQueen, "kill_queen", ModItems.HEART_OF_THE_EXPANSE.get(), AdvancementType.CHALLENGE, 1000,
                "killed", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(ModEntities.SHARD_QUEEN.get())));
    }

    private AdvancementHolder task(AdvancementHolder parent, String id, ItemLike icon, AdvancementType type, String criterion, Criterion<?> trigger) {
        return this.advancement(parent, id, icon, type, 0, criterion, trigger);
    }

    private AdvancementHolder advancement(@Nullable AdvancementHolder parent, String id, ItemLike icon, AdvancementType type, int experience,
                                          String criterion, Criterion<?> trigger) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        if (parent != null) {
            builder.parent(parent);
        }
        builder.display(icon, title(id), description(id), null, type, true, type != AdvancementType.TASK, false)
                .addCriterion(criterion, trigger);
        if (experience > 0) {
            builder.rewards(AdvancementRewards.Builder.experience(experience));
        }
        return builder.save(this.saver, Shardline.id(id), this.fileHelper);
    }

    private static Component title(String id) {
        return Component.translatable("advancements.shardline." + id + ".title");
    }

    private static Component description(String id) {
        return Component.translatable("advancements.shardline." + id + ".description");
    }
}
