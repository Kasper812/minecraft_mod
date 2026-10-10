package Aru.Aru.ashvehicle.client.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import Aru.Aru.ashvehicle.entity.vehicle.F35Entity;

public class F35GlowLayer extends GeoRenderLayer<F35Entity> {
    private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath("ashvehicle", "textures/entity/f-35-glow.png");

    public F35GlowLayer(GeoRenderer<F35Entity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, F35Entity animatable, BakedGeoModel bakedModel,
                       RenderType baseRenderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        // 🔸 発光用レンダータイプ（常に最大光量で描画）
        RenderType glowRenderType = RenderTypes.eyes(GLOW_TEXTURE);

        // 🔸 VertexConsumer を取得
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        // 🔸 モデル描画（GeckoLib の正しい呼び出し方法）
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                0xF000F0,         // packedLight
                packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f // RGBA
        );
    }
}
