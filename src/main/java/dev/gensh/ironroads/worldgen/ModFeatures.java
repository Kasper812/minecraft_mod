package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.IronRoads;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, IronRoads.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> RAILWAY =
            FEATURES.register("railway",
                    () -> new RailwayFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }

    private ModFeatures() {}
}
