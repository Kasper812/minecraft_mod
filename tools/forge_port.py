import os
import re
import sys

IMPORTS = {
    "net.minecraftforge.registries.DeferredRegister": "com.atsuishio.portlib.forge.DeferredRegister",
    "net.minecraftforge.registries.RegistryObject": "com.atsuishio.portlib.forge.RegistryObject",
    "net.minecraftforge.registries.ForgeRegistries": "com.atsuishio.portlib.forge.ForgeRegistries",
    "net.minecraftforge.common.util.Lazy": "com.atsuishio.portlib.forge.Lazy",
    "net.minecraftforge.api.distmarker.Dist": "net.fabricmc.api.EnvType",
    "net.minecraftforge.api.distmarker.OnlyIn": "net.fabricmc.api.Environment",
    "software.bernie.geckolib.core.animatable.model.CoreGeoBone": "software.bernie.geckolib.cache.object.GeoBone",
    "software.bernie.geckolib.core.object.PlayState": "software.bernie.geckolib.animation.PlayState",
    "net.minecraft.resources.ResourceLocation": "net.minecraft.resources.Identifier",
    "net.minecraftforge.network.NetworkEvent": "com.atsuishio.portlib.forge.network.NetworkEvent",
    "net.minecraftforge.network.simple.SimpleChannel": "com.atsuishio.portlib.forge.network.SimpleChannel",
    "net.minecraftforge.network.NetworkRegistry": "com.atsuishio.portlib.forge.network.NetworkRegistry",
    "net.minecraftforge.network.PacketDistributor": "com.atsuishio.portlib.forge.network.PacketDistributor",
    "net.minecraftforge.network.NetworkDirection": "com.atsuishio.portlib.forge.network.NetworkDirection",
    "net.minecraftforge.fml.DistExecutor": "com.atsuishio.portlib.forge.DistExecutor",
    "net.minecraftforge.client.gui.overlay.ForgeGui": "com.atsuishio.portlib.forge.client.ForgeGui",
    "net.minecraftforge.client.gui.overlay.IGuiOverlay": "com.atsuishio.portlib.forge.client.IGuiOverlay",
    "net.minecraftforge.event.entity.player.PlayerInteractEvent": "com.atsuishio.portlib.forge.event.PlayerInteractEvent",
    "net.minecraftforge.event.entity.EntityMountEvent": "com.atsuishio.portlib.forge.event.EntityMountEvent",
    "net.minecraftforge.common.util.FakePlayer": "net.fabricmc.fabric.api.entity.FakePlayer",
}
PKG = [
    (r"\bsoftware\.bernie\.geckolib\.core\.animatable\.", "software.bernie.geckolib.animatable."),
    (r"\bsoftware\.bernie\.geckolib\.core\.animation\.", "software.bernie.geckolib.animation."),
    (r"\bsoftware\.bernie\.geckolib\.core\.molang\.", "software.bernie.geckolib.loading.math."),
    (r"\bsoftware\.bernie\.geckolib\.core\.keyframe\.", "software.bernie.geckolib.animation.keyframe."),
    (r"\bsoftware\.bernie\.geckolib\.core\.state\.", "software.bernie.geckolib.animation.state."),
]
SIMPLE = [
    (r"@OnlyIn\(\s*Dist\.CLIENT\s*\)", "@Environment(EnvType.CLIENT)"),
    (r"@OnlyIn\(\s*Dist\.DEDICATED_SERVER\s*\)", "@Environment(EnvType.SERVER)"),
    (r"\bDist\.CLIENT\b", "EnvType.CLIENT"),
    (r"\bDist\.DEDICATED_SERVER\b", "EnvType.SERVER"),
    (r"\bCoreGeoBone\b", "GeoBone"),
    (r"\.setTrackingRange\(", ".clientTrackingRange("),
    (r"\.setUpdateInterval\(", ".updateInterval("),
    (r"\.setShouldReceiveVelocityUpdates\(", ".alwaysUpdateVelocity("),
    (r"\bCreativeModeTab\.builder\(\)", "net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder()"),
    (r"\bnet\.minecraftforge\.network\.simple\.SimpleChannel\b", "com.atsuishio.portlib.forge.network.SimpleChannel"),
    (r"\bResourceLocation\.tryParse\(", "Identifier.tryParse("),
    (r"\bResourceLocation\.fromNamespaceAndPath\(", "Identifier.fromNamespaceAndPath("),
    (r"\bResourceLocation\.parse\(", "Identifier.parse("),
    (r"\bResourceLocation\b", "Identifier"),
    (r"\bForgeRegistries\.ITEMS\.getValue\(", "BuiltInRegistries.ITEM.getValue("),
    (r"\bForgeRegistries\.BLOCKS\.getValue\(", "BuiltInRegistries.BLOCK.getValue("),
    (r"\bForgeRegistries\.ENTITY_TYPES\.getValue\(", "BuiltInRegistries.ENTITY_TYPE.getValue("),
    (r"\bForgeRegistries\.SOUND_EVENTS\.getValue\(", "BuiltInRegistries.SOUND_EVENT.getValue("),
    (r"\bForgeRegistries\.ITEMS\.getKey\(", "BuiltInRegistries.ITEM.getKey("),
    (r"\bForgeRegistries\.BLOCKS\.getKey\(", "BuiltInRegistries.BLOCK.getKey("),
    (r"\bForgeRegistries\.ENTITY_TYPES\.getKey\(", "BuiltInRegistries.ENTITY_TYPE.getKey("),
    (r"\bForgeRegistries\.ITEMS\.containsKey\(", "BuiltInRegistries.ITEM.containsKey("),
    (r"\bForgeRegistries\.ENTITY_TYPES\.containsKey\(", "BuiltInRegistries.ENTITY_TYPE.containsKey("),
]


