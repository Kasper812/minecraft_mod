package com.atsuishio.superbwarfare.client.renderer.special

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
import com.atsuishio.superbwarfare.tools.OBB
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.LevelRenderer
import org.joml.Quaterniond
import org.joml.Quaternionf

/**
 * Codes based on @AnECanSaiTin's [HitboxAPI](https://github.com/AnECanSaiTin/HitboxAPI)
 */
object OBBRenderer {
    fun render(
        entity: VehicleEntity,
        obbList: MutableList<OBB>,
        poseStack: PoseStack,
        buffer: VertexConsumer,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        pPartialTicks: Float
    ) {
        val position = entity.position()
        for (obb in obbList) {
            val center = obb.center
            val halfExtents = obb.extents
            val rotation = obb.rotation
            if (obb.part == OBB.Part.INTERACTIVE) {
                renderOBB(
                    poseStack, buffer,
                    center.x() - position.x(), center.y() - position.y(), center.z() - position.z(),
                    rotation,
                    halfExtents.x(), halfExtents.y(), halfExtents.z(),
                    1f, 0.8f, 0f, 1f
                )
            } else if (obb.part == OBB.Part.COLLISION) {
                renderOBB(
                    poseStack, buffer,
                    center.x() - position.x(), center.y() - position.y(), center.z() - position.z(),
                    rotation,
                    halfExtents.x(), halfExtents.y(), halfExtents.z(),
                    1f, 0f, 0f, 1f
                )
            } else {
                renderOBB(
                    poseStack, buffer,
                    center.x() - position.x(), center.y() - position.y(), center.z() - position.z(),
                    rotation,
                    halfExtents.x(), halfExtents.y(), halfExtents.z(),
                    red, green, blue, alpha
                )
            }
        }
    }

    fun renderOBB(
        poseStack: PoseStack,
        buffer: VertexConsumer,
        centerX: Double,
        centerY: Double,
        centerZ: Double,
        rotation: Quaterniond,
        halfX: Double,
        halfY: Double,
        halfZ: Double,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        poseStack.pushPose()
        poseStack.translate(centerX, centerY, centerZ)
        poseStack.mulPose(Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w))
        com.atsuishio.portlib.render.LegacyLines.renderLineBox(
            poseStack,
            buffer,
            -halfX,
            -halfY,
            -halfZ,
            halfX,
            halfY,
            halfZ,
            red,
            green,
            blue,
            alpha
        )
        poseStack.popPose()
    }
}

object OBBGizmos {
    private val EDGES = arrayOf(
        intArrayOf(0, 1), intArrayOf(1, 3), intArrayOf(3, 2), intArrayOf(2, 0),
        intArrayOf(4, 5), intArrayOf(5, 7), intArrayOf(7, 6), intArrayOf(6, 4),
        intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
    )

    @JvmStatic
    fun emit(entity: VehicleEntity, partialTicks: Float) {
        val offset = entity.getPosition(partialTicks).subtract(entity.position())
        for (obb in entity.getOBBs()) {
            val color = when (obb.part) {
                OBB.Part.INTERACTIVE -> 0xFFFFCC00.toInt()
                OBB.Part.COLLISION -> 0xFFFF0000.toInt()
                else -> 0xFF00FF00.toInt()
            }
            val center = obb.center
            val ext = obb.extents
            val rot = Quaternionf(obb.rotation.x.toFloat(), obb.rotation.y.toFloat(), obb.rotation.z.toFloat(), obb.rotation.w.toFloat())
            val corners = Array(8) { i ->
                val v = org.joml.Vector3f(
                    (if (i and 1 == 0) -ext.x() else ext.x()).toFloat(),
                    (if (i and 2 == 0) -ext.y() else ext.y()).toFloat(),
                    (if (i and 4 == 0) -ext.z() else ext.z()).toFloat()
                )
                rot.transform(v)
                net.minecraft.world.phys.Vec3(center.x() + v.x + offset.x, center.y() + v.y + offset.y, center.z() + v.z + offset.z)
            }
            for (edge in EDGES) {
                net.minecraft.gizmos.Gizmos.line(corners[edge[0]], corners[edge[1]], color)
            }
        }
    }
}
