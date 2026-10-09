package com.atsuishio.superbwarfare.data.gun.subdata

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.data.gun.GunData
import com.atsuishio.superbwarfare.init.ModPerks
import com.atsuishio.superbwarfare.item.misc.PerkItem
import com.atsuishio.superbwarfare.perk.Perk
import com.atsuishio.superbwarfare.perk.PerkInstance
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag

/**
 * Manages perk storage for a single [GunData] instance.
 *
 * Perk data is persisted in a [CompoundTag] partitioned by [Perk.Type].
 * Uses a static O(1) map lookup for resolving perks by name.
 *
 * @param gun the owning [GunData] instance.
 */
class Perks(private val gun: GunData) {

    private val rootTag: CompoundTag = gun.perk()

    /** Invalidation callback — clears the computed properties when perk state changes. */
    private val invalidateStructural: () -> Unit = gun::invalidateProperties

    companion object {
        /**
         * Flat map of perk name -> [Perk] instance, built once on class loading.
         * Replaces O(n) linear scans and list merge allocations during runtime tick calls.
         */
        @JvmStatic
        private val PERK_BY_NAME: Map<String, Perk> by lazy {
            val all = ModPerks.AMMO_PERKS + ModPerks.FUNC_PERKS + ModPerks.DAMAGE_PERKS

            buildMap(all.size) {
                for (perk in all) {
                    put(perk.name, perk)
                }
            }
        }
    }

    /**
     * Looks up a [Perk] by its registry name in O(1) time.
     *
     * @param name the perk name string.
     * @return the matching [Perk], or `null` if not found.
     */
    private fun findPerkByName(name: String): Perk? = PERK_BY_NAME[name]

    /**
     * Returns the [ListTag] for [type], creating it if absent.
     *
     * @param type the perk type category.
     * @return existing or new [ListTag].
     */
    fun getOrCreateList(type: Perk.Type): ListTag {
        val typeName = type.typeName
        return if (rootTag.contains(typeName)) {
            rootTag.getListOrEmpty(typeName)
        } else {
            val tag = rootTag.getCompoundOrEmpty(typeName)
            ListTag().also { rootTag.put(typeName, tag) }
        }
    }

    /**
     * Checks if [perk] is currently applied on the gun.
     *
     * @param perk the perk to check.
     * @return `true` if active.
     */
    fun has(perk: Perk): Boolean {
        val list = rootTag.getListOrEmpty(perk.type.typeName)
        return list.any { (it as CompoundTag).getStringOr("Name", "") == perk.name }
    }

    /**
     * Checks if any perk of [type] is applied.
     *
     * @param type the perk category.
     * @return `true` if any perk of this type exists.
     */
    fun has(type: Perk.Type): Boolean {
        val list = rootTag.getListOrEmpty(type.typeName)
        return !list.isEmpty()
    }

    /**
     * Gets the level of [perk], returning 0 if not present.
     *
     * @param perk the target perk.
     * @return the perk level.
     */
    fun getLevel(perk: Perk): Short {
        val name = perk.type.typeName

        // TODO 自动转换旧版tag
        if (rootTag.contains(name) && rootTag.getCompoundOrEmpty(name)
                .getStringOr("Name", "") == perk.name
        ) {
            return rootTag.getCompoundOrEmpty(name).getShortOr("Level", 0.toShort())
        }

        if (rootTag.contains(name)) {
            val list = rootTag.getListOrEmpty(name)
            val entry = list.firstOrNull { (it as CompoundTag).getStringOr("Name", "") == perk.name } as? CompoundTag
            return entry?.getShortOr("Level", 0.toShort()) ?: 0
        }

        return 0
    }

    fun getLevel(item: PerkItem): Short = getLevel(item.perk)

    /**
     * Returns all active [PerkInstance]s for [type].
     *
     * @param type the perk category.
     * @return list of active perk instances.
     */
    fun getInstances(type: Perk.Type): List<PerkInstance> {
        val typeName = type.typeName
        val instances = mutableListOf<PerkInstance>()

        if (rootTag.contains(typeName)) {
            val list = rootTag.getListOrEmpty(typeName)
            for (i in list.indices) {
                val tag = list.getCompoundOrEmpty(i)
                val perk = findPerkByName(tag.getStringOr("Name", "")) ?: continue
                instances.add(PerkInstance(perk, tag.getShortOr("Level", 0.toShort())))
            }
        } else {
            val tag = rootTag.getCompoundOrEmpty(typeName)
            val perk = findPerkByName(tag.getStringOr("Name", "")) ?: return instances
            instances.add(PerkInstance(perk, tag.getShortOr("Level", 0.toShort())))
        }

        return instances
    }

