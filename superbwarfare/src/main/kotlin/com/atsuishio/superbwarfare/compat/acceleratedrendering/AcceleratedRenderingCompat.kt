package com.atsuishio.superbwarfare.compat.acceleratedrendering

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object AcceleratedRenderingCompat {
    fun init() {
    }

    val isLoaded: Boolean
        get() = false

    val shouldAccelerate: Boolean
        get() = false

    fun beginVanillaAcceleration() {
    }

    fun endVanillaAcceleration() {
    }
}
