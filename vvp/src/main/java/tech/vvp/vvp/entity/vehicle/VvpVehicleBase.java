package tech.vvp.vvp.entity.vehicle;



import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;

import net.minecraft.world.entity.EntityType;

import net.minecraft.world.level.Level;

import tech.vvp.vvp.entity.vehicle.util.CompactEntityHitbox;

import tech.vvp.vvp.entity.vehicle.util.ICompactEntityHitbox;



/**

 * Base for all VVP vehicles.

 * Low vanilla AABB for lock-on / F3+B; green OBB handles combat hits; visual height is HUD-only.

 */

public abstract class VvpVehicleBase extends GeoVehicleEntity implements ICompactEntityHitbox {



    public VvpVehicleBase(EntityType<? extends GeoVehicleEntity> type, Level world) {

        super(type, world);

    }



    @Override

    public float getVisualBbHeight() {

        return CompactEntityHitbox.visualHeight(this.getType());

    }

}

