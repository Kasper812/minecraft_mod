package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TrackProfilerCostTest {
    private static final int GRADE = 12;
    private static final int SPACING = 1536;
    private static final int ALIGNMENT = 40;
    private static final Line LINE = new Line(Line.Axis.Z, 0);

    private static class CountingTerrain implements HeightSampler {
        private final Set<Long> distinct = new HashSet<>();
        private int calls;

        @Override
        public final int heightAt(int x, int z) {
            calls++;
            distinct.add(((long) x << 32) ^ (z & 0xFFFFFFFFL));
            return height(x, z);
        }

        int height(int x, int z) {
            return 80 + (int) (20 * Math.sin(z / 300.0) + 15 * Math.cos(x / 250.0));
        }
    }

    private static TrackProfiler profiler(CountingTerrain terrain) {
        return new TrackProfiler(new RailNetwork(42L, SPACING, ALIGNMENT), GRADE, terrain);
    }

    // Фича спрашивает профиль и за краем чанка: там решается, идёт сечение
    // тоннелем или выемкой, и решение обязано совпасть с ответом соседнего чанка,
    // иначе тоннель рвётся ровно по границе. Запас входит в цену первого чанка.
    private static final int MARGIN = 10;

    private static void buildChunk(TrackProfiler p, int chunkMinZ) {
        for (int step = -MARGIN; step < 16 + MARGIN; step++) {
            p.heightAt(LINE, chunkMinZ + step);
            p.perpendicularAt(LINE, chunkMinZ + step);
        }
        for (int step = 0; step < 16; step++) {
            p.isGap(LINE, chunkMinZ + step);
        }
    }

    @Test
    void repeatedChunksCostAlmostNothing() {
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
        CountingTerrain terrain = new CountingTerrain();
        buildChunk(profiler(terrain), 0);

        assertTrue(terrain.distinct.size() < 250,
                   "первый чанк на линии спросил рельеф в "
                   + terrain.distinct.size() + " точках");
    }

    // Рваный рельеф — худший случай для поиска обхода: почти на каждом отрезке
    // прямой ход дорог, и профиль щупает все разрешённые полосы. Порог сторожит
    // именно эту цену: она решает, встанет ли генерация на горах.
    //
    // 325 → 460 после того, как чанк стал смотреть на MARGIN сечений за свой край.
    // Прибавка — это уровень соседнего 96-блочного отрезка, за который всё равно
    // заплатил бы следующий чанк: работа перенесена вперёд, а не добавлена. Порог
    // держит именно перенос от превращения в рост.
    @Test
    void firstChunkOnBrokenGroundStaysWithinBudget() {
        CountingTerrain terrain = new CountingTerrain() {
            @Override
            int height(int x, int z) {
                return 80 + (int) (40 * Math.sin(z / 60.0) + 30 * Math.cos(x / 45.0));
            }
        };
        buildChunk(profiler(terrain), 0);

        assertTrue(terrain.distinct.size() < 500,
                   "первый чанк на рваном рельефе спросил рельеф в "
                   + terrain.distinct.size() + " точках: поиск обхода стоит слишком дорого");
    }

    @Test
    void nothingIsSampledTwice() {
        CountingTerrain terrain = new CountingTerrain();
        TrackProfiler p = profiler(terrain);
        buildChunk(p, 0);
        buildChunk(p, 16);

        assertEquals(terrain.distinct.size(), terrain.calls,
                     "рельеф спрошен " + terrain.calls + " раз при "
                     + terrain.distinct.size() + " различных точках");
    }
}
