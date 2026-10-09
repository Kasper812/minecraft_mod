package com.atsuishio.portlib.util;

import net.minecraft.util.Mth;

public final class LegacyMth {
    private LegacyMth() {
    }

    public static float sin(float value) {
        return Mth.sin(value);
    }

    public static float sin(double value) {
        return Mth.sin(value);
    }

    public static float cos(float value) {
        return Mth.cos(value);
    }

    public static float cos(double value) {
        return Mth.cos(value);
    }
}
