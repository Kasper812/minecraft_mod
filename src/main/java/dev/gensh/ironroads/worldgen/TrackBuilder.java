package dev.gensh.ironroads.worldgen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import dev.gensh.ironroads.geometry.BranchNetwork;
import dev.gensh.ironroads.geometry.Junctions;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class TrackBuilder {
    static final int TRACK_OFFSET = 2;

    private final RailNetwork network;
    private final TrackProfiler profiler;
    private final List<BranchNetwork.Branch> branches;

    public TrackBuilder(RailNetwork network, TrackProfiler profiler,
                        List<BranchNetwork.Branch> branches) {
        this.network = network;
        this.profiler = profiler;
        this.branches = branches;
    }

    public void layTrack(WorldGenLevel level, Line line, int along, int lateralOffset,
                         int chunkMinX, int chunkMinZ) {
        boolean alongZ = line.axis() == Line.Axis.Z;
        int perpendicular = profiler.perpendicularAt(line, along) + lateralOffset;
        int y = profiler.heightAt(line, along);

        int x = alongZ ? perpendicular : along;
        int z = alongZ ? along : perpendicular;

        if (x < chunkMinX || x >= chunkMinX + 16
         || z < chunkMinZ || z >= chunkMinZ + 16) {
            return;
        }

        if (profiler.isGap(line, along)) {
            return;
        }

        boolean entity = needsEntity(line, along, lateralOffset);
        BlockState track = AllBlocks.TRACK.getDefaultState()
                .setValue(TrackBlock.SHAPE, chooseShape(line, x, z))
                .setValue(TrackBlock.HAS_BE, entity);

        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, track, 2);

        // Заглушку блочной сущности убираем сразу за setBlock, и это не
        // косметика. TrackBlock у Create — EntityBlock, поэтому
        // state.hasBlockEntity() истинно у любого пути, и WorldGenRegion.setBlock
        // кладёт в протокусок NBT-заглушку с id "DUMMY" на каждый рельс. При
        // загрузке чанка promotePendingBlockEntity пытается её развернуть,
        // newBlockEntity при HAS_BE = false отдаёт null — и в лог уходит
        // предупреждение на каждый рельс. Это и были те тысячи строк: на одном
        // прогоне спавна их набегало больше тысячи, впятеро больше всего
        // остального лога вместе взятого. Заглушка нужна только там, где
        // сущность правда будет: на концах разрыва под кривую.
        if (!entity) {
            level.getChunk(pos).removeBlockEntity(pos);
        }
    }

    private TrackShape chooseShape(Line line, int x, int z) {
        if (!line.isBranch() && isCrossingPoint(x, z)) {
            return TrackShape.CR_O;
        }

        boolean alongZ = line.axis() == Line.Axis.Z;

        return alongZ ? TrackShape.ZO : TrackShape.XO;
    }

    private boolean needsEntity(Line line, int along, int lateralOffset) {
        if (profiler.isRampEnd(line, along)) {
            return true;
        }
        int spacing = network.spacing();

        if (line.isBranch()) {
            for (BranchNetwork.Branch branch : branches) {
                if (branch.line().equals(line) && along == branch.fromAlong()) {
                    return true;
                }
            }
            return false;
        }

        if (Junctions.isCrossingAnchor(spacing, along)) {
            return true;
        }

        for (BranchNetwork.Branch branch : branches) {
            Line spur = branch.line();
            if (spur.axis() == line.axis()
             || spur.anchorAlong() != line.index() * spacing
             || lateralOffset != branch.direction() * TRACK_OFFSET) {
                continue;
            }
            int junction = spur.index() * spacing + spur.offset();
            for (int side = -1; side <= 1; side += 2) {
                if (along == profiler.clearOfGap(line, junction + side * Junctions.REACH, side)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCrossingPoint(int x, int z) {
        return Math.abs(offsetFromNode(x)) == TRACK_OFFSET
            && Math.abs(offsetFromNode(z)) == TRACK_OFFSET;
    }

    private int offsetFromNode(int coordinate) {
        int spacing = network.spacing();
        int distance = Math.floorMod(coordinate, spacing);
        return distance <= spacing / 2 ? distance : distance - spacing;
    }
}
