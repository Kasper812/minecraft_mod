package com.atsuishio.portlib.render.legacy;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.atsuishio.portlib.render.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface LegacyBlockEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer<T, LegacyBlockEntityRenderState<T>> {
    @Override
    default LegacyBlockEntityRenderState<T> createRenderState() {
        return new LegacyBlockEntityRenderState<>();
    }

    @Override
    default void extractRenderState(T blockEntity, LegacyBlockEntityRenderState<T> state, float partialTicks, Vec3 cameraPosition,
                                    ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTick = partialTicks;
    }

    @Override
    default void submit(LegacyBlockEntityRenderState<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        T blockEntity = state.blockEntity;
        if (blockEntity == null) {
            return;
        }
        SubmitBufferSource buffers = new SubmitBufferSource(collector);
        try {
            this.render(blockEntity, state.partialTick, poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
        } finally {
            buffers.flush();
        }
    }

    void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay);
}
