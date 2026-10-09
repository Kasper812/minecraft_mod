package cn.sh1rocu.simplebedrockmodel.client.port;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class SbmPipelines {
    public static final DepthStencilState DEPTH_TEST_NO_WRITE = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);

    public static final RenderPipeline ENTITY_CUTOUT_TRIANGLES = register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(id("pipeline/entity_cutout_triangles"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withCull(false)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .build(),
            "ENTITIES"
    );

    public static final RenderPipeline ENTITY_TRANSLUCENT_ADDITIVE = register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(id("pipeline/entity_translucent_additive"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.LIGHTNING), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
                    .withDepthStencilState(DEPTH_TEST_NO_WRITE)
                    .withCull(false)
                    .build(),
            "ENTITIES_TRANSLUCENT"
    );

    public static final RenderPipeline PARTICLE_OPAQUE = register(
            RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                    .withLocation(id("pipeline/particle_opaque"))
                    .withCull(false)
                    .build(),
            "PARTICLES"
    );

    public static final RenderPipeline PARTICLE_BLEND = register(
            RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                    .withLocation(id("pipeline/particle_blend"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .build(),
            "PARTICLES_TRANSLUCENT"
    );

    public static final RenderPipeline PARTICLE_ADD = register(
            RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                    .withLocation(id("pipeline/particle_add"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withDepthStencilState(DEPTH_TEST_NO_WRITE)
                    .withCull(false)
                    .build(),
            "PARTICLES_TRANSLUCENT"
    );

    private SbmPipelines() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("simplebedrockmodel", path);
    }

    public static RenderPipeline register(RenderPipeline pipeline, String irisProgram) {
        RenderPipelines.register(pipeline);
        return IrisCompat.assign(pipeline, irisProgram);
    }

    public static void init() {
    }
}
