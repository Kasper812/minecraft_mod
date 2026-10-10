package frontline.combat.fcp.client.renderer.Fmtv4x4;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import frontline.combat.fcp.client.model.Fmtv4x4.FMTV4x4Model;
import frontline.combat.fcp.entity.vehicle.Fmtv4x4.FMTV4x4Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import frontline.combat.fcp.client.renderer.FcpVehicleTexture;

public class FMTV4x4Renderer extends VehicleRenderer<FMTV4x4Entity> {
    // "tent" canopy hitbox, matching the interaction handler (FMTV4x4TentHandler). Shrunk from
    // the 6x6 FMTV's tent box to match this chassis's shorter rear overhang (measured directly
    // from the uploaded 4x4 geo's "tent" bone, which carries no local rotation either).
    private static final AABB TENT_BOX = new AABB(-1.2428, 2.0679, -3.2869, 1.2428, 3.2390, 0.7819);

    public FMTV4x4Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FMTV4x4Model());
    }

    @Override
    public Identifier getTextureLocation(FMTV4x4Entity entity) {
        return FcpVehicleTexture.resolve(entity, entity.getCurrentTexture());
    }

    @Override
    public void render(FMTV4x4Entity entity, float entityYaw, float partialTick,
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
