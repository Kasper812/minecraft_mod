package Aru.Aru.ashvehicle.mixin;

import Aru.Aru.ashvehicle.client.renderer.ThermalShaderManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRenderThermalMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void ashvehicle$thermalFullBright(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (ThermalShaderManager.isEnabled() && entity instanceof LivingEntity) {
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }
    }
}
