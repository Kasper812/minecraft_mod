package Aru.Aru.ashvehicle.init.client;

import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public class ClientKeyMappings {
    public static KeyMapping OPEN_COORDINATE_SCREEN;
    public static KeyMapping TOGGLE_POD;
    public static KeyMapping THERMAL_VISION;
    public static KeyMapping VTOL_TOGGLE;
    public static KeyMapping AC130_ORBIT_TOGGLE;

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("ashvehicle", "main"));

    public static void register() {
        OPEN_COORDINATE_SCREEN = new KeyMapping(
                "key.ashvehicle.open_coordinate",
                GLFW.GLFW_KEY_G,  // Changed to G to avoid conflict
                CATEGORY
        );
        KeyMappingHelper.registerKeyMapping(OPEN_COORDINATE_SCREEN);
        
        TOGGLE_POD = new KeyMapping(
                "key.ashvehicle.toggle_pod",
                GLFW.GLFW_KEY_LEFT_CONTROL,  // Ctrl key for pod toggle
                CATEGORY
        );
        KeyMappingHelper.registerKeyMapping(TOGGLE_POD);

        THERMAL_VISION = new KeyMapping(
                "key.ashvehicle.thermalvision",
                GLFW.GLFW_KEY_F,  // Ctrl key for pod toggle
                CATEGORY
        );
        KeyMappingHelper.registerKeyMapping(THERMAL_VISION);

        VTOL_TOGGLE = new KeyMapping(
                "key.ashvehicle.vtol_toggle",
                GLFW.GLFW_KEY_H,  // Ctrl key for pod toggle
                CATEGORY
        );
        KeyMappingHelper.registerKeyMapping(VTOL_TOGGLE);

        AC130_ORBIT_TOGGLE = new KeyMapping(
                "key.ashvehicle.ac130_orbit",
                GLFW.GLFW_KEY_J,
                CATEGORY
        );
        KeyMappingHelper.registerKeyMapping(AC130_ORBIT_TOGGLE);
    }
}

