package com.atsuishio.portlib.gui;

import com.atsuishio.portlib.render.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.SubmitNodeCollector;

public class Legacy3DRenderer extends PictureInPictureRenderer<Legacy3DRenderState> {
    private static final float DEPTH_SCALE = 0.25F;

    @Override
    public Class<Legacy3DRenderState> getRenderStateClass() {
        return Legacy3DRenderState.class;
    }

    @Override
    protected void renderToTexture(Legacy3DRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        poseStack.pushPose();
        poseStack.translate(-state.x0() - (state.x1() - state.x0()) / 2.0F, state.y0(), 0.0F);
        poseStack.scale(1.0F, -1.0F, DEPTH_SCALE);
        SubmitBufferSource buffers = new SubmitBufferSource(submitNodeCollector);
        try {
            state.callback().render(poseStack, buffers);
        } finally {
            buffers.flush();
            poseStack.popPose();
        }
    }

    @Override
    protected String getTextureLabel() {
        return "sbw_portlib legacy 3d";
    }
}
