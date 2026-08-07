package dev.gensh.ironroads.geometry;

import java.util.Random;

/**
 * Раскладка деревень — единственное, что сеть знает о чужих структурах.
 *
 * <p>Ветка целится в деревню, но расставляет деревни не мод, а датапак
 * генератора: любой мод на структуры меняет spacing, separation и salt в
 * {@code structure_set}. Поэтому раскладка приходит извне (реализация в
 * {@code worldgen.Branches} читает настоящий {@code minecraft:villages}), а
 * здесь лежит только ванильный вариант — для тестов и на случай, когда набора
 * структур в реестре нет.
 */
public interface VillageSites {
    /** «Здесь деревни нет»: регион пустой или биом её не держит. */
    long NONE = Long.MIN_VALUE;

    /** Шаг сетки деревень в чанках: по нему идёт перебор регионов. */
    int spacingChunks();

    /**
     * Центр деревни региона, упакованный как {@code blockX << 32 | blockZ},
     * или {@link #NONE}.
     *
     * <p>Отбор по биому делает реализация, а не вызывающий: трасса уходит в
     * сторону от деревень, и обход призрака — крюк в полсотни блоков вокруг
     * пустого поля.
     */
    long siteAt(long seed, int regionX, int regionZ);

    static long pack(int blockX, int blockZ) {
        return ((long) blockX << 32) | (blockZ & 0xFFFFFFFFL);
    }

    /**
     * Ванильная раскладка (spacing 34, separation 8, salt 10387312, linear).
     * Совпадает с {@code RandomSpreadStructurePlacement} до бита: там тот же
     * {@code setLargeFeatureWithSalt} поверх LCG-совместимого генератора и тот
     * же {@code nextInt} на каждую ось. Не «примерно как ванилла» — точно она.
     */
    VillageSites VANILLA = new VillageSites() {
        private static final int SPACING = 34;
        private static final int SEPARATION = 8;
        private static final int SALT = 10387312;

        @Override
        public int spacingChunks() {
            return SPACING;
        }

        @Override
        public long siteAt(long seed, int regionX, int regionZ) {
            Random random = new Random((long) regionX * 341873128712L
                                     + (long) regionZ * 132897987541L
                                     + seed + SALT);
            int spread = SPACING - SEPARATION;
            int chunkX = regionX * SPACING + random.nextInt(spread);
            int chunkZ = regionZ * SPACING + random.nextInt(spread);
            return pack(chunkX * 16 + 8, chunkZ * 16 + 8);
        }
    };

    /** Мир без деревень: для тестов, где проверяется только рельеф. */
    VillageSites EMPTY = new VillageSites() {
        @Override
        public int spacingChunks() {
            return VANILLA.spacingChunks();
        }

        @Override
        public long siteAt(long seed, int regionX, int regionZ) {
            return NONE;
        }
    };
}
