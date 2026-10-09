package com.atsuishio.superbwarfare.entity.vehicle

import net.minecraft.world.phys.Vec3

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.level.storage.ValueOutput

import net.minecraft.world.level.storage.ValueInput

import com.atsuishio.portlib.nbt.LegacyNbt

import com.atsuishio.superbwarfare.entity.getValue
import com.atsuishio.superbwarfare.entity.setValue
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeItem
import net.minecraft.world.level.Level

open class TinySpeedboatEntity(type: EntityType<TinySpeedboatEntity>, world: Level) : VehicleEntity(type, world) {
    companion object {
        @JvmField
        val COLOR_ID: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(TinySpeedboatEntity::class.java, EntityDataSerializers.INT)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(COLOR_ID, 0)
    }

    var colorId by COLOR_ID

    override fun addAdditionalSaveData(output: ValueOutput) {

        val compound = LegacyNbt.tagOf(output)
        super.addAdditionalSaveData(output)
        compound.putInt("ColorId", colorId)
    }

    public override fun readAdditionalSaveData(input: ValueInput) {

        val compound = LegacyNbt.tagOf(input)
        super.readAdditionalSaveData(input)
        colorId = compound.getIntOr("ColorId", 0)
    }

    override fun interact(player: Player, hand: InteractionHand, location: Vec3): InteractionResult {
        val stack = player.mainHandItem
        if (stack.item is DyeItem) {
            if (customName != null && customName!!.string == "jeb_") return InteractionResult.PASS
            val stackColor = (stack.get(net.minecraft.core.component.DataComponents.DYE) ?: net.minecraft.world.item.DyeColor.WHITE).id

            if (colorId == stackColor) {
                return super.interact(player, hand, location)
            }

            colorId = stackColor

            if (!player.isCreative) {
                stack.shrink(1)
            }
            this.level().playSound(null, this, SoundEvents.BONE_MEAL_USE, this.soundSource, 1f, 1f)
            return InteractionResult.SUCCESS
        }
        return super.interact(player, hand, location)
    }
}
