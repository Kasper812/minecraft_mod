import re, sys

def find_body(s, start):
    depth = 1; j = start
    while depth:
        c = s[j]
        if c == '{': depth += 1
        elif c == '}': depth -= 1
        j += 1
    return j

def parse_params(p):
    out = []; depth = 0; cur = ''
    for ch in p:
        if ch in '(<': depth += 1
        if ch in ')>': depth -= 1
        if ch == ',' and depth == 0:
            out.append(cur.strip()); cur = ''
        else:
            cur += ch
    if cur.strip(): out.append(cur.strip())
    return [x.split(':')[0].strip() for x in out], out

def split_args(a):
    out = []; depth = 0; cur = ''
    for ch in a:
        if ch in '([{<': depth += 1
        if ch in ')]}>': depth -= 1
        if ch == ',' and depth == 0:
            out.append(cur.strip()); cur = ''
        else:
            cur += ch
    if cur.strip(): out.append(cur.strip())
    return out

def rewrite_super(body, name, fn):
    res = ''; i = 0
    pat = 'super.' + name + '('
    while True:
        k = body.find(pat, i)
        if k < 0:
            res += body[i:]; break
        res += body[i:k]
        st = k + len(pat); depth = 1; j = st
        while depth:
            c = body[j]
            if c == '(': depth += 1
            elif c == ')': depth -= 1
            j += 1
        args = split_args(body[st:j - 1])
        res += pat + ', '.join(fn(args)) + ')'
        i = j
    return res

def ensure_import(s, imp):
    if re.search(r'^import ' + re.escape(imp) + r'$', s, re.M): return s
    return re.sub(r'^(package .*\n)', r'\1\nimport ' + imp + '\n', s, count=1, flags=re.M)

def process(s):
    changed = []
    def method(name, handler):
        nonlocal s
        pos = 0
        while True:
            m = re.compile(r'override fun ' + name + r'\s*\(').search(s, pos)
            if not m: break
            st = m.end(); depth = 1; j = st
            while depth:
                c = s[j]
                if c == '(': depth += 1
                elif c == ')': depth -= 1
                j += 1
            params = s[st:j - 1]
            names, full = parse_params(params)
            rest = s[j:]
            mb = re.match(r'(\s*:\s*[\w.<>?, ]+)?\s*\{', rest)
            if not mb:
                pos = j; continue
            bstart = j + mb.end(); bend = find_body(s, bstart)
            res = handler(names, full, s[bstart:bend])
            if res is None:
                pos = j; continue
            newparams, body, ret = res
            header = 'override fun ' + name + '(\n        ' + ',\n        '.join(newparams) + '\n    )' + (ret if ret is not None else (mb.group(1) or '')) + ' {'
            s = s[:m.start()] + header + body + s[bend:]
            pos = m.start() + len(header)
            changed.append(name)

    def update_shape(names, full, body):
        if len(names) != 6: return None
        st, d, ns, lv, p, np = names
        nparams = [f'{st}: BlockState', f'{lv}: LevelReader', 'scheduledTickAccess: ScheduledTickAccess', f'{p}: BlockPos', f'{d}: Direction', f'{np}: BlockPos', f'{ns}: BlockState', 'randomSource: RandomSource']
        body = rewrite_super(body, 'updateShape', lambda a: [a[0], a[3], 'scheduledTickAccess', a[4], a[1], a[5], a[2], 'randomSource'] if len(a) == 6 else a)
        body = re.sub(r'\b' + re.escape(lv) + r'\.scheduleTick\(', 'scheduledTickAccess.scheduleTick(', body)
        return nparams, body, None
    method('updateShape', update_shape)

    def entity_inside(names, full, body):
        if len(names) != 4: return None
        nparams = full + ['effectApplier: InsideBlockEffectApplier', 'isPrecise: Boolean']
        body = rewrite_super(body, 'entityInside', lambda a: a + ['effectApplier', 'isPrecise'] if len(a) == 4 else a)
        return nparams, body, None
    method('entityInside', entity_inside)

    def neighbor_changed(names, full, body):
        if len(names) != 6 or 'Orientation' in full[4]: return None
        nparams = full[:4] + ['orientation: Orientation?', full[5]]
        body = rewrite_super(body, 'neighborChanged', lambda a: a[:4] + ['orientation', a[5]] if len(a) == 6 else a)
        return nparams, body, None
    method('neighborChanged', neighbor_changed)

    def skylight(names, full, body):
        if len(names) != 3: return None
        body = rewrite_super(body, 'propagatesSkylightDown', lambda a: a[:1])
        return full[:1], body, None
    method('propagatesSkylightDown', skylight)

    def clone(names, full, body):
        if len(names) != 3: return None
        body = rewrite_super(body, 'getCloneItemStack', lambda a: a + ['includeData'] if len(a) == 3 else a)
        return full + ['includeData: Boolean'], body, None
    method('getCloneItemStack', clone)

    def fall_on(names, full, body):
        if len(names) != 5: return None
        full = full[:4] + [names[4] + ': Double']
        return full, body, None
    method('fallOn', fall_on)

    s = s.replace('.noCollission()', '.noCollision()')
    s = re.sub(r'StateDefinition\.Builder<Block\?, BlockState\?>', 'StateDefinition.Builder<Block, BlockState>', s)
    if 'ScheduledTickAccess' in s: s = ensure_import(s, 'net.minecraft.world.level.ScheduledTickAccess')
    if 'LevelReader' in s: s = ensure_import(s, 'net.minecraft.world.level.LevelReader')
    if 'randomSource: RandomSource' in s: s = ensure_import(s, 'net.minecraft.util.RandomSource')
    if 'InsideBlockEffectApplier' in s: s = ensure_import(s, 'net.minecraft.world.entity.InsideBlockEffectApplier')
    if 'Orientation?' in s: s = ensure_import(s, 'net.minecraft.world.level.redstone.Orientation')
    return s, changed

for path in sys.argv[1:]:
    s = open(path).read()
    ns, ch = process(s)
    if ns != s:
        open(path, 'w').write(ns); print('fixed', path, ch)
