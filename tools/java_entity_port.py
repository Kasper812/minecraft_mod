import os
import re
import sys

GETTERS = [
    ("getInt", "getIntOr", "0"), ("getFloat", "getFloatOr", "0.0F"), ("getDouble", "getDoubleOr", "0.0D"),
    ("getLong", "getLongOr", "0L"), ("getShort", "getShortOr", "(short) 0"), ("getByte", "getByteOr", "(byte) 0"),
    ("getBoolean", "getBooleanOr", "false"), ("getString", "getStringOr", "\"\""),
]
ARG = r'((?:"[^"\n]*"|[A-Za-z_][\w.]*(?:\(\))?|\d+))'


def find_block(s, start):
    i = s.index("{", start)
    depth = 0
    j = i
    while j < len(s):
        if s[j] == "{":
            depth += 1
        elif s[j] == "}":
            depth -= 1
            if depth == 0:
                return i, j
        j += 1
    return i, len(s) - 1


def add_import(src, imp):
    line = "import %s;" % imp
    if line in src:
        return src
    m = re.search(r"^package [^\n]*\n", src, re.M)
    pos = m.end() if m else 0
    return src[:pos] + "\n" + line + src[pos:]


def synched(s):
    pat = re.compile(r"(protected|public)\s+void\s+defineSynchedData\(\s*\)")
    out = s
    while True:
        m = pat.search(out)
        if not m:
            break
        a, b = find_block(out, m.end())
        body = out[a:b + 1]
        body = re.sub(r"\bsuper\.defineSynchedData\(\s*\)", "super.defineSynchedData(builder)", body)
        body = re.sub(r"\b(?:this\.)?entityData\.define\(", "builder.define(", body)
        body = re.sub(r"\b(?:this\.)?getEntityData\(\)\.define\(", "builder.define(", body)
        out = out[:m.start()] + "%s void defineSynchedData(SynchedEntityData.Builder builder)" % m.group(1) + out[m.end():a] + body + out[b + 1:]
    if "SynchedEntityData.Builder" in out:
        out = add_import(out, "net.minecraft.network.syncher.SynchedEntityData")
    return out


SAVE_METHODS = [
    ("addAdditionalSaveData", "ValueOutput", "net.minecraft.world.level.storage.ValueOutput"),
    ("readAdditionalSaveData", "ValueInput", "net.minecraft.world.level.storage.ValueInput"),
    ("saveAdditional", "ValueOutput", "net.minecraft.world.level.storage.ValueOutput"),
]


def save_methods(s):
    out = s
    for name, typ, imp in SAVE_METHODS:
        pat = re.compile(r"((?:public|protected)\s+void\s+%s)\(\s*(?:@\w+\s+)?(?:final\s+)?(?:net\.minecraft\.nbt\.)?CompoundTag\s+(\w+)\s*\)" % name)
        while True:
            m = pat.search(out)
            if not m:
                break
            var = m.group(2)
            a, b = find_block(out, m.end())
            body = out[a + 1:b]
            body = re.sub(r"\bsuper\.%s\(\s*%s\s*\)" % (name, var), "super.%s(%s$io)" % (name, var), body)
            new_body = "\n        CompoundTag %s = LegacyNbt.tagOf(%s$io);" % (var, var) + body
            out = out[:m.start()] + "%s(%s %s$io)" % (m.group(1), typ, var) + out[m.end():a] + "{" + new_body + "}" + out[b + 1:]
            out = add_import(out, imp)
            out = add_import(out, "com.atsuishio.portlib.nbt.LegacyNbt")
            out = add_import(out, "net.minecraft.nbt.CompoundTag")
    pat = re.compile(r"((?:public|protected)\s+void\s+)load\(\s*(?:final\s+)?CompoundTag\s+(\w+)\s*\)")
    while "extends BlockEntity" in out or "BlockEntity {" in out:
        m = pat.search(out)
        if not m:
            break
        var = m.group(2)
        a, b = find_block(out, m.end())
        body = out[a + 1:b]
        body = re.sub(r"\bsuper\.load\(\s*%s\s*\)" % var, "super.loadAdditional(%s$io)" % var, body)
        new_body = "\n        CompoundTag %s = LegacyNbt.tagOf(%s$io);" % (var, var) + body
        out = out[:m.start()] + "%sloadAdditional(ValueInput %s$io)" % (m.group(1), var) + out[m.end():a] + "{" + new_body + "}" + out[b + 1:]
        out = add_import(out, "net.minecraft.world.level.storage.ValueInput")
        out = add_import(out, "com.atsuishio.portlib.nbt.LegacyNbt")
    out = out.replace("$io", "Io")
    return out


