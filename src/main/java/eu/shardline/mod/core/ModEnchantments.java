package eu.shardline.mod.core;

import eu.shardline.mod.Shardline;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

/**
 * Datengetriebene Verzauberungen (data/shardline/enchantment/), erzeugt ueber den Datengenerator.
 * <ul>
 *   <li>Splitterbann (I-V): +2,5 Schaden pro Stufe gegen Wesen der Splitterweite und Infizierte
 *       (Tag {@code shardline:sensitive_to_shard_bane}); schliesst Schaerfe, Bann und Nemesis aus.</li>
 *   <li>Kristallschutz (I-IV): Schutz gegen Splitter, Splitterfieber, Kristallsaeulen, Virus und Void-Zone
 *       (Tag {@code shardline:shard_damage}); schliesst die anderen Schutzverzauberungen aus.</li>
 * </ul>
 */
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> SHARD_BANE = ResourceKey.create(Registries.ENCHANTMENT, Shardline.id("shard_bane"));
    public static final ResourceKey<Enchantment> CRYSTAL_WARD = ResourceKey.create(Registries.ENCHANTMENT, Shardline.id("crystal_ward"));

    private ModEnchantments() {
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        context.register(SHARD_BANE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE), items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 5, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(25, 8), 2, EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(2.5F)),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(ModTags.EntityTypes.SENSITIVE_TO_SHARD_BANE))))
                .build(SHARD_BANE.location()));

        context.register(CRYSTAL_WARD, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 5, 4, Enchantment.dynamicCost(8, 8), Enchantment.dynamicCost(16, 8),
                        2, EquipmentSlotGroup.ARMOR))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.DAMAGE_PROTECTION, new AddValue(LevelBasedValue.perLevel(2.0F)),
                        AllOfCondition.allOf(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
                                .tag(TagPredicate.is(ModTags.DamageTypes.SHARD_DAMAGE))
                                .tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))))
                .build(CRYSTAL_WARD.location()));
    }
}
