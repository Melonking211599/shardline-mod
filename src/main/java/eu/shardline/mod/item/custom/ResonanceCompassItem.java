package eu.shardline.mod.item.custom;

import com.mojang.datafixers.util.Pair;
import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.world.dimension.ModDimensions;
import eu.shardline.mod.world.structure.ModStructures;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Resonanzkompass: Rechtsklick sucht das naechste Bauwerk des gewaehlten Typs und zeigt Richtung und Entfernung in der
 * Aktionsleiste; eine Lichtspur weist kurz den Weg. Schleichen + Rechtsklick wechselt das Ziel. In der Splitterweite
 * stehen alle Bauwerke der Weite zur Wahl, in der Oberwelt der Splitterkrater. Abklingzeit 3 Sekunden.
 */
public class ResonanceCompassItem extends Item {
    private static final int SEARCH_RADIUS_CHUNKS = 64;
    private static final int COOLDOWN = 60;

    private static final List<ResourceKey<Structure>> EXPANSE_TARGETS = List.of(
            ModStructures.RUIN_TOWER_KEY, ModStructures.KEEPER_SHRINE_KEY, ModStructures.TRADER_OUTPOST_KEY,
            ModStructures.ASH_CRYPT_KEY, ModStructures.CRYSTAL_GEODE_KEY, ModStructures.KEEPER_RUIN_KEY, ModStructures.COLOSSUS_ARENA_KEY,
            ModStructures.QUEEN_ARENA_KEY);
    private static final List<ResourceKey<Structure>> OVERWORLD_TARGETS = List.of(ModStructures.SHARD_CRATER_KEY);

    public ResonanceCompassItem(Properties properties) {
        super(properties);
    }

    private static List<ResourceKey<Structure>> targets(Level level) {
        if (level.dimension() == ModDimensions.SPLITTERWEITE_LEVEL) {
            return EXPANSE_TARGETS;
        }
        return level.dimension() == Level.OVERWORLD ? OVERWORLD_TARGETS : List.of();
    }

    private static int index(ItemStack stack, int size) {
        int raw = stack.getOrDefault(ModDataComponents.RESONANCE_TARGET.get(), 0);
        return size == 0 ? 0 : Math.floorMod(raw, size);
    }

    public static Component structureName(ResourceKey<Structure> key) {
        return Component.translatable("structure." + key.location().getNamespace() + "." + key.location().getPath());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        List<ResourceKey<Structure>> targets = targets(level);
        if (targets.isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable("item.shardline.resonance_compass.silent")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        int idx = index(stack, targets.size());
        if (player.isShiftKeyDown()) {
            idx = (idx + 1) % targets.size();
            stack.set(ModDataComponents.RESONANCE_TARGET.get(), idx);
            serverPlayer.displayClientMessage(Component.translatable("item.shardline.resonance_compass.target",
                    structureName(targets.get(idx)).copy().withStyle(ChatFormatting.LIGHT_PURPLE)), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.8F, 1.4F);
            return InteractionResultHolder.success(stack);
        }

        ResourceKey<Structure> key = targets.get(idx);
        player.getCooldowns().addCooldown(this, COOLDOWN);
        Holder<Structure> holder = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(key).orElse(null);
        Pair<BlockPos, Holder<Structure>> found = holder == null ? null : serverLevel.getChunkSource().getGenerator()
                .findNearestMapStructure(serverLevel, HolderSet.direct(holder), player.blockPosition(), SEARCH_RADIUS_CHUNKS, false);
        if (found == null) {
            serverPlayer.displayClientMessage(Component.translatable("item.shardline.resonance_compass.none",
                    structureName(key)).withStyle(ChatFormatting.GRAY), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.6F, 0.6F);
            return InteractionResultHolder.consume(stack);
        }

        // Gemeldet wird die Chunk-Ecke; die Bauwerke stehen in der Chunk-Mitte.
        BlockPos target = found.getFirst().offset(8, 0, 8);
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        int distance = Mth.floor(Math.sqrt(dx * dx + dz * dz));
        serverPlayer.displayClientMessage(Component.translatable("item.shardline.resonance_compass.found",
                structureName(key).copy().withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.translatable("item.shardline.resonance_compass.dir." + direction(dx, dz)).withStyle(ChatFormatting.AQUA),
                distance), true);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Lichtspur Richtung Ziel (nur fuer diesen Spieler sichtbar)
        double len = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
        for (int i = 1; i <= 12; i++) {
            double t = i * 0.5;
            serverLevel.sendParticles(serverPlayer, ParticleTypes.END_ROD, true, player.getX() + dx / len * t,
                    player.getEyeY() - 0.3, player.getZ() + dz / len * t, 1, 0.0, 0.0, 0.0, 0.0);
        }
        return InteractionResultHolder.consume(stack);
    }

    /** Himmelsrichtung (8 Sektoren); Minecraft: Norden = -Z, Osten = +X. */
    private static String direction(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        int sector = Math.floorMod((int) Math.round(angle / 45.0), 8);
        return new String[] {"n", "ne", "e", "se", "s", "sw", "w", "nw"}[sector];
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.resonance_compass.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.shardline.resonance_compass.tooltip_cycle").withStyle(ChatFormatting.DARK_GRAY));
        Level level = context.level();
        List<ResourceKey<Structure>> list = level == null ? EXPANSE_TARGETS : targets(level);
        if (!list.isEmpty()) {
            tooltip.add(Component.translatable("item.shardline.resonance_compass.current",
                    structureName(list.get(index(stack, list.size())))).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
