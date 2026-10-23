package eu.shardline.mod.world.portal;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.ShardPortalBlock;
import eu.shardline.mod.world.ModPoiTypes;
import eu.shardline.mod.world.dimension.ModDimensions;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Sucht ein bestehendes Splitterportal in der Zieldimension oder baut ein neues.
 * In der Splitterweite wird bei fehlendem Boden eine schwebende Plattform erzeugt.
 */
public final class ShardPortalForcer {
    private static final int SEARCH_RADIUS = 48;
    private static final int PLACEMENT_RADIUS = 16;

    private ShardPortalForcer() {
    }

    public static Optional<BlockPos> findClosestPortalPosition(ServerLevel level, BlockPos exitPos, WorldBorder border) {
        PoiManager poiManager = level.getPoiManager();
        poiManager.ensureLoadedAndValid(level, exitPos, SEARCH_RADIUS);
        return poiManager.getInSquare(type -> type.is(ModPoiTypes.SHARD_PORTAL_KEY), exitPos, SEARCH_RADIUS, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(border::isWithinBounds)
                .filter(pos -> level.getBlockState(pos).is(ModBlocks.SHARD_PORTAL.get()))
                .min(Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(exitPos)).thenComparingInt(Vec3i::getY));
    }

    public static Optional<BlockUtil.FoundRectangle> createPortal(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        boolean floatingWorld = level.dimension() == ModDimensions.SPLITTERWEITE_LEVEL;
        double bestDistance = -1.0;
        BlockPos best = null;
        double fallbackDistance = -1.0;
        BlockPos fallback = null;
        WorldBorder border = level.getWorldBorder();
        int maxY = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos cursor = pos.mutable();

        for (BlockPos.MutableBlockPos column : BlockPos.spiralAround(pos, PLACEMENT_RADIUS, Direction.EAST, Direction.SOUTH)) {
            int top = Math.min(maxY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()));
            if (!border.isWithinBounds(column) || !border.isWithinBounds(column.move(direction, 1))) {
                continue;
            }
            column.move(direction.getOpposite(), 1);
            for (int y = top; y >= level.getMinBuildHeight(); y--) {
                column.setY(y);
                if (!canPortalReplaceBlock(level, column)) {
                    continue;
                }
                int start = y;
                while (y > level.getMinBuildHeight() && canPortalReplaceBlock(level, column.move(Direction.DOWN))) {
                    y--;
                }
                if (y + 4 > maxY) {
                    continue;
                }
                int gap = start - y;
                if (gap > 0 && gap < 3) {
                    continue;
                }
                column.setY(y);
                if (canHostFrame(level, column, cursor, direction, 0)) {
                    double distance = pos.distSqr(column);
                    if (canHostFrame(level, column, cursor, direction, -1) && canHostFrame(level, column, cursor, direction, 1)
                            && (bestDistance == -1.0 || bestDistance > distance)) {
                        bestDistance = distance;
                        best = column.immutable();
                    }
                    if (bestDistance == -1.0 && (fallbackDistance == -1.0 || fallbackDistance > distance)) {
                        fallbackDistance = distance;
                        fallback = column.immutable();
                    }
                }
            }
        }

        if (best == null && fallback != null) {
            best = fallback;
        }

        if (best == null) {
            // Kein Platz gefunden: schwebende Plattform bauen.
            int minY = Math.max(level.getMinBuildHeight() + 1, floatingWorld ? 96 : 70);
            int maxPlatformY = maxY - 9;
            if (maxPlatformY < minY) {
                return Optional.empty();
            }
            best = border.clampToBounds(new BlockPos(pos.getX() - direction.getStepX(),
                    Mth.clamp(pos.getY(), minY, floatingWorld ? Math.min(maxPlatformY, 140) : maxPlatformY),
                    pos.getZ() - direction.getStepZ()));
            Direction side = direction.getClockWise();
            BlockState platform = floatingWorld ? ModBlocks.SHARDSTONE_BRICKS.get().defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
            for (int s = -2; s <= 2; s++) {
                for (int a = -1; a <= 2; a++) {
                    for (int y = -1; y < 3; y++) {
                        BlockState state = y < 0 ? platform : Blocks.AIR.defaultBlockState();
                        cursor.setWithOffset(best, a * direction.getStepX() + s * side.getStepX(), y, a * direction.getStepZ() + s * side.getStepZ());
                        level.setBlockAndUpdate(cursor, state);
                    }
                }
            }
        }

        BlockState frame = ModBlocks.SHARD_PORTAL_FRAME.get().defaultBlockState();
        for (int a = -1; a < 3; a++) {
            for (int y = -1; y < 4; y++) {
                if (a == -1 || a == 2 || y == -1 || y == 3) {
                    cursor.setWithOffset(best, a * direction.getStepX(), y, a * direction.getStepZ());
                    level.setBlock(cursor, frame, 3);
                }
            }
        }

        BlockState portal = ModBlocks.SHARD_PORTAL.get().defaultBlockState().setValue(ShardPortalBlock.AXIS, axis);
        for (int a = 0; a < 2; a++) {
            for (int y = 0; y < 3; y++) {
                cursor.setWithOffset(best, a * direction.getStepX(), y, a * direction.getStepZ());
                level.setBlock(cursor, portal, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(best.immutable(), 2, 3));
    }

    private static boolean canPortalReplaceBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean canHostFrame(ServerLevel level, BlockPos origin, BlockPos.MutableBlockPos cursor, Direction direction, int offsetScale) {
        Direction side = direction.getClockWise();
        for (int a = -1; a < 3; a++) {
            for (int y = -1; y < 4; y++) {
                cursor.setWithOffset(origin, direction.getStepX() * a + side.getStepX() * offsetScale, y,
                        direction.getStepZ() * a + side.getStepZ() * offsetScale);
                if (y < 0 && !level.getBlockState(cursor).isSolid()) {
                    return false;
                }
                if (y >= 0 && !canPortalReplaceBlock(level, cursor)) {
                    return false;
                }
            }
        }
        return true;
    }
}
