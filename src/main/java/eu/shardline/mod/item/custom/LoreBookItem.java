package eu.shardline.mod.item.custom;

import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.StoryEvents;
import eu.shardline.mod.story.lore.LoreBooks;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

/**
 * Schreibgeschuetztes Lore-Buch. Welches der zwoelf Buecher es ist, steht in der Komponente
 * {@code shardline:lore_book}; der Text wird beim Lesen aus den Sprachdateien aufgebaut.
 */
public class LoreBookItem extends Item {
    public LoreBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        LoreBooks.LoreBook book = LoreBooks.byId(stack.get(ModDataComponents.LORE_BOOK.get()));
        return book != null ? book.title() : super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity.tickCount % 20 == 0) {
            refreshContent(stack);
        }
    }

    /** Legt den Buchtext an bzw. aktualisiert ihn. @return true, wenn sich etwas geaendert hat */
    private static boolean refreshContent(ItemStack stack) {
        LoreBooks.LoreBook book = LoreBooks.byId(stack.get(ModDataComponents.LORE_BOOK.get()));
        if (book == null) {
            return false;
        }
        WrittenBookContent content = LoreBooks.content(book);
        if (Objects.equals(stack.get(DataComponents.WRITTEN_BOOK_CONTENT), content)) {
            return false;
        }
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        LoreBooks.LoreBook book = LoreBooks.byId(stack.get(ModDataComponents.LORE_BOOK.get()));
        if (book == null) {
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (refreshContent(stack)) {
                serverPlayer.containerMenu.broadcastChanges();
            }
            serverPlayer.openItemGui(stack, hand);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LORE_BOOK_OPEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            StoryEvents.onLoreBookRead(serverPlayer, book);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LoreBooks.LoreBook book = LoreBooks.byId(stack.get(ModDataComponents.LORE_BOOK.get()));
        if (book == null) {
            tooltip.add(Component.translatable("item.shardline.lore_book.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable("item.shardline.lore_book.author", book.author()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.shardline.lore_book.number", book.number(), LoreBooks.ALL.size())
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
