package dev.gensh.ironroads.geometry;

/**
 * Поперечник тоннеля и склейка сечений в один тоннель.
 *
 * Чистая геометрия, ноль зависимостей от Minecraft: свод — половина эллипса на
 * вертикальных стенах, ровно как у настоящего однопролётного тоннеля. Прямоугольная
 * нора проверена и отклонена: у неё стена стоит вплотную к габариту на всей высоте,
 * и на боковом сдвиге (кривая Безье уводит путь вбок внутри разрыва) состав задевает
 * угол. Свод убирает стену оттуда, где путь качается, и оставляет её там, где не
 * качается никогда — у пола.
 */
public final class TunnelProfile {
    private static final int MIN_RISE = 2;

    /**
     * На сколько выработка шире полотна в каждую сторону.
     *
     * Тоннель нарочно просторнее насыпи: на открытом воздухе состав шире габарита
     * просто свесится над откосом, а в тоннеле — упрётся в кладку. Один блок запаса
     * с каждой стороны превращает магистральную нору из девяти блоков в одиннадцать
     * и поднимает свод на два, чего хватает крупным сборным составам, у которых
     * вагон шире стандартного создателева.
     */
    private static final int BORE_MARGIN = 1;

    /** Радиус выработки по полуширине полотна. */
    public static int bore(int bedHalfWidth) {
        return bedHalfWidth + BORE_MARGIN;
    }

    /** Колонка обделки: на блок дальше самой широкой части свода. */
    public static int wallOffset(int halfWidth) {
        return halfWidth + 1;
    }

    /**
     * Высота свода над головкой рельса для колонки в {@code offset} блоках от оси,
     * либо −1, если колонка не относится к своду (стена, портал, снаружи).
     */
    public static int crownAt(int offset, int halfWidth) {
        int distance = Math.abs(offset);
        if (distance > halfWidth) {
            return -1;
        }
        int rise = Math.max(MIN_RISE, halfWidth - 1);
        // Полуось эллипса на полблока шире крайней колонки: при полуоси ровно
        // halfWidth свод садится на неё вертикально, подкова вырождается в
        // прямоугольник со скошенными углами и вся затея теряет смысл.
        double semi = halfWidth + 0.5;
        double t = distance / semi;
        return rise + (int) Math.round(rise * Math.sqrt(1.0 - t * t));
    }

    /** Верх обделки: до него заполняется всё, что выше свода. */
    public static int shellTop(int halfWidth) {
        return crownAt(0, halfWidth) + 1;
    }

    /**
     * Замыкание, затем размыкание по серии сечений: сначала затягиваются дыры
     * короче {@code radius}, потом выбрасываются участки короче того же.
     *
     * Без этого тоннель рвётся на куски по паре блоков на каждом бугре рельефа, и
     * на каждом куске встаёт по два портала — вместо тоннеля получается частокол
     * арок. Края массива дублируются наружу, поэтому результат для любого индекса
     * дальше {@code 5 * radius} от края не зависит от того, где массив обрезали:
     * соседние чанки считают одно и то же сечение одинаково.
     */
    public static boolean[] settle(boolean[] sections, int radius) {
        boolean[] closed = morph(morph(sections, radius, true), radius, false);
        return morph(morph(closed, radius, false), radius, true);
    }

    private static boolean[] morph(boolean[] source, int radius, boolean dilate) {
        boolean[] out = new boolean[source.length];
        for (int i = 0; i < source.length; i++) {
            boolean value = !dilate;
            for (int d = -radius; d <= radius; d++) {
                int at = Math.max(0, Math.min(source.length - 1, i + d));
                value = dilate ? value | source[at] : value & source[at];
            }
            out[i] = value;
        }
        return out;
    }

    private TunnelProfile() {}
}
