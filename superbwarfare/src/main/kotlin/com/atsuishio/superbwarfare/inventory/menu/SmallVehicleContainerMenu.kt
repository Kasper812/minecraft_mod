package com.atsuishio.superbwarfare.inventory.menu

import com.atsuishio.superbwarfare.port.*

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType

class SmallVehicleContainerMenu(id: Int, inventory: Inventory, entityId: Int) :
    AbstractVehicleContainerMenu(TYPE, id, inventory, entityId) {
    override fun getRows(): Int = 3

    override fun addVehicleInventory() {
        for (r in 0 until getRows()) {
            for (c in 0 until 9) {
                this.addSlot(VehicleSlot(this.vehicle, c + r * 9, 8 + c * 18, 18 + r * 18))
            }
        }
    }

    companion object {
        @JvmField
        val TYPE: MenuType<SmallVehicleContainerMenu> =
            ExtendedMenuType({ id, inventory, entityId ->
                SmallVehicleContainerMenu(id, inventory, entityId)
            }, ByteBufCodecs.INT)
    }
}
