package tech.vvp.vvp.entity.projectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.world.level.storage.ValueInput;
import com.atsuishio.portlib.nbt.LegacyNbt;
import net.minecraft.world.level.storage.ValueOutput;
import com.atsuishio.superbwarfare.entity.projectile.Ru9m336MissileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import tech.vvp.vvp.entity.vehicle.PantsirS1Entity;

/**
 * Ракета 57Э6 для ЗРПК Панцирь-С1.
 * Наведение — стандартная логика SBW ({@link Ru9m336MissileEntity}).
 */
public class PantsirMissileEntity extends Ru9m336MissileEntity {

    private static final EntityDataAccessor<Integer> LAUNCHER_ID = SynchedEntityData.defineId(PantsirMissileEntity.class, EntityDataSerializers.INT);

    private int launcherEntityId = -1;

    public PantsirMissileEntity(EntityType<? extends PantsirMissileEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 1 && launcherEntityId == -1) {
            Entity owner = getOwner();
            if (owner != null && owner.getVehicle() instanceof PantsirS1Entity pantsir) {
                setLauncherId(pantsir.getId());
            }
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput compoundIo) {
        CompoundTag compound = LegacyNbt.tagOf(compoundIo);
        super.readAdditionalSaveData(compoundIo);
        setLauncherId(compound.getIntOr("LauncherId", -1));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compoundIo) {
        CompoundTag compound = LegacyNbt.tagOf(compoundIo);
        super.addAdditionalSaveData(compoundIo);
        compound.putInt("LauncherId", launcherEntityId);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAUNCHER_ID, -1);
    }

    private void setLauncherId(int id) {
        this.launcherEntityId = id;
        this.entityData.set(LAUNCHER_ID, id);
    }

    public int getLauncherId() {
        return this.level().isClientSide() ? this.entityData.get(LAUNCHER_ID) : launcherEntityId;
    }

    public void setInitialRotation(Vec3 direction) {
        double horizontalDist = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) (-Math.atan2(direction.x, direction.z) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(direction.y, horizontalDist) * 180.0 / Math.PI);

        setYRot(yaw);
        setXRot(pitch);
        yRotO = yaw;
        xRotO = pitch;
    }

    @Override
    public float getVolume() {
        return 0.5f;
    }
}
