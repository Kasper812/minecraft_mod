package com.atsuishio.portlib.render.world;

import com.atsuishio.portlib.render.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class WorldRenderEvents {
    public static final Phase START = new Phase();
    public static final Phase AFTER_SETUP = new Phase();
    public static final Phase BEFORE_ENTITIES = new Phase();
    public static final Phase AFTER_ENTITIES = new Phase();
    public static final Phase BEFORE_DEBUG_RENDER = new Phase();
    public static final Phase AFTER_TRANSLUCENT = new Phase();
    public static final Phase LAST = new Phase();
    public static final Phase END = new Phase();

    private static final Phase[] ORDER = {START, AFTER_SETUP, BEFORE_ENTITIES, AFTER_ENTITIES, BEFORE_DEBUG_RENDER, AFTER_TRANSLUCENT, LAST, END};

    private WorldRenderEvents() {
    }

    @FunctionalInterface
    public interface Callback {
        void onRender(WorldRenderContext context);
    }

    public static final class Phase {
        private final List<Callback> callbacks = new CopyOnWriteArrayList<>();

        public void register(Callback callback) {
            callbacks.add(callback);
        }
    }

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(WorldRenderEvents::dispatch);
    }

    private static void dispatch(LevelRenderContext levelContext) {
        boolean any = false;
        for (Phase phase : ORDER) {
            if (!phase.callbacks.isEmpty()) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        SubmitBufferSource buffers = new SubmitBufferSource(levelContext.submitNodeCollector());
        PoseStack poseStack = levelContext.poseStack();
        WorldRenderContext context = new WorldRenderContext() {
            @Override
            public Camera camera() {
                return minecraft.gameRenderer.mainCamera();
            }

            @Override
            public PoseStack matrixStack() {
                return poseStack;
            }

            @Override
            public DeltaTracker tickCounter() {
                return minecraft.getDeltaTracker();
            }

            @Override
            public SubmitBufferSource consumers() {
                return buffers;
            }

            @Override
            public SubmitNodeCollector collector() {
                return levelContext.submitNodeCollector();
            }

            @Override
            public @Nullable ClientLevel world() {
                return minecraft.level;
            }

            @Override
            public GameRenderer gameRenderer() {
                return levelContext.gameRenderer();
            }

            @Override
            public LevelRenderer worldRenderer() {
                return levelContext.levelRenderer();
            }
        };
        try {
            for (Phase phase : ORDER) {
                for (Callback callback : phase.callbacks) {
                    poseStack.pushPose();
                    try {
                        callback.onRender(context);
                    } finally {
                        poseStack.popPose();
                        buffers.endBatch();
                    }
                }
            }
        } finally {
            buffers.flush();
        }
    }
}
