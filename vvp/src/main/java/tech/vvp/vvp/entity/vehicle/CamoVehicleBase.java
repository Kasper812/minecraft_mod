package tech.vvp.vvp.entity.vehicle;

import net.minecraft.world.level.storage.ValueInput;
import com.atsuishio.portlib.nbt.LegacyNbt;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tech.vvp.vvp.init.ModItems;
import tech.vvp.vvp.init.ModSounds;

/**
 * Базовый класс для техники с поддержкой камуфляжа.
 * Наследуйся от этого класса и переопредели getCamoTextures() и getCamoNames()
 */
public abstract class CamoVehicleBase extends VvpVehicleBase implements ICamoVehicle {

    private static final EntityDataAccessor<Integer> CAMO_TYPE = SynchedEntityData.defineId(CamoVehicleBase.class, EntityDataSerializers.INT);

    public CamoVehicleBase(EntityType<? extends GeoVehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAMO_TYPE, 0);
    }

    @Override
    public int getCamoType() {
        return this.entityData.get(CAMO_TYPE);
    }

    @Override
    public void setCamoType(int camoType) {
        this.entityData.set(CAMO_TYPE, camoType);
    }

    @Override
    public void cycleCamo() {
        int current = getCamoType();
        setCamoType((current + 1) % getCamoTextures().length);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, net.minecraft.world.phys.Vec3 interactLocation) {
        if (player.getItemInHand(hand).is(ModItems.SPRAY.get())) {
            if (!this.level().isClientSide()) {
                cycleCamo();

                String[] camoNames = getCamoNames();
                int camoType = getCamoType();
                String camoName = (camoType >= 0 && camoType < camoNames.length)
                        ? camoNames[camoType]
                        : "Unknown";

                player.sendOverlayMessage(
                        Component.translatable("message.vvp.camo_changed", camoName).withStyle(ChatFormatting.GREEN));

                this.level().playSound(null, this.blockPosition(),
                        ModSounds.SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return super.interact(player, hand, interactLocation);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compoundIo) {
        CompoundTag compound = LegacyNbt.tagOf(compoundIo);
        super.addAdditionalSaveData(compoundIo);
        compound.putInt("CamoType", getCamoType());
    }

    @Override
    public void readAdditionalSaveData(ValueInput compoundIo) {
        CompoundTag compound = LegacyNbt.tagOf(compoundIo);
        super.readAdditionalSaveData(compoundIo);
        if (compound.contains("CamoType")) {
            setCamoType(compound.getIntOr("CamoType", 0));
        }
    }

    @Override
    public abstract Identifier[] getCamoTextures();

    @Override
    public abstract String[] getCamoNames();
}
