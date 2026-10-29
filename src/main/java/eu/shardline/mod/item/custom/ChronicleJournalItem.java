package eu.shardline.mod.item.custom;

import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.quest.QuestSync;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Chronik-Tagebuch: oeffnet das Auftragsbuch (Kapitel, Auftraege, Figuren, Lore), wie die Taste J. Rhea gibt es beim
 * ersten Treffen; herstellbar aus Buch und Shard-Splitter.
 */
public class ChronicleJournalItem extends Item {
    public ChronicleJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LORE_BOOK_OPEN.get(), SoundSource.PLAYERS, 0.7F, 1.1F);
            QuestSync.send(serverPlayer, true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shardline.chronicle_journal.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
