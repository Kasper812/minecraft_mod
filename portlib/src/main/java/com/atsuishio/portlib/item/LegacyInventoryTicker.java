package com.atsuishio.portlib.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface LegacyInventoryTicker {
    void legacyInventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected);
}
