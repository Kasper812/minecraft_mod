package cn.sh1rocu.simplebedrockmodel.client.port;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class BuiltinItemRendererRegistry {
    public static final BuiltinItemRendererRegistry INSTANCE = new BuiltinItemRendererRegistry();

    private final Map<Item, DynamicItemRenderer> renderers = new IdentityHashMap<>();

    private BuiltinItemRendererRegistry() {
    }

    public void register(ItemLike item, DynamicItemRenderer renderer) {
        renderers.put(item.asItem(), renderer);
    }

    public @Nullable DynamicItemRenderer get(ItemLike item) {
        return renderers.get(item.asItem());
    }
}
