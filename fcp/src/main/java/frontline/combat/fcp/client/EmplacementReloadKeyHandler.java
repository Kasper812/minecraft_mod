package frontline.combat.fcp.client;

import com.atsuishio.superbwarfare.init.ModKeyMappings;
import frontline.combat.fcp.entity.vehicle.CamoEmplacementEntity;
import frontline.combat.fcp.network.FCPNetwork;
import frontline.combat.fcp.network.ReloadEmplacementPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.EnvType;

/**
 * Sends a manual reload request when the player presses SBW's reload key (R by default) while
 * manning an emplacement.
 *
 * SBW only runs its reload logic for a gun held in the main hand (GunEventHandler gates it behind
 * {@code if (inMainHand)}), so a crew-served weapon never sees the key. Reusing SBW's own binding
 * keeps reloading on the key players already know rather than inventing a second one.
 *
 * Edge-triggered: fires once per press, not once per tick held.
 */
public final class EmplacementReloadKeyHandler {

    private static boolean wasDown = false;

    private EmplacementReloadKeyHandler() {
    }

    public static void onClientTick() {

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gui.screen() != null) {
            wasDown = false;
            return;
        }

        boolean down = ModKeyMappings.RELOAD.isDown();
        if (down && !wasDown && isManningEmplacement(player)) {
            FCPNetwork.FCP_HANDLER.sendToServer(new ReloadEmplacementPacket());
        }
        wasDown = down;
    }

    private static boolean isManningEmplacement(Player player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof CamoEmplacementEntity) return true;
        return player.getRootVehicle() instanceof CamoEmplacementEntity;
    }
}