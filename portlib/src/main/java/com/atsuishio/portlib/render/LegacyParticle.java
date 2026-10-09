package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

public abstract class LegacyParticle extends Particle {
    protected LegacyParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    protected LegacyParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za) {
        super(level, x, y, z, xa, ya, za);
    }

    @Override
    public ParticleRenderType getGroup() {
        return LegacyParticleGroup.TYPE;
    }

    public abstract @Nullable RenderType getLegacyRenderType();

    public abstract void render(VertexConsumer buffer, Camera camera, float partialTicks);
}
