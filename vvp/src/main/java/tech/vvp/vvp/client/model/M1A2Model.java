package tech.vvp.vvp.client.model;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.entity.vehicle.M1A2Entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M1A2Model extends VvpVehicleModel<M1A2Entity> {

    private static final Pattern ROLLER_PATTERN = Pattern.compile("^roller(?<side>[LR])(?<id>\\d+)$");

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<M1A2Entity> collectTransform(String boneName) {
        Matcher rollerMatcher = ROLLER_PATTERN.matcher(boneName);
        if (rollerMatcher.matches()) {
            boolean left = "L".equals(rollerMatcher.group("side"));
            return (bone, vehicle, state) -> {
                float rot = Mth.lerp(
                        state.getPartialTick(),
                        left ? vehicle.getLeftWheelRotO() : vehicle.getRightWheelRotO(),
                        left ? vehicle.getLeftWheelRot() : vehicle.getRightWheelRot()
                );
                bone.setRotX(1.5f * rot);
            };
        }

        if ("TopWheel1".equals(boneName) || "TopWheel2".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float track = Mth.lerp(state.getPartialTick(), vehicle.getLeftTrackO(), vehicle.getLeftTrack());
                bone.setPosZ(-track * 0.04f);
            };
        }

        if ("TrackL".equals(boneName) || "trackTemplateL".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float track = Mth.lerp(state.getPartialTick(), vehicle.getLeftTrackO(), vehicle.getLeftTrack());
                bone.setPosZ(-track * 0.04f);
            };
        }

        if ("TrackR".equals(boneName) || "trackTemplateR".equals(boneName)) {
            return (bone, vehicle, state) -> {
                float track = Mth.lerp(state.getPartialTick(), vehicle.getRightTrackO(), vehicle.getRightTrack());
                bone.setPosZ(-track * 0.04f);
            };
        }

        return super.collectTransform(boneName);
    }
}
