package com.atsuishio.superbwarfare.compat.realcamera

object RealCameraCompatHolder {
    @JvmStatic
    fun hasMod(): Boolean = false

    @JvmStatic
    fun getCompatMoveX(moveX: Float): Float = moveX

    @JvmStatic
    fun getCompatMoveY(moveY: Float): Float = moveY
}
