package com.atsuishio.superbwarfare.data.gun

import com.atsuishio.superbwarfare.port.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SeekType {
    @SerialName("None")
    NONE,

    @SerialName("HoldFire")
    HOLD_FIRE,

    @SerialName("HoldZoom")
    HOLD_ZOOM,
}
