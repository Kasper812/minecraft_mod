package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class T72B3MEntity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/t72b3m.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/t72b3m_camo.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/t72b3m_sandy.png")
    };
    
    private static final String[] CAMO_NAMES = {"Green", "Camo", "Sandy"};

    public T72B3MEntity(EntityType<T72B3MEntity> type, Level world) {
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
