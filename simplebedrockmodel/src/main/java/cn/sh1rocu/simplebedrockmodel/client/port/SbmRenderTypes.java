package cn.sh1rocu.simplebedrockmodel.client.port;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class SbmRenderTypes {
    private static final Function<Identifier, RenderType> PARTICLE_OPAQUE = Util.memoize(t -> particle("sbm_particle_opaque", SbmPipelines.PARTICLE_OPAQUE, t, false));
    private static final Function<Identifier, RenderType> PARTICLE_BLEND = Util.memoize(t -> particle("sbm_particle_blend", SbmPipelines.PARTICLE_BLEND, t, true));
    private static final Function<Identifier, RenderType> PARTICLE_ADD = Util.memoize(t -> particle("sbm_particle_add", SbmPipelines.PARTICLE_ADD, t, true));
    private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_ADDITIVE = Util.memoize(t -> RenderType.create(
            "sbm_entity_translucent_additive",
            RenderSetup.builder(SbmPipelines.ENTITY_TRANSLUCENT_ADDITIVE)
                    .withTexture("Sampler0", t)
                    .useLightmap()
                    .useOverlay()
                    .sortOnUpload()
                    .createRenderSetup()));

    private SbmRenderTypes() {
    }

    private static RenderType particle(String name, com.mojang.blaze3d.pipeline.RenderPipeline pipeline, Identifier texture, boolean sort) {
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", texture)
                .useLightmap();
        if (sort) {
            builder.sortOnUpload();
        }
        return RenderType.create(name, builder.createRenderSetup());
    }

    public static RenderType particleOpaque(Identifier texture) {
        return PARTICLE_OPAQUE.apply(texture);
    }

    public static RenderType particleBlend(Identifier texture) {
        return PARTICLE_BLEND.apply(texture);
    }

    public static RenderType particleAdd(Identifier texture) {
        return PARTICLE_ADD.apply(texture);
    }

    public static RenderType entityTranslucentAdditive(Identifier texture) {
        return ENTITY_TRANSLUCENT_ADDITIVE.apply(texture);
    }
}
