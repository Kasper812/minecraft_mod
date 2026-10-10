package com.atsuishio.portlib.render.clip;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.List;

public final class PositionCapture implements MultiBufferSource.BufferSource, VertexConsumer {
    private final Matrix4fc viewProjection;
    private final List<float[]> points = new ArrayList<>();

    public PositionCapture(Matrix4fc viewProjection) {
        this.viewProjection = viewProjection;
    }

    public List<float[]> points() {
        return this.points;
    }

    public ScreenClipRegion hull() {
        return ScreenClipRegion.convexHull(this.viewProjection, this.points);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return this;
    }

    @Override
    public void endBatch() {
    }

    @Override
    public void endBatch(RenderType renderType) {
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        float[] ndc = ScreenClipRegion.project(this.viewProjection, x, y, z);
        if (ndc != null) {
            this.points.add(new float[]{ndc[0], ndc[1]});
        }
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
}
