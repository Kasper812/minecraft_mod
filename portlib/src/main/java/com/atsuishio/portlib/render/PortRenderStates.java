package com.atsuishio.portlib.render;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class PortRenderStates {
    public static final RenderStateDataKey<Entity> ENTITY = RenderStateDataKey.create(() -> "sbw_portlib:entity");
    public static final RenderStateDataKey<Float> PARTIAL_TICK = RenderStateDataKey.create(() -> "sbw_portlib:partial_tick");

    private PortRenderStates() {
    }

    public static @Nullable Entity entity(EntityRenderState state) {
        return state.getData(ENTITY);
    }

    public static float partialTick(EntityRenderState state) {
        Float value = state.getData(PARTIAL_TICK);
        return value == null ? 1.0F : value;
    }
}
