package frontline.combat.fcp.init;

import com.mojang.serialization.Codec;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.client.particle.FCPMuzzleParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import com.atsuishio.portlib.forge.DeferredRegister;
import com.atsuishio.portlib.forge.ForgeRegistries;
import com.atsuishio.portlib.forge.RegistryObject;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, FCP.MODID);

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_SMOKE =
            PARTICLE_TYPES.register("muzzle_smoke", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_BLOOM =
            PARTICLE_TYPES.register("muzzle_bloom", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_FLASH =
            PARTICLE_TYPES.register("muzzle_flash", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_BANG =
            PARTICLE_TYPES.register("muzzle_bang", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<FCPMuzzleParticleOption>> MUZZLE_SPARK =
            PARTICLE_TYPES.register("muzzle_spark", () -> create(FCPMuzzleParticleOption.CODEC, FCPMuzzleParticleOption.STREAM_CODEC));

    private static <T extends ParticleOptions> ParticleType<T> create(com.mojang.serialization.MapCodec<T> codec, net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec) {
        return new ParticleType<>(true) {
            @Override
            public com.mojang.serialization.MapCodec<T> codec() {
                return codec;
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }
}
