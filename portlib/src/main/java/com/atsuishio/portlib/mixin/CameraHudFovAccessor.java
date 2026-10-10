package com.atsuishio.portlib.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraHudFovAccessor {
    @Accessor("hudFov")
    float sbw_portlib$getHudFov();
}
