package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * Erzeugt assets/shardline/sounds.json. Events mit eigener Datei ({@link ModSounds#FILES}) spielen
 * assets/shardline/sounds/&lt;datei&gt;.ogg, alle anderen verweisen auf ein passendes Vanilla-Event.
 */
public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, Shardline.MODID, fileHelper);
    }

    @Override
    public void registerSounds() {
        ModSounds.PLACEHOLDERS.forEach((holder, placeholder) -> {
            String path = holder.getId().getPath();
            String file = ModSounds.FILES.get(holder);
            SoundDefinition definition = definition().with(file != null
                    ? sound(Shardline.id(file), SoundDefinition.SoundType.SOUND)
                    : sound(ResourceLocation.withDefaultNamespace(placeholder), SoundDefinition.SoundType.EVENT));
            if (!path.startsWith("music.")) {
                definition.subtitle("subtitles.shardline." + path);
            }
            add(holder, definition);
        });
    }
}
