package tech.vvp.vvp.client.model;

import tech.vvp.vvp.entity.vehicle.Btr4Entity;

public class Btr4Model extends VvpVehicleModel<Btr4Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
