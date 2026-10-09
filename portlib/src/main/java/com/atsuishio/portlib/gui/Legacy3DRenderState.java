package com.atsuishio.portlib.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

public record Legacy3DRenderState(
    Legacy3DCallback callback,
    int x0,
    int y0,
    int x1,
    int y1,
    float scale,
    @Nullable ScreenRectangle scissorArea,
    @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public Legacy3DRenderState(Legacy3DCallback callback, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea) {
        this(callback, x0, y0, x1, y1, 1.0F, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
