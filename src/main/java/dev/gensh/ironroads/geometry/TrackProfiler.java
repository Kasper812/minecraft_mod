package dev.gensh.ironroads.geometry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TrackProfiler {
    public record Ramp(int startAlong, int endAlong, int startHeight, int endHeight) {}

    private record Key(Line line, int at) {}

    private record ShiftKey(Line line, int boundary, int offset) {}

    private record CrossKey(int zIndex, int xIndex) {}

    private static final int SMOOTH_RADIUS = 64;

    private static final int ENVELOPE_RADIUS = 512;

    private static final int STRIDE = 32;

    public static final int SEGMENT = 96;

    private static final int SLOPE_RUN = 10;

    private static final int MIN_FLAT = Junctions.REACH + 2;

    private static final int MAX_SPAN = SEGMENT - 2 * MIN_FLAT;

    /**
     * Предельный увод линии от оси. По нему же считается коридор поиска линий
     * ({@link RailNetwork#linesNear}): линия, ушедшая дальше, не нашлась бы при
     * укладке своего чанка и просто не построилась бы.
     */
    public static final int MAX_SHIFT = 56;

    private static final int TERRAIN_SHIFT = 40;

    private static final int SHIFT_STEP = 8;

    private static final int DETOUR_STEP = 20;

    private static final int STRUCTURE_CLEARANCE = MAX_SHIFT;

    private static final int DETOUR_LOOKAHEAD = 6 * SEGMENT;

    private static final int SHIFT_GUARD = 1;

    private static final int SHIFT_RUN = 8;

    // Порог «здесь дорого идти прямо»: подъём в блоках на 64 блока пути. Ниже —
    // трасса не ищет обход вовсе и не платит за выборки рельефа в стороне.
    // Это главный регулятор цены профиля: 10 давало 385 выборок на первом чанке
    // рваного рельефа против 133 на спокойном.
    private static final int SHIFT_TROUBLE = 16;

    private static final int SHIFT_GAIN = 4;

    private static final int WATER_WEIGHT = 3;

    private final RailNetwork network;
    private final BranchNetwork spurs;
    private final int maxGradeRun;
    private final HeightSampler terrain;

    private final Map<Key, Double> smoothCache = new ConcurrentHashMap<>();

    private final Map<Long, Integer> terrainCache = new ConcurrentHashMap<>();

    private final Map<CrossKey, Double> crossingCache = new ConcurrentHashMap<>();

    private final Map<Key, Integer> levelCache = new ConcurrentHashMap<>();

    private final Map<Key, Integer> shiftCache = new ConcurrentHashMap<>();

    private final Map<ShiftKey, Integer> costCache = new ConcurrentHashMap<>();

    public TrackProfiler(RailNetwork network, int maxGradeRun, HeightSampler terrain) {
        if (maxGradeRun < 1) {
            throw new IllegalArgumentException("maxGradeRun должен быть положительным");
        }
        this.network = network;
        this.spurs = new BranchNetwork(network);
        this.maxGradeRun = maxGradeRun;
        this.terrain = terrain;
    }

    public RailNetwork network() {
        return network;
    }

    public HeightSampler terrain() {
        return terrain;
    }

    public int perpendicularAt(Line line, int along) {
        return network.perpendicularAt(line, along) + shiftFor(line, along);
    }

    public int heightAt(Line line, int along) {
        Ramp ramp = rampCovering(line, along);
        if (ramp == null) {
            return levelAt(line, Math.floorDiv(along, SEGMENT));
        }
        return ease(ramp, along, ramp.startHeight(), ramp.endHeight());
    }

    /**
     * Значение внутри разрыва под кривую.
     *
     * Не линейная интерполяция, а ровно та кубика, по которой Create тянет
     * {@code BezierConnection} между концами пандуса: у неё на обоих концах
     * горизонтальная касательная, и от хорды она отходит на 0.096 перепада — почти
     * на блок при перепаде в восемь. Пока полотно строилось по хорде, а поезд ехал
     * по кривой, на подъёме балласт вылезал на путь, а на боковом сдвиге состав шёл
     * сквозь стенку выемки. Считаем то же, что и Create: расхождение исчезает по
     * построению, а не становится маленьким.
     *
     * Побочный выигрыш ровно тот, что нужен дороге: у пандуса появились
     * вертикальные сопряжения — уклон нарастает от нуля, а не ломается сразу.
     */
    private static int ease(Ramp ramp, int along, int from, int to) {
        if (along <= ramp.startAlong()) {
            return from;
        }
        if (along >= ramp.endAlong()) {
            return to;
        }
        double t = (double) (along - ramp.startAlong())
                 / (ramp.endAlong() - ramp.startAlong());
        return from + (int) Math.round((to - from) * t * t * (3 - 2 * t));
    }

    public Ramp rampCovering(Line line, int along) {
        int boundary = Math.floorDiv(along, SEGMENT);
        int from = levelAt(line, boundary);
        int to = levelAt(line, boundary + 1);
        int fromShift = shiftAt(line, boundary);
        int toShift = shiftAt(line, boundary + 1);
        if (from == to && fromShift == toShift) {
            return null;
        }
        int span = spanFor(to - from, toShift - fromShift);
        int start = boundary * SEGMENT + (SEGMENT - span) / 2;

        return new Ramp(start, start + span, from, to);
    }

    private int spanFor(int rise, int swing) {
        int wanted = Math.max(Math.abs(rise) * SLOPE_RUN, Math.abs(swing) * SHIFT_RUN);
        return Math.min(wanted, MAX_SPAN);
    }

    public boolean isGap(Line line, int along) {
        Ramp ramp = rampCovering(line, along);
        return ramp != null && along > ramp.startAlong() && along < ramp.endAlong();
    }

    public int clearOfGap(Line line, int along, int direction) {
        for (int step = 0; step < SEGMENT; step++) {
            int at = along + direction * step;
            if (!isGap(line, at)) {
                return at;
            }
        }
        return along;
    }

    /**
     * Где на самом деле встанет остановка у деревни.
     *
     * <p>Платформу и станцию Create нельзя ставить в разрыв под кривую: там нет
     * ни полотна, ни пути. Раньше от разрыва спасало то, что у остановки путь
     * держали на оси, — но обход деревни как раз и делается разрывами, и это
     * правило означало «деревню с остановкой не обходить никогда». Теперь
     * остановка отъезжает вдоль пути до ближайшего целого места, одинаково у
     * фичи и у сшивания.
     */
    public int haltAlong(Line line, int along) {
        return clearOfGap(line, along, 1);
    }

    public boolean isReady(Line line, int along) {
        int boundary = Math.floorDiv(along, SEGMENT);
        return ready(line, boundary) && ready(line, boundary + 1);
    }

    // Сдвиг проверяется наравне с высотой: он тоже стоит выборок рельефа, и без
    // этой проверки холодный сдвиг доставался бы серверному потоку, ради чего и
    // заведён прогрев. У ветки сдвига нет и кеша для него тоже.
    private boolean ready(Line line, int boundary) {
        Key key = new Key(line, boundary);
        return levelCache.containsKey(key)
            && (line.isBranch() || shiftCache.containsKey(key));
    }

    public boolean isRampEnd(Line line, int along) {
        Ramp ramp = rampCovering(line, along);
        return ramp != null && (along == ramp.startAlong() || along == ramp.endAlong());
    }

    private int shiftFor(Line line, int along) {
        if (line.isBranch()) {
            return 0;
        }
        int boundary = Math.floorDiv(along, SEGMENT);
        int from = shiftAt(line, boundary);
        int to = shiftAt(line, boundary + 1);
        if (from == to) {
            return from;
        }
        // Той же кубикой, что и высота: боковой увод — вторая координата той же
        // кривой Безье, и разъехаться им нельзя.
        return ease(rampCovering(line, along), along, from, to);
    }

    private int shiftAt(Line line, int boundary) {
        if (line.isBranch()) {
            return 0;
        }
        Key key = new Key(line, boundary);
        Integer hit = shiftCache.get(key);
        if (hit != null) {
            return hit;
        }
        int value = computeShift(line, boundary);
        shiftCache.put(key, value);
        return value;
    }

    private int computeShift(Line line, int boundary) {
        int along = boundary * SEGMENT;
        long structure = spurs.obstacleNear(line, along, DETOUR_LOOKAHEAD, STRUCTURE_CLEARANCE);
        int cone = detourCone(line, along, structure);

        int step = cone > 0 ? DETOUR_STEP : SHIFT_STEP;
        int limit = shiftLimit(line, along, step, Math.max(TERRAIN_SHIFT, cone));
        if (limit == 0) {
            return 0;
        }
        int previous = clamp(shiftAt(line, boundary - 1), limit);

        if (cone > 0) {
            return towards(previous, detourSide(line, along, structure) * limit, step, limit);
        }

        int design = clamp(tangent(network.alignmentAt(line, along)), limit);

        int cost = segmentCost(line, boundary, design);
        if (cost <= SHIFT_TROUBLE) {
            return towards(previous, design, SHIFT_STEP, limit);
        }

        int best = design;
        int bestCost = cost;
        for (int candidate : new int[] { design - SHIFT_STEP, design + SHIFT_STEP,
                                         -limit, limit }) {
            if (candidate == design || Math.abs(candidate) > limit) {
                continue;
            }
            int candidateCost = segmentCost(line, boundary, candidate);
            if (candidateCost + SHIFT_GAIN < bestCost) {
                best = candidate;
                bestCost = candidateCost;
            }
        }
        return towards(previous, best, SHIFT_STEP, limit);
    }

    private static int tangent(int alignment) {
        return Math.round((float) alignment / SHIFT_STEP) * SHIFT_STEP;
    }

    private static int towards(int previous, int target, int step, int limit) {
        int move = Math.max(-step, Math.min(step, target - previous));
        return clamp(previous + move, limit);
    }

    /**
     * Насколько увод разрешён здесь: и до развязки на решётке, и до стрелки путь
     * обязан вернуться на ось, а вернуться он может не быстрее {@code step} за
     * отрезок — отсюда линейный конус в обе стороны от обеих помех.
     */
    private int shiftLimit(Line line, int along, int step, int cap) {
        int spacing = network.spacing();
        int distance = Math.floorMod(along, spacing);
        distance = Math.min(distance, spacing - distance);
        distance = Math.min(distance, spurs.turnoutDistance(line, along, DETOUR_LOOKAHEAD));

        int room = step * (distance / SEGMENT - SHIFT_GUARD);
        return Math.max(0, Math.min(cap, room));
    }

    private int detourCone(Line line, int along, long structure) {
        if (structure == VillageSites.NONE) {
            return 0;
        }
        boolean alongX = line.axis() == Line.Axis.X;
        int structureAlong = alongX ? (int) (structure >> 32) : (int) structure;
        int distance = Math.abs(structureAlong - along);

        return STRUCTURE_CLEARANCE * (DETOUR_LOOKAHEAD - distance) / DETOUR_LOOKAHEAD;
    }

    private int detourSide(Line line, int along, long structure) {
        boolean alongX = line.axis() == Line.Axis.X;
        int structureSide = alongX ? (int) structure : (int) (structure >> 32);

        return structureSide > network.perpendicularAt(line, along) ? -1 : 1;
    }

    private static int clamp(int value, int limit) {
        return Math.max(-limit, Math.min(limit, value));
    }

    private int segmentCost(Line line, int boundary, int offset) {
        ShiftKey key = new ShiftKey(line, boundary, offset);
        Integer hit = costCache.get(key);
        if (hit != null) {
            return hit;
        }
        int start = boundary * SEGMENT;
        int climb = 0;
        int water = 0;
        int previous = 0;

        // Начало отрезка не щупаем: его уже прошёл предыдущий отрезок, а лишняя
        // выборка на каждую полосу-кандидата — самая дорогая строчка в профиле.
        for (int at = STRIDE; at <= SEGMENT; at += STRIDE) {
            int perpendicular = network.perpendicularAt(line, start + at) + offset;
            int x = line.axis() == Line.Axis.Z ? perpendicular : start + at;
            int z = line.axis() == Line.Axis.Z ? start + at : perpendicular;

            int height = sampleAt(x, z);
            if (at > STRIDE) {
                climb += Math.abs(height - previous);
            }
            previous = height;
            water += terrain.waterDepth(x, z);
        }

        int value = climb + WATER_WEIGHT * water;
        costCache.put(key, value);
        return value;
    }

    private int levelAt(Line line, int boundary) {
        Key key = new Key(line, boundary);
        Integer hit = levelCache.get(key);
        if (hit != null) {
            return hit;
        }
        int value = (int) Math.round(anchoredHeightAt(line, boundary * SEGMENT));
        levelCache.put(key, value);
        return value;
    }

    private double anchoredHeightAt(Line line, int along) {
        if (line.isBranch()) {
            return branchHeightAt(line, along);
        }
        double best = envelopeAt(line, along);

        int spacing = network.spacing();
        int firstIndex = Math.floorDiv(along, spacing);
        for (int index = firstIndex; index <= firstIndex + 1; index++) {
            double candidate = crossingHeight(line, index)
                             + (double) Math.abs(index * spacing - along) / maxGradeRun;
            if (candidate < best) {
                best = candidate;
            }
        }
        return best;
    }

    private double branchHeightAt(Line line, int along) {
        int spacing = network.spacing();
        Line main = new Line(line.crossAxis(), Math.floorDiv(line.anchorAlong(), spacing));
        double junction = heightAt(main, line.index() * spacing + line.offset());
        double reach = (double) Math.abs(along - line.anchorAlong()) / maxGradeRun;

        return Math.max(junction - reach, Math.min(envelopeAt(line, along), junction + reach));
    }

    private double envelopeAt(Line line, int along) {
        int firstNode = Math.floorDiv(along - ENVELOPE_RADIUS, STRIDE) * STRIDE;
        int lastNode = Math.floorDiv(along + ENVELOPE_RADIUS, STRIDE) * STRIDE;

        double best = Double.MAX_VALUE;
        for (int node = firstNode; node <= lastNode; node += STRIDE) {
            double candidate = cachedSmoothed(line, node)
                             + (double) Math.abs(node - along) / maxGradeRun;
            if (candidate < best) {
                best = candidate;
            }
        }
        return best;
    }

    private double crossingHeight(Line line, int crossingIndex) {
        boolean alongZ = line.axis() == Line.Axis.Z;
        int zIndex = alongZ ? line.index() : crossingIndex;
        int xIndex = alongZ ? crossingIndex : line.index();

        CrossKey key = new CrossKey(zIndex, xIndex);
        Double hit = crossingCache.get(key);
        if (hit != null) {
            return hit;
        }
        int spacing = network.spacing();
        double value = Math.min(
                envelopeAt(new Line(Line.Axis.Z, zIndex), xIndex * spacing),
                envelopeAt(new Line(Line.Axis.X, xIndex), zIndex * spacing));
        crossingCache.put(key, value);
        return value;
    }

    private double cachedSmoothed(Line line, int node) {
        Key key = new Key(smoothKey(line), node);
        Double hit = smoothCache.get(key);
        if (hit != null) {
            return hit;
        }
        double value = smoothedAt(line, node);
        smoothCache.put(key, value);
        return value;
    }

    private static Line smoothKey(Line line) {
        return new Line(line.axis(), line.index(), line.offset(), 0);
    }

    private double smoothedAt(Line line, int node) {
        double sum = 0;
        int count = 0;
        for (int d = -SMOOTH_RADIUS; d <= SMOOTH_RADIUS; d += STRIDE) {
            sum += sampleTerrain(line, node + d);
            count++;
        }
        return sum / count;
    }

    // Огибающая берёт ступенчатый сдвиг, а не интерполированный: интерполяция
    // требует rampCovering, тот — levelAt, а levelAt приходит сюда за рельефом.
    // Внутри разрыва выборка уходит от пути на несколько блоков вбок — сглаживанию
    // на радиусе 64 это безразлично, а цикл разрывает.
    private int sampleTerrain(Line line, int along) {
        int perpendicular = network.perpendicularAt(line, along)
                          + shiftAt(line, Math.floorDiv(along, SEGMENT));
        int x = line.axis() == Line.Axis.Z ? perpendicular : along;
        int z = line.axis() == Line.Axis.Z ? along : perpendicular;
        return sampleAt(x, z);
    }

    private int sampleAt(int x, int z) {
        long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
        Integer hit = terrainCache.get(key);
        if (hit != null) {
            return hit;
        }
        int height = terrain.heightAt(x, z);
        terrainCache.put(key, height);
        return height;
    }
}
