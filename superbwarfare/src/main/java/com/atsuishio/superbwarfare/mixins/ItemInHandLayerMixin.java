package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "submitArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItemHead(net.minecraft.client.renderer.entity.state.ArmedEntityRenderState state,
                                       net.minecraft.client.renderer.item.ItemStackRenderState item,
                                       ItemStack stack,
                                       HumanoidArm arm,
                                       PoseStack poseStack,
                                       net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector,
                                       int light,
                                       CallbackInfo ci) {
        if (com.atsuishio.portlib.render.PortRenderStates.entity(state) instanceof Player player) {
            if (arm == HumanoidArm.LEFT || arm == HumanoidArm.RIGHT) {
                if (player.getVehicle() instanceof VehicleEntity vehicle && vehicle.banHand(player)) {
                    ci.cancel();
                }
            }
            if (arm == HumanoidArm.LEFT) {
                ItemStack mainHand = player.getMainHandItem();
                if (GunItem.isHeldWeapon(mainHand) || mainHand.is(ModItems.LUNGE_MINE)) {
                    ci.cancel();
                }
            }
        }
    }
}
