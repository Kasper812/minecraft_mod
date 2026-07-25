package dev.gensh.ironroads;

import com.mojang.logging.LogUtils;
import dev.gensh.ironroads.worldgen.ModFeatures;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IronRoads.MOD_ID)
public class IronRoads {
    public static final String MOD_ID = "ironroads";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronRoads() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModFeatures.register(bus);
        LOGGER.info("IronRoads loaded");
    }
}
