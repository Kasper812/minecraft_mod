package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.render.PortRenderStates;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.atsuishio.portlib.entity.LegacyEntityFlags;
import net.minecraft.client.renderer.culling.Frustum;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void portlib$captureEntity(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        state.setData(PortRenderStates.ENTITY, entity);
        state.setData(PortRenderStates.PARTIAL_TICK, partialTicks);
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void portlib$noCulling(Entity entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (((LegacyEntityFlags) entity).sbw_portlib$isNoCulling() && entity.shouldRender(camX, camY, camZ)) {
            cir.setReturnValue(true);
        }
    }
}
