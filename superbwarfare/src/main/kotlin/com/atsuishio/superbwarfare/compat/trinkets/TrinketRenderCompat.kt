package com.atsuishio.superbwarfare.compat.trinkets

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.portlib.render.MultiBufferSource
import com.atsuishio.portlib.render.PortRenderStates
import com.atsuishio.portlib.render.SubmitBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import eu.pb4.trinkets.api.TrinketSlotAccess
import eu.pb4.trinkets.api.client.TrinketRendererRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

@Environment(EnvType.CLIENT)
interface TrinketRenderer {
    fun render(
        stack: ItemStack,
        slotReference: SlotReference,
        contextModel: EntityModel<out LivingEntityRenderState>,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        state: LivingEntityRenderState,
        entity: LivingEntity,
        partialTick: Float
    )
}

@Environment(EnvType.CLIENT)
object TrinketRendererCompat {
    @JvmStatic
    fun registerRenderer(item: Item, renderer: TrinketRenderer) {
        TrinketRendererRegistry.registerRenderer(item, object : eu.pb4.trinkets.api.client.TrinketRenderer {
            override fun submit(
                stack: ItemStack,
                access: TrinketSlotAccess,
                model: EntityModel<out LivingEntityRenderState>,
                poseStack: PoseStack,
                collector: SubmitNodeCollector,
                light: Int,
                state: LivingEntityRenderState,
                limbAngle: Float,
                limbDistance: Float
            ) {
                val entity = PortRenderStates.entity(state) as? LivingEntity ?: return
                val buffers = SubmitBufferSource(collector)
                try {
                    renderer.render(
                        stack,
                        SlotReference(access),
                        model,
                        poseStack,
                        buffers,
                        light,
                        state,
                        entity,
                        PortRenderStates.partialTick(state)
                    )
                } finally {
                    buffers.flush()
                }
            }
        })
    }
}
