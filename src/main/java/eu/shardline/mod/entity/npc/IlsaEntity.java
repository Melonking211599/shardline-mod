package eu.shardline.mod.entity.npc;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.dialogue.DialogueSessions;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/**
 * Ilsa Brandt, Schmiedin und Alchemistin aus Birkenfurt. Baut Laternen, braut Reinigungsessenz und handelt gegen
 * Shard-Splitter (Aktion {@code open_trade} im Dialog). Im versiegelten Ende hilft sie beim Wiederaufbau, im
 * beherrschten Ende fuehrt sie heimlich die Stimmlosen an. Die Angebote fuellen sich jeden Morgen wieder auf.
 */
public class IlsaEntity extends AftermathNpcEntity implements Merchant {
    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;
    private long lastRestockDay = -1;

    public IlsaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public String npcId() {
        return "ilsa";
    }

    // ------------------------------------------------------------------ Handel

    @Override
    public void openTrade(ServerPlayer player) {
        if (this.tradingPlayer != null && this.tradingPlayer != player) {
            return;   // handelt schon mit jemand anderem
        }
        DialogueSessions.close(player, false);
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = buildOffers();
        }
        return this.offers;
    }

    /** Feste Angebote: Werkstatt und Kueche des Lagers, bezahlt in Shard-Splittern. */
    public static MerchantOffers buildOffers() {
        MerchantOffers list = new MerchantOffers();
        list.add(buy(ModItems.PURIFICATION_ESSENCE.get(), 1, 10, 4));
        list.add(buy(ModBlocks.SHARD_LANTERN.get(), 2, 6, 8));
        list.add(buy(ModItems.BERRY_TART.get(), 3, 4, 12));
        list.add(buy(ModItems.CRYSTAL_CANDY.get(), 4, 3, 12));
        list.add(new MerchantOffer(new ItemCost(ModItems.SHARD_FRAGMENT.get(), 16), Optional.of(new ItemCost(Items.COMPASS, 1)),
                new ItemStack(ModItems.RESONANCE_COMPASS.get()), 1, 10, 0.1F));
        list.add(sell(ModItems.ASH_DUST.get(), 16, 3));
        list.add(sell(ModItems.CRYSTAL_SLIME.get(), 4, 2));
        list.add(sell(ModItems.LUMINOUS_BERRY.get(), 16, 2));
        return list;
    }

    private static MerchantOffer buy(ItemLike item, int count, int shards, int maxUses) {
        return new MerchantOffer(new ItemCost(ModItems.SHARD_FRAGMENT.get(), shards), Optional.empty(), new ItemStack(item, count), maxUses, 5, 0.05F);
    }

    private static MerchantOffer sell(ItemLike item, int count, int shards) {
        return new MerchantOffer(new ItemCost(item, count), Optional.empty(), new ItemStack(ModItems.SHARD_FRAGMENT.get(), shards), 16, 3, 0.05F);
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(ModSounds.ILSA_VOICE.get(), 0.8F, 1.2F);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return ModSounds.ILSA_VOICE.get();
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        if (this.tradingPlayer != null) {
            if (!this.tradingPlayer.isAlive() || this.tradingPlayer.distanceToSqr(this) > 64.0) {
                this.tradingPlayer = null;
            } else {
                this.getNavigation().stop();
                this.getLookControl().setLookAt(this.tradingPlayer);
            }
        }
        long day = this.level().getDayTime() / 24000L;
        if (this.offers != null && day != this.lastRestockDay && this.tickCount % 100 == 0) {
            this.lastRestockDay = day;
            this.offers.forEach(MerchantOffer::resetUses);
        }
    }

    @Override
    protected boolean canRest() {
        return this.tradingPlayer == null;
    }

    // ------------------------------------------------------------------ Speichern und Klaenge

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.offers != null) {
            MerchantOffers.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.offers)
                    .ifSuccess(nbt -> tag.put("Offers", nbt));
        }
        tag.putLong("RestockDay", this.lastRestockDay);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Offers")) {
            MerchantOffers.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("Offers"))
                    .ifSuccess(loaded -> this.offers = loaded);
        }
        this.lastRestockDay = tag.getLong("RestockDay");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isResting() || this.isTalking() ? null : ModSounds.ILSA_VOICE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ILSA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ILSA_DEATH.get();
    }
}
