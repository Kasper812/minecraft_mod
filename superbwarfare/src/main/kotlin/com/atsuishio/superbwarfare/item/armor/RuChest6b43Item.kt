package com.atsuishio.superbwarfare.item.armor

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod
import com.atsuishio.superbwarfare.Mod.Companion.loc
import com.atsuishio.superbwarfare.init.ModAttributes
import com.atsuishio.superbwarfare.init.ModItems
import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.resource.model.ArmorModelReloadListener
import com.atsuishio.superbwarfare.tiers.ModArmorMaterials
import com.github.mcmodderanchor.simplebedrockmodel.v2.client.renderer.GeoArmorRendererV2
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.core.Holder
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType
import net.minecraft.world.item.component.ItemAttributeModifiers

@RegistryName("ru_chest_6b43")
class RuChest6b43Item : Item(
    Properties().humanoidArmor(ModArmorMaterials.CEMENTED_CARBIDE, ArmorType.CHESTPLATE).bulletResistance(ModArmorMaterials.CEMENTED_CARBIDE, ArmorType.CHESTPLATE, 0.5)
) {

    companion object {
        val TEXTURE = loc("textures/bedrock/armor/ru_chest_6b43.png")
        val MODEL = loc("models/bedrock/armor/ru_chest_6b43.geo.json")

        @Environment(EnvType.CLIENT)
        @JvmStatic
        fun registerRenderer() {
            ArmorRenderCompat.register(ModItems.RU_CHEST_6B43) { slot ->
                ArmorModelReloadListener.getModel(MODEL)?.let { GeoArmorRendererV2(it, slot, TEXTURE) }
            }
        }
    }
}
