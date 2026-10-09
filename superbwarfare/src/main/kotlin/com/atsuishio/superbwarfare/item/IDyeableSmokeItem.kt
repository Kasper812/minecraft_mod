package com.atsuishio.superbwarfare.item

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.item.ItemStack

interface IDyeableSmokeItem {
    fun setColor(stack: ItemStack, color: Int)

    fun getColor(stack: ItemStack): Int

    companion object {
        const val TAG_COLOR: String = "Color"
    }
}
