package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.data.vehicle.subdata.VehicleType;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.atsuishio.portlib.forge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tech.vvp.vvp.network.VVPNetwork;
import tech.vvp.vvp.network.message.PantsirRadarSyncMessage;

import tech.vvp.vvp.entity.projectile.PantsirMissileEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Pantsir-S1 ЗРК - требует захват цели для стрельбы ракетами
 */
public class PantsirS1Entity extends CamoVehicleBase {

    // Синхронизированные данные для автонаведения башни
    private static final EntityDataAccessor<Boolean> AUTO_AIM_ACTIVE = SynchedEntityData.defineId(PantsirS1Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> AIM_DIR_X = SynchedEntityData.defineId(PantsirS1Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_DIR_Y = SynchedEntityData.defineId(PantsirS1Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_DIR_Z = SynchedEntityData.defineId(PantsirS1Entity.class, EntityDataSerializers.FLOAT);

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/pantsir_s1.png")
    };
    
    private static final String[] CAMO_NAMES = {"Standard"};
    
    // Параметры радара
    private static final double RADAR_RANGE = 1100.0;          // Дальность обзорного радара
    private static final double RADAR_TRACK_RANGE = 1000.0;    // Дальность сопровождения
    private static final int LOCK_TIME_TICKS = 40;             // Время захвата (2 секунды) - БАЛАНС: больше времени на уклонение
    
    // Параметры вращающегося радара (синхронизация с анимацией)
    private static final float RADAR_ROTATION_TICKS = 42.5f;   // Тиков на полный оборот
    private static final float RADAR_BEAM_WIDTH = 30.0f;       // Ширина луча обзорного радара
    
    // Состояние радара (серверная сторона)
    private int radarState = PantsirRadarSyncMessage.STATE_IDLE;
    private Entity trackedTarget = null;
    private Entity lockedTarget = null;
    private int lockingProgress = 0;
    private int syncTimer = 0;
    private float radarRotationTicks = 0;  // Угол вращения обзорного радара
    
    // Система потери радиолокационного сопровождения (для GUI)
    private Vec3 lastKnownTargetPos = null;  // Последняя известная позиция цели
    private boolean signalLost = false;      // Флаг потери сигнала
    private int noSignalTicks = 0;           // Счётчик тиков без сигнала
    private static final int MAX_REACQUIRE_TICKS = 40; // 2 секунды на восстановление
    
    // Список всех обнаруженных целей (для отображения на радаре)
    private final List<Entity> detectedTargets = new ArrayList<>();
    private int targetScanTimer = 0;
    private int scanOffset = 0; // Для постепенного сканирования
    
    // Кеш line of sight для оптимизации
    private final java.util.Map<Integer, CachedLineOfSight> losCache = new java.util.HashMap<>();
    
    /**
     * Кешированный результат проверки line of sight
     */
    private static class CachedLineOfSight {
        boolean hasLOS;
        int expiresAt;
        
        CachedLineOfSight(boolean hasLOS, int expiresAt) {
            this.hasLOS = hasLOS;
            this.expiresAt = expiresAt;
        }
    }
    
    // Список выпущенных ракет (для отображения на радаре)
    private final List<PantsirMissileEntity> activeMissiles = new ArrayList<>();
    
    // Кеш оператора (обновляется каждые 10 тиков)
    private LivingEntity cachedOperator = null;
    private int operatorCacheExpiry = 0;
    
    // Throttling звуков предупреждения
    private int soundCooldown = 0;

    public PantsirS1Entity(EntityType<PantsirS1Entity> type, Level world) {
        super(type, world);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AUTO_AIM_ACTIVE, false);
        builder.define(AIM_DIR_X, 0f);
        builder.define(AIM_DIR_Y, 0f);
        builder.define(AIM_DIR_Z, 1f); // По умолчанию смотрим вперёд
    }

    @Override
    public Identifier[] getCamoTextures() {
        return CAMO_TEXTURES;
    }
    
    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity$, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }
    
    /**
     * Переопределяем управление башней - при захвате цели используем автонаведение
     * Работает и на сервере и на клиенте через синхронизированные данные
     */
    @Override
    public void adjustTurretAngle() {
        // Проверяем синхронизированный флаг автонаведения
        if (this.entityData.get(AUTO_AIM_ACTIVE)) {
            // Получаем вектор направления стрельбы из синхронизированных данных
            Vec3 aimDirection = new Vec3(
                this.entityData.get(AIM_DIR_X),
                this.entityData.get(AIM_DIR_Y),
                this.entityData.get(AIM_DIR_Z)
            );
            
            // Передаём вектор направления напрямую в turretAutoAimFromVector
            if (aimDirection.lengthSqr() > 0.001) {
                this.turretAutoAimFromVector(aimDirection);
            }
            return;
        }
        
        // Иначе - стандартное ручное управление
        super.adjustTurretAngle();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        
        if (!this.level().isClientSide()) {
            tickRadar();
        }
    }
    
