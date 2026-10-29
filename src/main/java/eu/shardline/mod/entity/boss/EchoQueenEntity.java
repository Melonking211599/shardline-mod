package eu.shardline.mod.entity.boss;

import eu.shardline.mod.config.ShardlineConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Der Nachhall (Kapitel 9, versiegelte Welt): das Echo der Splitterkoenigin, das im Archiv der Hueter erwacht. Kaempft
 * wie die Koenigin (Salven, Herabstuerzen, Klone, Leerzonen), ist aber ein durchscheinender Geist mit weniger Leben
 * ({@code aftermath.echoQueenHealth}). Sein Tod loest weder den Ausbruch noch den Entscheidungsaltar aus; danach wartet
 * Veyl auf die Entscheidung des Spielers (Epilog "Stille" oder "Chronik").
 */
public class EchoQueenEntity extends ShardQueenEntity {
    public EchoQueenEntity(EntityType<? extends ShardQueenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 250;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ShardlineConfig.ECHO_QUEEN_HEALTH.get());
        this.setHealth(this.getMaxHealth());
        this.bossEvent().setName(Component.translatable("entity.shardline.echo_queen"));
        this.bossEvent().setColor(BossEvent.BossBarColor.BLUE);
    }
}
