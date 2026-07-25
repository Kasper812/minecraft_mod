package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Один профиль на мир, общий для генерации и для прохода по загруженным чанкам.
 *
 * Общий он не ради экономии памяти, а потому что расчёт высоты дорогой: первое
 * обращение к линии стоит тысячи выборок шума ландшафта. Пока кешей было два,
 * проход по чанкам считал на серверном потоке ровно то же, что фича уже
 * посчитала при генерации, и держал поток на загрузке мира.
 *
 * Совпадение значений тоже обязательно: концы разрывов под кривые вычисляются
 * дважды, и разойтись они не имеют права.
 */
public final class RailwayProfilers {

    private static final Map<Long, TrackProfiler> CACHE = new ConcurrentHashMap<>();

    public static TrackProfiler forLevel(ServerLevel level, ChunkGenerator generator) {
        return CACHE.computeIfAbsent(level.getSeed(), seed -> {
            RailNetwork network = new RailNetwork(seed, RailwayFeature.SPACING,
                                                  RailwayFeature.JITTER);
            // Границы высоты берём у ServerLevel, а не у одноразового
            // WorldGenLevel: экземпляр переживает генерацию отдельного чанка.
            HeightSampler terrain = new ChunkGeneratorHeightSampler(
                    generator, level, level.getChunkSource().randomState());
            return new TrackProfiler(network, RailwayFeature.MAX_GRADE_RUN, terrain);
        });
    }

    private RailwayProfilers() {}
}
