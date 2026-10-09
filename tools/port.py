import os
import re
import sys

SHIM = "cn.sh1rocu.simplebedrockmodel.client.port.MultiBufferSource"

RT_FACTORIES = [
    "armorCutoutNoCull", "armorTranslucent", "entitySolid", "entitySolidZOffsetForward", "entityCutoutCull",
    "entityCutout", "entityCutoutZOffset", "entityTranslucentCullItemTarget", "itemCutout", "itemTranslucent",
    "entityTranslucent", "entityTranslucentEmissive", "endCrystalBeam", "beaconBeam", "bannerPattern",
    "entityShadow", "eyes", "breezeEyes", "breezeWind", "energySwirl", "leash", "waterMask", "outline",
    "armorEntityGlint", "glintTranslucent", "glint", "entityGlint", "crumbling", "text", "textBackground",
    "textGrayscale", "textPolygonOffset", "textSeeThrough", "textBackgroundSeeThrough", "lightning",
    "dragonRays", "endPortal", "endGateway", "lines", "linesTranslucent", "secondaryBlockOutline",
    "debugFilledBox", "debugQuads", "solidMovingBlock", "cutoutMovingBlock", "translucentMovingBlock",
]
RT_RENAMES = {
    "entityCutoutNoCull": "entityCutout",
    "entityCutoutNoCullZOffset": "entityCutoutZOffset",
    "entitySmoothCutout": "entityCutout",
    "entityDecal": "entityCutout",
    "entityNoOutline": "entityTranslucent",
    "entityTranslucentCull": "entityTranslucentCullItemTarget",
    "itemEntityTranslucentCull": "entityTranslucentCullItemTarget",
    "entityTranslucentEmissive": "entityTranslucentEmissive",
    "lineStrip": "lines",
    "debugLineStrip": "lines",
}

SIMPLE = [
    (r"\bnet\.minecraft\.resources\.ResourceLocation\b", "net.minecraft.resources.Identifier"),
    (r"\bResourceLocation\b", "Identifier"),
    (r"\bnet\.minecraft\.client\.renderer\.MultiBufferSource\b", SHIM),
    (r"\bnet\.minecraft\.client\.renderer\.RenderType\b", "net.minecraft.client.renderer.rendertype.RenderType"),
    (r"\bnet\.minecraft\.util\.FastColor\b", "net.minecraft.util.ARGB"),
    (r"\bFastColor\.ARGB32\.", "ARGB."),
    (r"\bFastColor\.ABGR32\.", "ARGB."),
    (r"\bnet\.minecraft\.client\.renderer\.LightTexture\b", "net.minecraft.util.LightCoordsUtil"),
    (r"\bLightTexture\.FULL_BRIGHT\b", "LightCoordsUtil.FULL_BRIGHT"),
    (r"\bLightTexture\.FULL_SKY\b", "LightCoordsUtil.FULL_SKY"),
    (r"\bLightTexture\.FULL_BLOCK\b", "LightCoordsUtil.FULL_BLOCK"),
    (r"\bLightTexture\.pack\(", "LightCoordsUtil.pack("),
    (r"\bLightTexture\.block\(", "LightCoordsUtil.block("),
    (r"\bLightTexture\.sky\(", "LightCoordsUtil.sky("),
    (r"\bLightTexture\b(?=[\s;])", "LightCoordsUtil"),
]


def add_import(src, imp, kotlin):
    line = ("import %s" % imp) if kotlin else ("import %s;" % imp)
    if line in src:
        return src
    m = re.search(r"^package [^\n]*\n", src, re.M)
    pos = m.end() if m else 0
    return src[:pos] + "\n" + line + "\n" + src[pos:]


def migrate(src, kotlin):
    out = src
    for pat, rep in SIMPLE:
        out = re.sub(pat, rep, out)
    for old, new in RT_RENAMES.items():
        out = re.sub(r"\bRenderType\.%s\(" % old, "RenderTypes.%s(" % new, out)
    for f in RT_FACTORIES:
        out = re.sub(r"\bRenderType\.%s\(" % f, "RenderTypes.%s(" % f, out)
    if "RenderTypes." in out and not re.search(r"import net\.minecraft\.client\.renderer\.rendertype\.RenderTypes\b", out):
        out = add_import(out, "net.minecraft.client.renderer.rendertype.RenderTypes", kotlin)
    if re.search(r"\bARGB\.", out) and "import net.minecraft.util.ARGB" not in out:
        out = add_import(out, "net.minecraft.util.ARGB", kotlin)
    if "LightCoordsUtil" in out and "import net.minecraft.util.LightCoordsUtil" not in out:
        out = add_import(out, "net.minecraft.util.LightCoordsUtil", kotlin)
    return out


def main(roots):
    changed = 0
    for root in roots:
        for d, _, files in os.walk(root):
            for name in files:
                if not (name.endswith(".java") or name.endswith(".kt")):
                    continue
                p = os.path.join(d, name)
                with open(p, encoding="utf-8") as fh:
                    src = fh.read()
                out = migrate(src, name.endswith(".kt"))
                if out != src:
                    with open(p, "w", encoding="utf-8") as fh:
                        fh.write(out)
                    changed += 1
    print("changed", changed)


if __name__ == "__main__":
    main(sys.argv[1:])
