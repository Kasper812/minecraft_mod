package com.atsuishio.portlib.forge;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.function.Supplier;

public final class RegistryObject<T> implements Supplier<T> {
    private final ResourceKey<T> key;
    private final T value;
    private final Holder<T> holder;

    RegistryObject(ResourceKey<T> key, T value, Holder<T> holder) {
        this.key = key;
        this.value = value;
        this.holder = holder;
    }

    @Override
    public T get() {
        return value;
    }

    public Identifier getId() {
        return key.identifier();
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    public Holder<T> getHolder() {
        return holder;
    }

    public Holder<T> holder() {
        return holder;
    }

    public boolean isPresent() {
        return true;
    }

    public Optional<T> asOptional() {
        return Optional.of(value);
    }

    @SuppressWarnings("unchecked")
    static <T> RegistryObject<T> of(Registry<T> registry, ResourceKey<T> key, T value) {
        Holder<T> holder = registry.wrapAsHolder(value);
        return new RegistryObject<>(key, value, holder);
    }
}
