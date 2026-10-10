package tech.vvp.vvp.item;

import com.atsuishio.portlib.render.DynamicItemRenderer;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import tech.vvp.vvp.client.renderer.item.Shell122mmCasingRenderer;

import java.util.function.Consumer;

public class Shell122mmCasingItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Shell122mmCasingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations for the casing
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private Shell122mmCasingRenderer renderer;

            @Override
            public DynamicItemRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new Shell122mmCasingRenderer();

                return this.renderer;
            }
        });
    }
}
