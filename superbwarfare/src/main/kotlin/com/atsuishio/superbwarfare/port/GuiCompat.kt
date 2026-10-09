package com.atsuishio.superbwarfare.port

import com.atsuishio.portlib.gui.GuiGraphics
import com.atsuishio.portlib.gui.LegacyEditBox
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo
import java.util.function.Predicate

fun GuiEventListener.mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
    mouseClicked(MouseButtonEvent(mouseX, mouseY, MouseButtonInfo(button, 0)), false)

fun GuiEventListener.mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
    mouseReleased(MouseButtonEvent(mouseX, mouseY, MouseButtonInfo(button, 0)))

fun GuiEventListener.mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean =
    mouseDragged(MouseButtonEvent(mouseX, mouseY, MouseButtonInfo(button, 0)), dragX, dragY)

fun GuiEventListener.keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean =
    keyPressed(KeyEvent(keyCode, scanCode, modifiers))

fun GuiEventListener.keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean =
    keyReleased(KeyEvent(keyCode, scanCode, modifiers))

fun GuiEventListener.charTyped(codePoint: Char, modifiers: Int): Boolean =
    charTyped(CharacterEvent(codePoint.code))

fun Renderable.render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) =
    extractRenderState(guiGraphics.extractor(), mouseX, mouseY, partialTick)

fun EditBox.setFilter(filter: Predicate<String>) = (this as LegacyEditBox).`sbw_portlib$setFilter`(filter)

object LegacyItemRenderer {
    fun renderStatic(
        stack: net.minecraft.world.item.ItemStack,
        context: net.minecraft.world.item.ItemDisplayContext,
        light: Int,
        overlay: Int,
        poseStack: com.mojang.blaze3d.vertex.PoseStack,
        buffers: com.atsuishio.portlib.render.MultiBufferSource,
        level: net.minecraft.world.level.Level?,
        seed: Int
    ) = com.atsuishio.portlib.render.LegacyRenderHelpers.renderItemStatic(stack, context, light, overlay, poseStack, buffers, level, seed)

    fun renderStatic(
        entity: net.minecraft.world.entity.LivingEntity?,
        stack: net.minecraft.world.item.ItemStack,
        context: net.minecraft.world.item.ItemDisplayContext,
        leftHand: Boolean,
        poseStack: com.mojang.blaze3d.vertex.PoseStack,
        buffers: com.atsuishio.portlib.render.MultiBufferSource,
        level: net.minecraft.world.level.Level?,
        light: Int,
        overlay: Int,
        seed: Int
    ) = com.atsuishio.portlib.render.LegacyRenderHelpers.renderItemStatic(entity, stack, context, poseStack, buffers, level, light, overlay, seed)
}

val net.minecraft.client.Minecraft.itemRenderer: LegacyItemRenderer get() = LegacyItemRenderer

fun net.minecraft.client.renderer.entity.EntityRenderDispatcher.setRenderShadow(value: Boolean) {
}

fun <E : net.minecraft.world.entity.Entity> net.minecraft.client.renderer.entity.EntityRenderDispatcher.render(
    entity: E,
    x: Double,
    y: Double,
    z: Double,
    yaw: Float,
    partialTick: Float,
    poseStack: com.mojang.blaze3d.vertex.PoseStack,
    buffers: com.atsuishio.portlib.render.MultiBufferSource,
    light: Int
) = com.atsuishio.portlib.render.LegacyRenderHelpers.renderEntity(entity, x, y, z, yaw, partialTick, poseStack, buffers, light)

fun net.minecraft.client.KeyMapping.matchesMouse(button: Int): Boolean =
    matchesMouse(MouseButtonEvent(0.0, 0.0, MouseButtonInfo(button, 0)))

fun net.minecraft.client.KeyMapping.matches(keyCode: Int, scanCode: Int): Boolean =
    matches(KeyEvent(keyCode, scanCode, 0))

fun net.minecraft.client.Minecraft.renderBuffers(): com.atsuishio.portlib.render.LegacyRenderBuffers =
    com.atsuishio.portlib.render.LegacyRenderBuffers.INSTANCE

fun net.minecraft.client.renderer.entity.EntityRenderDispatcher.cameraOrientation(): org.joml.Quaternionf =
    org.joml.Quaternionf(net.minecraft.client.Minecraft.getInstance().gameRenderer.mainCamera().rotation())

fun net.minecraft.client.renderer.entity.EntityRenderDispatcher.shouldRenderHitBoxes(): Boolean =
    net.minecraft.client.Minecraft.getInstance().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES)

fun net.minecraft.client.gui.Font.drawInBatch(
    text: String, x: Float, y: Float, color: Int, dropShadow: Boolean, pose: org.joml.Matrix4f,
    buffers: com.atsuishio.portlib.render.MultiBufferSource, mode: net.minecraft.client.gui.Font.DisplayMode, backgroundColor: Int, light: Int
): Int = com.atsuishio.portlib.gui.LegacyFont.drawInBatch(this, text, x, y, color, dropShadow, pose, buffers, mode, backgroundColor, light)

fun net.minecraft.client.gui.Font.drawInBatch(
    text: net.minecraft.network.chat.Component, x: Float, y: Float, color: Int, dropShadow: Boolean, pose: org.joml.Matrix4f,
    buffers: com.atsuishio.portlib.render.MultiBufferSource, mode: net.minecraft.client.gui.Font.DisplayMode, backgroundColor: Int, light: Int
): Int = com.atsuishio.portlib.gui.LegacyFont.drawInBatch(this, text, x, y, color, dropShadow, pose, buffers, mode, backgroundColor, light)

fun net.minecraft.client.gui.Font.drawInBatch(
    text: net.minecraft.util.FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean, pose: org.joml.Matrix4f,
    buffers: com.atsuishio.portlib.render.MultiBufferSource, mode: net.minecraft.client.gui.Font.DisplayMode, backgroundColor: Int, light: Int
): Int = com.atsuishio.portlib.gui.LegacyFont.drawInBatch(this, text, x, y, color, dropShadow, pose, buffers, mode, backgroundColor, light)

fun renderScrollingString(
    guiGraphics: GuiGraphics, font: net.minecraft.client.gui.Font, text: net.minecraft.network.chat.Component,
    minX: Int, minY: Int, maxX: Int, maxY: Int, color: Int
) {
    val message = if (color and 0xFFFFFF == 0xFFFFFF) text else text.copy().withColor(color and 0xFFFFFF)
    guiGraphics.extractor().textRenderer().acceptScrollingWithDefaultCenter(message, minX, maxX, minY, maxY)
}

fun net.minecraft.client.gui.components.MultiLineLabel.renderLeftAligned(guiGraphics: GuiGraphics, x: Int, y: Int, lineHeight: Int, color: Int): Int =
    visitLines(net.minecraft.client.gui.TextAlignment.LEFT, x, y, lineHeight, guiGraphics.extractor().textRenderer())

fun net.minecraft.client.gui.components.MultiLineLabel.renderCentered(guiGraphics: GuiGraphics, x: Int, y: Int, lineHeight: Int, color: Int): Int =
    visitLines(net.minecraft.client.gui.TextAlignment.CENTER, x, y, lineHeight, guiGraphics.extractor().textRenderer())

fun net.minecraft.client.gui.components.MultiLineLabel.renderCentered(guiGraphics: GuiGraphics, x: Int, y: Int): Int =
    visitLines(net.minecraft.client.gui.TextAlignment.CENTER, x, y, 9, guiGraphics.extractor().textRenderer())
