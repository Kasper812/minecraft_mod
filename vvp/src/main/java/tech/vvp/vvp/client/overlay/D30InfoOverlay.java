package tech.vvp.vvp.client.overlay;

import com.atsuishio.superbwarfare.item.misc.FiringParametersItem;
import com.atsuishio.superbwarfare.item.misc.FiringParametersItemKt;
import com.atsuishio.superbwarfare.tools.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.atsuishio.portlib.gui.GuiGraphics;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.atsuishio.portlib.forge.client.ForgeGui;
import com.atsuishio.portlib.forge.client.IGuiOverlay;
import tech.vvp.vvp.entity.vehicle.D30Entity;

import static com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils.getXRotFromVector;
import static tech.vvp.vvp.entity.vehicle.D30Entity.*;

@Environment(EnvType.CLIENT)
public class D30InfoOverlay implements IGuiOverlay {

    public static final String ID = "vvp_d30_info";

    private static final float PROJECTILE_VELOCITY = 15.0f;
    private static final float GRAVITY = 0.05f;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics,
                       float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        Player player = mc.player;
        if (player == null) return;

        Entity lookingEntity = TraceTool.findLookingEntity(player, 30.0f);

        if (!(lookingEntity instanceof D30Entity d30)) return;

        float pitch  = d30.getEntityData().get(TARGET_PITCH);
        float yaw    = d30.getEntityData().get(TARGET_YAW);
        boolean loaded = d30.getEntityData().get(LOADED);

        // ─── Left side: current parameters ───
        guiGraphics.drawString(mc.font,
                Component.translatable("tips.superbwarfare.mortar.pitch")
                        .append(Component.literal(
                                FormatTool.format2D(-pitch, "°"))),
                screenWidth / 2 - 130,
                screenHeight / 2 - 26, -1, false);

        guiGraphics.drawString(mc.font,
                Component.translatable("tips.superbwarfare.mortar.yaw")
                        .append(Component.literal(
                                FormatTool.format2D(yaw, "°"))),
                screenWidth / 2 - 130,
                screenHeight / 2 - 16, -1, false);

        double range = RangeTool.getRange(-pitch, PROJECTILE_VELOCITY, GRAVITY);
        guiGraphics.drawString(mc.font,
                Component.translatable("tips.superbwarfare.mortar.range")
                        .append(Component.literal(
                                FormatTool.format1D(
                                        Math.max((int) range, 0), "m"))),
                screenWidth / 2 - 130,
                screenHeight / 2 - 6, -1, false);

        // Loaded status
        int color = loaded ? 0xFF55FF55 : 0xFFFF5555;
        String status = loaded ? "[LOADED]" : "[EMPTY]";
        guiGraphics.drawString(mc.font,
                Component.literal(status),
                screenWidth / 2 - 130,
                screenHeight / 2 + 6, color, false);

        // ─── Right side: firing parameters (if holding item) ───
        ItemStack stack = player.getOffhandItem();
        if (player.getMainHandItem().getItem() instanceof FiringParametersItem) {
            stack = player.getMainHandItem();
        }

        if (stack.getItem() instanceof FiringParametersItem) {
            FiringParametersItem.Parameters parameters = FiringParametersItemKt.getFiringParameters(stack);
            double targetX = parameters.pos().getX();
            double targetY = parameters.pos().getY() - 1;
            double targetZ = parameters.pos().getZ();
            boolean isDepressed = parameters.isDepressed();

            Vec3 targetPos = new Vec3(targetX, targetY, targetZ);
            // Calculate launch angle using ballistic trajectory
            double dx = targetPos.x - d30.getShootPos().x;
            double dy = targetPos.y - d30.getShootPos().y;
            double dz = targetPos.z - d30.getShootPos().z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            
            // Ballistic calculation for launch angle
            double v = PROJECTILE_VELOCITY;
            double g = GRAVITY;
            double discriminant = v*v*v*v - g*(g*horizontalDist*horizontalDist + 2*dy*v*v);
            
            Vec3 launchVector = null;
            if (discriminant >= 0) {
                double angle1 = Math.atan((v*v + Math.sqrt(discriminant)) / (g*horizontalDist));
                double angle2 = Math.atan((v*v - Math.sqrt(discriminant)) / (g*horizontalDist));
                double angle = isDepressed ? Math.min(angle1, angle2) : Math.max(angle1, angle2);
                
                double horizontalVel = v * Math.cos(angle);
                double verticalVel = v * Math.sin(angle);
                launchVector = new Vec3(
                    (dx / horizontalDist) * horizontalVel,
                    verticalVel,
                    (dz / horizontalDist) * horizontalVel
                );
            }

            Vec3 vec3 = EntityAnchorArgument.Anchor.EYES.apply(d30);
            double d0 = (targetPos.x - vec3.x) * 0.2;
            double d2 = (targetPos.z - vec3.z) * 0.2;
            double targetYaw = Mth.wrapDegrees(
                    (float)(Mth.atan2(d2, d0) * 57.2957763671875) - 90F);

            float angle;

            if (launchVector != null) {
                angle = (float) getXRotFromVector(launchVector);
            } else {
                guiGraphics.drawString(mc.font,
                        Component.translatable(
                                        "tips.superbwarfare.mortar.out_of_range")
                                .withStyle(ChatFormatting.RED),
                        screenWidth / 2 + 90,
                        screenHeight / 2 - 26, -1, false);
                return;
            }

            guiGraphics.drawString(mc.font,
                    Component.translatable("tips.superbwarfare.target.pitch")
                            .append(Component.literal(
                                    FormatTool.format2D(angle, "°"))),
                    screenWidth / 2 + 90,
                    screenHeight / 2 - 26, -1, false);

            guiGraphics.drawString(mc.font,
                    Component.translatable("tips.superbwarfare.target.yaw")
                            .append(Component.literal(
                                    FormatTool.format2D(targetYaw, "°"))),
                    screenWidth / 2 + 90,
                    screenHeight / 2 - 16, -1, false);

            guiGraphics.drawString(mc.font,
                    Component.translatable("tips.superbwarfare.mortar.target_pos")
                            .append(Component.literal(
                                    FormatTool.format0D(targetX) + " "
                                            + FormatTool.format0D(targetY) + " "
                                            + FormatTool.format0D(targetZ))),
                    screenWidth / 2 + 90,
                    screenHeight / 2 - 6, -1, false);

            if (angle < -70 || angle > 7) {
                guiGraphics.drawString(mc.font,
                        Component.translatable(
                                        "tips.superbwarfare.mortar.warn",
                                        d30.getDisplayName())
                                .withStyle(ChatFormatting.RED),
                        screenWidth / 2 + 90,
                        screenHeight / 2 + 4, -1, false);
            }
        }
    }
}