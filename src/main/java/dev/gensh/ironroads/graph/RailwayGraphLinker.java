package dev.gensh.ironroads.graph;

import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackPropagator;
import dev.gensh.ironroads.IronRoads;
import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.worldgen.RailwayFeature;
import dev.gensh.ironroads.worldgen.ChunkGeneratorHeightSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Доводит сгенерированные пути до состояния, в котором по ним едет поезд.
 *
 * Делает две вещи, которые worldgen-фиче недоступны, потому что обе требуют
 * соседних чанков: натягивает кривые через разрывы под уклонами и регистрирует
 * блоки путей в графе Create.
 */
@Mod.EventBusSubscriber(modid = IronRoads.MOD_ID)
public final class RailwayGraphLinker {

    private static final Map<Long, SlopeLinker> LINKERS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ChunkAccess chunk = event.getChunk();
        ChunkPos pos = chunk.getPos();
        if (LinkedChunks.contains(pos)) {
            return;
        }
        LinkedChunks.add(pos);

        int curves = linkerFor(level)
                .linkSlopes(level, pos.getMinBlockX(), pos.getMinBlockZ(),
                            RailwayFeature.BED_HALF_WIDTH, RailwayFeature.TRACK_OFFSET);
        int rails = registerTracks(level, chunk);

        if (rails > 0) {
            IronRoads.LOGGER.debug("Чанк {}: путей {}, кривых {}", pos, rails, curves);
        }
    }

    /**
     * Профиль обязан быть тем же, что и при генерации, иначе концы разрывов
     * не совпадут. Он чистая функция от сида и генератора, поэтому достаточно
     * собрать его из тех же исходных данных.
     */
    private static SlopeLinker linkerFor(ServerLevel level) {
        return LINKERS.computeIfAbsent(level.getSeed(), seed -> {
            RailNetwork network = new RailNetwork(seed, RailwayFeature.SPACING,
                                                  RailwayFeature.JITTER);
            HeightSampler terrain = new ChunkGeneratorHeightSampler(
                    level.getChunkSource().getGenerator(), level,
                    level.getChunkSource().randomState());
            return new SlopeLinker(network,
                    new TrackProfiler(network, RailwayFeature.MAX_GRADE_RUN, terrain));
        });
    }

    private static int registerTracks(ServerLevel level, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int count = 0;

        for (int index = 0; index < chunk.getSectionsCount(); index++) {
            LevelChunkSection section = chunk.getSection(index);
            if (section.hasOnlyAir()) {
                continue;
            }
            int sectionMinY = chunk.getMinBuildHeight() + index * 16;

            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!(state.getBlock() instanceof TrackBlock)) {
                            continue;
                        }
                        TrackPropagator.onRailAdded(level,
                                new BlockPos(pos.getMinBlockX() + x,
                                             sectionMinY + y,
                                             pos.getMinBlockZ() + z),
                                state);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private RailwayGraphLinker() {}
}
