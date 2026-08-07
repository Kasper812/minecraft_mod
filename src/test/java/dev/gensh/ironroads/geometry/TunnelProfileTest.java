package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TunnelProfileTest {
    /** Полуширина полотна магистрали и ветки — не выработки. */
    private static final int MAIN_BED = 4;
    private static final int BRANCH_BED = 2;

    private static final int MAIN = TunnelProfile.bore(MAIN_BED);
    private static final int BRANCH = TunnelProfile.bore(BRANCH_BED);
    private static final int TRACK_OFFSET = 2;
    private static final int GAUGE_HEIGHT = 4;

    /**
     * Половина ширины крупного сборного состава. Стандартный вагон Create шире
     * одного блока, у сборок с других модов вагон доходит до пяти — ради них
     * выработка и разошлась с полотном.
     */
    private static final int WIDE_TRAIN = 2;

    private static final int WIDE_TRAIN_HEIGHT = 5;

    // Ради чего вся правка: в нору должен влезть широкий состав на обоих путях, не
    // задев ни свода, ни стены. Проверяем оба пути целиком, а не одну ось.
    @Test
    void theBoreSwallowsAWideTrainOnBothTracks() {
        for (int track : new int[] { -TRACK_OFFSET, +TRACK_OFFSET }) {
            for (int offset = track - WIDE_TRAIN; offset <= track + WIDE_TRAIN; offset++) {
                assertTrue(Math.abs(offset) < TunnelProfile.wallOffset(MAIN),
                           "стена режет широкий состав в offset=" + offset);
                assertTrue(TunnelProfile.crownAt(offset, MAIN) >= WIDE_TRAIN_HEIGHT,
                           "свод давит на широкий состав в offset=" + offset
                           + ": высота " + TunnelProfile.crownAt(offset, MAIN));
            }
        }
    }

    // Проверка «не выродилось»: выработка обязана быть шире полотна, иначе весь
    // запас, ради которого заведён BORE_MARGIN, куда-то делся.
    @Test
    void theBoreIsWiderThanTheTrackbed() {
        assertTrue(TunnelProfile.bore(MAIN_BED) > MAIN_BED,
                   "выработка не шире полотна");
        assertTrue(TunnelProfile.bore(BRANCH_BED) > BRANCH_BED,
                   "выработка ветки не шире полотна");
    }

    @Test
    void theArchIsSymmetricAndNeverRisesOutward() {
        for (int halfWidth : new int[] { BRANCH, 3, MAIN, 6 }) {
            int previous = Integer.MAX_VALUE;
            for (int offset = 0; offset <= halfWidth; offset++) {
                int crown = TunnelProfile.crownAt(offset, halfWidth);
                assertEquals(crown, TunnelProfile.crownAt(-offset, halfWidth),
                             "свод несимметричен при halfWidth=" + halfWidth);
                assertTrue(crown <= previous,
                           "свод растёт наружу при halfWidth=" + halfWidth
                           + ", offset=" + offset);
                previous = crown;
            }
        }
    }

    @Test
    void everythingBeyondTheBoreIsWall() {
        assertEquals(-1, TunnelProfile.crownAt(MAIN + 1, MAIN));
        assertEquals(MAIN + 1, TunnelProfile.wallOffset(MAIN));
        assertTrue(TunnelProfile.crownAt(MAIN, MAIN) >= 0);
    }

    // Главное требование к сечению: состав не задевает обделку. Габарит — блок в
    // каждую сторону от пути и GAUGE_HEIGHT вверх, пути лежат в ±TRACK_OFFSET.
    @Test
    void theBoreClearsTheGaugeOfBothTracks() {
        for (int offset = -TRACK_OFFSET - 1; offset <= TRACK_OFFSET + 1; offset++) {
            assertTrue(TunnelProfile.crownAt(offset, MAIN) >= GAUGE_HEIGHT,
                       "свод давит на габарит в offset=" + offset);
        }
        for (int offset = -1; offset <= 1; offset++) {
            assertTrue(TunnelProfile.crownAt(offset, BRANCH) >= GAUGE_HEIGHT,
                       "свод ветки давит на габарит в offset=" + offset);
        }
    }

    @Test
    void theWallStandsClearOfTheTracks() {
        assertTrue(TunnelProfile.wallOffset(MAIN) - TRACK_OFFSET >= 3,
                   "стена ближе трёх блоков к оси пути: на боковом сдвиге заденет");
        assertTrue(TunnelProfile.wallOffset(BRANCH) >= 3,
                   "стена ветки ближе трёх блоков к пути");
    }

    @Test
    void theShellCoversTheWholeArch() {
        for (int halfWidth : new int[] { BRANCH, MAIN }) {
            int top = TunnelProfile.shellTop(halfWidth);
            for (int offset = -halfWidth; offset <= halfWidth; offset++) {
                assertTrue(top > TunnelProfile.crownAt(offset, halfWidth),
                           "обделка ниже свода при halfWidth=" + halfWidth);
            }
        }
    }

    // Проверка «не выродилось»: подкова обязана быть подковой, а не коробкой.
    @Test
    void theArchActuallyCurves() {
        assertTrue(TunnelProfile.crownAt(0, MAIN) - TunnelProfile.crownAt(MAIN, MAIN) >= 2,
                   "свод плоский: сечение выродилось в прямоугольник");
    }

    @Test
    void shortHolesGetClosedAndShortRunsGetDropped() {
        boolean[] raw = new boolean[40];
        for (int i = 5; i < 20; i++) {
            raw[i] = true;
        }
        raw[12] = false;
        raw[13] = false;
        raw[30] = true;

        boolean[] settled = TunnelProfile.settle(raw, 2);

        assertTrue(settled[12] && settled[13], "дыра в два сечения не затянулась");
        assertFalse(settled[30], "одиночное сечение не выброшено");
        assertTrue(settled[10] && settled[17], "длинный участок повреждён склейкой");
    }

    // Соседние чанки считают одно и то же сечение по своим массивам со своими
    // краями. Если склейка зависит от того, где массив обрезали, тоннель рвётся
    // ровно по границе чанка.
    @Test
    void theResultDoesNotDependOnWhereTheWindowWasCut() {
        boolean[] wide = new boolean[64];
        for (int i = 0; i < wide.length; i++) {
            wide[i] = (i * i / 7 + i / 3) % 5 < 2;
        }
        boolean[] settledWide = TunnelProfile.settle(wide, 2);

        int shift = 11;
        boolean[] narrow = new boolean[42];
        System.arraycopy(wide, shift, narrow, 0, narrow.length);
        boolean[] settledNarrow = TunnelProfile.settle(narrow, 2);

        int margin = 10;
        int agreed = 0;
        for (int i = margin; i < narrow.length - margin; i++) {
            assertEquals(settledWide[shift + i], settledNarrow[i],
                         "склейка зависит от края окна на сечении " + i);
            agreed++;
        }
        assertTrue(agreed > 20, "сравнили слишком мало сечений: " + agreed);
    }

    @Test
    void settlingIsNotTheIdentity() {
        boolean[] raw = new boolean[40];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (i / 2) % 3 == 0;
        }
        boolean[] settled = TunnelProfile.settle(raw, 2);

        int changes = 0;
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] != settled[i]) {
                changes++;
            }
        }
        assertTrue(changes > 5, "склейка ничего не сделала: " + changes + " изменений");
    }
}
