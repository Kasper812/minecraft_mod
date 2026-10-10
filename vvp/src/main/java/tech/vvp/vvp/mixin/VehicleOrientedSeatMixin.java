package tech.vvp.vvp.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.vvp.vvp.entity.vehicle.util.OrientedBenchSeats;

@Mixin(VehicleEntity.class)
public class VehicleOrientedSeatMixin {

    @Inject(
            method = "copyEntityData(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void vvp$orientedSeatBody(Entity entity, CallbackInfo ci) {
        OrientedBenchSeats.afterCopyEntityData((VehicleEntity) (Object) this, entity);
    }
}
