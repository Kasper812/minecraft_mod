package com.atsuishio.portlib.gui;

import net.minecraft.client.input.CharacterEvent;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.input.MouseButtonInfo;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class LegacyContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected LegacyContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    protected LegacyContainerScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        LegacyRenderSystem.resetState();
        this.render(GuiGraphics.of(graphics), mouseX, mouseY, a);
        LegacyRenderSystem.resetState();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.extractor().nextStratum();
        super.extractContents(guiGraphics.extractor(), mouseX, mouseY, partialTick);
        super.extractCarriedItem(guiGraphics.extractor(), mouseX, mouseY);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics.extractor(), mouseX, mouseY, partialTick);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    protected abstract void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.renderLabels(GuiGraphics.of(graphics), mouseX, mouseY);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics.extractor(), mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.renderTooltip(GuiGraphics.of(graphics), mouseX, mouseY);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics.extractor(), mouseX, mouseY);
    }

    public void renderBackground(GuiGraphics guiGraphics) {
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

    public void renderPanorama(GuiGraphics guiGraphics, float partialTick) {
        this.extractPanorama(guiGraphics.extractor(), partialTick);
    }

    public void renderMenuBackground(GuiGraphics guiGraphics) {
        this.extractMenuBackground(guiGraphics.extractor());
    }
}
