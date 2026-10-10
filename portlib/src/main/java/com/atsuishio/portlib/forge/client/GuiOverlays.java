package com.atsuishio.portlib.forge.client;

import com.atsuishio.portlib.gui.GuiGraphics;
import com.atsuishio.portlib.gui.LegacyRenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class GuiOverlays {
    private GuiOverlays() {
    }

    public static void registerAboveAll(Identifier id, IGuiOverlay overlay) {
        HudElementRegistry.addLast(id, element(overlay));
    }

    public static void registerBelowAll(Identifier id, IGuiOverlay overlay) {
        HudElementRegistry.addFirst(id, element(overlay));
    }

    private static net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement element(IGuiOverlay overlay) {
        return (extractor, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.hud.isHidden()) {
                return;
            }
            LegacyRenderSystem.resetState();
            overlay.render(ForgeGui.INSTANCE, GuiGraphics.of(extractor), deltaTracker.getGameTimeDeltaPartialTick(false),
                    mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            LegacyRenderSystem.resetState();
        };
    }
}
