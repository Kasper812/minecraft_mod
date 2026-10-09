package com.atsuishio.superbwarfare.item.gun.launcher

import net.minecraft.world.item.Item

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.client.TooltipTool.addHideText
import com.atsuishio.superbwarfare.init.ModRarities
import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.item.gun.GeoGunItemV2
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

@RegistryName("secondary_cataclysm")
object SecondaryCataclysmItem : GeoGunItemV2(Properties().rarity(ModRarities.VIRTUAL)) {

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        tooltipComponents.add(Component.empty())
        tooltipComponents.add(
            Component.translatable("des.superbwarfare.secondary_cataclysm_1").withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC)
        )

        addHideText(tooltipComponents, Component.empty())
        addHideText(
            tooltipComponents,
            Component.translatable("des.superbwarfare.trachelium_3").withStyle(ChatFormatting.WHITE)
        )
        addHideText(
            tooltipComponents,
            Component.translatable("des.superbwarfare.secondary_cataclysm_2").withStyle(Style.EMPTY.withColor(0x68B9F6))
        )
    }
}
