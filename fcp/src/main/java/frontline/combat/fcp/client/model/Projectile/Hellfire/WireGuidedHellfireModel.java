package frontline.combat.fcp.client.model.Projectile.Hellfire;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Hellfire.WireGuidedHellfireEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WireGuidedHellfireModel extends GeoModel<WireGuidedHellfireEntity> {

    @Override
    public Identifier getAnimationResource(WireGuidedHellfireEntity entity) {
        return Mod.loc("animations/javelin.animation.json");
    }

    @Override
    public Identifier getModelResource(WireGuidedHellfireEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "geo/hellfire.geo.json");
    }

    @Override
    public Identifier getTextureResource(WireGuidedHellfireEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "textures/entity/hellfire/hellfire.png");
    }
}
