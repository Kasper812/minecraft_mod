package com.atsuishio.portlib.gui;

import net.minecraft.client.input.CharacterEvent;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.input.MouseButtonInfo;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class LegacyScreen extends Screen {
    protected LegacyScreen(Component title) {
        super(title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    private GuiGraphics sbw_portlib$currentGraphics;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        LegacyRenderSystem.resetState();
        this.sbw_portlib$currentGraphics = GuiGraphics.of(graphics);
        this.render(this.sbw_portlib$currentGraphics, mouseX, mouseY, a);
        LegacyRenderSystem.resetState();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.extractor().nextStratum();
        super.extractRenderState(guiGraphics.extractor(), mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics.extractor(), mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        this.renderBackground(guiGraphics, 0, 0, 0.0F);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.mouseScrolled(mouseX, mouseY, scrollY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, 0.0, delta);
    }

    public void renderTransparentBackground(GuiGraphics guiGraphics) {
        this.extractTransparentBackground(guiGraphics.extractor());
    }

    public static boolean hasShiftDown() {
        return LegacyInput.hasShiftDown();
    }

    public static boolean hasControlDown() {
        return LegacyInput.hasControlDown();
    }

    public static boolean hasAltDown() {
        return LegacyInput.hasAltDown();
    }

    private int sbw_portlib$modifiers;
    private boolean sbw_portlib$doubleClick;

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.sbw_portlib$modifiers = event.modifiers();
        this.sbw_portlib$doubleClick = doubleClick;
        return this.mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, this.sbw_portlib$modifiers)), this.sbw_portlib$doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.sbw_portlib$modifiers = event.modifiers();
        return this.mouseReleased(event.x(), event.y(), event.button());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, this.sbw_portlib$modifiers)));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        this.sbw_portlib$modifiers = event.modifiers();
        return this.mouseDragged(event.x(), event.y(), event.button(), dx, dy);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, this.sbw_portlib$modifiers)), dragX, dragY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return this.keyReleased(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (Character.isBmpCodePoint(event.codepoint())) {
            return this.charTyped((char) event.codepoint(), 0);
        }
        return super.charTyped(event);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(new CharacterEvent(codePoint));
    }

    public void renderBlurredBackground(GuiGraphics guiGraphics) {
        this.extractBlurredBackground(guiGraphics.extractor());
    }

    public void renderBlurredBackground(float partialTick) {
        if (this.sbw_portlib$currentGraphics != null) {
            this.extractBlurredBackground(this.sbw_portlib$currentGraphics.extractor());
        }
    }

    public void renderPanorama(GuiGraphics guiGraphics, float partialTick) {
        this.extractPanorama(guiGraphics.extractor(), partialTick);
    }

    public void renderMenuBackground(GuiGraphics guiGraphics) {
        this.extractMenuBackground(guiGraphics.extractor());
    }
}
