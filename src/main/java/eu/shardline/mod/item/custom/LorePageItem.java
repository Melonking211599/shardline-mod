package eu.shardline.mod.item.custom;

import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.network.OpenLorePagePayload;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.lore.LoreBooks;
import java.util.List;
import javax.annotation.Nullable;
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
 * Lose Lore-Seite: eine herausgerissene Seite eines der zwoelf Lore-Buecher (Komponenten {@code shardline:lore_book}
 * und {@code shardline:lore_page}). Rechtsklick oeffnet das Lesefenster (ohne Mod-Kanal: Text im Chat). Der Splitterhaendler kauft lose Seiten
 * gegen Shard-Splitter und bindet vier Seiten desselben Buchs zum ganzen Buch.
 */
public class LorePageItem extends Item {
    public LorePageItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static LoreBooks.LoreBook book(ItemStack stack) {
        return LoreBooks.byId(stack.get(ModDataComponents.LORE_BOOK.get()));
    }

    public static int pageNumber(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.LORE_PAGE.get(), 1);
    }

    @Override
    public Component getName(ItemStack stack) {
        LoreBooks.LoreBook book = book(stack);
        return book != null ? Component.translatable("item.shardline.lore_page.named", book.title()) : super.getName(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        LoreBooks.LoreBook book = book(stack);
        if (book == null) {
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            int page = Math.min(pageNumber(stack), book.pages());
            // Auftragsschritt "read_lore": jede Seite zaehlt nur einmal (Ziel "any" oder die Buch-ID)
            var data = StoryManager.data(serverPlayer.server);
            String readFlag = "lore_read:" + book.id() + ":" + page;
            if (!data.hasFlag(serverPlayer.getUUID(), readFlag)) {
                data.addFlag(serverPlayer.getUUID(), readFlag);
                eu.shardline.mod.story.quest.Quests.trigger(serverPlayer, "read_lore",
                        target -> target.equals("any") || target.equals(book.id()), 1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LORE_BOOK_OPEN.get(), SoundSource.PLAYERS, 0.7F, 1.3F);
            player.getCooldowns().addCooldown(this, 10);
            if (serverPlayer.connection != null && serverPlayer.connection.hasChannel(OpenLorePagePayload.TYPE)) {
                StoryManager.send(serverPlayer, new OpenLorePagePayload(book.id(), page));
                return InteractionResultHolder.consume(stack);
            }
            // Fallback ohne Kanal: Text im Chat
            player.sendSystemMessage(Component.translatable("item.shardline.lore_page.header", book.title(), page, book.pages())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            player.sendSystemMessage(book.page(page).copy().withStyle(ChatFormatting.GRAY));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LoreBooks.LoreBook book = book(stack);
        if (book != null) {
            tooltip.add(Component.translatable("item.shardline.lore_page.page", pageNumber(stack), book.pages()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.shardline.lore_page.tooltip").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
