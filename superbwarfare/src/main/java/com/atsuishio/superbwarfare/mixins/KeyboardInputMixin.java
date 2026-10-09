package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModMobEffects;
import com.atsuishio.superbwarfare.tools.NBTTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {

    // 按键修改mixin
    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        var tag = NBTTool.getTag(stack);

        if (stack.is(ModItems.MONITOR) && tag.getBooleanOr("Using", false) && tag.getBooleanOr("Linked", false)) {
            superbwarfare$blockMovement();
        }

        if (Minecraft.getInstance().player == null
                || !Minecraft.getInstance().player.hasEffect(ModMobEffects.SHOCK)
                || Minecraft.getInstance().player.isSpectator()
        ) return;

        superbwarfare$blockMovement();
    }

    @org.spongepowered.asm.mixin.Unique
    private void superbwarfare$blockMovement() {
        this.keyPresses = new net.minecraft.world.entity.player.Input(false, false, false, false, false, false, this.keyPresses.sprint());
        this.moveVector = net.minecraft.world.phys.Vec2.ZERO;
    }
}
