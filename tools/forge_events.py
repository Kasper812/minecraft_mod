import os
import re
import sys

KNOWN = {
    "EntityRenderersEvent.RegisterRenderers": [
        (r"\b{ev}\.registerEntityRenderer\(", "net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register("),
        (r"\b{ev}\.registerBlockEntityRenderer\(", "net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register("),
    ],
    "RegisterParticleProvidersEvent": [
        (r"\b{ev}\.registerSpriteSet\(", "net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register("),
        (r"\b{ev}\.registerSpecial\(", "net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register("),
    ],
    "RegisterKeyMappingsEvent": [
        (r"\b{ev}\.register\(", "net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping("),
    ],
}


def process(src, path, report):
    out = src
    out = re.sub(r"^import net\.minecraftforge\.eventbus\.api\.IEventBus;\n", "", out, flags=re.M)
    out = re.sub(r"\bIEventBus\b", "Object", out)
    cls = re.search(r"\bclass\s+(\w+)", out)
    pkg = re.search(r"^package\s+([\w.]+);", out, re.M)
    fqcn = (pkg.group(1) + "." if pkg else "") + (cls.group(1) if cls else "?")
    client = bool(re.search(r"@Mod\.EventBusSubscriber\([^)]*(?:Dist|EnvType)\.CLIENT", out))
    out = re.sub(r"@Mod\.EventBusSubscriber(?:\([^)]*\))?\s*\n", "", out)
    pat = re.compile(r"@SubscribeEvent(?:\([^)]*\))?\s*\n(\s*)(public\s+static\s+void\s+(\w+))\(\s*(?:final\s+)?([\w.<>?]+)\s+(\w+)\s*\)")
    pos = 0
    while True:
        m = pat.search(out, pos)
        if not m:
            break
        indent, head, name, evtype, ev = m.groups()
        if evtype in KNOWN:
            start = out.index("{", m.end())
            depth = 0
            j = start
            while j < len(out):
                if out[j] == "{":
                    depth += 1
                elif out[j] == "}":
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            body = out[start:j + 1]
            for p, r in KNOWN[evtype]:
                body = re.sub(p.format(ev=ev), r, body)
            new_head = "%s()" % head
            out = out[:m.start()] + new_head + out[m.end():start] + body + out[j + 1:]
            report.append(("client", fqcn + "." + name, evtype))
            pos = m.start() + len(new_head)
        else:
            report.append(("client" if client else "common", fqcn + "." + name, evtype))
            pos = m.end()
    out = re.sub(r"[ \t]*@SubscribeEvent(?:\([^)]*\))?\s*\n", "", out)
    for imp in ["net.minecraftforge.eventbus.api.SubscribeEvent", "net.minecraftforge.fml.common.Mod",
                "net.minecraftforge.client.event.EntityRenderersEvent", "net.minecraftforge.client.event.RegisterParticleProvidersEvent",
                "net.minecraftforge.client.event.RegisterKeyMappingsEvent"]:
        if imp.split(".")[-1] + "." not in out.replace("import " + imp + ";", "") and not re.search(r"\b%s\b" % imp.split(".")[-1], out.replace("import " + imp + ";", "")):
            out = out.replace("import %s;\n" % imp, "")
    return out


def main(roots):
    report = []
    for root in roots:
        for d, _, files in os.walk(root):
            for f in files:
                if not f.endswith(".java"):
                    continue
                p = os.path.join(d, f)
                src = open(p, encoding="utf-8").read()
                out = process(src, p, report)
                if out != src:
                    open(p, "w", encoding="utf-8").write(out)
    for side, name, ev in report:
        print(side, name, ev)


if __name__ == "__main__":
    main(sys.argv[1:])
