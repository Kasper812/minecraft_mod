package com.atsuishio.portlib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;

public final class LegacyRenderSystem {
    private static final float[] SHADER_COLOR = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static boolean blend;
    private static boolean additive;
    private static boolean depthTest = true;
    private static boolean depthMask = true;
    private static @Nullable Identifier shaderTexture;

    private LegacyRenderSystem() {
    }

    public static void setShaderColor(float r, float g, float b, float a) {
        SHADER_COLOR[0] = r;
        SHADER_COLOR[1] = g;
        SHADER_COLOR[2] = b;
        SHADER_COLOR[3] = a;
    }

    private static float lineWidth = 1.0F;

    public static void lineWidth(float width) {
        lineWidth = width;
    }

    public static float getShaderLineWidth() {
        return lineWidth;
    }

    public static float[] getShaderColor() {
        return SHADER_COLOR;
    }

    public static int shaderColorArgb() {
        return argb(SHADER_COLOR[0], SHADER_COLOR[1], SHADER_COLOR[2], SHADER_COLOR[3]);
    }

    public static int tint(int color) {
        if (SHADER_COLOR[0] == 1.0F && SHADER_COLOR[1] == 1.0F && SHADER_COLOR[2] == 1.0F && SHADER_COLOR[3] == 1.0F) {
            return color;
        }
        int a = Math.round(((color >>> 24) & 255) * clamp(SHADER_COLOR[3]));
        int r = Math.round(((color >>> 16) & 255) * clamp(SHADER_COLOR[0]));
        int g = Math.round(((color >>> 8) & 255) * clamp(SHADER_COLOR[1]));
        int b = Math.round((color & 255) * clamp(SHADER_COLOR[2]));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static float clamp(float v) {
        return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
    }

    public static int argb(float r, float g, float b, float a) {
        return Math.round(clamp(a) * 255.0F) << 24 | Math.round(clamp(r) * 255.0F) << 16 | Math.round(clamp(g) * 255.0F) << 8 | Math.round(clamp(b) * 255.0F);
    }

    public static boolean isAdditive() {
        return additive;
    }

    public static void enableBlend() {
        blend = true;
    }

    public static void disableBlend() {
        blend = false;
        additive = false;
    }

    public static void defaultBlendFunc() {
        additive = false;
    }

    public static void blendFunc(LegacyGlStateManager.SourceFactor src, LegacyGlStateManager.DestFactor dst) {
        additive = dst == LegacyGlStateManager.DestFactor.ONE;
    }

    public static void blendFunc(int src, int dst) {
        additive = dst == 1;
    }

    public static void blendFuncSeparate(LegacyGlStateManager.SourceFactor src, LegacyGlStateManager.DestFactor dst,
                                         LegacyGlStateManager.SourceFactor srcAlpha, LegacyGlStateManager.DestFactor dstAlpha) {
        additive = dst == LegacyGlStateManager.DestFactor.ONE;
    }

    public static void blendFuncSeparate(int src, int dst, int srcAlpha, int dstAlpha) {
        additive = dst == 1;
    }

    public static void enableDepthTest() {
        depthTest = true;
    }

    public static void disableDepthTest() {
        depthTest = false;
    }

    public static void depthMask(boolean flag) {
        depthMask = flag;
    }

    public static void colorMask(boolean r, boolean g, boolean b, boolean a) {
    }

    public static void enableCull() {
    }

    public static void disableCull() {
    }

    public static void setShader(Object shader) {
    }

    public static void setShaderTexture(int unit, Identifier texture) {
        if (unit == 0) {
            shaderTexture = texture;
        }
    }

    public static @Nullable Identifier getShaderTexture() {
        return shaderTexture;
    }

    public static void setShaderGameTime(long gameTime, float partialTick) {
    }

    public static void setShaderFogStart(float start) {
    }

    public static void setShaderFogEnd(float end) {
    }

    public static void setShaderFogShape(Object shape) {
    }

    public static void setShaderFogColor(float r, float g, float b) {
    }

    public static void setShaderFogColor(float r, float g, float b, float a) {
    }

    public static float getShaderFogStart() {
        return Float.MAX_VALUE;
    }

    public static float getShaderFogEnd() {
        return Float.MAX_VALUE;
    }

    public static @Nullable Object getShaderFogShape() {
        return null;
    }

    public static void enablePolygonOffset() {
    }

    public static void disablePolygonOffset() {
    }

    public static void polygonOffset(float factor, float units) {
    }

    public static void stencilFunc(int func, int ref, int mask) {
    }

    public static void stencilOp(int sFail, int dpFail, int dpPass) {
    }

    public static void stencilMask(int mask) {
    }

    public static void clearStencil(int value) {
    }

    public static void clear(int mask, boolean checkError) {
    }

    public static void clear(int mask) {
    }

    public static void clearColor(float r, float g, float b, float a) {
    }

    public static void enableScissor(int x, int y, int width, int height) {
    }

    public static void disableScissor() {
    }

    public static void applyModelViewMatrix() {
    }

    public static Matrix4fStack getModelViewStack() {
        return RenderSystem.getModelViewStack();
    }

    public static Matrix4f getModelViewMatrix() {
        return new Matrix4f(RenderSystem.getModelViewMatrixCopy());
    }

    public static Matrix4f getProjectionMatrix() {
        return new Matrix4f(IDENTITY);
    }

    public static void setProjectionMatrix(Matrix4f matrix, Object sorting) {
    }

    public static void assertOnRenderThread() {
        RenderSystem.assertOnRenderThread();
    }

    public static void resetState() {
        setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blend = false;
        additive = false;
        depthTest = true;
        depthMask = true;
        shaderTexture = null;
    }
}
