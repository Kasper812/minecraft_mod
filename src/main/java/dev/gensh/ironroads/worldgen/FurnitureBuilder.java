package dev.gensh.ironroads.worldgen;

import com.simibubi.create.AllBlocks;
import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

final class FurnitureBuilder {
    private static final int PLATFORM_LENGTH = 5;

    static void station(WorldGenLevel level, TrackProfiler profiler,
                        BranchNetwork.Branch branch, int along, int chunkMinX, int chunkMinZ) {
        if (along != branch.toAlong() || profiler.isGap(branch.line(), along)) {
            return;
        }
        platform(level, profiler, branch.line(), along,
                 RailwayFeature.STATION_OFFSET, -RailwayFeature.STATION_OFFSET,
                 chunkMinX, chunkMinZ);
    }

    static void halt(WorldGenLevel level, TrackProfiler profiler,
                     List<BranchNetwork.Halt> halts, Line line, int along,
                     int chunkMinX, int chunkMinZ) {
        for (BranchNetwork.Halt stop : halts) {
            if (!stop.line().equals(line)
             || profiler.haltAlong(line, stop.along()) != along
             || profiler.isGap(line, along)) {
                continue;
            }
            int side = stop.side() * RailwayFeature.BED_HALF_WIDTH;
            platform(level, profiler, line, along, side, side - stop.side(),
                     chunkMinX, chunkMinZ);
        }
    }

    private static void platform(WorldGenLevel level, TrackProfiler profiler,
                                 Line line, int along, int stationOffset, int signOffset,
                                 int chunkMinX, int chunkMinZ) {
        int y = profiler.heightAt(line, along);

        place(level, profiler, line, along, y - 1, stationOffset,
              Palette.pick(Palette.Kind.SHOULDER, along, y - 1, stationOffset),
              chunkMinX, chunkMinZ);
        place(level, profiler, line, along, y, stationOffset,
              AllBlocks.TRACK_STATION.getDefaultState(), chunkMinX, chunkMinZ);

        Direction facing = line.axis() == Line.Axis.Z ? Direction.WEST : Direction.NORTH;
        BlockState sign = Blocks.OAK_SIGN.defaultBlockState()
                .setValue(BlockStateProperties.ROTATION_16, facing.get2DDataValue() * 4);
        place(level, profiler, line, along, y, signOffset, sign, chunkMinX, chunkMinZ);
    }

    static void platformLight(WorldGenLevel level, TrackProfiler profiler,
                              BranchNetwork.Branch branch, int along, int y,
                              int chunkMinX, int chunkMinZ) {
        int distance = Math.abs(along - branch.toAlong());
        if (distance == 0 || distance > PLATFORM_LENGTH || distance % 4 != 0) {
            return;
        }
        BlockState lantern = Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, false);
        for (int side = -1; side <= 1; side += 2) {
            place(level, profiler, branch.line(), along, y, side * RailwayFeature.STATION_OFFSET,
                  lantern, chunkMinX, chunkMinZ);
        }
    }

    private static void place(WorldGenLevel level, TrackProfiler profiler, Line line, int along,
                              int y, int lateralOffset, BlockState state,
                              int chunkMinX, int chunkMinZ) {
        boolean alongZ = line.axis() == Line.Axis.Z;
        int perpendicular = profiler.perpendicularAt(line, along) + lateralOffset;
        int x = alongZ ? perpendicular : along;
        int z = alongZ ? along : perpendicular;

        if (x < chunkMinX || x >= chunkMinX + 16
         || z < chunkMinZ || z >= chunkMinZ + 16) {
            return;
        }
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    private FurnitureBuilder() {}
}
