package Aru.Aru.ashvehicle.client.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import Aru.Aru.ashvehicle.entity.vehicle.F22Entity;

public class F22GlowLayer extends GeoRenderLayer<F22Entity> {

    private static final Identifier GLOW_TEXTURE =
            Identifier.fromNamespaceAndPath("ashvehicle", "textures/glow/f_22_glow.png");

    public F22GlowLayer(GeoRenderer<F22Entity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack,
                       F22Entity animatable,
                       BakedGeoModel bakedModel,
                       RenderType baseRenderType,
                       MultiBufferSource bufferSource,
                       VertexConsumer buffer,
                       float partialTick,
                       int packedLight,
                       int packedOverlay) {

        RenderType glowRenderType = RenderTypes.eyes(GLOW_TEXTURE);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                0xF000F0, // フルブライト
                OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f
        );
    }
}