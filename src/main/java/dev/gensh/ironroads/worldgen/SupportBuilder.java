package dev.gensh.ironroads.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

final class SupportBuilder {
    /**
     * Выше этого подъёма над землёй полотно едет не по насыпи, а по эстакаде.
     * Настоящая дорога тянет насыпь сколько может — она дешевле моста, — поэтому
     * порог высокий, а откос под него подобран: BANK_WINGS полос ровно закрывают
     * склон 1:1 от кромки полотна до земли.
     */
    private static final int EMBANKMENT_MAX = 5;

    private static final int PILLAR_SPACING = 8;

    static final int RAILING_HEIGHT = 6;

    private static final int FOOTING_DEPTH = 2;

    /** Толщина настила: под полотном должна быть плита, а не воздух. */
    private static final int DECK = 2;

    /**
     * Откос насыпи. Без него полотно обрывается отвесной стенкой — единственная
     * деталь, по которой дорога читается как вставленная в мир. Полоса {@code wing}
     * отсчитывается от кромки полотна наружу, вершина откоса опускается на блок за
     * полосу: склон 1:1, как у любой отсыпанной насыпи.
     */
    static void skirt(WorldGenLevel level, int x, int y, int z, int ground, int wing) {
        int rise = y - ground;
        if (rise <= wing || rise > EMBANKMENT_MAX) {
            return;
        }
        fill(level, x, y - 1 - wing, z, ground + 1, Palette.Kind.VERGE);
    }

    static void pave(WorldGenLevel level, int x, int y, int z, int ground) {
        if (y - ground > 0) {
            fill(level, x, y - 2, z, ground - 1, Palette.Kind.PILLAR);
        }
    }

    /**
     * Всё, что держит полотно снизу: насыпь, настил эстакады, опоры и арочные
     * перемычки между ними.
     *
     * @param edge колонка на кромке полотна — только она несёт опоры и перила
     */
    static void support(WorldGenLevel level, int x, int y, int z, int ground,
                        boolean edge, int along, boolean allowRailing) {
        int rise = y - ground;
        if (rise <= 0) {
            return;
        }

        if (rise <= EMBANKMENT_MAX) {
            fill(level, x, y - 2, z, ground - 1, Palette.Kind.BALLAST);
            return;
        }

        // Плита настила на всю ширину. Без неё эстакада — лента в один блок,
        // висящая в воздухе: снизу видно небо между шпалами.
        fill(level, x, y - DECK, z, y - DECK, Palette.Kind.PILLAR);

        if (!edge) {
            return;
        }

        int step = Math.floorMod(along, PILLAR_SPACING);
        int distance = Math.min(step, PILLAR_SPACING - step);

        if (distance == 0) {
            fill(level, x, y - DECK - 1, z, ground - FOOTING_DEPTH, Palette.Kind.PILLAR);
        } else {
            // Пазуха арки: у опоры кладка спускается ниже всего, к середине пролёта
            // сходит на нет. Отсюда и берётся арка — виадук без неё выглядит
            // бетонной эстакадой из другого века.
            fill(level, x, y - DECK - 1, z,
                 y - DECK - Math.max(0, PILLAR_SPACING / 2 - distance),
                 Palette.Kind.PILLAR);
        }

        if (allowRailing && rise >= RAILING_HEIGHT) {
            railing(level, x, y, z);
        }
    }

    private static void railing(WorldGenLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Palette.pick(Palette.Kind.RAILING, x, y, z), 2);
        }
    }

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
