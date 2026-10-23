package eu.shardline.mod.story.lore;

import eu.shardline.mod.core.ModDataComponents;
import eu.shardline.mod.item.ModItems;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Die zwoelf Lore-Buecher. Titel, Autor und Seiten stehen in den Sprachdateien
 * ({@code lore.shardline.<id>.title}, {@code .author}, {@code .page.<n>}), dadurch erscheinen
 * die Buecher in der Sprache des jeweiligen Spielers.
 */
public final class LoreBooks {
    public record LoreBook(int number, String id, int pages) {
        public Component title() {
            return Component.translatable("lore.shardline." + this.id + ".title");
        }

        public Component author() {
            return Component.translatable("lore.shardline." + this.id + ".author");
        }

        public Component page(int page) {
            return Component.translatable("lore.shardline." + this.id + ".page." + page);
        }
    }

    public static final List<LoreBook> ALL = List.of(
            new LoreBook(1, "wanderer_notes", 7),
            new LoreBook(2, "crystal_kingdom", 7),
            new LoreBook(3, "keepers_oath", 7),
            new LoreBook(4, "the_resonance", 7),
            new LoreBook(5, "first_infection", 7),
            new LoreBook(6, "the_colossus", 7),
            new LoreBook(7, "the_last_knights", 7),
            new LoreBook(8, "queen_selvaine", 7),
            new LoreBook(9, "the_seal", 7),
            new LoreBook(10, "the_shattering", 7),
            new LoreBook(11, "traders_ledger", 7),
            new LoreBook(12, "the_breach_core", 7));

    private LoreBooks() {
    }

    @Nullable
    public static LoreBook byId(@Nullable String id) {
        for (LoreBook book : ALL) {
            if (book.id().equals(id)) {
                return book;
            }
        }
        return null;
    }

    /** Buchinhalt als (schreibgeschuetzter) Written-Book-Inhalt. */
    public static WrittenBookContent content(LoreBook book) {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (int page = 1; page <= book.pages(); page++) {
            pages.add(Filterable.passThrough(book.page(page)));
        }
        return new WrittenBookContent(Filterable.passThrough(book.id()), "Shardline", 0, pages, true);
    }

    public static ItemStack create(LoreBook book) {
        ItemStack stack = new ItemStack(ModItems.LORE_BOOK.get());
        stack.set(ModDataComponents.LORE_BOOK.get(), book.id());
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content(book));
        return stack;
    }
}
