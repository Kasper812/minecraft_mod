package dev.gensh.ironroads.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class Palette {
    public enum Kind { BALLAST, SHOULDER, LINING, PILLAR, RAILING, VERGE, PORTAL }

    /**
     * Сторона участка с одним набором материалов. Настоящая дорога строилась
     * очередями, и материал меняется от участка к участку, а не от блока к блоку:
     * один перегон одет кирпичом, следующий — тёсаным камнем.
     *
     * ponytail: участок — квадрат сетки, а не постройка (тоннель, эстакада): у
     * pick() на входе только координата. Шов посреди длинного тоннеля возможен, но
     * при стороне вдвое больше шага решётки попадается редко. Переделать на
     * привязку к постройке, если шов в игре окажется заметен.
     */
    private static final int REGION = 2048;

    private static final int FAMILY_SALT = 0x5EED;

    // Кладка: обделка тоннеля, плечо полотна.
    private static final BlockState[][] MASONRY = {
        {
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
            Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
        },
        {
            Blocks.BRICKS.defaultBlockState(),
            Blocks.BRICKS.defaultBlockState(),
            Blocks.BRICKS.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
        },
        {
            Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
            Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(),
            Blocks.DEEPSLATE_TILES.defaultBlockState(),
        },
        {
            Blocks.POLISHED_ANDESITE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
        },
    };

    // Бут: опоры, арки эстакады, забутовка.
    private static final BlockState[][] RUBBLE = {
        {
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
        },
        {
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
            Blocks.BRICKS.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
        },
        {
            Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
            Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
        },
        {
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
        },
    };

    // Тёсаный камень: только портал тоннеля, у него лицо чище остального.
    private static final BlockState[][] DRESSED = {
        {
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
        },
        {
            Blocks.BRICKS.defaultBlockState(),
            Blocks.BRICKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
        },
        {
            Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
            Blocks.CHISELED_DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE_TILES.defaultBlockState(),
        },
        {
            Blocks.POLISHED_ANDESITE.defaultBlockState(),
            Blocks.POLISHED_ANDESITE.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
        },
    };

    private static final BlockState[][] WALLS = {
        {
            Blocks.COBBLESTONE_WALL.defaultBlockState(),
            Blocks.COBBLESTONE_WALL.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(),
        },
        {
            Blocks.BRICK_WALL.defaultBlockState(),
            Blocks.BRICK_WALL.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(),
        },
        {
            Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(),
            Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(),
        },
        {
            Blocks.ANDESITE_WALL.defaultBlockState(),
            Blocks.COBBLESTONE_WALL.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState(),
        },
    };

    // Балласт и кромка от участка не зависят: щебень везде щебень, а кромка
    // растаскивается ногами и погодой одинаково при любой кладке.
    private static final BlockState[] BALLAST = {
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
    };

    // Ни одного блока из BlockTags.DIRT, и это обязательное свойство, а не вкус:
    // кромкой и полосой отчуждения дорога держит лес в стороне, а ванильный саженец
    // приживается на всём, что в этом теге, — грубая земля пустила бы дубы вплотную
    // к пути. Проверять при добавлении материала.
    private static final BlockState[] VERGE = {
        Blocks.GRAVEL.defaultBlockState(),
        Blocks.GRAVEL.defaultBlockState(),
        Blocks.ANDESITE.defaultBlockState(),
        Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.STONE_BRICKS.defaultBlockState(),
        Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
    };

    public static boolean frayed(int x, int z) {
        return Math.floorMod(hash(x, 0, z, Kind.VERGE.ordinal()), 5) < 2;
    }

    public static BlockState pick(Kind kind, int x, int y, int z) {
        BlockState[] choices = choicesFor(kind, family(x, z));
        int index = (int) Math.floorMod(hash(x, y, z, kind.ordinal()), choices.length);
        return choices[index];
    }

    private static int family(int x, int z) {
        return (int) Math.floorMod(
                hash(Math.floorDiv(x, REGION), 0, Math.floorDiv(z, REGION), FAMILY_SALT),
                MASONRY.length);
    }

    private static BlockState[] choicesFor(Kind kind, int family) {
        return switch (kind) {
            case BALLAST -> BALLAST;
            case VERGE -> VERGE;
            case SHOULDER, LINING -> MASONRY[family];
            case PILLAR -> RUBBLE[family];
            case RAILING -> WALLS[family];
            case PORTAL -> DRESSED[family];
        };
    }

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
