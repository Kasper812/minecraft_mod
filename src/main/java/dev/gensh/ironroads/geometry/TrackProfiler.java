package dev.gensh.ironroads.geometry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Продольный профиль высоты пути.
 *
 * Берётся нижняя огибающая Липшица по узлам фиксированной сетки:
 *
 *     h(t) = min по узлам n в окне от ( smooth(n) + |n - t| / G )
 *
 * Это наибольшая функция, не превышающая сглаженный рельеф и нигде не
 * нарушающая уклон 1/G.
 *
 * Узлы n кратны STRIDE в МИРОВЫХ координатах, а не отсчитываются от t.
 * Это принципиально. При сдвиге t множество узлов остаётся тем же, и из
 * неравенства треугольника |n - t1| <= |n - t2| + |t1 - t2| немедленно
 * следует |h(t1) - h(t2)| <= |t1 - t2| / G. Если бы окно ездило вместе с t,
 * набор сэмплов менялся бы на каждом шаге и профиль давал бы рывки.
 *
 * Из той же фиксированности следует позиционная локальность: значение в
 * точке не зависит от того, откуда мы начали считать, поэтому профиль
 * сходится на границах чанков без ступеней.
 *
 * Источник рельефа — поле, а не параметр метода: кэш узлов привязан к
 * экземпляру, и профайлер, обслуживающий два разных рельефа, раздавал бы
 * чужие значения.
 */
public final class TrackProfiler {

    /** Полуширина окна сглаживания. */
    private static final int SMOOTH_RADIUS = 48;

    /**
     * Полуширина окна огибающей. Обязана покрывать максимальный перепад,
     * умноженный на maxGradeRun: при уклоне 1:12 обрыв в 60 блоков
     * размазывается на 720. Меньшее окно упирается в свой край и даёт
     * скачок высоты там, где обрыв выходит из зоны видимости.
     */
    private static final int ENVELOPE_RADIUS = 768;

    /**
     * Шаг сетки узлов.
     * ponytail: шаг 8 при радиусе 768 даёт 192 узла на точку вместо 1536.
     * Разрежение не вредит плавности, потому что сетка абсолютная; оно лишь
     * слегка приподнимает профиль. Уменьшать, если профиль окажется грубым.
     */
    private static final int STRIDE = 8;

    private final RailNetwork network;
    private final int maxGradeRun;
    private final HeightSampler terrain;

    /**
     * Кэш сглаженной высоты по узлам сетки. Соседние точки пути делят почти
     * все свои узлы, поэтому без кэша каждая точка стоила бы заново 193*13
     * обращений к рельефу — а рельеф считает шумовую функцию колонкой и стоит
     * дорого.
     *
     * Экземпляр обязан жить на весь мир, а не на чанк: узлы общие для всей
     * линии, и пересоздание кэша на каждый чанк делает генерацию неиграбельно
     * медленной. Отсюда и ConcurrentHashMap — worldgen многопоточный. На
     * детерминированность кэш не влияет: значения чистые, гонка безобидна.
     *
     * ponytail: размер не ограничен. На линию длиной 30k блоков это ~4k
     * записей, при разумном исследовании мира счёт не страшен. Если память
     * станет проблемой — вводить LRU, а не урезать радиус огибающей.
     */
    private final Map<Long, Double> smoothCache = new ConcurrentHashMap<>();

    /** Кэш высот развязок. Ключ — пара индексов линий (Z, X). */
    private final Map<Long, Double> crossingCache = new ConcurrentHashMap<>();

    public TrackProfiler(RailNetwork network, int maxGradeRun, HeightSampler terrain) {
        if (maxGradeRun < 1) {
            throw new IllegalArgumentException("maxGradeRun должен быть положительным");
        }
        this.network = network;
        this.maxGradeRun = maxGradeRun;
        this.terrain = terrain;
    }

    public RailNetwork network() {
        return network;
    }

    /** Высота полотна пути в точке along вдоль линии. */
    public int heightAt(Line line, int along) {
        double best = envelopeAt(line, along);

        // Развязки. Каждый узел решётки добавляет в тот же минимум ещё одно
        // липшицево слагаемое. Ровно в узле оно и побеждает, а поскольку обе
        // пересекающиеся линии считают его из одной и той же пары огибающих,
        // они приходят в развязку на одну высоту — иначе четыре ветви сходились
        // бы на разных уровнях и путь на перекрёстке рвался бы.
        // Слагаемое вида c + |n - t|/G само липшицево с константой 1/G, поэтому
        // ограничение на уклон от его добавления не страдает.
        int spacing = network.spacing();
        int firstIndex = Math.floorDiv(along - ENVELOPE_RADIUS, spacing);
        int lastIndex = Math.floorDiv(along + ENVELOPE_RADIUS, spacing);
        for (int index = firstIndex; index <= lastIndex; index++) {
            double candidate = crossingHeight(line, index)
                             + (double) Math.abs(index * spacing - along) / maxGradeRun;
            if (candidate < best) {
                best = candidate;
            }
        }
        return (int) Math.round(best);
    }

    /** Нижняя огибающая Липшица по узлам абсолютной сетки STRIDE. */
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

    /**
     * Высота развязки в узле решётки, где линия index-й перпендикулярной
     * магистрали скрещивается с данной. Симметрична по построению: обе линии
     * берут минимум из одной и той же пары огибающих, так что от того, какая
     * сторона спрашивает, результат не зависит.
     */
    private double crossingHeight(Line line, int crossingIndex) {
        boolean alongZ = line.axis() == Line.Axis.Z;
        int zIndex = alongZ ? line.index() : crossingIndex;
        int xIndex = alongZ ? crossingIndex : line.index();

        long key = ((long) zIndex << 32) ^ (xIndex & 0xFFFFFFFFL);
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
        long key = ((long) line.index() << 34)
                 ^ ((long) (line.axis() == Line.Axis.Z ? 1 : 0) << 33)
                 ^ (node & 0xFFFFFFFFL);
        Double hit = smoothCache.get(key);
        if (hit != null) {
            return hit;
        }
        double value = smoothedAt(line, node);
        smoothCache.put(key, value);
        return value;
    }

    /** Скользящее среднее рельефа вокруг узла сетки. */
    private double smoothedAt(Line line, int node) {
        double sum = 0;
        int count = 0;
        for (int d = -SMOOTH_RADIUS; d <= SMOOTH_RADIUS; d += STRIDE) {
            sum += sampleTerrain(line, node + d);
            count++;
        }
        return sum / count;
    }

    /** Рельеф в точке линии — с учётом того, что линия виляет. */
    private int sampleTerrain(Line line, int along) {
        int perpendicular = network.perpendicularAt(line, along);
        return line.axis() == Line.Axis.Z
             ? terrain.heightAt(perpendicular, along)
             : terrain.heightAt(along, perpendicular);
    }
}
