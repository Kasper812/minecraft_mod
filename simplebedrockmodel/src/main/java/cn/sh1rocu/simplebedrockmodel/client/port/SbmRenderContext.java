package cn.sh1rocu.simplebedrockmodel.client.port;

public final class SbmRenderContext {
    private static final ThreadLocal<int[]> GUI_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private SbmRenderContext() {
    }

    public static void pushGui() {
        GUI_DEPTH.get()[0]++;
    }

    public static void popGui() {
        GUI_DEPTH.get()[0]--;
    }

    public static boolean normalCullingEnabled() {
        return GUI_DEPTH.get()[0] <= 0;
    }
}
