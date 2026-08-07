package dev.gensh.ironroads.geometry;

import java.util.ArrayList;
import java.util.List;

public final class BranchNetwork {
    public static final int MAX_LENGTH = 384;
    public static final int TURNOUT_REACH = 16;

    /**
     * Насколько ветка не доходит до центра деревни.
     *
     * Деревня — не точка: постройки расходятся от центра на десятки блоков, а
     * ветка приходит не одним рельсом, а насыпью, выемкой и полосой отвода.
     * Прежние 24 блока ставили конечную станцию посреди застройки: отсыпка
     * вставала стеной поперёк улицы, выемка резала выход из дома — жаловались
     * ровно на это. Радиус взят по габариту ванильной деревни; станция встаёт
     * на околице, как ей и положено, и ни один блок дороги не ложится в улицу.
     */
    private static final int STOP_SHORT = 72;

    /**
     * Ближе этого ветка не строится вовсе: такую деревню обслуживает остановка
     * на самой магистрали. Порог обязан быть заметно больше {@link #STOP_SHORT}
     * плюс {@link #TURNOUT_REACH} — иначе «ветка» выходит нулевой длины, а
     * станция уезжает за магистраль. Прежние 64 давали ветку, которая целиком
     * помещалась внутри деревни.
     */
    private static final int MIN_LENGTH = STOP_SHORT + TURNOUT_REACH + 24;

    private static final int MIN_SIDE_CLEARANCE = 48;

    public record Branch(Line line, int fromAlong, int toAlong, int villageX, int villageZ) {
        public int direction() {
            return Integer.signum(toAlong - fromAlong);
        }

        public boolean covers(int along) {
            return along >= Math.min(fromAlong, toAlong)
                && along <= Math.max(fromAlong, toAlong);
        }
    }

    public record Halt(Line line, int along, int side, int villageX, int villageZ) {}

    private final RailNetwork network;

    public BranchNetwork(RailNetwork network) {
        this.network = network;
    }

    public List<Halt> haltsNear(int blockX, int blockZ, int radius) {
        List<Halt> result = new ArrayList<>(1);
        int cell = cell();
        // Запас на отъезд от разрыва (TrackProfiler.haltAlong): платформа встаёт
        // до отрезка дальше по ходу, и без запаса её чанк остановку не нашёл бы.
        int slack = radius + TrackProfiler.SEGMENT;
        int reach = MIN_LENGTH + slack;

        for (int rx = Math.floorDiv(blockX - reach, cell);
             rx <= Math.floorDiv(blockX + reach, cell); rx++) {
            for (int rz = Math.floorDiv(blockZ - reach, cell);
                 rz <= Math.floorDiv(blockZ + reach, cell); rz++) {
                Halt halt = haltFor(rx, rz);
                if (halt == null) {
                    continue;
                }
                boolean alongX = halt.line().axis() == Line.Axis.X;
                int along = alongX ? blockX : blockZ;
                int perpendicular = alongX ? blockZ : blockX;
                if (Math.abs(halt.along() - along) <= slack
                 && Math.abs(network.perpendicularAt(halt.line(), halt.along()) - perpendicular)
                    <= radius + TrackProfiler.MAX_SHIFT) {
                    result.add(halt);
                }
            }
        }
        return result;
    }

    public Halt haltFor(int regionX, int regionZ) {
        long packed = village(regionX, regionZ);
        if (packed == VillageSites.NONE) {
            return null;
        }
        int villageX = (int) (packed >> 32);
        int villageZ = (int) packed;

        int spacing = network.spacing();
        int anchorX = Math.floorDiv(villageX + spacing / 2, spacing) * spacing;
        int anchorZ = Math.floorDiv(villageZ + spacing / 2, spacing) * spacing;
        int dx = Math.abs(villageX - anchorX);
        int dz = Math.abs(villageZ - anchorZ);

        if (Math.min(dx, dz) >= MIN_LENGTH) {
            return null;
        }
        boolean onZLine = dx <= dz;
        Line main = onZLine
                  ? new Line(Line.Axis.Z, Math.floorDiv(anchorX, spacing))
                  : new Line(Line.Axis.X, Math.floorDiv(anchorZ, spacing));
        int along = onZLine ? villageZ : villageX;
        int side = onZLine ? Integer.signum(villageX - anchorX)
                           : Integer.signum(villageZ - anchorZ);
        return new Halt(main, along, side == 0 ? 1 : side, villageX, villageZ);
    }

