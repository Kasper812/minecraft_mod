package com.atsuishio.superbwarfare.item.misc

import net.minecraft.world.item.Item

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.entity.vehicle.MortarEntity
import com.atsuishio.superbwarfare.init.RegistryName
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import javax.annotation.ParametersAreNonnullByDefault

@RegistryName("mortar_deployer")
class MortarDeployerItem : AbstractDeployerItem(Properties().rarity(Rarity.RARE)) {
    override fun spawnDeployedEntity(
        level: Level,
        player: Player
    ): Entity {
        return MortarEntity(level, player.yRot)
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
            Component.translatable("des.superbwarfare.mortar_deployer").withStyle(ChatFormatting.GRAY)
        )
    }
}
