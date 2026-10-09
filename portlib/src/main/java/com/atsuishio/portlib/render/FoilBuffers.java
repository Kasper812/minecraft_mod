package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class FoilBuffers {
    private FoilBuffers() {
    }

    public static VertexConsumer getFoilBufferDirect(MultiBufferSource bufferSource, RenderType renderType, boolean noEntity, boolean withGlint) {
        if (!withGlint) {
            return bufferSource.getBuffer(renderType);
        }
        VertexConsumer glint = bufferSource.getBuffer(noEntity ? RenderTypes.glint() : RenderTypes.entityGlint());
        return new DualVertexConsumer(glint, bufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getArmorFoilBuffer(MultiBufferSource bufferSource, RenderType renderType, boolean withGlint) {
        if (!withGlint) {
            return bufferSource.getBuffer(renderType);
        }
        return new DualVertexConsumer(bufferSource.getBuffer(RenderTypes.armorEntityGlint()), bufferSource.getBuffer(renderType));
    }
}
