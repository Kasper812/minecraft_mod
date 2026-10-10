package tech.vvp.vvp.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import tech.vvp.vvp.VVP;

/**
 * Теги для VVP мода
 */
/**
 * Теги для VVP мода
 * Используются для добавления кастомных сущностей из других модов
 */
public class ModTags {
    
    public static class EntityTypes {
        /**
         * Воздушные цели для радара Панциря из ДРУГИХ модов (не SBW/VVP)
         * VehicleEntity с типом HELICOPTER/AIRPLANE работают автоматически!
         * Этот тег нужен только для кастомных сущностей которые НЕ наследуются от VehicleEntity
         * 
         * Пример: вертолёты из MCHeli, самолёты из Flan's Mod и т.д.
         */
        public static final TagKey<EntityType<?>> PANTSIR_AIR_TARGET = modEntityTag("pantsir_air_target");
        
        /**
         * НЕ ИСПОЛЬЗУЕТСЯ - оставлен для совместимости
         * Панцирь это ПВО, он не стреляет по наземной технике
         */
        public static final TagKey<EntityType<?>> PANTSIR_GROUND_TARGET = modEntityTag("pantsir_ground_target");
        
        /**
         * Ракеты и снаряды из ДРУГИХ модов которые можно сбивать Панцирём
         * MissileProjectile из SBW работают автоматически!
         * Этот тег нужен только для кастомных ракет из других модов
         * 
         * Пример: ракеты из ICBM, снаряды из других военных модов
         */
        public static final TagKey<EntityType<?>> PANTSIR_MISSILE_TARGET = modEntityTag("pantsir_missile_target");
    }
    
    private static TagKey<EntityType<?>> modEntityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(VVP.MOD_ID, name));
    }
}
