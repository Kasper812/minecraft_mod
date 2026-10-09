package com.atsuishio.superbwarfare.item

import com.atsuishio.superbwarfare.port.*

import net.minecraft.core.dispenser.DispenseItemBehavior

interface DispenserLaunchable {
    fun getLaunchBehavior(): DispenseItemBehavior
}
