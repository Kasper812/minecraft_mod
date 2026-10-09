package com.atsuishio.superbwarfare.port

import net.fabricmc.fabric.api.recipe.v1.FabricRecipeAccess
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import java.util.Optional

class LegacyRecipeView(private val level: Level) {
    private fun key(id: Identifier): ResourceKey<Recipe<*>> = ResourceKey.create(Registries.RECIPE, id)

    fun <I : RecipeInput, T : Recipe<I>> getAllRecipesFor(type: RecipeType<T>): List<RecipeHolder<T>> {
        val access = level.recipeAccess()
        if (level is ServerLevel) {
            return level.recipeAccess().getRecipes()
                .filter { it.value().getType() == type }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it as RecipeHolder<T>
                }
        }
        return (access as FabricRecipeAccess).synchronizedRecipes.getAllOfType(type).toList()
    }

    fun <I : RecipeInput, T : Recipe<I>> getRecipeFor(type: RecipeType<T>, input: I, level: Level): Optional<RecipeHolder<T>> {
        if (this.level is ServerLevel) {
            return this.level.recipeAccess().getRecipeFor(type, input, level)
        }
        return (this.level.recipeAccess() as FabricRecipeAccess).synchronizedRecipes.getFirstMatch(type, input, level)
    }

    fun byKey(id: Identifier): Optional<RecipeHolder<*>> {
        if (level is ServerLevel) {
            return level.recipeAccess().byKey(key(id))
        }
        return Optional.ofNullable((level.recipeAccess() as FabricRecipeAccess).synchronizedRecipes.get(key(id)))
    }
}

val Level.recipeManager: LegacyRecipeView get() = LegacyRecipeView(this)
