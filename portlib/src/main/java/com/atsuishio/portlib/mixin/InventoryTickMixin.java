package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.item.LegacyInventoryTicker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryTickMixin {
    @Shadow
    @Final
    public Player player;

    @Shadow
    public abstract int getSelectedSlot();

    @Shadow
    public abstract ItemStack getItem(int slot);

    @Inject(method = "tick", at = @At("TAIL"))
    private void sbw_portlib$legacyTick(CallbackInfo ci) {
        int selected = getSelectedSlot();
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof LegacyInventoryTicker ticker) {
                ticker.legacyInventoryTick(stack, player.level(), player, i, i == selected);
            }
        }
        int index = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof LegacyInventoryTicker ticker) {
                ticker.legacyInventoryTick(stack, player.level(), player, index, false);
            }
            index++;
        }
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!offhand.isEmpty() && offhand.getItem() instanceof LegacyInventoryTicker ticker) {
            ticker.legacyInventoryTick(offhand, player.level(), player, 0, false);
        }
    }
}
