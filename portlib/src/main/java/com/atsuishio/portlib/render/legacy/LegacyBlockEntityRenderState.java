package com.atsuishio.portlib.render.legacy;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class LegacyBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {
    public @Nullable T blockEntity;
    public float partialTick;
}
