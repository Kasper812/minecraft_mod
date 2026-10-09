package com.atsuishio.superbwarfare.init

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization

import net.minecraft.network.RegistryFriendlyByteBuf

import net.minecraft.network.codec.StreamCodec

import com.mojang.serialization.codecs.RecordCodecBuilder

import com.mojang.serialization.MapCodec

import net.minecraft.world.item.crafting.CraftingBookCategory

import net.minecraft.world.item.crafting.CustomRecipe

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod
import com.atsuishio.superbwarfare.recipe.*
import com.atsuishio.superbwarfare.recipe.vehicle.VehicleAssemblingRecipe
import com.atsuishio.superbwarfare.recipe.vehicle.VehicleAssemblingRecipeSerializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType

@Suppress("unused")
object ModRecipes {
    @JvmField
    val POTION_MORTAR_SHELL_SERIALIZER: RecipeSerializer<PotionMortarShellRecipe> =
        registerSerializer("potion_mortar_shell", simpleCraftingSerializer(::PotionMortarShellRecipe))

    @JvmField
    val SMOKE_DYE_SERIALIZER: RecipeSerializer<SmokeDyeRecipe> =
        registerSerializer("smoke_dye", simpleCraftingSerializer(::SmokeDyeRecipe))

    @JvmField
    val VEHICLE_ASSEMBLING_SERIALIZER: RecipeSerializer<VehicleAssemblingRecipe> =
        registerSerializer("vehicle_assembling", VehicleAssemblingRecipeSerializer.INSTANCE)

    @JvmField
    val VEHICLE_RESET_SERIALIZER: RecipeSerializer<VehicleResetRecipe> =
        registerSerializer("vehicle_reset", simpleCraftingSerializer(::VehicleResetRecipe))

    @JvmField
    val RESEARCHING_SERIALIZER: RecipeSerializer<ResearchingRecipe> =
        registerSerializer("researching", ResearchingRecipe.Serializer.INSTANCE)

    @JvmField
    val VEHICLE_ASSEMBLING_TYPE: RecipeType<VehicleAssemblingRecipe> =
        registerType("vehicle_assembling", object : RecipeType<VehicleAssemblingRecipe> {
            override fun toString(): String {
                return Mod.MODID + ":vehicle_assembling"
            }
        })

    @JvmField
    val RESEARCHING_TYPE: RecipeType<ResearchingRecipe> =
        registerType("researching", object : RecipeType<ResearchingRecipe> {
            override fun toString(): String {
                return Mod.MODID + ":researching"
            }
        })

    private fun <T : RecipeSerializer<*>> registerSerializer(name: String, serializer: T): T {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Mod.loc(name), serializer)
    }

    private fun <T : RecipeType<*>> registerType(name: String, type: T): T {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, Mod.loc(name), type)
    }

    private fun <T : CustomRecipe> simpleCraftingSerializer(factory: (CraftingBookCategory) -> T): RecipeSerializer<T> {
        val codec: MapCodec<T> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter { it.category() }
            ).apply(i, factory)
        }
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, T> = StreamCodec.composite(
            CraftingBookCategory.STREAM_CODEC, { it.category() }, factory
        )
        return RecipeSerializer(codec, streamCodec)
    }

    @JvmStatic
    fun init() {
        RecipeSynchronization.synchronizeRecipeSerializer(VEHICLE_ASSEMBLING_SERIALIZER)
        RecipeSynchronization.synchronizeRecipeSerializer(RESEARCHING_SERIALIZER)
    }
}
