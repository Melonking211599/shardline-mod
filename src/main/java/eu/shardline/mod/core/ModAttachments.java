package eu.shardline.mod.core;

import com.mojang.serialization.Codec;
import eu.shardline.mod.Shardline;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Spielerdaten, die gespeichert und beim Tod uebernommen werden.
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Shardline.MODID);

    /** Spielzeit (Game-Time), ab der der Kristallschild wieder benutzt werden darf. */
    public static final Supplier<AttachmentType<Long>> CRYSTAL_SHIELD_READY_AT = ATTACHMENT_TYPES.register("crystal_shield_ready_at",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());
    /** Splitterherrscher: Spielzeit, ab der der Splitterruf wieder bereit ist. */
    public static final Supplier<AttachmentType<Long>> SHARD_CALL_READY_AT = ATTACHMENT_TYPES.register("shard_call_ready_at",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());
    /** Splitterherrscher: Spielzeit, ab der die Kristallwelle wieder bereit ist. */
    public static final Supplier<AttachmentType<Long>> CRYSTAL_WAVE_READY_AT = ATTACHMENT_TYPES.register("crystal_wave_ready_at",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());

    private ModAttachments() {
    }
}
