package com.atsuishio.superbwarfare.world.saveddata

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.config.server.VehicleConfig
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.TicketType
import net.minecraft.util.Unit
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.saveddata.SavedData

class ChunkPosSavedData : SavedData() {
    val chunkPositions = mutableSetOf<ChunkPos>()

    fun save(tag: CompoundTag, registries: HolderLookup.Provider? = null): CompoundTag {
        tag.put("Pos", this.savePos())
        return tag
    }

    fun savePos(): ListTag {
        val tag = ListTag()
        for (pos in chunkPositions) {
            tag.add(CompoundTag().also {
                it.putInt("X", pos.x)
                it.putInt("Z", pos.z)
            })
        }
        return tag
    }

    fun loadPos(tag: ListTag) {
        val list = mutableListOf<ChunkPos>()
        for (i in 0 until tag.size) {
            val pos = tag.getCompoundOrEmpty(i)
            list.add(ChunkPos(pos.getIntOr("X", 0), pos.getIntOr("Z", 0)))
        }
        this.chunkPositions.addAll(list)
    }

    fun clearPos() {
        this.chunkPositions.clear()
    }

    companion object {
        @JvmField
        val TYPE: net.minecraft.world.level.saveddata.SavedDataType<ChunkPosSavedData> = legacySavedDataType(
            "superbwarfare_chunk_pos",
            { ChunkPosSavedData() },
            { load(it) },
            { it.save(net.minecraft.nbt.CompoundTag()) }
        )

        const val FILE_ID: String = "superbwarfare_chunk_pos"

        fun load(tag: CompoundTag, registries: HolderLookup.Provider? = null): ChunkPosSavedData {
            val savedData = ChunkPosSavedData()
            if (tag.contains("Pos")) {
                savedData.loadPos(tag.getListOrEmpty("Pos"))
            }
            return savedData
        }

        @JvmStatic
        fun register() {
            ServerLifecycleEvents.SERVER_STARTED.register { server ->
                if (!VehicleConfig.VEHICLE_CHUNK_LOADING.get()) return@register

                for (level in server.allLevels) {
                    val data = level.dataStorage.get(
                        TYPE) ?: continue

                    val posSet = data.chunkPositions
                    if (posSet.isEmpty()) continue

                    for (pos in posSet) {
                        level.chunkSource.addTicketWithRadius(com.atsuishio.superbwarfare.init.ModTicketTypes.VEHICLE_START, pos, 3)
                    }

                    data.clearPos()
                    data.setDirty()
                }
            }

            ServerLifecycleEvents.SERVER_STOPPING.register { server ->
                if (!VehicleConfig.VEHICLE_CHUNK_LOADING.get()) return@register

                for (level in server.allLevels) {
                    val data = level.dataStorage.computeIfAbsent(
                        TYPE)

                    val list = level.allEntities
                        .asSequence()
                        .filterIsInstance<VehicleEntity>()
                        .filter { it.computed().keepChunkLoaded }
                        .map { it.chunkPosition() }
                        .toList()

                    if (list.isEmpty()) continue

                    data.chunkPositions.addAll(list)
                    data.setDirty()
                }
            }
        }
    }
}
