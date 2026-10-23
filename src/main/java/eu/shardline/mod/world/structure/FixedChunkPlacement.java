package eu.shardline.mod.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

/**
 * Platziert eine Struktur genau in einem festen Chunk (fuer die Arena im Zentrum der Splitterweite).
 */
public class FixedChunkPlacement extends StructurePlacement {
    public static final MapCodec<FixedChunkPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("chunk_x").forGetter(placement -> placement.chunkX),
            Codec.INT.fieldOf("chunk_z").forGetter(placement -> placement.chunkZ),
            Codec.INT.fieldOf("salt").forGetter(placement -> placement.salt())
    ).apply(instance, FixedChunkPlacement::new));

    private final int chunkX;
    private final int chunkZ;

    public FixedChunkPlacement(int chunkX, int chunkZ, int salt) {
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, salt, Optional.empty());
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        return x == this.chunkX && z == this.chunkZ;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.FIXED_CHUNK.get();
    }
}
