package com.atsuishio.superbwarfare.item.misc

import com.atsuishio.portlib.item.LegacyInventoryTicker
import net.minecraft.world.InteractionResult

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.capability.PersistentDataAccessor
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity
import com.atsuishio.superbwarfare.event.ClientEventHandler
import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.network.message.receive.ResetCameraTypeMessage
import com.atsuishio.superbwarfare.tools.EntityFindUtil
import com.atsuishio.superbwarfare.tools.FormatTool.format1D
import com.atsuishio.superbwarfare.tools.NBTTool
import com.atsuishio.superbwarfare.tools.mc
import com.atsuishio.superbwarfare.tools.sendPacket
import net.minecraft.ChatFormatting
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import javax.annotation.ParametersAreNonnullByDefault

@RegistryName("monitor")
open class MonitorItem : Item(Properties().stacksTo(1).attackAttributes(2.0, -2.4)), LegacyInventoryTicker {
    private fun resetDroneData(drone: DroneEntity?) {
        if (drone == null) return

        val data = (drone as PersistentDataAccessor).`superbwarfare$getPersistentData`()
        data.putBoolean("left", false)
        data.putBoolean("right", false)
        data.putBoolean("forward", false)
        data.putBoolean("backward", false)
        data.putBoolean("up", false)
        data.putBoolean("down", false)
    }

    @ParametersAreNonnullByDefault
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.mainHandItem
        val tag = NBTTool.getTag(stack)

        if (!tag.getBooleanOr(LINKED, false)) {
            return super.use(level, player, hand)
        }

        if (tag.getBooleanOr("Using", false)) {
            tag.putBoolean("Using", false)
            if (level.isClientSide) {
                val lastCameraType = ClientEventHandler.lastCameraType
                if (lastCameraType != null) {
                    Minecraft.getInstance().options.cameraType = lastCameraType
                }
            }
        } else {
            tag.putBoolean("Using", true)
            if (level.isClientSide) {
                ClientEventHandler.lastCameraType = Minecraft.getInstance().options.cameraType
                Minecraft.getInstance().options.cameraType = CameraType.THIRD_PERSON_BACK
            }
        }

        NBTTool.saveTag(stack, tag)
        val drone = EntityFindUtil.findDrone(
            player.level(),
            tag.getStringOr(LINKED_DRONE, "")
        )
        this.resetDroneData(drone)

        return super.use(level, player, hand)
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
        val tag = NBTTool.getTag(stack)
        if (!tag.contains(LINKED_DRONE) || tag.getStringOr(LINKED_DRONE, "") == "none") return

        val player: Player? = Minecraft.getInstance().player
        if (player == null) return

        if (!tag.contains("PosX") || !tag.contains("PosY") || !tag.contains("PosZ")) return

        val droneVec = Vec3(tag.getDoubleOr("PosX", 0.0), tag.getDoubleOr("PosY", 0.0), tag.getDoubleOr("PosZ", 0.0))

        tooltipComponents.add(
            Component.translatable(
                "des.superbwarfare.monitor",
                format1D(player.position().distanceTo(droneVec), "m")
            ).withStyle(ChatFormatting.GRAY)
        )
        tooltipComponents.add(
            Component.literal(
                "X: " + format1D(droneVec.x) +
                        " Y: " + format1D(droneVec.y) +
                        " Z: " + format1D(droneVec.z)
            )
        )
    }

    @ParametersAreNonnullByDefault
    override fun legacyInventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        val tag = NBTTool.getTag(stack)
        val drone = EntityFindUtil.findDrone(
            entity.level(),
            tag.getStringOr(LINKED_DRONE, "")
        )

        val lastCameraType = ClientEventHandler.lastCameraType
        if (!selected) {
            if (tag.getBooleanOr("Using", false)) {
                tag.putBoolean("Using", false)
                NBTTool.saveTag(stack, tag)
                if (entity.level().isClientSide) {
                    if (lastCameraType != null) {
                        mc.options.cameraType = lastCameraType
                    }
                }
            }
            this.resetDroneData(drone)
        } else if (drone == null) {
            if (tag.getBooleanOr("Using", false)) {
                tag.putBoolean("Using", false)
                NBTTool.saveTag(stack, tag)
                if (entity.level().isClientSide) {
                    if (lastCameraType != null) {
                        mc.options.cameraType = lastCameraType
                    }
                }
            }
        }
    }

    companion object {
        const val LINKED: String = "Linked"
        const val LINKED_DRONE: String = "LinkedDrone"
        const val USING: String = "Using"

        @JvmStatic
        fun link(tag: CompoundTag, id: String) {
            tag.putBoolean(LINKED, true)
            tag.putString(LINKED_DRONE, id)
        }

        @JvmStatic
        fun disLink(tag: CompoundTag, player: Player?) {
            tag.putBoolean(LINKED, false)
            tag.putString(LINKED_DRONE, "none")
            if (player is ServerPlayer) {
                player.sendPacket(ResetCameraTypeMessage)
            }
        }

        @JvmStatic
        fun getDronePos(stack: ItemStack, vec3: Vec3) {
            val tag = NBTTool.getTag(stack)
            tag.putDouble("PosX", vec3.x)
            tag.putDouble("PosY", vec3.y)
            tag.putDouble("PosZ", vec3.z)
            NBTTool.saveTag(stack, tag)
        }
    }
}
