package Aru.Aru.ashvehicle.client;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.init.ModEntityRenderers;
import Aru.Aru.ashvehicle.init.ParticleRegistry;
import Aru.Aru.ashvehicle.init.client.ClientEntityHighlighter;
import Aru.Aru.ashvehicle.init.client.ClientKeyRegister;
import Aru.Aru.ashvehicle.init.event.ClientEvents;
import Aru.Aru.ashvehicle.init.event.ClientRenderEvents;
import com.atsuishio.portlib.gui.LegacyHudLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class AshVehicleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntityRenderers.registerEntityRenderers();
        ParticleRegistry.registerParticleProviders();
        ClientKeyRegister.onRegisterKeyMappings();
        ClientRenderEvents.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEvents.onClientTick());
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(AshVehicle.MODID, "entity_highlighter"), (LegacyHudLayer) (guiGraphics, deltaTracker) -> ClientEntityHighlighter.onRenderGuiOverlay(guiGraphics));
    }
}
