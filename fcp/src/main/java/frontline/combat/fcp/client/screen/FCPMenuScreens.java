package frontline.combat.fcp.client.screen;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.VehicleInventoryKeyHandler;
import frontline.combat.fcp.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.fabricmc.api.EnvType;

/** Binds FCP menu types to their screens. Self-registering — no wiring needed. */
public class FCPMenuScreens {

    public static void onClientSetup() {
        MenuScreens.register(ModMenus.VEHICLE_INVENTORY.get(), VehicleInventoryScreen::new);
        VehicleInventoryKeyHandler.register();
    }
}