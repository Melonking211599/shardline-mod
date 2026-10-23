package eu.shardline.mod.world;

import com.google.common.collect.ImmutableSet;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Point of Interest fuer Splitterportal-Bloecke, damit bestehende Portale schnell gefunden werden.
 */
public final class ModPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Shardline.MODID);

    public static final ResourceKey<PoiType> SHARD_PORTAL_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Shardline.id("shard_portal"));

    public static final DeferredHolder<PoiType, PoiType> SHARD_PORTAL = POI_TYPES.register("shard_portal",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.SHARD_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    private ModPoiTypes() {
    }
}
