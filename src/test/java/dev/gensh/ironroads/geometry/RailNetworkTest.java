package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RailNetworkTest {

    private static final long SEED = 123456789L;
    private static final int SPACING = 768;
    private static final int JITTER = 48;

    private RailNetwork net() {
        return new RailNetwork(SEED, SPACING, JITTER);
    }

    @Test
    void sameSeedAndPositionGiveSameResult() {
        Line line = new Line(Line.Axis.Z, 3);
        for (int along = -2000; along <= 2000; along += 37) {
            assertEquals(net().perpendicularAt(line, along),
                         net().perpendicularAt(line, along),
                         "перпендикуляр должен быть детерминирован");
        }
    }

    @Test
    void linesPassExactlyThroughLatticeCrossings() {
        Line line = new Line(Line.Axis.Z, 3);
        for (int m = -3; m <= 3; m++) {
            int along = m * SPACING;
            assertEquals(3 * SPACING, net().perpendicularAt(line, along),
                         "в точке пересечения джиттер обязан быть нулевым");
        }
    }

    @Test
    void jitterStaysWithinAmplitude() {
        Line line = new Line(Line.Axis.Z, -2);
        int base = -2 * SPACING;
        for (int along = -5000; along <= 5000; along++) {
            int perp = net().perpendicularAt(line, along);
            assertTrue(Math.abs(perp - base) <= JITTER,
                       "смещение " + (perp - base) + " превысило амплитуду на along=" + along);
        }
    }

    @Test
    void pathNeverTurnsSharperThanFortyFiveDegrees() {
        Line line = new Line(Line.Axis.Z, 1);
        RailNetwork n = net();
        int prev = n.perpendicularAt(line, -5000);
        for (int along = -4999; along <= 5000; along++) {
            int cur = n.perpendicularAt(line, along);
            assertTrue(Math.abs(cur - prev) <= 1,
                       "скачок " + (cur - prev) + " на along=" + along
                       + ": путь нельзя уложить прямыми и диагоналями");
            prev = cur;
        }
    }

    @Test
    void differentSeedsGiveDifferentPaths() {
        Line line = new Line(Line.Axis.Z, 0);
        RailNetwork a = new RailNetwork(1L, SPACING, JITTER);
        RailNetwork b = new RailNetwork(2L, SPACING, JITTER);
        boolean anyDifference = false;
        for (int along = 100; along < 700; along++) {
            if (a.perpendicularAt(line, along) != b.perpendicularAt(line, along)) {
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
