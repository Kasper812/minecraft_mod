package com.atsuishio.portlib.render.clip;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

final class ClippingVertexConsumer implements VertexConsumer {
    private static final int HAS_COLOR = 1;
    private static final int HAS_UV = 2;
    private static final int HAS_UV1 = 4;
    private static final int HAS_UV2 = 8;
    private static final int HAS_NORMAL = 16;
    private static final int HAS_WIDTH = 32;

    private final VertexConsumer target;
    private final int primitiveLength;
    private final List<ScreenClipRegion> insideRegions;
    private final List<ScreenClipRegion> outsideRegions;
    private final List<Vertex> primitive = new ArrayList<>(4);
    private Vertex current;

    ClippingVertexConsumer(VertexConsumer target, int primitiveLength, List<ScreenClipRegion> insideRegions, List<ScreenClipRegion> outsideRegions) {
        this.target = target;
        this.primitiveLength = primitiveLength;
        this.insideRegions = insideRegions;
        this.outsideRegions = outsideRegions;
    }

    VertexConsumer target() {
        return this.target;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        commitCurrent();
        this.current = new Vertex();
        this.current.x = x;
        this.current.y = y;
        this.current.z = z;
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        if (this.current != null) {
            this.current.flags |= HAS_COLOR;
            this.current.r = r;
            this.current.g = g;
            this.current.b = b;
            this.current.a = a;
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        if (this.current != null) {
            this.current.flags |= HAS_UV;
            this.current.u = u;
            this.current.v = v;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        if (this.current != null) {
            this.current.flags |= HAS_UV1;
            this.current.u1 = u;
            this.current.v1 = v;
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        if (this.current != null) {
            this.current.flags |= HAS_UV2;
            this.current.u2 = u;
            this.current.v2 = v;
        }
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        if (this.current != null) {
            this.current.flags |= HAS_NORMAL;
            this.current.nx = x;
            this.current.ny = y;
            this.current.nz = z;
        }
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        if (this.current != null) {
            this.current.flags |= HAS_WIDTH;
            this.current.width = width;
        }
        return this;
    }

    void finish() {
        commitCurrent();
        this.primitive.clear();
    }

    private void commitCurrent() {
        if (this.current == null) {
            return;
        }
        this.primitive.add(this.current);
        this.current = null;
        if (this.primitive.size() == this.primitiveLength) {
            processPrimitive(new ArrayList<>(this.primitive));
            this.primitive.clear();
        }
    }

    private void processPrimitive(List<Vertex> prim) {
        Matrix4f viewProjection = (!this.insideRegions.isEmpty() ? this.insideRegions.get(0) : this.outsideRegions.get(0)).viewProjection();
        boolean behind = false;
        for (Vertex vertex : prim) {
            Vector4f clip = viewProjection.transform(new Vector4f(vertex.x, vertex.y, vertex.z, 1.0F));
            vertex.cx = clip.x;
            vertex.cy = clip.y;
            vertex.cw = clip.w;
            if (clip.w < NEAR_W) {
                behind = true;
            }
        }

        boolean modified = false;
        List<Vertex> polygon = prim;
        if (behind) {
            polygon = clipNear(prim);
            if (polygon.size() < 3) {
                return;
            }
            modified = true;
        }
        for (ScreenClipRegion region : this.insideRegions) {
            if (fullyInside(polygon, region)) {
                continue;
            }
            if (fullyOutsideAnyEdge(polygon, region)) {
                return;
            }
            modified = true;
            List<Vertex> clipped = new ArrayList<>(polygon);
            for (int e = 0; e < region.edgeCount() && clipped.size() >= 3; e++) {
                clipped = clipHalfPlane(clipped, region, e, true);
            }
            if (clipped.size() < 3) {
                return;
            }
            polygon = clipped;
        }

        List<List<Vertex>> pieces = new ArrayList<>();
        pieces.add(polygon);
        for (ScreenClipRegion region : this.outsideRegions) {
            List<List<Vertex>> next = new ArrayList<>();
            for (List<Vertex> piece : pieces) {
                if (fullyOutsideAnyEdge(piece, region)) {
                    next.add(piece);
                    continue;
                }
                modified = true;
                List<Vertex> remaining = piece;
                for (int e = 0; e < region.edgeCount() && remaining.size() >= 3; e++) {
                    List<Vertex> outside = clipHalfPlane(remaining, region, e, false);
                    if (outside.size() >= 3) {
                        next.add(outside);
                    }
                    remaining = clipHalfPlane(remaining, region, e, true);
                }
            }
            pieces = next;
        }
        if (!modified) {
            emitOriginal(prim);
            return;
        }
        for (List<Vertex> piece : pieces) {
            emitPolygon(piece);
        }
    }

    private static final float NEAR_W = 1.0E-4F;

    private static List<Vertex> clipNear(List<Vertex> polygon) {
        List<Vertex> out = new ArrayList<>(polygon.size() + 2);
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Vertex a = polygon.get(i);
            Vertex b = polygon.get((i + 1) % n);
            float ga = a.cw - NEAR_W;
            float gb = b.cw - NEAR_W;
            boolean ia = ga >= 0.0F;
            boolean ib = gb >= 0.0F;
            if (ia) {
                out.add(a);
            }
            if (ia != ib) {
                out.add(Vertex.lerp(a, b, ga / (ga - gb)));
            }
        }
        return out;
    }

    private static boolean fullyInside(List<Vertex> polygon, ScreenClipRegion region) {
        for (int e = 0; e < region.edgeCount(); e++) {
            for (Vertex vertex : polygon) {
                if (region.evaluate(e, vertex.cx, vertex.cy, vertex.cw) < 0.0F) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean fullyOutsideAnyEdge(List<Vertex> polygon, ScreenClipRegion region) {
        for (int e = 0; e < region.edgeCount(); e++) {
            boolean allOutside = true;
            for (Vertex vertex : polygon) {
                if (region.evaluate(e, vertex.cx, vertex.cy, vertex.cw) >= 0.0F) {
                    allOutside = false;
                    break;
                }
            }
            if (allOutside) {
                return true;
            }
        }
        return false;
    }

    private static List<Vertex> clipHalfPlane(List<Vertex> polygon, ScreenClipRegion region, int edge, boolean keepPositive) {
        List<Vertex> out = new ArrayList<>(polygon.size() + 2);
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Vertex a = polygon.get(i);
            Vertex b = polygon.get((i + 1) % n);
            float ga = region.evaluate(edge, a.cx, a.cy, a.cw);
            float gb = region.evaluate(edge, b.cx, b.cy, b.cw);
            if (!keepPositive) {
                ga = -ga;
                gb = -gb;
            }
            boolean ia = ga >= 0.0F;
            boolean ib = gb >= 0.0F;
            if (ia) {
                out.add(a);
            }
            if (ia != ib) {
                float t = ga / (ga - gb);
                out.add(Vertex.lerp(a, b, t));
            }
        }
        return out;
    }

    private void emitOriginal(List<Vertex> prim) {
        for (Vertex vertex : prim) {
            put(vertex);
        }
    }

    private void emitPolygon(List<Vertex> polygon) {
        if (polygon.size() < 3) {
            return;
        }
        for (int i = 1; i + 1 < polygon.size(); i++) {
            put(polygon.get(0));
            put(polygon.get(i));
            put(polygon.get(i + 1));
            if (this.primitiveLength == 4) {
                put(polygon.get(i + 1));
            }
        }
    }

    private void put(Vertex vertex) {
        this.target.addVertex(vertex.x, vertex.y, vertex.z);
        if ((vertex.flags & HAS_COLOR) != 0) {
            this.target.setColor(vertex.r, vertex.g, vertex.b, vertex.a);
        }
        if ((vertex.flags & HAS_UV) != 0) {
            this.target.setUv(vertex.u, vertex.v);
        }
        if ((vertex.flags & HAS_UV1) != 0) {
            this.target.setUv1(vertex.u1, vertex.v1);
        }
        if ((vertex.flags & HAS_UV2) != 0) {
            this.target.setUv2(vertex.u2, vertex.v2);
        }
        if ((vertex.flags & HAS_NORMAL) != 0) {
            this.target.setNormal(vertex.nx, vertex.ny, vertex.nz);
        }
        if ((vertex.flags & HAS_WIDTH) != 0) {
            this.target.setLineWidth(vertex.width);
        }
    }

    private static final class Vertex {
        int flags;
        float x;
        float y;
        float z;
        int r;
        int g;
        int b;
        int a;
        float u;
        float v;
        int u1;
        int v1;
        int u2;
        int v2;
        float nx;
        float ny;
        float nz;
        float width;
        float cx;
        float cy;
        float cw;

        static Vertex lerp(Vertex p, Vertex q, float t) {
            Vertex o = new Vertex();
            o.flags = p.flags | q.flags;
            o.x = p.x + (q.x - p.x) * t;
            o.y = p.y + (q.y - p.y) * t;
            o.z = p.z + (q.z - p.z) * t;
            o.r = Math.round(p.r + (q.r - p.r) * t);
            o.g = Math.round(p.g + (q.g - p.g) * t);
            o.b = Math.round(p.b + (q.b - p.b) * t);
            o.a = Math.round(p.a + (q.a - p.a) * t);
            o.u = p.u + (q.u - p.u) * t;
            o.v = p.v + (q.v - p.v) * t;
            o.u1 = t < 0.5F ? p.u1 : q.u1;
            o.v1 = t < 0.5F ? p.v1 : q.v1;
            o.u2 = t < 0.5F ? p.u2 : q.u2;
            o.v2 = t < 0.5F ? p.v2 : q.v2;
            float nx = p.nx + (q.nx - p.nx) * t;
            float ny = p.ny + (q.ny - p.ny) * t;
            float nz = p.nz + (q.nz - p.nz) * t;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 1.0E-6F) {
                nx /= len;
                ny /= len;
                nz /= len;
            }
            o.nx = nx;
            o.ny = ny;
            o.nz = nz;
            o.width = p.width + (q.width - p.width) * t;
            o.cx = p.cx + (q.cx - p.cx) * t;
            o.cy = p.cy + (q.cy - p.cy) * t;
            o.cw = p.cw + (q.cw - p.cw) * t;
            return o;
        }
    }
}
