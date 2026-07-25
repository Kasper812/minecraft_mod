package dev.gensh.ironroads.graph;

import dev.gensh.ironroads.IronRoads;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.worldgen.RailwayFeature;
import dev.gensh.ironroads.worldgen.RailwayProfilers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Доводит сгенерированные пути до состояния, в котором по ним едет поезд:
 * натягивает кривые через разрывы под уклонами и регистрирует блоки путей в
 * графе Create. Worldgen-фиче это недоступно — обе операции требуют соседних
 * чанков.
 *
 * Работа никогда не делается на месте. Загрузка чанка только ставит его в
 * очередь, а разбирается очередь в обычном тике мира небольшими порциями. Иначе
 * проход попадает внутрь генерации мира: сервер держит блокировки чанковой
 * системы, а нам нужны соседи — и загрузка встаёт намертво.
 */
@Mod.EventBusSubscriber(modid = IronRoads.MOD_ID)
public final class RailwayGraphLinker {

    /**
     * Сколько чанков разбирать за тик.
     * ponytail: подобрано на глаз как заведомо незаметное. Поднимать, если
     * кривые заметно отстают от игрока, а не потому что «можно быстрее».
     */
    private static final int PER_TICK = 4;

    private static final Queue<ChunkPos> PENDING = new ArrayDeque<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel)) {
            return;
        }
        // Только готовые чанки: на прото-чанке, который ещё генерируется,
        // обращение к блокам и соседям тянет каскадную генерацию.
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        if (LinkedChunks.contains(pos)) {
            return;
        }
        LinkedChunks.add(pos);
        synchronized (PENDING) {
            PENDING.add(pos);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
         || !(event.level instanceof ServerLevel level)) {
            return;
        }
        for (int done = 0; done < PER_TICK; done++) {
            ChunkPos pos;
            synchronized (PENDING) {
                pos = PENDING.poll();
            }
            if (pos == null) {
                return;
            }
            // Чанк мог выгрузиться, пока стоял в очереди.
            if (level.hasChunk(pos.x, pos.z)) {
                link(level, pos);
            }
        }
    }

    private static void link(ServerLevel level, ChunkPos pos) {
        TrackProfiler profiler =
                RailwayProfilers.forLevel(level, level.getChunkSource().getGenerator());
        SlopeLinker linker = new SlopeLinker(profiler.network(), profiler);

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

    private RailwayGraphLinker() {}
}
