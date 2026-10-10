package frontline.combat.fcp.init;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.menu.VehicleInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import com.atsuishio.portlib.forge.DeferredRegister;
import com.atsuishio.portlib.forge.ForgeRegistries;
import com.atsuishio.portlib.forge.RegistryObject;

/**
 * FCP menu types.
 *
 * One menu type covers every vehicle hold regardless of size or filter, so adding cargo to
 * a new vehicle never touches this class.
 *
 * NOTE: like ModTabs, this DeferredRegister must be attached to the mod event bus in the
 * FCP main class constructor:
 *     ModMenus.MENUS.register(modEventBus);
 */
public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FCP.MODID);

    public static final RegistryObject<MenuType<VehicleInventoryMenu>> VEHICLE_INVENTORY =
            MENUS.register("vehicle_inventory",
                    () -> new net.fabricmc.fabric.api.menu.v1.ExtendedMenuType<>(VehicleInventoryMenu::fromNetwork, VehicleInventoryMenu.OpenData.STREAM_CODEC));
}