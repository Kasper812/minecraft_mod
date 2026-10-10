package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

public final class RecordedGeometry implements VertexConsumer, SubmitNodeCollector.CustomGeometryRenderer {
    private static final int F_STRIDE = 9;
    private static final int I_STRIDE = 4;
    private static final int COLOR = 1;
    private static final int UV0 = 2;
    private static final int UV1 = 4;
    private static final int UV2 = 8;
    private static final int NORMAL = 16;
    private static final int LINE_WIDTH = 32;

    private float[] floats = new float[F_STRIDE * 64];
    private int[] ints = new int[I_STRIDE * 64];
    private int count;
    private int cur = -1;

    public boolean isEmpty() {
        return count == 0;
    }

    public int vertexCount() {
        return count;
    }

    public float[] bounds2d() {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < count; i++) {
            float x = floats[i * F_STRIDE];
            float y = floats[i * F_STRIDE + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    public float x(int i) {
        return floats[i * F_STRIDE];
    }

    public float y(int i) {
        return floats[i * F_STRIDE + 1];
    }

    public float z(int i) {
        return floats[i * F_STRIDE + 2];
    }

    public int color(int i) {
        return (ints[i * I_STRIDE] & COLOR) != 0 ? ints[i * I_STRIDE + 1] : -1;
    }

    public void fillMissingColor(int color) {
        for (int i = 0; i < count; i++) {
            int b = i * I_STRIDE;
            if ((ints[b] & COLOR) == 0) {
                ints[b] |= COLOR;
                ints[b + 1] = color;
            }
        }
    }

    private void ensure() {
        if ((count + 1) * F_STRIDE > floats.length) {
            floats = Arrays.copyOf(floats, floats.length * 2);
            ints = Arrays.copyOf(ints, ints.length * 2);
        }
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        ensure();
        cur = count++;
        int f = cur * F_STRIDE;
        floats[f] = x;
        floats[f + 1] = y;
        floats[f + 2] = z;
        ints[cur * I_STRIDE] = 0;
        return this;
    }

    private int base(int flag) {
        if (cur < 0) {
            throw new IllegalStateException("Not currently building vertex");
        }
        ints[cur * I_STRIDE] |= flag;
        return cur;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int v = base(COLOR);
        ints[v * I_STRIDE + 1] = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        int v = base(COLOR);
        ints[v * I_STRIDE + 1] = color;
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        int i = base(UV0);
        floats[i * F_STRIDE + 3] = u;
        floats[i * F_STRIDE + 4] = v;
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        int i = base(UV1);
        ints[i * I_STRIDE + 2] = (u & 65535) | (v & 65535) << 16;
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        int i = base(UV2);
        ints[i * I_STRIDE + 3] = (u & 65535) | (v & 65535) << 16;
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        int i = base(NORMAL);
        int f = i * F_STRIDE;
        floats[f + 5] = x;
        floats[f + 6] = y;
        floats[f + 7] = z;
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        int i = base(LINE_WIDTH);
        floats[i * F_STRIDE + 8] = width;
        return this;
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer buffer) {
        Matrix4f matrix = pose.pose();
        boolean identity = (matrix.properties() & Matrix4f.PROPERTY_IDENTITY) != 0;
        Vector3f tmp = new Vector3f();
        for (int i = 0; i < count; i++) {
            int f = i * F_STRIDE;
            int b = i * I_STRIDE;
            int mask = ints[b];
            if (identity) {
                buffer.addVertex(floats[f], floats[f + 1], floats[f + 2]);
            } else {
                matrix.transformPosition(floats[f], floats[f + 1], floats[f + 2], tmp);
                buffer.addVertex(tmp.x, tmp.y, tmp.z);
            }
            if ((mask & COLOR) != 0) {
                buffer.setColor(ints[b + 1]);
            } else {
                buffer.setColor(-1);
            }
            if ((mask & UV0) != 0) {
                buffer.setUv(floats[f + 3], floats[f + 4]);
            } else {
                buffer.setUv(0.0F, 0.0F);
            }
            if ((mask & UV1) != 0) {
                int p = ints[b + 2];
                buffer.setUv1(p & 65535, p >>> 16);
            } else {
                buffer.setUv1(0, 10);
            }
            if ((mask & UV2) != 0) {
                int p = ints[b + 3];
                buffer.setUv2(p & 65535, p >>> 16);
            } else {
                buffer.setUv2(240, 240);
            }
            if ((mask & NORMAL) != 0) {
                if (identity) {
                    buffer.setNormal(floats[f + 5], floats[f + 6], floats[f + 7]);
                } else {
                    buffer.setNormal(pose, floats[f + 5], floats[f + 6], floats[f + 7]);
                }
            } else {
                buffer.setNormal(0.0F, 1.0F, 0.0F);
            }
            if ((mask & LINE_WIDTH) != 0) {
                buffer.setLineWidth(floats[f + 8]);
            } else {
                buffer.setLineWidth(1.0F);
            }
        }
    }
}
