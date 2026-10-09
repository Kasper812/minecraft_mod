package com.atsuishio.superbwarfare.item.trinket

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.compat.trinkets.SlotReference
import com.atsuishio.superbwarfare.compat.trinkets.TrinketItem
import com.atsuishio.superbwarfare.compat.trinkets.TrinketsApi
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

class ThermalImagingGogglesItem : TrinketItem(Properties().stacksTo(1)) {
    override fun canEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity): Boolean {
        return TrinketsApi.getTrinketComponent(entity)
            .map { component -> !component.isEquipped(this) }
            .orElse(false)!!
    }
}
