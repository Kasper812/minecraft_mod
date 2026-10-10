package com.atsuishio.superbwarfare.port

import com.atsuishio.portlib.entity.LegacyEntityFlags
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import net.minecraft.tags.TagKey
import net.minecraft.util.ARGB
import net.minecraft.util.profiling.Profiler
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.TypedEntityData
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

var Entity.noCulling: Boolean
    get() = (this as LegacyEntityFlags).`sbw_portlib$isNoCulling`()
    set(value) = (this as LegacyEntityFlags).`sbw_portlib$setNoCulling`(value)

fun <T : Any> ResourceKey<T>.location(): Identifier = identifier()

val Entity.server: MinecraftServer? get() = level().server

fun Entity.moveTo(x: Double, y: Double, z: Double, yRot: Float, xRot: Float) = snapTo(x, y, z, yRot, xRot)

fun Entity.moveTo(x: Double, y: Double, z: Double) = snapTo(x, y, z)

fun Entity.moveTo(pos: Vec3) = snapTo(pos)

fun Entity.moveTo(pos: Vec3, yRot: Float, xRot: Float) = snapTo(pos, yRot, xRot)

fun Entity.moveTo(pos: net.minecraft.core.BlockPos, yRot: Float, xRot: Float) = snapTo(pos, yRot, xRot)

fun TypedEntityData<*>.copyTag(): CompoundTag = getUnsafe().copy()

fun EntityType<*>.`is`(tag: TagKey<EntityType<*>>): Boolean = builtInRegistryHolder().`is`(tag)

fun NativeImage.getPixelRGBA(x: Int, y: Int): Int = ARGB.toABGR(getPixel(x, y))

fun NativeImage.setPixelRGBA(x: Int, y: Int, abgr: Int) = setPixelABGR(x, y, abgr)

val Level.profiler: ProfilerFiller get() = Profiler.get()

@JvmName("legacyGetProfiler")
fun Level.getProfiler(): ProfilerFiller = Profiler.get()

fun Level.isNight(): Boolean = isDarkOutside

val Camera.lookVector: org.joml.Vector3fc get() = forwardVector()

@JvmName("legacyGetItems")
fun Ingredient.getItems(): Array<ItemStack> = items().map { ItemStack(it) }.toList().toTypedArray()

val Ingredient.items: Array<ItemStack> get() = getItems()

val ItemStack.descriptionId: String get() = item.descriptionId

fun legacyPermission(level: Int): net.minecraft.server.permissions.Permission = when {
    level >= 4 -> Permissions.COMMANDS_OWNER
    level == 3 -> Permissions.COMMANDS_ADMIN
    level == 2 -> Permissions.COMMANDS_GAMEMASTER
    else -> Permissions.COMMANDS_MODERATOR
}

fun Player.hasPermissions(level: Int): Boolean = level <= 0 || permissions().hasPermission(legacyPermission(level))

fun net.minecraft.commands.CommandSourceStack.hasPermission(level: Int): Boolean =
    level <= 0 || permissions().hasPermission(legacyPermission(level))

fun ServerPlayer.serverLevel(): ServerLevel = level()

fun ChunkPos.toLong(): Long = pack()

fun Item.hasCraftingRemainingItem(): Boolean = craftingRemainder != null

fun Minecraft.setScreen(screen: Screen?) = gui.setScreen(screen)

val Minecraft.screen: Screen? get() = gui.screen()

val Minecraft.timer: DeltaTracker get() = deltaTracker

val Minecraft.mainRenderTarget: com.mojang.blaze3d.pipeline.RenderTarget get() = gameRenderer.mainRenderTarget()

@JvmName("legacyGetMainRenderTarget")
fun Minecraft.getMainRenderTarget(): com.mojang.blaze3d.pipeline.RenderTarget = gameRenderer.mainRenderTarget()

val com.mojang.blaze3d.platform.Window.window: Long get() = handle()

fun legacyParseItem(provider: net.minecraft.core.HolderLookup.Provider, tag: net.minecraft.nbt.Tag): java.util.Optional<ItemStack> =
    ItemStack.CODEC.parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result()

