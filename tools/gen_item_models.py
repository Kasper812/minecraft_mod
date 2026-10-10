import json, os, sys
from PIL import Image

ITEMS = sys.argv[1]
NS = sys.argv[2]
RES = sys.argv[3:]
ASSETS = [os.path.join(r, "assets", NS) for r in RES]
MAIN = ASSETS[0]
EGG_TEMPLATE = os.environ.get("EGG_TEMPLATE")

GUI_ICON_EXTRA = {"lunge_mine"}
EGG_COLORS = {}
for spec in os.environ.get("EGG_COLORS", "").split(";"):
    if spec:
        k, a, b = spec.split(",")
        EGG_COLORS[k] = (int(a), int(b))
CONDITIONS = {}
for spec in os.environ.get("CONDITIONS", "").split(";"):
    if spec:
        k, key, alt = spec.split(",")
        CONDITIONS[k] = (key, alt)
TINTS = {}
for spec in os.environ.get("POTION_TINT", "").split(";"):
    if spec:
        TINTS[spec] = [{"type": "minecraft:constant", "value": -1}, {"type": "minecraft:potion", "default": -1}]


def find(rel):
    for a in ASSETS:
        p = os.path.join(a, rel)
        if os.path.exists(p):
            return p
    return None


def load_model(ref):
    ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
    if ns != NS:
        return None, None
    p = find("models/" + path + ".json")
    if not p:
        return None, None
    return p, json.load(open(p))


def chain_end(ref):
    seen = []
    while ref:
        seen.append(ref)
        p, m = load_model(ref)
        if m is None:
            return ref, seen
        ref = m.get("parent")
    return None, seen


def norm(ref):
    if ":" not in ref:
        return "minecraft:" + ref
    return ref


fixed_builtin = 0
for a in ASSETS:
    d = os.path.join(a, "models")
    for dp, dn, fn in os.walk(d):
        for f in fn:
            if not f.endswith(".json"):
                continue
            p = os.path.join(dp, f)
            try:
                m = json.load(open(p))
            except Exception:
                continue
            if isinstance(m, dict) and m.get("parent") in ("builtin/entity", "minecraft:builtin/entity"):
                del m["parent"]
                m["sbw_builtin"] = True
                json.dump(m, open(p, "w"), indent=2)
                fixed_builtin += 1


def is_builtin(ref):
    while ref:
        p, m = load_model(ref)
        if m is None:
            return False
        if m.get("sbw_builtin"):
            return True
        ref = m.get("parent")
    return False


def bake_egg(idn, colors):
    base = Image.open(os.path.join(EGG_TEMPLATE, "spawn_egg.png")).convert("RGBA")
    over = Image.open(os.path.join(EGG_TEMPLATE, "spawn_egg_overlay.png")).convert("RGBA")

    def tint(img, c):
        c &= 0xFFFFFF
        r, g, b = (c >> 16) & 255, (c >> 8) & 255, c & 255
        px = img.load()
        out = Image.new("RGBA", img.size)
        op = out.load()
        for y in range(img.size[1]):
            for x in range(img.size[0]):
                pr, pg, pb, pa = px[x, y]
                op[x, y] = (pr * r // 255, pg * g // 255, pb * b // 255, pa)
        return out

    img = Image.alpha_composite(tint(base, colors[0]), tint(over, colors[1]))
    out = os.path.join(MAIN, "textures/item", idn + ".png")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    img.save(out)
    mp = os.path.join(MAIN, "models/item", idn + ".json")
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{idn}"}}, open(mp, "w"), indent=2)


def plain(ref, tints=None):
    m = {"type": "minecraft:model", "model": ref}
    if tints:
        m["tints"] = tints
    return m


written = 0
missing = []
items_dir = os.path.join(MAIN, "items")
os.makedirs(items_dir, exist_ok=True)
for line in open(ITEMS):
    parts = line.split()
    if not parts:
        continue
    idn, cls, is_block, chain = parts[0], parts[1], parts[2] == "true", parts[3].split(",")
    if idn in EGG_COLORS and EGG_TEMPLATE:
        bake_egg(idn, EGG_COLORS[idn])
    ref = f"{NS}:item/{idn}"
    if load_model(ref)[1] is None:
        if is_block and load_model(f"{NS}:block/{idn}")[1] is not None:
            ref = f"{NS}:block/{idn}"
        else:
            missing.append(idn)
            continue
    if is_builtin(ref):
        model = {"type": "sbw_portlib:builtin", "base": ref}
    else:
        model = plain(ref, TINTS.get(idn))
    if idn in CONDITIONS:
        key, alt = CONDITIONS[idn]
        model = {
            "type": "minecraft:condition",
            "property": "minecraft:component",
            "predicate": "minecraft:custom_data",
            "value": "{" + key + ":1b}",
            "on_true": plain(f"{NS}:item/{alt}"),
            "on_false": model,
        }
    if "GunGeoItem" in chain and load_model(f"{NS}:item/{idn}_icon")[1] is not None:
        model = {
            "type": "minecraft:select",
            "property": "minecraft:display_context",
            "cases": [{"when": ["gui", "ground"], "model": plain(f"{NS}:item/{idn}_icon")}],
            "fallback": model,
        }
    if idn in GUI_ICON_EXTRA:
        model = {
            "type": "minecraft:select",
            "property": "minecraft:display_context",
            "cases": [
                {"when": ["gui", "ground", "fixed", "thirdperson_lefthand"], "model": plain(f"{NS}:item/{idn}_icon")},
                {"when": ["thirdperson_righthand"], "model": plain(f"{NS}:item/{idn}_3d")},
            ],
            "fallback": model,
        }
    out = {"model": model}
    json.dump(out, open(os.path.join(items_dir, idn + ".json"), "w"), indent=2)
    written += 1

print("builtin parents removed:", fixed_builtin)
print("items written:", written)
print("missing models:", missing)
