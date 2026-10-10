package com.atsuishio.portlib.forge;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ForgeRegistries {
    public static final Registry<Item> ITEMS = BuiltInRegistries.ITEM;
    public static final Registry<Block> BLOCKS = BuiltInRegistries.BLOCK;
    public static final Registry<EntityType<?>> ENTITY_TYPES = BuiltInRegistries.ENTITY_TYPE;
    public static final Registry<SoundEvent> SOUND_EVENTS = BuiltInRegistries.SOUND_EVENT;
    public static final Registry<ParticleType<?>> PARTICLE_TYPES = BuiltInRegistries.PARTICLE_TYPE;
    public static final Registry<MobEffect> MOB_EFFECTS = BuiltInRegistries.MOB_EFFECT;
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = BuiltInRegistries.BLOCK_ENTITY_TYPE;
    public static final Registry<MenuType<?>> MENU_TYPES = BuiltInRegistries.MENU;
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZERS = BuiltInRegistries.RECIPE_SERIALIZER;
    public static final Registry<CreativeModeTab> CREATIVE_MODE_TABS = BuiltInRegistries.CREATIVE_MODE_TAB;

    private ForgeRegistries() {
    }
}
