package com.atsuishio.superbwarfare.port

import com.atsuishio.portlib.nbt.LegacyNbt
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.phys.Vec3
import java.util.UUID

fun CompoundTag.putUUID(key: String, uuid: UUID) = LegacyNbt.putUUID(this, key, uuid)

fun CompoundTag.getUUID(key: String): UUID = LegacyNbt.getUUID(this, key) ?: UUID(0L, 0L)

fun CompoundTag.hasUUID(key: String): Boolean = LegacyNbt.hasUUID(this, key)

val CompoundTag.allKeys: Set<String> get() = keySet()

fun Player.displayClientMessage(message: Component, overlay: Boolean) {
    if (overlay) sendOverlayMessage(message) else sendSystemMessage(message)
}

val BlockPos.center: Vec3 get() = Vec3.atCenterOf(this)

val LevelHeightAccessor.minBuildHeight: Int get() = minY

val LevelHeightAccessor.maxBuildHeight: Int get() = maxY + 1

fun ItemStack.save(provider: HolderLookup.Provider): Tag = LegacyNbt.saveItem(this, provider)

fun ItemStack.saveOptional(provider: HolderLookup.Provider): Tag = LegacyNbt.saveItemOptional(this, provider)

fun blockTag(tag: net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block>): net.minecraft.core.HolderSet<net.minecraft.world.level.block.Block> =
    net.minecraft.core.registries.BuiltInRegistries.acquireBootstrapRegistrationLookup(net.minecraft.core.registries.BuiltInRegistries.BLOCK).getOrThrow(tag)

fun bulletResistanceModifierId(slot: net.minecraft.world.entity.EquipmentSlot): net.minecraft.resources.Identifier =
    com.atsuishio.superbwarfare.Mod.ATTRIBUTE_MODIFIER.withSuffix("/" + slot.getName())

fun isBulletResistanceModifier(id: net.minecraft.resources.Identifier): Boolean =
    id.namespace == com.atsuishio.superbwarfare.Mod.ATTRIBUTE_MODIFIER.namespace &&
        (id.path == com.atsuishio.superbwarfare.Mod.ATTRIBUTE_MODIFIER.path || id.path.startsWith(com.atsuishio.superbwarfare.Mod.ATTRIBUTE_MODIFIER.path + "/"))

fun net.minecraft.world.item.Item.Properties.bulletResistance(
    material: net.minecraft.world.item.equipment.ArmorMaterial,
    type: net.minecraft.world.item.equipment.ArmorType,
    value: Double
): net.minecraft.world.item.Item.Properties = attributes(
    material.createAttributes(type).withModifierAdded(
        com.atsuishio.superbwarfare.init.ModAttributes.bulletResistanceHolder(),
        net.minecraft.world.entity.ai.attributes.AttributeModifier(
            bulletResistanceModifierId(type.slot),
            value,
            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
        ),
        net.minecraft.world.entity.EquipmentSlotGroup.bySlot(type.slot)
    )
)

fun ItemStack.armorDefense(slot: net.minecraft.world.entity.EquipmentSlot): Int {
    val modifiers = getOrDefault(
        net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
        net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY
    )
    var total = 0.0
    modifiers.forEach(slot) { attribute, modifier ->
        if (attribute.`is`(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) &&
            modifier.operation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
        ) {
            total += modifier.amount()
        }
    }
    return total.toInt()
}

fun net.minecraft.world.item.Item.Properties.attackAttributes(damage: Double, speed: Double): net.minecraft.world.item.Item.Properties =
    attributes(
        net.minecraft.world.item.component.ItemAttributeModifiers.builder()
            .add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID,
                    damage,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                ),
                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND
            )
            .add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID,
                    speed,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                ),
                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND
            )
            .build()
    )

fun ItemStack.save(provider: HolderLookup.Provider, tag: net.minecraft.nbt.CompoundTag): Tag {
    val saved = LegacyNbt.saveItem(this, provider)
    if (saved is net.minecraft.nbt.CompoundTag) tag.merge(saved)
    return tag
}
