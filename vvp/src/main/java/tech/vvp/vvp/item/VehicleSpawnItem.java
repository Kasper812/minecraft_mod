package tech.vvp.vvp.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class VehicleSpawnItem extends Item {
    private final Supplier<EntityType<?>> entityTypeSupplier;

    public VehicleSpawnItem(Supplier<EntityType<?>> entityTypeSupplier, Properties properties) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level instanceof ServerLevelAccessor serverLevel) {
            EntityType<?> entityType = this.entityTypeSupplier.get();
            Entity entity = entityType.create(serverLevel.getLevel(), net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
            if (entity != null) {
                entity.setPos(context.getClickLocation().x, context.getClickLocation().y, context.getClickLocation().z);
                
                net.minecraft.core.Direction direction = context.getHorizontalDirection();
                float yaw = direction.toYRot();
                
                entity.setYRot(yaw);
                entity.setXRot(0);
                entity.yRotO = yaw;
                entity.xRotO = 0;
                
                serverLevel.getLevel().addFreshEntity(entity);
                
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag flag) {
        java.util.List<Component> tooltip = new com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder);
        super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltipAdder, flag);
    }
}
