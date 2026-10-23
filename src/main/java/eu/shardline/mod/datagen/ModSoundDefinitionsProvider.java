package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * Erzeugt assets/shardline/sounds.json. Jedes Event verweist vorerst auf ein passendes Vanilla-Event;
 * eigene Sounds koennen spaeter als .ogg unter assets/shardline/sounds/ abgelegt und hier eingetragen werden.
 */
public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, Shardline.MODID, fileHelper);
    }

    @Override
    public void registerSounds() {
        ModSounds.PLACEHOLDERS.forEach((holder, placeholder) -> {
            String path = holder.getId().getPath();
            SoundDefinition definition = definition()
                    .with(sound(ResourceLocation.withDefaultNamespace(placeholder), SoundDefinition.SoundType.EVENT));
            if (!path.startsWith("music.")) {
                definition.subtitle("subtitles.shardline." + path);
            }
            add(holder, definition);
        });
    }
}
