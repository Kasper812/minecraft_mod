package tech.vvp.vvp.client.model;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.entity.vehicle.GazTigrEntity;

public class GazTigrModel extends VvpVehicleModel<GazTigrEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
