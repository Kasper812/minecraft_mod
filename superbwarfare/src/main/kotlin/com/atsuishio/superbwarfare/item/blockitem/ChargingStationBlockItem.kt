package com.atsuishio.superbwarfare.item.blockitem

import net.minecraft.world.item.Item

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.client.tooltip.component.ChargingStationImageComponent
import com.atsuishio.superbwarfare.config.server.MiscConfig
import com.atsuishio.superbwarfare.init.ModBlocks
import com.atsuishio.superbwarfare.init.ModDataComponents
import com.atsuishio.superbwarfare.init.RegistryName
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.util.*
import javax.annotation.ParametersAreNonnullByDefault
import kotlin.math.max
import kotlin.math.roundToInt

@RegistryName("charging_station")
class ChargingStationBlockItem : BlockItem(ModBlocks.CHARGING_STATION, Properties().stacksTo(1)) {
    override fun isBarVisible(stack: ItemStack): Boolean {
        val energy = stack.getOrDefault(ModDataComponents.ENERGY, 0)
        return energy != MiscConfig.CHARGING_STATION_MAX_ENERGY.get() && energy != 0
    }

    override fun getBarWidth(stack: ItemStack): Int {
        val energy = stack.getOrDefault(ModDataComponents.ENERGY, 0)
        return (energy * 13f / max(1, MiscConfig.CHARGING_STATION_MAX_ENERGY.get())).roundToInt()
    }

    override fun getBarColor(pStack: ItemStack): Int {
        return 0xFFFF00
    }

    override fun getTooltipImage(pStack: ItemStack): Optional<TooltipComponent> {
        return Optional.of(ChargingStationImageComponent(pStack))
    }

    @ParametersAreNonnullByDefault
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        tooltipComponents.add(
            Component.translatable("des.superbwarfare.charging_station").withStyle(ChatFormatting.GRAY)
        )
    }
}
