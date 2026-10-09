package com.atsuishio.superbwarfare.tools

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.config.client.DisplayConfig
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object RenderDistanceHelper {
    private var GUI_RENDER_TIMESTAMP: Long = -1

    private val PONDER_LEVEL: Class<*>? = try {
        Class.forName("net.createmod.ponder.api.level.PonderLevel")
    } catch (_: Throwable) {
        null
    }

    @JvmStatic
    fun markGuiRenderTimestamp() {
        GUI_RENDER_TIMESTAMP = System.currentTimeMillis()
    }

    @JvmStatic
    fun isInGui(): Boolean {
        return System.currentTimeMillis() - GUI_RENDER_TIMESTAMP < 100L
    }

    @JvmStatic
    fun shouldRenderLOD(entity: VehicleEntity, poseStack: PoseStack, distance: Double): Boolean {
        if (isInGui()) return false
        if (PONDER_LEVEL?.isInstance(entity.level()) == true) return false
        val globalLODDistance = try {
            DisplayConfig.VEHICLE_LOD_DISTANCE.get()
        } catch (_: Exception) {
            -1
        }
        if (globalLODDistance < 0) return false
        if (distance < globalLODDistance) return false
        val matrix = poseStack.last().pose()
        val viewDistance = matrix.m30() * matrix.m30() + matrix.m31() * matrix.m31() + matrix.m32() * matrix.m32()
        return viewDistance >= distance * distance
    }
}
