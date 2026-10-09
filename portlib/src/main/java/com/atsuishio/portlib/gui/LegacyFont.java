package com.atsuishio.portlib.gui;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class LegacyFont {
    private LegacyFont() {
    }

    public static int drawInBatch(Font font, String text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int light) {
        Font.PreparedText prepared = font.prepareText(text, x, y, color, dropShadow, backgroundColor);
        prepared.visit(visitor(pose, buffers, mode, light));
        return (int) x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public static int drawInBatch(Font font, Component text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int light) {
        return drawInBatch(font, text.getVisualOrderText(), x, y, color, dropShadow, pose, buffers, mode, backgroundColor, light);
    }

    public static int drawInBatch(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int backgroundColor, int light) {
        Font.PreparedText prepared = font.prepareText(text, x, y, color, dropShadow, false, backgroundColor);
        prepared.visit(visitor(pose, buffers, mode, light));
        return (int) x + font.width(text) + (dropShadow ? 1 : 0);
    }

    private static Font.GlyphVisitor visitor(Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int light) {
        Matrix4f matrix = new Matrix4f(pose);
        return new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                VertexConsumer consumer = buffers.getBuffer(renderable.renderType(mode));
                renderable.render(matrix, consumer, light, false);
            }
        };
    }
}
