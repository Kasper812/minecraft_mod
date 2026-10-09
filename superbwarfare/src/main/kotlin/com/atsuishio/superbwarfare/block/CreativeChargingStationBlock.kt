package com.atsuishio.superbwarfare.block

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.init.ModCapabilities
import com.atsuishio.superbwarfare.capability.energy.EnergyStorageHelper
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext

import com.atsuishio.superbwarfare.block.entity.CreativeChargingStationBlockEntity
import com.atsuishio.superbwarfare.init.ModBlockEntities
import com.mojang.serialization.MapCodec
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult

class CreativeChargingStationBlock(properties: Properties) : BaseEntityBlock(properties) {
    constructor() : this(Properties.of().sound(SoundType.METAL).strength(3.0f).requiresCorrectToolForDrops()) {
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    init {
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    public override fun getRenderShape(pState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return CreativeChargingStationBlockEntity(pPos, pState)
    }

    override fun <T : BlockEntity> getTicker(
        pLevel: Level,
        pState: BlockState,
        pBlockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (!pLevel.isClientSide) {
            return createTickerHelper<CreativeChargingStationBlockEntity, T>(
                pBlockEntityType, ModBlockEntities.CREATIVE_CHARGING_STATION,
                CreativeChargingStationBlockEntity::serverTick
            )
        }
        return null
    }
    override fun affectNeighborsAfterRemoval(
        pState: BlockState,
        pLevel: net.minecraft.server.level.ServerLevel,
        pPos: BlockPos,
        pMovedByPiston: Boolean
    ) {
        pLevel.updateNeighbourForOutputSignal(pPos, this)
        super.affectNeighborsAfterRemoval(pState, pLevel, pPos, pMovedByPiston)
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (stack.isEmpty) return InteractionResult.TRY_WITH_EMPTY_HAND

        val cap = ContainerItemContext.forPlayerInteraction(player, hand)
            .find(ModCapabilities.ENERGY_ITEM) ?: return InteractionResult.FAIL

        if (cap.supportsInsertion() && cap.amount < cap.capacity) {
            EnergyStorageHelper.insert(cap, Long.MAX_VALUE)
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("des.superbwarfare.creative_charging_station.charge.success")
                        .withStyle(ChatFormatting.GREEN), true
                )
            }
        } else if (cap.supportsExtraction()) {
            EnergyStorageHelper.extract(cap, Long.MAX_VALUE)
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("des.superbwarfare.creative_charging_station.extract.success")
                        .withStyle(ChatFormatting.GREEN), true
                )
            }
        } else {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("des.superbwarfare.creative_charging_station.fail")
                        .withStyle(ChatFormatting.RED), true
                )
            }
            return InteractionResult.FAIL
        }

        return InteractionResult.SUCCESS
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is CreativeChargingStationBlockEntity) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS
            }

            blockEntity.showRange = !blockEntity.showRange
            level.sendBlockUpdated(pos, state, state, UPDATE_CLIENTS)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.SUCCESS
    }

    override fun createBlockStateDefinition(pBuilder: StateDefinition.Builder<Block, BlockState>) {
        pBuilder.add(FACING)
    }

    override fun getStateForPlacement(pContext: BlockPlaceContext): BlockState? {
        return this.defaultBlockState()
            .setValue(FACING, pContext.horizontalDirection.opposite)
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos, direction: net.minecraft.core.Direction): Int {
        return 15
    }

    companion object {
        val FACING: EnumProperty<Direction> = HorizontalDirectionalBlock.FACING

        private val CODEC: MapCodec<CreativeChargingStationBlock> = simpleCodec(::CreativeChargingStationBlock)
    }
}
