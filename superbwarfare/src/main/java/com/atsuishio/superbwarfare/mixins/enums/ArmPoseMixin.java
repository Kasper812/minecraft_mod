package com.atsuishio.superbwarfare.mixins.enums;

import net.minecraft.client.model.HumanoidModel;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(HumanoidModel.ArmPose.class)
public abstract class ArmPoseMixin {
    @Shadow
    @Final
    @Mutable
    private static HumanoidModel.ArmPose[] $VALUES;

    @Invoker("<init>")
    private static HumanoidModel.ArmPose sbw$create(String constant, int ordinal, boolean twoHanded, boolean affectsOffhandPose) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/HumanoidModel$ArmPose;$VALUES:[Lnet/minecraft/client/model/HumanoidModel$ArmPose;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER))
    private static void sbw$addPoses(CallbackInfo ci) {
        sbw$add("SUPERBWARFARE_LUNGE_MINE_POSE");
        sbw$add("SUPERBWARFARE_MINIGUN_POSE");
        sbw$add("SUPERBWARFARE_M2_POSE");
        sbw$add("SUPERBWARFARE_SUPER_STAR_SHOOTER_POSE");
        sbw$add("SUPERBWARFARE_REPAIR_TOOL_POSE");
    }

    @Unique
    private static void sbw$add(String constant) {
        HumanoidModel.ArmPose[] values = $VALUES;
        HumanoidModel.ArmPose value = sbw$create(constant, values.length, false, false);
        HumanoidModel.ArmPose[] extended = Arrays.copyOf(values, values.length + 1);
        extended[values.length] = value;
        $VALUES = extended;
    }
}
