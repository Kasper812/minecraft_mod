package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.client.renderer.special.OBBRenderer;
import com.atsuishio.superbwarfare.config.server.MiscConfig;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "showHitboxes", at = @At("RETURN"))
    private void renderHitbox(Entity entity, float partialTicks, boolean isServerEntity, CallbackInfo ci) {
        if (!isServerEntity && entity instanceof VehicleEntity vehicle && !vehicle.enableAABB()) {
            com.atsuishio.superbwarfare.client.renderer.special.OBBGizmos.emit(vehicle, partialTicks);
        }
    }

    @Inject(method = "showHitboxes", at = @At("HEAD"), cancellable = true)
    private void onPreRenderHitbox(Entity entity, float partialTicks, boolean isServerEntity, CallbackInfo ci) {
        if (entity.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MINE) && MiscConfig.MINE_HITBOX_INVISIBLE.get()) {
            ci.cancel();
        }
    }
}
