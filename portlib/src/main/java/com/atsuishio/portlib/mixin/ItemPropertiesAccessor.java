package com.atsuishio.portlib.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Properties.class)
public interface ItemPropertiesAccessor {
    @Accessor("id")
    ResourceKey<Item> portlib$getId();
}
