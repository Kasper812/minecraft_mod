package com.atsuishio.superbwarfare.item.material

import com.atsuishio.superbwarfare.port.*

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag

class ShortcutPackItem : Item(Properties().rarity(Rarity.EPIC)) {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        tooltipComponents.add(
            Component.translatable("des.superbwarfare.shortcut_pack_2").withStyle(ChatFormatting.AQUA)
        )
        tooltipComponents.add(
            Component.translatable("des.superbwarfare.shortcut_pack_1").withStyle(ChatFormatting.GRAY)
        )
    }
}
