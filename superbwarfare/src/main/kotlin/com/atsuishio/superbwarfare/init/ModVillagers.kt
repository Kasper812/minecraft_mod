package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.Mod
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PoiHelper
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.entity.npc.villager.VillagerProfession
import net.minecraft.world.item.trading.TradeSet

object ModVillagers {
    @JvmField
    val ARMORY_POI: PoiType = PoiHelper.register(Mod.loc("armory"), 1, 1, ModBlocks.REFORGING_TABLE)

    private fun tradeSet(level: Int): ResourceKey<TradeSet> =
        ResourceKey.create(Registries.TRADE_SET, Mod.loc("armory/level_$level"))

    @JvmField
    val ARMORY: VillagerProfession = Registry.register(
        BuiltInRegistries.VILLAGER_PROFESSION,
        Mod.loc("armory"),
        VillagerProfession(
            Component.translatable("entity.minecraft.villager.superbwarfare.armory"),
            { it.value() === ARMORY_POI },
            { it.value() === ARMORY_POI },
            ImmutableSet.of(),
            ImmutableSet.of(),
            null,
            Int2ObjectMap.ofEntries(
                Int2ObjectMap.entry(1, tradeSet(1)),
                Int2ObjectMap.entry(2, tradeSet(2)),
                Int2ObjectMap.entry(3, tradeSet(3)),
                Int2ObjectMap.entry(4, tradeSet(4)),
                Int2ObjectMap.entry(5, tradeSet(5))
            )
        )
    )

    @JvmStatic
    fun init() {
    }
}
