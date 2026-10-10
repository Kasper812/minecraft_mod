package tech.vvp.vvp.tiers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import tech.vvp.vvp.VVP;

import java.util.EnumMap;
import java.util.Map;

public final class ModArmorMaterial {
    private static final TagKey<net.minecraft.world.item.Item> REPAIR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("superbwarfare", "cemented_carbide_tool_materials"));

    public static final ArmorMaterial CEMENTED_CARBIDE = create("cemented_carbide", 3, 6, 8, 3, 4.0F, 0.05F);
    public static final ArmorMaterial MULTICAM = create("multicam", 0, 0, 10, 5, 5.0F, 0.1F);
    public static final ArmorMaterial MI28 = create("mi28", 0, 2, 5, 3, 1.0F, 0.0F);
    public static final ArmorMaterial KEPKI = create("kepki", 0, 0, 0, 1, 0.0F, 0.0F);
    public static final ArmorMaterial UKR = create("ukr", 0, 3, 10, 6, 1.5F, 0.2F);
    public static final ArmorMaterial RUS = create("rus", 0, 3, 10, 6, 1.5F, 0.2F);
    public static final ArmorMaterial PMC = create("pmc", 0, 3, 10, 6, 1.5F, 0.2F);

    private ModArmorMaterial() {
    }

    private static ArmorMaterial create(String name, int boots, int leggings, int chest, int helmet, float toughness, float knockbackResistance) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, boots);
        defense.put(ArmorType.LEGGINGS, leggings);
        defense.put(ArmorType.CHESTPLATE, chest);
        defense.put(ArmorType.HELMET, helmet);
        defense.put(ArmorType.BODY, 0);
        return new ArmorMaterial(50, defense, 10, SoundEvents.ARMOR_EQUIP_IRON, toughness, knockbackResistance, REPAIR,
                ResourceKey.create(EquipmentAssets.ROOT_ID, VVP.loc(name)));
    }
}
