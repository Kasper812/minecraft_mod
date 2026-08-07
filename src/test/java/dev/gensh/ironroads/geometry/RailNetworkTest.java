package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RailNetworkTest {
    private static final long SEED = 123456789L;
    private static final int SPACING = 768;
    private static final int ALIGNMENT = 40;

    private static final int SEGMENT = TrackProfiler.SEGMENT;

    private RailNetwork net() {
        return new RailNetwork(SEED, SPACING, ALIGNMENT);
    }

    @Test
    void sameSeedAndPositionGiveSameResult() {
        Line line = new Line(Line.Axis.Z, 3);
        for (int along = -2000; along <= 2000; along += 37) {
            assertEquals(net().alignmentAt(line, along),
                         net().alignmentAt(line, along),
                         "проектная ось должна быть детерминирована");
        }
    }

    @Test
    void linesPassExactlyThroughLatticeCrossings() {
        Line line = new Line(Line.Axis.Z, 3);
        for (int m = -3; m <= 3; m++) {
            int along = m * SPACING;
            assertEquals(3 * SPACING, net().perpendicularAt(line, along),
                         "ось линии не имеет права зависеть от точки");
        }
    }

    @Test
    void alignmentStaysWithinAmplitude() {
        Line line = new Line(Line.Axis.Z, -2);
        for (int along = -5000; along <= 5000; along++) {
            assertTrue(Math.abs(net().alignmentAt(line, along)) <= ALIGNMENT,
                       "проектная ось ушла за амплитуду на along=" + along);
        }
    }

    @Test
    void alignmentTurnsNoSharperThanTheProfileCanFollow() {
        Line line = new Line(Line.Axis.Z, 1);
        RailNetwork n = net();
        int swings = 0;

        for (int along = -5000; along + SEGMENT <= 5000; along += SEGMENT) {
            int swing = n.alignmentAt(line, along + SEGMENT) - n.alignmentAt(line, along);
            assertTrue(Math.abs(swing) <= 12,
                       "увод " + swing + " блоков за отрезок на along=" + along
                       + ": такой поворот не уложить кривой Безье");
            if (swing != 0) {
                swings++;
            }
        }
        assertTrue(swings > 50, "трасса почти не гуляет (" + swings + "), проверка вхолостую");
    }

    @Test
    void alignmentActuallyLeavesTheAxis() {
        Line line = new Line(Line.Axis.Z, 1);
        int worst = 0;
        for (int along = 0; along <= 30_000; along++) {
            worst = Math.max(worst, Math.abs(net().alignmentAt(line, along)));
        }
        assertTrue(worst >= ALIGNMENT / 2,
                   "трасса ушла от оси всего на " + worst + " блоков — это прямая");
    }

    @Test
    void differentSeedsGiveDifferentPaths() {
        Line line = new Line(Line.Axis.Z, 0);
        RailNetwork a = new RailNetwork(1L, SPACING, ALIGNMENT);
        RailNetwork b = new RailNetwork(2L, SPACING, ALIGNMENT);
        boolean anyDifference = false;
        for (int along = 100; along < 700; along++) {
            if (a.alignmentAt(line, along) != b.alignmentAt(line, along)) {
                anyDifference = true;
                break;
            }
        }
        assertTrue(anyDifference, "разные сиды обязаны давать разные трассы");
    }

    @Test
    void linesNearFindsBothAxesAroundOrigin() {
        List<Line> found = net().linesNear(0, 0, 64);
        assertTrue(found.contains(new Line(Line.Axis.Z, 0)));
        assertTrue(found.contains(new Line(Line.Axis.X, 0)));
    }

    @Test
    void linesNearIgnoresDistantCorridors() {
        List<Line> found = net().linesNear(SPACING / 2, SPACING / 2, 16);
        assertTrue(found.isEmpty(), "в середине ячейки сетки линий быть не должно");
    }

    @Test
    void crossingDetectedAtLatticePoint() {
        assertTrue(net().isCrossing(SPACING, -SPACING));
        assertFalse(net().isCrossing(SPACING / 2, -SPACING));
    }
}
