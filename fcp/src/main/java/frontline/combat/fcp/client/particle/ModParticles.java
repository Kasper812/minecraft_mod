package frontline.combat.fcp.client.particle;

import frontline.combat.fcp.FCP;
import frontline.combat.fcp.init.ModParticleTypes;
import net.fabricmc.api.EnvType;

public class ModParticles {
    public static void registerProviders() {
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_SMOKE.get(), FCPMuzzleParticle.Provider::new);
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_BLOOM.get(), FCPMuzzleParticle.Provider::new);
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_FLASH.get(), FCPMuzzleParticle.Provider::new);
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_BANG.get(), FCPMuzzleParticle.Provider::new);
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(ModParticleTypes.MUZZLE_SPARK.get(), FCPMuzzleParticle.Provider::new);
    }
}
