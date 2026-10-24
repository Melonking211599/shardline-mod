package eu.shardline.mod.client;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.block.entity.ModBlockEntities;
import eu.shardline.mod.client.particle.ShardParticle;
import eu.shardline.mod.client.renderer.ArmorExtraModels;
import eu.shardline.mod.client.renderer.ArmorExtrasLayer;
import eu.shardline.mod.client.renderer.CrystalGolemRenderer;
import eu.shardline.mod.client.renderer.InfectedRenderers;
import eu.shardline.mod.client.renderer.KeeperCapeLayer;
import eu.shardline.mod.client.renderer.PurityAnchorRenderer;
import eu.shardline.mod.client.renderer.QueenCloneRenderer;
import eu.shardline.mod.client.renderer.ShardCreeperRenderer;
import eu.shardline.mod.client.renderer.ShardlineGeoRenderer;
import eu.shardline.mod.client.renderer.SlimArmorModels;
import eu.shardline.mod.client.renderer.VoidRayRenderer;
import eu.shardline.mod.client.sky.SplitterweiteEffects;
import eu.shardline.mod.client.story.ChapterCardOverlay;
import eu.shardline.mod.client.story.StoryScreenEffects;
import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.core.ModParticles;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.entity.boss.VirusHeartEntity;
import eu.shardline.mod.item.ModItems;
import eu.shardline.mod.world.dimension.ModDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-Einstieg: Renderer, Partikel, Himmel, Tasten und Konfigurationsbildschirm.
 */
