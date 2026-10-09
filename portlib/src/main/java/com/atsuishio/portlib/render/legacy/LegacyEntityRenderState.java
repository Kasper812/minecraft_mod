package com.atsuishio.portlib.render.legacy;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class LegacyEntityRenderState<T extends Entity> extends EntityRenderState {
    public @Nullable T entity;
    public float partialTick;
    public float entityYaw;
}
