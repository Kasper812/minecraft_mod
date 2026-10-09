package com.atsuishio.superbwarfare.compat.jei

import com.atsuishio.superbwarfare.init.ModItems
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.crafting.*
import java.util.*

object PotionMortarShellRecipeMaker {
    fun createRecipes(): MutableList<RecipeHolder<CraftingRecipe>> {
        val group = "jei.potion_mortar_shell"
        val shell = Optional.of(Ingredient.of(ModItems.MORTAR_SHELL))
        val potionIngredient = Optional.of(Ingredient.of(Items.LINGERING_POTION))
        val empty = Optional.empty<Ingredient>()

        return BuiltInRegistries.POTION.listElements().map { potion ->
            val output = ItemStackTemplate(
                BuiltInRegistries.ITEM.wrapAsHolder(ModItems.POTION_MORTAR_SHELL),
                4,
                DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, PotionContents(potion)).build()
            )
            val inputs = listOf(
                empty, shell, empty,
                shell, potionIngredient, shell,
                empty, shell, empty
            )
            val id = Identifier.withDefaultNamespace(group + "." + potion.key().identifier().path)
            RecipeHolder<CraftingRecipe>(
                ResourceKey.create(Registries.RECIPE, id),
                ShapedRecipe(
                    Recipe.CommonInfo(false),
                    CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group),
                    ShapedRecipePattern(3, 3, inputs, Optional.empty()),
                    output
                )
            )
        }.toList()
    }
}
