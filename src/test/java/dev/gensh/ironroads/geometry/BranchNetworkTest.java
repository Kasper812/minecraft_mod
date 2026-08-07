package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BranchNetworkTest {
    private static final int SPACING = 768;
    private static final int GRADE = 12;

    private static final HeightSampler HILLY = (x, z) ->
            80 + (int) (22 * Math.sin(x / 90.0) + 17 * Math.cos(z / 70.0));

    private RailNetwork network() {
        return new RailNetwork(4242L, SPACING, 0);
    }

    private static List<BranchNetwork.Branch> allBranches(BranchNetwork branches, int span) {
        return java.util.stream.IntStream.rangeClosed(-span, span).boxed()
                .flatMap(rx -> java.util.stream.IntStream.rangeClosed(-span, span)
                        .mapToObj(rz -> branches.branchFor(rx, rz)))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Test
    void villageGridMatchesVanillaSpread() {
        long seed = 4242L;
        for (int rx = -3; rx <= 3; rx++) {
            for (int rz = -3; rz <= 3; rz++) {
                Random expected = new Random(rx * 341873128712L + rz * 132897987541L
                                             + seed + 10387312);
                int chunkX = rx * 34 + expected.nextInt(26);
                int chunkZ = rz * 34 + expected.nextInt(26);

                BranchNetwork.Branch branch = new BranchNetwork(network()).branchFor(rx, rz);
                if (branch == null) {
                    continue;
                }
                assertEquals(chunkX * 16 + 8, branch.villageX());
                assertEquals(chunkZ * 16 + 8, branch.villageZ());
            }
        }
    }

    // Раскладку деревень задаёт мир, а не мод: мод на структуры меняет шаг и
    // соль, и ветки обязаны целиться туда, где деревни стоят у него.
    @Test
    void branchesFollowTheWorldsVillageLayout() {
        int cellChunks = 20;
        int cell = cellChunks * 16;
        VillageSites shifted = new VillageSites() {
            @Override
            public int spacingChunks() {
                return cellChunks;
            }

            @Override
            public long siteAt(long seed, int regionX, int regionZ) {
                return VillageSites.pack(regionX * cell + 200, regionZ * cell + 200);
            }
        };

        BranchNetwork branches = new BranchNetwork(new RailNetwork(4242L, SPACING, 0, shifted));
        List<BranchNetwork.Branch> found = allBranches(branches, 6);
        assertFalse(found.isEmpty(), "новая раскладка не дала ни одной ветки");
        assertNotEquals(allBranches(new BranchNetwork(network()), 6), found,
                        "ветки не заметили чужой раскладки");

        for (BranchNetwork.Branch branch : found) {
            assertEquals(200, Math.floorMod(branch.villageX(), cell),
                         "ветка целится не в деревню мира");
            assertEquals(200, Math.floorMod(branch.villageZ(), cell));
        }

        // Перебор кандидатов тоже идёт по чужому шагу, иначе ветка есть,
        // но её никто не находит при укладке чанка.
        BranchNetwork.Branch first = found.get(0);
        boolean alongX = first.line().axis() == Line.Axis.X;
        int perpendicular = first.line().index() * SPACING + first.line().offset();
        int x = alongX ? first.toAlong() : perpendicular;
        int z = alongX ? perpendicular : first.toAlong();
        assertTrue(branches.branchesNear(x, z, 12).contains(first),
                   "ветка потеряна при переборе регионов");
    }

    @Test
    void branchesRunFromAMainlineAndStayShort() {
        BranchNetwork branches = new BranchNetwork(network());
        List<BranchNetwork.Branch> found = allBranches(branches, 6);
        assertTrue(found.size() > 10, "веток почти нет (" + found.size() + ")");

        for (BranchNetwork.Branch branch : found) {
            Line line = branch.line();
            assertEquals(0, Math.floorMod(line.anchorAlong(), SPACING),
                         "ветка отходит не от магистрали");
            assertTrue(Math.abs(branch.toAlong() - line.anchorAlong())
                       <= BranchNetwork.MAX_LENGTH,
                       "ветка длиннее половины шага решётки");
            assertNotEquals(0, line.offset(), "ветка легла поверх магистрали");
        }
    }

    // Дорога кончается на околице, а не в деревне: конечная станция посреди
    // застройки — это насыпь поперёк улицы и выемка перед дверью.
    @Test
    void branchesStopOutsideTheVillage() {
        List<BranchNetwork.Branch> found = allBranches(new BranchNetwork(network()), 6);
        assertFalse(found.isEmpty());

        for (BranchNetwork.Branch branch : found) {
            boolean alongX = branch.line().axis() == Line.Axis.X;
            int villageAlong = alongX ? branch.villageX() : branch.villageZ();
            int stop = Math.abs(villageAlong - branch.toAlong());

            assertTrue(stop >= 64, "ветка встала в " + stop + " блоках от центра деревни");
            assertTrue(Math.abs(branch.toAlong() - branch.line().anchorAlong())
                       > BranchNetwork.TURNOUT_REACH,
                       "станция уехала за стрелку — ветки не осталось");
        }
    }

    @Test
    void branchIsFoundAlongItsWholeLength() {
        BranchNetwork branches = new BranchNetwork(network());
        BranchNetwork.Branch branch = allBranches(branches, 6).get(0);
        RailNetwork network = network();

        int from = Math.min(branch.fromAlong(), branch.toAlong());
        int to = Math.max(branch.fromAlong(), branch.toAlong());

        for (int along = from; along <= to; along += 7) {
            int perpendicular = network.perpendicularAt(branch.line(), along);
            boolean alongX = branch.line().axis() == Line.Axis.X;
            int x = alongX ? along : perpendicular;
            int z = alongX ? perpendicular : along;

            assertTrue(branches.branchesNear(x, z, 12).contains(branch),
                       "ветка потеряна в точке " + x + "," + z);
        }
    }

    @Test
    void branchMeetsItsMainlineAtTheSameHeight() {
        TrackProfiler profiler = new TrackProfiler(network(), GRADE, HILLY);
        BranchNetwork branches = new BranchNetwork(network());
        List<BranchNetwork.Branch> found = allBranches(branches, 6);
        assertFalse(found.isEmpty());

        for (BranchNetwork.Branch branch : found) {
            Line spur = branch.line();
            Line main = new Line(spur.crossAxis(),
                                 Math.floorDiv(spur.anchorAlong(), SPACING));
            int junction = spur.index() * SPACING + spur.offset();

            assertEquals(profiler.heightAt(main, junction),
                         profiler.heightAt(spur, branch.fromAlong()),
                         "ветка и магистраль разошлись по высоте на стрелке");
        }
    }

    @Test
    void branchProfileNeverJumps() {
        TrackProfiler profiler = new TrackProfiler(network(), GRADE, HILLY);
        BranchNetwork branches = new BranchNetwork(network());
        int changes = 0;

        for (BranchNetwork.Branch branch : allBranches(branches, 4)) {
            int from = Math.min(branch.fromAlong(), branch.toAlong());
            int to = Math.max(branch.fromAlong(), branch.toAlong());
            int prev = profiler.heightAt(branch.line(), from);

            for (int along = from + 1; along <= to; along++) {
                int cur = profiler.heightAt(branch.line(), along);
                assertTrue(Math.abs(cur - prev) <= 1,
                           "подъём " + Math.abs(cur - prev) + " за блок на ветке");
                if (cur != prev) {
                    changes++;
                }
                prev = cur;
            }
        }
        assertTrue(changes > 20,
                   "ветки почти плоские (" + changes + "), проверка вхолостую");
    }

    @Test
    void branchesAreDeterministic() {
        BranchNetwork first = new BranchNetwork(network());
        BranchNetwork second = new BranchNetwork(network());
        for (int rx = -4; rx <= 4; rx++) {
            for (int rz = -4; rz <= 4; rz++) {
                assertEquals(first.branchFor(rx, rz), second.branchFor(rx, rz));
            }
        }
    }
}
