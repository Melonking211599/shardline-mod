package eu.shardline.mod.entity.npc;

import eu.shardline.mod.core.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * Rhea Tannwald, Leiterin des Ueberlebendenlagers. Frueher Hauptfrau der Wache von Birkenfurt in der Oberwelt; floh
 * vor dem Virus durch ein Portal. Pragmatisch, muede, beschuetzend, misstraut Magie. Vergibt Auftraege und fuehrt durch
 * die Kapitel 7-9. Im versiegelten Ende: "Lass es ruhen." Im beherrschten Ende: wachsam neutral.
 */
public class RheaEntity extends AftermathNpcEntity {
    public RheaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public String npcId() {
        return "rhea";
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isResting() || this.isTalking() ? null : ModSounds.RHEA_VOICE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RHEA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RHEA_DEATH.get();
    }
}
