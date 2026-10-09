package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.LightmapRenderStateExtractor.class)
public class LightTextureMixin {

    @Inject(method = "extract", at = @At("TAIL"))
    private void superbwarfare$thermalLightmap(net.minecraft.client.renderer.state.LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (!renderState.needsUpdate) return;

        if (ClientEventHandler.hasThermalImagingGoggles()) {
            renderState.nightVisionEffectIntensity = ClientEventHandler.activeThermalImaging ? 0.51F : 1.0F;
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof VehicleEntity vehicle) {
            var index = vehicle.getSeatIndex(player);
            var seats = vehicle.computed().seats();
            if (index >= 0 && index < seats.size()) {
                var seat = seats.get(index);
                if (seat.hasThermalImaging && ClientEventHandler.activeThermalImaging) {
                    renderState.nightVisionEffectIntensity = 0.51F;
                }
            }
        }
    }
}
