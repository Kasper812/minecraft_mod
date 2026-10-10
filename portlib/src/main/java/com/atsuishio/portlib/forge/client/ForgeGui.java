package com.atsuishio.portlib.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public final class ForgeGui {
    public static final ForgeGui INSTANCE = new ForgeGui();

    private ForgeGui() {
    }

    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public Font getFont() {
        return Minecraft.getInstance().font;
    }
}
