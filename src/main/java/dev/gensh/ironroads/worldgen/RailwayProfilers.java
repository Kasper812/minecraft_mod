package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RailwayProfilers {
    private static final Map<Long, TrackProfiler> CACHE = new ConcurrentHashMap<>();

    private static final ExecutorService WARMER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "IronRoads profile warmer");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    public static void warm(Runnable task) {
        WARMER.execute(task);
    }

    public static TrackProfiler forLevel(ServerLevel level, ChunkGenerator generator) {
        return CACHE.computeIfAbsent(level.getSeed(), seed -> {
            RailNetwork network = new RailNetwork(seed, RailwayFeature.SPACING,
                                                  RailwayFeature.ALIGNMENT,
                                                  Branches.sitesFor(level, generator),
                                                  Branches.obstaclesFor(level, generator));
            HeightSampler terrain = new ChunkGeneratorHeightSampler(
                    generator, level, level.getChunkSource().randomState());
            return new TrackProfiler(network, RailwayFeature.MAX_GRADE_RUN, terrain);
        });
    }

    private RailwayProfilers() {}
}
