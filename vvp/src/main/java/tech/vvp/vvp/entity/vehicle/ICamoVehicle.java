package tech.vvp.vvp.entity.vehicle;

import net.minecraft.resources.Identifier;

/**
 * Interface for vehicles with camouflage support
 */
public interface ICamoVehicle {
    
    int getCamoType();
    
    void setCamoType(int camoType);
    
    void cycleCamo();
    
    Identifier[] getCamoTextures();
    
    String[] getCamoNames();
}
