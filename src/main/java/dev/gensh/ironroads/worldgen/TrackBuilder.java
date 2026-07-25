package dev.gensh.ironroads.worldgen;

import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Кладёт один путь Create вдоль линии. Форма блока выбирается по тому, как
 * трасса меняется на этом шаге: ровно — прямая, со сдвигом вбок — диагональ,
 * с перепадом высоты — ascending, в точке скрещения с перпендикулярной
 * магистралью — ортогональное пересечение.
 */
public final class TrackBuilder {

    /** Смещение путей двухпутной магистрали от осевой линии. */
    static final int TRACK_OFFSET = 2;

    private static final ResourceLocation TRACK_ID = new ResourceLocation("create", "track");

    /**
     * Блок пути берётся из реестра по имени, а не через AllBlocks: константы
     * Registrate типизированы BlockEntry, а Registrate в толстом jar Create не
     * лежит и на compile-classpath отсутствует.
     */
    private static final BlockState TRACK = trackState();

    private final RailNetwork network;
    private final TrackProfiler profiler;

    public TrackBuilder(RailNetwork network, TrackProfiler profiler) {
        this.network = network;
        this.profiler = profiler;
    }

    private static BlockState trackState() {
        Block block = ForgeRegistries.BLOCKS.getValue(TRACK_ID);
        if (block == null || block == Blocks.AIR) {
            // Реестр блоков подставляет воздух вместо отсутствующего значения,
            // поэтому без явной проверки Create без путей дал бы молча пустой мир.
            throw new IllegalStateException(TRACK_ID + " не найден — Create не загружен");
        }
        return block.defaultBlockState();
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

        int nextPerpendicular = network.perpendicularAt(line, along + 1) + lateralOffset;
        int nextY = profiler.heightAt(line, along + 1);
        TrackShape shape = chooseShape(line, x, z, perpendicular, nextPerpendicular, y, nextY);

        level.setBlock(new BlockPos(x, y, z), TRACK.setValue(TrackBlock.SHAPE, shape), 2);
    }

    private TrackShape chooseShape(Line line, int x, int z,
                                   int perpendicular, int nextPerpendicular,
                                   int y, int nextY) {
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

        // PD — ось (+x, +z), ND — (-x, +z). Для линии вдоль Z снос по X, для
        // линии вдоль X снос по Z: в обоих случаях положительный снос даёт
        // диагональ (+x, +z), поэтому ветвление по оси не нужно.
        int drift = nextPerpendicular - perpendicular;
        if (drift == 0) {
            return alongZ ? TrackShape.ZO : TrackShape.XO;
        }
        return drift > 0 ? TrackShape.PD : TrackShape.ND;
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
