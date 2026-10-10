package frontline.combat.fcp.item.varies;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class SprayItem extends Item {
    public SprayItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack pStack, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag pIsAdvanced) {
        java.util.List<Component> pTooltipComponents = new com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder);
        pTooltipComponents.add(Component.translatable("des.fcp.spray").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("des.fcp.spray.usage").withStyle(ChatFormatting.DARK_GRAY));
    }

}
