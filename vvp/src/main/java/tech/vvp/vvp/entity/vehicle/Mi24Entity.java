package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


public class Mi24Entity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_sand_ru.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_green_ru.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_green_rb.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_sand.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_snow.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_tan.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi24_tan_zov.png")
    };

    private static final String[] CAMO_NAMES = {
        "Sand RU", "Green RU", "Green RB", "Sand", "Snow", "Tan", "Tan ZOV"
    };

    @Override
    public Identifier[] getCamoTextures() {
        return CAMO_TEXTURES;
    }
    
    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }


    public Mi24Entity(EntityType<Mi24Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity$, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }


}
