package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.RenderItemInHandBobEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.RenderLevelBobEvent;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @WrapWithCondition(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean sbm$itemBobHurt(GameRenderer instance, CameraRenderState cameraState, PoseStack poseStack) {
        var event = new RenderItemInHandBobEvent.BobHurt();
        RenderItemInHandBobEvent.BOB_HURT.invoker().post(event);
        return !event.isCanceled();
    }

    @WrapWithCondition(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean sbm$itemBobView(GameRenderer instance, CameraRenderState cameraState, PoseStack poseStack) {
        var event = new RenderItemInHandBobEvent.BobView();
        RenderItemInHandBobEvent.BOB_VIEW.invoker().post(event);
        return !event.isCanceled();
    }

    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean sbm$levelBobHurt(GameRenderer instance, CameraRenderState cameraState, PoseStack poseStack) {
        var event = new RenderLevelBobEvent.BobHurt();
        RenderLevelBobEvent.BOB_HURT.invoker().post(event);
        return !event.isCanceled();
    }

    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean sbm$levelBobView(GameRenderer instance, CameraRenderState cameraState, PoseStack poseStack) {
        var event = new RenderLevelBobEvent.BobView();
        RenderLevelBobEvent.BOB_VIEW.invoker().post(event);
        return !event.isCanceled();
    }
}
