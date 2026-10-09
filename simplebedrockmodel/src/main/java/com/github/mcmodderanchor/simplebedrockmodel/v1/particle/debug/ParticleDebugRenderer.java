package com.github.mcmodderanchor.simplebedrockmodel.v1.particle.debug;

import com.github.mcmodderanchor.simplebedrockmodel.v1.particle.world.WorldEmitterManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;



@Environment(EnvType.CLIENT)
public class ParticleDebugRenderer {
    private static final double BOX_RADIUS = 0.25D;

    public static void onRenderLevelStage(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            return;
        }

        if (WorldEmitterManager.getInstance().getEmitters().isEmpty()) {
            return;
        }

        Vec3 cameraPos = mc.gameRenderer.mainCamera().position();
        PoseStack poseStack = context.poseStack();
        for (WorldEmitterManager.ActiveWorldEmitter emitter : WorldEmitterManager.getInstance().getEmitters()) {
            if (emitter.level != mc.level) {
                continue;
            }
            float x0 = (float) (emitter.worldX - BOX_RADIUS - cameraPos.x);
            float y0 = (float) (emitter.worldY - BOX_RADIUS - cameraPos.y);
            float z0 = (float) (emitter.worldZ - BOX_RADIUS - cameraPos.z);
            float x1 = (float) (emitter.worldX + BOX_RADIUS - cameraPos.x);
            float y1 = (float) (emitter.worldY + BOX_RADIUS - cameraPos.y);
            float z1 = (float) (emitter.worldZ + BOX_RADIUS - cameraPos.z);
            context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> lineBox(pose, buffer, x0, y0, z0, x1, y1, z1));
        }
    }

    private static void lineBox(PoseStack.Pose pose, VertexConsumer buffer, float x0, float y0, float z0, float x1, float y1, float z1) {
        line(pose, buffer, x0, y0, z0, x1, y0, z0);
        line(pose, buffer, x0, y1, z0, x1, y1, z0);
        line(pose, buffer, x0, y0, z1, x1, y0, z1);
        line(pose, buffer, x0, y1, z1, x1, y1, z1);
        line(pose, buffer, x0, y0, z0, x0, y1, z0);
        line(pose, buffer, x1, y0, z0, x1, y1, z0);
        line(pose, buffer, x0, y0, z1, x0, y1, z1);
        line(pose, buffer, x1, y0, z1, x1, y1, z1);
        line(pose, buffer, x0, y0, z0, x0, y0, z1);
        line(pose, buffer, x1, y0, z0, x1, y0, z1);
        line(pose, buffer, x0, y1, z0, x0, y1, z1);
        line(pose, buffer, x1, y1, z0, x1, y1, z1);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer buffer, float ax, float ay, float az, float bx, float by, float bz) {
        float nx = bx - ax;
        float ny = by - ay;
        float nz = bz - az;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len;
        ny /= len;
        nz /= len;
        buffer.addVertex(pose, ax, ay, az).setColor(0.2F, 1.0F, 1.0F, 1.0F).setNormal(pose, nx, ny, nz).setLineWidth(1.0F);
        buffer.addVertex(pose, bx, by, bz).setColor(0.2F, 1.0F, 1.0F, 1.0F).setNormal(pose, nx, ny, nz).setLineWidth(1.0F);
    }
}
