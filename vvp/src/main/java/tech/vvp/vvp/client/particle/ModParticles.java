package tech.vvp.vvp.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.init.ModParticleTypes;

public class ModParticles {
    public static void registerProviders() {
        ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_SMOKE.get(), VvpMuzzleParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_BLOOM.get(), VvpMuzzleParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_FLASH.get(), VvpMuzzleParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_BANG.get(), VvpMuzzleParticle.Provider::new);
        ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_SPARK.get(), VvpMuzzleParticle.Provider::new);
    }
}
