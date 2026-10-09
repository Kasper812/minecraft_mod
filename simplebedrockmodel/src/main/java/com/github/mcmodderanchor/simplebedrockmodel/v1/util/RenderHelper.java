package com.github.mcmodderanchor.simplebedrockmodel.v1.util;

import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import cn.sh1rocu.simplebedrockmodel.client.port.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelPart;
import cn.sh1rocu.simplebedrockmodel.client.port.SubmitBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class RenderHelper {


//    public static void enableItemEntityStencilTest() {
//        RenderSystem.assertOnRenderThread();
//        if (OptifineCompat.isOptifineInstalled()) {
//            // 以下代码用于应对 使用 optifine 的场景
//            int depthTextureId = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
//            int stencilTextureId = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
//            if (depthTextureId != GL30.GL_NONE && stencilTextureId == GL30.GL_NONE) {
//                GL30.glBindTexture(GL30.GL_TEXTURE_2D, depthTextureId);
//                int dataType = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_DEPTH_TYPE);
//                if (dataType == GL30.GL_UNSIGNED_NORMALIZED) {
//                    int width = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_WIDTH);
//                    int height = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_HEIGHT);
//                    GlStateManager._texImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_DEPTH24_STENCIL8, width, height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, null);
//                    GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 3553, depthTextureId, 0);
//                }
//            }
//        } else {
//            Minecraft.getInstance().getMainRenderTarget().enableStencil();
//        }
//        GL11.glEnable(GL11.GL_STENCIL_TEST);
//    }

    public static void renderFirstPersonArm(LocalPlayer player, @org.jspecify.annotations.Nullable MultiBufferSource bufferSource, HumanoidArm hand, PoseStack matrixStack, int combinedLight) {
        if (!(bufferSource instanceof SubmitBufferSource submit)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        AvatarRenderer<AbstractClientPlayer> renderer = renderManager.getPlayerRenderer(player);
        Identifier skin = player.getSkin().body().texturePath();

        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(matrixStack, submit.collector(), combinedLight, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(matrixStack, submit.collector(), combinedLight, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
    }

    /**
     * 将第一人称模型空间中的局部坐标转换为 Minecraft 绝对世界坐标。
     * <p>
     * 典型用法：传入骨骼的 globalTransform 变换后的坐标（或直接传 locator 坐标），
     * 得到该点在 Minecraft 世界中的绝对位置。
     *
     * @param localX         模型空间中的 X 坐标
     * @param localY         模型空间中的 Y 坐标
     * @param localZ         模型空间中的 Z 坐标
     * @param modelTransform 第一人称模型变换矩阵（antibob × gunOffset），即 identity → 视图空间的变换
     * @param poseInitial    RenderHandEvent 的 poseStack 初始 pose
     * @param camera         当前摄像机
     * @return 绝对世界坐标
     */
    public static Vec3 firstPersonToWorld(float localX, float localY, float localZ,
                                          Matrix4f modelTransform, Matrix4f poseInitial,
                                          Camera camera) {
        // modelTransform × localPos → 视图空间
        // cameraRotInv × poseInitial × 视图空间坐标 → 以摄像机为原点的世界对齐坐标
        // + camera.position() → 绝对世界坐标
        Matrix4f cameraRotInv = new Matrix4f(buildCameraRotation(camera)).invert();
        Matrix4f toWorldAligned = new Matrix4f(cameraRotInv).mul(poseInitial).mul(modelTransform);

        float wx = toWorldAligned.m00() * localX + toWorldAligned.m10() * localY + toWorldAligned.m20() * localZ + toWorldAligned.m30();
        float wy = toWorldAligned.m01() * localX + toWorldAligned.m11() * localY + toWorldAligned.m21() * localZ + toWorldAligned.m31();
        float wz = toWorldAligned.m02() * localX + toWorldAligned.m12() * localY + toWorldAligned.m22() * localZ + toWorldAligned.m32();

        Vec3 camPos = camera.position();
        return new Vec3(wx + camPos.x, wy + camPos.y, wz + camPos.z);
    }

    /**
     * 用摄像机的 pitch/yaw 构建视图旋转矩阵（与 Minecraft 内部一致）。
     * Minecraft 的视图矩阵构建顺序：先绕 X 旋转 pitch，再绕 Y 旋转 (yaw + 180)。
     */
    public static Matrix4f buildCameraRotation(Camera camera) {
        return new Matrix4f()
                .rotationX((float) Math.toRadians(camera.xRot()))
                .rotateY((float) Math.toRadians(camera.yRot() + 180f));
    }
}
