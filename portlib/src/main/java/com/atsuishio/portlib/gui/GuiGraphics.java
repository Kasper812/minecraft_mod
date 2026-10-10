package com.atsuishio.portlib.gui;

import com.atsuishio.portlib.render.RecordedGeometry;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.FormattedText;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class GuiGraphics {
    private static final Map<GuiGraphicsExtractor, GuiGraphics> CACHE = new WeakHashMap<>();

    private final GuiGraphicsExtractor extractor;
    private final LegacyPose pose;

    public GuiGraphics(GuiGraphicsExtractor extractor) {
        this.extractor = extractor;
        this.pose = new LegacyPose(extractor.pose());
    }

    public static GuiGraphics of(GuiGraphicsExtractor extractor) {
        synchronized (CACHE) {
            return CACHE.computeIfAbsent(extractor, GuiGraphics::new);
        }
    }

    public GuiGraphicsExtractor extractor() {
        return extractor;
    }

    public LegacyPose pose() {
        return pose;
    }

    public int guiWidth() {
        return extractor.guiWidth();
    }

    public int guiHeight() {
        return extractor.guiHeight();
    }

    public void flush() {
    }

    public void setColor(float r, float g, float b, float a) {
        LegacyRenderSystem.setShaderColor(r, g, b, a);
    }

    public void enableScissor(int x0, int y0, int x1, int y1) {
        extractor.enableScissor(x0, y0, x1, y1);
    }

    public void disableScissor() {
        extractor.disableScissor();
    }

    private static int textColor(int color) {
        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }
        return LegacyRenderSystem.tint(color);
    }

    public int drawString(Font font, @Nullable String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        }
        extractor.text(font, text, x, y, textColor(color), dropShadow);
        return x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        }
        extractor.pose().pushMatrix();
        extractor.pose().translate(x, y);
        extractor.text(font, text, 0, 0, textColor(color), dropShadow);
        extractor.pose().popMatrix();
        return (int) x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        extractor.text(font, text, x, y, textColor(color), dropShadow);
        return x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        extractor.pose().pushMatrix();
        extractor.pose().translate(x, y);
        extractor.text(font, text, 0, 0, textColor(color), dropShadow);
        extractor.pose().popMatrix();
        return (int) x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean dropShadow) {
        extractor.text(font, text, x, y, textColor(color), dropShadow);
        return x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public int drawString(Font font, Component text, float x, float y, int color, boolean dropShadow) {
        extractor.pose().pushMatrix();
        extractor.pose().translate(x, y);
        extractor.text(font, text, 0, 0, textColor(color), dropShadow);
        extractor.pose().popMatrix();
        return (int) x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        drawString(font, text, x - font.width(text) / 2, y, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        FormattedCharSequence seq = text.getVisualOrderText();
        drawString(font, seq, x - font.width(seq) / 2, y, color);
    }

    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color) {
        drawString(font, text, x - font.width(text) / 2, y, color);
    }

    public void drawWordWrap(Font font, FormattedText text, int x, int y, int width, int color) {
        extractor.textWithWordWrap(font, text, x, y, width, textColor(color));
    }

    public void hLine(int x0, int x1, int y, int color) {
        if (x1 < x0) {
            int t = x0;
            x0 = x1;
            x1 = t;
        }
        fill(x0, y, x1 + 1, y + 1, color);
    }

    public void vLine(int x, int y0, int y1, int color) {
        if (y1 < y0) {
            int t = y0;
            y0 = y1;
            y1 = t;
        }
        fill(x, y0 + 1, x + 1, y1, color);
    }

    public void fill(int x0, int y0, int x1, int y1, int color) {
        fillFloat(x0, y0, x1, y1, color);
    }

    public void fill(int x0, int y0, int x1, int y1, int z, int color) {
        fillFloat(x0, y0, x1, y1, color);
    }

    public void fillFloat(float x0, float y0, float x1, float y1, int color) {
        if (x0 > x1) {
            float t = x0;
            x0 = x1;
            x1 = t;
        }
        if (y0 > y1) {
            float t = y0;
            y0 = y1;
            y1 = t;
        }
        int c = LegacyRenderSystem.tint(color);
        RecordedGeometry geometry = new RecordedGeometry();
        Matrix3x2f m = new Matrix3x2f(extractor.pose());
        vertexColor(geometry, m, x0, y0, c);
        vertexColor(geometry, m, x0, y1, c);
        vertexColor(geometry, m, x1, y1, c);
        vertexColor(geometry, m, x1, y0, c);
        submit(LegacyRenderSystem.isAdditive() ? LegacyGuiPipelines.GUI_ADDITIVE : RenderPipelines.GUI, TextureSetup.noTexture(), geometry);
    }

    public void fillGradient(int x0, int y0, int x1, int y1, int colorFrom, int colorTo) {
        extractor.fillGradient(x0, y0, x1, y1, LegacyRenderSystem.tint(colorFrom), LegacyRenderSystem.tint(colorTo));
    }

    public void renderOutline(int x, int y, int width, int height, int color) {
        extractor.outline(x, y, width, height, LegacyRenderSystem.tint(color));
    }

    public void blit(Identifier texture, int x, int y, int u, int v, int width, int height) {
        blit(texture, x, y, (float) u, (float) v, width, height, 256, 256);
    }

    public void blit(Identifier texture, int x, int y, int blitOffset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void blit(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    public void blit(Identifier texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        innerBlit(texture, x, x + width, y, y + height,
                u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight);
    }

    public void blit(Identifier texture, int x0, int x1, int y0, int y1, int blitOffset, int uWidth, int vHeight, float u, float v, int textureWidth, int textureHeight) {
        innerBlit(texture, x0, x1, y0, y1, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight);
    }

    public void innerBlit(Identifier texture, float x0, float x1, float y0, float y1, float u0, float u1, float v0, float v1) {
        innerBlit(texture, x0, x1, y0, y1, u0, u1, v0, v1, LegacyRenderSystem.shaderColorArgb());
    }

    public void innerBlit(Identifier texture, float x0, float x1, float y0, float y1, float u0, float u1, float v0, float v1, int color) {
        RecordedGeometry geometry = new RecordedGeometry();
        Matrix3x2f m = new Matrix3x2f(extractor.pose());
        vertexTex(geometry, m, x0, y0, u0, v0, color);
        vertexTex(geometry, m, x0, y1, u0, v1, color);
        vertexTex(geometry, m, x1, y1, u1, v1, color);
        vertexTex(geometry, m, x1, y0, u1, v0, color);
        submit(LegacyRenderSystem.isAdditive() ? LegacyGuiPipelines.GUI_TEXTURED_ADDITIVE : RenderPipelines.GUI_TEXTURED, textureSetup(texture), geometry);
    }

    public void blitSprite(Identifier sprite, int x, int y, int width, int height) {
        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, LegacyRenderSystem.shaderColorArgb());
    }

    public void renderItem(ItemStack stack, int x, int y) {
        extractor.item(stack, x, y);
    }

    public void renderItem(ItemStack stack, int x, int y, int seed) {
        extractor.item(stack, x, y, seed);
    }

    public void renderItem(LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        extractor.item(entity, stack, x, y, seed);
    }

    public void renderFakeItem(ItemStack stack, int x, int y) {
        extractor.fakeItem(stack, x, y);
    }

    public void renderFakeItem(ItemStack stack, int x, int y, int seed) {
        extractor.fakeItem(stack, x, y, seed);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        extractor.itemDecorations(font, stack, x, y);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, @Nullable String text) {
        extractor.itemDecorations(font, stack, x, y, text);
    }

    public void renderTooltip(Font font, Component text, int x, int y) {
        extractor.setTooltipForNextFrame(font, text, x, y);
    }

    public void renderTooltip(Font font, ItemStack stack, int x, int y) {
        extractor.setTooltipForNextFrame(font, stack, x, y);
    }

    public void renderTooltip(Font font, List<Component> lines, Optional<TooltipComponent> image, int x, int y) {
        extractor.setTooltipForNextFrame(font, lines, image, x, y);
    }

    public void renderTooltip(Font font, List<? extends FormattedCharSequence> lines, int x, int y) {
        extractor.setTooltipForNextFrame(font, lines, x, y);
    }

    public void renderComponentTooltip(Font font, List<Component> lines, int x, int y) {
        extractor.setComponentTooltipForNextFrame(font, lines, x, y);
    }

    public LegacyGuiBuffer begin(Mode mode, boolean textured) {
        return new LegacyGuiBuffer(this, mode, textured);
    }

    void submitBuffer(LegacyGuiBuffer buffer) {
        RenderPipeline pipeline;
        if (buffer.textured) {
            pipeline = switch (buffer.mode) {
                case TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> LegacyGuiPipelines.GUI_TEXTURED_TRIANGLES;
                default -> LegacyRenderSystem.isAdditive() ? LegacyGuiPipelines.GUI_TEXTURED_ADDITIVE : RenderPipelines.GUI_TEXTURED;
            };
            Identifier texture = LegacyRenderSystem.getShaderTexture();
            if (texture == null) {
                return;
            }
            submit(pipeline, textureSetup(texture), buffer.geometry());
        } else {
            pipeline = switch (buffer.mode) {
                case TRIANGLES -> LegacyGuiPipelines.GUI_TRIANGLES;
                case TRIANGLE_STRIP -> LegacyGuiPipelines.GUI_TRIANGLE_STRIP;
                case TRIANGLE_FAN -> LegacyGuiPipelines.GUI_TRIANGLE_FAN;
                default -> LegacyRenderSystem.isAdditive() ? LegacyGuiPipelines.GUI_ADDITIVE : RenderPipelines.GUI;
            };
            submit(pipeline, TextureSetup.noTexture(), buffer.geometry());
        }
    }

    private static TextureSetup textureSetup(Identifier texture) {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
        return TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler());
    }

    private static void vertexColor(RecordedGeometry geometry, Matrix3x2f m, float x, float y, int color) {
        Vector2f p = m.transformPosition(x, y, new Vector2f());
        geometry.addVertex(p.x, p.y, 0.0F).setColor(color);
    }

    private static void vertexTex(RecordedGeometry geometry, Matrix3x2f m, float x, float y, float u, float v, int color) {
        Vector2f p = m.transformPosition(x, y, new Vector2f());
        geometry.addVertex(p.x, p.y, 0.0F).setUv(u, v).setColor(color);
    }

    void submit(RenderPipeline pipeline, TextureSetup textureSetup, RecordedGeometry geometry) {
        if (geometry.isEmpty()) {
            return;
        }
        geometry.fillMissingColor(LegacyRenderSystem.shaderColorArgb());
        ScreenRectangle scissor = extractor.scissorStack.peek();
        float[] b = geometry.bounds2d();
        ScreenRectangle bounds = new ScreenRectangle((int) Math.floor(b[0]), (int) Math.floor(b[1]),
                Math.max(1, (int) Math.ceil(b[2] - Math.floor(b[0]))), Math.max(1, (int) Math.ceil(b[3] - Math.floor(b[1]))));
        if (scissor != null) {
            bounds = scissor.intersection(bounds);
            if (bounds == null) {
                return;
            }
        }
        extractor.guiRenderState.addGuiElement(new LegacyGuiElement(pipeline, textureSetup, geometry, scissor, bounds));
    }

    public enum Mode {
        QUADS, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, LINES
    }

    public static final class LegacyGuiBuffer implements VertexConsumer {
        private final GuiGraphics owner;
        final Mode mode;
        final boolean textured;
        private final RecordedGeometry geometry = new RecordedGeometry();

        LegacyGuiBuffer(GuiGraphics owner, Mode mode, boolean textured) {
            this.owner = owner;
            this.mode = mode;
            this.textured = textured;
        }

        RecordedGeometry geometry() {
            return geometry;
        }

        public void end() {
            owner.submitBuffer(this);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            geometry.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            geometry.setColor(LegacyRenderSystem.tint(a << 24 | r << 16 | g << 8 | b));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            geometry.setColor(LegacyRenderSystem.tint(color));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            geometry.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }

    public void render3D(int x0, int y0, int x1, int y1, Legacy3DCallback callback) {
        extractor.guiRenderState.addPicturesInPictureState(new Legacy3DRenderState(callback, x0, y0, x1, y1, extractor.scissorStack.peek()));
    }
}