    /**
     * Серверная логика радара
     */
    private void tickRadar() {
        // Вращаем радар всегда
        radarRotationTicks += 1.0f;
        if (radarRotationTicks >= RADAR_ROTATION_TICKS) {
            radarRotationTicks -= RADAR_ROTATION_TICKS;
        }
        
        // Throttling звуков
        if (soundCooldown > 0) soundCooldown--;
        
        LivingEntity operator = getOperator();
        if (operator == null) {
            resetRadar();
            return;
        }
        
        // Сканируем цели каждые 5 тиков
        targetScanTimer++;
        if (targetScanTimer >= 5) {
            targetScanTimer = 0;
            scanForTargets();
            scanForMissiles();
        }
        
        updateRadarState(operator);
        
        // Синхронизируем с клиентом каждые 2 тика
        syncTimer++;
        if (syncTimer >= 2) {
            syncTimer = 0;
            syncRadarToClient(operator);
        }
    }
    
    /**
     * Сканирует цели в радиусе радара (постепенно, по 4 за раз)
     */
    private void scanForTargets() {
        Vec3 radarPos = this.position().add(0, 2.5, 0);
        
        AABB searchBox = new AABB(radarPos, radarPos).inflate(RADAR_RANGE);
        List<Entity> allEntities = this.level().getEntities(this, searchBox);
        
        // Фильтруем по базовым критериям
        List<Entity> candidates = new ArrayList<>();
        for (Entity entity : allEntities) {
            if (isValidRadarTargetBasic(entity, radarPos)) {
                candidates.add(entity);
            }
        }
        
        // Постепенное сканирование: проверяем LOS только для 4 целей за раз
        int batchSize = 4;
        int startIdx = scanOffset % Math.max(candidates.size(), 1);
        int endIdx = Math.min(startIdx + batchSize, candidates.size());
        
        for (int i = startIdx; i < endIdx; i++) {
            Entity entity = candidates.get(i);
            if (hasLineOfSightCached(entity, radarPos)) {
                if (!detectedTargets.contains(entity)) {
                    detectedTargets.add(entity);
                }
            } else {
                detectedTargets.remove(entity);
            }
        }
        
        detectedTargets.removeIf(e -> !e.isAlive()
                || e.position().distanceTo(radarPos) > RADAR_RANGE
                || !isValidRadarTargetBasic(e, radarPos));

        scanOffset += batchSize;
        if (scanOffset < 0) scanOffset = 0;

        // Очищаем старый кеш
        if (this.tickCount % 100 == 0) {
            losCache.entrySet().removeIf(entry -> entry.getValue().expiresAt < this.tickCount);
        }
    }
    
    /**
     * Базовая проверка цели без line of sight
     */
    private boolean isValidRadarTargetBasic(Entity entity, Vec3 radarPos) {
        if (entity == null || !entity.isAlive()) return false;
        if (entity == this || this.hasPassenger(entity)) return false;
        
        double distance = entity.position().distanceTo(radarPos);
        if (distance > RADAR_RANGE) return false;
        
        // Теги
        EntityType<?> type = entity.getType();
        if (type.builtInRegistryHolder().is(tech.vvp.vvp.init.ModTags.EntityTypes.PANTSIR_AIR_TARGET) ||
            type.builtInRegistryHolder().is(tech.vvp.vvp.init.ModTags.EntityTypes.PANTSIR_MISSILE_TARGET)) {
            return true;
        }
        
        // MissileProjectile
        if (entity instanceof MissileProjectile) {
            return !(entity instanceof PantsirMissileEntity pm) || pm.getLauncherId() != this.getId();
        }
        
        // VehicleEntity
        if (entity instanceof VehicleEntity vehicle) {
            VehicleType vt = vehicle.getVehicleType();
            if (vt == VehicleType.HELICOPTER || vt == VehicleType.AIRPLANE) {
                return true;
            }
        }
        
        // Баллистика (последняя проверка)
        String className = entity.getClass().getSimpleName();
        return className.contains("Missile") || className.contains("Rocket") || className.contains("Bomb");
    }
    
    /**
     * Проверка line of sight с кешем (TTL = 20 тиков)
     */
    private boolean hasLineOfSightCached(Entity entity, Vec3 radarPos) {
        int entityId = entity.getId();
        
        CachedLineOfSight cached = losCache.get(entityId);
        if (cached != null && cached.expiresAt > this.tickCount) {
            return cached.hasLOS;
        }
        
        Vec3 targetPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        boolean hasLOS = hasLineOfSightOptimized(radarPos, targetPos);
        
        losCache.put(entityId, new CachedLineOfSight(hasLOS, this.tickCount + 20));
        
        return hasLOS;
    }
    
    /**
     * Сканирует свои ракеты
     */
    private void scanForMissiles() {
        Vec3 radarPos = this.position().add(0, 2.5, 0);
        activeMissiles.clear();
        
        AABB searchBox = new AABB(radarPos, radarPos).inflate(RADAR_RANGE);
        List<PantsirMissileEntity> missiles = this.level().getEntitiesOfClass(
            PantsirMissileEntity.class, searchBox, 
            m -> m.getLauncherId() == this.getId() && m.isAlive()
        );
        
        activeMissiles.addAll(missiles);
    }
    
