package dev.gensh.ironroads;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(IronRoads.MOD_ID)
public class IronRoads {
    public static final String MOD_ID = "ironroads";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronRoads() {
        LOGGER.info("IronRoads loaded");
    }
}
