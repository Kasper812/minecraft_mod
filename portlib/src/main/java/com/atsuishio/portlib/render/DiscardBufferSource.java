package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class DiscardBufferSource implements MultiBufferSource.BufferSource {
    public static final DiscardBufferSource INSTANCE = new DiscardBufferSource();

    private static final VertexConsumer SINK = new VertexConsumer() {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    };

    private DiscardBufferSource() {
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return SINK;
    }

    @Override
    public void endBatch() {
    }

    @Override
    public void endBatch(RenderType renderType) {
    }
}
