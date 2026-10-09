package com.atsuishio.superbwarfare.network.dataslot

import com.atsuishio.superbwarfare.port.*

interface ContainerEnergyData {
    operator fun get(index: Int): Long

    operator fun set(index: Int, value: Long)

    fun getCount(): Int
}
