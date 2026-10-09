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
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

open class AirSheepEntity(type: EntityType<AirSheepEntity>, world: Level) : VehicleEntity(type, world) {
    companion object {
        @JvmField
        val COLOR_ID: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(AirSheepEntity::class.java, EntityDataSerializers.INT)

        /** Maps dye color ID to wool item, mirroring vanilla [net.minecraft.world.entity.animal.Sheep.ITEM_BY_DYE] */
        private val WOOL_BY_DYE_COLOR = arrayOf(
            Items.WOOL.white(), Items.WOOL.orange(), Items.WOOL.magenta(), Items.WOOL.lightBlue(),
            Items.WOOL.yellow(), Items.WOOL.lime(), Items.WOOL.pink(), Items.WOOL.gray(),
            Items.WOOL.lightGray(), Items.WOOL.cyan(), Items.WOOL.purple(), Items.WOOL.blue(),
            Items.WOOL.brown(), Items.WOOL.green(), Items.WOOL.red(), Items.WOOL.black()
        )
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

    override fun destroy() {
        val level = this.level()
        if (level is ServerLevel) {
            val x = this.x
            val y = this.y
            val z = this.z
            level.explode(null, x, y, z, 0f, Level.ExplosionInteraction.NONE)

            // Drop raw mutton and color-matched wool (mirrors vanilla Sheep drops)
            val woolItem = WOOL_BY_DYE_COLOR.getOrElse(colorId) { Items.WOOL.white() }
            val mutton = ItemEntity(level, x, (y + 1), z, ItemStack(Items.MUTTON))
            val wool = ItemEntity(level, x, (y + 1), z, ItemStack(woolItem))
            val boat = ItemEntity(level, x, (y + 1), z, ItemStack(Items.OAK_BOAT))

            mutton.setPickUpDelay(10)
            wool.setPickUpDelay(10)

            boat.setPickUpDelay(10)

            level.addFreshEntity(mutton)
            level.addFreshEntity(wool)
            level.addFreshEntity(boat)
        }
        super.destroy()
        this.discard()
    }
}
