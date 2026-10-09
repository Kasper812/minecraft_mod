package com.atsuishio.superbwarfare.compat.trinkets

import com.atsuishio.superbwarfare.port.*

import eu.pb4.trinkets.api.TrinketAttachment
import eu.pb4.trinkets.api.TrinketInventory
import eu.pb4.trinkets.api.TrinketSlotAccess
import eu.pb4.trinkets.api.callback.TrinketCallback
import com.atsuishio.portlib.util.Tuple
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.Optional

class SlotReference(val access: TrinketSlotAccess) {
    fun inventory(): TrinketInventory = access.inventory()

    fun index(): Int = access.index()
}

class TrinketComponent(val attachment: TrinketAttachment) {
    fun isEquipped(item: Item): Boolean = attachment.isEquipped(item)

    fun isEquipped(predicate: (ItemStack) -> Boolean): Boolean = attachment.isEquipped { predicate(it) }

    fun getEquipped(item: Item): List<Tuple<SlotReference, ItemStack>> =
        attachment.equipped(item, false).map { Tuple(SlotReference(it), it.get()) }

    fun getEquipped(predicate: (ItemStack) -> Boolean): List<Tuple<SlotReference, ItemStack>> =
        attachment.equipped({ predicate(it) }, false).map { Tuple(SlotReference(it), it.get()) }

    val inventory: Map<String, Map<String, TrinketInventory>>
        get() = attachment.getInventory()
}

object TrinketsApi {
    @JvmStatic
    fun getTrinketComponent(entity: LivingEntity?): Optional<TrinketComponent> {
        if (entity == null) return Optional.empty()
        return Optional.ofNullable(eu.pb4.trinkets.api.TrinketsApi.getAttachment(entity)).map { TrinketComponent(it) }
    }
}

interface Trinket {
    fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {}

    fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {}

    fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {}

    fun canEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity): Boolean = true

    fun canUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity): Boolean = true
}

open class TrinketItem(properties: Properties) : Item(properties), Trinket {
    init {
        registerTrinket(this, this)
    }

    companion object {
        @JvmStatic
        fun registerTrinket(item: Item, trinket: Trinket) {
            TrinketCallback.setCallback(item, object : TrinketCallback {
                override fun tick(stack: ItemStack, access: TrinketSlotAccess, entity: LivingEntity) =
                    trinket.tick(stack, SlotReference(access), entity)

                override fun onEquip(stack: ItemStack, access: TrinketSlotAccess, entity: LivingEntity) =
                    trinket.onEquip(stack, SlotReference(access), entity)

                override fun onUnequip(stack: ItemStack, access: TrinketSlotAccess, entity: LivingEntity) =
                    trinket.onUnequip(stack, SlotReference(access), entity)

                override fun canEquip(stack: ItemStack, access: TrinketSlotAccess, entity: LivingEntity): Boolean =
                    trinket.canEquip(stack, SlotReference(access), entity)

                override fun canUnequip(stack: ItemStack, access: TrinketSlotAccess, entity: LivingEntity): Boolean =
                    trinket.canUnequip(stack, SlotReference(access), entity)

                override fun canEquipFromUse(stack: ItemStack, entity: LivingEntity): Boolean = true
            })
        }
    }
}
