package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.TunnelProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

final class TunnelBuilder {
    /**
     * Порода над обделкой, ниже которой тоннель не роем. Меньше двух блоков — и
     * свод вылезает наружу каменным горбом посреди склона.
     */
    static final int MIN_COVER = 2;

    /** Крылья портала за стеной обделки: щёковые стенки, врезающиеся в откос. */
    private static final int PORTAL_WING = 2;

    /** Карниз над аркой у самого устья. */
    private static final int PARAPET = 1;

    private static final int LIGHT_SPACING = 8;

    private static final int SHAFT_SPACING = 64;

    private static final int SHAFT_MIN = 8;

    /** Докуда сечение достаёт вбок от оси пути. */
    static int reach(int halfWidth, boolean portal) {
        return TunnelProfile.wallOffset(TunnelProfile.bore(halfWidth))
             + (portal ? PORTAL_WING : 0);
    }

    /**
     * Одна колонка поперечника. Свод вычищается, всё остальное до верха обделки
     * закладывается кладкой — сплошным слоем, а не одним кольцом: у кольца по
     * диагонали остаётся щель, и там, где тоннель идёт под тонкой кровлей, в неё
     * видно небо.
     *
     * @param halfWidth полуширина полотна; выработка шире неё на BORE_MARGIN
     * @param portal сечение устья — глухая стена с аркой вместо свода
     * @param mouth  крайнее сечение устья, только у него карниз
     */
    static void section(WorldGenLevel level, int x, int floorY, int railY, int z,
                        int offset, int along, int halfWidth,
                        boolean portal, boolean mouth) {
        int bore = TunnelProfile.bore(halfWidth);
        int crown = TunnelProfile.crownAt(offset, bore);
        int top = railY + TunnelProfile.shellTop(bore) + (mouth ? PARAPET : 0);
        Palette.Kind kind = portal ? Palette.Kind.PORTAL : Palette.Kind.LINING;

        for (int cy = floorY; cy <= top; cy++) {
            BlockPos pos = new BlockPos(x, cy, z);
            if (crown >= 0 && cy <= railY + crown) {
                if (!level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            } else {
                level.setBlock(pos, Palette.pick(kind, x, cy, z), 2);
            }
        }

        if (!portal) {
            lamp(level, x, railY, z, offset, along, bore);
        }
    }

    private static void lamp(WorldGenLevel level, int x, int railY, int z,
                             int offset, int along, int bore) {
        if (Math.abs(offset) != bore - 1
         || Math.floorMod(along, LIGHT_SPACING) != 0) {
            return;
        }
        // Через один по сторонам: парные фонари напротив друг друга читаются как
        // размеченная сетка, а не как обстановка тоннеля.
        if ((Math.floorDiv(along, LIGHT_SPACING) & 1) == (offset > 0 ? 0 : 1)) {
            return;
        }
        BlockState lantern = Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true);
        level.setBlock(new BlockPos(x, railY + TunnelProfile.crownAt(offset, bore), z),
                       lantern, 2);
    }

    static boolean wantsShaft(int along) {
        return Math.floorMod(along, SHAFT_SPACING) == 0;
    }

    /**
     * Вентиляционный ствол на поверхность. Настоящие горные тоннели дышат через
     * стволы, и столб света посреди перегона — самая заметная примета, что тоннель
     * прорыт по правилам, а не выдолблен генератором. Ствол идёт по оси, между
     * путями, поэтому габарит не трогает.
     */
    static void shaft(WorldGenLevel level, int x, int railY, int z, int halfWidth) {
        int rock = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        // Под водой ствол не роем: решётка не держит воду, и тоннель зальёт.
        if (rock != surface) {
            return;
        }
        int from = railY + TunnelProfile.shellTop(TunnelProfile.bore(halfWidth));
        if (surface - 1 - from < SHAFT_MIN) {
            return;
        }
        for (int cy = from; cy < surface - 1; cy++) {
            level.setBlock(new BlockPos(x, cy, z), Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(new BlockPos(x, surface - 1, z),
                       Blocks.IRON_BARS.defaultBlockState(), 2);
    }

    private TunnelBuilder() {}
}
