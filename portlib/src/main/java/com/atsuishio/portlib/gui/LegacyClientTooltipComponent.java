package com.atsuishio.portlib.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public interface LegacyClientTooltipComponent extends ClientTooltipComponent {
    int getHeight();

    @Override
    default int getHeight(Font font) {
        return getHeight();
    }

    default void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
    }

    default void renderText(Font font, int x, int y, GuiGraphics guiGraphics) {
    }

    @Override
    default void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        renderImage(font, x, y, GuiGraphics.of(graphics));
    }

    @Override
    default void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        renderText(font, x, y, GuiGraphics.of(graphics));
    }
}
