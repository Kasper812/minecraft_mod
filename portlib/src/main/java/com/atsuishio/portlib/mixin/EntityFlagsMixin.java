package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.entity.LegacyEntityFlags;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityFlagsMixin implements LegacyEntityFlags {
    @Unique
    private boolean sbw_portlib$noCulling;

    @Override
    public boolean sbw_portlib$isNoCulling() {
        return this.sbw_portlib$noCulling;
    }

    @Override
    public void sbw_portlib$setNoCulling(boolean value) {
        this.sbw_portlib$noCulling = value;
    }
}