    /**
     * Проигрывает звук предупреждения о локе (с throttling)
     */
    private void playLockWarningSound(Entity target, boolean locked) {
        if (target == null || soundCooldown > 0) return;
        
        if (!target.getPassengers().isEmpty() || target instanceof VehicleEntity) {
            target.level().playSound(null, target.getOnPos(), 
                target instanceof Pig ? com.atsuishio.superbwarfare.port.Compat262Kt.legacyPigHurtSound() : 
                    (locked ? ModSounds.LOCKED_WARNING : ModSounds.LOCKING_WARNING), 
                SoundSource.PLAYERS, 2, 1f);
            soundCooldown = locked ? 2 : 3;
        }
    }
    
    /**
     * Получает оператора оружия (с кешем на 10 тиков)
     */
    @Nullable
    private LivingEntity getOperator() {
        if (cachedOperator != null && cachedOperator.isAlive() && operatorCacheExpiry > this.tickCount) {
            return cachedOperator;
        }
        
        List<Entity> passengers = this.getPassengers();
        if (passengers.isEmpty()) {
            cachedOperator = null;
            return null;
        }
        
        for (Entity passenger : passengers) {
            if (passenger instanceof LivingEntity living) {
                int seatIndex = this.getSeatIndex(living);
                if (seatIndex == 1) {
                    cachedOperator = living;
                    operatorCacheExpiry = this.tickCount + 10;
                    return living;
                }
            }
        }
        
        cachedOperator = null;
        return null;
    }
    
    /**
     * Обновляет состояние радара
     */
    private void updateRadarState(LivingEntity operator) {
        Vec3 radarPos = this.position().add(0, 2.5, 0);
        
        switch (radarState) {
            case PantsirRadarSyncMessage.STATE_IDLE:
                trackedTarget = findClosestTarget(radarPos);
                disableAutoAim();
                if (trackedTarget != null) {
                    radarState = PantsirRadarSyncMessage.STATE_DETECTED;
                }
                break;
                
            case PantsirRadarSyncMessage.STATE_DETECTED:
                disableAutoAim();
                if (!isTargetValidForTracking(trackedTarget, radarPos)) {
                    trackedTarget = findClosestTarget(radarPos);
                    if (trackedTarget == null) {
                        radarState = PantsirRadarSyncMessage.STATE_IDLE;
                    }
                }
                break;
                
            case PantsirRadarSyncMessage.STATE_LOCKING:
                if (!isTargetValidForTracking(trackedTarget, radarPos)) {
                    lockingProgress = 0;
                    disableAutoAim();
                    radarState = PantsirRadarSyncMessage.STATE_LOST;
                } else {
                    // Проверяем флаеры
                    Entity decoyNearTarget = findDecoyNearTarget(trackedTarget, 50.0);
                    if (decoyNearTarget != null) {
                        trackedTarget = decoyNearTarget;
                    }
                    
                    lockingProgress++;
                    disableAutoAim();
                    
                    playLockWarningSound(trackedTarget, false);
                    
                    if (lockingProgress >= LOCK_TIME_TICKS) {
                        lockedTarget = trackedTarget;
                        radarState = PantsirRadarSyncMessage.STATE_LOCKED;
                    }
                }
                break;
                
            case PantsirRadarSyncMessage.STATE_LOCKED:
                // Проверяем потерю сигнала
                boolean targetInRange = lockedTarget != null && lockedTarget.isAlive();
                boolean hasLOS = false;
                
                if (targetInRange) {
                    Vec3 targetPos = lockedTarget.position().add(0, lockedTarget.getBbHeight() * 0.5, 0);
                    double distance = radarPos.distanceTo(targetPos);
                    
                    if (distance <= RADAR_TRACK_RANGE) {
                        hasLOS = hasLineOfSightOptimized(radarPos, targetPos);
                    }
                    
                    if (hasLOS) {
                        lastKnownTargetPos = targetPos;
                        signalLost = false;
                        noSignalTicks = 0;
                    } else {
                        if (!signalLost) {
                            signalLost = true;
                            noSignalTicks = 0;
                        }
                        noSignalTicks++;
                        
                        if (noSignalTicks > MAX_REACQUIRE_TICKS) {
                            lockedTarget = null;
                            disableAutoAim();
                            signalLost = false;
                            lastKnownTargetPos = null;
                            radarState = PantsirRadarSyncMessage.STATE_LOST;
                        }
                    }
                }
                
                if (!isTargetValidForLock(lockedTarget, radarPos)) {
                    lockedTarget = null;
                    disableAutoAim();
                    signalLost = false;
                    lastKnownTargetPos = null;
                    radarState = PantsirRadarSyncMessage.STATE_LOST;
                } else if (hasLOS) {
                    updateAutoAimTarget(lockedTarget);
                    playLockWarningSound(lockedTarget, true);
                }
                break;
                
            case PantsirRadarSyncMessage.STATE_LOST:
                disableAutoAim();
                lockingProgress++;
                if (lockingProgress > 20) {
                    lockingProgress = 0;
                    radarState = PantsirRadarSyncMessage.STATE_IDLE;
                }
                break;
        }
    }
    
