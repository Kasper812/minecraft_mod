package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RightOfWayTest {
    private static final Line LINE = new Line(Line.Axis.Z, 0);
    private static final int MAX = 4;

    @Test
    void theEdgeStaysInsideItsBudget() {
        for (int along = -3000; along <= 3000; along++) {
            for (int side = -1; side <= 1; side += 2) {
                int wings = RightOfWay.wings(LINE, along, side, MAX);
                assertTrue(wings >= 1 && wings <= MAX,
                           "полоса вышла за пределы на along=" + along
                           + ", сторона " + side + ": " + wings);
            }
        }
    }

    // Ради чего всё и заведено: полоса постоянной ширины даёт отрезанную по
    // линейке стену леса. Проверяем, что кромка гуляет по всей амплитуде, а не
    // застряла на одном значении.
    @Test
    void theEdgeIsNotStraight() {
        Set<Integer> seen = new HashSet<>();
        int changes = 0;
        int previous = RightOfWay.wings(LINE, 0, +1, MAX);

        for (int along = 1; along <= 3000; along++) {
            int wings = RightOfWay.wings(LINE, along, +1, MAX);
            seen.add(wings);
            if (wings != previous) {
                changes++;
            }
            previous = wings;
        }
        assertEquals(MAX, seen.size(), "кромка использует не все ширины: " + seen);
        assertTrue(changes > 60,
                   "кромка почти прямая: " + changes + " изменений на 3000 блоков");
    }

    // И не бахрома: если ширина скачет на каждом блоке, просека выходит пилой, а
    // не извилистой. Между изменениями должны быть заметные ровные участки.
    @Test
    void theEdgeIsNotSawtooth() {
        int changes = 0;
        int previous = RightOfWay.wings(LINE, 0, +1, MAX);

        for (int along = 1; along <= 3000; along++) {
            int wings = RightOfWay.wings(LINE, along, +1, MAX);
            if (wings != previous) {
                changes++;
            }
            previous = wings;
        }
        assertTrue(changes < 600,
                   "кромка меняется слишком часто (" + changes
                   + " раз на 3000 блоков): это пила, а не опушка");
    }

    /**
     * Соседние чанки считают одну и ту же колонку по отдельности. Если ширина
     * полосы зависит от чего-то, кроме координаты, просека порвётся ровно по
     * границе чанка — тот же класс дефекта, что и разрывы тоннеля.
     */
    @Test
    void theSamePlaceAlwaysGetsTheSameEdge() {
        for (int along = -500; along <= 500; along += 7) {
            for (int side = -1; side <= 1; side += 2) {
                assertEquals(RightOfWay.wings(LINE, along, side, MAX),
                             RightOfWay.wings(new Line(Line.Axis.Z, 0), along, side, MAX),
                             "ширина полосы зависит от контекста вызова");
            }
        }
    }

    @Test
    void theTwoSidesAreIndependent() {
        int differences = 0;
        for (int along = -1000; along <= 1000; along++) {
            if (RightOfWay.wings(LINE, along, -1, MAX)
             != RightOfWay.wings(LINE, along, +1, MAX)) {
                differences++;
            }
        }
        assertTrue(differences > 500,
                   "стороны идут одинаково (" + differences
                   + " расхождений на 2000): просека вышла симметричной трубой");
    }

    @Test
    void differentLinesGetDifferentEdges() {
        int differences = 0;
        for (int along = -1000; along <= 1000; along++) {
            if (RightOfWay.wings(new Line(Line.Axis.Z, 0), along, +1, MAX)
             != RightOfWay.wings(new Line(Line.Axis.Z, 1), along, +1, MAX)) {
                differences++;
            }
        }
        assertTrue(differences > 500,
                   "все линии получили одну кромку: " + differences);
    }

    // Вырожденный предел не должен кидать и не должен уводить полосу в минус.
    @Test
    void aDegenerateBudgetIsHandled() {
        assertEquals(0, RightOfWay.wings(LINE, 17, +1, 0));
        assertEquals(1, RightOfWay.wings(LINE, 17, +1, 1));
    }
}
