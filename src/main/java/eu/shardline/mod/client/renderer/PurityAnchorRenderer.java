package eu.shardline.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import eu.shardline.mod.block.entity.PurityAnchorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Goldene Linie am Boden entlang des Zonenrands eines Reinheitsankers. Der Radius kommt aus den vom Server
 * gemeldeten Werten (Setzzeit, Start, Ende, Wachstumsdauer); die Hoehe jedes Punkts aus der Hoehenkarte des
 * Clients, damit die Linie dem Gelaende folgt. Waehrend die Zone waechst, pulsiert die Linie heller.
 */
public class PurityAnchorRenderer implements BlockEntityRenderer<PurityAnchorBlockEntity> {
    private static final float GOLD_R = 0xF4 / 255.0F;
    private static final float GOLD_G = 0xC9 / 255.0F;
    private static final float GOLD_B = 0x5A / 255.0F;

    public PurityAnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PurityAnchorBlockEntity anchor, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       int packedOverlay) {
        Level level = anchor.getLevel();
        if (level == null) {
            return;
        }
        long time = level.getGameTime();
        double radius = anchor.radius(time);
        boolean growing = anchor.isGrowing(time);
        float pulse = 0.55F + 0.25F * Mth.sin((time + partialTick) * (growing ? 0.25F : 0.08F));
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        Matrix4f pose = poseStack.last().pose();
        BlockPos center = anchor.getBlockPos();
        int segments = Mth.clamp((int) (radius * 6.0), 32, 192);
        // Doppellinie: aussen kraeftig, innen schwach
        drawRing(level, lines, poseStack, pose, center, radius, segments, pulse);
        drawRing(level, lines, poseStack, pose, center, Math.max(0.5, radius - 0.4), segments, pulse * 0.45F);
    }

    private static void drawRing(Level level, VertexConsumer lines, PoseStack poseStack, Matrix4f pose, BlockPos center, double radius,
                                 int segments, float alpha) {
        double cx = center.getX() + 0.5;
        double cz = center.getZ() + 0.5;
        float[] prev = null;
        for (int i = 0; i <= segments; i++) {
            double angle = i * Math.PI * 2.0 / segments;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
            float[] point = {(float) (x - center.getX()), (float) (ground + 0.06 - center.getY()), (float) (z - center.getZ())};
            if (prev != null && Math.abs(point[1] - prev[1]) <= 4.0F) {
                float nx = point[0] - prev[0];
                float ny = point[1] - prev[1];
                float nz = point[2] - prev[2];
                float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                if (length > 0.0F) {
                    nx /= length;
                    ny /= length;
                    nz /= length;
                    lines.addVertex(pose, prev[0], prev[1], prev[2]).setColor(GOLD_R, GOLD_G, GOLD_B, alpha)
                            .setNormal(poseStack.last(), nx, ny, nz);
                    lines.addVertex(pose, point[0], point[1], point[2]).setColor(GOLD_R, GOLD_G, GOLD_B, alpha)
                            .setNormal(poseStack.last(), nx, ny, nz);
                }
            }
            prev = point;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(PurityAnchorBlockEntity anchor) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(PurityAnchorBlockEntity anchor) {
        int r = anchor.maxRadius() + 1;
        BlockPos pos = anchor.getBlockPos();
        return new AABB(pos.getX() - r, pos.getY() - 16, pos.getZ() - r, pos.getX() + r + 1, pos.getY() + 16, pos.getZ() + r + 1);
    }
}
