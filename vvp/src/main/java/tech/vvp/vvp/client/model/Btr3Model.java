package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.Btr3Entity;

public class Btr3Model extends VvpVehicleModel<Btr3Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
