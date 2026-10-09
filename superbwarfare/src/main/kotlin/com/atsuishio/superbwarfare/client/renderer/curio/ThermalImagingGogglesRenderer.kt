package com.atsuishio.superbwarfare.client.renderer.curio

import com.atsuishio.superbwarfare.port.*

import net.minecraft.client.renderer.rendertype.RenderTypes

import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.atsuishio.superbwarfare.client.model.trinket.ThermalImagingGogglesModel
import com.atsuishio.superbwarfare.tools.mc
import com.mojang.blaze3d.vertex.PoseStack
import com.atsuishio.superbwarfare.compat.trinkets.SlotReference
import com.atsuishio.superbwarfare.compat.trinkets.TrinketRenderer
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.HumanoidModel
import com.atsuishio.portlib.render.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderType
import com.atsuishio.portlib.render.FoilBuffers
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

class ThermalImagingGogglesRenderer : TrinketRenderer {
    private var model: ThermalImagingGogglesModel? = null

    private fun getModel(): ThermalImagingGogglesModel {
        if (model == null) {
            model = ThermalImagingGogglesModel(mc.entityModels.bakeLayer(ThermalImagingGogglesModel.LAYER_LOCATION))
        }
        return model!!
    }

    override fun render(
        stack: ItemStack,
        slotReference: SlotReference,
        contextModel: EntityModel<out LivingEntityRenderState>,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        state: LivingEntityRenderState,
        entity: LivingEntity,
        partialTick: Float
    ) {
        matrices.pushPose()

        val goggles = getModel()

        if (contextModel is HumanoidModel<*>) {
            goggles.bone.loadPose(contextModel.head.storePose())
        }

        val vertexConsumer = FoilBuffers.getArmorFoilBuffer(
            vertexConsumers,
            RenderTypes.armorCutoutNoCull(TEXTURE),
            stack.hasFoil()
        )

        goggles.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1)
        matrices.popPose()
    }

    companion object {
        private val TEXTURE = loc("textures/trinket/thermal_imaging_goggles.png")
    }
}
