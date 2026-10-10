package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class VartaEntity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/varta.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/varta_basic.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/varta_black.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/varta_sandy.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/varta_snow.png")
    };

    private static final String[] CAMO_NAMES = {"Standard", "Basic", "Black", "Sandy", "Snow"};

    public VartaEntity(EntityType<VartaEntity> type, Level world) {
        super(type, world);
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

}
