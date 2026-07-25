package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;


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
 * Кеша здесь намеренно нет: он живёт в TrackProfiler, то есть в геометрии, где
 * инвариант «ни одна точка не спрашивается дважды» проверяется юнит-тестом
 * (TrackProfilerCostTest) и держится при любом источнике рельефа. Второй кеш
 * поверх этого только маскировал бы регрессию.
 *
 * Вывод для всех, кто добавляет сюда новых потребителей: не вызывать этот
 * сэмплер в цикле по блокам. Внутри своего чанка высоту брать из готовой карты
 * высот через LevelReader.getHeight — она уже посчитана и стоит чтения массива.
 */
public final class ChunkGeneratorHeightSampler implements HeightSampler {

    private final ChunkGenerator generator;
    private final LevelHeightAccessor heightAccessor;
    private final RandomState randomState;

    public ChunkGeneratorHeightSampler(ChunkGenerator generator,
                                       LevelHeightAccessor heightAccessor,
                                       RandomState randomState) {
        this.generator = generator;
        this.heightAccessor = heightAccessor;
        this.randomState = randomState;
    }

    @Override
    public int heightAt(int x, int z) {
        return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                                       heightAccessor, randomState);
    }
}
