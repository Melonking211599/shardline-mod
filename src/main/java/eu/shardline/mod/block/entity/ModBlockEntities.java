package eu.shardline.mod.block.entity;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block-Entities der Mod.
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Shardline.MODID);

    /** Reinheitsanker: kennt Setzzeit und Radien, damit der Client die wachsende Zone zeichnen kann. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PurityAnchorBlockEntity>> PURITY_ANCHOR =
            BLOCK_ENTITY_TYPES.register("purity_anchor", () -> BlockEntityType.Builder.of(PurityAnchorBlockEntity::new,
                    ModBlocks.PURITY_ANCHOR.get()).build(null));

    private ModBlockEntities() {
    }
}
