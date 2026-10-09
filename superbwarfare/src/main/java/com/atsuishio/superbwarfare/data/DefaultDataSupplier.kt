package com.atsuishio.superbwarfare.data

import com.atsuishio.superbwarfare.port.*

interface DefaultDataSupplier<T> {
    fun getDefault(): T
}
