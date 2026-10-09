package com.atsuishio.superbwarfare.tiers

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod.Companion.loc
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.TagKey
import net.minecraft.world.item.equipment.ArmorMaterial
import net.minecraft.world.item.equipment.ArmorType
import net.minecraft.world.item.equipment.EquipmentAssets
import java.util.EnumMap

object ModArmorMaterials {
    private fun defense(boots: Int, leggings: Int, chest: Int, helmet: Int, body: Int): Map<ArmorType, Int> =
        EnumMap<ArmorType, Int>(ArmorType::class.java).apply {
            put(ArmorType.BOOTS, boots)
            put(ArmorType.LEGGINGS, leggings)
            put(ArmorType.CHESTPLATE, chest)
            put(ArmorType.HELMET, helmet)
            put(ArmorType.BODY, body)
        }

    @JvmField
    val CEMENTED_CARBIDE: ArmorMaterial = ArmorMaterial(
        50,
        defense(3, 6, 8, 3, 11),
        10,
        SoundEvents.ARMOR_EQUIP_IRON,
        4f,
        0.05f,
        TagKey.create(Registries.ITEM, loc("cemented_carbide_tool_materials")),
        ResourceKey.create(EquipmentAssets.ROOT_ID, loc("cemented_carbide"))
    )

    @JvmField
    val STEEL: ArmorMaterial = ArmorMaterial(
        35,
        defense(2, 5, 7, 2, 9),
        9,
        SoundEvents.ARMOR_EQUIP_IRON,
        1f,
        0f,
        TagKey.create(Registries.ITEM, loc("steel_tool_materials")),
        ResourceKey.create(EquipmentAssets.ROOT_ID, loc("steel"))
    )

    @JvmStatic
    fun register() {
    }
}
