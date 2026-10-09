package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

public final class LegacyLines {
    private LegacyLines() {
    }

    public static float lineWidth() {
        return Minecraft.getInstance().getWindow().getAppropriateLineWidth();
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, AABB box, float red, float green, float blue, float alpha) {
        renderLineBox(poseStack, consumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
        renderLineBox(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, red, green, blue);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha, float red2, float green2, float blue2) {
        PoseStack.Pose pose = poseStack.last();
        float x0 = (float) minX;
        float y0 = (float) minY;
        float z0 = (float) minZ;
        float x1 = (float) maxX;
        float y1 = (float) maxY;
        float z1 = (float) maxZ;
        float w = lineWidth();
        line(pose, consumer, x0, y0, z0, x1, y0, z0, red, green2, blue2, alpha, 1, 0, 0, w);
        line(pose, consumer, x0, y0, z0, x0, y1, z0, red2, green, blue2, alpha, 0, 1, 0, w);
        line(pose, consumer, x0, y0, z0, x0, y0, z1, red2, green2, blue, alpha, 0, 0, 1, w);
        line(pose, consumer, x1, y0, z0, x1, y1, z0, red, green, blue, alpha, 0, 1, 0, w);
        line(pose, consumer, x1, y1, z0, x0, y1, z0, red, green, blue, alpha, -1, 0, 0, w);
        line(pose, consumer, x0, y1, z0, x0, y1, z1, red, green, blue, alpha, 0, 0, 1, w);
        line(pose, consumer, x0, y1, z1, x0, y0, z1, red, green, blue, alpha, 0, -1, 0, w);
        line(pose, consumer, x0, y0, z1, x1, y0, z1, red, green, blue, alpha, 1, 0, 0, w);
        line(pose, consumer, x1, y0, z1, x1, y0, z0, red, green, blue, alpha, 0, 0, -1, w);
        line(pose, consumer, x0, y1, z1, x1, y1, z1, red, green, blue, alpha, 1, 0, 0, w);
        line(pose, consumer, x1, y0, z1, x1, y1, z1, red, green, blue, alpha, 0, 1, 0, w);
        line(pose, consumer, x1, y1, z0, x1, y1, z1, red, green, blue, alpha, 0, 0, 1, w);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer consumer, float ax, float ay, float az, float bx, float by, float bz, float r, float g, float b, float a, float nx, float ny, float nz, float width) {
        consumer.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(width);
        consumer.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
