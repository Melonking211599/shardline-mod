package eu.shardline.mod.compat.jei;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.compat.ShardlineCompatInfo;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI-Integration. Alle Rezepte der Mod sind Vanilla-Rezepttypen (Werkbank, Ofen, Schmiedetisch,
 * Steinsaege) und erscheinen automatisch; zusaetzlich gibt es Infoseiten fuer Sondermechaniken.
 * Wird nur geladen, wenn JEI installiert ist.
 */
@JeiPlugin
public class ShardlineJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = Shardline.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (ShardlineCompatInfo.Entry entry : ShardlineCompatInfo.ENTRIES) {
            registration.addIngredientInfo(new ItemStack(entry.item().get()), VanillaTypes.ITEM_STACK,
                    Component.translatable(entry.translationKey()));
        }
    }
}
