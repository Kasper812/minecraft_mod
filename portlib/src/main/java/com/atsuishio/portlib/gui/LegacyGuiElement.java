package com.atsuishio.portlib.gui;

import com.atsuishio.portlib.render.RecordedGeometry;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;

public record LegacyGuiElement(RenderPipeline pipeline, TextureSetup textureSetup, RecordedGeometry geometry,
                               @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    private static final PoseStack.Pose IDENTITY = new PoseStack().last();

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        geometry.render(IDENTITY, vertexConsumer);
    }
}
