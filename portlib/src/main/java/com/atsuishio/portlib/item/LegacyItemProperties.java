package com.atsuishio.portlib.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

public final class LegacyItemProperties {
    private LegacyItemProperties() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static <T> Item.Properties removeComponent(Item.Properties properties, DataComponentType<T> type) {
        return properties.component(type, null);
    }
}
