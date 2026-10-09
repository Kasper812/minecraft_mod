package com.atsuishio.superbwarfare.entity.mixin

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.entity.LivingEntity

@Suppress("FunctionName")
interface BeastEntityKiller {
    fun `sbw$kill`()

    companion object {
        fun getInstance(entity: LivingEntity): BeastEntityKiller {
            return entity as BeastEntityKiller
        }
    }
}
