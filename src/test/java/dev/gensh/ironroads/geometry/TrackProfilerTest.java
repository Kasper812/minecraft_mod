package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackProfilerTest {

    private static final int GRADE = 12;
    private static final Line LINE = new Line(Line.Axis.Z, 0);

    private RailNetwork network() {
        return new RailNetwork(42L, 768, 48);
    }

    private TrackProfiler profiler(HeightSampler terrain) {
        return new TrackProfiler(network(), GRADE, terrain);
    }

    @Test
    void flatTerrainGivesFlatProfile() {
        TrackProfiler p = profiler((x, z) -> 70);
        for (int along = -500; along <= 500; along += 13) {
            assertEquals(70, p.heightAt(LINE, along));
        }
    }

    @Test
    void gradeNeverExceedsLimit() {
        // Обрыв в 60 блоков — профиль обязан размазать его в пологий уклон.
        TrackProfiler p = profiler((x, z) -> z < 0 ? 60 : 120);
        int prev = p.heightAt(LINE, -800);
        for (int along = -799; along <= 800; along++) {
            int cur = p.heightAt(LINE, along);
            assertTrue(Math.abs(cur - prev) <= 1,
                       "подъём " + Math.abs(cur - prev)
                       + " блоков за один блок пути на along=" + along);
            prev = cur;
        }
        for (int along = -700; along <= 700; along += 5) {
            int a = p.heightAt(LINE, along);
            int b = p.heightAt(LINE, along + GRADE);
            assertTrue(Math.abs(b - a) <= 1,
                       "уклон круче 1:" + GRADE + " на отрезке от " + along);
        }
    }

    @Test
    void profileIsPositionLocalNotPathDependent() {
        // Один и тот же along, посчитанный разными экземплярами, обязан
        // совпадать — иначе на границах чанков будут ступени.
        HeightSampler hilly = (x, z) -> 80 + (int) (25 * Math.sin(z / 90.0));
        TrackProfiler p = profiler(hilly);
        for (int along = -300; along <= 300; along += 7) {
            assertEquals(p.heightAt(LINE, along),
                         profiler(hilly).heightAt(LINE, along),
                         "профиль зависит от контекста вызова");
        }
    }

    @Test
    void narrowValleyIsBridgedNotFollowed() {
        // Узкое ущелье в 50 блоков глубиной: путь должен пройти поверху.
        TrackProfiler p = profiler((x, z) -> (z > -20 && z < 20) ? 40 : 90);
        assertTrue(p.heightAt(LINE, 0) > 70,
                   "профиль нырнул в ущелье вместо того, чтобы его перелететь");
    }
}