def getters(s):
    for old, new, default in GETTERS:
        s = re.sub(r"\.%s\(%s\)" % (old, ARG), lambda m: ".%s(%s, %s)" % (new, m.group(1), default), s)
    s = re.sub(r"\.getCompound\(%s\)" % ARG, lambda m: ".getCompoundOrEmpty(%s)" % m.group(1), s)
    s = re.sub(r"\.getList\(%s,\s*[\w.]+\)" % ARG, lambda m: ".getListOrEmpty(%s)" % m.group(1), s)
    s = re.sub(r"\.getIntArray\(%s\)" % ARG, lambda m: ".getIntArray(%s).orElseGet(() -> new int[0])" % m.group(1), s)
    s = re.sub(r"\.contains\(%s,\s*(?:\d+|Tag\.TAG_\w+)\)" % ARG, lambda m: ".contains(%s)" % m.group(1), s)
    s = re.sub(r"(\w+)\.hasUUID\(%s\)" % ARG, lambda m: "LegacyNbt.hasUUID(%s, %s)" % (m.group(1), m.group(2)), s)
    s = re.sub(r"(\w+)\.getUUID\(%s\)" % ARG, lambda m: "LegacyNbt.getUUID(%s, %s)" % (m.group(1), m.group(2)), s)
    s = re.sub(r"(\w+)\.putUUID\(%s,\s*" % ARG, lambda m: "LegacyNbt.putUUID(%s, %s, " % (m.group(1), m.group(2)), s)
    if "LegacyNbt." in s:
        s = add_import(s, "com.atsuishio.portlib.nbt.LegacyNbt")
    return s


def misc(s):
    s = re.sub(r"\.isClientSide\b(?!\()", ".isClientSide()", s)
    s = re.sub(r"\.displayClientMessage\(([^;]*?),\s*true\s*\);", r".sendOverlayMessage(\1);", s)
    s = re.sub(r"\.displayClientMessage\(([^;]*?),\s*false\s*\);", r".sendSystemMessage(\1);", s)
    s = re.sub(r"\.custom\(\(\s*(\w+)\s*,\s*(\w+)\s*\)\s*->", r".custom((vvp$e, \1, \2) ->", s).replace("vvp$e", "entity$")
    s = re.sub(r"\bnet\.minecraft\.world\.entity\.animal\.Pig\b", "net.minecraft.world.entity.animal.pig.Pig", s)
    return s


def particle_providers(s):
    out = []
    i = 0
    pat = re.compile(r"(?:public\s+)?(?:@Nullable\s+)?[\w.<>]*Particle\s+createParticle\(")
    while True:
        m = pat.search(s, i)
        if not m:
            out.append(s[i:])
            break
        j = m.end()
        depth = 0
        k = j
        while k < len(s):
            if s[k] == "(":
                depth += 1
            elif s[k] == ")":
                if depth == 0:
                    break
                depth -= 1
            k += 1
        params = s[j:k]
        out.append(s[i:j])
        if params.count(",") == 7 and "RandomSource" not in params:
            out.append(params.rstrip() + ", net.minecraft.util.RandomSource randomSource")
        else:
            out.append(params)
        i = k
    return "".join(out)


SBW_PLAIN = ["ModSounds", "ModItems", "ModEntities", "ModBlocks", "ModBlockEntities", "ModParticleTypes", "ModAttributes",
             "ModPotions", "ModMenuTypes", "ModRecipes", "ModSerializers", "ModDataComponents", "ModMobEffects", "ModEnchantments",
             "ModCriteriaTriggers"]


def sbw_api(s):
    for name in SBW_PLAIN:
        s = re.sub(r"\bcom\.atsuishio\.superbwarfare\.init\.%s\.([A-Z0-9_]+)\.get\(\)" % name, r"com.atsuishio.superbwarfare.init.%s.\1" % name, s)
        if re.search(r"^import com\.atsuishio\.superbwarfare\.init\.%s;" % name, s, re.M):
            s = re.sub(r"(?<![\w.])%s\.([A-Z0-9_]+)\.get\(\)" % name, r"%s.\1" % name, s)
    return s


