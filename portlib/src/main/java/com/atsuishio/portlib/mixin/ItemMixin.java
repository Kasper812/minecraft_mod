package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.registry.RegistrationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Item.Properties portlib$assignItemId(Item.Properties properties) {
        ResourceKey<Item> key = RegistrationContext.currentItem();
        if (key != null && ((ItemPropertiesAccessor) properties).portlib$getId() == null) {
            properties.setId(key);
        }
        return properties;
    }
}
