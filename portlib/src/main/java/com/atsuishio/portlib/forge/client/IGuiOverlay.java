package com.atsuishio.portlib.forge.client;

import com.atsuishio.portlib.gui.GuiGraphics;

@FunctionalInterface
public interface IGuiOverlay {
    void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight);
}
