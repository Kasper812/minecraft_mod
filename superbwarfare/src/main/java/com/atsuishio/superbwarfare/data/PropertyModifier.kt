package com.atsuishio.superbwarfare.data

import com.atsuishio.superbwarfare.port.*

// TODO 替换旧版modifier
// 新版kt PropertyModifier
fun interface PropertyModifier<DATA : DefaultDataSupplier<DEFAULT_DATA>, DEFAULT_DATA> {
    fun modifyProperty(modifier: PMC<DATA, DEFAULT_DATA>)
}
