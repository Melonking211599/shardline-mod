package eu.shardline.mod.entity.infected;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.OwnableEntity;

/**
 * Vom Shardvirus befallene Kreatur. Splitterherrscher koennen sie mit einem Shard-Splitter
 * zaehmen; der Splitterruf kann zusaetzlich zeitlich begrenzte Diener erschaffen.
 */
public interface InfectedMob extends OwnableEntity {
    void setOwnerUUID(@Nullable UUID owner);

    default boolean isTamed() {
        return this.getOwnerUUID() != null;
    }

    /**
     * Ob das Wesen einen Besitzer hat. Anders als {@link #isTamed()} auch auf dem Client gueltig
     * (synchronisiert); der Renderer zeichnet dann den Kragen.
     */
    boolean hasCollar();

    /** Verbleibende Lebenszeit eines beschworenen Dieners in Ticks (0 = dauerhaft). */
    int getSummonTicks();

    void setSummonTicks(int ticks);
}
