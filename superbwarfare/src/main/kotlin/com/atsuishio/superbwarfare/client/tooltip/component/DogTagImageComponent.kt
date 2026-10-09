package com.atsuishio.superbwarfare.client.tooltip.component

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack

class DogTagImageComponent(var width: Int, var height: Int, var stack: ItemStack) : TooltipComponent {
    constructor(stack: ItemStack) : this(80, 80, stack)
}
