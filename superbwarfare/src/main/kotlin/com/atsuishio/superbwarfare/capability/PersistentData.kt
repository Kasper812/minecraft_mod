package com.atsuishio.superbwarfare.capability

import com.atsuishio.superbwarfare.port.*

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity

val Entity.persistentData: CompoundTag
    get() = (this as PersistentDataAccessor).`superbwarfare$getPersistentData`()
