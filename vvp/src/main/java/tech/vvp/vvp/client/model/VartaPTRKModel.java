package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.VartaPTRKEntity;

public class VartaPTRKModel extends VvpVehicleModel<VartaPTRKEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
