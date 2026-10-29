package eu.shardline.mod.entity;

import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Gemeinsame GeckoLib-Animationen. Die Animationsnamen muessen in jeder
 * assets/shardline/animations/entity/&lt;mob&gt;.animation.json vorhanden sein.
 */
public final class ShardlineAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    public static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");

    /** Name des Controllers fuer ausloesbare Einzelanimationen (Angriff, Zauber, ...). */
    public static final String ACTION_CONTROLLER = "action";
    /** Eigener Controller fuer das kurze Zusammenzucken bei Treffern; unterbricht laufende Zauber nicht. */
    public static final String HURT_CONTROLLER = "hurt";

    private ShardlineAnimations() {
    }

    /** Standard-Controller: Laufen, wenn sich das Wesen bewegt, sonst Leerlauf. */
    public static <T extends Entity & GeoAnimatable> AnimationController<T> walkIdle(T entity) {
        return new AnimationController<>(entity, "movement", 4,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE));
    }

    /** Controller fuer Flieger: dauerhaft Flug-Animation. */
    public static <T extends Entity & GeoAnimatable> AnimationController<T> flying(T entity) {
        return new AnimationController<>(entity, "movement", 4, state -> state.setAndContinue(FLY));
    }

    /** Controller fuer den Clip {@code hurt}: spielt ihn auf {@code triggerAnim(HURT_CONTROLLER, "hurt")} neben den anderen Clips. */
    public static <T extends Entity & GeoAnimatable> AnimationController<T> flinch(T entity) {
        AnimationController<T> controller = new AnimationController<>(entity, HURT_CONTROLLER, 1, state -> PlayState.STOP);
        controller.triggerableAnim("hurt", RawAnimation.begin().thenPlay("hurt"));
        return controller;
    }

    /**
     * Controller, der nur auf Befehl vom Server ({@code triggerAnim(ACTION_CONTROLLER, name)}) Animationen abspielt.
     */
    public static <T extends Entity & GeoAnimatable> AnimationController<T> actions(T entity, String... animations) {
        AnimationController<T> controller = new AnimationController<>(entity, ACTION_CONTROLLER, 2, state -> PlayState.STOP);
        for (String name : animations) {
            controller.triggerableAnim(name, RawAnimation.begin().thenPlay(name));
        }
        return controller;
    }
}
