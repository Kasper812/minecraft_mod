package dev.gensh.ironroads.graph;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.content.trains.track.TrackPropagator;
import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SlopeLinker {
    private static final int TRACK_OFFSET = dev.gensh.ironroads.worldgen.RailwayFeature.TRACK_OFFSET;

    private final RailNetwork network;
    private final TrackProfiler profiler;
    private final List<BranchNetwork.Branch> branches;

    private int skipped;
    private final Set<ChunkPos> waitingOn = new HashSet<>();

    SlopeLinker(RailNetwork network, TrackProfiler profiler,
                List<BranchNetwork.Branch> branches) {
        this.network = network;
        this.profiler = profiler;
        this.branches = branches;
    }

    int skipped() {
        return skipped;
    }

    /**
     * Чанки, которых не хватило кривым: их загрузка — единственное событие,
     * способное достроить разрыв. Пустое множество означает, что все кривые
     * этого чанка натянуты.
     */
    Set<ChunkPos> waitingOn() {
        return waitingOn;
    }

    int linkCurves(ServerLevel level, int chunkMinX, int chunkMinZ, int bedHalfWidth) {
        int created = 0;
        int reach = 8 + bedHalfWidth;

        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, reach)) {
            created += linkRamps(level, line, chunkMinX, chunkMinZ,
                                 Integer.MIN_VALUE, Integer.MAX_VALUE,
                                 new int[] { -TRACK_OFFSET, +TRACK_OFFSET });
        }
        for (BranchNetwork.Branch branch : branches) {
            created += linkRamps(level, branch.line(), chunkMinX, chunkMinZ,
                                 Math.min(branch.fromAlong(), branch.toAlong()),
                                 Math.max(branch.fromAlong(), branch.toAlong()),
                                 new int[] { 0 });
            created += linkSpur(level, branch);
        }
        created += linkCrossings(level, chunkMinX, chunkMinZ);
        return created;
    }

    /**
     * Внести пути чанка в граф Create.
     *
     * <p>Обход графа запускается не от каждого рельса, а от одного на сплошной
     * перегон: {@code onRailAdded} сам проходит связный путь до ближайшего
     * разрыва и заводит узлы по дороге, поэтому тридцать два вызова на чанк
     * делали одну и ту же работу тридцать два раза — а каждый из них это обход
     * с бюджетом в тысячу узлов. Затравка ставится на первый рельс перегона,
     * счётчик перегонов рвётся на разрыве под кривую: за разрыв обход не
     * проходит, пока кривая не натянута, значит по ту сторону нужна своя.
     */
    int registerTracks(ServerLevel level, int chunkMinX, int chunkMinZ, int bedHalfWidth) {
        int count = 0;
        int reach = 8 + bedHalfWidth;

        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, reach)) {
            count += register(level, line, chunkMinX, chunkMinZ,
                              Integer.MIN_VALUE, Integer.MAX_VALUE,
                              new int[] { -TRACK_OFFSET, +TRACK_OFFSET });
        }
        for (BranchNetwork.Branch branch : branches) {
            count += register(level, branch.line(), chunkMinX, chunkMinZ,
                              Math.min(branch.fromAlong(), branch.toAlong()),
                              Math.max(branch.fromAlong(), branch.toAlong()),
                              new int[] { 0 });
        }
        return count;
    }

    private int register(ServerLevel level, Line line, int chunkMinX, int chunkMinZ,
                         int alongMin, int alongMax, int[] lateralOffsets) {
        int count = 0;
        int alongStart = line.axis() == Line.Axis.Z ? chunkMinZ : chunkMinX;
        boolean[] seeded = new boolean[lateralOffsets.length];

        for (int step = 0; step < 16; step++) {
            int along = alongStart + step;
            if (along < alongMin || along > alongMax) {
                continue;
            }
            if (profiler.isGap(line, along)) {
                Arrays.fill(seeded, false);
                continue;
            }
            int y = profiler.heightAt(line, along);

            for (int lateral = 0; lateral < lateralOffsets.length; lateral++) {
                if (seeded[lateral]) {
                    continue;
                }
                BlockPos pos = blockAt(line, along, y, lateralOffsets[lateral]);
                if (pos.getX() < chunkMinX || pos.getX() >= chunkMinX + 16
                 || pos.getZ() < chunkMinZ || pos.getZ() >= chunkMinZ + 16) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof TrackBlock)) {
                    continue;
                }
                // Обход отсюда не уйдёт за прогруженное: аргумент подменён
                // миксином на LoadedOnly, см. TrackPropagatorMixin.
                TrackPropagator.onRailAdded(level, pos, state);
                seeded[lateral] = true;
                count++;
            }
        }
        return count;
    }

    private int linkRamps(ServerLevel level, Line line, int chunkMinX, int chunkMinZ,
                          int alongMin, int alongMax, int[] lateralOffsets) {
        int created = 0;
        int alongStart = line.axis() == Line.Axis.Z ? chunkMinZ : chunkMinX;

        int first = Math.floorDiv(alongStart, TrackProfiler.SEGMENT);
        int last = Math.floorDiv(alongStart + 15, TrackProfiler.SEGMENT);

        for (int boundary = first; boundary <= last; boundary++) {
            TrackProfiler.Ramp ramp =
                    profiler.rampCovering(line, boundary * TrackProfiler.SEGMENT);
            if (ramp == null
             || ramp.startAlong() < alongMin || ramp.endAlong() > alongMax) {
                continue;
            }
            Vec3 axis = alongAxis(line);
            for (int lateral : lateralOffsets) {
                created += curve(level,
                        blockAt(line, ramp.startAlong(), ramp.startHeight(), lateral), axis,
                        blockAt(line, ramp.endAlong(), ramp.endHeight(), lateral), axis.scale(-1),
                        true);
            }
        }
        return created;
    }

    private int linkCrossings(ServerLevel level, int chunkMinX, int chunkMinZ) {
        int created = 0;
        int spacing = network.spacing();
        int span = Junctions.REACH + 8;

        for (int nx = Math.floorDiv(chunkMinX - span, spacing);
             nx <= Math.floorDiv(chunkMinX + 15 + span, spacing); nx++) {
            for (int nz = Math.floorDiv(chunkMinZ - span, spacing);
                 nz <= Math.floorDiv(chunkMinZ + 15 + span, spacing); nz++) {
                if (Math.abs(nx * spacing - (chunkMinX + 8)) > span
                 || Math.abs(nz * spacing - (chunkMinZ + 8)) > span) {
                    continue;
                }
                Line zLine = new Line(Line.Axis.Z, nx);
                Line xLine = new Line(Line.Axis.X, nz);

                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sz = -1; sz <= 1; sz += 2) {
                        int alongZ = nz * spacing + sz * Junctions.REACH;
                        int alongX = nx * spacing + sx * Junctions.REACH;

                        created += curve(level,
                                blockAt(zLine, alongZ, profiler.heightAt(zLine, alongZ),
                                        sx * TRACK_OFFSET),
                                new Vec3(0, 0, -sz),
                                blockAt(xLine, alongX, profiler.heightAt(xLine, alongX),
                                        sz * TRACK_OFFSET),
                                new Vec3(-sx, 0, 0),
                                false);
                    }
                }
            }
        }
        return created;
    }

    private int linkSpur(ServerLevel level, BranchNetwork.Branch branch) {
        Line spur = branch.line();
        int spacing = network.spacing();
        int direction = branch.direction();

        Line main = new Line(spur.crossAxis(), Math.floorDiv(spur.anchorAlong(), spacing));
        int junction = spur.index() * spacing + spur.offset();

        BlockPos spurEnd = blockAt(spur, branch.fromAlong(),
                                   profiler.heightAt(spur, branch.fromAlong()), 0);
        Vec3 spurAxis = alongAxis(spur).scale(-direction);

        int created = 0;
        for (int side = -1; side <= 1; side += 2) {
            int along = profiler.clearOfGap(main, junction + side * Junctions.REACH, side);
            BlockPos mainEnd = blockAt(main, along, profiler.heightAt(main, along),
                                       direction * TRACK_OFFSET);
            created += curve(level, mainEnd, alongAxis(main).scale(-side),
                             spurEnd, spurAxis, true);
        }
        return created;
    }

    private Vec3 alongAxis(Line line) {
        return line.axis() == Line.Axis.Z ? new Vec3(0, 0, 1) : new Vec3(1, 0, 0);
    }

    private int curve(ServerLevel level, BlockPos from, Vec3 fromAxis,
                      BlockPos to, Vec3 toAxis, boolean girder) {
        if (from.equals(to)) {
            return 0;
        }
        // Записываем не «что-то не вышло», а какого именно чанка не хватило:
        // ждать по этому списку дешевле и точнее, чем будиться на любую загрузку
        // в округе. Конец кривой уезжает на MAX_SPAN вдоль и на MAX_SHIFT вбок,
        // то есть радиусом соседства это не покрыть, не перебирая полсотни чанков.
        if (!level.isLoaded(from) || !level.isLoaded(to)) {
            skipped++;
            if (!level.isLoaded(from)) {
                waitingOn.add(new ChunkPos(from));
            }
            if (!level.isLoaded(to)) {
                waitingOn.add(new ChunkPos(to));
            }
            // Не логируем: пропуск — штатное состояние растущего мира, а не
            // происшествие. Их сотни на игрока, и раньше они и составляли те
            // тысячи строк, из-за которых лог было не читать. Сколько кривых
            // не натянулось, видно из сводки по чанку в RailwayGraphLinker.
            return 0;
        }

        if (!(level.getBlockEntity(from) instanceof TrackBlockEntity fromEntity)
         || !(level.getBlockEntity(to) instanceof TrackBlockEntity toEntity)) {
            skipped++;
            if (dev.gensh.ironroads.IronRoads.DEBUG) {
                dev.gensh.ironroads.IronRoads.LOGGER.debug(
                        "curve skipped: no BE at {} ({}) or {} ({})",
                        from, level.getBlockState(from).getBlock(),
                        to, level.getBlockState(to).getBlock());
            }
            return 0;
        }
        if (fromEntity.getConnections().containsKey(to)) {
            return 0;
        }

        BlockState fromState = level.getBlockState(from);
        BlockState toState = level.getBlockState(to);
        if (!(fromState.getBlock() instanceof ITrackBlock fromTrack)
         || !(toState.getBlock() instanceof ITrackBlock toTrack)) {
            return 0;
        }

        Couple<Vec3> starts = Couple.create(
                fromTrack.getCurveStart(level, from, fromState, fromAxis),
                toTrack.getCurveStart(level, to, toState, toAxis));
        Couple<Vec3> axes = Couple.create(fromAxis.normalize(), toAxis.normalize());
        Couple<Vec3> normals = Couple.create(
                fromTrack.getUpNormal(level, from, fromState).normalize(),
                toTrack.getUpNormal(level, to, toState).normalize());

        BezierConnection connection = new BezierConnection(
                Couple.create(from, to), starts, axes, normals,
                true, girder, TrackMaterial.ANDESITE);

        fromEntity.addConnection(connection);
        toEntity.addConnection(connection.secondary());
        return 1;
    }

    private BlockPos blockAt(Line line, int along, int y, int lateralOffset) {
        int perpendicular = profiler.perpendicularAt(line, along) + lateralOffset;
        return line.axis() == Line.Axis.Z
             ? new BlockPos(perpendicular, y, along)
             : new BlockPos(along, y, perpendicular);
    }
}
