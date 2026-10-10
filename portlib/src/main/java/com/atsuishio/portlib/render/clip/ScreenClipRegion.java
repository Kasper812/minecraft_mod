package com.atsuishio.portlib.render.clip;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public final class ScreenClipRegion {
    private final Matrix4f viewProjection;
    private final float[] edgeA;
    private final float[] edgeB;
    private final float[] edgeC;

    private ScreenClipRegion(Matrix4fc viewProjection, float[] xs, float[] ys) {
        this.viewProjection = new Matrix4f(viewProjection);
        int n = xs.length;
        this.edgeA = new float[n];
        this.edgeB = new float[n];
        this.edgeC = new float[n];
        float area = 0.0F;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += xs[i] * ys[j] - xs[j] * ys[i];
        }
        float sign = area >= 0.0F ? 1.0F : -1.0F;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float ex = xs[j] - xs[i];
            float ey = ys[j] - ys[i];
            this.edgeA[i] = -ey * sign;
            this.edgeB[i] = ex * sign;
            this.edgeC[i] = (ey * xs[i] - ex * ys[i]) * sign;
        }
    }

    public static ScreenClipRegion ellipse(Matrix4fc viewProjection, float centerX, float centerY, float radiusX, float radiusY, int segments) {
        float[] xs = new float[segments];
        float[] ys = new float[segments];
        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2.0 / segments;
            xs[i] = centerX + (float) Math.cos(angle) * radiusX;
            ys[i] = centerY + (float) Math.sin(angle) * radiusY;
        }
        return new ScreenClipRegion(viewProjection, xs, ys);
    }

    public static ScreenClipRegion convexHull(Matrix4fc viewProjection, List<float[]> ndcPoints) {
        if (ndcPoints.size() < 3) {
            return null;
        }
        List<float[]> pts = new ArrayList<>(ndcPoints);
        pts.sort((p, q) -> p[0] != q[0] ? Float.compare(p[0], q[0]) : Float.compare(p[1], q[1]));
        List<float[]> hull = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            int start = hull.size();
            for (float[] p : pts) {
                while (hull.size() >= start + 2 && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0.0F) {
                    hull.remove(hull.size() - 1);
                }
                hull.add(p);
            }
            hull.remove(hull.size() - 1);
            java.util.Collections.reverse(pts);
        }
        if (hull.size() < 3) {
            return null;
        }
        float[] xs = new float[hull.size()];
        float[] ys = new float[hull.size()];
        for (int i = 0; i < hull.size(); i++) {
            xs[i] = hull.get(i)[0];
            ys[i] = hull.get(i)[1];
        }
        return new ScreenClipRegion(viewProjection, xs, ys);
    }

    private static float cross(float[] o, float[] a, float[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    public static float[] project(Matrix4fc viewProjection, float x, float y, float z) {
        Vector4f v = viewProjection.transform(new Vector4f(x, y, z, 1.0F));
        if (v.w <= 1.0E-6F) {
            return null;
        }
        return new float[]{v.x / v.w, v.y / v.w, v.w};
    }

    Matrix4f viewProjection() {
        return this.viewProjection;
    }

    int edgeCount() {
        return this.edgeA.length;
    }

    float evaluate(int edge, float x, float y, float w) {
        return this.edgeA[edge] * x + this.edgeB[edge] * y + this.edgeC[edge] * w;
    }
}
