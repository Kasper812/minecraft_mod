package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.forge.client.ScreenOpeningEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiSetScreenMixin {
    @Shadow
    private Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void sbw_portlib$screenOpening(Screen next, CallbackInfo ci) {
        if (ScreenOpeningEvent.post(this.screen, next)) {
            ci.cancel();
        }
    }
}
