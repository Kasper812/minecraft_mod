package com.atsuishio.superbwarfare.data.gun.value.base

import com.atsuishio.superbwarfare.port.*

interface TagValue<T> {
    val defaultValue: T

    fun get(): T
    fun set(value: T)

    fun reset() {
        set(defaultValue)
    }
}
