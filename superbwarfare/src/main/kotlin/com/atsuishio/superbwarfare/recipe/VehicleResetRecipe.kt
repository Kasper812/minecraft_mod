package com.atsuishio.superbwarfare.recipe

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.init.ModItems
import com.atsuishio.superbwarfare.init.ModRecipes
import com.atsuishio.superbwarfare.item.container.ContainerBlockItem
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level
import javax.annotation.ParametersAreNonnullByDefault

class VehicleResetRecipe(private val bookCategory: CraftingBookCategory = CraftingBookCategory.MISC) : CustomRecipe() {
    override fun category(): CraftingBookCategory = bookCategory

    override fun matches(input: CraftingInput, pLevel: Level): Boolean {
        var kit = ItemStack.EMPTY
        var container = ItemStack.EMPTY

        for (i in 0..<input.size()) {
            val stack = input.getItem(i)
            if (!stack.isEmpty) {
                if (stack.`is`(ModItems.VEHICLE_RESET_KIT)) {
                    if (!kit.isEmpty) {
                        return false
                    }
                    kit = stack
                } else if (stack.`is`(ModItems.CONTAINER)) {
                    if (!container.isEmpty) {
                        return false
                    }
                    container = stack
                }
            }
        }
        return !kit.isEmpty && !container.isEmpty
    }

    @ParametersAreNonnullByDefault
    override fun assemble(input: CraftingInput): ItemStack {
        var kit = ItemStack.EMPTY
        var container = ItemStack.EMPTY

        for (i in 0..<input.size()) {
            val stack = input.getItem(i)
            if (!stack.isEmpty) {
                if (stack.`is`(ModItems.VEHICLE_RESET_KIT)) {
                    if (!kit.isEmpty) {
                        return ItemStack.EMPTY
                    }
                    kit = stack.copy()
                } else if (stack.`is`(ModItems.CONTAINER)) {
                    if (!container.isEmpty) {
                        return ItemStack.EMPTY
                    }
                    container = stack.copy()
                }
            }
        }

        if (!kit.isEmpty && !container.isEmpty) {
            val data = container.get(DataComponents.BLOCK_ENTITY_DATA)
            val tag = if (data != null) data.copyTag() else CompoundTag()
            if (tag.contains("EntityType")) {
                val type = tag.getStringOr("EntityType", "")
                val entityType = entityTypeByString(type).orElse(null)
                if (entityType != null) {
                    return ContainerBlockItem.createInstance(entityType)
                }
            }
        }
        return ItemStack.EMPTY
    }


    override fun getSerializer(): RecipeSerializer<out CustomRecipe> {
        return ModRecipes.VEHICLE_RESET_SERIALIZER
    }
}
