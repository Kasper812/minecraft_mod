package com.atsuishio.superbwarfare.data.gun

import com.atsuishio.superbwarfare.port.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReloadType {
    @SerialName("Magazine")
    MAGAZINE,

    @SerialName("Clip")
    CLIP,

    @SerialName("Iterative")
    ITERATIVE,
}
