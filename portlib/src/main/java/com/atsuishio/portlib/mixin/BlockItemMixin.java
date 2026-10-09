package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.registry.RegistrationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;<init>(Lnet/minecraft/world/item/Item$Properties;)V"))
    private static Item.Properties portlib$blockItemProperties(Item.Properties properties) {
        ResourceKey<Item> key = RegistrationContext.currentItem();
        if (key != null && ((ItemPropertiesAccessor) properties).portlib$getId() == null) {
            properties.setId(key);
            properties.useBlockDescriptionPrefix();
        }
        return properties;
    }
}
