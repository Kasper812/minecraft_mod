package dev.gensh.ironroads.worldgen;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllBlocks;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;


/**
 * Ставит блоки железной дороги в пределах одного чанка.
 * Ни одного обращения к соседним чанкам: всё, что нужно знать о трассе,
 * вычисляется из сида и координат.
 */
public class RailwayFeature extends Feature<NoneFeatureConfiguration> {

    // Открыты: проход при загрузке чанка обязан собрать ровно тот же профиль,
    // иначе концы разрывов под кривые не совпадут с уложенными блоками.
    public static final int SPACING = 768;

    /**
     * Пока 0: линия обязана быть строго прямой. Виляние требует поворотов, а
     * поворот в Create — кривая Безье в данных блока, не блок. Диагональными
     * блоками прямую линию не изогнуть: они не стыкуются с прямыми. Значение
     * оживёт, когда появится генерация кривых.
     */
    public static final int JITTER = 0;

    public static final int MAX_GRADE_RUN = 12;

    /** Полуширина полотна: 3 блока между путями плюс обочины. */
    public static final int BED_HALF_WIDTH = 4;

    /** Смещение путей двухпутной магистрали от осевой линии. */
    public static final int TRACK_OFFSET = TrackBuilder.TRACK_OFFSET;

    public RailwayFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;

        TrackProfiler profiler = RailwayProfilers.forLevel(level.getLevel(),
                                                          context.chunkGenerator());
        RailNetwork network = profiler.network();
        TrackBuilder trackBuilder = new TrackBuilder(network, profiler);

        boolean placedAnything = false;
        // Радиус обязан включать полуширину полотна: линия, чья осевая проходит
        // за границей чанка, всё равно кладёт сюда часть насыпи. С прежним
        // радиусом 8 такие полосы терялись — раньше это маскировал джиттер,
        // раздувавший коридор поиска на 48 блоков.
        int reach = 8 + BED_HALF_WIDTH;
        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, reach)) {
            placedAnything |= placeSegment(level, network, profiler, trackBuilder,
                                           line, chunkMinX, chunkMinZ);
        }
        return placedAnything;
    }


    private boolean placeSegment(WorldGenLevel level, RailNetwork network,
                                 TrackProfiler profiler, TrackBuilder trackBuilder,
                                 Line line, int chunkMinX, int chunkMinZ) {
        boolean placed = false;
        int alongStart = line.axis() == Line.Axis.Z ? chunkMinZ : chunkMinX;

        for (int step = 0; step < 16; step++) {
            int along = alongStart + step;
            int perpendicular = network.perpendicularAt(line, along);
            int y = profiler.heightAt(line, along);

            // Под кривой насыпи нет: её держат балки самой кривой. Габарит
            // всё равно расчищаем, иначе дуга уйдёт в рельеф.
            boolean gap = profiler.isGap(line, along);

            for (int offset = -BED_HALF_WIDTH; offset <= BED_HALF_WIDTH; offset++) {
                int x = line.axis() == Line.Axis.Z ? perpendicular + offset : along;
                int z = line.axis() == Line.Axis.Z ? along : perpendicular + offset;

                if (x < chunkMinX || x >= chunkMinX + 16
                 || z < chunkMinZ || z >= chunkMinZ + 16) {
                    continue;  // соседний чанк построит это сам
                }

                if (!gap) {
                    level.setBlock(new BlockPos(x, y - 1, z), bedBlock(offset), 2);
                }
                clearGauge(level, x, y, z);
                placed = true;
            }

            // Строго после расчистки габарита: иначе она снесла бы рельсы.
            trackBuilder.layTrack(level, line, along, -TrackBuilder.TRACK_OFFSET,
                                  chunkMinX, chunkMinZ);
            trackBuilder.layTrack(level, line, along, +TrackBuilder.TRACK_OFFSET,
                                  chunkMinX, chunkMinZ);
        }
        return placed;
    }

    /**
     * Полотно из блоков Create: под самими путями железнодорожный корпус,
     * по краям андезитовый настил. Обочина шириной 2 блока с каждой стороны
     * отделяет полотно от рельефа, чтобы насыпь читалась как насыпь.
     */
    private BlockState bedBlock(int offset) {
        return Math.abs(offset) <= TrackBuilder.TRACK_OFFSET
             ? AllBlocks.RAILWAY_CASING.getDefaultState()
             : AllBlocks.ANDESITE_SCAFFOLD.getDefaultState();
    }

    /** Вырубает пространство над полотном: 4 блока по высоте. */
    private void clearGauge(WorldGenLevel level, int x, int y, int z) {
        for (int dy = 0; dy < 4; dy++) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
}
