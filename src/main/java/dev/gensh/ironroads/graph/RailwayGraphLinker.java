package dev.gensh.ironroads.graph;

import dev.gensh.ironroads.IronRoads;
import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.worldgen.ChunkGeneratorHeightSampler;
import dev.gensh.ironroads.worldgen.RailwayFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
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
        // Только готовые чанки. На прото-чанке, который ещё генерируется,
        // обращение к блокам и к соседям тянет за собой каскадную генерацию.
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        if (LinkedChunks.contains(pos)) {
            return;
        }
        LinkedChunks.add(pos);

        // Не внутри колбэка загрузки: там сервер держит блокировки чанковой
        // системы, а нам нужно читать соседние чанки.
        level.getServer().execute(() -> link(level, pos));
    }

    private static void link(ServerLevel level, ChunkPos pos) {
        SlopeLinker linker = linkerFor(level);
        int curves = linker.linkSlopes(level, pos.getMinBlockX(), pos.getMinBlockZ(),
                                       RailwayFeature.BED_HALF_WIDTH,
                                       RailwayFeature.TRACK_OFFSET);
        int rails = linker.registerTracks(level, pos.getMinBlockX(), pos.getMinBlockZ(),
                                          RailwayFeature.BED_HALF_WIDTH,
                                          RailwayFeature.TRACK_OFFSET);
        if (rails > 0) {
            IronRoads.LOGGER.debug("Чанк {}: путей {}, кривых {}", pos, rails, curves);
        }
    }

    /**
     * Профиль обязан быть тем же, что и при генерации, иначе концы разрывов
     * не совпадут с уложенными блоками. Он чистая функция от сида и генератора,
     * поэтому достаточно собрать его из тех же исходных данных.
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

    private RailwayGraphLinker() {}
}
