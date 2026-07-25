package dev.gensh.ironroads.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Материалы дороги: старая каменная кладка, местами замшелая и потрескавшаяся.
 *
 * Выбор блока детерминирован — хеш от координат, а не Random. Иначе одна и та
 * же точка получала бы разный блок в зависимости от того, в каком порядке
 * генерировались чанки, и на их границах пятна мха обрывались бы швом.
 *
 * Веса заданы повторами в массиве: это короче любого механизма весов и видно
 * глазом.
 */
public final class Palette {

    public enum Kind { BALLAST, SHOULDER, LINING, PILLAR, RAILING }

    /** Под путями: щебёночная насыпь. */
    private static final BlockState[] BALLAST = {
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
    };

    /** Обочины: кладка поаккуратнее, чтобы полотно читалось как сооружение. */
    private static final BlockState[] SHOULDER = {
        Blocks.STONE_BRICKS.defaultBlockState(),
        Blocks.STONE_BRICKS.defaultBlockState(),
        Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
        Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
    };

    /** Обделка тоннелей и настил мостов. */
    private static final BlockState[] LINING = {
        Blocks.STONE_BRICKS.defaultBlockState(),
        Blocks.STONE_BRICKS.defaultBlockState(),
        Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
        Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
    };

    /** Опоры эстакад. */
    private static final BlockState[] PILLAR = {
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
        Blocks.STONE_BRICKS.defaultBlockState(),
    };

    /** Перила высоких пролётов. */
    private static final BlockState[] RAILING = {
        Blocks.COBBLESTONE_WALL.defaultBlockState(),
        Blocks.COBBLESTONE_WALL.defaultBlockState(),
        Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(),
    };

    public static BlockState pick(Kind kind, int x, int y, int z) {
        BlockState[] choices = choicesFor(kind);
        int index = (int) Math.floorMod(hash(x, y, z, kind.ordinal()), choices.length);
        return choices[index];
    }

    private static BlockState[] choicesFor(Kind kind) {
        return switch (kind) {
            case BALLAST -> BALLAST;
            case SHOULDER -> SHOULDER;
            case LINING -> LINING;
            case PILLAR -> PILLAR;
            case RAILING -> RAILING;
        };
    }

    /** splitmix64 по координатам: тот же приём, что в RailNetwork. */
    private static long hash(int x, int y, int z, int salt) {
        long z0 = x * 0x9E3779B97F4A7C15L
                ^ y * 0xBF58476D1CE4E5B9L
                ^ z * 0x94D049BB133111EBL
                ^ salt * 0x165667B19E3779F9L;
        z0 += 0x9E3779B97F4A7C15L;
        z0 = (z0 ^ (z0 >>> 30)) * 0xBF58476D1CE4E5B9L;
        z0 = (z0 ^ (z0 >>> 27)) * 0x94D049BB133111EBL;
        return z0 ^ (z0 >>> 31);
    }

    private Palette() {}
}
