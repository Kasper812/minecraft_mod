package tech.vvp.vvp.entity.vehicle;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


public class Mi8Entity extends CamoVehicleBase {

    private static final Identifier[] CAMO_TEXTURES = {
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_default.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_pepeshneyna.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_rf2.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_rf3.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_rf4.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_ukr.png"),
        Identifier.fromNamespaceAndPath("vvp", "textures/entity/mi8_ukr2.png")
    };

    private static final String[] CAMO_NAMES = {"Default", "Pepeshneyna", "RF2", "RF3", "RF4", "Ukraine", "Ukraine2"};


    public Mi8Entity(EntityType<Mi8Entity> type, Level world) {
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


}
