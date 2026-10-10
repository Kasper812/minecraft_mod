package tech.vvp.vvp.client.particle;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.minecraft.client.particle.Particle;

import net.minecraft.client.particle.ParticleProvider;

import com.atsuishio.portlib.render.LegacyParticleRenderType;

import net.minecraft.client.particle.SpriteSet;

import com.atsuishio.portlib.render.LegacyTextureSheetParticle;

import net.minecraft.util.Mth;

import net.fabricmc.api.EnvType;

import net.fabricmc.api.Environment;



@Environment(EnvType.CLIENT)

public class VvpMuzzleParticle extends LegacyTextureSheetParticle {



    /** MTS scale units → MC billboard size (MTS toScale 10 ≈ 2 blocks). */

    private static final float SIZE_UNIT = 0.2f;



    private final SpriteSet sprites;

    private final float fade;

    private final int animationSpeed;

    private final int frameCount;

    private final int layer;

    private final float startSize;

    private final float endSize;

    private final float targetR;

    private final float targetG;

    private final float targetB;

    private final boolean lingerSmoke;

    private final int movementDuration;

    private final double initialXd;

    private final double initialYd;

    private final double initialZd;

    private final int attachVehicleId;

    private final int attachSeatIndex;

    private double lastMuzzleX = Double.NaN;

    private double lastMuzzleY = Double.NaN;

    private double lastMuzzleZ = Double.NaN;

    protected VvpMuzzleParticle(

            ClientLevel level,

            double x,

            double y,

            double z,

            double vx,

            double vy,

            double vz,

            SpriteSet sprites,

            VvpMuzzleParticleOption options

    ) {

        super(level, x, y, z);

        this.sprites = sprites;

        this.fade = options.fade();

        this.animationSpeed = Math.max(1, options.animationSpeed());

        this.frameCount = Math.max(1, options.frameCount());

        this.layer = options.layer();

        this.lingerSmoke = options.lingerSmoke();

        this.movementDuration = options.movementDuration();

        this.targetR = options.red();

        this.targetG = options.green();

        this.targetB = options.blue();

        this.startSize = SIZE_UNIT * options.baseScale();

        this.endSize = SIZE_UNIT * options.targetScale();

        this.initialXd = vx;

        this.initialYd = vy;

        this.initialZd = vz;

        this.attachVehicleId = options.attachVehicleId();

        this.attachSeatIndex = options.attachSeatIndex();

        this.quadSize = this.startSize;

        this.lifetime = Math.max(1, options.life());

        this.hasPhysics = false;

        this.xd = vx;

        this.yd = vy;

        this.zd = vz;

        this.roll = layer == VvpMuzzleParticleOption.LAYER_SMOKE

                ? (float) (this.random.nextDouble() * Math.PI * 2.0)

                : 0f;



        switch (layer) {

            case VvpMuzzleParticleOption.LAYER_BLOOM -> {

                this.gravity = 0f;

                this.rCol = targetR;

                this.gCol = targetG;

                this.bCol = targetB;

                this.alpha = 0.95f;

                this.setSprite(sprites.get(0, 1));

            }

            case VvpMuzzleParticleOption.LAYER_BANG_STATIC -> {

                this.gravity = 0f;

                this.rCol = 1f;

                this.gCol = 1f;

                this.bCol = 1f;

                this.alpha = 1f;

                this.setSprite(sprites.get(this.random.nextInt(frameCount), frameCount));

            }

            case VvpMuzzleParticleOption.LAYER_BANG_SPARK -> {

                this.gravity = 0f;

                this.rCol = 1f;

                this.gCol = 0.95f;

                this.bCol = 0.8f;

                this.alpha = 1f;

                this.setSprite(sprites.get(this.random.nextInt(frameCount), frameCount));

            }

            case VvpMuzzleParticleOption.LAYER_SMOKE -> {

                this.gravity = 0f;

                this.rCol = targetR;

                this.gCol = targetG;

                this.bCol = targetB;

                this.alpha = lingerSmoke ? 0.24f : 0.18f;

                this.setSprite(sprites.get(this.random.nextInt(frameCount), frameCount));

            }

            default -> {

                this.gravity = 0f;

                this.alpha = 1f;

                this.setSprite(sprites.get(0, 1));

            }

        }

        this.setSize(this.quadSize, this.quadSize);

    }



    @Override

    public int getLightColor(float partialTick) {

        if (layer == VvpMuzzleParticleOption.LAYER_SMOKE) {

            return super.getLightColor(partialTick);

        }

        return 15728880;

    }



