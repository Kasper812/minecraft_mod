package frontline.combat.fcp.client.model.Projectile.Malyutka;

import com.atsuishio.superbwarfare.Mod;
import frontline.combat.fcp.FCP;
import frontline.combat.fcp.entity.projectile.Malyutka.MalyutkaEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class MalyutkaModel extends GeoModel<MalyutkaEntity> {

    @Override
    public Identifier getAnimationResource(MalyutkaEntity entity) {
        return Mod.loc("animations/javelin.animation.json");
    }

    @Override
    public Identifier getModelResource(MalyutkaEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "geo/malyutka.geo.json");
    }

    @Override
    public Identifier getTextureResource(MalyutkaEntity entity) {
        return Identifier.fromNamespaceAndPath(FCP.MODID, "textures/entity/malyutka/malyutka.png");
    }
}
