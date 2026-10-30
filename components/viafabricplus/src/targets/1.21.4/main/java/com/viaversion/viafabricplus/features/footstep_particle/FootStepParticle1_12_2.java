package com.viaversion.viafabricplus.features.footstep_particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public final class FootStepParticle1_12_2 extends SpriteBillboardParticle {

    public static final Identifier ID = Identifier.of("viafabricplus", "footstep");
    public static int RAW_ID;

    static {
        final SimpleParticleType footStepType = FabricParticleTypes.simple(true);

        Registry.register(Registries.PARTICLE_TYPE, ID, footStepType);
        ParticleFactoryRegistry.getInstance().register(footStepType, FootStepParticle1_12_2.Factory::new);

        RAW_ID = Registries.PARTICLE_TYPE.getRawId(footStepType);
    }

    private FootStepParticle1_12_2(ClientWorld clientWorld, double x, double y, double z) {
        super(clientWorld, x, y, z);

        this.scale = 0.125F;
        this.setMaxAge(200);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        final float strength = ((float) this.age + tickDelta) / (float) this.maxAge;
        this.alpha = 2.0F - (strength * strength) * 2.0F;
        if (this.alpha > 1.0F) {
            this.alpha = 0.2F;
        } else {
            this.alpha *= 0.2F;
        }

        final Vec3d cameraPos = camera.getPos();
        final float x = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.getX());
        final float y = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.getY());
        final float z = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.getZ());

        final float minU = this.getMinU();
        final float maxU = this.getMaxU();
        final float minV = this.getMinV();
        final float maxV = this.getMaxV();

        final int light = this.getBrightness(tickDelta);
        vertexConsumer.vertex(x - scale, y, z + scale).texture(maxU, maxV).color(this.red, this.green, this.blue, this.alpha).light(light);
        vertexConsumer.vertex(x + scale, y, z + scale).texture(maxU, minV).color(this.red, this.green, this.blue, this.alpha).light(light);
        vertexConsumer.vertex(x + scale, y, z - scale).texture(minU, minV).color(this.red, this.green, this.blue, this.alpha).light(light);
        vertexConsumer.vertex(x - scale, y, z - scale).texture(minU, maxV).color(this.red, this.green, this.blue, this.alpha).light(light);
    }

    public static void init() {

    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            if (ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_12_2)) {
                throw new UnsupportedOperationException("FootStepParticle is not supported on versions newer than 1.12.2");
            }

            final FootStepParticle1_12_2 particle = new FootStepParticle1_12_2(world, x, y, z);
            particle.setSprite(this.spriteProvider);
            return particle;
        }

    }

}