    @Override

    public LegacyParticleRenderType getRenderType() {

        return LegacyParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;

    }



    @Override

    public void tick() {

        this.xo = this.x;

        this.yo = this.y;

        this.zo = this.z;



        if (this.age++ >= this.lifetime) {

            this.remove();

            return;

        }

        applyBarrelMovement();

        if (layer == VvpMuzzleParticleOption.LAYER_SMOKE) {

            if (lingerSmoke) {

                this.xd *= 0.9;

                this.yd = this.yd * 0.9 + 0.004;

                this.zd *= 0.9;

            } else if (movementDuration > 0 && this.age <= movementDuration) {

                float factor = (movementDuration - this.age) / (float) movementDuration;

                clampVelocityToInitial(factor);

            }

        } else if (movementDuration > 0 && this.age <= movementDuration) {

            float factor = (movementDuration - this.age) / (float) movementDuration;

            clampVelocityToInitial(factor);

        } else if (layer == VvpMuzzleParticleOption.LAYER_BANG_SPARK) {

            this.xd *= 0.96;

            this.yd *= 0.96;

            this.zd *= 0.96;

        }



        this.move(this.xd, this.yd, this.zd);



        float progress = (float) this.age / (float) this.lifetime;

        this.quadSize = Mth.lerp(progress, this.startSize, this.endSize);

        this.setSize(this.quadSize, this.quadSize);



        if (frameCount > 1 && layer == VvpMuzzleParticleOption.LAYER_SMOKE) {

            int frame = ((this.age / animationSpeed) % frameCount + frameCount) % frameCount;

            this.setSprite(this.sprites.get(frame, frameCount));

        }



        if (layer == VvpMuzzleParticleOption.LAYER_BLOOM || layer == VvpMuzzleParticleOption.LAYER_BANG_STATIC || layer == VvpMuzzleParticleOption.LAYER_BANG_SPARK) {

            this.alpha *= fade;

        } else if (layer == VvpMuzzleParticleOption.LAYER_SMOKE) {

            if (this.age < 2) {

                this.alpha *= this.age / 2f;

            }

            float fadeStart = lingerSmoke ? 0.45f : 0.25f;

            if (progress > fadeStart) {

                float tail = (progress - fadeStart) / (1f - fadeStart);

                this.alpha *= 1f - tail * (lingerSmoke ? 0.04f : 0.025f);

            }

            this.alpha *= fade;

        }

    }



    private void applyBarrelMovement() {
        if (attachVehicleId < 0 || layer != VvpMuzzleParticleOption.LAYER_SMOKE) {
            return;
        }
        Entity entity = this.level.getEntity(attachVehicleId);
        if (!(entity instanceof VehicleEntity vehicle)) {
            return;
        }
        double[] last = {lastMuzzleX, lastMuzzleY, lastMuzzleZ};
        double[] pos = {this.x, this.y, this.z};
        BarrelMuzzleTracking.applyFollowDelta(vehicle, attachSeatIndex, last, pos, 0.4f);
        this.x = pos[0];
        this.y = pos[1];
        this.z = pos[2];
        lastMuzzleX = last[0];
        lastMuzzleY = last[1];
        lastMuzzleZ = last[2];
    }

    private void clampVelocityToInitial(float factor) {

        double maxX = Math.abs(initialXd * factor);

        double maxY = Math.abs(initialYd * factor);

        double maxZ = Math.abs(initialZd * factor);

        if (Math.abs(this.xd) > maxX) {

            this.xd = Math.signum(this.xd) * maxX;

        }

        if (Math.abs(this.yd) > maxY) {

            this.yd = Math.signum(this.yd) * maxY;

        }

        if (Math.abs(this.zd) > maxZ) {

            this.zd = Math.signum(this.zd) * maxZ;

        }

    }



    @Environment(EnvType.CLIENT)

    public static class Provider implements ParticleProvider<VvpMuzzleParticleOption> {

        private final SpriteSet sprites;



        public Provider(SpriteSet sprites) {

            this.sprites = sprites;

        }



        @Override

        public Particle createParticle(

                VvpMuzzleParticleOption type,

                ClientLevel level,

                double x,

                double y,

                double z,

                double xSpeed,

                double ySpeed,

                double zSpeed, net.minecraft.util.RandomSource randomSource) {

            return new VvpMuzzleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, type);

        }

    }

}


