package Aru.Aru.ashvehicle;

import Aru.Aru.ashvehicle.init.*;
import Aru.Aru.ashvehicle.item.JerryCanItem;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import org.slf4j.Logger;

public class AshVehicle implements ModInitializer {
    public static final String MODID = "ashvehicle";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModSounds.REGISTRY.getEntries();
        ModParticleTypes.PARTICLES.getEntries();
        ModEntities.REGISTRY.getEntries();
        ModItem.ITEMS.getEntries();
        ModRecipes.init();
        Aru.Aru.ashvehicle.entity.projectile.BallisticMissileEntity.initTicket();
        ModTabs.TABS.getEntries();
        ModNetwork.register();
        UseEntityCallback.EVENT.register(JerryCanItem.EventHandler::onEntityInteract);
        LOGGER.info("common setup");
    }
}
