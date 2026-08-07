package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteDeviationTest {
    private static final int GRADE = 12;
    private static final int SPACING = 1536;
    private static final int ALIGNMENT = 40;
    private static final Line LINE = new Line(Line.Axis.Z, 0);

    private static TrackProfiler profiler(HeightSampler terrain) {
        return new TrackProfiler(new RailNetwork(42L, SPACING, ALIGNMENT, VillageSites.EMPTY),
                                 GRADE, terrain);
    }

    private static int deviation(TrackProfiler p, Line line, int along) {
        return p.perpendicularAt(line, along) - p.network().perpendicularAt(line, along);
    }

    private static int maxDeviation(TrackProfiler p, int from, int to) {
        int worst = 0;
        for (int along = from; along <= to; along++) {
            worst = Math.max(worst, Math.abs(deviation(p, LINE, along)));
        }
        return worst;
    }

    /** Холм посреди пролёта: конус радиусом 40 и высотой 40 сидит прямо на оси. */
    private static HeightSampler mountain() {
        return (x, z) -> {
            double distance = Math.hypot(x, z - 384);
            return 70 + (int) Math.max(0, 40 - distance);
        };
    }

    @Test
    void calmGroundStillCurves() {
        TrackProfiler p = profiler((x, z) -> 70);
        assertTrue(maxDeviation(p, 0, 4 * SPACING) >= ALIGNMENT / 2,
                   "на ровном месте дорога пошла по линейке");
    }

    @Test
    void curvesAreSeparatedByStraightRuns() {
        TrackProfiler p = profiler((x, z) -> 70);
        int straight = 0;
        int longest = 0;

        for (int along = 1; along <= 4 * SPACING; along++) {
            if (deviation(p, LINE, along) == deviation(p, LINE, along - 1)) {
                straight++;
                longest = Math.max(longest, straight);
            } else {
                straight = 0;
            }
        }
        assertTrue(longest >= 2 * TrackProfiler.SEGMENT,
                   "самая длинная прямая вставка всего " + longest + " блоков");
    }

    @Test
    void theLineStepsAsideFromAMountain() {
        TrackProfiler p = profiler(mountain());
        assertTrue(maxDeviation(p, 0, SPACING) >= 8,
                   "дорога пошла сквозь холм вместо того, чтобы его обойти");
    }

    @Test
    void theLineStepsAsideFromWater() {
        HeightSampler lake = new HeightSampler() {
            @Override
            public int heightAt(int x, int z) {
                return 70;
            }

            @Override
            public int waterDepth(int x, int z) {
                return z > 200 && z < 560 && Math.abs(x) < 10 ? 8 : 0;
            }
        };
        TrackProfiler p = profiler(lake);
        assertTrue(maxDeviation(p, 0, SPACING) >= 8,
                   "дорога пошла прямо через воду, хотя рядом есть сухой берег");
    }

    @Test
    void deviationStaysWithinItsLimit() {
        TrackProfiler p = profiler(mountain());
        assertTrue(maxDeviation(p, -SPACING, 2 * SPACING) <= TrackProfiler.MAX_SHIFT,
                   "отклонение вышло за MAX_SHIFT, коридор поиска линий его не покроет");
    }

    @Test
    void deviationsVanishAtCrossings() {
        TrackProfiler p = profiler(mountain());
        for (int node = -SPACING; node <= 2 * SPACING; node += SPACING) {
            for (int offset = -TrackProfiler.SEGMENT; offset <= TrackProfiler.SEGMENT; offset++) {
                assertEquals(0, deviation(p, LINE, node + offset),
                             "развязка на " + node + " съехала с оси, смещение " + offset);
            }
        }
    }

    @Test
    void everySidestepHappensInsideAGap() {
        TrackProfiler p = profiler(mountain());
        int steps = 0;
        for (int along = 1; along <= 2 * SPACING; along++) {
            if (deviation(p, LINE, along) == deviation(p, LINE, along - 1)) {
                continue;
            }
            steps++;
            assertTrue(p.isGap(LINE, along) || p.isGap(LINE, along - 1),
                       "путь шагнул вбок на along=" + along
                       + " вне разрыва — блоки пути так не стыкуются");
        }
        assertTrue(steps > 10, "боковых шагов почти нет (" + steps + "), проверка вхолостую");
    }

    /**
     * Главное свойство обхода: там, где деревня легла прямо на ось, путь идёт не
     * сквозь неё, а в стороне — на всю ширину, которую разрешает решётка.
     */
    @Test
    void theLineGoesAroundAVillage() {
        RailNetwork network = new RailNetwork(42L, SPACING, ALIGNMENT, VillageSites.VANILLA);
        TrackProfiler p = new TrackProfiler(network, GRADE, (x, z) -> 70);
        BranchNetwork villages = new BranchNetwork(network);

        int room = 3 * TrackProfiler.SEGMENT;
        int checked = 0;

        for (int index = -3; index <= 3; index++) {
            Line line = new Line(Line.Axis.Z, index);
            for (int along = -3 * SPACING; along <= 3 * SPACING; along++) {
                long village = villages.villageNear(line, along, 1, TrackProfiler.MAX_SHIFT);
                if (village == VillageSites.NONE) {
                    continue;
                }
                // У деревни на развязке или у стрелки увода нет по построению:
                // путь обязан вернуться на ось. Берём те, где место есть.
                int toCrossing = Math.floorMod(along, SPACING);
                toCrossing = Math.min(toCrossing, SPACING - toCrossing);
                if (toCrossing < room
                 || villages.turnoutDistance(line, along, room) < room) {
                    continue;
                }
                int gap = Math.abs(p.perpendicularAt(line, along) - (int) (village >> 32));
                checked++;
                assertTrue(gap >= 32,
                           "путь прошёл в " + gap + " блоках от центра деревни на "
                           + index + "/" + along);
            }
        }
        assertTrue(checked > 0, "деревень на оси не нашлось, проверка вхолостую");
        assertTrue(maxDeviation(p, -3 * SPACING, 3 * SPACING) <= TrackProfiler.MAX_SHIFT,
                   "обход вышел за MAX_SHIFT, коридор поиска линий его не покроет");
    }

    /** Обход — такой же боковой шаг, как и от горы: только внутри разрыва. */
    @Test
    void theDetourStepsOnlyInsideGaps() {
        RailNetwork network = new RailNetwork(42L, SPACING, ALIGNMENT, VillageSites.VANILLA);
        TrackProfiler p = new TrackProfiler(network, GRADE, (x, z) -> 70);

        int steps = 0;
        for (int along = -2 * SPACING + 1; along <= 2 * SPACING; along++) {
            if (deviation(p, LINE, along) == deviation(p, LINE, along - 1)) {
                continue;
            }
            steps++;
            assertTrue(p.isGap(LINE, along) || p.isGap(LINE, along - 1),
                       "обход шагнул вбок вне разрыва на along=" + along);
        }
        assertTrue(steps > 10, "боковых шагов почти нет (" + steps + "), проверка вхолостую");
    }

    @Test
    void theLineGoesAroundAnyStructure() {
        VillageSites ruins = new VillageSites() {
            @Override
            public int spacingChunks() {
                return 32;
            }

            @Override
            public long siteAt(long seed, int regionX, int regionZ) {
                return regionX == 0 && regionZ == 0
                     ? VillageSites.pack(0, SPACING / 2) : NONE;
            }
        };
        TrackProfiler p = new TrackProfiler(
                new RailNetwork(42L, SPACING, ALIGNMENT, VillageSites.EMPTY, ruins),
                GRADE, (x, z) -> 70);

        int gap = Math.abs(p.perpendicularAt(LINE, SPACING / 2));
        assertTrue(gap >= 32, "путь прошёл в " + gap + " блоках от центра постройки");
    }

    @Test
    void turnoutsKeepTheirStraightApproach() {
        RailNetwork network = new RailNetwork(42L, SPACING, ALIGNMENT);
        TrackProfiler p = new TrackProfiler(network, GRADE, mountain());
        BranchNetwork branches = new BranchNetwork(network);

        int checked = 0;
        for (int rx = -6; rx <= 6; rx++) {
            for (int rz = -6; rz <= 6; rz++) {
                BranchNetwork.Branch branch = branches.branchFor(rx, rz);
                if (branch == null) {
                    continue;
                }
                Line spur = branch.line();
                Line main = new Line(spur.crossAxis(),
                                     Math.floorDiv(spur.anchorAlong(), SPACING));
                int junction = spur.index() * SPACING + spur.offset();
                checked++;

                for (int offset = -Junctions.REACH; offset <= Junctions.REACH; offset++) {
                    assertEquals(0, deviation(p, main, junction + offset),
                                 "магистраль ушла вбок у стрелки на " + junction
                                 + ", смещение " + offset + ": съезду не за что зацепиться");
                }
            }
        }
        assertTrue(checked > 5, "веток не нашлось (" + checked + "), проверка вхолостую");
    }
}
