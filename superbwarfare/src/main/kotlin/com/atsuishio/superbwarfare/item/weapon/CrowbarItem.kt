package com.atsuishio.superbwarfare.item.weapon

import net.minecraft.world.item.Item

import net.minecraft.world.item.TooltipFlag

import net.minecraft.world.item.ItemStack

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.item.IVehicleInteract
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity.RemovalReason
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block

private val TIER = ToolMaterial(
    BlockTags.INCORRECT_FOR_IRON_TOOL,
    400,
    4f,
    3.5f,
    9,
    ItemTags.IRON_TOOL_MATERIALS
)

@RegistryName("crowbar")
open class CrowbarItem : Item(
    Properties().stacksTo(1)
        .sword(TIER, 2f, -2f)
        .attributes(
            ItemAttributeModifiers.builder()
                .add(
                    Attributes.ATTACK_DAMAGE,
                    AttributeModifier(BASE_ATTACK_DAMAGE_ID, 2.0 + 3.5, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
                )
                .add(
                    Attributes.ATTACK_SPEED,
                    AttributeModifier(BASE_ATTACK_SPEED_ID, -2.0, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
                )
                .add(
                    Attributes.BLOCK_INTERACTION_RANGE,
                    AttributeModifier(Mod.ATTRIBUTE_MODIFIER, 3.0, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
                )
                .build()
        )
), IVehicleInteract {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        tooltipComponents.add(Component.translatable("des.superbwarfare.crowbar").withStyle(ChatFormatting.GRAY))
        tooltipComponents.add(Component.translatable("des.superbwarfare.crowbar_2").withStyle(ChatFormatting.GRAY))
    }

    override fun onInteractVehicle(
        vehicle: VehicleEntity,
        stack: ItemStack,
        player: Player,
        hand: InteractionHand
    ): InteractionResult? {
        return crowbarInteract(vehicle, stack, player, hand)
    }

    companion object {
        @JvmStatic
        fun crowbarInteract(
            vehicle: VehicleEntity,
            stack: ItemStack,
            player: Player,
            hand: InteractionHand
        ): InteractionResult? {
            if (!player.isShiftKeyDown || vehicle.passengers.isNotEmpty()) return null
            if (vehicle.isWreck) {
                return InteractionResult.PASS
            } else {
                vehicle.clearTowingInfo()
                for (item in vehicle.getRetrieveItems()) {
                    player.inventory.placeItemBackInInventory(item)
                }
                vehicle.remove(RemovalReason.DISCARDED)
                vehicle.discard()
                return InteractionResult.SUCCESS
            }
        }
    }
}
