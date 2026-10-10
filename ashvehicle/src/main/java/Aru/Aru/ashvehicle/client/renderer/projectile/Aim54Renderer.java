package Aru.Aru.ashvehicle.client.renderer.projectile;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import Aru.Aru.ashvehicle.client.model.projectile.Aim54Model;
import Aru.Aru.ashvehicle.entity.projectile.Aim54Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Aim54Renderer extends GeoEntityRenderer<Aim54Entity> {
    public Aim54Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Aim54Model());
        //this.addRenderLayer(new Ru54m336MissileLayer(this));
    }

    public RenderType getRenderType(Aim54Entity animatable, Identifier texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderTypes.entityTranslucent(this.getTextureLocation(animatable));
    }

    public void render(Aim54Entity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }
}
