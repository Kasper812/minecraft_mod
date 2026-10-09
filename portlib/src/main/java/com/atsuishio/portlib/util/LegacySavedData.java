package com.atsuishio.portlib.util;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.function.Supplier;

public final class LegacySavedData {
    private LegacySavedData() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static <T extends SavedData> SavedDataType<T> type(Identifier id, Supplier<T> constructor, Codec<T> codec) {
        return new SavedDataType<>(id, constructor, codec, null);
    }
}
