package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "addEnvironmentAttributeLayers", at = @At("RETURN"))
    private void superbwarfare$thermalImagingLayers(net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder builder,
                                                    CallbackInfoReturnable<net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder> cir) {
        builder.addTimeBasedLayer(net.minecraft.world.attribute.EnvironmentAttributes.SKY_COLOR,
                (color, cacheTickId) -> ClientEventHandler.activeThermalImaging ? (color & 0xFF000000) | 0x1A1A1A : color);
        builder.addTimeBasedLayer(net.minecraft.world.attribute.EnvironmentAttributes.CLOUD_COLOR,
                (color, cacheTickId) -> ClientEventHandler.activeThermalImaging ? (color & 0xFF000000) | 0x333333 : color);
    }
}
