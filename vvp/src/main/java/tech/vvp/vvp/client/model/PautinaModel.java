package tech.vvp.vvp.client.model;


import tech.vvp.vvp.entity.vehicle.PautinaEntity;

public class PautinaModel extends VvpVehicleModel<PautinaEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

}
