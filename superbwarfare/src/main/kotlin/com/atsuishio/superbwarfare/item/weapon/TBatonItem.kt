package com.atsuishio.superbwarfare.item.weapon

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.init.ModSounds
import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.item.CustomDamageProperty
import com.atsuishio.superbwarfare.tiers.ModItemTier
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Item
import net.minecraft.world.item.ToolMaterial
import org.joml.Math

@RegistryName("t_baton")
class TBatonItem : Item(
    CustomDamageProperty(1115).sword(ModItemTier.STEEL.material, 3f, -2f)
) {
    override fun hurtEnemy(pStack: ItemStack, pTarget: LivingEntity, pAttacker: LivingEntity) {
        pAttacker.level().playSound(
            null,
            pTarget.onPos,
            ModSounds.MELEE_HIT,
            SoundSource.PLAYERS,
            1f,
            ((2 * Math.random() - 1) * 0.1f + 1.0f).toFloat()
        )
        super.hurtEnemy(pStack, pTarget, pAttacker)
    }
}
