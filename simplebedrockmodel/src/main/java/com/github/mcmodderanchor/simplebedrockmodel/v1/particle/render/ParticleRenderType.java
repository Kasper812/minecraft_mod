package com.github.mcmodderanchor.simplebedrockmodel.v1.particle.render;

import com.atsuishio.portlib.render.SbmRenderTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class ParticleRenderType {
    private ParticleRenderType() {
    }

    public static RenderType additiveParticle(Identifier texture) {
        return SbmRenderTypes.entityTranslucentAdditive(texture);
    }
}
