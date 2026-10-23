package eu.shardline.mod.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.compat.ShardlineCompatInfo;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * EMI-Integration: Vanilla-Rezepte erkennt EMI selbst, hier kommen die Infoseiten dazu.
 * Wird nur geladen, wenn EMI installiert ist.
 */
@EmiEntrypoint
public class ShardlineEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (ShardlineCompatInfo.Entry entry : ShardlineCompatInfo.ENTRIES) {
            registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(entry.item().get())),
                    List.of(Component.translatable(entry.translationKey())),
                    Shardline.id("/info/" + entry.name())));
        }
    }
}
