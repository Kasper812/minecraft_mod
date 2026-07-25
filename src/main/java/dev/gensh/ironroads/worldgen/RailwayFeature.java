package dev.gensh.ironroads.worldgen;

import com.mojang.serialization.Codec;
import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Ставит блоки железной дороги в пределах одного чанка.
 * Ни одного обращения к соседним чанкам: всё, что нужно знать о трассе,
 * вычисляется из сида и координат.
 */
public class RailwayFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SPACING = 768;
    private static final int JITTER = 48;
    private static final int MAX_GRADE_RUN = 12;

    /** Полуширина полотна: 3 блока между путями плюс обочины. */
    static final int BED_HALF_WIDTH = 4;

    public RailwayFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;

        RailNetwork network = new RailNetwork(level.getSeed(), SPACING, JITTER);
        HeightSampler terrain = new ChunkGeneratorHeightSampler(
                context.chunkGenerator(), level,
                level.getLevel().getChunkSource().randomState());
        TrackProfiler profiler = new TrackProfiler(network, MAX_GRADE_RUN, terrain);
        TrackBuilder trackBuilder = new TrackBuilder(network, profiler);

        boolean placedAnything = false;
        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, 8)) {
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

            for (int offset = -BED_HALF_WIDTH; offset <= BED_HALF_WIDTH; offset++) {
                int x = line.axis() == Line.Axis.Z ? perpendicular + offset : along;
                int z = line.axis() == Line.Axis.Z ? along : perpendicular + offset;

                if (x < chunkMinX || x >= chunkMinX + 16
                 || z < chunkMinZ || z >= chunkMinZ + 16) {
                    continue;  // соседний чанк построит это сам
                }

                level.setBlock(new BlockPos(x, y - 1, z),
                               Blocks.GRAVEL.defaultBlockState(), 2);
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
