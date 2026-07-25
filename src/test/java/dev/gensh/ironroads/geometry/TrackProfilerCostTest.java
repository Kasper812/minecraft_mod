package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Стоимость профиля в обращениях к рельефу.
 *
 * За рельефом в игре стоит ChunkGenerator.getBaseHeight, который на каждый
 * вызов строит целый NoiseChunk — операция дороже, чем выглядит, и уже дважды
 * вешавшая генерацию мира намертво. Дамп потоков зависшей игры упирался именно
 * в неё.
 *
 * Поэтому число обращений — не деталь реализации, а инвариант, который обязан
 * оставаться под контролем. Тест ловит регрессию, если кто-то увеличит радиус
 * огибающей, уменьшит шаг сетки или уберёт кеш уровней.
 */
class TrackProfilerCostTest {

    private static final int GRADE = 12;
    private static final int SPACING = 768;
    private static final Line LINE = new Line(Line.Axis.Z, 0);

    /** Рельеф, считающий обращения и запоминающий, какие точки спрашивали. */
    private static final class CountingTerrain implements HeightSampler {
        private final Set<Long> distinct = new HashSet<>();
        private int calls;

        @Override
        public int heightAt(int x, int z) {
            calls++;
            distinct.add(((long) x << 32) ^ (z & 0xFFFFFFFFL));
            return 80 + (int) (20 * Math.sin(z / 300.0) + 15 * Math.cos(x / 250.0));
        }
    }

    private static TrackProfiler profiler(CountingTerrain terrain) {
        return new TrackProfiler(new RailNetwork(42L, SPACING, 0), GRADE, terrain);
    }

    /** Один чанк — 16 шагов вдоль линии. */
    private static void buildChunk(TrackProfiler p, int chunkMinZ) {
        for (int step = 0; step < 16; step++) {
            p.heightAt(LINE, chunkMinZ + step);
            p.isGap(LINE, chunkMinZ + step);
        }
    }

    @Test
    void repeatedChunksCostAlmostNothing() {
        // Главный инвариант. Первый чанк на линии платит за окно огибающей и
        // за привязку развязок, но дальше профиль обязан идти по кешу: узлы
        // сетки общие для всей линии. Именно отсутствие этого свойства делало
        // генерацию неиграбельной, когда профиль пересоздавался на каждый чанк.
        CountingTerrain terrain = new CountingTerrain();
        TrackProfiler p = profiler(terrain);

        buildChunk(p, 0);
        int afterFirst = terrain.distinct.size();

        for (int chunk = 1; chunk <= 20; chunk++) {
            buildChunk(p, chunk * 16);
        }
        int added = terrain.distinct.size() - afterFirst;

        assertTrue(added < afterFirst / 4,
                   "двадцать следующих чанков добавили " + added
                   + " новых точек рельефа против " + afterFirst
                   + " у первого — кеш профиля не работает");
    }

    @Test
    void firstChunkStaysWithinBudget() {
        // Разовая цена входа на линию. Не догма, но выход за этот порядок
        // означает, что окно огибающей или привязка развязок раздулись.
        CountingTerrain terrain = new CountingTerrain();
        buildChunk(profiler(terrain), 0);

        assertTrue(terrain.distinct.size() < 4000,
                   "первый чанк на линии спросил рельеф в "
                   + terrain.distinct.size() + " точках");
    }

    @Test
    void nothingIsSampledTwice() {
        // Повторный спрос той же точки означает дыру в кешировании: в игре
        // каждый такой вызов заново строит NoiseChunk.
        CountingTerrain terrain = new CountingTerrain();
        TrackProfiler p = profiler(terrain);
        buildChunk(p, 0);
        buildChunk(p, 16);

        assertEquals(terrain.distinct.size(), terrain.calls,
                     "рельеф спрошен " + terrain.calls + " раз при "
                     + terrain.distinct.size() + " различных точках");
    }
}
