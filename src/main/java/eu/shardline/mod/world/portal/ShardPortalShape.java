package eu.shardline.mod.world.portal;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.block.custom.ShardPortalBlock;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Erkennt einen rechteckigen Rahmen aus Splitterrahmen-Bloecken (2x3 bis 21x21 Innenflaeche),
 * analog zum Netherportal, aber ausschliesslich fuer den Shardline-Rahmenblock.
 */
public class ShardPortalShape {
    public static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    public static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;

    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<ShardPortalShape> findEmptyPortalShape(LevelAccessor level, BlockPos bottomLeft, Direction.Axis axis) {
        return findPortalShape(level, bottomLeft, shape -> shape.isValid() && shape.numPortalBlocks == 0, axis);
    }

    public static Optional<ShardPortalShape> findPortalShape(LevelAccessor level, BlockPos bottomLeft, Predicate<ShardPortalShape> predicate, Direction.Axis axis) {
        Optional<ShardPortalShape> shape = Optional.of(new ShardPortalShape(level, bottomLeft, axis)).filter(predicate);
        if (shape.isPresent()) {
            return shape;
        }
        Direction.Axis other = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new ShardPortalShape(level, bottomLeft, other)).filter(predicate);
    }

    public ShardPortalShape(LevelAccessor level, BlockPos bottomLeft, Direction.Axis axis) {
        this.level = level;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(bottomLeft);
        if (this.bottomLeft == null) {
            this.bottomLeft = bottomLeft;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    private static boolean isFrame(BlockState state) {
        return state.is(ModBlocks.SHARD_PORTAL_FRAME.get());
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(ModBlocks.SHARD_PORTAL.get()) || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int minY = Math.max(this.level.getMinBuildHeight(), pos.getY() - MAX_HEIGHT);
        while (pos.getY() > minY && isEmpty(this.level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        Direction leftDir = this.rightDir.getOpposite();
        int distance = this.getDistanceUntilEdgeAboveFrame(pos, leftDir) - 1;
        return distance < 0 ? null : pos.relative(leftDir, distance);
    }

    private int calculateWidth() {
        int distance = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return distance >= MIN_WIDTH && distance <= MAX_WIDTH ? distance : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= MAX_WIDTH; i++) {
            cursor.set(pos).move(direction, i);
            BlockState state = this.level.getBlockState(cursor);
            if (!isEmpty(state)) {
                if (isFrame(state)) {
                    return i;
                }
                break;
            }
            BlockState below = this.level.getBlockState(cursor.move(Direction.DOWN));
            if (!isFrame(below)) {
                break;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int distance = this.getDistanceUntilTop(cursor);
        return distance >= MIN_HEIGHT && distance <= MAX_HEIGHT && this.hasTopFrame(cursor, distance) ? distance : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos cursor, int distanceToTop) {
        for (int i = 0; i < this.width; i++) {
            cursor.set(this.bottomLeft).move(Direction.UP, distanceToTop).move(this.rightDir, i);
            if (!isFrame(this.level.getBlockState(cursor))) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos cursor) {
        for (int i = 0; i < MAX_HEIGHT; i++) {
            cursor.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
            if (!isFrame(this.level.getBlockState(cursor))) {
                return i;
            }
            cursor.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
            if (!isFrame(this.level.getBlockState(cursor))) {
                return i;
            }
            for (int j = 0; j < this.width; j++) {
                cursor.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
                BlockState state = this.level.getBlockState(cursor);
                if (!isEmpty(state)) {
                    return i;
                }
                if (state.is(ModBlocks.SHARD_PORTAL.get())) {
                    this.numPortalBlocks++;
                }
            }
        }
        return MAX_HEIGHT;
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= MIN_WIDTH && this.width <= MAX_WIDTH
                && this.height >= MIN_HEIGHT && this.height <= MAX_HEIGHT;
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public void createPortalBlocks() {
        BlockState portal = ModBlocks.SHARD_PORTAL.get().defaultBlockState().setValue(ShardPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
                .forEach(pos -> this.level.setBlock(pos, portal, 18));
    }
}
