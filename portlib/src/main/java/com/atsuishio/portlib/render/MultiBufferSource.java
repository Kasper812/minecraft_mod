package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);
}
