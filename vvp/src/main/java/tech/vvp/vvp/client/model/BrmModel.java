package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.BrmEntity;

public class BrmModel extends VvpVehicleModel<BrmEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
