package com.atsuishio.portlib.mixin;

import com.atsuishio.portlib.registry.RegistrationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static BlockBehaviour.Properties portlib$assignBlockId(BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = RegistrationContext.currentBlock();
        if (key != null && ((BlockPropertiesAccessor) properties).portlib$getId() == null) {
            properties.setId(key);
        }
        return properties;
    }
}
