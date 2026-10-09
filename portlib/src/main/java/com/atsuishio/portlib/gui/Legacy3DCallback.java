package com.atsuishio.portlib.gui;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface Legacy3DCallback {
    void render(PoseStack poseStack, MultiBufferSource bufferSource);
}
