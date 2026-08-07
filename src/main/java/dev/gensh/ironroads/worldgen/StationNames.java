package dev.gensh.ironroads.worldgen;

public final class StationNames {
    private static final String[] PREFIX = {
        "North", "South", "East", "West", "Upper", "Lower", "Old", "New",
    };

    private static final String[] ROOT = {
        "field", "ford", "bridge", "hollow", "ridge", "mill", "haven", "cross",
        "moor", "vale", "wood", "stone", "gate", "market", "bourne", "hill",
    };

    public static String of(int villageX, int villageZ) {
        long hash = mix(villageX * 0x9E3779B97F4A7C15L
                      ^ villageZ * 0xC2B2AE3D27D4EB4FL);

        String prefix = PREFIX[(int) Math.floorMod(hash, PREFIX.length)];
        String root = ROOT[(int) Math.floorMod(hash >>> 16, ROOT.length)];
        return prefix + root;
    }

    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private StationNames() {}
}
