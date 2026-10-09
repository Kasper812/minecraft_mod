package com.atsuishio.superbwarfare.item.material

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.init.RegistryName
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import javax.annotation.ParametersAreNonnullByDefault

@RegistryName("vehicle_reset_kit")
class VehicleResetKitItem : Item(Properties().rarity(Rarity.UNCOMMON).stacksTo(1)) {
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
            Component.translatable("des.superbwarfare.vehicle_reset_kit_1").withStyle(ChatFormatting.AQUA)
        )
        tooltipComponents.add(
            Component.translatable("des.superbwarfare.vehicle_reset_kit_2").withStyle(ChatFormatting.GRAY)
        )
    }

    companion object {
        @JvmStatic
        fun getCraftingRemainingItem(itemstack: ItemStack): ItemStack {
            return itemstack.copy()
        }
    }
}
