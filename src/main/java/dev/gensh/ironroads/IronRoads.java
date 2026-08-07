package dev.gensh.ironroads;

import com.mojang.logging.LogUtils;
import dev.gensh.ironroads.worldgen.ModFeatures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(IronRoads.MOD_ID)
public class IronRoads {
    public static final String MOD_ID = "ironroads";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Подробный лог укладки и сшивания: {@code -Dironroads.debug=true}.
     *
     * <p>Отдельный ключ, а не просто уровень логгера, потому что дорогá не сама
     * запись, а её подготовка: пропуск кривой печатал блок из мира, то есть
     * читал {@code getBlockState} ради строки, которую никто не смотрит. Таких
     * пропусков сотни на игрока — отсюда и были тысячи строк в логе. Проверка
     * поля дешевле, чем {@code LOGGER.isDebugEnabled()}, и, главное, видна на
     * месте вызова.
     */
    public static final boolean DEBUG = Boolean.getBoolean("ironroads.debug");

    public IronRoads(IEventBus bus) {
        ModFeatures.register(bus);
        LOGGER.info("IronRoads loaded");
    }
}
