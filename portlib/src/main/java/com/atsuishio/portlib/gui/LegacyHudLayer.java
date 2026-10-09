package com.atsuishio.portlib.gui;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface LegacyHudLayer extends HudElement {
    float Z_SEPARATION = 0.0F;

    void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker);

    @Override
    default void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        LegacyRenderSystem.resetState();
        this.render(GuiGraphics.of(graphics), deltaTracker);
        LegacyRenderSystem.resetState();
    }
}
