package Aru.Aru.ashvehicle.item;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JerryCanItem extends Item {
    // 燃料の最大容量 (10000ユニット)
    public static final int MAX_FUEL = 1000000;

    public JerryCanItem(Properties pProperties) {
        super(pProperties.durability(MAX_FUEL));
    }

    /**
     * 車両に対するインタラクションをイベントで処理します。
     * シフト＋右クリックで燃料を補充します。
     */
    public static class EventHandler {
        public static InteractionResult onEntityInteract(Player player, net.minecraft.world.level.Level level, net.minecraft.world.InteractionHand hand, net.minecraft.world.entity.Entity target, net.minecraft.world.phys.EntityHitResult hitResult) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof JerryCanItem) {
                if (target instanceof VehicleEntity vehicle) {
                    
                    // シフト右クリック時のみ発動
                    if (player.isShiftKeyDown()) {
                        
                        if (!player.level().isClientSide()) {
                            double currentEnergy = vehicle.getEnergy();
                            double maxEnergy = vehicle.getMaxEnergy();
                            
                            if (currentEnergy < maxEnergy) {
                                // ジェリー缶内の燃料残量を取得 (耐久値が0に近いほど燃料が多い)
                                int fuelInCan = stack.getMaxDamage() - stack.getDamageValue();
                                double needed = maxEnergy - currentEnergy;
                                int toTransfer = (int) Math.min(fuelInCan, needed);
                                
                                if (toTransfer > 0) {
                                    // 燃料を補充。Accessor経由でProtectedメソッドを呼び出し
                                    try {
                                        ((Aru.Aru.ashvehicle.mixin.VehicleEntityAccessor) vehicle).superbwarfare$invokeSetEnergy((int)(currentEnergy + toTransfer));
                                        
                                        // ジェリー缶の燃料を消費
                                        stack.setDamageValue(stack.getDamageValue() + toTransfer);
                                        
                                        player.sendOverlayMessage(Component.translatable("message.ashvehicle.refilled", toTransfer).withStyle(ChatFormatting.GREEN));
                                    } catch (Exception e) {
                                        player.sendSystemMessage(Component.literal("燃料の補充に失敗しました。").withStyle(ChatFormatting.RED));
                                    }
                                }
                            } else {
                                player.sendOverlayMessage(Component.translatable("message.ashvehicle.already_full").withStyle(ChatFormatting.RED));
                            }
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag pIsAdvanced) {
        java.util.List<Component> pTooltipComponents = new com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder);
        int fuel = pStack.getMaxDamage() - pStack.getDamageValue();
        pTooltipComponents.add(Component.translatable("tooltip.ashvehicle.jerry_can.fuel", fuel, MAX_FUEL).withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.ashvehicle.jerry_can.usage").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(pStack, tooltipContext, tooltipDisplay, tooltipAdder, pIsAdvanced);
    }
}