    /**
     * Расстояние вдоль линии до ближайшей стрелки, но не больше {@code reach}.
     *
     * <p>Профиль сводит боковой увод к нулю по этому расстоянию: съезд строится
     * от точки на магистрали к точке на ветке, и уехавшая вбок магистраль
     * подходит к ветке вплотную — кривой негде натянуться. По расстоянию, а не
     * по флагу «стрелка рядом»: флаг обнулял увод скачком, и при уводе в
     * полсотни блоков ради деревни это был бы рывок вбок за один разрыв.
     *
     * <p>Остановки на обочине здесь не считаются, хотя раньше считались:
     * остановка стоит ровно у деревни, а деревню путь обходит — держать ради неё
     * линию на оси значило бы не обходить деревню никогда. Платформа едет вместе
     * с путём, а от разрыва её уводит {@link TrackProfiler#haltAlong}.
     */
    public int turnoutDistance(Line main, int along, int reach) {
        if (main.isBranch()) {
            return reach;
        }
        int spacing = network.spacing();
        int mainPerpendicular = main.index() * spacing;
        boolean mainAlongX = main.axis() == Line.Axis.X;
        int cell = cell();

        int minAlong = along - reach;
        int maxAlong = along + reach;
        int minSide = mainPerpendicular - MAX_LENGTH;
        int maxSide = mainPerpendicular + MAX_LENGTH;

        int minX = mainAlongX ? minAlong : minSide;
        int maxX = mainAlongX ? maxAlong : maxSide;
        int minZ = mainAlongX ? minSide : minAlong;
        int maxZ = mainAlongX ? maxSide : maxAlong;

        int best = reach;
        for (int rx = Math.floorDiv(minX, cell); rx <= Math.floorDiv(maxX, cell); rx++) {
            for (int rz = Math.floorDiv(minZ, cell); rz <= Math.floorDiv(maxZ, cell); rz++) {
                best = Math.min(best, distanceTo(branchFor(rx, rz), main, spacing, along, reach));
            }
        }
        return best;
    }

    /**
     * Ближайшая деревня, мимо которой пойдёт линия, или {@link VillageSites#NONE}.
     *
     * @param reach насколько далеко вдоль линии смотреть
     * @param band  насколько далеко вбок от оси деревня ещё мешает
     */
    public long villageNear(Line line, int along, int reach, int band) {
        return siteNear(network.villages(), line, along, reach, band);
    }

    public long obstacleNear(Line line, int along, int reach, int band) {
        return siteNear(network.obstacles(), line, along, reach, band);
    }

    private long siteNear(VillageSites sites, Line line, int along, int reach, int band) {
        if (line.isBranch()) {
            return VillageSites.NONE;
        }
        boolean alongX = line.axis() == Line.Axis.X;
        int axis = line.index() * network.spacing();
        int cell = sites.spacingChunks() * 16;

        int minAlong = along - reach;
        int maxAlong = along + reach;
        int minX = alongX ? minAlong : axis - band;
        int maxX = alongX ? maxAlong : axis + band;
        int minZ = alongX ? axis - band : minAlong;
        int maxZ = alongX ? axis + band : maxAlong;

        long best = VillageSites.NONE;
        int bestDistance = reach;

        for (int rx = Math.floorDiv(minX, cell); rx <= Math.floorDiv(maxX, cell); rx++) {
            for (int rz = Math.floorDiv(minZ, cell); rz <= Math.floorDiv(maxZ, cell); rz++) {
                long packed = sites.siteAt(network.seed(), rx, rz);
                if (packed == VillageSites.NONE) {
                    continue;
                }
                int villageX = (int) (packed >> 32);
                int villageZ = (int) packed;
                int villageAlong = alongX ? villageX : villageZ;
                int villageSide = alongX ? villageZ : villageX;

                int distance = Math.abs(villageAlong - along);
                if (distance >= bestDistance || Math.abs(villageSide - axis) > band) {
                    continue;
                }
                best = packed;
                bestDistance = distance;
            }
        }
        return best;
    }

