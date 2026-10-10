package tech.vvp.vvp.client;

import com.atsuishio.portlib.forge.client.GuiOverlays;
import com.atsuishio.portlib.gui.LegacyHudLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.client.hud.PantsirOperatorOverlay;
import tech.vvp.vvp.client.overlay.D30InfoOverlay;
import tech.vvp.vvp.client.particle.ModParticles;
import tech.vvp.vvp.init.ModEntityRenderers;

@Environment(EnvType.CLIENT)
public class VVPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntityRenderers.registerRenderers();
        ModParticles.registerProviders();
        GuiOverlays.registerAboveAll(VVP.loc(D30InfoOverlay.ID), new D30InfoOverlay());
        HudElementRegistry.addLast(VVP.loc("pantsir_operator"), (LegacyHudLayer) (guiGraphics, deltaTracker) -> PantsirOperatorOverlay.onRenderGui(guiGraphics));
    }
}
