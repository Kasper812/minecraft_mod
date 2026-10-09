package com.atsuishio.superbwarfare.client.renderer.curio

import com.atsuishio.superbwarfare.port.*

import net.minecraft.client.renderer.rendertype.RenderTypes

import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.atsuishio.superbwarfare.client.model.trinket.ParachuteModel
import com.atsuishio.superbwarfare.item.trinket.ParachuteItem
import com.atsuishio.superbwarfare.tools.getOrCreateTag
import com.atsuishio.superbwarfare.tools.mc
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.atsuishio.superbwarfare.compat.trinkets.SlotReference
import com.atsuishio.superbwarfare.compat.trinkets.TrinketRenderer
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import com.atsuishio.portlib.render.SubmitBufferSource
import net.minecraft.client.CameraType
import net.minecraft.client.model.EntityModel
import com.atsuishio.portlib.render.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderType
import com.atsuishio.portlib.render.FoilBuffers
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

class ParachuteRenderer : TrinketRenderer {
    private var model: ParachuteModel? = null

    private fun getModel(): ParachuteModel {
        if (model == null) {
            model = ParachuteModel(mc.entityModels.bakeLayer(ParachuteModel.LAYER_LOCATION))
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
        matrices.scale(0.5f, 0.5f, 0.5f)
        matrices.translate(0.0, 1.25, 0.0)

        if (stack.getOrCreateTag().getBooleanOr(ParachuteItem.TAG_OPEN, false)) {
            val parachute = getModel()

            val vertexConsumer = FoilBuffers.getArmorFoilBuffer(
                vertexConsumers,
                RenderTypes.armorCutoutNoCull(TEXTURE),
                stack.hasFoil()
            )

            parachute.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1)
        }

        matrices.popPose()
    }

    companion object {
        private var firstPersonModel: ParachuteModel? = null
        private val TEXTURE = loc("textures/trinket/parachute.png")

        @JvmStatic
        fun onRenderLevelStage() {
            LevelRenderEvents.COLLECT_SUBMITS.register { context ->
                val player = mc.player ?: return@register

                if (!ParachuteItem.isParachuteOpen(player)) return@register
                if (!ParachuteItem.isParachuteVisible(player)) return@register
                if (mc.options.cameraType != CameraType.FIRST_PERSON) return@register

                val stack = context.poseStack()
                stack.pushPose()

                if (firstPersonModel == null) {
                    firstPersonModel = ParachuteModel(mc.entityModels.bakeLayer(ParachuteModel.LAYER_LOCATION))
                }

                val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(false)
                stack.mulPose(Axis.XP.rotationDegrees(180f))
                stack.mulPose(Axis.YP.rotationDegrees(player.getViewYRot(tickDelta)))
                stack.translate(0.0, 1.5, 0.0)

                val buffers = SubmitBufferSource(context.submitNodeCollector())
                firstPersonModel!!.renderToBuffer(
                    stack,
                    buffers.getBuffer(RenderTypes.armorCutoutNoCull(TEXTURE)),
                    0xFFFFFF,
                    OverlayTexture.NO_OVERLAY,
                    -1
                )

                buffers.flush()
                stack.popPose()
            }
        }
    }
}
