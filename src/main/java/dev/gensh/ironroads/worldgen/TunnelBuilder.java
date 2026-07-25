package dev.gensh.ironroads.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Закрытый тоннель там, где рельеф поднимается над путём слишком высоко для
 * открытой выемки.
 *
 * Сечение 7 блоков в ширину и 6 в высоту: по краям полотна стены из обделки,
 * между ними проход. Открытая выемка такой глубины превратила бы гору в
 * каньон, а узкий лаз выглядел бы норой.
 */
final class TunnelBuilder {

    /** Глубина, с которой выемка перестаёт иметь смысл. */
    static final int MIN_DEPTH = 8;

    /** Высота прохода от полотна до свода. */
    private static final int CLEARANCE = 6;

    /** Шаг фонарей вдоль тоннеля. */
    private static final int LIGHT_SPACING = 8;

    /** Высота, на которой фонарь висит в нише. */
    private static final int LIGHT_HEIGHT = 3;

    /**
     * Обрабатывает одну колонку полотна.
     *
     * @param offset смещение колонки от осевой линии
     * @param along  позиция вдоль линии — по ней расставляются фонари
     */
    static void carve(WorldGenLevel level, int x, int y, int z, int offset, int along,
                      int halfWidth) {

        boolean wall = Math.abs(offset) == halfWidth;

        for (int dy = 0; dy < CLEARANCE; dy++) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            if (wall) {
                level.setBlock(pos, Palette.pick(Palette.Kind.LINING, x, y + dy, z), 2);
            } else if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // Свод. Без него над проходом остаётся голая порода, и тоннель
        // выглядит дырой, а не сооружением.
        BlockPos crown = new BlockPos(x, y + CLEARANCE, z);
        level.setBlock(crown, Palette.pick(Palette.Kind.LINING, x, y + CLEARANCE, z), 2);

        if (!wall || Math.floorMod(along, LIGHT_SPACING) != 0) {
            return;
        }

        // Фонарь в нише стены: висит на блоке обделки над собой, поэтому
        // в габарит не лезет и не гаснет от обновления блока.
        BlockState lantern = Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true);
        level.setBlock(new BlockPos(x, y + LIGHT_HEIGHT, z), lantern, 2);
    }

    private TunnelBuilder() {}
}
