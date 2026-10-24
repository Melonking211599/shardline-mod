package eu.shardline.mod.virus;

import com.mojang.serialization.Codec;
import eu.shardline.mod.config.ShardlineConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/**
 * Virusstaemme. Der Stamm haengt am befallenen Block und bestimmt Farbe, Ausbreitungstempo und die Wirkung auf
 * Lebewesen, die darauf stehen:
 * <ul>
 *   <li>{@link #SHARD} Shardvirus (Oberwelt, Splitterweite): Splitterfieber, Kristallisierung.</li>
 *   <li>{@link #EMBER} Glutbrand (Nether): breitet sich schneller aus, setzt Lebewesen kurz in Brand.</li>
 *   <li>{@link #VOID} Leerenfaeule (End): langsamer, verdunkelt die Sicht.</li>
 * </ul>
 */
public enum VirusStrain implements StringRepresentable {
    SHARD("shard"),
    EMBER("ember"),
    VOID("void");

    public static final Codec<VirusStrain> CODEC = StringRepresentable.fromEnum(VirusStrain::values);

    private final String name;

    VirusStrain(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** Faktor auf {@code virus.spreadChance}. */
    public double spreadMultiplier() {
        return switch (this) {
            case SHARD -> 1.0;
            case EMBER -> ShardlineConfig.VIRUS_EMBER_SPREAD_MULTIPLIER.get();
            case VOID -> ShardlineConfig.VIRUS_VOID_SPREAD_MULTIPLIER.get();
        };
    }

    /** Stamm, der in einer Dimension entsteht (Nether: Glutbrand, End: Leerenfaeule, sonst Shardvirus). */
    public static VirusStrain of(ResourceKey<Level> dimension) {
        if (dimension == Level.NETHER) {
            return EMBER;
        }
        return dimension == Level.END ? VOID : SHARD;
    }
}
