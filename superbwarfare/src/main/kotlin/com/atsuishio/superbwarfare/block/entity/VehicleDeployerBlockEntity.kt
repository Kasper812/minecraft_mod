package com.atsuishio.superbwarfare.block.entity

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.level.storage.ValueOutput

import net.minecraft.world.level.storage.ValueInput

import com.atsuishio.portlib.nbt.LegacyNbt

import com.atsuishio.superbwarfare.block.VehicleDeployerBlock
import com.atsuishio.superbwarfare.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.joml.Math
import java.util.*

open class VehicleDeployerBlockEntity(pPos: BlockPos, pBlockState: BlockState) :
    BlockEntity(ModBlockEntities.VEHICLE_DEPLOYER, pPos, pBlockState) {
    @JvmField
    var entityData: CompoundTag = CompoundTag()

    override fun saveAdditional(output: ValueOutput) {

        val tag = LegacyNbt.tagOf(output)

        val registries = LegacyNbt.lookupOrEmpty(this.level?.registryAccess())
        super.saveAdditional(output)

        if (this.entityData.contains("EntityType")) {
            tag.putString("EntityType", this.entityData.getStringOr("EntityType", ""))
        }
        if (this.entityData.contains("Entity")) {
            tag.put("Entity", this.entityData.getCompoundOrEmpty("Entity"))
        }
    }

    override fun loadAdditional(input: ValueInput) {

        val tag = LegacyNbt.tagOf(input)

        val registries = input.lookup()
        super.loadAdditional(input)

        this.entityData = tag.copy()
    }

    fun deploy(state: BlockState) {
        val level = this.level ?: return

        if (this.entityData.contains("EntityType")) {
            val entityType = entityTypeByString(entityData.getStringOr("EntityType", "")).orElse(null) ?: return

            val entity = entityType.create(level) ?: return

            if (entityData.contains("Entity")) {
                val entityTag = entityData.getCompoundOrEmpty("Entity").copy()
                entityTag.remove("UUID")
                entity.load(entityTag)
            }

            val direction = state.getValue(VehicleDeployerBlock.FACING)

            entity.setUUID(UUID.randomUUID())
            entity.setPos(
                this.blockPos.x + 0.5 + (2 * Math.random() - 1) * 0.1f,
                this.blockPos.y + 1.5 + (2 * Math.random() - 1) * 0.1f,
                this.blockPos.z + 0.5 + (2 * Math.random() - 1) * 0.1f
            )
            entity.yRot = direction.toYRot()
            level.addFreshEntity(entity)
        }
    }

    fun writeEntityInfo(stack: ItemStack) {
        val tag = stack.get(DataComponents.BLOCK_ENTITY_DATA) ?: return

        this.entityData = tag.copyTag()
    }
}
