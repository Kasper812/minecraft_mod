package com.atsuishio.portlib;

import com.atsuishio.portlib.render.BuiltinItemModel;
import com.atsuishio.portlib.render.LegacyParticleGroup;
import com.atsuishio.portlib.render.SbmPipelines;
import net.fabricmc.api.ClientModInitializer;

public class PortLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SbmPipelines.init();
        BuiltinItemModel.register();
        LegacyParticleGroup.register();
        com.atsuishio.portlib.render.world.WorldRenderEvents.init();
        net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry.register(ctx -> new com.atsuishio.portlib.gui.Legacy3DRenderer());
    }
}
