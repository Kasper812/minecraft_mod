package com.atsuishio.portlib.gui;

import net.minecraft.client.input.CharacterEvent;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.input.MouseButtonInfo;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public abstract class LegacyAbstractButton extends AbstractButton {
    protected LegacyAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.renderWidget(GuiGraphics.of(graphics), mouseX, mouseY, a);
    }

    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractDefaultSprite(guiGraphics.extractor());
        this.extractDefaultLabel(guiGraphics.extractor().textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
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

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onClick(event.x(), event.y());
    }

    public void onClick(double mouseX, double mouseY) {
        super.onClick(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(0, 0)), false);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.onRelease(event.x(), event.y());
    }

    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(0, 0)));
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        this.onDrag(event.x(), event.y(), dx, dy);
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(0, 0)), dragX, dragY);
    }
}
