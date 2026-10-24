package eu.shardline.mod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Partikel der Mod: funkelnde Kristallsplitter (leuchtend) und dunkle Void-Partikel.
 */
public class ShardParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean glowing;

    protected ShardParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, boolean glowing) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.sprites = sprites;
        this.glowing = glowing;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.friction = 0.92F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 20 + this.random.nextInt(20);
        this.quadSize *= glowing ? 0.55F : 0.8F;
        if (!glowing) {
            float shade = 0.15F + this.random.nextFloat() * 0.2F;
            this.setColor(shade * 0.8F, shade * 0.5F, shade * 1.6F);
        }
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = 1.0F - Mth.clamp((float) this.age / this.lifetime, 0.0F, 1.0F) * 0.8F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return this.glowing ? 0xF000F0 : super.getLightColor(partialTick);
    }

    public static class SparkleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SparkleProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new ShardParticle(level, x, y, z, dx, dy, dz, this.sprites, true);
        }
    }

    /** Virussporen: leuchtend violett, steigen langsam auf. */
    public static class SporeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SporeProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            ShardParticle particle = new ShardParticle(level, x, y, z, dx, dy, dz, this.sprites, true);
            float shade = 0.8F + particle.random.nextFloat() * 0.2F;
            particle.setColor(0.72F * shade, 0.32F * shade, 1.0F * shade);
            particle.setLifetime(40 + particle.random.nextInt(30));
            particle.quadSize *= 0.7F;
            return particle;
        }
    }

    /** Goldene Funken der Reinheitszone: steigen langsam auf, leuchten im Dunkeln. */
    public static class PurityProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PurityProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            ShardParticle particle = new ShardParticle(level, x, y, z, dx, dy + 0.012, dz, this.sprites, true);
            particle.setLifetime(30 + particle.random.nextInt(20));
            particle.quadSize *= 0.8F;
            return particle;
        }
    }

    public static class VoidProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public VoidProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new ShardParticle(level, x, y, z, dx, dy, dz, this.sprites, false);
        }
    }
}
