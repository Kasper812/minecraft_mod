package com.atsuishio.portlib.gui;

public final class LegacyGlStateManager {
    private LegacyGlStateManager() {
    }

    public enum SourceFactor {
        CONSTANT_ALPHA, CONSTANT_COLOR, DST_ALPHA, DST_COLOR, ONE, ONE_MINUS_CONSTANT_ALPHA, ONE_MINUS_CONSTANT_COLOR,
        ONE_MINUS_DST_ALPHA, ONE_MINUS_DST_COLOR, ONE_MINUS_SRC_ALPHA, ONE_MINUS_SRC_COLOR, SRC_ALPHA, SRC_ALPHA_SATURATE,
        SRC_COLOR, ZERO
    }

    public enum DestFactor {
        CONSTANT_ALPHA, CONSTANT_COLOR, DST_ALPHA, DST_COLOR, ONE, ONE_MINUS_CONSTANT_ALPHA, ONE_MINUS_CONSTANT_COLOR,
        ONE_MINUS_DST_ALPHA, ONE_MINUS_DST_COLOR, ONE_MINUS_SRC_ALPHA, ONE_MINUS_SRC_COLOR, SRC_ALPHA, SRC_COLOR, ZERO
    }

    public static void _enableBlend() {
        LegacyRenderSystem.enableBlend();
    }

    public static void _disableBlend() {
        LegacyRenderSystem.disableBlend();
    }

    public static void _depthMask(boolean flag) {
        LegacyRenderSystem.depthMask(flag);
    }

    public static void _enableDepthTest() {
        LegacyRenderSystem.enableDepthTest();
    }

    public static void _disableDepthTest() {
        LegacyRenderSystem.disableDepthTest();
    }
}
