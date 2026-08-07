package dev.gensh.ironroads.graph;

import dev.gensh.ironroads.IronRoads;
import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.worldgen.RailwayFeature;
import dev.gensh.ironroads.worldgen.RailwayProfilers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

@EventBusSubscriber(modid = IronRoads.MOD_ID)
public final class RailwayGraphLinker {
    private static final long BUDGET_NANOS = 3_000_000L;

    private static final int WALK_CHUNKS = 20;

    private static final int NEIGHBOUR_CHUNKS = 6;

    // ponytail: 100 тиков ожидания, потом сшиваем как есть. Требование
    // runLoadedFor (20 чанков в каждую сторону) на узле решётки недостижимо при
    // дальности прорисовки меньше 20, и чанк узла оставался неучтённым навсегда,
    // перезапуская подметание и кривые каждый тик. Цена — один возможный рывок
    // раз в 5 секунд ожидания; убрать, когда появится способ спросить, сгенерирован
    // ли чанк, не загружая его.
    private static final int FORCE_AFTER = 100;

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        PendingChunks queue = PendingChunks.of(level.dimension());

        // Сосед, которого не хватало чужой валке, только что появился — единственное
        // событие, способное её разблокировать. Будим независимо от того, есть ли
        // дорога в самом загрузившемся чанке: дерево у пути вполне может стоять
        // корнями в чанке без дороги.
        queue.wakeNeighbours(pos);
        queue.wakeWaiters(pos);

        if (!carriesRailway(level, pos)) {
            return;
        }
        queue.offer(pos);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        TrackFurniture.tickNaming(level);

