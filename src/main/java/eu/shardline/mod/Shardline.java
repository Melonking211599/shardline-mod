package eu.shardline.mod;

import com.mojang.logging.LogUtils;
import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.config.ShardlineClientConfig;
import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModAttachments;
import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.effect.ModEffects;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.item.ModArmorMaterials;
import eu.shardline.mod.item.ModCreativeTabs;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.world.ModPoiTypes;
import eu.shardline.mod.world.structure.ModStructures;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Einstiegspunkt der Shardline-Mod. Registriert alle DeferredRegister und Configs.
 */
@Mod(Shardline.MODID)
public final class Shardline {
    public static final String MODID = "shardline";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Shardline(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModEffects.MOB_EFFECTS.register(modBus);
        ModPoiTypes.POI_TYPES.register(modBus);
        ModStructures.STRUCTURE_TYPES.register(modBus);
        ModStructures.STRUCTURE_PIECES.register(modBus);
        ModStructures.STRUCTURE_PLACEMENTS.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, ShardlineConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ShardlineClientConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
