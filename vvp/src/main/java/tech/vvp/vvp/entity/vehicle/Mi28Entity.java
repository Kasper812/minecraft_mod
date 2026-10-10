package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


public class Mi28Entity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi28_black.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi28_camo.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi28_iraq.png")
    };
    
    private static final String[] CAMO_NAMES = {"Black", "Camo", "Iraq"};

    @Override
    public Identifier[] getCamoTextures() {
        return CAMO_TEXTURES;
    }
    
    @Override
    public String[] getCamoNames() {
        return CAMO_NAMES;
    }


    public Mi28Entity(EntityType<Mi28Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity$, source, damage) -> getSourceAngle(source, 0.4f) * damage);
    }


}
