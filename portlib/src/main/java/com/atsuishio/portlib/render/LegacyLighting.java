package com.atsuishio.portlib.render;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Lighting;

public final class LegacyLighting {
    private LegacyLighting() {
    }

    private static void setup(Lighting.Entry entry) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(entry);
    }

    public static void setupFor3DItems() {
        setup(Lighting.Entry.ITEMS_3D);
    }

    public static void setupForFlatItems() {
        setup(Lighting.Entry.ITEMS_FLAT);
    }

    public static void setupForEntityInInventory() {
        setup(Lighting.Entry.ENTITY_IN_UI);
    }

    public static void setupLevel() {
        setup(Lighting.Entry.LEVEL);
    }
}
