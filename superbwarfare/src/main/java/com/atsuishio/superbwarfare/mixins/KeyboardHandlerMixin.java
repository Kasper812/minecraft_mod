package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.event.ClickEventHandler;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void superbwarfare$onKeyPressed(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        if (ClickEventHandler.onKeyPressed(event.key(), event.scancode(), action, event.modifiers())) {
            ci.cancel();
        }
    }
}