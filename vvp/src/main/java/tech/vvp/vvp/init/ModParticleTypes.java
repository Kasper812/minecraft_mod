package tech.vvp.vvp.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import com.atsuishio.portlib.forge.DeferredRegister;
import com.atsuishio.portlib.forge.ForgeRegistries;
import com.atsuishio.portlib.forge.RegistryObject;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.client.particle.VvpMuzzleParticleOption;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, VVP.MOD_ID);

    public static final RegistryObject<ParticleType<VvpMuzzleParticleOption>> MUZZLE_SMOKE =
            PARTICLE_TYPES.register("muzzle_smoke", () -> create(VvpMuzzleParticleOption.CODEC, VvpMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<VvpMuzzleParticleOption>> MUZZLE_BLOOM =
            PARTICLE_TYPES.register("muzzle_bloom", () -> create(VvpMuzzleParticleOption.CODEC, VvpMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<VvpMuzzleParticleOption>> MUZZLE_FLASH =
            PARTICLE_TYPES.register("muzzle_flash", () -> create(VvpMuzzleParticleOption.CODEC, VvpMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<VvpMuzzleParticleOption>> MUZZLE_BANG =
            PARTICLE_TYPES.register("muzzle_bang", () -> create(VvpMuzzleParticleOption.CODEC, VvpMuzzleParticleOption.STREAM_CODEC));

    public static final RegistryObject<ParticleType<VvpMuzzleParticleOption>> MUZZLE_SPARK =
            PARTICLE_TYPES.register("muzzle_spark", () -> create(VvpMuzzleParticleOption.CODEC, VvpMuzzleParticleOption.STREAM_CODEC));

    private static <T extends ParticleOptions> ParticleType<T> create(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new ParticleType<>(true) {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }
}
