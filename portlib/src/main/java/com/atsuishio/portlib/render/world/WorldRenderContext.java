package com.atsuishio.portlib.render.world;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jspecify.annotations.Nullable;

public interface WorldRenderContext {
    Camera camera();

    @Nullable PoseStack matrixStack();

    DeltaTracker tickCounter();

    MultiBufferSource consumers();

    SubmitNodeCollector collector();

    @Nullable ClientLevel world();

    GameRenderer gameRenderer();

    LevelRenderer worldRenderer();
}
