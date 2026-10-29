package eu.shardline.mod.entity.mob;

import eu.shardline.mod.block.ModBlocks;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.core.ModTags;
import eu.shardline.mod.entity.ShardlineAnimations;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.story.lore.TraderDialogue;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Splitterhaendler (NPC): handelt im normalen Handelsfenster, bezahlt wird mit Shard-Splittern.
 * Bietet u. a. eine Karte zur naechsten Koloss-Arena an.
 */
public class ShardTraderEntity extends AbstractVillager implements GeoEntity {
    private static final RawAnimation TRADE = RawAnimation.begin().thenLoop("trade");
    private static final int COMMON_OFFERS = 5;
    private static final int RARE_OFFERS = 2;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Wird erst gesetzt, wenn ein Spieler den Haendler anspricht. */
    private boolean offersRequested;

    public ShardTraderEntity(EntityType<? extends ShardTraderEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ShardCreeperEntity.class, 8.0F, 0.5, 0.6));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, AshWalkerEntity.class, 8.0F, 0.5, 0.6));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, CrystalSpiderEntity.class, 8.0F, 0.5, 0.6));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ShatteredKnightEntity.class, 10.0F, 0.5, 0.6));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.6));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    /**
     * Rechtsklick oeffnet das Gespraech (Dialogfenster mit Porträt), Schleichen + Rechtsklick handelt direkt.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                if (player.isShiftKeyDown()) {
                    this.beginTrading(serverPlayer);
                } else {
                    TraderDialogue.open(serverPlayer, this);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /** Oeffnet das Handelsfenster (Angebote entstehen beim ersten Mal). */
    public void beginTrading(Player player) {
        this.offersRequested = true;
        if (this.getOffers().isEmpty()) {
            return;
        }
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    /**
     * Vanilla erzeugt die Angebote auch beim Speichern. Haendler werden aber schon waehrend der
     * Weltgenerierung gespeichert (Ruinen-Turm, Spawns im Kristallwald), und die Kartensuche wuerde
     * dort Chunks vom Generierungs-Thread anfordern - das blockiert den Server. Deshalb entstehen
     * die Angebote erst, wenn ein Spieler den Haendler anspricht.
     */
    @Override
    public MerchantOffers getOffers() {
        if (!this.level().isClientSide && this.offers == null && !this.offersRequested) {
            return new MerchantOffers();
        }
        return super.getOffers();
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        this.addOffersFromItemListings(offers, commonTrades(), COMMON_OFFERS);
        this.addOffersFromItemListings(offers, rareTrades(), RARE_OFFERS);
    }

    private static VillagerTrades.ItemListing[] commonTrades() {
        return new VillagerTrades.ItemListing[] {
                new BuyForShards(ModItems.LUMINOUS_BERRY.get(), 3, 6, 16),
                new BuyForShards(ModBlocks.GLOWBLOOM.get(), 4, 8, 12),
                new BuyForShards(ModBlocks.CRYSTAL_SAPLING.get(), 2, 10, 8),
                new BuyForShards(ModBlocks.GLOW_CRYSTAL_BLOCK.get(), 4, 12, 8),
                new BuyForShards(ModBlocks.SHARD_PORTAL_FRAME.get(), 4, 20, 6),
                new BuyForShards(Items.SADDLE, 1, 16, 3),
                new BuyForShards(Items.ENDER_PEARL, 2, 12, 8),
                new SellForShards(ModItems.RAW_SHARD.get(), 2, 6, 16),
                new SellForShards(ModItems.SHADOW_PELT.get(), 3, 10, 12),
                new SellForShards(ModItems.ASH_DUST.get(), 8, 4, 16),
                new SellForShards(ModItems.VOID_RAY_HIDE.get(), 2, 8, 12),
        };
    }

    private static VillagerTrades.ItemListing[] rareTrades() {
        return new VillagerTrades.ItemListing[] {
                new ColossusMapTrade(24),
                new BuyForShards(ModItems.SHARD_KEY.get(), 1, 32, 2),
                new BuyForShards(ModItems.CRYSTAL_BOW.get(), 1, 48, 1),
                new SummoningCrystalTrade(40),
        };
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int xp = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), xp));
        }
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return ModSounds.SHARD_TRADER_YES.get();
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean isYesSound) {
        return isYesSound ? ModSounds.SHARD_TRADER_YES.get() : ModSounds.SHARD_TRADER_NO.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? ModSounds.SHARD_TRADER_TRADE.get() : ModSounds.SHARD_TRADER_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHARD_TRADER_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHARD_TRADER_DEATH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> {
            if (this.isTrading()) {
                return state.setAndContinue(TRADE);
            }
            return state.setAndContinue(state.isMoving() ? ShardlineAnimations.WALK : ShardlineAnimations.IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // --- Handelsangebote ---

    /** Spieler kauft eine Ware gegen Shard-Splitter. */
    record BuyForShards(ItemLike item, int count, int shardCost, int maxUses) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(ModItems.SHARD_FRAGMENT.get(), this.shardCost), Optional.empty(),
                    new ItemStack(this.item, this.count), this.maxUses, 5, 0.05F);
        }
    }

    /** Spieler verkauft Rohstoffe gegen Shard-Splitter. */
    record SellForShards(ItemLike item, int count, int shardReward, int maxUses) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(this.item, this.count), Optional.empty(),
                    new ItemStack(ModItems.SHARD_FRAGMENT.get(), this.shardReward), this.maxUses, 3, 0.05F);
        }
    }

    /** Erweckungskristall gegen Splitter und ein Ritterwappen. */
    record SummoningCrystalTrade(int shardCost) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(ModItems.SHARD_FRAGMENT.get(), this.shardCost),
                    Optional.of(new ItemCost(ModItems.KNIGHT_CREST.get(), 1)),
                    new ItemStack(ModItems.COLOSSUS_SUMMONING_CRYSTAL.get()), 2, 15, 0.1F);
        }
    }

    /** Karte zur naechstgelegenen Koloss-Arena. */
    record ColossusMapTrade(int shardCost) implements VillagerTrades.ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            // Nur auf dem Server-Hauptthread suchen; von anderen Threads aus wuerde die Suche Chunks blockierend nachladen.
            if (!(trader.level() instanceof ServerLevel serverLevel) || !serverLevel.getServer().isSameThread()) {
                return null;
            }
            // Ohne "skipKnown": mehrere Karten duerfen zur selben Arena fuehren, die Suche bleibt dadurch kurz.
            BlockPos target = serverLevel.findNearestMapStructure(ModTags.Structures.ON_COLOSSUS_MAPS, trader.blockPosition(), 50, false);
            if (target == null) {
                return null;
            }
            ItemStack map = MapItem.create(serverLevel, target.getX(), target.getZ(), (byte) 2, true, true);
            MapItem.renderBiomePreviewMap(serverLevel, map);
            MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.RED_X);
            map.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.shardline.colossus_arena"));
            return new MerchantOffer(new ItemCost(ModItems.SHARD_FRAGMENT.get(), this.shardCost), Optional.of(new ItemCost(Items.COMPASS, 1)),
                    map, 1, 10, 0.2F);
        }
    }
}
