package com.atsuishio.portlib.render.clip;

import com.atsuishio.portlib.mixin.CameraHudFovAccessor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

public final class HandProjection {
    private HandProjection() {
    }

    public static Matrix4f projection() {
        Minecraft mc = Minecraft.getInstance();
        float fov = ((CameraHudFovAccessor) mc.gameRenderer.mainCamera()).sbw_portlib$getHudFov();
        if (fov <= 0.0F) {
            fov = 70.0F;
        }
        Window window = mc.getWindow();
        float aspect = (float) window.getWidth() / Math.max(1, window.getHeight());
        return new Matrix4f().setPerspective((float) Math.toRadians(fov), aspect, 0.05F, 100.0F);
    }

    public static Matrix4f modelView() {
        return new Matrix4f(RenderSystem.getModelViewStack());
    }

    public static Matrix4f viewProjection() {
        return projection().mul(modelView());
    }
}
