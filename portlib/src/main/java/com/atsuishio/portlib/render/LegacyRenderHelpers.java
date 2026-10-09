package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class LegacyRenderHelpers {
    private LegacyRenderHelpers() {
    }

    public static @Nullable SubmitNodeCollector collectorOf(@Nullable MultiBufferSource buffers) {
        if (buffers instanceof SubmitBufferSource submit) {
            return submit.collector();
        }
        SubmitBufferSource current = SubmitBufferSource.current();
        return current != null ? current.collector() : null;
    }

    public static void renderItemStatic(ItemStack stack, ItemDisplayContext context, int light, int overlay, PoseStack poseStack, MultiBufferSource buffers, @Nullable Level level, int seed) {
        renderItemStatic(null, stack, context, poseStack, buffers, level, light, overlay, seed);
    }

    public static void renderItemStatic(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, @Nullable Level level, int light, int overlay, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        SubmitNodeCollector collector = collectorOf(buffers);
        if (collector == null) {
            return;
        }
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft minecraft = Minecraft.getInstance();
        if (entity != null) {
            minecraft.getItemModelResolver().updateForLiving(state, stack, context, entity);
        } else {
            minecraft.getItemModelResolver().updateForTopItem(state, stack, context, level, null, seed);
        }
        state.submit(poseStack, collector, light, overlay, 0);
    }

    public static CameraRenderState cameraState() {
        Minecraft minecraft = Minecraft.getInstance();
        CameraRenderState state = minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        return state != null ? state : new CameraRenderState();
    }

    @SuppressWarnings("unchecked")
    public static <E extends Entity> void renderEntity(E entity, double x, double y, double z, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        SubmitNodeCollector collector = collectorOf(buffers);
        if (collector == null) {
            return;
        }
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super E, ?> renderer = dispatcher.getRenderer(entity);
        EntityRenderState state = ((EntityRenderer<? super E, EntityRenderState>) renderer).createRenderState(entity, partialTick);
        state.lightCoords = light;
        state.shadowPieces.clear();
        dispatcher.submit(state, cameraState(), x, y, z, poseStack, collector);
    }
}
