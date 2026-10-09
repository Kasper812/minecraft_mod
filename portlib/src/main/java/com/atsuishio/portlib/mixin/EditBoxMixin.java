package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.gui.LegacyEditBox;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EditBox.class)
public abstract class EditBoxMixin implements LegacyEditBox {
    @Shadow
    private String value;
    @Shadow
    private int cursorPos;
    @Shadow
    private int highlightPos;

    @Unique
    private Predicate<String> sbw_portlib$filter = s -> true;
    @Unique
    private String sbw_portlib$oldValue;
    @Unique
    private int sbw_portlib$oldCursor;
    @Unique
    private int sbw_portlib$oldHighlight;

    @Override
    public void sbw_portlib$setFilter(Predicate<String> filter) {
        this.sbw_portlib$filter = filter;
    }

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void sbw_portlib$filterSetValue(String newValue, CallbackInfo ci) {
        if (!this.sbw_portlib$filter.test(newValue)) {
            ci.cancel();
        }
    }

    @Inject(method = {"insertText", "deleteCharsToPos"}, at = @At("HEAD"))
    private void sbw_portlib$remember(CallbackInfo ci) {
        this.sbw_portlib$oldValue = this.value;
        this.sbw_portlib$oldCursor = this.cursorPos;
        this.sbw_portlib$oldHighlight = this.highlightPos;
    }

    @Inject(method = {"insertText", "deleteCharsToPos"}, at = @At("RETURN"))
    private void sbw_portlib$revert(CallbackInfo ci) {
        if (this.sbw_portlib$oldValue != null && !this.sbw_portlib$filter.test(this.value)) {
            this.value = this.sbw_portlib$oldValue;
            this.cursorPos = this.sbw_portlib$oldCursor;
            this.highlightPos = this.sbw_portlib$oldHighlight;
        }
        this.sbw_portlib$oldValue = null;
    }
}
