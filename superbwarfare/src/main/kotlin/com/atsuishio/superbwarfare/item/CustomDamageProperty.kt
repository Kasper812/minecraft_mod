package com.atsuishio.superbwarfare.item

import com.atsuishio.superbwarfare.port.*

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Item

/**
 * 强行设置自定义耐久的物品属性，适用于需要使用Tier又不想使用Tier提供的耐久的物品的属性注册
 */
class CustomDamageProperty : Item.Properties {
    /**
     * 创建有限耐久的物品
     */
    constructor(maxDamage: Int) {
        super.durability(maxDamage)
    }

    /**
     * 创建无限耐久的物品
     */
    constructor(showInTooltip: Boolean) {
        this.component(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE)
    }

    override fun durability(maxDamage: Int): CustomDamageProperty {
        return this
    }
}
