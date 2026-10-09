package com.github.mcmodderanchor.simplebedrockmodel.v2.client.compat.acceleratedrendering;

import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.baked.BakedGeometryChunk;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.tree.TreeBoneDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Environment(EnvType.CLIENT)
public final class AcceleratedRenderingCompat {
    private AcceleratedRenderingCompat() {
    }

    public static boolean renderQuads(BakedGeometryChunk chunk, VertexConsumer consumer, PoseStack.Pose pose,
                                      int lightmap, int overlay, float red, float green, float blue, float alpha) {
        return false;
    }

    public static boolean renderVertices(BakedGeometryChunk chunk, VertexConsumer consumer, PoseStack.Pose pose,
                                         int lightmap, int overlay, float red, float green, float blue, float alpha) {
        return false;
    }

    public static boolean renderCubes(TreeBoneDefinition bone, VertexConsumer consumer, PoseStack.Pose pose,
                                      int lightmap, int overlay, float red, float green, float blue, float alpha) {
        return false;
    }

    public static boolean renderPolyMeshes(TreeBoneDefinition bone, VertexConsumer consumer, PoseStack.Pose pose,
                                           int lightmap, int overlay, float red, float green, float blue, float alpha) {
        return false;
    }

}
