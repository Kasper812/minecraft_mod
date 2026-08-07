package dev.gensh.ironroads.geometry;

import java.util.ArrayList;
import java.util.List;

public final class RailNetwork {
    private static final int ALIGN_PERIOD = 5120;

    private static final int ALIGN_FINE_PERIOD = 1792;

    private static final double FINE_SHARE = 0.25;

    private static final double ALIGN_GAIN = 2.0;

    private static final int MIN_SPACING = 8 * TrackProfiler.SEGMENT;

    private final long seed;
    private final int spacing;
    private final int alignment;
    private final VillageSites villages;
    private final VillageSites obstacles;

    public RailNetwork(long seed, int spacing, int alignment) {
        this(seed, spacing, alignment, VillageSites.VANILLA);
    }

    public RailNetwork(long seed, int spacing, int alignment, VillageSites villages) {
        this(seed, spacing, alignment, villages, villages);
    }

    public RailNetwork(long seed, int spacing, int alignment,
                       VillageSites villages, VillageSites obstacles) {
        if (spacing < MIN_SPACING) {
            throw new IllegalArgumentException("spacing должен быть не меньше " + MIN_SPACING);
        }
        if (alignment < 0) {
            throw new IllegalArgumentException("alignment не может быть отрицательным");
        }
        this.seed = seed;
        this.spacing = spacing;
        this.alignment = alignment;
        this.villages = villages;
        this.obstacles = obstacles;
    }

    public int spacing() {
        return spacing;
    }

    public VillageSites villages() {
        return villages;
    }

    public VillageSites obstacles() {
        return obstacles;
    }

    public long seed() {
        return seed;
    }

    public int perpendicularAt(Line line, int along) {
        int base = Math.multiplyExact(line.index(), spacing);
        return line.isBranch() ? base + line.offset() : base;
    }

    public int alignmentAt(Line line, int along) {
        if (line.isBranch() || alignment == 0) {
            return 0;
        }
        long lineSeed = lineSeed(line);
        double value = (1 - FINE_SHARE) * noise1D(lineSeed, (double) along / ALIGN_PERIOD)
                     + FINE_SHARE * noise1D(mix(lineSeed ^ 0x2545F4914F6CDD1DL),
                                            (double) along / ALIGN_FINE_PERIOD);

        double swept = Math.max(-1.0, Math.min(1.0, value * ALIGN_GAIN));
        return (int) Math.round(swept * alignment);
    }

    public List<Line> linesNear(int blockX, int blockZ, int radius) {
        List<Line> result = new ArrayList<>(4);
        int reach = radius + TrackProfiler.MAX_SHIFT;
        collect(result, Line.Axis.Z, blockX, reach);
        collect(result, Line.Axis.X, blockZ, reach);
        return result;
    }

    private void collect(List<Line> out, Line.Axis axis, int perpendicular, int reach) {
        int first = Math.floorDiv(perpendicular - reach, spacing);
        int last = Math.floorDiv(perpendicular + reach, spacing);
        for (int index = first; index <= last; index++) {
            int base = index * spacing;
            if (Math.abs(base - perpendicular) <= reach) {
                out.add(new Line(axis, index));
            }
        }
    }

    public boolean isCrossing(int blockX, int blockZ) {
        return Math.floorMod(blockX, spacing) == 0
            && Math.floorMod(blockZ, spacing) == 0;
    }

    private long lineSeed(Line line) {
        return mix(seed ^ (line.axis() == Line.Axis.X ? 0x9E3779B9L : 0x7F4A7C15L)
                        ^ ((long) line.index() * 0x165667B1L));
    }

    private static double noise1D(long lineSeed, double t) {
        int cell = (int) Math.floor(t);
        double f = t - cell;
        double a = unitAt(lineSeed, cell);
        double b = unitAt(lineSeed, cell + 1);
        double smooth = f * f * (3.0 - 2.0 * f);
        return (a + (b - a) * smooth) * 2.0 - 1.0;
    }

    private static double unitAt(long lineSeed, int cell) {
        long h = mix(lineSeed ^ ((long) cell * 0x27D4EB2FL));
        return (h >>> 11) * 0x1.0p-53;
    }

    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
