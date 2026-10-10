package frontline.combat.fcp.client.model.Projectile.Sidewinder;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.LockOnHellfireEntity;
import frontline.combat.fcp.entity.projectile.Sidewinder.SidewinderEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class SidewinderModel extends GeoModel<SidewinderEntity> {

    @Override
    public Identifier getAnimationResource(SidewinderEntity entity) {
        return Mod.loc("animations/javelin.animation.json");
    }

    @Override
    public Identifier getModelResource(SidewinderEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "geo/sidewinder.geo.json");
    }

    @Override
    public Identifier getTextureResource(SidewinderEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "textures/entity/sidewinder/sidewinder.png");
    }
}