    // Для вычисления скорости цели по позициям
    private Vec3 lastTargetPos = null;
    private int lastTargetId = -1;
    private Vec3 calculatedTargetVel = Vec3.ZERO;
    
    /**
     * Обновляет синхронизированные данные для автонаведения башни (только на сервере)
     * Использует упреждение для движущихся целей и компенсацию гравитации для пушки
     */
    private void updateAutoAimTarget(Entity target) {
        if (target == null) {
            this.entityData.set(AUTO_AIM_ACTIVE, false);
            lastTargetPos = null;
            lastTargetId = -1;
            return;
        }
        
        // Получаем оператора для определения параметров оружия
        LivingEntity operator = getOperator();
        if (operator == null) {
            this.entityData.set(AUTO_AIM_ACTIVE, false);
            return;
        }
        
        // Позиция цели (центр)
        Vec3 targetPos = target.getBoundingBox().getCenter();
        
        // Позиция откуда стреляем - используем реальную позицию ствола
        Vec3 shootPos = this.getShootPos(operator, 1.0f);
        
        // Проверяем какое оружие выбрано (0 = пушка, 1 = ракеты)
        int seatIndex = this.getSeatIndex(operator);
        if (seatIndex < 0) {
            this.entityData.set(AUTO_AIM_ACTIVE, false);
            return;
        }
        int weaponIndex = this.getSelectedWeapon(seatIndex);
        
        Vec3 aimVector;
        
        if (weaponIndex == 1) {
            // Ракеты - целимся прямо на цель (ракеты сами наводятся)
            aimVector = targetPos.subtract(shootPos).normalize();
        } else {
            // Пушка - вычисляем скорость цели САМИ (getDeltaMovement может врать)
            Vec3 targetVel = getTargetVelocity(target, targetPos);
            
            // Параметры пушки 2А38М из конфига
            double projectileSpeed = 20.0;  // Velocity из конфига
            double gravity = 0.03;          // Gravity из конфига
            
            // Вычисляем баллистическую траекторию
            Vec3 predictedPos = calculateBallisticAimPointForTarget(shootPos, targetPos, targetVel, projectileSpeed, gravity, target);
            
            // Целимся в предсказанную позицию
            aimVector = predictedPos.subtract(shootPos).normalize();
        }
        
        // Обновляем синхронизированные данные (вектор направления)
        this.entityData.set(AUTO_AIM_ACTIVE, true);
        this.entityData.set(AIM_DIR_X, (float) aimVector.x);
        this.entityData.set(AIM_DIR_Y, (float) aimVector.y);
        this.entityData.set(AIM_DIR_Z, (float) aimVector.z);
    }
    
    /**
     * Вычисляет скорость цели по изменению позиции (надёжнее чем getDeltaMovement)
     */
    private Vec3 getTargetVelocity(Entity target, Vec3 currentPos) {
        // Если цель сменилась - сбрасываем
        if (target.getId() != lastTargetId) {
            lastTargetId = target.getId();
            lastTargetPos = currentPos;
            calculatedTargetVel = target.getDeltaMovement(); // Fallback
            return calculatedTargetVel;
        }
        
        // Вычисляем скорость по разнице позиций
        if (lastTargetPos != null) {
            Vec3 newVel = currentPos.subtract(lastTargetPos);
            
            // Сглаживаем скорость (80% новая + 20% старая)
            calculatedTargetVel = newVel.scale(0.8).add(calculatedTargetVel.scale(0.2));
        }
        
        lastTargetPos = currentPos;
        return calculatedTargetVel;
    }
    
