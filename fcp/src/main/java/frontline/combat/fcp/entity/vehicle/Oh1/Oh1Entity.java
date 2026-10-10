package frontline.combat.fcp.entity.vehicle.Oh1;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.nbt.CompoundTag;
import com.atsuishio.portlib.nbt.LegacyNbt;
import net.minecraft.world.level.storage.ValueOutput;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import frontline.combat.fcp.entity.vehicle.CamoVehicleBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Oh1Entity extends CamoVehicleBase {

    public int INVENTORY_SIZE = 9;

    @Override
    public int inventorySize() {
        return INVENTORY_SIZE;
    }

    @Override public InventoryStyle inventoryStyle() { return InventoryStyle.GRID; }

    private static final EntityDataAccessor<Boolean> TOGGLE1 = SynchedEntityData.defineId(Oh1Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TOGGLE2 = SynchedEntityData.defineId(Oh1Entity.class, EntityDataSerializers.BOOLEAN);

    private boolean toggleInit = false;

    public Oh1Entity(EntityType<Oh1Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TOGGLE1, true);
        builder.define(TOGGLE2, true);
    }

    public boolean hasToggle1() {return this.entityData.get(TOGGLE1);}
    public void setToggle1(boolean v) {this.entityData.set(TOGGLE1, v);}
    public void toggleToggle1() {setToggle1(!hasToggle1());}

    public boolean hasToggle2() {return this.entityData.get(TOGGLE2);}
    public void setToggle2(boolean v) {this.entityData.set(TOGGLE2, v);}
    public void toggleToggle2() {setToggle2(!hasToggle2());}

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compoundIo) {
        CompoundTag compound = LegacyNbt.tagOf(compoundIo);
        super.addAdditionalSaveData(compoundIo);
        compound.putBoolean("Toggle1", hasToggle1());
        compound.putBoolean("Toggle2", hasToggle2());
        compound.putBoolean("ToggleInit", toggleInit);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compoundIo) {
        CompoundTag compound = LegacyNbt.tagOf(compoundIo);
        super.readAdditionalSaveData(compoundIo);
        if (compound.contains("ToggleInit")) toggleInit = compound.getBooleanOr("ToggleInit", false);
        if (compound.contains("Toggle1")) setToggle1(compound.getBooleanOr("Toggle1", false));
        if (compound.contains("Toggle2")) setToggle2(compound.getBooleanOr("Toggle2", false));
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // Randomise each pod independently, once on first spawn (like the Ural's tent /
        // the humvee attachments), then persist whatever was rolled (or later toggled).
        if (!this.level().isClientSide() && !toggleInit) {
            setToggle1(this.random.nextBoolean());
            setToggle2(this.random.nextBoolean());
            toggleInit = true;
        }
    }
}
