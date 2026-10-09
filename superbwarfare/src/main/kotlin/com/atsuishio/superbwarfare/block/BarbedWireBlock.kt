package com.atsuishio.superbwarfare.block

import net.minecraft.world.entity.InsideBlockEffectApplier

import net.minecraft.util.RandomSource

import net.minecraft.world.level.LevelReader

import net.minecraft.world.level.ScheduledTickAccess

import com.atsuishio.superbwarfare.port.*

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
open class BarbedWireBlock : Block(
    Properties.of().ignitedByLava().forceSolidOn().instrument(NoteBlockInstrument.BASS)
        .sound(SoundType.WOOD).strength(10f, 2f)
        .noCollision().speedFactor(0.01f).noOcclusion()
        .isRedstoneConductor { _, _, _ -> false }
) {
    init {
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
        )
    }

    override fun propagatesSkylightDown(
        state: BlockState
    ): Boolean  {
        return true
    }

    override fun getVisualShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return Shapes.empty()
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, WATERLOGGED)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val flag = context.level.getFluidState(context.clickedPos).type === Fluids.WATER
        return this.defaultBlockState()
            .setValue(FACING, context.horizontalDirection.opposite)
            .setValue(WATERLOGGED, flag)
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) Fluids.WATER.getSource(false) else super.getFluidState(state)
    }

    override fun rotate(state: BlockState, rot: Rotation): BlockState {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)))
    }

    override fun mirror(state: BlockState, mirrorIn: Mirror): BlockState {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)))
    }

    override fun updateShape(
        state: BlockState,
        world: LevelReader,
        scheduledTickAccess: ScheduledTickAccess,
        currentPos: BlockPos,
        facing: Direction,
        facingPos: BlockPos,
        facingState: BlockState,
        randomSource: RandomSource
    ): BlockState  {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world))
        }
        return super.updateShape(state, world, scheduledTickAccess, currentPos, facing, facingPos, facingState, randomSource)
    }

    override fun entityInside(
        blockstate: BlockState,
        world: Level,
        pos: BlockPos,
        entity: Entity,
        effectApplier: InsideBlockEffectApplier,
        isPrecise: Boolean
    ) {
        super.entityInside(blockstate, world, pos, entity, effectApplier, isPrecise)
        if (entity is LivingEntity) {
            entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), Vec3(0.15, 0.04, 0.15))
            entity.hurtOrSimulate(
                DamageSource(
                    world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(DamageTypes.CACTUS)
                ), 2f
            )
        }
    }

    companion object {
        @JvmField
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED

        @JvmField
        val FACING: EnumProperty<Direction> = HorizontalDirectionalBlock.FACING
    }
}