@Mod(value = Shardline.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Shardline.MODID, value = Dist.CLIENT)
public final class ShardlineClient {
    public ShardlineClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.CRYSTAL_BOW.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) -> {
                if (entity == null || entity.getUseItem() != stack) {
                    return 0.0F;
                }
                return (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
            });
            ItemProperties.register(ModItems.CRYSTAL_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            // Kapitel-Emblem: Kapitel 1-6 als 0.1-0.6 fuer die Modell-Overrides
            ItemProperties.register(ModItems.CHAPTER_EMBLEM.get(), Shardline.id("chapter"),
                    (stack, level, entity, seed) -> stack.getOrDefault(ModDataComponents.CHAPTER.get(), 1) / 10.0F);
        });
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHARD_CREEPER.get(), ShardCreeperRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_GOLEM.get(), CrystalGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.GLOW_MOTH.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "glow_moth", 0.2F, true));
        event.registerEntityRenderer(ModEntities.ASH_WALKER.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "ash_walker", 0.5F, false));
        event.registerEntityRenderer(ModEntities.SHARD_SCOUT.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "shard_scout", 0.4F, true));
        event.registerEntityRenderer(ModEntities.VOID_RAY.get(), VoidRayRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_SPIDER.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "crystal_spider", 0.8F, true));
        event.registerEntityRenderer(ModEntities.SHARD_TRADER.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "shard_trader", 0.5F, false));
        event.registerEntityRenderer(ModEntities.CRYSTAL_SNAIL.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "crystal_snail", 0.35F, true));
        event.registerEntityRenderer(ModEntities.SHADOW_FOX.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "shadow_fox", 0.4F, true));
        event.registerEntityRenderer(ModEntities.SHATTERED_KNIGHT.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "shattered_knight", 0.6F, true));
        event.registerEntityRenderer(ModEntities.CRYSTAL_COLOSSUS.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "crystal_colossus", 1.6F, true));
        event.registerEntityRenderer(ModEntities.SHARD_QUEEN.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "shard_queen", 0.8F, true));
        event.registerEntityRenderer(ModEntities.QUEEN_CLONE.get(), QueenCloneRenderer::new);
        event.registerEntityRenderer(ModEntities.SHARD_PROJECTILE.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.0F, true));
        event.registerEntityRenderer(ModEntities.CRYSTAL_PILLAR.get(), ctx -> new ShardlineGeoRenderer<>(ctx, "crystal_pillar", 0.0F, true));
        event.registerEntityRenderer(ModEntities.VOID_ZONE.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PURITY_ANCHOR.get(), PurityAnchorRenderer::new);
        event.registerEntityRenderer(ModEntities.SHARD_ZOMBIE.get(), InfectedRenderers.ShardZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTALLIZED_COW.get(), InfectedRenderers.CrystallizedCowRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTALLIZED_SHEEP.get(), InfectedRenderers.CrystallizedSheepRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTALLIZED_PIG.get(), InfectedRenderers.CrystallizedPigRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTALLIZED_VILLAGER.get(), InfectedRenderers.CrystallizedVillagerRenderer::new);
        // Kein Umkippen beim Tod: Der Clip "death" laesst das Herz an Ort und Stelle zerfallen.
        event.registerEntityRenderer(ModEntities.VIRUS_HEART.get(), ctx -> new ShardlineGeoRenderer<VirusHeartEntity>(ctx, "virus_heart", 1.2F, true) {
            @Override
            protected float getDeathMaxRotation(VirusHeartEntity animatable) {
                return 0.0F;
            }
        });
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ArmorExtrasLayer.SHARD_LAYER, ArmorExtraModels::shard);
        event.registerLayerDefinition(ArmorExtrasLayer.COLOSSUS_LAYER, ArmorExtraModels::colossus);
        event.registerLayerDefinition(SlimArmorModels.OUTER, SlimArmorModels::outerDefinition);
        event.registerLayerDefinition(SlimArmorModels.INNER, SlimArmorModels::innerDefinition);
    }

    @SubscribeEvent
    static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> SlimArmorModels.clear());
    }

    /** Alle Shardline-Ruestungsteile benutzen das schlanke Ruestungsmodell. */
    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                return SlimArmorModels.get(slot);
            }
        }, ModItems.SHARD_HELMET.get(), ModItems.SHARD_CHESTPLATE.get(), ModItems.SHARD_LEGGINGS.get(), ModItems.SHARD_BOOTS.get(),
                ModItems.COLOSSUS_HELMET.get(), ModItems.COLOSSUS_CHESTPLATE.get(), ModItems.COLOSSUS_LEGGINGS.get(), ModItems.COLOSSUS_BOOTS.get());
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new KeeperCapeLayer(renderer));
                renderer.addLayer(new ArmorExtrasLayer<>(renderer, event.getEntityModels()));
            }
        }
        // Ruestungsstaender und andere humanoide Wesen (Zombies, Skelette ...) zeigen die Zusatzteile ebenfalls.
        for (EntityType<?> type : event.getEntityTypes()) {
            if (event.getRenderer(type) instanceof LivingEntityRenderer living && !(living instanceof PlayerRenderer)
                    && living.getModel() instanceof HumanoidModel) {
                living.addLayer(new ArmorExtrasLayer(living, event.getEntityModels()));
            }
        }
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SHARD_SPARKLE.get(), ShardParticle.SparkleProvider::new);
        event.registerSpriteSet(ModParticles.VOID_MOTE.get(), ShardParticle.VoidProvider::new);
        event.registerSpriteSet(ModParticles.VIRUS_SPORE.get(), ShardParticle.SporeProvider::new);
        event.registerSpriteSet(ModParticles.PURITY_MOTE.get(), ShardParticle.PurityProvider::new);
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.CRYSTAL_SHIELD);
        event.register(KeyBindings.SHARD_CALL);
        event.register(KeyBindings.CRYSTAL_WAVE);
    }

    @SubscribeEvent
    static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ModDimensions.SPLITTERWEITE_EFFECTS, new SplitterweiteEffects());
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, Shardline.id("shard_portal_overlay"), PortalOverlay::render);
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, Shardline.id("story_effects"), StoryScreenEffects::render);
        event.registerAbove(VanillaGuiLayers.TITLE, Shardline.id("chapter_card"), ChapterCardOverlay::render);
    }
}
