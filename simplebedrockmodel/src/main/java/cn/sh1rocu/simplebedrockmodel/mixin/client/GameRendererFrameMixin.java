package cn.sh1rocu.simplebedrockmodel.mixin.client;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderFrameEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererFrameMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void sbm$renderTickStart(DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderFrameEvent.EVENT.invoker().post(new RenderFrameEvent(Minecraft.getInstance(), RenderFrameEvent.Phase.START, (DeltaTracker.Timer) Minecraft.getInstance().getDeltaTracker()));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void sbm$renderTickEnd(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        RenderFrameEvent.EVENT.invoker().post(new RenderFrameEvent(Minecraft.getInstance(), RenderFrameEvent.Phase.END, (DeltaTracker.Timer) Minecraft.getInstance().getDeltaTracker()));
    }
}
