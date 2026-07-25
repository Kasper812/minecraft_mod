package dev.gensh.ironroads.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Всё, что держит путь над землёй: насыпь на невысоких участках, эстакада на
 * опорах над впадинами и водой, перила на высоких пролётах.
 *
 * Кривые Безье на уклонах несут собственные балки, поэтому здесь речь только о
 * прямых участках.
 */
final class SupportBuilder {

    /** Выше этого путь уже не насыпают, а ставят на опоры. */
    private static final int EMBANKMENT_MAX = 3;

    /** Шаг опор вдоль пути. */
    private static final int PILLAR_SPACING = 8;

    /** С этой высоты пролёт получает перила. */
    private static final int RAILING_HEIGHT = 6;

    /** Насколько глубоко опора уходит в грунт или в дно водоёма. */
    private static final int FOOTING_DEPTH = 2;

    /**
     * Обрабатывает одну колонку полотна.
     *
     * @param y      высота полотна (блок под рельсом лежит на y-1)
     * @param ground высота поверхности в этой точке
     */
    static void support(WorldGenLevel level, int x, int y, int z, int ground,
                        int offset, int along, int halfWidth) {

        int rise = y - ground;
        if (rise <= 0) {
            return;  // выемка или тоннель — это не наша забота
        }

        if (rise <= EMBANKMENT_MAX) {
            fill(level, x, y - 2, z, ground - 1, Palette.Kind.BALLAST);
            return;
        }

        // Эстакада. Опоры только под краями полотна и только через шаг: сплошная
        // стена под мостом выглядела бы дамбой.
        boolean edge = Math.abs(offset) == halfWidth;
        if (edge && Math.floorMod(along, PILLAR_SPACING) == 0) {
            fill(level, x, y - 2, z, ground - FOOTING_DEPTH, Palette.Kind.PILLAR);
        }

        if (edge && rise >= RAILING_HEIGHT) {
            railing(level, x, y, z);
        }
    }

    /** Перила по краю пролёта — чтобы мост читался как мост. */
    private static void railing(WorldGenLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Palette.pick(Palette.Kind.RAILING, x, y, z), 2);
        }
    }

    /**
     * Заполняет колонку сверху вниз, не трогая уже стоящие блоки, кроме воды:
     * иначе опора в реке оставила бы под собой водяной карман.
     */
    private static void fill(WorldGenLevel level, int x, int fromY, int z, int toY,
                             Palette.Kind kind) {
        for (int y = fromY; y >= toY; y--) {
            if (y < level.getMinBuildHeight()) {
                return;
            }
            BlockPos pos = new BlockPos(x, y, z);
            BlockState existing = level.getBlockState(pos);
            if (existing.isAir() || !level.getFluidState(pos).isEmpty()) {
                level.setBlock(pos, Palette.pick(kind, x, y, z), 2);
            }
        }
    }

    private SupportBuilder() {}
}
