package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class M1A2SepEntity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
            Identifier.fromNamespaceAndPath("vvp", "textures/entity/m1a2.png"),
            Identifier.fromNamespaceAndPath("vvp", "textures/entity/m1a2_sand.png")
    };

    private static final String[] CAMO_NAMES = {"Camo", "Iraq"};

    public M1A2SepEntity(EntityType<M1A2SepEntity> type, Level world) {
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