    /**
     * Вычисляет точку прицеливания с учётом размера и типа цели
     */
    private Vec3 calculateBallisticAimPointForTarget(Vec3 shootPos, Vec3 targetPos, Vec3 targetVel, 
            double projectileSpeed, double gravity, @Nullable Entity target) {
        
        double distance = shootPos.distanceTo(targetPos);
        double timeToTarget = distance / projectileSpeed;
        
        // Проверяем - это цель из нашего тега (дроны, шахеды)?
        boolean isTaggedTarget = target != null && 
            target.getType().builtInRegistryHolder().is(tech.vvp.vvp.init.ModTags.EntityTypes.PANTSIR_AIR_TARGET);
        
        // Для целей из тега (дроны, шахеды) - простой расчёт
        if (isTaggedTarget) {
            // Время полёта снаряда (в тиках)
            double flightTime = distance / projectileSpeed;
            
            // Предсказываем где будет цель
            Vec3 predictedPos = targetPos.add(targetVel.scale(flightTime));
            
            // Компенсация гравитации
            double gravityComp = 0.5 * gravity * flightTime * flightTime;
            
            return predictedPos.add(0, gravityComp, 0);
        }
        
        // === СТАНДАРТНЫЙ РАСЧЁТ ДЛЯ VehicleEntity (самолёты, вертолёты) ===
        Vec3 aimPoint = targetPos;
        for (int i = 0; i < 5; i++) {
            Vec3 predictedTargetPos = targetPos.add(targetVel.scale(timeToTarget));
            
            Vec3 toTarget = predictedTargetPos.subtract(shootPos);
            double horizontalDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            
            timeToTarget = horizontalDist / projectileSpeed;
            double gravityDrop = 0.5 * gravity * timeToTarget * timeToTarget;
            
            aimPoint = predictedTargetPos.add(0, gravityDrop, 0);
            distance = shootPos.distanceTo(aimPoint);
            timeToTarget = distance / projectileSpeed;
        }
        
        return aimPoint;
    }
    
    /**
     * Отключает автонаведение башни
     */
    private void disableAutoAim() {
        this.entityData.set(AUTO_AIM_ACTIVE, false);
    }
    
    /**
     * Возвращает текущий угол обзорного радара в градусах
     */
    public float getRadarAngle() {
        return -(radarRotationTicks / RADAR_ROTATION_TICKS) * 360.0f;
    }
    
    /**
     * Ищет ближайшую не союзную цель
     */
    @Nullable
    private Entity findClosestTarget(Vec3 radarPos) {
        LivingEntity operator = getOperator();
        
        Entity closest = null;
        double minDistSq = Double.MAX_VALUE;
        
        for (Entity e : detectedTargets) {
            if (!e.isAlive() || isAllyTarget(e, operator)) continue;
            
            double distSq = e.position().distanceToSqr(radarPos);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = e;
            }
        }
        
