package eu.shardline.mod.entity.boss;

import eu.shardline.mod.config.ShardlineConfig;
import eu.shardline.mod.core.ModSounds;
import eu.shardline.mod.story.Encounters;
import eu.shardline.mod.story.StoryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Das Schwaerenherz (Kapitel 9, beherrschte Welt): ein Virusherz, das gegen seinen Herrscher singt. Kaempft wie das
 * Virusherz (drei Phasen), greift aber auch Splitterherrscher an und schwaecht sie alle vier Sekunden mit dem
 * Gegengesang (Schwaeche, Langsamkeit, magischer Schaden). Leben: {@code aftermath.festeringHeartHealth}. Der Sieg
 * zaehlt nicht fuer den Kern des Bruchs; danach entscheidet der Spieler bei Ilsa ("Waffenstillstand" oder
 * "Alleinherrschaft").
 */
public class FesteringHeartEntity extends VirusHeartEntity {
    private static final double SONG_RADIUS = 20.0;

    public FesteringHeartEntity(EntityType<? extends VirusHeartEntity> type, Level level) {
        super(type, level);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ShardlineConfig.FESTERING_HEART_HEALTH.get());
        this.setHealth(this.getMaxHealth());
        this.bossEvent().setName(Component.translatable("entity.shardline.festering_heart"));
        this.bossEvent().setColor(BossEvent.BossBarColor.YELLOW);
    }

    /** Wie das Virusherz, aber Herrscher sind nicht sicher. */
    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, target -> true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 80 == 0 && this.isAlive()) {
            this.counterSong((ServerLevel) this.level());
        }
    }

    /** Gegengesang: trifft nur Splitterherrscher im Umkreis (fuer alle anderen ist das Herz ein gewoehnliches Virusherz). */
    private void counterSong(ServerLevel level) {
        boolean sang = false;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(this.blockPosition()).inflate(SONG_RADIUS),
                player -> !player.isCreative() && !player.isSpectator() && StoryManager.isRuler(player))) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            player.hurt(this.damageSources().indirectMagic(this, this), 2.0F);
            player.displayClientMessage(Component.translatable("message.shardline.counter_song").withStyle(ChatFormatting.GOLD), true);
            sang = true;
        }
        if (sang) {
            level.playSound(null, this.blockPosition(), ModSounds.FESTERING_NODE_SCREAM.get(), SoundSource.HOSTILE, 1.4F, 1.3F);
        }
    }

    @Override
    protected void onDefeated(ServerLevel level, DamageSource source) {
        Encounters.onBossDefeated(level, this, source.getEntity() instanceof ServerPlayer player ? player : null, Encounters.FESTERING_HEART);
    }
}
