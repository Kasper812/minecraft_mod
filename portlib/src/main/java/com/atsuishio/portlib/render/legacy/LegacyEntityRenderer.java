package com.atsuishio.portlib.render.legacy;

import com.atsuishio.portlib.render.MultiBufferSource;
import com.atsuishio.portlib.render.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;

public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderState<T>> {
    private boolean baseRendered;

    protected LegacyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LegacyEntityRenderState<T> createRenderState() {
        return new LegacyEntityRenderState<>();
    }

    @Override
    public void extractRenderState(T entity, LegacyEntityRenderState<T> state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
        state.partialTick = partialTicks;
        state.entityYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
    }

    @Override
    public void submit(LegacyEntityRenderState<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        T entity = state.entity;
        if (entity == null) {
            return;
        }
        SubmitBufferSource buffers = new SubmitBufferSource(collector);
        this.baseRendered = false;
        try {
            this.render(entity, state.entityYaw, state.partialTick, poseStack, buffers, state.lightCoords);
        } finally {
            buffers.flush();
        }
        if (this.baseRendered) {
            super.submit(state, poseStack, collector, camera);
        }
    }

    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.baseRendered = true;
    }

    @Override
    public Vec3 getRenderOffset(LegacyEntityRenderState<T> state) {
        Vec3 base = super.getRenderOffset(state);
        if (state.entity == null) {
            return base;
        }
        return base.add(this.getRenderOffset(state.entity, state.partialTick));
    }

    public Vec3 getRenderOffset(T entity, float partialTick) {
        return Vec3.ZERO;
    }

    @Override
    protected boolean shouldShowName(T entity, double distanceToCameraSq) {
        return this.shouldShowName(entity);
    }

    public boolean shouldShowName(T entity) {
        return entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public static boolean isEntityUpsideDown(LivingEntity entity) {
        if (entity instanceof Player || entity.hasCustomName()) {
            String name = ChatFormatting.stripFormatting(entity.getName().getString());
            if ("Dinnerbone".equals(name) || "Grumm".equals(name)) {
                return !(entity instanceof Player player) || player.isModelPartShown(PlayerModelPart.CAPE);
            }
        }
        return false;
    }

    public Identifier getTextureLocation(T entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