    private int distanceTo(Branch branch, Line main, int spacing, int along, int reach) {
        if (branch == null || branch.line().axis() == main.axis()
         || branch.line().anchorAlong() != main.index() * spacing) {
            return reach;
        }
        int junction = branch.line().index() * spacing + branch.line().offset();
        return Math.min(reach, Math.abs(junction - along));
    }

    public List<Branch> branchesNear(int blockX, int blockZ, int radius) {
        List<Branch> result = new ArrayList<>(2);
        int reach = MAX_LENGTH + radius;
        int cell = cell();

        for (int rx = Math.floorDiv(blockX - reach, cell);
             rx <= Math.floorDiv(blockX + reach, cell); rx++) {
            for (int rz = Math.floorDiv(blockZ - reach, cell);
                 rz <= Math.floorDiv(blockZ + reach, cell); rz++) {
                Branch branch = branchFor(rx, rz);
                if (branch != null && touches(branch, blockX, blockZ, radius)) {
                    result.add(branch);
                }
            }
        }
        return result;
    }

    private long village(int regionX, int regionZ) {
        return network.villages().siteAt(network.seed(), regionX, regionZ);
    }

    /** Сторона региона деревень в блоках — шаг перебора кандидатов. */
    private int cell() {
        return network.villages().spacingChunks() * 16;
    }

    public Branch branchFor(int regionX, int regionZ) {
        long packed = village(regionX, regionZ);
        if (packed == VillageSites.NONE) {
            return null;
        }
        int villageX = (int) (packed >> 32);
        int villageZ = (int) packed;

        int spacing = network.spacing();
        int anchorX = Math.floorDiv(villageX + spacing / 2, spacing) * spacing;
        int anchorZ = Math.floorDiv(villageZ + spacing / 2, spacing) * spacing;

        boolean alongX = Math.abs(villageX - anchorX) <= Math.abs(villageZ - anchorZ);
        Line.Axis axis = alongX ? Line.Axis.X : Line.Axis.Z;
        int anchorAlong = alongX ? anchorX : anchorZ;
        int villageAlong = alongX ? villageX : villageZ;
        int perpendicular = alongX ? villageZ : villageX;

        int length = Math.abs(villageAlong - anchorAlong);
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            return null;
        }

        int index = Math.floorDiv(perpendicular, spacing);
        int offset = perpendicular - index * spacing;
        if (offset < MIN_SIDE_CLEARANCE || offset > spacing - MIN_SIDE_CLEARANCE) {
            return null;
        }

        int direction = Integer.signum(villageAlong - anchorAlong);
        return new Branch(new Line(axis, index, offset, anchorAlong),
                          anchorAlong + direction * TURNOUT_REACH,
                          villageAlong - direction * STOP_SHORT,
                          villageX, villageZ);
    }

    private boolean touches(Branch branch, int blockX, int blockZ, int radius) {
        boolean alongX = branch.line().axis() == Line.Axis.X;
        int along = alongX ? blockX : blockZ;
        int perpendicular = alongX ? blockZ : blockX;
        int linePerp = network.perpendicularAt(branch.line(), along);

        return Math.abs(linePerp - perpendicular) <= radius
            && along + radius >= Math.min(branch.fromAlong(), branch.toAlong())
            && along - radius <= Math.max(branch.fromAlong(), branch.toAlong());
    }
}
