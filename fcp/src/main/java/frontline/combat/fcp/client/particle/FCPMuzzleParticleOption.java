package frontline.combat.fcp.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frontline.combat.fcp.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import com.atsuishio.portlib.forge.ForgeRegistries;

public record FCPMuzzleParticleOption (
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

    public FCPMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer) {
        this(r, g, b, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, false, 0);
    }

    public FCPMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer, boolean lingerSmoke) {
        this(r, g, b, life, fade, animationSpeed, baseScale, targetScale, frameCount, layer, lingerSmoke, 0);
    }

    public FCPMuzzleParticleOption(float r, float g, float b, int life, float fade, int animationSpeed, float baseScale, float targetScale, int frameCount, int layer, boolean lingerSmoke, int movementDuration) {
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

    public FCPMuzzleParticleOption withBarrelAttach(int vehicleId, int seatIndex) {
        return new FCPMuzzleParticleOption(
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
    public ParticleType<FCPMuzzleParticleOption> getType() {
        return switch (layer) {
            case LAYER_BLOOM -> ModParticleTypes.MUZZLE_BLOOM.get();
            case LAYER_BANG_STATIC, LAYER_BANG_SPARK -> ModParticleTypes.MUZZLE_BANG.get();
            default -> ModParticleTypes.MUZZLE_SMOKE.get();
        };
    }

    public static final com.mojang.serialization.MapCodec<FCPMuzzleParticleOption> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("color").forGetter(FCPMuzzleParticleOption::color),
            Codec.INT.fieldOf("life").forGetter(FCPMuzzleParticleOption::life),
            Codec.FLOAT.fieldOf("fade").forGetter(FCPMuzzleParticleOption::fade),
            Codec.INT.fieldOf("animationSpeed").forGetter(FCPMuzzleParticleOption::animationSpeed),
            Codec.FLOAT.fieldOf("baseScale").forGetter(FCPMuzzleParticleOption::baseScale),
            Codec.FLOAT.fieldOf("targetScale").forGetter(FCPMuzzleParticleOption::targetScale),
            Codec.INT.fieldOf("frameCount").forGetter(FCPMuzzleParticleOption::frameCount),
            Codec.INT.fieldOf("layer").forGetter(FCPMuzzleParticleOption::layer),
            Codec.BOOL.fieldOf("lingerSmoke").forGetter(FCPMuzzleParticleOption::lingerSmoke),
            Codec.INT.optionalFieldOf("movementDuration", 0).forGetter(FCPMuzzleParticleOption::movementDuration),
            Codec.INT.optionalFieldOf("attachVehicleId", NO_ATTACH).forGetter(FCPMuzzleParticleOption::attachVehicleId),
            Codec.INT.optionalFieldOf("attachSeatIndex", 0).forGetter(FCPMuzzleParticleOption::attachSeatIndex)
    ).apply(builder, FCPMuzzleParticleOption::new));

    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, FCPMuzzleParticleOption> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
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
            buffer -> new FCPMuzzleParticleOption(
                    buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readInt(),
                    buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readInt(), buffer.readBoolean(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()
            )
    );
}
