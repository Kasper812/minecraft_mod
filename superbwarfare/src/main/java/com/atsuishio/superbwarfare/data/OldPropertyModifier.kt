package com.atsuishio.superbwarfare.data

import com.atsuishio.superbwarfare.port.*

interface OldPropertyModifier<DATA : DefaultDataSupplier<DEFAULT_DATA>, DEFAULT_DATA> {
    fun computeProperties(data: DATA, rawData: DEFAULT_DATA): DEFAULT_DATA {
        return rawData
    }
}
