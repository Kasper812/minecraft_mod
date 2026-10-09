package cn.sh1rocu.simplebedrockmodel.mixin.client;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class GameRendererMixin {
    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float sbm$viewportFovEvent(float original, float partialTicks) {
        var event = new ViewportEvent.ComputeFov(Minecraft.getInstance().gameRenderer, (Camera) (Object) this, partialTicks, original, true);
        ViewportEvent.FOV.invoker().post(event);
        return (float) event.getFOV();
    }

    @ModifyReturnValue(method = "calculateHudFov", at = @At("RETURN"))
    private float sbm$viewportHudFovEvent(float original, float partialTicks) {
        var event = new ViewportEvent.ComputeFov(Minecraft.getInstance().gameRenderer, (Camera) (Object) this, partialTicks, original, false);
        ViewportEvent.FOV.invoker().post(event);
        return (float) event.getFOV();
    }
}