        return closest;
    }
    
    /**
     * Проверяет валидность цели для отслеживания
     */
    private boolean isTargetValidForTracking(Entity target, Vec3 radarPos) {
        if (target == null || !target.isAlive()) return false;
        double distance = target.position().distanceTo(radarPos);
        if (distance > RADAR_RANGE) return false;
        
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        return hasLineOfSightOptimized(radarPos, targetPos);
    }
    
    /**
     * Оптимизированная проверка line of sight
     * 1. Быстрая проверка высоты (heightmap) — не более 5 точек
     * 2. Если цель низко - упрощённый raycast (3 точки)
     */
    private boolean hasLineOfSightOptimized(Vec3 from, Vec3 to) {
        int fromX = (int) from.x;
        int fromZ = (int) from.z;
        int toX = (int) to.x;
        int toZ = (int) to.z;

        double horizontalDist = Math.sqrt((toX - fromX) * (toX - fromX) + (toZ - fromZ) * (toZ - fromZ));
        // Ограничиваем до 5 шагов — достаточно для выявления гор на 1100 блоков
        int steps = Math.min(5, Math.max(1, (int) (horizontalDist / 50)));

        boolean targetIsHigh = true;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) (fromX + (toX - fromX) * t);
            int z = (int) (fromZ + (toZ - fromZ) * t);

            int highestY = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);

            if (to.y < highestY + 10) {
                targetIsHigh = false;
                break;
            }
        }
        
        // Если цель высоко - пропускаем raycast
        if (targetIsHigh) {
            return true;
        }
        
        // Цель низко - делаем упрощённый raycast (3 точки)
        Vec3 direction = to.subtract(from);
        
        for (double t : new double[]{0.25, 0.5, 0.75}) {
            Vec3 checkPoint = from.add(direction.scale(t));
            BlockPos pos = BlockPos.containing(checkPoint.x, checkPoint.y, checkPoint.z);
            
            net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.isSolidRender()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * КРИТИЧНО: Проверяет что цель в прицеле оператора (в конусе обзора)
     * Это нужно чтобы локинг не останавливался на 2% когда оператор не смотрит на цель
     * @param target цель
     * @param operator оператор (игрок в турели)
     * @param maxAngle максимальный угол отклонения (градусы)
     * @return true если цель в прицеле
     */
    private boolean isTargetInOperatorSight(Entity target, Entity operator, double maxAngle) {
        if (target == null || operator == null) return false;
        
        // Направление взгляда оператора
        Vec3 lookVec = operator.getLookAngle();
        
        // Направление от оператора к цели
        Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0)
                .subtract(operator.position().add(0, operator.getEyeHeight(), 0))
                .normalize();
        
        // Вычисляем угол между направлением взгляда и направлением к цели
        double dot = lookVec.dot(toTarget);
        double angle = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
        
        // Цель в прицеле если угол меньше maxAngle
        return angle <= maxAngle;
    }
    
    /**
     * Проверяет валидность цели для удержания захвата
     */
    private boolean isTargetValidForLock(Entity target, Vec3 radarPos) {
        if (target == null || !target.isAlive()) return false;
        
        double distance = target.position().distanceTo(radarPos);
        return distance <= RADAR_TRACK_RANGE * 1.2;
    }
    
    /**
     * БАЛАНС: Проверяет делает ли цель резкий манёвр
     * Резкие манёвры сбивают захват или замедляют его прогресс
     * @param target цель
     * @return true если цель делает резкий манёвр
     */
    private boolean isTargetManeuvering(Entity target) {
        if (target == null) return false;
        
        // Только для VehicleEntity (самолёты/вертолёты)
        if (!(target instanceof VehicleEntity vehicle)) {
            return false;
        }
        
        // Получаем скорость и ускорение цели
        Vec3 velocity = target.getDeltaMovement();
        double speed = velocity.length();
        
        // Если цель стоит или движется медленно - манёвра нет
        if (speed < 0.3) return false;
        
        // Проверяем угловую скорость (насколько быстро меняется направление)
        // Сравниваем текущее направление с направлением 5 тиков назад
        Vec3 currentDir = velocity.normalize();
        
        // Простая проверка: если цель резко меняет высоту или направление
        // Проверяем вертикальную составляющую скорости
        double verticalSpeed = Math.abs(velocity.y);
        
        // Резкий набор высоты или пикирование (> 0.5 блоков/тик вертикально)
        if (verticalSpeed > 0.5) {
            return true;
        }
        
        // Проверяем горизонтальное ускорение через изменение yaw
        float currentYaw = target.getYRot();
        float oldYaw = target.yRotO;
        float yawDelta = Math.abs(currentYaw - oldYaw);
        
        // Нормализуем угол в диапазон [0, 180]
        if (yawDelta > 180) yawDelta = 360 - yawDelta;
        
        // Резкий поворот (> 15 градусов за тик при высокой скорости)
        if (yawDelta > 15 && speed > 0.5) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Ищет флаер рядом с целью
     */
    @Nullable
    private Entity findDecoyNearTarget(Entity target, double radius) {
        if (target == null) return null;
        
        Vec3 targetPos = target.position();
        AABB searchBox = new AABB(targetPos, targetPos).inflate(radius);
        
        List<Entity> entities = this.level().getEntities(target, searchBox, e -> {
            if (e == null || !e.isAlive()) return false;
            return e.getType().builtInRegistryHolder().is(ModTags.EntityTypes.DECOY);
        });
        
        return entities.stream()
                .min(Comparator.comparingDouble(e -> e.position().distanceTo(targetPos)))
                .orElse(null);
    }
    
    /**
     * Определяет тип цели для отображения иконки на радаре
     */
    private int getTargetType(Entity entity) {
        // MissileProjectile из SBW
        if (entity instanceof MissileProjectile) {
            return PantsirRadarSyncMessage.TARGET_TYPE_MISSILE;
        }
        // Баллистические ракеты и бомбы по имени класса
        String className = entity.getClass().getSimpleName();
        if (className.contains("Missile") || className.contains("Rocket") || className.contains("Bomb")) {
            return PantsirRadarSyncMessage.TARGET_TYPE_MISSILE;
        }
        // Техника
        if (entity instanceof VehicleEntity vehicle) {
            VehicleType type = vehicle.getVehicleType();
            if (type == VehicleType.HELICOPTER) {
                return PantsirRadarSyncMessage.TARGET_TYPE_HELICOPTER;
            }
            if (type == VehicleType.AIRPLANE) {
                return PantsirRadarSyncMessage.TARGET_TYPE_AIRPLANE;
            }
        }
        return PantsirRadarSyncMessage.TARGET_TYPE_UNKNOWN;
    }
    
    /**
     * Синхронизирует состояние радара с клиентом
     */
    private void syncRadarToClient(LivingEntity operator) {
        if (!(operator instanceof ServerPlayer serverPlayer)) return;
        
        // Цель для отображения (tracked или locked)
        Entity radarTarget = (radarState == PantsirRadarSyncMessage.STATE_LOCKED) ? lockedTarget : trackedTarget;
        
        int targetId = (radarTarget != null) ? radarTarget.getId() : -1;
        
        // Координаты цели
        double targetX = (radarTarget != null) ? radarTarget.getX() : 0;
        double targetY = (radarTarget != null) ? radarTarget.getY() + radarTarget.getBbHeight() / 2 : 0;
        double targetZ = (radarTarget != null) ? radarTarget.getZ() : 0;
        
        // Скорость цели (блоков/тик)
        double targetVelX = (radarTarget != null) ? radarTarget.getDeltaMovement().x : 0;
        double targetVelY = (radarTarget != null) ? radarTarget.getDeltaMovement().y : 0;
        double targetVelZ = (radarTarget != null) ? radarTarget.getDeltaMovement().z : 0;
        
        int progress = (radarState == PantsirRadarSyncMessage.STATE_LOCKING) 
                ? (lockingProgress * 100 / LOCK_TIME_TICKS) : 0;
        double distance = (radarTarget != null) ? radarTarget.position().distanceTo(operator.position()) : 0;
        
        // Угол обзорного радара (абсолютный, для кости модели и GUI)
        float radarAngle = getRadarAngle();
        
        // Угол башни (пассивный радар) - вычисляем из вектора направления ствола
        Vec3 barrelDir = this.getBarrelVector(1.0f);
        float turretAngle = (float) Math.toDegrees(-Math.atan2(barrelDir.x, barrelDir.z));
        
        // Собираем все обнаруженные цели для отображения на радаре
        int[] targetIds = new int[Math.min(detectedTargets.size(), 16)]; // Максимум 16 целей
        double[] targetXs = new double[targetIds.length];
        double[] targetYs = new double[targetIds.length];
        double[] targetZs = new double[targetIds.length];
        int[] targetTypes = new int[targetIds.length];
        boolean[] targetIsAlly = new boolean[targetIds.length];
        
        for (int i = 0; i < targetIds.length; i++) {
            Entity target = detectedTargets.get(i);
            targetIds[i] = target.getId();
            targetXs[i] = target.getX();
            targetYs[i] = target.getY() + target.getBbHeight() / 2;
            targetZs[i] = target.getZ();
            targetTypes[i] = getTargetType(target);
            targetIsAlly[i] = isAllyTarget(target, operator);
        }
        
        // Убираем мёртвые/улетевшие ракеты перед отправкой (scanForMissiles обновляется только раз в 5 тиков)
        activeMissiles.removeIf(m -> !m.isAlive());

        // Собираем позиции ракет
        int missileCount = Math.min(activeMissiles.size(), 8); // Максимум 8 ракет
        double[] missileX = new double[missileCount];
        double[] missileY = new double[missileCount];
        double[] missileZ = new double[missileCount];
        
        for (int i = 0; i < missileCount; i++) {
            PantsirMissileEntity missile = activeMissiles.get(i);
            missileX[i] = missile.getX();
            missileY[i] = missile.getY();
            missileZ[i] = missile.getZ();
        }
        
        PantsirRadarSyncMessage message = new PantsirRadarSyncMessage(
            this.getId(), radarState, targetId, targetX, targetY, targetZ, 
            targetVelX, targetVelY, targetVelZ,
            progress, distance, radarAngle, turretAngle,
            targetIds, targetXs, targetYs, targetZs, targetTypes, targetIsAlly,
            missileX, missileY, missileZ,
            signalLost, 
            lastKnownTargetPos != null ? lastKnownTargetPos.x : 0,
            lastKnownTargetPos != null ? lastKnownTargetPos.y : 0,
            lastKnownTargetPos != null ? lastKnownTargetPos.z : 0
        );
        
        VVPNetwork.VVP_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }
    
    /**
     * Проверяет является ли цель союзником
     */
    private boolean isAllyTarget(Entity target, LivingEntity operator) {
        if (operator == null || target == null) return false;
        if (operator.getTeam() == null) return false;
        
        if (operator.isAlliedTo(target)) return true;
        
        // Для VehicleEntity проверяем пассажиров
        if (target instanceof VehicleEntity vehicle) {
            for (Entity passenger : vehicle.getPassengers()) {
                if (passenger instanceof LivingEntity living) {
                    if (operator.isAlliedTo(living)) return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Сбрасывает состояние радара
     */
    private void resetRadar() {
        radarState = PantsirRadarSyncMessage.STATE_IDLE;
        trackedTarget = null;
        lockedTarget = null;
        lockingProgress = 0;
        disableAutoAim();
        // radarRotationTicks продолжает вращаться даже без оператора
    }
    
    /**
     * Запрос захвата цели
     */
    public void requestLock(LivingEntity operator) {
        if (radarState == PantsirRadarSyncMessage.STATE_DETECTED && trackedTarget != null) {
            if (isAllyTarget(trackedTarget, operator)) {
                return;
            }
            radarState = PantsirRadarSyncMessage.STATE_LOCKING;
            lockingProgress = 0;
        }
    }
    
    /**
     * Отмена захвата цели
     */
    public void cancelLock(LivingEntity operator) {
        if (radarState == PantsirRadarSyncMessage.STATE_LOCKING || 
            radarState == PantsirRadarSyncMessage.STATE_LOCKED) {
            lockedTarget = null;
            lockingProgress = 0;
            radarState = PantsirRadarSyncMessage.STATE_DETECTED;
        }
    }
    
    /**
     * Выбирает следующую цель (пропускает союзников)
     */
    public void selectNextTarget() {
        if (detectedTargets.isEmpty()) return;
        if (radarState == PantsirRadarSyncMessage.STATE_LOCKING || 
            radarState == PantsirRadarSyncMessage.STATE_LOCKED) return;
        
        LivingEntity operator = getOperator();
        
        int currentIndex = -1;
        if (trackedTarget != null) {
            currentIndex = detectedTargets.indexOf(trackedTarget);
        }
        
        // Если текущая цель не в списке, начинаем с конца (симметрично selectPrevTarget)
        if (currentIndex < 0) currentIndex = detectedTargets.size() - 1;

        // Ищем следующую не союзную цель
        int startIndex = currentIndex;
        int nextIndex = currentIndex;
        int checked = 0;
        do {
            nextIndex = (nextIndex + 1) % detectedTargets.size();
            if (++checked > detectedTargets.size()) return; // все цели — союзники, выходим
            Entity candidate = detectedTargets.get(nextIndex);
            if (!isAllyTarget(candidate, operator)) {
                trackedTarget = candidate;
                if (radarState == PantsirRadarSyncMessage.STATE_IDLE) {
                    radarState = PantsirRadarSyncMessage.STATE_DETECTED;
                }
                return;
            }
        } while (nextIndex != startIndex && nextIndex != currentIndex);
    }
    
    /**
     * Выбирает предыдущую цель (пропускает союзников)
     */
    public void selectPrevTarget() {
        if (detectedTargets.isEmpty()) return;
        if (radarState == PantsirRadarSyncMessage.STATE_LOCKING || 
            radarState == PantsirRadarSyncMessage.STATE_LOCKED) return;
        
        LivingEntity operator = getOperator();
        
        int currentIndex = -1;
        if (trackedTarget != null) {
            currentIndex = detectedTargets.indexOf(trackedTarget);
        }
        
        // Если текущая цель не в списке, начинаем с конца
        if (currentIndex < 0) currentIndex = 0;
        
        // Ищем предыдущую не союзную цель
        int startIndex = currentIndex;
        int prevIndex = currentIndex;
        int checked = 0;
        do {
            prevIndex = prevIndex - 1;
            if (prevIndex < 0) prevIndex = detectedTargets.size() - 1;
            if (++checked > detectedTargets.size()) return; // все цели — союзники, выходим
            Entity candidate = detectedTargets.get(prevIndex);
            if (!isAllyTarget(candidate, operator)) {
                trackedTarget = candidate;
                if (radarState == PantsirRadarSyncMessage.STATE_IDLE) {
                    radarState = PantsirRadarSyncMessage.STATE_DETECTED;
                }
                return;
            }
        } while (prevIndex != startIndex && prevIndex != currentIndex);
    }
    
    /**
     * Возвращает захваченную цель
     */
    @Nullable
    public Entity getLockedTarget() {
        return lockedTarget;
    }
    
    /**
     * Проверяет захвачена ли цель
     */
    public boolean hasLockedTarget() {
        return radarState == PantsirRadarSyncMessage.STATE_LOCKED && lockedTarget != null;
    }

    /**
     * SBW 0.8.9 can NPE in {@code AmmoConsumer.init} when item registries are unavailable
     * (e.g. disconnect) while {@code ClientEventHandler#handleVehicleFire} still ticks.
     */
    private boolean safeCanShoot(LivingEntity living) {
        if (living == null || !living.isAlive() || this.isRemoved()) {
            return false;
        }
        try {
            return super.canShoot(living);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Блокирует стрельбу ракетами без захвата цели
     */
    @Override
    public boolean canShoot(LivingEntity living) {
        int seatIndex = getSeatIndex(living);
        int weaponIndex = getSelectedWeapon(seatIndex);

        if (weaponIndex == 1) {
            if (this.level().isClientSide()) {
                return isTargetLocked() && safeCanShoot(living);
            }
            return hasLockedTarget() && safeCanShoot(living);
        }

        return safeCanShoot(living);
    }
    
    /**
     * Проверяет захват на клиенте
     */
    @Environment(EnvType.CLIENT)
    private boolean isTargetLocked() {
        return tech.vvp.vvp.client.PantsirClientHandler.isTargetLocked(this.getId());
    }

    /**
     * Блокирует стрельбу ракетами без захвата на сервере
     */
    @Override
    public void vehicleShoot(@Nullable LivingEntity living, @Nullable UUID uuid, @Nullable Vec3 targetPos) {
        if (living == null) {
            try {
                super.vehicleShoot(living, uuid, targetPos);
            } catch (RuntimeException ignored) {
            }
            return;
        }

        int seatIndex = getSeatIndex(living);
        int weaponIndex = getSelectedWeapon(seatIndex);

        if (weaponIndex == 0) {
            try {
                super.vehicleShoot(living, uuid, targetPos);
            } catch (RuntimeException ignored) {
            }
            return;
        }

        if (weaponIndex == 1) {
            if (hasLockedTarget() && lockedTarget.isAlive()) {
                UUID targetUuid = lockedTarget.getUUID();
                Vec3 targetPosition = lockedTarget.position().add(0, lockedTarget.getBbHeight() / 2, 0);
                try {
                    super.vehicleShoot(living, targetUuid, targetPosition);
                } catch (RuntimeException ignored) {
                }
            }
            return;
        }

        try {
            super.vehicleShoot(living, uuid, targetPos);
        } catch (RuntimeException ignored) {
        }
    }
}
