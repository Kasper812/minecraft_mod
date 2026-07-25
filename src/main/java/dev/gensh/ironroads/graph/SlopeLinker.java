package dev.gensh.ironroads.graph;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Натягивает кривые Безье через разрывы, оставленные worldgen-фичей под уклоны.
 *
 * Фича этого сделать не может: кривая хранится в BlockEntity обоих своих концов,
 * а концы разнесены на десятки блоков и почти всегда лежат в разных чанках.
 * Здесь же есть полноценный ServerLevel с загруженными соседями.
 *
 * Геометрия разрывов детерминирована, поэтому концы не ищутся перебором блоков,
 * а вычисляются из тех же RailNetwork и TrackProfiler, что и при генерации.
 */
final class SlopeLinker {

    private final RailNetwork network;
    private final TrackProfiler profiler;

    SlopeLinker(RailNetwork network, TrackProfiler profiler) {
        this.network = network;
        this.profiler = profiler;
    }

    /**
     * Обрабатывает все уклоны, задевающие этот чанк.
     *
     * @return сколько кривых создано
     */
    int linkSlopes(ServerLevel level, int chunkMinX, int chunkMinZ, int bedHalfWidth,
                   int trackOffset) {
        int created = 0;
        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, 8 + bedHalfWidth)) {
            int alongStart = line.axis() == Line.Axis.Z ? chunkMinZ : chunkMinX;

            int first = Math.floorDiv(alongStart, TrackProfiler.SEGMENT);
            int last = Math.floorDiv(alongStart + 15, TrackProfiler.SEGMENT);

            for (int boundary = first; boundary <= last; boundary++) {
                TrackProfiler.Ramp ramp =
                        profiler.rampCovering(line, boundary * TrackProfiler.SEGMENT);
                if (ramp == null) {
                    continue;
                }
                created += connect(level, line, ramp, -trackOffset);
                created += connect(level, line, ramp, +trackOffset);
            }
        }
        return created;
    }

    /** Одна кривая: между блоками на концах разрыва для одного из двух путей. */
    private int connect(ServerLevel level, Line line, TrackProfiler.Ramp ramp,
                        int lateralOffset) {

        BlockPos from = blockAt(line, ramp.startAlong(), ramp.startHeight(), lateralOffset);
        BlockPos to = blockAt(line, ramp.endAlong(), ramp.endHeight(), lateralOffset);

        // Второй конец может лежать в ещё не загруженном чанке. Тогда кривую
        // создаст загрузка того чанка: она переберёт те же уклоны.
        if (!level.isLoaded(from) || !level.isLoaded(to)) {
            return 0;
        }

        if (!(level.getBlockEntity(from) instanceof TrackBlockEntity fromEntity)
         || !(level.getBlockEntity(to) instanceof TrackBlockEntity toEntity)) {
            return 0;
        }
        if (fromEntity.getConnections().containsKey(to)) {
            return 0;  // уже натянута с другой стороны
        }

        BlockState fromState = level.getBlockState(from);
        BlockState toState = level.getBlockState(to);
        if (!(fromState.getBlock() instanceof ITrackBlock fromTrack)
         || !(toState.getBlock() instanceof ITrackBlock toTrack)) {
            return 0;
        }

        // Ось указывает внутрь кривой: от своего конца к чужому.
        Vec3 alongAxis = line.axis() == Line.Axis.Z ? new Vec3(0, 0, 1) : new Vec3(1, 0, 0);
        Vec3 fromAxis = alongAxis;
        Vec3 toAxis = alongAxis.scale(-1);

        // Точки стыка и вертикаль считает сам Create — угадывать смещения не нужно.
        Couple<Vec3> starts = Couple.create(
                fromTrack.getCurveStart(level, from, fromState, fromAxis),
                toTrack.getCurveStart(level, to, toState, toAxis));
        Couple<Vec3> axes = Couple.create(fromAxis.normalize(), toAxis.normalize());
        Couple<Vec3> normals = Couple.create(
                fromTrack.getUpNormal(level, from, fromState).normalize(),
                toTrack.getUpNormal(level, to, toState).normalize());

        BezierConnection curve = new BezierConnection(
                Couple.create(from, to), starts, axes, normals,
                true, true, TrackMaterial.ANDESITE);

        fromEntity.addConnection(curve);
        toEntity.addConnection(curve.secondary());
        return 1;
    }

    private BlockPos blockAt(Line line, int along, int y, int lateralOffset) {
        int perpendicular = network.perpendicularAt(line, along) + lateralOffset;
        return line.axis() == Line.Axis.Z
             ? new BlockPos(perpendicular, y, along)
             : new BlockPos(along, y, perpendicular);
    }
}
