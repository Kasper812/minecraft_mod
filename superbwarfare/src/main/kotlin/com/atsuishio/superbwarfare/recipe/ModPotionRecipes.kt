package com.atsuishio.superbwarfare.recipe

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.init.ModPotions
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.Ingredient

object ModPotionRecipes {
    @JvmStatic
    fun register() {
        FabricPotionBrewingBuilder.BUILD.register { builder ->
            builder.registerPotionRecipe(Potions.WATER, Ingredient.of(Items.LIGHTNING_ROD.weathering().unaffected()), holder(ModPotions.SHOCK))
            builder.registerPotionRecipe(holder(ModPotions.SHOCK), Ingredient.of(Items.GLOWSTONE_DUST), holder(ModPotions.STRONG_SHOCK))
            builder.registerPotionRecipe(holder(ModPotions.SHOCK), Ingredient.of(Items.REDSTONE), holder(ModPotions.LONG_SHOCK))
        }
    }

    fun potion(potion: Holder<Potion>): ItemStack {
        val stack = Items.POTION.defaultInstance
        val contents = stack.get(DataComponents.POTION_CONTENTS)

        if (contents != null) {
            stack.set(DataComponents.POTION_CONTENTS, contents.withPotion(potion))
        }

        return stack
    }

    private fun holder(potion: Potion): Holder<Potion> {
        return BuiltInRegistries.POTION.wrapAsHolder(potion)
    }
}
