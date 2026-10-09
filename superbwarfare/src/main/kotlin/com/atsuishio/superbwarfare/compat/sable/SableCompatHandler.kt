package com.atsuishio.superbwarfare.compat.sable

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.entity.projectile.IAdvancedHitDetection
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.ExplosionDamageCalculator
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

object SableCompatHandler {
    @JvmStatic
    fun hasMod(): Boolean = false

    @JvmStatic
    fun collectSubLevelBlockCollisions(level: Level, worldSearchBox: AABB, targetList: MutableList<AABB>) {
    }

    @JvmStatic
    fun rayTraceBlocksWithSable(
        world: Level,
        context: ClipContext,
        ignorePredicate: Predicate<BlockState>
    ): BlockHitResult = IAdvancedHitDetection.rayTraceBlocks(world, context, ignorePredicate)

    @JvmStatic
    fun processExplosionOnSubLevels(
        world: Level,
        center: Vec3,
        radius: Float,
        damage: Float,
        explosion: Explosion,
        damageCalculator: ExplosionDamageCalculator
    ) {
    }

    @JvmStatic
    fun isOnSubLevelGround(level: Level, testObbAabb: AABB): Boolean = false
}
