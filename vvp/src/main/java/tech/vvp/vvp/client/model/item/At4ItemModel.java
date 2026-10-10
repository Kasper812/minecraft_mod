package tech.vvp.vvp.client.model.item;

import com.atsuishio.superbwarfare.client.animation.AnimationHelper;
import com.atsuishio.superbwarfare.client.model.item.CustomGunModel;
import com.atsuishio.superbwarfare.client.overlay.CrossHairOverlay;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import tech.vvp.vvp.item.gun.At4Item;

public class At4ItemModel extends CustomGunModel<At4Item> {

    @Override
    public Identifier getAnimationResource(At4Item animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "animations/at4.animation.json");
    }

    @Override
    public Identifier getModelResource(At4Item animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "geo/at4.geo.json");
    }

    @Override
    public Identifier getTextureResource(At4Item animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "textures/item/at4.png");
    }

    @Override
    public Identifier getLODModelResource(At4Item animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "geo/at4.geo.json");
    }

    @Override
    public Identifier getLODTextureResource(At4Item animatable) {
        return Identifier.fromNamespaceAndPath("vvp", "textures/item/at4.png");
    }

    @Override
    public void setCustomAnimations(At4Item animatable, long instanceId, AnimationState<At4Item> animationState) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (shouldCancelRender(stack, animationState)) return;

        GeoBone gun = getAnimationProcessor().getBone("bone");
        GeoBone at4 = getAnimationProcessor().getBone("at4");

        if (gun == null || at4 == null) return;

        double zt = ClientEventHandler.zoomTime;
        double zp = ClientEventHandler.zoomPos;
        double zpz = ClientEventHandler.zoomPosZ;

        ClientEventHandler.handleShootAnimation(at4, 1, -0.4f, 1.2f, 1.3f, 1, 1, 0.5f, 0.7f);

        CrossHairOverlay.gunRot = at4.getRotZ();

        gun.setPosX(0.91f * (float) zp);
        gun.setPosY(-0.04f * (float) zp - (float) (0.2f * zpz));
        gun.setPosZ(2f * (float) zp + (float) (0.15f * zpz));
        gun.setRotZ(0.45f * (float) zp + (float) (0.02f * zpz));
        gun.setScaleZ(1f - (0.5f * (float) zp));

        ClientEventHandler.gunRootMove(getAnimationProcessor(), 0, 0, 0, true);

        GeoBone camera = getAnimationProcessor().getBone("camera");
        GeoBone main = getAnimationProcessor().getBone("0");

        if (camera == null || main == null) return;

        float numR = (float) (1 - 0.82 * zt);
        float numP = (float) (1 - 0.78 * zt);

        AnimationHelper.handleReloadShakeAnimation(stack, main, camera, numR, numP);
        ClientEventHandler.handleReloadShake(Mth.RAD_TO_DEG * camera.getRotX(), Mth.RAD_TO_DEG * camera.getRotY(), Mth.RAD_TO_DEG * camera.getRotZ());
    }
}
