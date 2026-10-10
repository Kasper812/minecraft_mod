package frontline.combat.fcp.team;

import net.minecraft.core.registries.BuiltInRegistries;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Team;
import com.atsuishio.portlib.forge.ForgeRegistries;


public final class VehicleTeamLock {

    public static final String TEAM_KEY = "fcp_team";

    private VehicleTeamLock() {
    }

    public static String getTeam(VehicleEntity vehicle) {
        CompoundTag data = com.atsuishio.superbwarfare.capability.PersistentDataKt.getPersistentData(vehicle);
        if (data.contains(TEAM_KEY)) {
            String s = data.getStringOr(TEAM_KEY, "");
            return s.isEmpty() ? null : s;
        }
        return null;
    }

    public static void setTeam(VehicleEntity vehicle, String teamName) {
        com.atsuishio.superbwarfare.capability.PersistentDataKt.getPersistentData(vehicle).putString(TEAM_KEY, teamName);
    }

    public static void clearTeam(VehicleEntity vehicle) {
        com.atsuishio.superbwarfare.capability.PersistentDataKt.getPersistentData(vehicle).remove(TEAM_KEY);
    }

    public static String teamNameOf(Entity entity) {
        Team team = entity.getTeam();
        return team == null ? null : team.getName();
    }

    public static boolean isFcpVehicle(VehicleEntity vehicle) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
        return key != null && key.getNamespace().equals("fcp");
    }
}