fun legacyParseItemOptional(provider: net.minecraft.core.HolderLookup.Provider, tag: net.minecraft.nbt.Tag?): ItemStack =
    com.atsuishio.portlib.nbt.LegacyNbt.parseItemOptional(provider, tag)

fun entityTypeByString(id: String): java.util.Optional<EntityType<*>> {
    val identifier = Identifier.tryParse(id) ?: return java.util.Optional.empty()
    return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(identifier)
}

fun <T : Entity> EntityType<T>.create(level: Level): T? = create(level, net.minecraft.world.entity.EntitySpawnReason.LOAD)

fun Entity.spawnAtLocation(stack: ItemStack, offset: Float): net.minecraft.world.entity.item.ItemEntity? =
    (level() as? ServerLevel)?.let { spawnAtLocation(it, stack, offset) }

fun Entity.spawnAtLocation(stack: ItemStack): net.minecraft.world.entity.item.ItemEntity? =
    (level() as? ServerLevel)?.let { spawnAtLocation(it, stack) }

fun Entity.spawnAtLocation(item: net.minecraft.world.level.ItemLike): net.minecraft.world.entity.item.ItemEntity? =
    (level() as? ServerLevel)?.let { spawnAtLocation(it, item) }

fun legacyPigHurtSound(): net.minecraft.sounds.SoundEvent =
    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace("entity.pig.hurt"))
        ?: net.minecraft.sounds.SoundEvents.GENERIC_HURT

fun legacyLoadEffect(tag: CompoundTag): net.minecraft.world.effect.MobEffectInstance? =
    net.minecraft.world.effect.MobEffectInstance.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElse(null)

fun net.minecraft.world.effect.MobEffectInstance.save(): net.minecraft.nbt.Tag =
    net.minecraft.world.effect.MobEffectInstance.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, this).result().orElseGet { CompoundTag() }

object LegacyIngredients {
    @JvmField
    val EMPTY: Ingredient = Ingredient.of(
        net.minecraft.core.HolderSet.emptyNamed(
            net.minecraft.core.registries.BuiltInRegistries.ITEM,
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath("superbwarfare", "legacy_empty"))
        )
    )

    @JvmStatic
    fun isEmpty(ingredient: Ingredient?): Boolean = ingredient == null || ingredient === EMPTY || ingredient.isEmpty

    @JvmStatic
    fun test(ingredient: Ingredient?, stack: ItemStack): Boolean = if (isEmpty(ingredient)) stack.isEmpty else ingredient!!.test(stack)

    @JvmField
    val STREAM_CODEC: net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Ingredient> =
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.map(
            { it.orElse(EMPTY) },
            { if (isEmpty(it)) java.util.Optional.empty() else java.util.Optional.of(it) }
        )

    @JvmStatic
    fun upgradeJson(element: com.google.gson.JsonElement): com.google.gson.JsonElement? {
        if (element.isJsonNull) return null
        if (element.isJsonArray) {
            val out = com.google.gson.JsonArray()
            for (e in element.asJsonArray) {
                val up = upgradeJson(e) ?: continue
                if (up.isJsonArray) up.asJsonArray.forEach { out.add(it) } else out.add(up)
            }
            if (out.isEmpty) return null
            if (out.size() == 1) return out[0]
            if (out.any { it.isJsonObject || (it.isJsonPrimitive && it.asString.startsWith("#")) }) {
                val any = com.google.gson.JsonObject()
                any.addProperty("fabric:type", "fabric:any")
                any.add("ingredients", out)
                return any
            }
            return out
        }
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            if (obj.has("item")) return com.google.gson.JsonPrimitive(obj.get("item").asString)
            if (obj.has("tag")) return com.google.gson.JsonPrimitive("#" + obj.get("tag").asString)
            return null
        }
        return element
    }
}

fun net.minecraft.client.renderer.GameRenderer.loadEffect(id: Identifier) {
    val path = id.path.removePrefix("shaders/post/").removeSuffix(".json")
    setPostEffect(Identifier.fromNamespaceAndPath(id.namespace, path))
}

fun net.minecraft.client.renderer.GameRenderer.shutdownEffect() = clearPostEffect()

fun net.minecraft.client.renderer.GameRenderer.currentEffect(): Identifier? = currentPostEffect()

