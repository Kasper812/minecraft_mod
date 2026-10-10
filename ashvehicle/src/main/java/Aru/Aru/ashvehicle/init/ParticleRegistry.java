package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.client.particle.AfterburnerFlameParticleProvider;
import net.fabricmc.api.EnvType;

public class ParticleRegistry {
    public static void registerParticleProviders() {
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(ModParticleTypes.AFTERBURNER_FLAME.get(), AfterburnerFlameParticleProvider::new);
    }
}
