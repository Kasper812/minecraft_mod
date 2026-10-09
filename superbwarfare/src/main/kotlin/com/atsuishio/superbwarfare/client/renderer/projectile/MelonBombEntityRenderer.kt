package com.atsuishio.superbwarfare.client.renderer.projectile

import com.atsuishio.portlib.render.legacy.LegacyEntityRenderer

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.entity.projectile.MelonBombEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.atsuishio.portlib.render.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.TntMinecartRenderer
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Blocks

class MelonBombEntityRenderer(context: EntityRendererProvider.Context) : LegacyEntityRenderer<MelonBombEntity>(context) {
    private val blockModelResolver: net.minecraft.client.renderer.block.BlockModelResolver = context.blockModelResolver
    private val melonModel = net.minecraft.client.renderer.block.BlockModelRenderState()

    init {
        this.shadowRadius = 0.2f
    }

    override fun render(
        entity: MelonBombEntity,
        entityYaw: Float,
        partialTicks: Float,
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        matrixStack.pushPose()
        matrixStack.translate(0.0, 0.5, 0.0)
        matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0f))
        matrixStack.translate(-0.5, -0.5, 0.5)
        matrixStack.mulPose(Axis.YP.rotationDegrees(90.0f))
        blockModelResolver.update(melonModel, Blocks.MELON.defaultBlockState(), net.minecraft.client.renderer.entity.TntRenderer.BLOCK_DISPLAY_CONTEXT)
            com.atsuishio.portlib.render.LegacyRenderHelpers.collectorOf(buffer)?.let {
                TntMinecartRenderer.submitWhiteSolidBlock(melonModel, matrixStack, it, packedLight, false, 0)
            }
        matrixStack.popPose()
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight)
    }

    @Suppress("DEPRECATION")
    override fun getTextureLocation(entity: MelonBombEntity): Identifier {
        return TextureAtlas.LOCATION_BLOCKS
    }
}
