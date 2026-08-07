package dev.gensh.ironroads.geometry;

/**
 * Полоса отвода — сколько блоков за кромкой полотна дорога держит за собой.
 *
 * Внутри полосы фича выкладывает щебень, и на нём не приживается ни одно
 * ванильное растение: и саженцы, и трава, и цветы проверяют блок под собой на
 * {@link "BlockTags.DIRT"}. Лес обходит дорогу сам, вырубать нечего. Ширина полосы
 * решает, насколько далеко от пути стоит ближайший ствол, а значит — свесится ли
 * его крона в габарит.
 *
 * Ширина не постоянная, и это главное здесь. Полоса постоянной ширины даёт по
 * линейке отрезанную стену леса на весь перегон — самую заметную примету
 * сгенерированного, а не выросшего. Ширина гуляет между {@link #MIN_WINGS} и
 * пределом двумя волнами разной длины: длинная задаёт, где просека расходится и
 * где сужается, короткая ломает её край. Обе взяты хешем от координаты, а не
 * {@code Random}: соседние чанки обязаны получить одну и ту же кромку, иначе она
 * порвётся ровно по границе чанка.
 *
 * Стороны считаются отдельно: у настоящей дороги отвод несимметричен — с одной
 * стороны кювет и опушка вплотную, с другой площадка под будущий второй путь.
 */
public final class RightOfWay {
    /** Уже этого полосу не сужаем: кромке полотна нужна хотя бы одна колонка. */
    private static final int MIN_WINGS = 1;

    /** Длинная волна: на таком масштабе просека расходится и сходится. */
    private static final int COARSE = 53;

    /** Короткая волна: ею ломается край, чтобы он не читался как дуга. */
    private static final int FINE = 13;

    private static final double FINE_SHARE = 0.3;

    private RightOfWay() {}

    /**
     * Ширина полосы отвода в колонках за кромкой полотна.
     *
     * @param side   −1 или +1 — стороны считаются независимо
     * @param maxWings предел, дальше которого полосу расширять нельзя: он
     *                 совпадает с самой дальней колонкой, которую фича и так
     *                 трогает, поэтому регион фичи от полосы не растёт
     */
    public static int wings(Line line, int along, int side, int maxWings) {
        if (maxWings <= MIN_WINGS) {
            return Math.max(0, maxWings);
        }
        double value = (1 - FINE_SHARE) * wave(line, side, along, COARSE, 0x1D)
                     + FINE_SHARE * wave(line, side, along, FINE, 0x2B);

        int span = maxWings - MIN_WINGS + 1;
        return MIN_WINGS + Math.min(span - 1, (int) (value * span));
    }

    /**
     * Значение волны в точке: узлы стоят через {@code period}, между ними —
     * сглаживание той же кубикой, что и у пандуса. Без сглаживания полоса
     * прыгала бы на узлах ступенькой во всю амплитуду, и просека получалась бы
     * не извилистой, а зубчатой.
     */
    private static double wave(Line line, int side, int along, int period, int salt) {
        int cell = Math.floorDiv(along, period);
        double from = unit(line, side, cell, salt);
        double to = unit(line, side, cell + 1, salt);

        double t = (double) Math.floorMod(along, period) / period;
        return from + (to - from) * t * t * (3 - 2 * t);
    }

    private static double unit(Line line, int side, int cell, int salt) {
        long hash = mix(line.axis() == Line.Axis.Z ? 1 : 2,
                        line.index(),
                        cell * 2L + (side > 0 ? 1 : 0),
                        salt);
        return (hash >>> 11) / (double) (1L << 53);
    }

    private static long mix(long axis, long index, long cell, long salt) {
        long h = axis * 0x9E3779B97F4A7C15L
               ^ index * 0xBF58476D1CE4E5B9L
               ^ cell * 0x94D049BB133111EBL
               ^ salt * 0x165667B19E3779F9L;
        h += 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }
}
