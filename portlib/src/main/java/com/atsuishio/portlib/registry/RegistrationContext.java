package com.atsuishio.portlib.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public final class RegistrationContext {
    private static final ThreadLocal<Deque<ResourceKey<Item>>> ITEMS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<ResourceKey<Block>>> BLOCKS = ThreadLocal.withInitial(ArrayDeque::new);

    private RegistrationContext() {
    }

    public static <T> T item(Identifier id, Supplier<T> factory) {
        ITEMS.get().push(ResourceKey.create(Registries.ITEM, id));
        try {
            return factory.get();
        } finally {
            ITEMS.get().pop();
        }
    }

    public static <T> T block(Identifier id, Supplier<T> factory) {
        BLOCKS.get().push(ResourceKey.create(Registries.BLOCK, id));
        try {
            return factory.get();
        } finally {
            BLOCKS.get().pop();
        }
    }

    public static @Nullable ResourceKey<Item> currentItem() {
        return ITEMS.get().peek();
    }

    public static @Nullable ResourceKey<Block> currentBlock() {
        return BLOCKS.get().peek();
    }
}
