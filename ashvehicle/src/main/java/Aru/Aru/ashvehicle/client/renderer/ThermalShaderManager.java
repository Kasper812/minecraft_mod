package Aru.Aru.ashvehicle.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ThermalShaderManager {

    private static final Identifier THERMAL_EFFECT = Identifier.fromNamespaceAndPath("ashvehicle", "thermal");

    private static boolean enabled = false;

    private static boolean renderingEntity = false;

    public static void beginFrame() {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (!THERMAL_EFFECT.equals(mc.gameRenderer.currentPostEffect())) {
            com.atsuishio.superbwarfare.port.Compat262Kt.loadEffect(mc.gameRenderer, THERMAL_EFFECT);
        }
    }

    public static void applyThermalEffect() {
    }

    public static void markEntityRendering(boolean rendering) {
        renderingEntity = rendering;
    }

    public static boolean isRenderingEntity() {
        return renderingEntity;
    }

    public static void cleanup() {
        Minecraft mc = Minecraft.getInstance();
        if (THERMAL_EFFECT.equals(mc.gameRenderer.currentPostEffect())) {
            mc.gameRenderer.clearPostEffect();
        }
    }

    public static void onResize(int width, int height) {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        enabled = true;
        beginFrame();
    }

    public static void disable() {
        enabled = false;
        cleanup();
    }
}
