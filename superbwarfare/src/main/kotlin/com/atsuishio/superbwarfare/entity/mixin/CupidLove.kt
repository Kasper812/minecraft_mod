package com.atsuishio.superbwarfare.entity.mixin

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.entity.npc.villager.Villager
@Suppress("FunctionName")
interface CupidLove {
    fun `superbwarfare$setCupidLove`(love: Boolean)

    fun `superbwarfare$getCupidLove`(): Boolean

    companion object {
        fun getInstance(villager: Villager): CupidLove {
            return villager as CupidLove
        }
    }
}
