package tech.vvp.vvp.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import tech.vvp.vvp.init.ModParticleTypes;

public record VvpMuzzleParticleOption(
        int color,
        int life,
        float fade,
        int animationSpeed,
        float baseScale,
        float targetScale,
        int frameCount,
        int layer,
        boolean lingerSmoke,
        int movementDuration,
        int attachVehicleId,
        int attachSeatIndex
) implements ParticleOptions {

    public static final int NO_ATTACH = -1;

    public static final int LAYER_SMOKE = 0;
    public static final int LAYER_BANG_STATIC = 1;
    public static final int LAYER_BANG_SPARK = 2;
    public static final int LAYER_BLOOM = 3;

    public VvpMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer) {
        this(r, g, b, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, false, 0);
    }

    public VvpMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer, boolean lingerSmoke) {
        this(r, g, b, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, lingerSmoke, 0);
    }

    public VvpMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer, boolean lingerSmoke, int movementDuration) {
        this(
                ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255),
                life,
                fade,
                animationSpeed,
                baseScale,
                targetScale,
                frameCount,
                layer,
                lingerSmoke,
                movementDuration,
                NO_ATTACH,
                0
        );
    }

    public boolean hasBarrelAttach() {
        return attachVehicleId >= 0 && layer == LAYER_SMOKE && lingerSmoke;
    }

    public VvpMuzzleParticleOption withBarrelAttach(int vehicleId, int seatIndex) {
        return new VvpMuzzleParticleOption(
                color, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer,
                lingerSmoke, movementDuration, vehicleId, seatIndex
        );
    }

    public float red() {
        return ((color >> 16) & 255) / 255f;
    }

    public float green() {
        return ((color >> 8) & 255) / 255f;
    }

    public float blue() {
        return (color & 255) / 255f;
    }

    @Override
    public ParticleType<VvpMuzzleParticleOption> getType() {
        return switch (layer) {
            case LAYER_BLOOM -> ModParticleTypes.MUZZLE_BLOOM.get();
            case LAYER_BANG_STATIC, LAYER_BANG_SPARK -> ModParticleTypes.MUZZLE_BANG.get();
            default -> ModParticleTypes.MUZZLE_SMOKE.get();
        };
    }

    public static final MapCodec<VvpMuzzleParticleOption> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("color").forGetter(VvpMuzzleParticleOption::color),
            Codec.INT.fieldOf("life").forGetter(VvpMuzzleParticleOption::life),
            Codec.FLOAT.fieldOf("fade").forGetter(VvpMuzzleParticleOption::fade),
            Codec.INT.fieldOf("animationSpeed").forGetter(VvpMuzzleParticleOption::animationSpeed),
            Codec.FLOAT.fieldOf("baseScale").forGetter(VvpMuzzleParticleOption::baseScale),
            Codec.FLOAT.fieldOf("targetScale").forGetter(VvpMuzzleParticleOption::targetScale),
            Codec.INT.fieldOf("frameCount").forGetter(VvpMuzzleParticleOption::frameCount),
            Codec.INT.fieldOf("layer").forGetter(VvpMuzzleParticleOption::layer),
            Codec.BOOL.fieldOf("lingerSmoke").forGetter(VvpMuzzleParticleOption::lingerSmoke),
            Codec.INT.optionalFieldOf("movementDuration", 0).forGetter(VvpMuzzleParticleOption::movementDuration),
            Codec.INT.optionalFieldOf("attachVehicleId", NO_ATTACH).forGetter(VvpMuzzleParticleOption::attachVehicleId),
            Codec.INT.optionalFieldOf("attachSeatIndex", 0).forGetter(VvpMuzzleParticleOption::attachSeatIndex)
    ).apply(builder, VvpMuzzleParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VvpMuzzleParticleOption> STREAM_CODEC = StreamCodec.of(
            (buffer, o) -> {
                buffer.writeInt(o.color);
                buffer.writeInt(o.life);
                buffer.writeFloat(o.fade);
                buffer.writeInt(o.animationSpeed);
                buffer.writeFloat(o.baseScale);
                buffer.writeFloat(o.targetScale);
                buffer.writeInt(o.frameCount);
                buffer.writeInt(o.layer);
                buffer.writeBoolean(o.lingerSmoke);
                buffer.writeVarInt(o.movementDuration);
                buffer.writeVarInt(o.attachVehicleId);
                buffer.writeVarInt(o.attachSeatIndex);
            },
            buffer -> new VvpMuzzleParticleOption(
                    buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readInt(),
                    buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readInt(), buffer.readBoolean(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()
            ));
}
