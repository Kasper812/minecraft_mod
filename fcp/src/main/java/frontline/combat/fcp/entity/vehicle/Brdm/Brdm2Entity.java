package frontline.combat.fcp.entity.vehicle.Brdm;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.nbt.CompoundTag;
import com.atsuishio.portlib.nbt.LegacyNbt;
import net.minecraft.world.level.storage.ValueOutput;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Brdm2Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Float> STEERING_ANGLE = SynchedEntityData.defineId(Brdm2Entity.class, EntityDataSerializers.FLOAT);
    private float prevSteeringAngle = 0f;
    private float wheelRotation = 0f;
    private float prevWheelRotation = 0f;
    public Brdm2Entity(EntityType<Brdm2Entity> type, Level world) {super(type, world);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {super.defineSynchedData(builder); builder.define(STEERING_ANGLE, 0f);}
    public float getSteeringAngle() {return this.entityData.get(STEERING_ANGLE);}
    public void setSteeringAngle(float angle) {this.entityData.set(STEERING_ANGLE, angle);}
    public float getPrevSteeringAngle(){return prevSteeringAngle;}
    public float getWheelRotation(){return wheelRotation;}
    public float getPrevWheelRotation(){return prevWheelRotation;}
    @Override public DamageModifier getDamageModifier() {return super.getDamageModifier().custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);}
    @Override public void addAdditionalSaveData(ValueOutput cIo) {
        CompoundTag c = LegacyNbt.tagOf(cIo);super.addAdditionalSaveData(cIo); c.putFloat("SteeringAngle", getSteeringAngle());}
    @Override public void readAdditionalSaveData(ValueInput cIo) {
        CompoundTag c = LegacyNbt.tagOf(cIo);super.readAdditionalSaveData(cIo); if (c.contains("SteeringAngle")) setSteeringAngle(c.getFloatOr("SteeringAngle", 0.0F));}
    @Override public void baseTick() {
        super.baseTick();
        prevSteeringAngle = getSteeringAngle();
        float a = getSteeringAngle();
        double speed = Math.sqrt(this.getDeltaMovement().x*this.getDeltaMovement().x + this.getDeltaMovement().z*this.getDeltaMovement().z);
        boolean moving = speed > 0.05;
        if (this.leftInputDown() && !this.rightInputDown()) {a=Math.min(45f,a+2.0f); setSteeringAngle(a);}
        else if (this.rightInputDown() && !this.leftInputDown()) {a=Math.max(-45f,a-2.0f); setSteeringAngle(a);}
        else if (moving && Math.abs(a)>0.5f) {a*=0.9f; setSteeringAngle(a);}
        if (moving && Math.abs(a)>1f) this.setYRot(this.getYRot() + a*0.009f*(float)speed);
        prevWheelRotation = wheelRotation;
        double fwd = this.getDeltaMovement().x*Math.sin(Math.toRadians(this.getYRot())) + this.getDeltaMovement().z*Math.cos(Math.toRadians(this.getYRot()));
        if (fwd>0) wheelRotation += (float)(speed*20f); else if (fwd<0) wheelRotation -= (float)(speed*20f);
    }
}
