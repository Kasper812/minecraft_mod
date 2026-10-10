package tech.vvp.vvp.event;

import com.atsuishio.superbwarfare.api.event.ShootEvent;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import tech.vvp.vvp.VVP;
import tech.vvp.vvp.effects.VvpMuzzleEffects;

public final class MuzzleShootEventHandler {

    private MuzzleShootEventHandler() {
    }

    public static void onShootPost(ShootEvent.Post event) {
        ShootParameters parameters = event.getParameters();
        Entity ammoSupplier = parameters.ammoSupplier;

        if (!(ammoSupplier instanceof VehicleEntity vehicle) || !VvpMuzzleEffects.isVvpVehicle(vehicle)) {
            return;
        }

        VvpMuzzleEffects.spawnFromShoot(parameters);
    }
}
