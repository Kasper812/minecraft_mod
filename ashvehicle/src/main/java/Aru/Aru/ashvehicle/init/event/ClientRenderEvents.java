package Aru.Aru.ashvehicle.init.event;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.client.renderer.ThermalShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientRenderEvents {

    // Nuclear explosion red sky effect
    private static long nukeStartTime = 0;
    private static int nukeDuration = 0; // in ticks
    private static Vec3 nukePosition = null;
    private static float nukeRadius = 0;

    /**
     * Activate red sky effect from nuclear explosion
     * @param pos Position of explosion
     * @param radius Effect radius
     * @param durationTicks How long the effect lasts
     */
    public static void activateNukeSky(Vec3 pos, float radius, int durationTicks) {
        nukeStartTime = System.currentTimeMillis();
        nukeDuration = durationTicks;
        nukePosition = pos;
        nukeRadius = radius;
    }

    public static void register() {
        com.atsuishio.portlib.render.FogColorEvents.COMPUTE.register((color, partialTick) -> onComputeFogColor(color));
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> ThermalShaderManager.beginFrame());
    }

    private static void onComputeFogColor(org.joml.Vector4f event) {
        if (nukePosition == null || nukeDuration <= 0) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        long elapsed = System.currentTimeMillis() - nukeStartTime;
        float elapsedTicks = elapsed / 50f; // 50ms per tick
        
        if (elapsedTicks > nukeDuration) {
            // Effect ended
            nukePosition = null;
            nukeDuration = 0;
            return;
        }
        
        // Calculate distance from player to explosion
        double distance = mc.player.position().distanceTo(nukePosition);
        if (distance > nukeRadius) return; // Too far
        
        // Calculate intensity based on time and distance
        float timeProgress = elapsedTicks / nukeDuration;
        float distanceFactor = 1.0f - (float)(distance / nukeRadius);
        
        // Intensity curve: quick rise, slow fade
        float intensity;
        if (timeProgress < 0.1f) {
            // Quick rise (first 10%)
            intensity = timeProgress * 10f;
        } else {
            // Slow fade (remaining 90%)
            intensity = 1.0f - ((timeProgress - 0.1f) / 0.9f);
        }
        intensity *= distanceFactor;
        intensity = Math.max(0, Math.min(1, intensity));
        
        if (intensity > 0.01f) {
            // Blend current fog color with red/orange
            float currentRed = event.x();
            float currentGreen = event.y();
            float currentBlue = event.z();
            
            // Target: deep red-orange (0.9, 0.2, 0.05)
            float targetRed = 0.9f;
            float targetGreen = 0.15f;
            float targetBlue = 0.05f;
            
            // Lerp to red
            float newRed = currentRed + (targetRed - currentRed) * intensity;
            float newGreen = currentGreen + (targetGreen - currentGreen) * intensity;
            float newBlue = currentBlue + (targetBlue - currentBlue) * intensity;
            
            event.set(newRed, newGreen, newBlue, event.w());
        }
    }
}
