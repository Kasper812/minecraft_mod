package dev.gensh.ironroads.geometry;

public final class Junctions {
    public static final int REACH = BranchNetwork.TURNOUT_REACH;

    public static final int FOOTPRINT = REACH + 3;

    public static final int SIGNAL_SPACING = 192;

    public static final int SIGNAL_OFFSET = 3;

    public static boolean isCrossingAnchor(int spacing, int along) {
        int distance = Math.floorMod(along, spacing);
        return distance == REACH || distance == spacing - REACH;
    }

    public static boolean isSignalAt(int along) {
        return Math.floorMod(along, SIGNAL_SPACING) == SIGNAL_SPACING / 2;
    }

    public static int offsetFromNode(int spacing, int coordinate) {
        int distance = Math.floorMod(coordinate, spacing);
        return distance <= spacing / 2 ? distance : distance - spacing;
    }

    public static boolean insideFootprint(int spacing, int x, int z) {
        return Math.abs(offsetFromNode(spacing, x)) <= FOOTPRINT
            && Math.abs(offsetFromNode(spacing, z)) <= FOOTPRINT;
    }

    public static int nodeCoordinate(int spacing, int coordinate) {
        return coordinate - offsetFromNode(spacing, coordinate);
    }

    private Junctions() {}
}
