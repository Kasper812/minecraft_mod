package com.atsuishio.superbwarfare.item.misc

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.entity.living.DPSGeneratorEntity
import com.atsuishio.superbwarfare.init.ModEntities
import com.atsuishio.superbwarfare.init.RegistryName
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.HitResult
import java.util.function.Predicate
import javax.annotation.ParametersAreNonnullByDefault

@RegistryName("dps_generator_deployer")
class DPSGeneratorDeployerItem : Item(Properties()) {
    @ParametersAreNonnullByDefault
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        tooltipComponents.add(
            Component.translatable("des.superbwarfare.dps_generator_deployer").withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC)
        )
    }

    override fun useOn(pContext: UseOnContext): InteractionResult {
        val level = pContext.level
        if (level !is ServerLevel) {
            return InteractionResult.SUCCESS
        } else {
            val itemstack = pContext.itemInHand
            val blockpos = pContext.clickedPos
            val direction = pContext.clickedFace
            val blockstate = level.getBlockState(blockpos)
            val pos: BlockPos?
            if (blockstate.getCollisionShape(level, blockpos).isEmpty) {
                pos = blockpos
            } else {
                pos = blockpos.relative(direction)
            }

            // 禁止堆叠
            if (!level.getEntities(
                    null as Entity?,
                    ModEntities.DPS_GENERATOR.getSpawnAABB(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
                    IS_GENERATOR
                ).isEmpty()
            ) {
                return InteractionResult.FAIL
            }

            val entity = ModEntities.DPS_GENERATOR.spawn(
                level,
                itemstack,
                pContext.player,
                pos,
                EntitySpawnReason.SPAWN_ITEM_USE,
                true,
                blockpos != pos && direction == Direction.UP
            )
            if (entity != null) {
                // 放置后让它面朝玩家（EntityType.create 里默认是随机朝向）
                pContext.player?.let { entity.facePlayer(it) }
                itemstack.shrink(1)
                level.gameEvent(pContext.player, GameEvent.ENTITY_PLACE, blockpos)
            }

            return InteractionResult.CONSUME
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val itemstack = player.getItemInHand(hand)
        val blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY)
        if (blockhitresult.type != HitResult.Type.BLOCK) {
            return InteractionResult.PASS
        } else if (level !is ServerLevel) {
            return InteractionResult.SUCCESS
        } else {
            val blockpos = blockhitresult.blockPos
            if (level.getBlockState(blockpos).block !is LiquidBlock) {
                return InteractionResult.PASS
            } else if (level.mayInteract(player, blockpos) && player.mayUseItemAt(
                    blockpos,
                    blockhitresult.direction,
                    itemstack
                )
            ) {
                // 禁止堆叠
                if (!level.getEntities(
                        null as Entity?,
                        ModEntities.DPS_GENERATOR
                            .getSpawnAABB(blockpos.x + 0.5, blockpos.y + 0.5, blockpos.z + 0.5),
                        IS_GENERATOR
                    ).isEmpty()
                ) {
                    return InteractionResult.FAIL
                }

                val entity = ModEntities.DPS_GENERATOR
                    .spawn(level, itemstack, player, blockpos, EntitySpawnReason.SPAWN_ITEM_USE, false, false)
                if (entity == null) {
                    return InteractionResult.PASS
                } else {
                    entity.facePlayer(player)
                    if (!player.abilities.instabuild) {
                        itemstack.shrink(1)
                    }

                    player.awardStat(Stats.ITEM_USED.get(this))
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position())
                    return InteractionResult.CONSUME
                }
            } else {
                return InteractionResult.FAIL
            }
        }
    }

    companion object {
        private val IS_GENERATOR = Predicate { e: Entity? -> e is DPSGeneratorEntity }
    }
}
