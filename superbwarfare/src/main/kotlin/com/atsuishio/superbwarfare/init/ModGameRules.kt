package com.atsuishio.superbwarfare.init

import com.atsuishio.superbwarfare.Mod
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder
import net.minecraft.world.level.gamerules.GameRule
import net.minecraft.world.level.gamerules.GameRuleCategory

object ModGameRules {
    @JvmField
    val MOD_RULE_DO_GENERATE_LOOTS: GameRule<Boolean> =
        GameRuleBuilder.forBoolean(true)
            .category(GameRuleCategory.SPAWNING)
            .buildAndRegister(Mod.loc("sbw_do_generate_loots"))

    fun bootstrap() {}
}
