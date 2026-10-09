package com.atsuishio.superbwarfare.client.renderer.entity

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance
import com.atsuishio.superbwarfare.entity.vehicle.Tom6Entity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.atsuishio.portlib.render.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.TntMinecartRenderer
import net.minecraft.world.level.block.Blocks

class Tom6Renderer(manager: EntityRendererProvider.Context) : GeoVehicleRenderer<Tom6Entity>(manager) {

    private val blockModelResolver: net.minecraft.client.renderer.block.BlockModelResolver = manager.blockModelResolver
    private val melonModel = net.minecraft.client.renderer.block.BlockModelRenderState()

    override fun renderCustomPart(
        entity: Tom6Entity,
        instance: VehicleModelInstance,
        poseStack: PoseStack,
        entityYaw: Float,
        partialTicks: Float,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        if (entity.hasMelon) {
            poseStack.pushPose()
            poseStack.scale(0.85f, 0.85f, 0.85f)

            poseStack.pushPose()
            poseStack.translate(0.0, 0.7, 0.0)
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f))
            poseStack.translate(-0.5, -0.5, 0.5)
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f))
            blockModelResolver.update(melonModel, Blocks.MELON.defaultBlockState(), net.minecraft.client.renderer.entity.TntRenderer.BLOCK_DISPLAY_CONTEXT)
            com.atsuishio.portlib.render.LegacyRenderHelpers.collectorOf(buffer)?.let {
                TntMinecartRenderer.submitWhiteSolidBlock(melonModel, poseStack, it, packedLight, false, 0)
            }
            poseStack.popPose()
            poseStack.popPose()
        }
    }
}