def mc_misc(s):
    s = re.sub(r"\.getEntityReach\(\)", ".entityInteractionRange()", s)
    s = re.sub(r"\.isSolidRender\([^()]*(?:\([^()]*\))?[^()]*\)", ".isSolidRender()", s)
    s = re.sub(r"\bgetFrameTime\(\)", "getDeltaTracker().getGameTimeDeltaPartialTick(false)", s)
    s = re.sub(r"\.getWindow\(\)\.getWindow\(\)", ".getWindow().handle()", s)
    s = re.sub(r"\.getType\(\)\.is\(", ".getType().builtInRegistryHolder().is(", s)
    s = re.sub(r"\bSoundEvents\.PIG_HURT\b", "com.atsuishio.superbwarfare.port.Compat262Kt.legacyPigHurtSound()", s)
    s = re.sub(r"\.getKey\(\)\.getDisplayName\(\)", ".getTranslatedKeyMessage()", s)
    return s


def entity_overrides(s):
    pat = re.compile(r"(InteractionResult\s+interact\(\s*(?:@\w+\s+)?(?:final\s+)?Player\s+(\w+)\s*,\s*(?:@\w+\s+)?(?:final\s+)?InteractionHand\s+(\w+))\s*\)")
    out = s
    pos = 0
    while True:
        m = pat.search(out, pos)
        if not m:
            break
        a, b = find_block(out, m.end())
        body = out[a:b + 1]
        body = re.sub(r"\bsuper\.interact\(\s*%s\s*,\s*%s\s*\)" % (m.group(2), m.group(3)), "super.interact(%s, %s, interactLocation)" % (m.group(2), m.group(3)), body)
        head = m.group(1) + ", net.minecraft.world.phys.Vec3 interactLocation)"
        out = out[:m.start()] + head + out[m.end():a] + body + out[b + 1:]
        pos = m.start() + len(head)
    out = re.sub(r"public boolean canBeCollidedWith\(\s*\)", "public boolean canBeCollidedWith(net.minecraft.world.entity.Entity collisionOther)", out)
    out = re.sub(r"\bsuper\.canBeCollidedWith\(\s*\)", "super.canBeCollidedWith(collisionOther)", out)
    return out


def hover_text(s):
    pat = re.compile(r"public\s+void\s+appendHoverText\(\s*(?:@\w+\s+)*(?:final\s+)?ItemStack\s+(\w+)\s*,\s*(?:@\w+\s+)*(?:final\s+)?Level\s+(\w+)\s*,\s*(?:@\w+\s+)*(?:final\s+)?List<Component>\s+(\w+)\s*,\s*(?:@\w+\s+)*(?:final\s+)?TooltipFlag\s+(\w+)\s*\)")
    out = s
    while True:
        m = pat.search(out)
        if not m:
            break
        st, lv, lst, fl = m.groups()
        a, b = find_block(out, m.end())
        body = out[a + 1:b]
        body = re.sub(r"\bsuper\.appendHoverText\(\s*%s\s*,\s*%s\s*,\s*%s\s*,\s*%s\s*\)" % (st, lv, lst, fl), "super.appendHoverText(%s, tooltipContext, tooltipDisplay, tooltipAdder, %s)" % (st, fl), body)
        pre = "\n        java.util.List<Component> %s = new com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder);" % lst
        if re.search(r"\b%s\b" % lv, body):
            pre += "\n        net.minecraft.world.level.Level %s = tooltipContext.level();" % lv
        head = "public void appendHoverText(ItemStack %s, net.minecraft.world.item.Item.TooltipContext tooltipContext, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipAdder, TooltipFlag %s)" % (st, fl)
        out = out[:m.start()] + head + out[m.end():a] + "{" + pre + body + "}" + out[b + 1:]
    return out


def migrate(src):
    out = particle_providers(src)
    out = hover_text(out)
    out = entity_overrides(out)
    out = sbw_api(out)
    out = mc_misc(out)
    out = synched(out)
    out = save_methods(out)
    out = getters(out)
    out = misc(out)
    return out


def main(roots):
    changed = 0
    for root in roots:
        for d, _, files in os.walk(root):
            for name in files:
                if not name.endswith(".java"):
                    continue
                p = os.path.join(d, name)
                with open(p, encoding="utf-8") as fh:
                    src = fh.read()
                out = migrate(src)
                if out != src:
                    with open(p, "w", encoding="utf-8") as fh:
                        fh.write(out)
                    changed += 1
    print("changed", changed)


if __name__ == "__main__":
    main(sys.argv[1:])