def split_args(s, start):
    depth = 0
    i = start
    args = []
    cur = start
    in_str = None
    while i < len(s):
        c = s[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in "\"'":
            in_str = c
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            if depth == 0:
                args.append(s[cur:i])
                return args, i
            depth -= 1
        elif c == "," and depth == 0:
            args.append(s[cur:i])
            cur = i + 1
        i += 1
    return None, None


def new_identifier(s):
    out = []
    i = 0
    pat = re.compile(r"\bnew\s+(?:ResourceLocation|Identifier)\s*\(")
    while True:
        m = pat.search(s, i)
        if not m:
            out.append(s[i:])
            break
        args, end = split_args(s, m.end())
        if args is None:
            out.append(s[i:m.end()])
            i = m.end()
            continue
        out.append(s[i:m.start()])
        args = [a.strip() for a in args]
        if len(args) == 2:
            out.append("Identifier.fromNamespaceAndPath(%s, %s)" % (args[0], args[1]))
        else:
            out.append("Identifier.parse(%s)" % args[0])
        i = end + 1
    return "".join(out)


def add_import(src, imp):
    line = "import %s;" % imp
    if line in src:
        return src
    m = re.search(r"^package [^\n]*\n", src, re.M)
    pos = m.end() if m else 0
    return src[:pos] + "\n" + line + src[pos:]


def migrate(src):
    out = src.replace("\r\n", "\n").replace("\r", "\n")
    for old, new in IMPORTS.items():
        out = re.sub(r"^import\s+%s\s*;" % re.escape(old), "import %s;" % new, out, flags=re.M)
    for pat, rep in PKG:
        out = re.sub(pat, rep, out)
    out = new_identifier(out)
    for pat, rep in SIMPLE:
        out = re.sub(pat, rep, out)
    if "BuiltInRegistries." in out:
        out = add_import(out, "net.minecraft.core.registries.BuiltInRegistries")
    if "Identifier" in out and "import net.minecraft.resources.Identifier;" not in out and not re.search(r"^package net\.minecraft\.resources;", out, re.M):
        out = add_import(out, "net.minecraft.resources.Identifier")
    out = re.sub(r"^(import [^;\n]+;\n)(?:\1)+", r"\1", out, flags=re.M)
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
