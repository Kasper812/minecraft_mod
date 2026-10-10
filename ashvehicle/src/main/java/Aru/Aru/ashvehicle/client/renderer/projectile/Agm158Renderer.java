package Aru.Aru.ashvehicle.client.renderer.projectile;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import Aru.Aru.ashvehicle.client.model.projectile.Agm158Model;
import Aru.Aru.ashvehicle.entity.projectile.Agm158Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Agm158Renderer extends GeoEntityRenderer<Agm158Entity> {
    public Agm158Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Agm158Model());
        //this.addRenderLayer(new Ru9m336MissileLayer(this));
    }

    public RenderType getRenderType(Agm158Entity animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(this.getTextureLocation(animatable));
    }

    public void render(Agm158Entity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }
}
