package com.atsuishio.superbwarfare.perk.js

import com.atsuishio.superbwarfare.port.*

import net.minecraft.nbt.CompoundTag

class PerkTagProxy(private val tag: CompoundTag) {

    fun getInt(key: String): Int = tag.getIntOr(key, 0)

    fun putInt(key: String, value: Int) = tag.putInt(key, value)

    fun getBoolean(key: String): Boolean = tag.getBooleanOr(key, false)

    fun putBoolean(key: String, value: Boolean) = tag.putBoolean(key, value)

    fun getShort(key: String): Short = tag.getShortOr(key, 0.toShort())

    fun putShort(key: String, value: Short) = tag.putShort(key, value)

    fun getDouble(key: String): Double = tag.getDoubleOr(key, 0.0)

    fun putDouble(key: String, value: Double) = tag.putDouble(key, value)

    fun getString(key: String): String = tag.getStringOr(key, "")

    fun putString(key: String, value: String) = tag.putString(key, value)

    fun has(key: String): Boolean = tag.contains(key)

    fun remove(key: String) = tag.remove(key)

    fun isEmpty(): Boolean = tag.isEmpty

    fun reduceCooldown(key: String) {
        if (!tag.contains(key)) return
        val next = tag.getIntOr(key, 0) - 1
        if (next <= 0) {
            tag.remove(key)
        } else {
            tag.putInt(key, next)
        }
    }
}
