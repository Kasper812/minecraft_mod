package frontline.combat.fcp.client.renderer.Fmtv;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Fmtv.FMTVModel;
import frontline.combat.fcp.client.model.Ural.UralModel;
import frontline.combat.fcp.entity.vehicle.Fmtv.FMTVEntity;
import frontline.combat.fcp.entity.vehicle.Ural.UralEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class FMTVRenderer extends VehicleRenderer<FMTVEntity> {
    // "tent" canopy hitbox, matching the interaction handler (FMTVTentHandler). The "tent" bone
    // carries no local rotation (unlike MAN HX58's TENT bone), so this is a direct geo->entity
    // conversion of its cubes' bounding box, no rotation-matrix correction needed.
    private static final AABB TENT_BOX = new AABB(-1.2428, 2.0679, -3.5525, 1.2428, 3.2390, 0.7819);

    public FMTVRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FMTVModel());
    }

    @Override
    public Identifier getTextureLocation(FMTVEntity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(FMTVEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        Minecraft mc = Minecraft.getInstance();
        if (!com.atsuishio.superbwarfare.port.GuiCompatKt.shouldRenderHitBoxes(mc.getEntityRenderDispatcher()) || mc.options.reducedDebugInfo().get()) return;

        double root = entity.getRotateOffsetHeight();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float roll = entity.getRoll(partialTick);
        VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());

        poseStack.pushPose();
        poseStack.translate(0, root, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.translate(0, -root, 0);
        com.atsuishio.portlib.render.LegacyLines.renderLineBox(poseStack, lines, TENT_BOX, 0.7f, 0.3f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
