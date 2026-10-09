package com.atsuishio.superbwarfare.data.loot

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod
import com.atsuishio.superbwarfare.tools.toKxJson
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.entity.EntityType

object WreckageLootDataManager : SimpleJsonResourceReloadListener<JsonElement>(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("sbw/loot")) {
    private val data: MutableMap<Identifier, WreckageLootData> = mutableMapOf()

    override fun apply(
        pObject: Map<Identifier, JsonElement>,
        pResourceManager: ResourceManager,
        pProfiler: ProfilerFiller
    ) {
        data.clear()
        pObject.forEach { (id, json) ->
            try {
                val obj = json.asJsonObject
                val json = Json.decodeFromJsonElement<WreckageLootData>(obj.toKxJson())
                data[id] = json
            } catch (_: Exception) {
                Mod.LOGGER.error("Failed to load wreckage loot data for {}", id)
            }
        }
    }
    private val FABRIC_ID: Identifier = Mod.loc("wreckage_loot")

    fun getLootData(id: Identifier): WreckageLootData? {
        return data[id]
    }

    fun getLootData(type: EntityType<*>): WreckageLootData? {
        return data[BuiltInRegistries.ENTITY_TYPE.getKey(type)]
    }

    @JvmStatic
    fun register() {
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(FABRIC_ID, this)
    }
}
