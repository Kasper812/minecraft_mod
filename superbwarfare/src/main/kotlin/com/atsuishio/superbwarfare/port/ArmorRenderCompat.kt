package com.atsuishio.superbwarfare.port

import com.atsuishio.portlib.render.PortRenderStates
import com.github.mcmodderanchor.simplebedrockmodel.v2.client.renderer.GeoArmorRendererV2
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ItemLike

@Environment(EnvType.CLIENT)
object ArmorRenderCompat {
    @JvmStatic
    fun register(item: ItemLike, factory: (EquipmentSlot) -> GeoArmorRendererV2?) {
        var cached: GeoArmorRendererV2? = null
        ArmorRenderer.register(
            ArmorRenderer { poseStack, collector, stack, state, slot, light, contextModel ->
                val renderer = cached ?: factory(slot)?.also { cached = it } ?: return@ArmorRenderer
                val entity = PortRenderStates.entity(state) as? LivingEntity
                renderer.preparePose(entity, stack, slot, contextModel)
                renderer.young = state.isBaby
                renderer.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, -1)
            },
            item
        )
    }
}
