package eu.shardline.mod.item;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.custom.BreachCoreItem;
import eu.shardline.mod.item.custom.ColossusGauntletItem;
import eu.shardline.mod.item.custom.CrystalBowItem;
import eu.shardline.mod.item.custom.HeartOfTheExpanseItem;
import eu.shardline.mod.item.custom.LoreBookItem;
import eu.shardline.mod.item.custom.LoreItem;
import eu.shardline.mod.item.custom.ModSmithingTemplates;
import eu.shardline.mod.item.custom.PurificationEssenceItem;
import eu.shardline.mod.item.custom.QueensShardbladeItem;
import eu.shardline.mod.item.custom.ShardKeyItem;
import eu.shardline.mod.item.custom.ShardlineArmorItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shardline.MODID);
    public static final List<DeferredItem<DeferredSpawnEggItem>> SPAWN_EGGS = new ArrayList<>();

    // --- Rohstoffe ---
    public static final DeferredItem<Item> SHARD_FRAGMENT = ITEMS.registerSimpleItem("shard_fragment");
    public static final DeferredItem<Item> RAW_SHARD = ITEMS.registerSimpleItem("raw_shard");
    public static final DeferredItem<Item> SHARD_INGOT = ITEMS.registerSimpleItem("shard_ingot");
    public static final DeferredItem<Item> ASH_DUST = ITEMS.registerSimpleItem("ash_dust");
    public static final DeferredItem<LoreItem> SCOUT_EYE = ITEMS.registerItem("scout_eye", LoreItem::new);
    public static final DeferredItem<LoreItem> VOID_RAY_HIDE = ITEMS.registerItem("void_ray_hide", LoreItem::new);
    public static final DeferredItem<LoreItem> SHADOW_PELT = ITEMS.registerItem("shadow_pelt", LoreItem::new);
    public static final DeferredItem<LoreItem> KNIGHT_CREST = ITEMS.registerItem("knight_crest", LoreItem::new,
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> LUMINOUS_BERRY = ITEMS.registerSimpleItem("luminous_berry",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.4F).alwaysEdible()
                    .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 300), 1.0F).build()));

    // --- Fortschritt ---
    public static final DeferredItem<ShardKeyItem> SHARD_KEY = ITEMS.registerItem("shard_key", ShardKeyItem::new,
            new Item.Properties().durability(8).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<LoreItem> COLOSSUS_SUMMONING_CRYSTAL = ITEMS.registerItem("colossus_summoning_crystal", LoreItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<LoreItem> COLOSSUS_CORE = ITEMS.registerItem("colossus_core", LoreItem::new,
            new Item.Properties().rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<SmithingTemplateItem> COLOSSUS_UPGRADE_SMITHING_TEMPLATE = ITEMS.register(
            "colossus_upgrade_smithing_template", ModSmithingTemplates::createColossusUpgradeTemplate);
    public static final DeferredItem<HeartOfTheExpanseItem> HEART_OF_THE_EXPANSE = ITEMS.registerItem("heart_of_the_expanse",
            HeartOfTheExpanseItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    // --- Story & Shardvirus ---
    public static final DeferredItem<LoreBookItem> LORE_BOOK = ITEMS.registerItem("lore_book", LoreBookItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<PurificationEssenceItem> PURIFICATION_ESSENCE = ITEMS.registerItem("purification_essence",
            PurificationEssenceItem::new, new Item.Properties().stacksTo(16).rarity(Rarity.RARE));
    public static final DeferredItem<BreachCoreItem> BREACH_CORE = ITEMS.registerItem("breach_core", BreachCoreItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    // --- Shard-Ruestung (Set 1) ---
    public static final DeferredItem<ShardlineArmorItem> SHARD_HELMET = shardArmor("shard_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ShardlineArmorItem> SHARD_CHESTPLATE = shardArmor("shard_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ShardlineArmorItem> SHARD_LEGGINGS = shardArmor("shard_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ShardlineArmorItem> SHARD_BOOTS = shardArmor("shard_boots", ArmorItem.Type.BOOTS);

    // --- Shard-Werkzeuge ---
    public static final DeferredItem<SwordItem> SHARD_SWORD = ITEMS.register("shard_sword", () -> new SwordItem(ModTiers.SHARD,
            new Item.Properties().attributes(SwordItem.createAttributes(ModTiers.SHARD, 3, -2.4F))));
    public static final DeferredItem<PickaxeItem> SHARD_PICKAXE = ITEMS.register("shard_pickaxe", () -> new PickaxeItem(ModTiers.SHARD,
            new Item.Properties().attributes(DiggerItem.createAttributes(ModTiers.SHARD, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> SHARD_AXE = ITEMS.register("shard_axe", () -> new AxeItem(ModTiers.SHARD,
            new Item.Properties().attributes(DiggerItem.createAttributes(ModTiers.SHARD, 6.0F, -3.05F))));
    public static final DeferredItem<ShovelItem> SHARD_SHOVEL = ITEMS.register("shard_shovel", () -> new ShovelItem(ModTiers.SHARD,
            new Item.Properties().attributes(DiggerItem.createAttributes(ModTiers.SHARD, 1.5F, -3.0F))));

    // --- Kolossruestung (Set 2) ---
    public static final DeferredItem<ShardlineArmorItem> COLOSSUS_HELMET = colossusArmor("colossus_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ShardlineArmorItem> COLOSSUS_CHESTPLATE = colossusArmor("colossus_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ShardlineArmorItem> COLOSSUS_LEGGINGS = colossusArmor("colossus_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ShardlineArmorItem> COLOSSUS_BOOTS = colossusArmor("colossus_boots", ArmorItem.Type.BOOTS);

    // --- Koloss-Werkzeuge ---
    public static final DeferredItem<SwordItem> COLOSSUS_SWORD = ITEMS.register("colossus_sword", () -> new SwordItem(ModTiers.COLOSSUS,
            new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(ModTiers.COLOSSUS, 3, -2.4F))));
    public static final DeferredItem<PickaxeItem> COLOSSUS_PICKAXE = ITEMS.register("colossus_pickaxe", () -> new PickaxeItem(ModTiers.COLOSSUS,
            new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(ModTiers.COLOSSUS, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> COLOSSUS_AXE = ITEMS.register("colossus_axe", () -> new AxeItem(ModTiers.COLOSSUS,
            new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(ModTiers.COLOSSUS, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> COLOSSUS_SHOVEL = ITEMS.register("colossus_shovel", () -> new ShovelItem(ModTiers.COLOSSUS,
            new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(ModTiers.COLOSSUS, 1.5F, -3.0F))));

    // --- Spezialwaffen ---
    public static final DeferredItem<CrystalBowItem> CRYSTAL_BOW = ITEMS.registerItem("crystal_bow", CrystalBowItem::new,
            new Item.Properties().durability(640).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ColossusGauntletItem> COLOSSUS_GAUNTLET = ITEMS.register("colossus_gauntlet",
            () -> new ColossusGauntletItem(new Item.Properties().durability(500).rarity(Rarity.EPIC).fireResistant()
                    .attributes(ColossusGauntletItem.createAttributes())));
    public static final DeferredItem<QueensShardbladeItem> QUEENS_SHARDBLADE = ITEMS.register("queens_shardblade",
            () -> new QueensShardbladeItem(ModTiers.QUEEN, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .attributes(SwordItem.createAttributes(ModTiers.QUEEN, 4, -2.2F))));

    // --- Spawn-Eier ---
    public static final DeferredItem<DeferredSpawnEggItem> SHARD_CREEPER_SPAWN_EGG = spawnEgg("shard_creeper", ModEntities.SHARD_CREEPER, 0x6A3FA0, 0xE07BFF);
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_GOLEM_SPAWN_EGG = spawnEgg("crystal_golem", ModEntities.CRYSTAL_GOLEM, 0x5B8FB9, 0xC6F1FF);
    public static final DeferredItem<DeferredSpawnEggItem> GLOW_MOTH_SPAWN_EGG = spawnEgg("glow_moth", ModEntities.GLOW_MOTH, 0x2C3E50, 0x9AF7FF);
    public static final DeferredItem<DeferredSpawnEggItem> ASH_WALKER_SPAWN_EGG = spawnEgg("ash_walker", ModEntities.ASH_WALKER, 0x4A4A4A, 0xFF7A3D);
    public static final DeferredItem<DeferredSpawnEggItem> SHARD_SCOUT_SPAWN_EGG = spawnEgg("shard_scout", ModEntities.SHARD_SCOUT, 0x7D3CC4, 0xFFD1FF);
    public static final DeferredItem<DeferredSpawnEggItem> VOID_RAY_SPAWN_EGG = spawnEgg("void_ray", ModEntities.VOID_RAY, 0x101025, 0x7A5CFF);
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_SPIDER_SPAWN_EGG = spawnEgg("crystal_spider", ModEntities.CRYSTAL_SPIDER, 0x2B1B3D, 0x7FE7FF);
    public static final DeferredItem<DeferredSpawnEggItem> SHARD_TRADER_SPAWN_EGG = spawnEgg("shard_trader", ModEntities.SHARD_TRADER, 0x3F2D5C, 0xE8C66A);
    public static final DeferredItem<DeferredSpawnEggItem> SHADOW_FOX_SPAWN_EGG = spawnEgg("shadow_fox", ModEntities.SHADOW_FOX, 0x111111, 0x6A2C8C);
    public static final DeferredItem<DeferredSpawnEggItem> SHATTERED_KNIGHT_SPAWN_EGG = spawnEgg("shattered_knight", ModEntities.SHATTERED_KNIGHT, 0x3A3A4A, 0xB486FF);
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTAL_COLOSSUS_SPAWN_EGG = spawnEgg("crystal_colossus", ModEntities.CRYSTAL_COLOSSUS, 0x4B2C7A, 0x9FFCFF);
    public static final DeferredItem<DeferredSpawnEggItem> SHARD_QUEEN_SPAWN_EGG = spawnEgg("shard_queen", ModEntities.SHARD_QUEEN, 0x1A0B2E, 0xFF5EDB);
    public static final DeferredItem<DeferredSpawnEggItem> SHARD_ZOMBIE_SPAWN_EGG = spawnEgg("shard_zombie", ModEntities.SHARD_ZOMBIE, 0x3B2A55, 0xC05CFF);
    public static final DeferredItem<DeferredSpawnEggItem> CRYSTALLIZED_COW_SPAWN_EGG = spawnEgg("crystallized_cow", ModEntities.CRYSTALLIZED_COW, 0x4A3A3A, 0xB57BFF);
    public static final DeferredItem<DeferredSpawnEggItem> VIRUS_HEART_SPAWN_EGG = spawnEgg("virus_heart", ModEntities.VIRUS_HEART, 0x240A3A, 0xE040FF);

    private ModItems() {
    }

    private static DeferredItem<ShardlineArmorItem> shardArmor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ShardlineArmorItem(ModArmorMaterials.SHARD, type,
                new Item.Properties().durability(type.getDurability(ModArmorMaterials.SHARD_DURABILITY)), ShardlineArmorItem.SetType.SHARD));
    }

    private static DeferredItem<ShardlineArmorItem> colossusArmor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ShardlineArmorItem(ModArmorMaterials.COLOSSUS, type,
                new Item.Properties().fireResistant().durability(type.getDurability(ModArmorMaterials.COLOSSUS_DURABILITY)),
                ShardlineArmorItem.SetType.COLOSSUS));
    }

    private static DeferredItem<DeferredSpawnEggItem> spawnEgg(String mob, Supplier<? extends EntityType<? extends Mob>> type,
                                                               int background, int highlight) {
        DeferredItem<DeferredSpawnEggItem> egg = ITEMS.register(mob + "_spawn_egg",
                () -> new DeferredSpawnEggItem(type, background, highlight, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        return egg;
    }
}
