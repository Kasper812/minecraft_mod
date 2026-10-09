package com.atsuishio.portlib.render;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class BuiltinItemRendererRegistry {
    public static final BuiltinItemRendererRegistry INSTANCE = new BuiltinItemRendererRegistry();

    private final Map<Item, DynamicItemRenderer> renderers = new IdentityHashMap<>();
    private final List<Function<ItemStack, DynamicItemRenderer>> fallbacks = new CopyOnWriteArrayList<>();

    private BuiltinItemRendererRegistry() {
    }

    public void register(ItemLike item, DynamicItemRenderer renderer) {
        renderers.put(item.asItem(), renderer);
    }

    public void registerFallback(Function<ItemStack, @Nullable DynamicItemRenderer> fallback) {
        fallbacks.add(fallback);
    }

    public @Nullable DynamicItemRenderer get(ItemLike item) {
        return renderers.get(item.asItem());
    }

    public @Nullable DynamicItemRenderer get(ItemStack stack) {
        DynamicItemRenderer renderer = renderers.get(stack.getItem());
        if (renderer != null) {
            return renderer;
        }
        for (Function<ItemStack, DynamicItemRenderer> fallback : fallbacks) {
            renderer = fallback.apply(stack);
            if (renderer != null) {
                return renderer;
            }
        }
        return null;
    }
}
