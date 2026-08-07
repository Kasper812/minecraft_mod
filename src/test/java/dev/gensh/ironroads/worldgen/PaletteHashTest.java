package dev.gensh.ironroads.worldgen;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaletteHashTest {
    private static long hash(int x, int y, int z, int salt) {
        long h = x * 0x9E3779B97F4A7C15L
               ^ y * 0xBF58476D1CE4E5B9L
               ^ z * 0x94D049BB133111EBL
               ^ salt * 0x165667B19E3779F9L;
        h += 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    private static int index(int x, int y, int z, int salt, int choices) {
        return (int) Math.floorMod(hash(x, y, z, salt), choices);
    }

    @Test
    void sameBlockAlwaysGetsSameMaterial() {
        for (int i = 0; i < 500; i++) {
            assertEquals(index(i, 64, -i, 0, 4), index(i, 64, -i, 0, 4));
        }
    }

    @Test
    void neighbouringBlocksDoNotFormAPattern() {
        int repeats = 0;
        int previous = index(0, 64, 0, 0, 4);
        for (int z = 1; z < 400; z++) {
            int current = index(0, 64, z, 0, 4);
            if (current == previous) {
                repeats++;
            }
            previous = current;
        }
        assertTrue(repeats > 50 && repeats < 200,
                   "разброс выродился: " + repeats + " повторов подряд из 399");
    }

    @Test
    void allVariantsGetUsed() {
        Set<Integer> seen = new HashSet<>();
        for (int x = 0; x < 40; x++) {
            for (int z = 0; z < 40; z++) {
                seen.add(index(x, 64, z, 0, 4));
            }
        }
        assertEquals(4, seen.size(), "часть материалов палитры недостижима");
    }

    @Test
    void differentKindsDifferOnTheSameBlock() {
        int differences = 0;
        for (int z = 0; z < 200; z++) {
            if (index(0, 64, z, 0, 4) != index(0, 64, z, 1, 4)) {
                differences++;
            }
        }
        assertTrue(differences > 100, "соль по типу не расцепляет выбор");
    }
}
