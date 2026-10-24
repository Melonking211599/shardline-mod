package eu.shardline.mod.core;

import com.mojang.serialization.Codec;
import eu.shardline.mod.Shardline;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Eigene Item-Datenkomponenten.
 */
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Shardline.MODID);

    /** ID des Lore-Buchs (siehe {@link eu.shardline.mod.story.lore.LoreBooks}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> LORE_BOOK = DATA_COMPONENTS.registerComponentType(
            "lore_book", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** Gewaehltes Ziel des Resonanzkompasses (Index in der Zielliste der aktuellen Dimension). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RESONANCE_TARGET = DATA_COMPONENTS.registerComponentType(
            "resonance_target", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private ModDataComponents() {
    }
}
