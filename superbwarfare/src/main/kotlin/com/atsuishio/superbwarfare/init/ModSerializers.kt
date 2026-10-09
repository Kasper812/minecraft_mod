package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.Mod
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityDataRegistry

import com.atsuishio.superbwarfare.port.*

import com.atsuishio.superbwarfare.data.gun.GunData
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.phys.Vec3

object ModSerializers {

    @JvmField
    val INT_LIST_SERIALIZER: EntityDataSerializer<List<Int>> =
        EntityDataSerializer.forValueType(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())
        )

    @JvmField
    val FLOAT_LIST_SERIALIZER: EntityDataSerializer<List<Float>> =
        EntityDataSerializer.forValueType(
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list())
        )

    @JvmField
    val VEC3_SERIALIZER: EntityDataSerializer<Vec3> =
        EntityDataSerializer.forValueType(
            object : StreamCodec<ByteBuf, Vec3> {
                override fun decode(buf: ByteBuf) = Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())

                override fun encode(buf: ByteBuf, value: Vec3) {
                    buf.writeDouble(value.x)
                    buf.writeDouble(value.y)
                    buf.writeDouble(value.z)
                }
            }
        )

    @JvmField
    val VEHICLE_GUN_DATA_MAP_SERIALIZER: EntityDataSerializer<Map<String, GunData>> =
        object : EntityDataSerializer<Map<String, GunData>> {
            override fun codec(): StreamCodec<in RegistryFriendlyByteBuf, Map<String, GunData>> {
                return ByteBufCodecs.map(
                    { HashMap(it) },
                    ByteBufCodecs.STRING_UTF8,
                    GunData.VEHICLE_GUN_STREAM_CODEC
                )
            }

            override fun copy(map: Map<String, GunData>): Map<String, GunData> {
                val newMap = HashMap<String, GunData>()
                map.forEach { (key: String, value: GunData) -> newMap[key] = value.copy() }
                return newMap
            }
        }

    @JvmField
    val SHORT_LIST_LIST_SERIALIZER: EntityDataSerializer<List<List<Short>>> =
        EntityDataSerializer.forValueType(
            ByteBufCodecs.SHORT.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list())
        )

    @JvmField
    val OPTIONAL_UUID: EntityDataSerializer<java.util.Optional<java.util.UUID>> =
        EntityDataSerializer.forValueType(
            net.minecraft.core.UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional)
        )

    @JvmField
    val VECTOR3F: EntityDataSerializer<org.joml.Vector3f> =
        EntityDataSerializer.forValueType(
            ByteBufCodecs.VECTOR3F.map({ org.joml.Vector3f(it) }, { it })
        )

    @JvmField
    val QUATERNIONF: EntityDataSerializer<org.joml.Quaternionf> =
        EntityDataSerializer.forValueType(
            ByteBufCodecs.QUATERNIONF.map({ org.joml.Quaternionf(it) }, { it })
        )

    @JvmField
    val COMPOUND_TAG: EntityDataSerializer<net.minecraft.nbt.CompoundTag> =
        object : EntityDataSerializer<net.minecraft.nbt.CompoundTag> {
            override fun codec(): StreamCodec<in RegistryFriendlyByteBuf, net.minecraft.nbt.CompoundTag> = ByteBufCodecs.TRUSTED_COMPOUND_TAG

            override fun copy(value: net.minecraft.nbt.CompoundTag): net.minecraft.nbt.CompoundTag = value.copy()
        }

    @JvmStatic
    fun init() {
        FabricEntityDataRegistry.register(Mod.loc("int_list"), INT_LIST_SERIALIZER)
        FabricEntityDataRegistry.register(Mod.loc("float_list"), FLOAT_LIST_SERIALIZER)
        FabricEntityDataRegistry.register(Mod.loc("vec3"), VEC3_SERIALIZER)
        FabricEntityDataRegistry.register(Mod.loc("vehicle_gun_data_map"), VEHICLE_GUN_DATA_MAP_SERIALIZER)
        FabricEntityDataRegistry.register(Mod.loc("short_list_list"), SHORT_LIST_LIST_SERIALIZER)
        FabricEntityDataRegistry.register(Mod.loc("optional_uuid"), OPTIONAL_UUID)
        FabricEntityDataRegistry.register(Mod.loc("vector3f"), VECTOR3F)
        FabricEntityDataRegistry.register(Mod.loc("compound_tag"), COMPOUND_TAG)
        FabricEntityDataRegistry.register(Mod.loc("quaternionf"), QUATERNIONF)
    }
}
