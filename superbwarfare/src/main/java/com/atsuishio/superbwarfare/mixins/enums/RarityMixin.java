package com.atsuishio.superbwarfare.mixins.enums;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
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

@Mixin(Rarity.class)
public abstract class RarityMixin {
    @Shadow
    @Final
    @Mutable
    private static Rarity[] $VALUES;

    @Invoker("<init>")
    private static Rarity sbw$create(String constant, int ordinal, int id, String name, ChatFormatting color) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Rarity;$VALUES:[Lnet/minecraft/world/item/Rarity;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER))
    private static void sbw$addRarities(CallbackInfo ci) {
        sbw$add("SUPERBWARFARE_LEGENDARY", "superbwarfare_legendary", ChatFormatting.GOLD);
        sbw$add("SUPERBWARFARE_SUPERB", "superbwarfare_superb", ChatFormatting.RED);
        sbw$add("SUPERBWARFARE_BEAST", "superbwarfare_beast", ChatFormatting.DARK_RED);
        sbw$add("SUPERBWARFARE_VIRTUAL", "superbwarfare_virtual", ChatFormatting.WHITE);
    }

    @Unique
    private static void sbw$add(String constant, String name, ChatFormatting color) {
        Rarity[] values = $VALUES;
        Rarity value = sbw$create(constant, values.length, values.length, name, color);
        Rarity[] extended = Arrays.copyOf(values, values.length + 1);
        extended[values.length] = value;
        $VALUES = extended;
    }
}
