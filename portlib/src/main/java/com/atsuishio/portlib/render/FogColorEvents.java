package com.atsuishio.portlib.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.joml.Vector4f;

public final class FogColorEvents {
    public static final Event<Modifier> COMPUTE = EventFactory.createArrayBacked(Modifier.class, listeners -> (color, partialTick) -> {
        for (Modifier listener : listeners) {
            listener.modify(color, partialTick);
        }
    });

    private FogColorEvents() {
    }

    @FunctionalInterface
    public interface Modifier {
        void modify(Vector4f color, float partialTick);
    }
}