    /**
     * Sets or updates [perk] at [level] and marks gun PMC as dirty.
     *
     * @param perk the perk to apply.
     * @param level the perk level.
     */
    fun set(perk: Perk, level: Short) {
        val list = getOrCreateList(perk.type)
        val existing = list.firstOrNull {
            (it as CompoundTag).getStringOr("Name", "") == perk.name
        } as? CompoundTag

        if (existing != null) {
            existing.putShort("Level", level)
        } else {
            list.add(CompoundTag().apply {
                putString("Name", perk.name)
                putShort("Level", level)
            })
        }
        rootTag.put(perk.type.typeName, list)
        gun.invalidateProperties()
    }

    fun set(instance: PerkInstance) = set(instance.perk, instance.level)

    /**
     * Removes [perk] from the weapon and invalidates structural version.
     *
     * @param perk the perk to remove.
     */
    fun remove(perk: Perk) {
        val typeName = perk.type.typeName
        if (!rootTag.contains(typeName)) return

        val list = rootTag.getListOrEmpty(typeName)
        val removed = list.removeIf { (it as CompoundTag).getStringOr("Name", "") == perk.name }

        if (removed) {
            if (list.isEmpty()) rootTag.remove(typeName)
            gun.invalidateProperties()
        }
    }

    /**
     * Removes all perks of [type].
     *
     * @param type perk category to remove.
     */
    fun removeAll(type: Perk.Type) {
        if (rootTag.contains(type.typeName)) {
            rootTag.remove(type.typeName)
            gun.invalidateProperties()
        }
    }

    /**
     * Decrements an integer cooldown stored inside a perk's compound entry.
     * Removes the key when the value reaches zero.
     *
     * This method increments [NbtVersion.structural] on the owning [GunData]
     * because several perks (e.g. KillClip, OneTwoPunch) read their cooldown
     * counters inside [Perk.modifyProperty], meaning a cooldown change affects
     * computed gun properties.
     *
     * @param perk        the owning perk.
     * @param cooldownKey the NBT key of the cooldown counter.
     */
    fun reduceCooldown(perk: Perk, cooldownKey: String) {
        val list = rootTag.getListOrEmpty(perk.type.typeName)
        val entry = list.firstOrNull {
            (it as CompoundTag).getStringOr("Name", "") == perk.name
        } as? CompoundTag ?: return

        if (!entry.contains(cooldownKey)) return

        val next = entry.getIntOr(cooldownKey, 0) - 1
        if (next <= 0) entry.remove(cooldownKey) else entry.putInt(cooldownKey, next)

        // Perk cooldowns affect modifyProperty() output — invalidate PMC.
        invalidateStructural()
    }

    /**
     * Sets (or creates) an integer value inside a perk's compound entry.
     *
     * When used for cooldown counters that are read by [Perk.modifyProperty],
     * this method ensures the structural version is incremented so that the
     * PMC is rebuilt with the updated predicate ({@code value > 0}).
     *
     * @param perk  the owning perk.
     * @param key   the NBT key to write.
     * @param value the integer value.
     */
    fun putStructuralInt(perk: Perk, key: String, value: Int) {
        val tag = getTag(perk) ?: return
        val old = if (tag.contains(key)) tag.getIntOr(key, 0) else 0
        tag.putInt(key, value)

        // Invalidate ONLY on status transition (0 -> Active or Active -> 0)
        if ((old <= 0) != (value <= 0)) {
            gun.invalidateProperties()
        }
    }

    fun getTag(perk: Perk): CompoundTag? =
        getOrCreateList(perk.type)
            .filterIsInstance<CompoundTag>()
            .firstOrNull { perk.name == it.getStringOr("Name", "") }

    fun getOrCreateTag(perk: Perk): CompoundTag {
        val type = perk.type
        if (!rootTag.contains(type.typeName)) {
            rootTag.put(type.typeName, CompoundTag())
        }
        return rootTag.getCompoundOrEmpty(type.typeName)
    }

    fun get(perk: Perk): Perk? = get(perk.type)

    fun get(type: Perk.Type): Perk? {
        val typeName = type.typeName
        return if (rootTag.contains(typeName)) {
            val list = rootTag.getListOrEmpty(typeName)
            if (list.isEmpty()) null
            else findPerkByName(list.getCompoundOrEmpty(0).getStringOr("Name", ""))
        } else {
            findPerkByName(rootTag.getCompoundOrEmpty(typeName).getStringOr("Name", ""))
        }
    }
}
