package dev.gensh.ironroads.graph;

import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import dev.gensh.ironroads.worldgen.RailwayFeature;
import dev.gensh.ironroads.worldgen.StationNames;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

final class TrackFurniture {
    private static final int NAMING_ATTEMPTS = 600;

    private record Naming(BlockPos pos, String name, int attempt) {}

    private static final Queue<Naming> UNNAMED = new ConcurrentLinkedQueue<>();

    static void attach(ServerLevel level, TrackProfiler profiler,
                       List<BranchNetwork.Branch> branches,
                       List<BranchNetwork.Halt> halts, int chunkMinX, int chunkMinZ) {
        for (BranchNetwork.Branch branch : branches) {
            bindStation(level, profiler, branch.line(), branch.toAlong(),
                        RailwayFeature.STATION_OFFSET, -RailwayFeature.STATION_OFFSET,
                        StationNames.of(branch.villageX(), branch.villageZ()),
                        chunkMinX, chunkMinZ);
        }
        for (BranchNetwork.Halt halt : halts) {
            int side = halt.side() * RailwayFeature.BED_HALF_WIDTH;
            // Тот же отъезд от разрыва, что у фичи: иначе станция ищется не там,
            // где её положили, и остаётся безымянной.
            int along = profiler.haltAlong(halt.line(), halt.along());
            bindStation(level, profiler, halt.line(), along, side, side - halt.side(),
                        StationNames.of(halt.villageX(), halt.villageZ()),
                        chunkMinX, chunkMinZ);
        }
    }

    static void tickNaming(ServerLevel level) {
        for (int done = 0, size = UNNAMED.size(); done < size; done++) {
            Naming naming = UNNAMED.poll();
            if (naming == null) {
                return;
            }
            if (level.isLoaded(naming.pos())
             && level.getBlockEntity(naming.pos()) instanceof StationBlockEntity station
             && station.getStation() != null) {
                station.updateName(naming.name());
                continue;
            }
            if (naming.attempt() < NAMING_ATTEMPTS) {
                UNNAMED.add(new Naming(naming.pos(), naming.name(), naming.attempt() + 1));
            }
        }
    }

    private static void bindStation(ServerLevel level, TrackProfiler profiler, Line line,
                                    int along, int stationOffset, int signOffset, String name,
                                    int chunkMinX, int chunkMinZ) {
        int y = profiler.heightAt(line, along);

        BlockPos station = blockAt(profiler, line, along, y, stationOffset);
        BlockPos track = blockAt(profiler, line, along, y,
                                 line.isBranch() ? 0
                                 : Integer.signum(stationOffset) * RailwayFeature.TRACK_OFFSET);

        if (!outside(station, chunkMinX, chunkMinZ)
         && level.getBlockEntity(station) instanceof StationBlockEntity entity
         && configure(level, entity, entity.edgePoint, station, track, true)) {
            UNNAMED.add(new Naming(station, name, 0));
        }

        BlockPos sign = blockAt(profiler, line, along, y, signOffset);
        if (!outside(sign, chunkMinX, chunkMinZ)
         && level.getBlockEntity(sign) instanceof SignBlockEntity board) {
            board.setText(board.getFrontText().setMessage(1, Component.literal(name)), true);
            board.setText(board.getBackText().setMessage(1, Component.literal(name)), false);
            board.setChanged();
        }
    }

    private static boolean configure(ServerLevel level, BlockEntity entity,
                                     TrackTargetingBehaviour<?> edgePoint,
                                     BlockPos self, BlockPos track, boolean positive) {
        if (!edgePoint.getGlobalPosition().equals(self)) {
            return false;
        }
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", UUID.randomUUID());
        tag.put("TargetTrack", NbtUtils.writeBlockPos(track.subtract(self)));
        tag.putBoolean("TargetDirection", positive);
        tag.putBoolean("Ortho", false);

        edgePoint.read(tag, level.registryAccess(), false);
        entity.setChanged();
        return true;
    }

    private static BlockPos blockAt(TrackProfiler profiler, Line line, int along, int y,
                                    int lateralOffset) {
        int perpendicular = profiler.perpendicularAt(line, along) + lateralOffset;
        return line.axis() == Line.Axis.Z
             ? new BlockPos(perpendicular, y, along)
             : new BlockPos(along, y, perpendicular);
    }

    private static boolean outside(BlockPos pos, int chunkMinX, int chunkMinZ) {
        return pos.getX() < chunkMinX || pos.getX() >= chunkMinX + 16
            || pos.getZ() < chunkMinZ || pos.getZ() >= chunkMinZ + 16;
    }

    private TrackFurniture() {}
}
