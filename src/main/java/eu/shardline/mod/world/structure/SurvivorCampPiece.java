package eu.shardline.mod.world.structure;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModLootTables;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.npc.AftermathNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Ueberlebendenlager (Kapitel 7-9): eine runde Lichtung in der Splitterweite mit Palisade aus Kristallholz, Laternen,
 * Lagerfeuer in der Mitte, drei Zelten, Ilsas Werkstatt unter einem Vordach und einer Vorratstruhe. Rhea steht am Feuer,
 * Ilsa in der Werkstatt, Kesh abseits am Rand; ihr Heimatpunkt ist der Ort, an dem sie erscheinen. Zugaenge in allen
 * vier Himmelsrichtungen.
 */
public class SurvivorCampPiece extends ShardlinePiece {
    public static final int RADIUS = 10;
    private static final int FOUNDATION = 4;
    private static final int CLEAR = 6;

    public SurvivorCampPiece(BlockPos origin, long seed) {
        super(ModStructures.SURVIVOR_CAMP_PIECE.get(), origin, seed, new BoundingBox(
                origin.getX() - RADIUS - 1, origin.getY() - FOUNDATION, origin.getZ() - RADIUS - 1,
                origin.getX() + RADIUS + 1, origin.getY() + CLEAR, origin.getZ() + RADIUS + 1));
    }

    public SurvivorCampPiece(CompoundTag tag) {
        super(ModStructures.SURVIVOR_CAMP_PIECE.get(), tag);
    }

    /** Heimatpunkte der NPCs (relativ zum Ursprung, eine Ebene ueber dem Boden). */
    public static BlockPos rheaPos(BlockPos origin) {
        return origin.offset(0, 1, -3);
    }

    public static BlockPos ilsaPos(BlockPos origin) {
        return origin.offset(5, 1, 4);
    }

    public static BlockPos keshPos(BlockPos origin) {
        return origin.offset(-7, 1, 3);
    }

    public static BlockPos chestPos(BlockPos origin) {
        return origin.offset(-2, 1, -7);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockState stone = ModBlocks.SHARDSTONE.get().defaultBlockState();
        BlockState moss = ModBlocks.CRYSTAL_MOSS.get().defaultBlockState();
        BlockState path = ModBlocks.ASH_BLOCK.get().defaultBlockState();
        BlockState fence = ModBlocks.CRYSTAL_FENCE.get().defaultBlockState();
        BlockState lantern = ModBlocks.SHARD_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, false);
        int ox = this.origin.getX();
        int oy = this.origin.getY();
        int oz = this.origin.getZ();
        BoundingBox self = this.getBoundingBox();

