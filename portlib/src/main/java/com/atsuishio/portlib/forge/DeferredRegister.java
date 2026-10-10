package com.atsuishio.portlib.forge;

import com.atsuishio.portlib.registry.RegistrationContext;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class DeferredRegister<T> {
    private final Registry<T> registry;
    private final String modId;
    private final List<RegistryObject<T>> entries = new ArrayList<>();

    private DeferredRegister(Registry<T> registry, String modId) {
        this.registry = registry;
        this.modId = modId;
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modId) {
        return new DeferredRegister<>(registry, modId);
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modId) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key.identifier());
        if (registry == null) {
            throw new IllegalArgumentException("Unknown registry " + key.identifier());
        }
        return new DeferredRegister<>(registry, modId);
    }

    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        Identifier id = Identifier.fromNamespaceAndPath(modId, name);
        ResourceKey<T> key = ResourceKey.create(registry.key(), id);
        I value;
        if ((Object) registry.key() == Registries.ITEM) {
            value = RegistrationContext.item(id, supplier::get);
        } else if ((Object) registry.key() == Registries.BLOCK) {
            value = RegistrationContext.block(id, supplier::get);
        } else {
            value = supplier.get();
        }
        Registry.register(registry, key, value);
        RegistryObject<T> object = RegistryObject.of(registry, key, value);
        entries.add(object);
        return (RegistryObject<I>) object;
    }

    public void register(Object bus) {
    }

    public Collection<RegistryObject<T>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public Registry<T> registry() {
        return registry;
    }

    public String getNamespace() {
        return modId;
    }
}
