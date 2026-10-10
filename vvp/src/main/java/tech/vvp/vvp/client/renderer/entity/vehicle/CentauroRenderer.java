package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import tech.vvp.vvp.client.model.CentauroModel;
import tech.vvp.vvp.entity.vehicle.CentauroEntity;

public class CentauroRenderer extends VehicleRenderer<CentauroEntity> {
    public CentauroRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CentauroModel());
    }

    @Override
    public void render(CentauroEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Поворачиваем модель на 180 градусов вокруг оси Y
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(CentauroEntity entity) {
        Identifier[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        return (camoType >= 0 && camoType < textures.length) ? textures[camoType] : textures[0];
    }
}
