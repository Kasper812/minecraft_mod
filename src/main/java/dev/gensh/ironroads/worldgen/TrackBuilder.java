package dev.gensh.ironroads.worldgen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Кладёт один путь Create вдоль линии. Форма блока выбирается по тому, как
 * трасса меняется на этом шаге: ровно — прямая, со сдвигом вбок — диагональ,
 * с перепадом высоты — ascending, в точке скрещения с перпендикулярной
 * магистралью — ортогональное пересечение.
 */
public final class TrackBuilder {

    /** Смещение путей двухпутной магистрали от осевой линии. */
    static final int TRACK_OFFSET = 2;

    private final RailNetwork network;
    private final TrackProfiler profiler;

    public TrackBuilder(RailNetwork network, TrackProfiler profiler) {
        this.network = network;
        this.profiler = profiler;
    }

    /**
     * @param lateralOffset смещение пути от осевой линии: ±TRACK_OFFSET для
     *                      двух путей магистрали, 0 для однопутного ответвления
     */
    public void layTrack(WorldGenLevel level, Line line, int along, int lateralOffset,
                         int chunkMinX, int chunkMinZ) {

        boolean alongZ = line.axis() == Line.Axis.Z;
        int perpendicular = network.perpendicularAt(line, along) + lateralOffset;
        int y = profiler.heightAt(line, along);

        int x = alongZ ? perpendicular : along;
        int z = alongZ ? along : perpendicular;

        if (x < chunkMinX || x >= chunkMinX + 16
         || z < chunkMinZ || z >= chunkMinZ + 16) {
            return;  // соседний чанк положит это сам
        }

        int nextY = profiler.heightAt(line, along + 1);
        TrackShape shape = chooseShape(line, x, z, y, nextY);

        BlockState track = AllBlocks.TRACK.getDefaultState().setValue(TrackBlock.SHAPE, shape);
        level.setBlock(new BlockPos(x, y, z), track, 2);
    }

    private TrackShape chooseShape(Line line, int x, int z, int y, int nextY) {
        if (isCrossingPoint(x, z)) {
            // Обе линии кладут сюда одно и то же, поэтому порядок записи
            // блоков внутри развязки не имеет значения.
            return TrackShape.CR_O;
        }

        boolean alongZ = line.axis() == Line.Axis.Z;

        if (nextY > y) {
            // Подъём Create — 1:1. Профиль ставит такие блоки редко: при
            // уклоне 1:12 между ними длинные горизонтальные полки.
            return alongZ ? TrackShape.AS : TrackShape.AE;
        }
        if (nextY < y) {
            return alongZ ? TrackShape.AN : TrackShape.AW;
        }

        // Диагоналей здесь быть не может, и это не упрощение, а требование
        // Create. Концы стыка у прямого пути лежат в серединах граней, у
        // диагонального — в углах блока. Они не совпадают, поэтому прямой и
        // диагональный пути, поставленные рядом, не соединяются: получается
        // «ёлочка» из несвязанных обрубков. Поворот в Create — это кривая
        // Безье в данных блока, а не блок. Пока кривых нет, линия строго прямая
        // (jitter = 0 в RailwayFeature).
        return alongZ ? TrackShape.ZO : TrackShape.XO;
    }

    /**
     * Точка скрещения двух магистралей. В узле решётки джиттер обеих линий
     * погашен, поэтому пути стоят строго на базовых координатах и пересекаются
     * ровно в четырёх точках вокруг узла — по одной на каждую пару путей.
     */
    private boolean isCrossingPoint(int x, int z) {
        return Math.abs(offsetFromNode(x)) == TRACK_OFFSET
            && Math.abs(offsetFromNode(z)) == TRACK_OFFSET;
    }

    /** Знаковое расстояние до ближайшего узла решётки. */
    private int offsetFromNode(int coordinate) {
        int spacing = network.spacing();
        int distance = Math.floorMod(coordinate, spacing);
        return distance <= spacing / 2 ? distance : distance - spacing;
    }
}
