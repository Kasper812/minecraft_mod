package frontline.combat.fcp.client;

import com.mojang.blaze3d.platform.InputConstants;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.screen.FCPConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.fabricmc.api.EnvType;
import org.lwjgl.glfw.GLFW;

/**
 * FCP keybinds. One so far: open the config screen in-game (default F8, rebindable under
 * its own "Frontline Combat Pack" category in Controls). IN_GAME conflict context plus the
 * open-screen check means it can't fire while typing in chat or another screen.
 */
public class FCPKeybinds {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("fcp", "main"));

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.fcp.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            CATEGORY);

    public static void onRegisterKeys() {
        net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(OPEN_CONFIG);
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_CONFIG.consumeClick()) {
            if (mc.gui.screen() == null) {
                mc.gui.setScreen(new FCPConfigScreen(null)); // null parent: closing returns to the game
            }
        }
    }
}