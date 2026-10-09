package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);

    interface BufferSource extends MultiBufferSource {
        void endBatch();

        void endBatch(RenderType renderType);

        default void endLastBatch() {
            endBatch();
        }
    }
}
