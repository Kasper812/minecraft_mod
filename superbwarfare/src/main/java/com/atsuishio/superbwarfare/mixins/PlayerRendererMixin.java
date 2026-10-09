package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModEnumExtensions;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.gun.GeoGunItemV2;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<Avatar, AvatarRenderState, PlayerModel> {
    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadow) {
        super(context, model, shadow);
    }

    @Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At("RETURN"), cancellable = true)
    private static void superbwarfare$getArmPose(
            Avatar player,
            ItemStack stack,
            InteractionHand hand,
            CallbackInfoReturnable<HumanoidModel.ArmPose> cir
    ) {
        if (stack.getItem() instanceof GunGeoItem gunGeoItem) {
            cir.setReturnValue(gunGeoItem.getArmPose(player, hand, stack));
        } else if (stack.getItem() instanceof GeoGunItemV2 geoGunItemV2) {
            cir.setReturnValue(geoGunItemV2.getArmPose(player, hand, stack));
        } else if (stack.is(ModItems.LUNGE_MINE) && player.getUsedItemHand() == hand) {
            cir.setReturnValue(ModEnumExtensions.Client.getLungeMinePose());
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void superbwarfare$artilleryIndicatorOnHead(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (state.isUsingItem && entity.swingTime == 0) {
            ItemStack useItem = entity.getItemInHand(state.useItemHand);
            if (useItem.is(ModItems.ARTILLERY_INDICATOR)) {
                this.itemModelResolver.updateForLiving(state.heldOnHead, useItem, ItemDisplayContext.HEAD, entity);
            }
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Identifier skinTexture, ModelPart arm, boolean hasSleeve, CallbackInfo ci) {
        if (ClientEventHandler.activeThermalImaging) {
            ci.cancel();
            PlayerModel model = this.getModel();
            arm.resetPose();
            arm.visible = true;
            model.leftSleeve.visible = hasSleeve;
            model.rightSleeve.visible = hasSleeve;
            model.leftArm.zRot = -0.1F;
            model.rightArm.zRot = 0.1F;
            submitNodeCollector.submitModelPart(arm, poseStack, RenderTypes.entityTranslucent(skinTexture), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.pack(15, 10), null);
        }
    }
}
