package com.atsuishio.superbwarfare.item

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.item.ItemStack

interface EnergyStorageItem {
    fun getMaxEnergy(stack: ItemStack): Int

    fun getMaxReceiveEnergy(stack: ItemStack): Int {
        return getMaxEnergy(stack)
    }

    fun getMaxExtractEnergy(stack: ItemStack): Int {
        return getMaxEnergy(stack)
    }
}
