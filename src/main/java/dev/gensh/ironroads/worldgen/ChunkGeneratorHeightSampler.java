package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Высота рельефа без генерации чанков. getBaseHeight считает шумовую функцию
 * ландшафта в произвольной точке — именно поэтому профиль пути может смотреть
 * на сотни блоков вперёд, не нарушая правило "фича трогает только свой чанк".
 *
 * Вызов дорогой не «немного», а на порядки: getBaseHeight на каждый вызов
 * строит целый NoiseChunk, то есть заново собирает весь конвейер функций
 * плотности. Дамп потоков зависшей игры упирался именно в NoiseRouter.mapAll
 * внутри NoiseChunk.&lt;init&gt;.
 *
 * Поэтому кеш здесь обязателен, а не желателен. Профиль опрашивает рельеф по
 * узлам с шагом 8, и соседние узлы делят почти все свои выборки: при радиусе
 * сглаживания 48 два соседних узла отличаются одной точкой из тринадцати.
 */
public final class ChunkGeneratorHeightSampler implements HeightSampler {

    private final ChunkGenerator generator;
    private final LevelHeightAccessor heightAccessor;
    private final RandomState randomState;

    /**
     * Ключ — упакованные координаты, значение — высота.
     *
     * ConcurrentHashMap, потому что worldgen многопоточный. Гонка безобидна:
     * функция чистая, два потока запишут одно и то же.
     *
     * ponytail: размер не ограничен. Опрашиваются только точки вдоль линий с
     * шагом 8, то есть примерно 200 записей на 96 блоков пути — при разумном
     * исследовании мира это десятки тысяч записей. Если память станет
     * проблемой, вводить LRU, а не сокращать радиус огибающей.
     */
    private final Map<Long, Integer> cache = new ConcurrentHashMap<>();

    public ChunkGeneratorHeightSampler(ChunkGenerator generator,
                                       LevelHeightAccessor heightAccessor,
                                       RandomState randomState) {
        this.generator = generator;
        this.heightAccessor = heightAccessor;
        this.randomState = randomState;
    }

    @Override
    public int heightAt(int x, int z) {
        long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
        Integer hit = cache.get(key);
        if (hit != null) {
            return hit;
        }
        int height = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                                             heightAccessor, randomState);
        cache.put(key, height);
        return height;
    }
}
