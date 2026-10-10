package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import com.atsuishio.portlib.forge.DeferredRegister;
import com.atsuishio.portlib.forge.ForgeRegistries;
import com.atsuishio.portlib.forge.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, AshVehicle.MODID);

    public static final RegistryObject<SimpleParticleType> AFTERBURNER_FLAME =
            PARTICLES.register("afterburner_flame", () -> net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple(true));

    public static void register(Object bus) {
        PARTICLES.register(bus);
    }
}
