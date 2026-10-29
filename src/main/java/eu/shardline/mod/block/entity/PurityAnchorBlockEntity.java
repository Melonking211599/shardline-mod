package eu.shardline.mod.block.entity;

import eu.shardline.mod.virus.PurityAnchors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Daten des Reinheitsankers fuer die Anzeige der Zone. Die Wahrheit liegt auf dem Server
 * ({@link eu.shardline.mod.story.ShardlineWorldData}); der Anker spiegelt Setzzeit und Radien hierher und
 * schickt sie per Block-Update an die Clients. Der Renderer berechnet daraus nur noch den Kreis.
 */
public class PurityAnchorBlockEntity extends BlockEntity {
    /** Setzzeit (Spielzeit) oder -1 fuer "voller Radius" (alte Anker). */
    private long since = -1L;
    private int startRadius = 6;
    private int maxRadius = 24;
    private long growTicks;

    public PurityAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PURITY_ANCHOR.get(), pos, state);
    }

    /** Uebernimmt die Werte vom Server; meldet nur bei Aenderung an die Clients. */
    public void sync(long since, int startRadius, int maxRadius, long growTicks) {
        if (this.since == since && this.startRadius == startRadius && this.maxRadius == maxRadius && this.growTicks == growTicks) {
            return;
        }
        this.since = since;
        this.startRadius = startRadius;
        this.maxRadius = maxRadius;
        this.growTicks = growTicks;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public double radius(long gameTime) {
        return PurityAnchors.radius(this.since, gameTime, this.startRadius, this.maxRadius, this.growTicks);
    }

    public boolean isGrowing(long gameTime) {
        return this.since >= 0L && this.growTicks > 0L && gameTime - this.since < this.growTicks;
    }

    public int maxRadius() {
        return Math.max(this.startRadius, this.maxRadius);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Since", this.since);
        tag.putInt("StartRadius", this.startRadius);
        tag.putInt("MaxRadius", this.maxRadius);
        tag.putLong("GrowTicks", this.growTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.since = tag.contains("Since") ? tag.getLong("Since") : -1L;
        this.startRadius = tag.contains("StartRadius") ? tag.getInt("StartRadius") : this.startRadius;
        this.maxRadius = tag.contains("MaxRadius") ? tag.getInt("MaxRadius") : this.maxRadius;
        this.growTicks = tag.getLong("GrowTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
