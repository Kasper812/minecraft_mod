package com.atsuishio.superbwarfare.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.atsuishio.superbwarfare.client.ClientRenderHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsMixin {

    @Inject(
            method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("TAIL")
    )
    private void superbwarfare$renderItemDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        ClientRenderHandler.renderItemDecorations(com.atsuishio.portlib.gui.GuiGraphics.of((GuiGraphicsExtractor) (Object) this), font, stack, x, y);
    }
}
