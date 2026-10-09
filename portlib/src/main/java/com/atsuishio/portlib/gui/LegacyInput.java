package com.atsuishio.portlib.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class LegacyInput {
    private LegacyInput() {
    }

    private static boolean down(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
    }

    public static boolean hasShiftDown() {
        return down(GLFW.GLFW_KEY_LEFT_SHIFT) || down(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean hasControlDown() {
        if (net.minecraft.client.input.InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
            return down(GLFW.GLFW_KEY_LEFT_SUPER) || down(GLFW.GLFW_KEY_RIGHT_SUPER);
        }
        return down(GLFW.GLFW_KEY_LEFT_CONTROL) || down(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean hasAltDown() {
        return down(GLFW.GLFW_KEY_LEFT_ALT) || down(GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
