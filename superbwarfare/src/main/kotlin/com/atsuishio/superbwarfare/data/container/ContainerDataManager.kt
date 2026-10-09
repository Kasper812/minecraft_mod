package com.atsuishio.superbwarfare.data.container

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.Mod
import com.google.gson.Gson
import com.google.gson.JsonElement
import it.unimi.dsi.fastutil.Pair
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

object ContainerDataManager :
    SimpleJsonResourceReloadListener<JsonElement>(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("sbw/containers")) {

    private val containerData: MutableMap<Identifier, MutableList<Pair<String, Int>>> = hashMapOf()
    private val FABRIC_ID: Identifier = Mod.loc("container_data_manager")

    @JvmStatic
    fun register() {
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(FABRIC_ID, this)
    }

    override fun apply(
        objects: Map<Identifier, JsonElement>,
        manager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        containerData.clear()

        objects.forEach { (id, json) ->
            try {
                val obj = json.asJsonObject
                val list: MutableList<Pair<String, Int>> = mutableListOf()
                val array = obj.getAsJsonArray("List")

                for (element in array) {
                    if (element.isJsonObject) {
                        val entry = element.asJsonObject
                        val type = entry.get("Type").asString
                        val weight = entry.get("Weight").asInt

                        list.add(Pair.of(type, weight))
                    } else {
                        list.add(Pair.of(element.asString, 1))
                    }
                }

                containerData[id] = list
            } catch (_: Exception) {
                Mod.LOGGER.error("Failed to load container data for {}", id)
            }
        }
    }

    fun getEntityTypes(id: Identifier): MutableList<Pair<String, Int>> {
        return containerData[id] ?: mutableListOf()
    }
}
