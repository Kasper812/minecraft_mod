package com.atsuishio.superbwarfare.compat.jade.elements

import com.atsuishio.superbwarfare.Mod.Companion.loc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import snownee.jade.api.theme.IThemeHelper
import snownee.jade.api.ui.Element
import snownee.jade.overlay.DisplayHelper

class WrenchHealthElement(maxHealth: Float, health: Float) : Element() {
    private val text: String = String.format(
        "  %s/%s",
        DisplayHelper.dfCommas.format(health.toDouble()),
        DisplayHelper.dfCommas.format(maxHealth.toDouble())
    )

    init {
        val font = Minecraft.getInstance().font
        size(8 + font.width(this.text), 10)
    }

    override fun getNarration(): Component = Component.literal(text.trim())

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val x = getX()
        val y = getY()
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WRENCH_ICON, x + 2, y, 0f, 0f, 8, 8, 8, 8)
        guiGraphics.text(Minecraft.getInstance().font, this.text, x + 6, y, IThemeHelper.get().normalColor, false)
    }

    companion object {
        private val WRENCH_ICON = loc("textures/overlay/vehicle/jade/vehicle_health.png")
    }
}
