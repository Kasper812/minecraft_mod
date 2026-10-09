package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.client.VehicleClientRenderState;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.item.LungeMine;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void superbwarfare$renderHandsWithItems(
            float partialTick, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector,
            LocalPlayer player, int packedLight, CallbackInfo ci
    ) {
        ClientEventHandler.handleWeaponTurn(partialTick);

        // Pop only the stack that actually received the push in bobHurt.
        if (ClientEventHandler.vehiclePoseStack != null && ClientEventHandler.vehiclePoseStack == poseStack) {
            poseStack.popPose();
            ClientEventHandler.vehiclePoseStack = null;
        }

        if (VehicleClientRenderState.shouldHideHandsAndHotbar(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void superbwarfare$skipEquipAnimation(ItemStack oldStack, ItemStack newStack, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!oldStack.isEmpty() && !newStack.isEmpty()
                && (oldStack.getItem() instanceof GunItem
                || oldStack.getItem() instanceof MonitorItem
                || oldStack.getItem() instanceof LungeMine)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "submitArmWithItem", at = @At("HEAD"), argsOnly = true, index = 5)
    private float superbwarfare$removeGunSwing(float value) {
        return GunItem.isHeldWeapon(mainHandItem) ? 0f : value;
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void superbwarfare$renderArmWithItem(
            AbstractClientPlayer player, float partialTick, float pitch,
            InteractionHand hand, float swingProgress, ItemStack stack,
            float equipProgress, PoseStack poseStack,
            net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int packedLight,
            CallbackInfo ci
    ) {
        if (ClientEventHandler.shouldCancelHandRender(hand)) {
            ci.cancel();
        }
    }
}
