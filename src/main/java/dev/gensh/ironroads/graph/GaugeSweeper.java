package dev.gensh.ironroads.graph;

import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.worldgen.RailwayFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Проверка габарита после генерации чанка — второй, страховочный слой.
 *
 * Первый слой — полоса отвода: фича идёт до расстановки растительности и
 * выкладывает вдоль пути щебень, на котором ванильный саженец не приживается.
 * Лес обходит дорогу сам, и валить в норме нечего. Но полоса удерживает только
 * стволы: крона дерева, выросшего за её краем, ложится в воздух над путём, а
 * воздух для листвы — законное место. Ради этого случая проход и остался.
 *
 * Правило прохода: трогаем только то, что **вошло в габарит**, и трогаем
 * **целиком**. Лес за габаритом не наш, крона выше габарита не мешает — реальная
 * дорога тоже не бреет просеку под ноль. Ср. прошлую версию, которая валила по
 * бюджету и оставляла висеть всё, что в него не влезло.
 */
final class GaugeSweeper {
    private static final int CLEAR_HEIGHT = 24;

    /**
     * Проверить габарит и убрать то, что в него влезло.
     *
     * @return false, если хотя бы одно дерево не удалось свалить целиком: чанк
     *         тогда переставляется в очередь, а мир остаётся нетронутым. Половина
     *         дерева хуже целого дерева.
     */
    static boolean clear(ServerLevel level, TrackProfiler profiler,
                         List<BranchNetwork.Branch> branches, int chunkMinX, int chunkMinZ) {
        RailNetwork network = profiler.network();
        boolean complete = true;

        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8,
                                           RailwayFeature.REACH)) {
            complete &= sweep(level, profiler, line, chunkMinX, chunkMinZ,
                              RailwayFeature.BED_HALF_WIDTH,
                              Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        for (BranchNetwork.Branch branch : branches) {
            complete &= sweep(level, profiler, branch.line(), chunkMinX, chunkMinZ,
                              RailwayFeature.BRANCH_HALF_WIDTH,
                              Math.min(branch.fromAlong(), branch.toAlong()),
                              Math.max(branch.fromAlong(), branch.toAlong()));
        }
        return sweepJunction(level, profiler, chunkMinX, chunkMinZ) && complete;
    }

    private static boolean sweep(ServerLevel level, TrackProfiler profiler, Line line,
                                 int chunkMinX, int chunkMinZ, int halfWidth,
                                 int alongMin, int alongMax) {
        boolean alongZ = line.axis() == Line.Axis.Z;
        int alongStart = alongZ ? chunkMinZ : chunkMinX;
        int corridor = halfWidth + 2;
        boolean complete = true;

        for (int step = 0; step < 16; step++) {
            int along = alongStart + step;
            if (along < alongMin || along > alongMax) {
                continue;
            }
            int y = profiler.heightAt(line, along);
            int perpendicular = profiler.perpendicularAt(line, along);

            for (int offset = -corridor; offset <= corridor; offset++) {
                int x = alongZ ? perpendicular + offset : along;
                int z = alongZ ? along : perpendicular + offset;

                if (x < chunkMinX || x >= chunkMinX + 16
                 || z < chunkMinZ || z >= chunkMinZ + 16) {
                    continue;
                }
                // Габарит чистится только внутри полотна: кромка несёт обделку
                // тоннеля и подпорную стенку выемки, и подметание их съедало.
                // Деревья же убираются по всему коридору — крона, свесившаяся на
                // кромку, мешает составу ровно так же, как над осью.
                complete &= sweepColumn(level, x, y, z, Math.abs(offset) < halfWidth);
            }
        }
        return complete;
    }

    private static boolean sweepJunction(ServerLevel level, TrackProfiler profiler,
                                         int chunkMinX, int chunkMinZ) {
        int spacing = profiler.network().spacing();
        boolean complete = true;

        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                if (!Junctions.insideFootprint(spacing, x, z)) {
                    continue;
                }
                int nodeX = Junctions.nodeCoordinate(spacing, x);
                int nodeZ = Junctions.nodeCoordinate(spacing, z);
                int y = profiler.heightAt(new Line(Line.Axis.Z,
                                                   Math.floorDiv(nodeX, spacing)), nodeZ);
                complete &= sweepColumn(level, x, y, z, true);
            }
        }
        return complete;
    }

    /**
     * Сверху вниз по колонке.
     *
     * Сплошной блок выше габарита — это свод тоннеля или склон над выемкой, и
     * расчищать габарит под ним нельзя: там либо уже пусто, либо гора, в которую
     * не надо вгрызаться. Но и **останавливать** на нём просмотр нельзя, а раньше
     * он останавливал: под козырьком выемки оставалась незамеченной вся
     * растительность, свесившаяся в габарит ниже. Поэтому «есть потолок»
     * запоминается флагом, а колонка досматривается до конца.
     */
    private static boolean sweepColumn(ServerLevel level, int x, int y, int z,
                                       boolean clearGauge) {
        int top = Math.min(y + CLEAR_HEIGHT,
                           level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
        int gauge = y + RailwayFeature.GAUGE_HEIGHT;
        boolean complete = true;
        boolean roofed = false;

        for (int cy = top; cy > y; cy--) {
            BlockPos pos = new BlockPos(x, cy, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            // Растительность потолком не считается ни на каком уровне: сквозь
            // крону надо видеть, что под ней.
            if (Vegetation.isTree(state)) {
                if (cy <= gauge) {
                    complete &= Vegetation.fell(level, pos);
                }
                continue;
            }
            if (Vegetation.isPlant(state)) {
                if (cy <= gauge) {
                    Vegetation.pull(level, pos);
                }
                continue;
            }
            if (cy > gauge) {
                roofed = true;
                continue;
            }
            if (clearGauge && !roofed) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
        return complete;
    }

    private GaugeSweeper() {}
}
