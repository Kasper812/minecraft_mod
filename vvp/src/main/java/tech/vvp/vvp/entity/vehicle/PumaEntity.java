package tech.vvp.vvp.entity.vehicle;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PumaEntity extends CamoVehicleBase {

    // Текстуры камуфляжей
    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/puma_green.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/puma_haki.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/puma_pink.png")
    };
    
    // Названия камуфляжей
    private static final String[] CAMO_NAMES = {"Green", "Haki", "Pink"};

    public PumaEntity(EntityType<PumaEntity> type, Level world) {
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

