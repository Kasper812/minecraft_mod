package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Leopard2A7VEntity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/leopard_2a7v.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/leopard_2a7v_camo.png")
    };
    
    private static final String[] CAMO_NAMES = {"Standard", "Camo"};

    public Leopard2A7VEntity(EntityType<Leopard2A7VEntity> type, Level world) {
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
