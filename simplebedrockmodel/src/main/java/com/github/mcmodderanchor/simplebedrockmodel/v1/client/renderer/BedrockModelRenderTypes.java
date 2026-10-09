package com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer;

import com.atsuishio.portlib.render.SbmPipelines;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class BedrockModelRenderTypes {
    private static final Function<Identifier, RenderType> POLY_MESH_CUTOUT = Util.memoize(BedrockModelRenderTypes::createPolyMeshCutout);

    private BedrockModelRenderTypes() {
    }

    public static RenderType polyMeshCutout(Identifier texture) {
        return POLY_MESH_CUTOUT.apply(texture);
    }

    private static RenderType createPolyMeshCutout(Identifier texture) {
        RenderSetup state = RenderSetup.builder(SbmPipelines.ENTITY_CUTOUT_TRIANGLES)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup();
        return RenderType.create("bedrock_poly_mesh_cutout", state);
    }
}
