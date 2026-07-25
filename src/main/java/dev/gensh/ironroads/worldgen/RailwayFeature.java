package dev.gensh.ironroads.worldgen;

import com.mojang.serialization.Codec;
import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Профиль на каждый мир, а не на каждый чанк. Внутри него кэш высот, общий
     * для всей линии; пересоздание на каждый чанк выбрасывало этот кэш и стоило
     * ~17 тысяч обращений к шуму ландшафта на чанк — генерация мира вставала.
     * Экземпляр фичи один на игру, поэтому карта живёт здесь; ключ — сид.
     */
    private final Map<Long, TrackProfiler> profilers = new ConcurrentHashMap<>();

    public RailwayFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;

        TrackProfiler profiler = profilerFor(context);
        RailNetwork network = profiler.network();
        TrackBuilder trackBuilder = new TrackBuilder(network, profiler);

        boolean placedAnything = false;
        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, 8)) {
            placedAnything |= placeSegment(level, network, profiler, trackBuilder,
                                           line, chunkMinX, chunkMinZ);
        }
        return placedAnything;
    }

    private TrackProfiler profilerFor(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        ServerLevel serverLevel = context.level().getLevel();
        return profilers.computeIfAbsent(serverLevel.getSeed(), seed -> {
            RailNetwork network = new RailNetwork(seed, SPACING, JITTER);
            // Границы высоты берём у ServerLevel, а не у переданного
            // WorldGenLevel: тот живёт один чанк, и держать его в кэше нельзя.
            HeightSampler terrain = new ChunkGeneratorHeightSampler(
                    context.chunkGenerator(), serverLevel,
                    serverLevel.getChunkSource().randomState());
            return new TrackProfiler(network, MAX_GRADE_RUN, terrain);
        });
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
