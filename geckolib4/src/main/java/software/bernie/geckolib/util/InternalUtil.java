package software.bernie.geckolib.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import com.atsuishio.portlib.render.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import com.atsuishio.portlib.render.PortRenderStates;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Internal-only class to store common code mostly for implementation purposes.
 * <p>
 * Shouldn't be used by dependent mods
 */
@ApiStatus.Internal
public class InternalUtil {
    /**
     * Attempt to render a GeckoLib {@link GeoArmorRenderer armor piece} for the given slot
     * <p>
     * This is typically only called by an internal mixin
     *
     * @return true if the armor piece was a GeckoLib armor piece and rendered
     * @param <T> The entity that is rendering its armor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean tryRenderGeoArmorPiece(PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack, EquipmentSlot equipmentSlot, int packedLight, HumanoidRenderState state, HumanoidModel baseModel) {
        final Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

        if (equippable == null || equippable.slot() != equipmentSlot)
            return false;

        final Entity entity = PortRenderStates.entity(state);
        final LivingEntity livingEntity = entity instanceof LivingEntity living ? living : null;
        final GeoArmorRenderer<?> geoArmorRenderer = GeoRenderProvider.of(stack).getGeoArmorRenderer(livingEntity, stack, equipmentSlot, baseModel);

        if (geoArmorRenderer == null)
            return false;

        baseModel.setupAnim(state);
        geoArmorRenderer.young = state.isBaby;
        geoArmorRenderer.crouching = state.isCrouching;
        geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel, null, PortRenderStates.partialTick(state), state.walkAnimationPos, state.walkAnimationSpeed, state.yRot, state.xRot);
        geoArmorRenderer.submit(poseStack, collector, packedLight);

        return true;
    }

    /**
     * Perform an {@link Object#equals(Object)} check on two {@link PatchedDataComponentMap}s,
     * ignoring any GeckoLib stack ids that may be present.
     * <p>
     * This is typically only called by an internal mixin
     */
    public static boolean areComponentsMatchingIgnoringGeckoLibId(PatchedDataComponentMap map1, PatchedDataComponentMap map2) {
        final DataComponentType<Long> stackId = GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get();
        boolean patched = false;

        if (map1.has(stackId)) {
            PatchedDataComponentMap prevMap = map1;
            boolean copyOnWrite = prevMap.copyOnWrite;
            (map1 = map1.copy()).remove(stackId);
            map1.copyOnWrite = copyOnWrite;
            patched = true;
        }

        if (map2.has(stackId)) {
            PatchedDataComponentMap prevMap = map2;
            boolean copyOnWrite = prevMap.copyOnWrite;
            (map2 = map2.copy()).remove(stackId);
            map2.copyOnWrite = copyOnWrite;
            patched = true;
        }

        return patched && Objects.equals(map1, map2);
    }
}