fun <T : net.minecraft.world.level.saveddata.SavedData> legacySavedDataType(
    id: String,
    constructor: () -> T,
    load: (CompoundTag) -> T,
    save: (T) -> CompoundTag
): net.minecraft.world.level.saveddata.SavedDataType<T> =
    com.atsuishio.portlib.util.LegacySavedData.type(
        Identifier.fromNamespaceAndPath("superbwarfare", id.removePrefix("superbwarfare_")),
        java.util.function.Supplier(constructor),
        CompoundTag.CODEC.xmap({ load(it) }, { save(it) })
    )

fun legacySetBlockEntityData(
    stack: ItemStack,
    type: net.minecraft.world.level.block.entity.BlockEntityType<*>,
    tag: CompoundTag
) {
    val copy = tag.copy()
    copy.remove("id")
    if (copy.isEmpty) {
        stack.remove(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA)
    } else {
        stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(type, copy))
    }
}

fun net.minecraft.world.level.block.entity.BlockEntity.legacySaveToItem(stack: ItemStack, registries: net.minecraft.core.HolderLookup.Provider) {
    val output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries)
    saveCustomOnly(output)
    removeComponentsFromTag(output)
    net.minecraft.world.item.BlockItem.setBlockEntityData(stack, type, output)
    stack.applyComponents(collectComponents())
}

fun Entity.load(tag: CompoundTag) = load(com.atsuishio.portlib.nbt.LegacyNbt.inputOf(tag, level().registryAccess()))

fun Entity.saveWithoutId(tag: CompoundTag): CompoundTag {
    val output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level().registryAccess())
    saveWithoutId(output)
    tag.merge(output.buildResult())
    return tag
}

fun Entity.saveAsPassenger(tag: CompoundTag): Boolean {
    val output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level().registryAccess())
    val result = saveAsPassenger(output)
    tag.merge(output.buildResult())
    return result
}

fun net.minecraft.world.level.block.entity.BlockEntity.loadWithComponents(tag: CompoundTag, registries: net.minecraft.core.HolderLookup.Provider) =
    loadWithComponents(com.atsuishio.portlib.nbt.LegacyNbt.inputOf(tag, registries))



fun net.minecraft.world.item.ItemCooldowns.addCooldown(item: net.minecraft.world.item.Item, time: Int) =
    addCooldown(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item), time)

fun net.minecraft.world.item.ItemCooldowns.isOnCooldown(item: net.minecraft.world.item.Item): Boolean =
    isOnCooldown(net.minecraft.world.item.ItemStack(item))

fun net.minecraft.world.item.ItemCooldowns.getCooldownPercent(item: net.minecraft.world.item.Item, a: Float): Float =
    getCooldownPercent(net.minecraft.world.item.ItemStack(item), a)

fun net.minecraft.world.item.ItemCooldowns.removeCooldown(item: net.minecraft.world.item.Item) =
    removeCooldown(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item))

fun Entity.startRiding(vehicle: Entity, force: Boolean): Boolean = startRiding(vehicle, force, true)

fun net.minecraft.world.entity.LivingEntity.knockback(power: Number, xd: Double, zd: Double) =
    knockback(power.toDouble(), xd, zd, damageSources().generic(), 0f)

fun net.minecraft.world.entity.player.Player.sweepAttack() {
    val d0 = (-net.minecraft.util.Mth.sin(yRot * (Math.PI / 180.0))).toDouble()
    val d1 = net.minecraft.util.Mth.cos(yRot * (Math.PI / 180.0)).toDouble()
    val level = level()
    if (level is net.minecraft.server.level.ServerLevel) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, x + d0, getY(0.5), z + d1, 0, d0, 0.0, d1, 0.0)
    }
}

fun Level.playSound(
    except: Entity?,
    pos: net.minecraft.core.BlockPos,
    sound: net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent>,
    source: net.minecraft.sounds.SoundSource,
    volume: Float,
    pitch: Float
) = playSound(except, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, sound, source, volume, pitch)

fun Entity.playSound(sound: net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent>, volume: Float, pitch: Float) =
    playSound(sound.value(), volume, pitch)
