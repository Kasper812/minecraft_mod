package com.atsuishio.superbwarfare.entity.projectile

import net.minecraft.server.level.ServerLevel

import com.atsuishio.superbwarfare.port.*

import net.minecraft.world.level.storage.ValueOutput

import net.minecraft.world.level.storage.ValueInput

import com.atsuishio.portlib.nbt.LegacyNbt

import com.atsuishio.superbwarfare.entity.getValue
import com.atsuishio.superbwarfare.entity.setValue
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier.Companion.createDefaultModifier
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.level.Level

abstract class DestroyableProjectile : FastThrowableProjectile {
    constructor(pEntityType: EntityType<out ThrowableItemProjectile>, pLevel: Level) : super(pEntityType, pLevel)

    constructor(pEntityType: EntityType<out ThrowableItemProjectile>, pShooter: Entity?, pLevel: Level) : super(
        pEntityType,
        pLevel
    ) {
        this.setOwner(pShooter)
        if (pShooter != null) {
            this.setPos(pShooter.x, pShooter.eyeY - 0.1, pShooter.z)
        }
    }

    var health by HEALTH

    override fun isPickable() = !this.isRemoved

    override fun hurtServer(serverLevel: ServerLevel, source: DamageSource, amount: Float): Boolean {
        val entity = source.directEntity
        if (entity is DestroyableProjectile && this.javaClass == entity.javaClass) {
            if (this.getOwner() == entity.owner) return false
        }

        this.health -= DAMAGE_MODIFIER.compute(this, source, amount)
        return super.hurtServer(serverLevel, source, amount)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(HEALTH, this.maxHealth)
    }

    override fun readAdditionalSaveData(input: ValueInput) {

        val compound = LegacyNbt.tagOf(input)
        super.readAdditionalSaveData(input)
        if (compound.contains("Health")) {
            health = compound.getFloatOr("Health", 0f)
        }
    }

    override fun addAdditionalSaveData(output: ValueOutput) {

        val compound = LegacyNbt.tagOf(output)
        super.addAdditionalSaveData(output)
        compound.putFloat("Health", health)
    }

    open val maxHealth get() = 30F

    companion object {
        @JvmField
        val HEALTH: EntityDataAccessor<Float> =
            SynchedEntityData.defineId(DestroyableProjectile::class.java, EntityDataSerializers.FLOAT)

        private val DAMAGE_MODIFIER = createDefaultModifier()
    }

    override fun tick() {
        super.tick()

        if (health <= 0) {
            if (!level().isClientSide) {
                causeExplode(position())
            }
            this.discard()
        }
    }
}
