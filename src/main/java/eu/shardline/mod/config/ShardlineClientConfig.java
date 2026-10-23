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
        SPEC = b.build();
    }

    private ShardlineClientConfig() {
    }
}