        // Lichtung: Fundament, Boden (Wege aus Asche zu den vier Toren), Luft darueber
        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                int dx = x - ox;
                int dz = z - oz;
                double distance = horizontalDistance(dx, dz);
                if (distance > RADIUS + 0.5) {
                    continue;
                }
                for (int depth = 1; depth <= FOUNDATION; depth++) {
                    if (distance <= RADIUS - depth) {
                        this.fill(level, chunkBox, x, oy - depth, z, stone);
                    }
                }
                boolean onPath = (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) && distance > 2.5;
                this.set(level, chunkBox, x, oy, z, onPath ? path : moss);
                for (int dy = 1; dy <= CLEAR; dy++) {
                    this.clear(level, chunkBox, x, oy + dy, z);
                }
                // Palisade mit Toren; jede sechste Stange traegt eine Laterne
                boolean ring = Math.abs(distance - RADIUS) < 0.5;
                boolean gate = Math.abs(dx) <= 1 || Math.abs(dz) <= 1;
                if (ring && !gate) {
                    this.set(level, chunkBox, x, oy + 1, z, fence);
                    this.set(level, chunkBox, x, oy + 2, z, fence);
                    if (Math.floorMod(dx * 7 + dz * 3, 6) == 0) {
                        this.set(level, chunkBox, x, oy + 3, z, lantern);
                    }
                }
            }
        }
        // Zaunstangen verbinden sich erst nach dem Setzen der Nachbarn: Form neu berechnen
        for (int x = minX(self, chunkBox); x <= maxX(self, chunkBox); x++) {
            for (int z = minZ(self, chunkBox); z <= maxZ(self, chunkBox); z++) {
                for (int dy = 1; dy <= 2; dy++) {
                    BlockPos pos = new BlockPos(x, oy + dy, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof FenceBlock) {
                        level.setBlock(pos, this.connectFence(level, pos, state), 2);
                    }
                }
            }
        }

        // Lagerfeuer mit Baenken
        this.set(level, chunkBox, ox, oy + 1, oz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        BlockState benchX = ModBlocks.CRYSTAL_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState benchZ = ModBlocks.CRYSTAL_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        for (int i = -1; i <= 1; i++) {
            this.set(level, chunkBox, ox + i, oy + 1, oz + 2, benchX);
            this.set(level, chunkBox, ox - 2, oy + 1, oz + i, benchZ);
        }

        // Zelte: Rhea (Nordwesten), Fluechtlinge (Nordosten), Kesh abseits (Suedwesten)
        this.tent(level, chunkBox, ox - 5, oz - 5, Blocks.WHITE_WOOL.defaultBlockState(), Blocks.BROWN_WOOL.defaultBlockState(), true);
        this.tent(level, chunkBox, ox + 5, oz - 5, Blocks.LIGHT_GRAY_WOOL.defaultBlockState(), Blocks.BROWN_WOOL.defaultBlockState(), true);
        this.tent(level, chunkBox, ox - 5, oz + 6, Blocks.GRAY_WOOL.defaultBlockState(), Blocks.PURPLE_WOOL.defaultBlockState(), false);

        // Ilsas Werkstatt: Vordach auf vier Stangen, Amboss, Braustand, Werkbank, Fass
        BlockState roof = ModBlocks.CRYSTAL_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        for (int dx = 3; dx <= 7; dx++) {
            for (int dz = 2; dz <= 6; dz++) {
                boolean post = (dx == 3 || dx == 7) && (dz == 2 || dz == 6);
                if (post) {
                    this.set(level, chunkBox, ox + dx, oy + 1, oz + dz, fence);
                    this.set(level, chunkBox, ox + dx, oy + 2, oz + dz, fence);
                }
                this.set(level, chunkBox, ox + dx, oy + 3, oz + dz, roof);
            }
        }
        this.set(level, chunkBox, ox + 6, oy + 1, oz + 3, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.WEST));
        this.set(level, chunkBox, ox + 6, oy + 1, oz + 5, Blocks.BREWING_STAND.defaultBlockState());
        this.set(level, chunkBox, ox + 4, oy + 1, oz + 6, Blocks.CRAFTING_TABLE.defaultBlockState());
        this.set(level, chunkBox, ox + 3, oy + 1, oz + 5, Blocks.BARREL.defaultBlockState());

        // Vorraete
        BlockPos chest = chestPos(this.origin);
        this.clear(level, chunkBox, chest.getX(), chest.getY(), chest.getZ());
        this.chest(level, chunkBox, random, chest.getX(), chest.getY(), chest.getZ(), Direction.SOUTH, ModLootTables.SURVIVOR_CAMP_CHEST);
        this.set(level, chunkBox, chest.getX() + 1, chest.getY(), chest.getZ(), Blocks.BARREL.defaultBlockState());
        this.set(level, chunkBox, ox + 2, oy + 1, oz - 2, lantern);
        this.set(level, chunkBox, ox - 2, oy + 1, oz - 2, lantern);

        // Bewohner
        this.npc(level, chunkBox, ModEntities.RHEA.get(), rheaPos(this.origin), 180.0F);
        this.npc(level, chunkBox, ModEntities.ILSA.get(), ilsaPos(this.origin), 90.0F);
        this.npc(level, chunkBox, ModEntities.KESH.get(), keshPos(this.origin), 270.0F);
    }

    private <T extends AftermathNpcEntity> void npc(WorldGenLevel level, BoundingBox chunkBox, EntityType<T> type, BlockPos pos, float yaw) {
        T npc = this.spawn(level, chunkBox, type, pos.getX(), pos.getY(), pos.getZ(), yaw);
        if (npc != null) {
            npc.setHome(pos);
        }
    }

    /**
     * Zelt 5 x 5 entlang z: Dachschraege aus Wolle (Firstreihe in der zweiten Farbe), Rueckwand geschlossen, vorne offen,
     * ein Bett an der Rueckwand. {@code openSouth}: Eingang nach Sueden (zum Feuer, wenn das Zelt im Norden steht).
     */
    private void tent(WorldGenLevel level, BoundingBox chunkBox, int cx, int cz, BlockState cloth, BlockState ridge, boolean openSouth) {
        int oy = this.origin.getY();
        int back = openSouth ? -2 : 2;
        for (int u = -2; u <= 2; u++) {
            int height = 2 - Math.abs(u);
            for (int v = -2; v <= 2; v++) {
                this.set(level, chunkBox, cx + u, oy + 1 + height, cz + v, u == 0 ? ridge : cloth);
                if (v == back) {
                    for (int dy = 1; dy <= height; dy++) {
                        this.set(level, chunkBox, cx + u, oy + dy, cz + v, cloth);
                    }
                }
            }
        }
        Direction toHead = openSouth ? Direction.NORTH : Direction.SOUTH;
        int headZ = cz + back - Integer.signum(back);
        int footZ = headZ - Integer.signum(back);
        this.set(level, chunkBox, cx, oy + 1, headZ, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, toHead)
                .setValue(BedBlock.PART, BedPart.HEAD));
        this.set(level, chunkBox, cx, oy + 1, footZ, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, toHead)
                .setValue(BedBlock.PART, BedPart.FOOT));
    }

    /** Zaunverbindungen zu benachbarten Zaeunen und festen Bloecken setzen. */
    private BlockState connectFence(WorldGenLevel level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            boolean connect = neighbor.getBlock() instanceof FenceBlock;
            state = state.setValue(switch (direction) {
                case NORTH -> FenceBlock.NORTH;
                case SOUTH -> FenceBlock.SOUTH;
                case EAST -> FenceBlock.EAST;
                default -> FenceBlock.WEST;
            }, connect);
        }
        return state;
    }
}
