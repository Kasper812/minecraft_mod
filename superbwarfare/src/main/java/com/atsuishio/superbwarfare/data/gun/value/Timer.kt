package com.atsuishio.superbwarfare.data.gun.value

import com.atsuishio.superbwarfare.port.*

import net.minecraft.nbt.CompoundTag

/**
 * 针对一种状态的计时器
 */
open class Timer(private val tag: CompoundTag, name: String) {
    val name: String = name + "Time"

    open fun get(): Int {
        return tag.getIntOr(name, 0)
    }

    open fun set(time: Int) {
        if (time <= 0) {
            tag.remove(name)
        } else {
            tag.putInt(name, time)
        }
    }

    fun add(time: Int) {
        set(get() + time)
    }

    fun reduce() {
        add(-1)
    }

    fun reset() {
        set(0)
    }
}
