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
    void heightChangesOneBlockAtATime() {
        // Наклонный путь Create поднимается ровно на блок за блок; перепад в
        // два блока за шаг уложить нечем.
        TrackProfiler p = profiler((x, z) -> z < 0 ? 60 : 120);
        int prev = p.heightAt(LINE, -800);
        for (int along = -799; along <= 800; along++) {
            int cur = p.heightAt(LINE, along);
            assertTrue(Math.abs(cur - prev) <= 1,
                       "подъём " + Math.abs(cur - prev)
                       + " блоков за один блок пути на along=" + along);
            prev = cur;
        }
    }

    @Test
    void profileNeverSpikes() {
        // Главная проверка: подъём не имеет права сразу переходить в спуск.
        // Такой зубец — это два наклонных блока, встречающихся вершинами; их
        // концы стыка не совпадают, путь рвётся и поезд не проходит.
        TrackProfiler p = profiler((x, z) -> 80 + (int) (30 * Math.sin(z / 40.0)));
        int changes = 0;
        for (int along = -2000; along < 2000; along++) {
            int first = p.heightAt(LINE, along + 1) - p.heightAt(LINE, along);
            int second = p.heightAt(LINE, along + 2) - p.heightAt(LINE, along + 1);
            if (first != 0) {
                changes++;
            }
            if (first != 0 && second != 0) {
                assertEquals(first, second,
                             "перелом профиля на along=" + along
                             + ": " + first + " затем " + second);
            }
        }
        assertTrue(changes > 20,
                   "профиль почти плоский (" + changes + " изменений), "
                   + "проверка на переломы прошла вхолостую");
    }

    @Test
    void averageGradeStaysGentle() {
        // Пандусы крутые по необходимости, но между ними длинные площадки,
        // поэтому средний уклон обязан остаться в пределах 1:GRADE.
        TrackProfiler p = profiler((x, z) -> z < 0 ? 60 : 120);
        int span = 96;
        for (int along = -700; along <= 700; along += span) {
            int rise = Math.abs(p.heightAt(LINE, along + span) - p.heightAt(LINE, along));
            assertTrue(rise <= span / GRADE + 1,
                       "средний уклон круче 1:" + GRADE + " на отрезке от " + along
                       + ": подъём " + rise + " за " + span);
        }
    }

    @Test
    void rampStaysInsideItsSegmentAndOffCrossings() {
        TrackProfiler p = profiler((x, z) -> 80 + (int) (30 * Math.sin(z / 40.0)));
        int ramps = 0;
        for (int boundary = -20; boundary <= 20; boundary++) {
            int start = boundary * 96;
            TrackProfiler.Ramp ramp = p.rampCovering(LINE, start);
            if (ramp == null) {
                continue;
            }
            ramps++;
            assertTrue(ramp.startAlong() > start,
                       "разрыв упёрся в начало отрезка " + boundary);
            assertTrue(ramp.endAlong() < start + 96,
                       "разрыв упёрся в конец отрезка " + boundary);
            assertEquals(ramp.startHeight(), p.heightAt(LINE, ramp.startAlong()));
            assertEquals(ramp.endHeight(), p.heightAt(LINE, ramp.endAlong()));
        }
        assertTrue(ramps > 5, "уклонов почти нет (" + ramps + "), проверка вхолостую");

        for (int node = -1536; node <= 1536; node += 768) {
            for (int offset = -3; offset <= 3; offset++) {
                assertFalse(p.isGap(LINE, node + offset),
                            "развязка на " + node + " попала в разрыв");
            }
        }
    }

    @Test
    void crossingsSitOnLevelGround() {
        // Развязка обязана быть горизонтальной: на уклоне пути двух линий
        // сошлись бы на разных высотах.
        TrackProfiler p = profiler((x, z) -> 80 + (int) (30 * Math.sin(z / 40.0)));
        for (int offset = -3; offset <= 3; offset++) {
            assertEquals(p.heightAt(LINE, 0), p.heightAt(LINE, offset),
                         "узел решётки попал на пандус, смещение " + offset);
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
        // Ущелье намеренно уведено от узла решётки: в узле высоту диктует
        // развязка, и тест мерил бы уже не перелёт, а согласование линий.
        TrackProfiler p = profiler((x, z) -> (z > 280 && z < 320) ? 40 : 90);
        assertTrue(p.heightAt(LINE, 300) > 70,
                   "профиль нырнул в ущелье вместо того, чтобы его перелететь");
    }

    @Test
    void crossingLinesAgreeOnHeight() {
        // Обе магистрали обязаны прийти в узел решётки на одну высоту, иначе
        // четыре ветви развязки сойдутся на разных уровнях и путь порвётся.
        HeightSampler rough = (x, z) ->
                80 + (int) (18 * Math.sin(x / 70.0) + 14 * Math.cos(z / 50.0));
        TrackProfiler p = profiler(rough);
        for (int i = -1; i <= 1; i++) {
            for (int k = -1; k <= 1; k++) {
                assertEquals(p.heightAt(new Line(Line.Axis.Z, i), k * 768),
                             p.heightAt(new Line(Line.Axis.X, k), i * 768),
                             "развязка (" + i + ", " + k + ") расходится по высоте");
            }
        }
    }
}
