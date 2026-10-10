package tech.vvp.vvp.mixin;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Prevents client crashes when a vehicle weapon references missing ammo/items
 * (e.g. ywzj_vehicle tanks removed from the modpack but still present in the world).
 */
@Mixin(VehicleEntity.class)
public class VehicleGunDataSafetyMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("vvp");

    @Redirect(
            method = "getGunDataMap",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/data/gun/GunData$Companion;from(Lnet/minecraft/world/item/ItemStack;)Lcom/atsuishio/superbwarfare/data/gun/GunData;"
            ),
            remap = false
    )
    private GunData vvp$safeGunDataFrom(
            GunData.Companion companion,
            ItemStack stack
    ) {
        try {
            return companion.from(stack);
        } catch (Throwable error) {
            LOGGER.warn("Skipping broken vehicle weapon (missing ammo or invalid gun data): {}", error.toString());
            return null;
        }
    }
}
