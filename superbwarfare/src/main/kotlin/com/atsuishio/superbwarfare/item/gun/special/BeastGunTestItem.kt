package com.atsuishio.superbwarfare.item.gun.special

import net.minecraft.world.item.Item

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.client.GunRendererBuilder
import com.atsuishio.superbwarfare.client.model.item.BeastGunTestModel
import com.atsuishio.superbwarfare.init.ModRarities
import com.atsuishio.superbwarfare.init.RegistryName
import com.atsuishio.superbwarfare.item.gun.GunGeoItem
import com.atsuishio.superbwarfare.item.weapon.BeastItem.Companion.beastKill
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import software.bernie.geckolib.renderer.GeoItemRenderer
import java.util.function.Supplier
import javax.annotation.ParametersAreNonnullByDefault

@RegistryName("beast_gun_test")
open class BeastGunTestItem : GunGeoItem(Properties().rarity(ModRarities.BEAST)) {
    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        beastKill(attacker, target)
        return
    }

    @ParametersAreNonnullByDefault
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,
        tooltipAdder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val tooltipComponents = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)
        tooltipComponents.add(Component.translatable("des.superbwarfare.beast").withColor(0xa56855))
    }

    override fun getRenderer(): Supplier<out GeoItemRenderer<*>> =
        GunRendererBuilder.simple { BeastGunTestModel }
}
