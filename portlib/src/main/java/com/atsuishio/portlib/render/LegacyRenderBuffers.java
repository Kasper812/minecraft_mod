package com.atsuishio.portlib.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class LegacyRenderBuffers {
    public static final LegacyRenderBuffers INSTANCE = new LegacyRenderBuffers();

    private static final MultiBufferSource.BufferSource DETACHED = new MultiBufferSource.BufferSource() {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new RecordedGeometry();
        }

        @Override
        public void endBatch() {
        }

        @Override
        public void endBatch(RenderType renderType) {
        }
    };

    private LegacyRenderBuffers() {
    }

    public MultiBufferSource.BufferSource bufferSource() {
        SubmitBufferSource current = SubmitBufferSource.current();
        return current != null ? current : DETACHED;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return bufferSource();
    }
}
