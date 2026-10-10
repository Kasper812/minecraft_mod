import json
import os
import re
import shutil
import subprocess
import sys

DIRS = {
    "recipes": "recipe",
    "loot_tables": "loot_table",
    "advancements": "advancement",
    "structures": "structure",
    "predicates": "predicate",
    "item_modifiers": "item_modifier",
    "functions": "function",
}
TAG_DIRS = {
    "items": "item",
    "blocks": "block",
    "entity_types": "entity_type",
    "fluids": "fluid",
    "game_events": "game_event",
    "functions": "function",
}
TAG_MAP = {
    "forge:glass": "c:glass_blocks",
    "forge:glass_panes": "c:glass_panes",
    "forge:string": "c:strings",
    "forge:gunpowder": "c:gunpowders",
    "forge:leather": "c:leathers",
    "forge:slimeballs": "c:slime_balls",
    "forge:rods/wooden": "c:rods/wooden",
    "forge:stone": "c:stones",
    "forge:cobblestone": "c:cobblestones",
    "forge:sand": "c:sands",
    "forge:gravel": "c:gravels",
    "forge:obsidian": "c:obsidians",
    "forge:chests": "c:chests",
    "forge:chests/wooden": "c:chests/wooden",
    "forge:dyes": "c:dyes",
    "forge:feathers": "c:feathers",
    "forge:bones": "c:bones",
    "forge:eggs": "c:eggs",
    "forge:ender_pearls": "c:ender_pearls",
    "forge:wool": "c:wools",
    "forge:crops": "c:crops",
    "forge:seeds": "c:seeds",
    "forge:tools/shears": "c:tools/shear",
    "forge:shears": "c:tools/shear",
}


SBW_TAGS = {
    "superbwarfare:ingots/steel": "c:ingots/steel",
    "superbwarfare:storage_blocks/steel": "c:storage_blocks/steel",
    "superbwarfare:plates/steel": "c:plates/steel",
}


def map_tag(t):
    if t in SBW_TAGS:
        return SBW_TAGS[t]
    if t in TAG_MAP:
        return TAG_MAP[t]
    if t.startswith("forge:"):
        return "c:" + t[len("forge:"):]
    return t


def map_strings(obj):
    if isinstance(obj, dict):
        return {k: map_strings(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [map_strings(v) for v in obj]
    if isinstance(obj, str):
        def rep(m):
            return m.group(1) + map_tag(m.group(2))
        return re.sub(r"(#?)((?:forge|superbwarfare):[a-z0-9_/.]+)", rep, obj)
    return obj


def move_tree(src, dst):
    if not os.path.isdir(src):
        return
    for d, _, files in os.walk(src):
        rel = os.path.relpath(d, src)
        target = os.path.join(dst, rel) if rel != "." else dst
        os.makedirs(target, exist_ok=True)
        for f in files:
            shutil.move(os.path.join(d, f), os.path.join(target, f))
    shutil.rmtree(src)


def main(data_root, tools_dir):
    for ns in os.listdir(data_root):
        base = os.path.join(data_root, ns)
        if not os.path.isdir(base):
            continue
        for old, new in DIRS.items():
            move_tree(os.path.join(base, old), os.path.join(base, new))
        tags = os.path.join(base, "tags")
        if os.path.isdir(tags):
            for old, new in TAG_DIRS.items():
                move_tree(os.path.join(tags, old), os.path.join(tags, new))
    forge = os.path.join(data_root, "forge")
    if os.path.isdir(forge):
        move_tree(forge, os.path.join(data_root, "c"))
        ctags = os.path.join(data_root, "c", "tags")
        if os.path.isdir(ctags):
            for old, new in TAG_DIRS.items():
                move_tree(os.path.join(ctags, old), os.path.join(ctags, new))
    changed = 0
    for d, _, files in os.walk(data_root):
        for f in files:
            if not f.endswith(".json"):
                continue
            p = os.path.join(d, f)
            try:
                with open(p, encoding="utf-8") as fh:
                    obj = json.load(fh)
            except Exception:
                continue
            new = map_strings(obj)
            if new != obj:
                with open(p, "w", encoding="utf-8") as fh:
                    json.dump(new, fh, indent=2, ensure_ascii=False)
                changed += 1
    print("json tag rewrites", changed)
    for ns in os.listdir(data_root):
        rdir = os.path.join(data_root, ns, "recipe")
        if os.path.isdir(rdir):
            subprocess.run([sys.executable, os.path.join(tools_dir, "migrate_recipes.py"), rdir])


if __name__ == "__main__":
    main(sys.argv[1], os.path.dirname(os.path.abspath(__file__)))
