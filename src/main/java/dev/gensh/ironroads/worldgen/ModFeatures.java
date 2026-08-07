package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.IronRoads;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, IronRoads.MOD_ID);

    public static final DeferredHolder<Feature<?>, RailwayFeature> RAILWAY =
            FEATURES.register("railway",
                    () -> new RailwayFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }

    private ModFeatures() {}
}
