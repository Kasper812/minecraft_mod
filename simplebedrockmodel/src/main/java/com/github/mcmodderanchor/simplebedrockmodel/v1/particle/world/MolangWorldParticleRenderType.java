package com.github.mcmodderanchor.simplebedrockmodel.v1.particle.world;

import com.atsuishio.portlib.render.SbmRenderTypes;
import com.github.mcmodderanchor.simplebedrockmodel.v1.particle.data.ParticleDescription;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * 世界粒子的自定义渲染类型，按材质+纹理选择对应的管线。
 */
@Environment(EnvType.CLIENT)
public final class MolangWorldParticleRenderType {
    private MolangWorldParticleRenderType() {
    }

    public static RenderType get(ParticleDescription.Material material, Identifier texture) {
        return switch (material) {
            case PARTICLES_OPAQUE -> SbmRenderTypes.particleOpaque(texture);
            case PARTICLES_ALPHA, PARTICLES_BLEND -> SbmRenderTypes.particleBlend(texture);
            case PARTICLES_ADD -> SbmRenderTypes.particleAdd(texture);
        };
    }

    public static void clearCache() {
    }
}
