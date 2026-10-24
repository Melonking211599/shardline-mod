package eu.shardline.mod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-Konfiguration (config/shardline-client.toml). Nur Darstellung, keine Spielmechanik.
 */
public final class ShardlineClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DENSE_FOG;
    public static final ModConfigSpec.IntValue FOG_DISTANCE;
    public static final ModConfigSpec.BooleanValue PORTAL_OVERLAY;
    public static final ModConfigSpec.BooleanValue INFECTION_FOG;
    public static final ModConfigSpec.BooleanValue INFECTION_MUSIC;
    public static final ModConfigSpec.BooleanValue INFECTED_SKY;
    public static final ModConfigSpec.BooleanValue SCREEN_SHAKE;
    public static final ModConfigSpec.BooleanValue FEVER_DISTORTION;
    public static final ModConfigSpec.BooleanValue CHAPTER_CARD;
    public static final ModConfigSpec.BooleanValue DIALOGUE_TYPEWRITER;
    public static final ModConfigSpec.IntValue DIALOGUE_TEXT_SPEED;
    public static final ModConfigSpec.BooleanValue DIALOGUE_TYPE_SOUND;
    public static final ModConfigSpec.BooleanValue DIALOGUE_AUTO_ADVANCE;
    public static final ModConfigSpec.IntValue DIALOGUE_AUTO_ADVANCE_SECONDS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        DENSE_FOG = b.comment("Dichter Nebel in der Splitterweite.")
                .translation("shardline.configuration.dense_fog")
                .define("denseFog", true);
        FOG_DISTANCE = b.comment("Sichtweite des Nebels in der Splitterweite (Bloecke).")
                .translation("shardline.configuration.fog_distance")
                .defineInRange("fogDistance", 96, 16, 512);
        PORTAL_OVERLAY = b.comment("Kristall-Overlay beim Stehen im Splitterportal anzeigen.")
                .translation("shardline.configuration.portal_overlay")
                .define("portalOverlay", true);
        INFECTION_FOG = b.comment("Violetter Nebel und Sporen-Partikel in infizierten Gebieten.")
                .translation("shardline.configuration.infection_fog")
                .define("infectionFog", true);
        INFECTION_MUSIC = b.comment("Eigene Musik in infizierten Gebieten.")
                .translation("shardline.configuration.infection_music")
                .define("infectionMusic", true);
        INFECTED_SKY = b.comment("Violetter Himmel in der Oberwelt, solange der Virus aktiv ist.")
                .translation("shardline.configuration.infected_sky")
                .define("infectedSky", true);
        SCREEN_SHAKE = b.comment("Kamerawackeln bei Story-Ereignissen (Virusausbruch, Versiegelung).")
                .translation("shardline.configuration.screen_shake")
                .define("screenShake", true);
        FEVER_DISTORTION = b.comment("Verzerrte Sicht durch Splitterfieber.")
                .translation("shardline.configuration.fever_distortion")
                .define("feverDistortion", true);
        CHAPTER_CARD = b.comment("Kapitelkarte (Emblem, Titel, Zusammenfassung) beim Kapitelwechsel; sonst die schlichten Vanilla-Titel.")
                .translation("shardline.configuration.chapter_card")
                .define("chapterCard", true);
        b.translation("shardline.configuration.dialogue").push("dialogue");
        DIALOGUE_TYPEWRITER = b.comment("Dialogtext erscheint Buchstabe fuer Buchstabe (aus: sofort ganz).")
                .translation("shardline.configuration.dialogue_typewriter")
                .define("typewriter", true);
        DIALOGUE_TEXT_SPEED = b.comment("Geschwindigkeit des Schreibmaschinen-Effekts in Zeichen pro Sekunde.")
                .translation("shardline.configuration.dialogue_text_speed")
                .defineInRange("textSpeed", 45, 5, 400);
        DIALOGUE_TYPE_SOUND = b.comment("Leises Tippgeraeusch beim Schreibmaschinen-Effekt.")
                .translation("shardline.configuration.dialogue_type_sound")
                .define("typeSound", true);
        DIALOGUE_AUTO_ADVANCE = b.comment("Seiten ohne Antworten blaettern nach einer Pause von selbst weiter.")
                .translation("shardline.configuration.dialogue_auto_advance")
                .define("autoAdvance", false);
        DIALOGUE_AUTO_ADVANCE_SECONDS = b.comment("Pause vor dem automatischen Weiterblaettern in Sekunden.")
                .translation("shardline.configuration.dialogue_auto_advance_seconds")
                .defineInRange("autoAdvanceSeconds", 4, 1, 30);
        b.pop();
        SPEC = b.build();
    }

    private ShardlineClientConfig() {
    }
}
