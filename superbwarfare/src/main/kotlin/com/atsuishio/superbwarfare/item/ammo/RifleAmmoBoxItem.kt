package com.atsuishio.superbwarfare.item.ammo

import net.minecraft.world.item.Item

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.data.gun.Ammo
import com.atsuishio.superbwarfare.init.RegistryName
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

@RegistryName("rifle_ammo_box")
class RifleAmmoBoxItem : AmmoSupplierItem(Ammo.RIFLE, 30, Properties()) {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag)
        tooltipComponents.add(Component.translatable("des.superbwarfare.rifle_ammo_box").withStyle(ChatFormatting.GRAY))
    }
}
