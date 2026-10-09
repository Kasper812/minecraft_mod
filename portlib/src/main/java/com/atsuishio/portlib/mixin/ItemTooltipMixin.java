package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.item.LegacyBlockTooltip;
import com.atsuishio.portlib.item.LegacyTooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemTooltipMixin {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void sbw_portlib$blockTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag, CallbackInfo ci) {
        if ((Object) this instanceof BlockItem blockItem && blockItem.getBlock() instanceof LegacyBlockTooltip tooltip) {
            tooltip.appendHoverText(stack, context, new LegacyTooltipList(builder), flag);
        }
    }
}
