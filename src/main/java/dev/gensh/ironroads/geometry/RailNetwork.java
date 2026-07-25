package dev.gensh.ironroads.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Геометрия сети магистралей. Класс без состояния сверх параметров конструктора
 * и без единой зависимости от Minecraft — это условие тестируемости.
 *
 * Линии образуют сетку с шагом spacing. Каждая линия виляет на величину до
 * jitter блоков по детерминированному шуму, но в точках решётки (кратных
 * spacing) джиттер плавно гасится до нуля — так пересечения всегда попадают
 * ровно в узел решётки и обе линии сходятся без подгонки.
 */
public final class RailNetwork {

    /** Период шума. Больше значение — длиннее и плавнее изгибы. */
    private static final int JITTER_PERIOD = 400;

    /** Длина зоны гашения джиттера по обе стороны от узла решётки. */
    private static final int CROSSING_FLAT = 64;

    private final long seed;
    private final int spacing;
    private final int jitter;

    public RailNetwork(long seed, int spacing, int jitter) {
        if (spacing < 4 * CROSSING_FLAT) {
            throw new IllegalArgumentException(
                "spacing должен быть не меньше " + (4 * CROSSING_FLAT));
        }
        if (jitter < 0) {
            throw new IllegalArgumentException("jitter не может быть отрицательным");
        }
        this.seed = seed;
        this.spacing = spacing;
        this.jitter = jitter;
    }

    public int spacing() {
        return spacing;
    }

    /** Перпендикулярная координата линии в точке along вдоль неё. */
    public int perpendicularAt(Line line, int along) {
        int base = Math.multiplyExact(line.index(), spacing);
        if (jitter == 0) {
            return base;
        }
        double noise = noise1D(lineSeed(line), (double) along / JITTER_PERIOD);
        double offset = noise * jitter * crossingTaper(along);
        return base + (int) Math.round(offset);
    }

    /**
     * Все линии, чей коридор попадает в квадрат радиуса radius вокруг точки.
     * Коридор шире номинала на jitter, потому что линия внутри него виляет.
     */
    public List<Line> linesNear(int blockX, int blockZ, int radius) {
        List<Line> result = new ArrayList<>(4);
        int reach = radius + jitter;
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

    /** Точка решётки, где магистрали пересекаются. */
    public boolean isCrossing(int blockX, int blockZ) {
        return Math.floorMod(blockX, spacing) == 0
            && Math.floorMod(blockZ, spacing) == 0;
    }

    /**
     * Коэффициент 0..1: гасит джиттер вблизи узлов решётки, чтобы линия
     * приходила в пересечение строго по базовой координате.
     */
    private double crossingTaper(int along) {
        int distance = Math.floorMod(along, spacing);
        distance = Math.min(distance, spacing - distance);
        if (distance >= CROSSING_FLAT) {
            return 1.0;
        }
        double t = (double) distance / CROSSING_FLAT;
        return t * t * (3.0 - 2.0 * t);
    }

    private long lineSeed(Line line) {
        return mix(seed ^ (line.axis() == Line.Axis.X ? 0x9E3779B9L : 0x7F4A7C15L)
                        ^ ((long) line.index() * 0x165667B1L));
    }

    /** Значение-шум с кубической интерполяцией, результат в [-1, 1]. */
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

    /** splitmix64 — быстрый и хорошо перемешивающий финализатор. */
    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
