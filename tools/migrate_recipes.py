import json, sys, os, glob
def ing(v, path):
    if isinstance(v, dict):
        if 'fabric:type' in v or 'type' in v and len(v) > 1 and 'item' not in v and 'tag' not in v:
            return v
        if 'item' in v and len(v) == 1:
            return v['item']
        if 'tag' in v and len(v) == 1:
            return '#' + v['tag']
        if 'item' in v:
            return v['item']
        if 'tag' in v:
            return '#' + v['tag']
        return v
    if isinstance(v, list):
        out = [ing(x, path) for x in v]
        if len(out) == 1:
            return out[0]
        tags = [x for x in out if isinstance(x, str) and x.startswith('#')]
        if tags:
            return {'fabric:type': 'fabric:any', 'ingredients': out}
        return out
    return v
def res(v):
    if isinstance(v, dict) and 'item' in v and 'id' not in v:
        v = dict(v); v['id'] = v.pop('item')
    if isinstance(v, str):
        v = {'id': v}
    return v
changed = 0
for root in sys.argv[1:]:
    for f in glob.glob(os.path.join(root, '**', '*.json'), recursive=True):
        d = json.load(open(f, encoding='utf-8'))
        t = d.get('type', '')
        o = json.dumps(d, sort_keys=True)
        if t == 'minecraft:crafting_shaped':
            d['key'] = {k: ing(v, f) for k, v in d['key'].items()}
            d['result'] = res(d['result'])
        elif t == 'minecraft:crafting_shapeless':
            d['ingredients'] = [ing(v, f) for v in d['ingredients']]
            d['result'] = res(d['result'])
        elif t == 'minecraft:smithing_transform':
            for k in ('template', 'base', 'addition'):
                if k in d: d[k] = ing(d[k], f)
            d['result'] = res(d['result'])
        elif t in ('minecraft:smelting', 'minecraft:blasting', 'minecraft:smoking', 'minecraft:campfire_cooking', 'minecraft:stonecutting'):
            d['ingredient'] = ing(d['ingredient'], f)
            d['result'] = res(d['result'])
        elif t.startswith('fabric:') or t.startswith('minecraft:'):
            pass
        if json.dumps(d, sort_keys=True) != o:
            json.dump(d, open(f, 'w', encoding='utf-8'), indent=2, ensure_ascii=False)
            open(f, 'a').write('\n')
            changed += 1
print('changed', changed)
