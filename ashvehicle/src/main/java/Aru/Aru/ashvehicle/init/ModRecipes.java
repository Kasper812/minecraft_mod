package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.recipe.JerryCanRefillRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {
    private static final MapCodec<JerryCanRefillRecipe> JERRY_CAN_REFILL_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(JerryCanRefillRecipe::category)
    ).apply(i, JerryCanRefillRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, JerryCanRefillRecipe> JERRY_CAN_REFILL_STREAM_CODEC =
            StreamCodec.composite(CraftingBookCategory.STREAM_CODEC, JerryCanRefillRecipe::category, JerryCanRefillRecipe::new);

    public static final RecipeSerializer<JerryCanRefillRecipe> JERRY_CAN_REFILL = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(AshVehicle.MODID, "jerry_can_refill"),
            new RecipeSerializer<>(JERRY_CAN_REFILL_CODEC, JERRY_CAN_REFILL_STREAM_CODEC));

    public static void init() {
    }
}
