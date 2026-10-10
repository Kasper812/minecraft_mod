package frontline.combat.fcp.client.model.Projectile.Hellfire;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class LockOnHellfireModel extends GeoModel<LockOnHellfireEntity> {

    @Override
    public Identifier getAnimationResource(LockOnHellfireEntity entity) {
        return Identifier.fromNamespaceAndPath("superbwarfare", "animations/javelin.animation.json");
    }

    @Override
    public Identifier getModelResource(LockOnHellfireEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "geo/hellfire.geo.json");
    }

    @Override
    public Identifier getTextureResource(LockOnHellfireEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "textures/entity/hellfire/hellfire.png");
    }
}