        PendingChunks queue = PendingChunks.of(level.dimension());
        queue.promoteDeferred();
        long deadline = System.nanoTime() + BUDGET_NANOS;
        do {
            ChunkPos pos = queue.poll();
            if (pos == null) {
                return;
            }
            if (!level.hasChunk(pos.x, pos.z)) {
                queue.drop(pos);
                continue;
            }
            boolean forced = queue.attempts(pos) >= FORCE_AFTER;
            if (!forced && !neighboursLoaded(level, pos)) {
                queue.defer(pos);
                continue;
            }
            if (link(level, pos, forced)) {
                queue.forget(pos);
            } else {
                queue.defer(pos);
            }
        } while (System.nanoTime() < deadline);
    }

    private static boolean neighboursLoaded(ServerLevel level, ChunkPos pos) {
        for (int dx = -NEIGHBOUR_CHUNKS; dx <= NEIGHBOUR_CHUNKS; dx++) {
            for (int dz = -NEIGHBOUR_CHUNKS; dz <= NEIGHBOUR_CHUNKS; dz++) {
                if (!level.hasChunk(pos.x + dx, pos.z + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean runLoadedFor(ServerLevel level, ChunkPos pos) {
        RailNetwork network = RailwayProfilers
                .forLevel(level, level.getChunkSource().getGenerator()).network();
        int spacing = network.spacing();

        for (Line line : network.linesNear(pos.getMinBlockX() + 8, pos.getMinBlockZ() + 8,
                                           8 + RailwayFeature.BED_HALF_WIDTH)) {
            if (!runLoaded(level, line, pos, spacing, +1)
             || !runLoaded(level, line, pos, spacing, -1)) {
                return false;
            }
        }
        return true;
    }

    private static boolean runLoaded(ServerLevel level, Line line, ChunkPos pos,
                                     int spacing, int direction) {
        boolean alongZ = line.axis() == Line.Axis.Z;
        int fixed = alongZ ? pos.x : pos.z;
        int start = alongZ ? pos.z : pos.x;

        for (int step = 1; step <= WALK_CHUNKS; step++) {
            int moving = start + direction * step;
            int chunkX = alongZ ? fixed : moving;
            int chunkZ = alongZ ? moving : fixed;
            if (!level.hasChunk(chunkX, chunkZ)) {
                return false;
            }
            if (Junctions.insideFootprint(spacing, chunkX * 16 + 8, chunkZ * 16 + 8)) {
                return true;
            }
        }
        return true;
    }

    private static boolean carriesRailway(ServerLevel level, ChunkPos pos) {
        RailNetwork network = RailwayProfilers
                .forLevel(level, level.getChunkSource().getGenerator()).network();
        int reach = RailwayFeature.REACH;
        int centerX = pos.getMinBlockX() + 8;
        int centerZ = pos.getMinBlockZ() + 8;
        int spacing = network.spacing();

        if (Math.abs(Junctions.offsetFromNode(spacing, centerX)) <= Junctions.FOOTPRINT + 8
         && Math.abs(Junctions.offsetFromNode(spacing, centerZ)) <= Junctions.FOOTPRINT + 8) {
            return true;
        }
        return !network.linesNear(centerX, centerZ, reach).isEmpty()
            || !new BranchNetwork(network).branchesNear(centerX, centerZ, reach).isEmpty();
    }

    private static boolean link(ServerLevel level, ChunkPos pos, boolean forced) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        TrackProfiler profiler = RailwayProfilers.forLevel(level, generator);
        RailNetwork network = profiler.network();

        // Проверка прогрузки перегона стоит десяток hasChunk и идёт первой.
        // Раньше она стояла после подметания, кривых и обстановки — то есть
        // неудачная попытка каждый тик заново подметала габарит целого чанка и
        // заново перебирала кривые, чтобы в конце выбросить всё сделанное и
        // отложиться. На чанке узла решётки, который до forced ждёт сто тиков,
        // это сотня полных проходов впустую. Отказ обязан быть дешёвым.
        if (!forced && !runLoadedFor(level, pos)) {
            return false;
        }

        int reach = 8 + RailwayFeature.BED_HALF_WIDTH;
        int spurReach = reach + Junctions.REACH;
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        PendingChunks queue = PendingChunks.of(level.dimension());

        BranchNetwork spurs = new BranchNetwork(network);
        List<BranchNetwork.Branch> branches =
                spurs.branchesNear(minX + 8, minZ + 8, spurReach);

        List<BranchNetwork.Halt> halts = spurs.haltsNear(minX + 8, minZ + 8, reach);

        if (!profileReady(profiler, network, branches, minX, minZ)) {
            RailwayProfilers.warm(() -> {
                warmProfile(profiler, network, branches, minX, minZ);
                queue.requeue(pos);
            });
            return true;
        }

        // Уборка габарита живёт своей очередью и через forced не проваливается.
        // Сшивание путей имеет право сдаться по таймауту — иначе чанк узла решётки
        // не сошьётся никогда. Уборка такого права не имеет: несваленное дерево
        // стоит поперёк пути навсегда. Именно на этом месте деревья и терялись —
        // forced затирал результат уборки, и чанк забывался насовсем.
        if (GaugeSweeper.clear(level, profiler, branches, minX, minZ)) {
            queue.markSwept(pos);
        } else {
            queue.markUnswept(pos);
        }

        SlopeLinker linker = new SlopeLinker(network, profiler, branches);
        int curves = linker.linkCurves(level, minX, minZ, RailwayFeature.BED_HALF_WIDTH);
        TrackFurniture.attach(level, profiler, branches, halts, minX, minZ);

        // Пропущенная кривая переживает и forced, и удаление из очереди: чанк
        // записывается в ждущие того чанка, которого не хватило. Без этого
        // forced отдавал «готово» вместе с пустым разрывом — путь обрывался
        // навсегда, и именно это видно в игре как несшитые концы.
        queue.waitFor(pos, linker.waitingOn());

        int rails = linker.registerTracks(level, minX, minZ, RailwayFeature.BED_HALF_WIDTH);

        if (rails > 0 && IronRoads.DEBUG) {
            IronRoads.LOGGER.debug("Чанк {}: затравок {}, кривых {}, пропущено {}",
                                   pos, rails, curves, linker.skipped());
        }
        return forced || linker.skipped() == 0;
    }

    private static boolean profileReady(TrackProfiler profiler, RailNetwork network,
                                        List<BranchNetwork.Branch> branches,
                                        int minX, int minZ) {
        int reach = 8 + RailwayFeature.BED_HALF_WIDTH;
        for (Line line : network.linesNear(minX + 8, minZ + 8, reach)) {
            int start = line.axis() == Line.Axis.Z ? minZ : minX;
            if (!profiler.isReady(line, start) || !profiler.isReady(line, start + 15)) {
                return false;
            }
        }
        for (BranchNetwork.Branch branch : branches) {
            int start = branch.line().axis() == Line.Axis.Z ? minZ : minX;
            if (!profiler.isReady(branch.line(), start)
             || !profiler.isReady(branch.line(), start + 15)) {
                return false;
            }
        }
        return true;
    }

    private static void warmProfile(TrackProfiler profiler, RailNetwork network,
                                    List<BranchNetwork.Branch> branches, int minX, int minZ) {
        int reach = 8 + RailwayFeature.BED_HALF_WIDTH;
        for (Line line : network.linesNear(minX + 8, minZ + 8, reach)) {
            int start = line.axis() == Line.Axis.Z ? minZ : minX;
            for (int step = 0; step < 16; step++) {
                profiler.heightAt(line, start + step);
            }
        }
        for (BranchNetwork.Branch branch : branches) {
            int start = branch.line().axis() == Line.Axis.Z ? minZ : minX;
            for (int step = 0; step < 16; step++) {
                profiler.heightAt(branch.line(), start + step);
            }
        }
    }

    private RailwayGraphLinker() {}
}
