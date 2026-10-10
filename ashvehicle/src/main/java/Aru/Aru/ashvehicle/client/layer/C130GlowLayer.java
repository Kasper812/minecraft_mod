package Aru.Aru.ashvehicle.client.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import Aru.Aru.ashvehicle.entity.vehicle.C130Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class C130GlowLayer extends GeoRenderLayer<C130Entity> {

    private static final Identifier GLOW_TEXTURE =
            Identifier.fromNamespaceAndPath("ashvehicle", "textures/glow/ac130uglow.png");

    public C130GlowLayer(GeoRenderer<C130Entity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack,
                       C130Entity animatable,
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