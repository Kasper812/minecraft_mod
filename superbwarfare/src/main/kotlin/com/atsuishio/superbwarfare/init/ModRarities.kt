package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.item.Rarity

object ModRarities {
    @JvmField
    val LEGENDARY = ModEnumExtensions.legendary

    @JvmField
    val SUPERB: Rarity = ModEnumExtensions.superb

    @JvmField
    val BEAST: Rarity = ModEnumExtensions.beast

    @JvmField
    val VIRTUAL: Rarity = ModEnumExtensions.virtual
}
