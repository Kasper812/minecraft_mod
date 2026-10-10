package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Ags30Entity extends VvpVehicleBase {

    public Ags30Entity(EntityType<Ags30Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public @NotNull List<ItemStack> getRetrieveItems() {
        var list = new ArrayList<ItemStack>();
        list.add(new ItemStack(tech.vvp.vvp.init.ModItems.AGS_30_ITEM.get()));
        return list;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand, net.minecraft.world.phys.Vec3 interactLocation) {
        if (player.isShiftKeyDown()) {
            retrieve(player);
            return InteractionResult.SUCCESS;
        }

        var gunData = getGunData(0);
        if (gunData == null) {
            return InteractionResult.SUCCESS;
        }

        var stack = player.getItemInHand(hand);
        
        // Check if player is using item_40_mm for full reload
        if (stack.is(tech.vvp.vvp.init.ModItems.ITEM_40_MM.get())) {
            if (!level().isClientSide()) {
                // Full reload - reload multiple times to fill magazine
                for (int i = 0; i < 30; i++) {
                    modifyGunData(0, data -> data.reloadAmmo(player));
                }
                
                // Consume one item_40_mm
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                
                level().playSound(null, getOnPos(), tech.vvp.vvp.init.ModSounds.HK_GMG_RELOAD.get(), SoundSource.PLAYERS, 1f, random.nextFloat() * 0.1f + 0.9f);
            }
            return InteractionResult.SUCCESS;
        }

        // Let players mount normally when the gun is ready to fire.
        if (gunData.hasEnoughAmmoToShoot(player)) {
            return super.interact(player, hand, interactLocation);
        }

        if (!gunData.selectedAmmoConsumer().isAmmoItem(stack)) {
            return super.interact(player, hand, interactLocation);
        }

        if (!level().isClientSide()) {
            modifyGunData(0, data -> data.reloadAmmo(player));
            level().playSound(null, getOnPos(), tech.vvp.vvp.init.ModSounds.HK_GMG_RELOAD.get(), SoundSource.PLAYERS, 1f, random.nextFloat() * 0.1f + 0.9f);
        }

        return InteractionResult.SUCCESS;
    }

    private void retrieve(Player player) {
        if (level().isClientSide()) {
            return;
        }

        for (var stack : getRetrieveItems()) {
            var copy = stack.copy();
            if (!player.addItem(copy)) {
                player.drop(copy, false);
            }
        }

        ejectPassengers();
        discard();
    }

    @Override
    public boolean banHand(LivingEntity entity) {
        return true;
    }
}
