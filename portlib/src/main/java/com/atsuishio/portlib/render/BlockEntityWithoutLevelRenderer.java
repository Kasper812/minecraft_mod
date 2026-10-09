package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class BlockEntityWithoutLevelRenderer implements DynamicItemRenderer {
    protected final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    protected final EntityModelSet entityModelSet;

    public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.entityModelSet = entityModelSet;
    }

    public abstract void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        renderByItem(stack, mode, matrices, buffers, light, overlay);
    }
}
