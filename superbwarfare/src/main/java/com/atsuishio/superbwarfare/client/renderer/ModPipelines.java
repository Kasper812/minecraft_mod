package com.atsuishio.superbwarfare.client.renderer;

import com.atsuishio.portlib.render.SbmPipelines;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class ModPipelines {
    private static final DepthStencilState NO_DEPTH_WRITE = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);
    private static final BlendFunction MUZZLE_FLASH_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ONE);

    public static final RenderPipeline LASER = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(id("pipeline/laser"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .withDepthStencilState(NO_DEPTH_WRITE)
                    .withCull(false)
                    .build(),
            "ENTITIES_TRANSLUCENT"
    );

    public static final RenderPipeline LASER_BEAM = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation(id("pipeline/laser_beam"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
                    .withCull(false)
                    .build(),
            "ENTITIES_TRANSLUCENT"
    );

    public static final RenderPipeline ILLUMINATED = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.BEACON_BEAM_SNIPPET)
                    .withLocation(id("pipeline/illuminated"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.GLINT))
                    .withDepthStencilState(NO_DEPTH_WRITE)
                    .withCull(false)
                    .build(),
            "BEACON_BEAM"
    );

    public static final RenderPipeline POLY_MESH_TRANSLUCENT_EMISSIVE = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(id("pipeline/poly_mesh_translucent_emissive"))
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withShaderDefine("EMISSIVE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withDepthStencilState(NO_DEPTH_WRITE)
                    .withCull(false)
                    .build(),
            "EMISSIVE_ENTITIES"
    );

    public static final RenderPipeline POLY_MESH_EYES = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(id("pipeline/poly_mesh_eyes"))
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withShaderDefine("EMISSIVE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withDepthStencilState(NO_DEPTH_WRITE)
                    .withCull(false)
                    .build(),
            "EMISSIVE_ENTITIES"
    );

    public static final RenderPipeline MUZZLE_FLASH = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    .withLocation(id("pipeline/muzzle_flash"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(MUZZLE_FLASH_BLEND))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(NO_DEPTH_WRITE)
                    .build(),
            "TEXTURED"
    );

    public static final RenderPipeline TOW_CHAIN = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    .withLocation(id("pipeline/tow_chain"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withCull(false)
                    .build(),
            "TEXTURED"
    );

    public static final RenderPipeline BLOCK_OVERLAY = SbmPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    .withLocation(id("pipeline/block_overlay"))
                    .withVertexShader("core/position_color")
                    .withFragmentShader("core/position_color")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(NO_DEPTH_WRITE)
                    .withCull(false)
                    .build(),
            "BASIC"
    );

    private ModPipelines() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("superbwarfare", path);
    }

    public static void init() {
    }
}
