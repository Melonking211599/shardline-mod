```java
package eu.shardline.mod.datagen;

import eu.shardline.mod.Shardline;
import eu.shardline.mod.core.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * Erzeugt assets/shardline/sounds.json.
 * Eigene Musikdateien werden aus assets/shardline/sounds/ geladen.
 * Alle anderen Sounds verwenden passende Vanilla-Events.
 */
public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public ModSoundDefinitionsProvider(
            PackOutput output,
            ExistingFileHelper fileHelper
    ) {
        super(output, Shardline.MODID, fileHelper);
    }

    @Override
    public void registerSounds() {
        ModSounds.PLACEHOLDERS.forEach((holder, placeholder) -> {
            String path = holder.getId().getPath();
            String file = ModSounds.FILES.get(holder);

            // Eigene Musikdateien, falls sie nicht bereits in FILES stehen.
            if (file == null) {
                if (path.equals("music.splitterweite")) {
                    file = "music/splitterweite";
                } else if (path.equals("music.infected")) {
                    file = "music/infected";
                }
            }

            SoundDefinition definition;

            if (file != null) {
                // Eigene Datei unter assets/shardline/sounds/<file>.ogg
                definition = definition()
                        .with(sound(
                                Shardline.id(file),
                                SoundDefinition.SoundType.SOUND
                        ))
                        .subtitleIfAbsent(path);
            } else {
                // Vanilla-Sound als Platzhalter für andere Sound-Events
                definition = definition()
                        .with(sound(
                                ResourceLocation.withDefaultNamespace(placeholder),
                                SoundDefinition.SoundType.EVENT
                        ));

                if (!path.startsWith("music.")) {
                    definition.subtitle("subtitles.shardline." + path);
                }
            }

            add(holder, definition);
        });
    }
}
```
