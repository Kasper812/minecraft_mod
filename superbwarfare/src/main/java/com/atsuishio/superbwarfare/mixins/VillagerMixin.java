package com.atsuishio.superbwarfare.mixins;

import com.atsuishio.superbwarfare.entity.mixin.CupidLove;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerMixin implements CupidLove {

    @Unique
    public boolean superbwarfare$cupidLove;

    @Override
    public void superbwarfare$setCupidLove(boolean love) {
        this.superbwarfare$cupidLove = love;
    }

    @Override
    public boolean superbwarfare$getCupidLove() {
        return this.superbwarfare$cupidLove;
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("RETURN"))
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        output.putBoolean("SuperbwarfareCupidLove", this.superbwarfare$cupidLove);
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("RETURN"))
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        this.superbwarfare$cupidLove = input.getBooleanOr("SuperbwarfareCupidLove", false);
    }

    @Inject(method = "canBreed()Z", at = @At("HEAD"), cancellable = true)
    public void canBreed(CallbackInfoReturnable<Boolean> cir) {
        if (this.superbwarfare$cupidLove && ((Villager) (Object) this).getAge() == 0) {
            cir.setReturnValue(true);
        }
    }
}
