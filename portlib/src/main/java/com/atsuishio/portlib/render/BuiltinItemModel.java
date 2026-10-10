package com.atsuishio.portlib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BuiltinItemModel implements ItemModel {
    public static final Identifier TYPE = Identifier.fromNamespaceAndPath("sbw_portlib", "builtin");
    private static final Vector3fc[] DEFAULT_EXTENTS = new Vector3fc[]{
            new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)
    };
    private static final Supplier<Vector3fc[]> EXTENTS = () -> DEFAULT_EXTENTS;
    private static final ThreadLocal<ItemOwner> CURRENT_OWNER = new ThreadLocal<>();

    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    private BuiltinItemModel(ModelRenderProperties properties, Matrix4fc transformation) {
        this.properties = properties;
        this.transformation = transformation;
    }

    public static void register() {
        ItemModels.ID_MAPPER.put(TYPE, Unbaked.MAP_CODEC);
    }

    public static @Nullable ItemOwner currentOwner() {
        return CURRENT_OWNER.get();
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        output.setAnimated();
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        if (item.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        }
        layer.setExtents(EXTENTS);
        layer.setLocalTransform(this.transformation);
        layer.setupSpecialModel(Renderer.INSTANCE, new Argument(item, displayContext, owner));
        this.properties.applyToLayer(layer, displayContext);
    }

    public record Argument(ItemStack stack, ItemDisplayContext context, @Nullable ItemOwner owner) {
    }

    public static final class Renderer implements SpecialModelRenderer<Argument> {
        static final Renderer INSTANCE = new Renderer();

        @Override
        public void submit(@Nullable Argument argument, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay,
                           boolean hasFoil, int outlineColor) {
            if (argument == null) {
                return;
            }
            DynamicItemRenderer renderer = BuiltinItemRendererRegistry.INSTANCE.get(argument.stack());
            if (renderer == null) {
                return;
            }
            SubmitBufferSource buffers = new SubmitBufferSource(collector);
            CURRENT_OWNER.set(argument.owner());
            poseStack.pushPose();
            boolean gui = argument.context() == ItemDisplayContext.GUI;
            if (gui) {
                SbmRenderContext.pushGui();
            }
            try {
                renderer.render(argument.stack(), argument.context(), poseStack, buffers, light, overlay);
            } finally {
                if (gui) {
                    SbmRenderContext.popGui();
                }
                poseStack.popPose();
                CURRENT_OWNER.remove();
                buffers.flush();
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (Vector3fc v : DEFAULT_EXTENTS) {
                output.accept(v);
            }
        }

        @Override
        public @Nullable Argument extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base)
        ).apply(i, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(this.base);
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots());
            return new BuiltinItemModel(properties, transformation);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
