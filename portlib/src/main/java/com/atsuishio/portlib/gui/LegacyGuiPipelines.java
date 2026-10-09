package com.atsuishio.portlib.gui;

import com.atsuishio.portlib.render.SbmPipelines;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class LegacyGuiPipelines {
    public static final RenderPipeline GUI_TEXTURED_ADDITIVE = register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(id("pipeline/gui_textured_additive"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build());
    public static final RenderPipeline GUI_ADDITIVE = register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(id("pipeline/gui_additive"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build());
    public static final RenderPipeline GUI_TRIANGLES = register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(id("pipeline/gui_triangles"))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build());
    public static final RenderPipeline GUI_TRIANGLE_STRIP = register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(id("pipeline/gui_triangle_strip"))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
            .build());
    public static final RenderPipeline GUI_TRIANGLE_FAN = register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(id("pipeline/gui_triangle_fan"))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
            .build());
    public static final RenderPipeline GUI_TEXTURED_TRIANGLES = register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(id("pipeline/gui_textured_triangles"))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build());

    private LegacyGuiPipelines() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("sbw_portlib", path);
    }

    private static RenderPipeline register(RenderPipeline pipeline) {
        return SbmPipelines.register(pipeline, "BASIC");
    }

    public static void init() {
    }
}
