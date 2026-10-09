package com.atsuishio.superbwarfare.mixins;

import org.joml.Matrix4fc;
import org.joml.Matrix4f;
import com.atsuishio.superbwarfare.config.client.DisplayConfig;
import com.atsuishio.superbwarfare.data.vehicle.VehicleData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModMobEffects;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V", shift = At.Shift.AFTER))
    private void superbWarfare$captureMatrices(DeltaTracker deltaTracker, CallbackInfo ci,
                                               @com.llamalad7.mixinextras.sugar.Local Matrix4fc modelViewMatrix,
                                               @com.llamalad7.mixinextras.sugar.Local Matrix4f projectionMatrix) {
        ClientEventHandler.modelViewMatrix = new Matrix4f(modelViewMatrix);
        ClientEventHandler.projectionMatrix = new Matrix4f(projectionMatrix);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void superbWarfare$renderFramePre(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ClientEventHandler.handleWeaponFire();
        ClientEventHandler.handleWeaponBreathSway();
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void bobView(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack p_109139_, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (GunItem.isHeldWeapon(stack) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
                ci.cancel();
            }
        }
    }

    // From Immersive_Aircraft
    @Shadow
    @Final
    private Camera mainCamera;

    @SuppressWarnings("ConstantValue")
    @Inject(method = "bobHurt", at = @At("HEAD"))
    public void superbWarfare$renderWorld(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack matrices, CallbackInfo ci) {
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        // Reset pushed stack from previous frame in case hand event didn't fire
        ClientEventHandler.vehiclePoseStack = null;

        Entity entity = mainCamera.entity();
        matrices.mulPose(Axis.ZP.rotationDegrees(ClientEventHandler.cameraRoll));

        if (entity instanceof Player player && !player.isSpectator() && player.hasEffect(ModMobEffects.SHOCK)) {
            float shakeStrength = (float) DisplayConfig.SHOCK_SCREEN_SHAKE.get() / 100.0f;
            if (shakeStrength <= 0.0f) return;
            matrices.mulPose(Axis.ZP.rotationDegrees((float) Mth.nextDouble(RandomSource.create(), 8, 12) * shakeStrength));
        }

        if (entity != null && entity.getRootVehicle() instanceof VehicleEntity vehicle && (!mainCamera.isDetached() || ClientEventHandler.zoomVehicle)) {
            var seats = VehicleData.compute(vehicle).seats();
            int index = vehicle.getSeatIndex(entity);
            if (index < 0 || index >= seats.size()) return;

            // Push before applying vehicle transforms so hand rendering can be isolated
            matrices.pushPose();
            ClientEventHandler.vehiclePoseStack = matrices;

            // rotate camera
            float a = Mth.wrapDegrees(mainCamera.yRot() - Mth.lerp(tickDelta, vehicle.yRotO, vehicle.getYRot()));

            var seat = seats.get(index);

            if (seat.transform.equals("VehicleFlat")) {
                a = 0;
            }

            float r = (Mth.abs(a) - 90f) / 90f;
            float r2;
            if (Mth.abs(a) <= 90f) {
                r2 = a / 90f;
            } else {
                if (a < 0) {
                    r2 = -(180f + a) / 90f;
                } else {
                    r2 = (180f - a) / 90f;
                }
            }

            matrices.mulPose(Axis.ZP.rotationDegrees(-r * vehicle.getRoll(tickDelta) - r2 * vehicle.getViewXRot(tickDelta)));

            if (!vehicle.useFixedCameraPos(entity)) {
                // fetch eye offset
                float eye = entity.getEyeHeight();

                // transform eye offset to match aircraft rotation
                Vector3f offset = new Vector3f(0, -eye, 0);
                Quaternionf quaternion = Axis.XP.rotationDegrees(0.0f);
                quaternion.mul(Axis.YP.rotationDegrees(-vehicle.getViewYRot(tickDelta)));
                quaternion.mul(Axis.XP.rotationDegrees(vehicle.getViewXRot(tickDelta)));
                quaternion.mul(Axis.ZP.rotationDegrees(vehicle.getRoll(tickDelta)));
                offset.rotate(quaternion);

                // apply camera offset
                matrices.mulPose(Axis.XP.rotationDegrees(mainCamera.xRot()));
                matrices.mulPose(Axis.YP.rotationDegrees(mainCamera.yRot() + 180.0f));
                matrices.translate(offset.x(), offset.y() + eye, offset.z());
                matrices.mulPose(Axis.YP.rotationDegrees(-mainCamera.yRot() - 180.0f));
                matrices.mulPose(Axis.XP.rotationDegrees(-mainCamera.xRot()));
            }
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void bobHurt(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack pMatrixStack, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() instanceof LivingEntity living) {
            var source = living.getLastDamageSource();
            if (source != null && source.is(ModTags.DamageTypes.NO_HURT_EFFECT)) {
                ci.cancel();
            }
        }
    }
}
