package com.atsuishio.portlib.entity;

import net.minecraft.world.phys.AABB;

public interface CullingBoxProvider {
    AABB sbw_portlib$getCullingBox();
}
