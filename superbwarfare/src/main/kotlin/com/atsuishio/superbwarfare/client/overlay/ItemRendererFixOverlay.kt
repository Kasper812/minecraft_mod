package com.atsuishio.superbwarfare.client.overlay

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.item.gun.GunItem
import net.minecraft.world.item.ItemDisplayContext
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

/**
 * 这个类的作用是在看不见的地方渲染一个第三人称的武器模型，别管为啥这么干
 * 反正删了这个绝对会出事
 */
@Environment(EnvType.CLIENT)
object ItemRendererFixOverlay : CommonOverlay("item_renderer_fix") {

    override fun RenderContext.render() {
        val stack = player.mainHandItem
        // 手持副武器时按普通物品处理
        if (!GunItem.isHeldWeapon(stack)) return

        val renderer = com.atsuishio.portlib.render.BuiltinItemRendererRegistry.INSTANCE.get(stack) ?: return
        val poseStack = com.mojang.blaze3d.vertex.PoseStack()
        poseStack.translate(-1145f, 0f, 0f)
        renderer.render(
            stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, poseStack,
            com.atsuishio.portlib.render.DiscardBufferSource.INSTANCE, 0,
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
        )
    }
}
