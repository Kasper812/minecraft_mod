package frontline.combat.fcp.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ModFoods {
    public static final FoodProperties DELICIOUS_SNACK = new FoodProperties.Builder().alwaysEdible().nutrition(5)
            .saturationModifier(0.6f).build();

    public static final FoodProperties REDBULL = new FoodProperties.Builder().alwaysEdible().nutrition(5)
            .saturationModifier(0.6f).build();

    public static final Consumable REDBULL_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0), 1.0f))
            .build();
}
