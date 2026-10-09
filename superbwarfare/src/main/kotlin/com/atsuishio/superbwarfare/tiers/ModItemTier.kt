package com.atsuishio.superbwarfare.tiers

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod.Companion.loc
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.level.block.Block

enum class ModItemTier(
    val uses: Int,
    val speed: Float,
    val attackDamageBonus: Float,
    val enchantmentValue: Int,
    repairTag: String
) {
    STEEL(400, 6F, 5F, 15, "steel_tool_materials"),
    CEMENTED_CARBIDE(2000, 8F, 8F, 18, "cemented_carbide_tool_materials");

    val repairItems: TagKey<Item> = TagKey.create(Registries.ITEM, loc(repairTag))

    val incorrectBlocksForDrops: TagKey<Block> get() = BlockTags.INCORRECT_FOR_IRON_TOOL

    val material: ToolMaterial = ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL,
        uses,
        speed,
        attackDamageBonus,
        enchantmentValue,
        repairItems
    )
}
