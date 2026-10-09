package com.atsuishio.superbwarfare.item.weapon

import com.atsuishio.superbwarfare.port.*
import com.atsuishio.superbwarfare.item.CustomDamageProperty

import com.atsuishio.superbwarfare.init.RegistryName
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ToolMaterial

@RegistryName("netherite_hammer")
class NetheriteHammerItem : HammerItem(
    ToolMaterial.NETHERITE,
    75,
    -3.5f,
    CustomDamageProperty(false).fireResistant()
)
