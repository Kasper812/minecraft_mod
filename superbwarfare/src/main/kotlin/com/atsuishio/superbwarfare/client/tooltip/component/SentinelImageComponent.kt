package com.atsuishio.superbwarfare.client.tooltip.component

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.item.ItemStack

class SentinelImageComponent(width: Int, height: Int, stack: ItemStack) : GunImageComponent(width, height, stack) {
    constructor(stack: ItemStack) : this(32, 16, stack)
}
