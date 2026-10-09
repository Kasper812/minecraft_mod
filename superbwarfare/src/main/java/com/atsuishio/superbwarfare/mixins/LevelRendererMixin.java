package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.portlib.gui.LegacyRenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.LightCoordsUtil;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.entity.EntityRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void superbwarfare$thermalFullBright(Entity entity, net.minecraft.client.renderer.entity.state.EntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (ClientEventHandler.activeThermalImaging) {
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }
    }
}
