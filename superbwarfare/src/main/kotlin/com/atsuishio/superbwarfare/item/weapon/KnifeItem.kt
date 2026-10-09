package com.atsuishio.superbwarfare.item.weapon

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.item.CustomDamageProperty
import com.atsuishio.superbwarfare.tiers.ModItemTier
import net.minecraft.world.item.Item

@RegistryName("knife")
open class KnifeItem : Item(
    CustomDamageProperty(1600).sword(ModItemTier.STEEL.material, 4f, -1.8f)
) {

}
