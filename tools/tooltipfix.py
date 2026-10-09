import re, sys

def find_close(s, i, o='(', c=')'):
    d = 1
    while d:
        ch = s[i]
        if ch == o: d += 1
        elif ch == c: d -= 1
        i += 1
    return i

def split_params(p):
    out = []; d = 0; cur = ''
    for ch in p:
        if ch in '(<': d += 1
        if ch in ')>': d -= 1
        if ch == ',' and d == 0: out.append(cur.strip()); cur = ''
        else: cur += ch
    if cur.strip(): out.append(cur.strip())
    return out

def ensure_import(s, imp):
    if re.search(r'^import ' + re.escape(imp) + r'$', s, re.M): return s
    return re.sub(r'^(package .*\n)', r'\1\nimport ' + imp + '\n', s, count=1, flags=re.M)

def class_supers(s, pos):
    best = None
    for m in re.finditer(r'^(?:\w+ )*class \w+[^\n{]*', s, re.M):
        if m.start() < pos: best = m
    if not best: return ''
    hdr = s[best.start():s.find('{', best.start())]
    k = hdr.find(':')
    return hdr[k + 1:] if k >= 0 else ''

def process(path):
    s = open(path).read(); o = s
    pos = 0
    block_iface = False
    while True:
        m = re.compile(r'override fun appendHoverText\s*\(').search(s, pos)
        if not m: break
        st = m.end(); en = find_close(s, st)
        params = split_params(s[st:en - 1])
        if len(params) != 4:
            pos = en; continue
        names = [p.split(':')[0].strip() for p in params]
        supers = class_supers(s, m.start())
        first = supers.strip().split('(')[0].split(',')[0].strip()
        is_block = first.endswith('Block') and 'Item' not in first
        rest = re.match(r'\s*(:\s*Unit\s*)?\{', s[en:])
        bstart = en + rest.end(); bend = find_close(s, bstart, '{', '}')
        body = s[bstart:bend]
        if is_block:
            block_iface = True
            header = f'override fun appendHoverText(\n        {names[0]}: ItemStack,\n        {names[1]}: Item.TooltipContext,\n        {names[2]}: MutableList<Component>,\n        {names[3]}: TooltipFlag\n    ) {{'
            body = re.sub(r'\n\s*super\.appendHoverText\([^\n]*\)\n', '\n', body)
        else:
            header = f'override fun appendHoverText(\n        {names[0]}: ItemStack,\n        {names[1]}: Item.TooltipContext,\n        tooltipDisplay: net.minecraft.world.item.component.TooltipDisplay,\n        tooltipAdder: java.util.function.Consumer<Component>,\n        {names[3]}: TooltipFlag\n    ) {{\n        val {names[2]} = com.atsuishio.portlib.item.LegacyTooltipList(tooltipAdder)'
            body = re.sub(r'super\.appendHoverText\(\s*(\w+),\s*(\w+),\s*(\w+),\s*(\w+)\s*\)', r'super.appendHoverText(\1, \2, tooltipDisplay, tooltipAdder, \4)', body)
        s = s[:m.start()] + header + body + s[bend:]
        pos = m.start() + len(header)
    if block_iface:
        s = re.sub(r'^((?:\w+ )*class \w+[^\n{]*?:\s*[^\n{]*?)(\s*\{)', lambda mm: mm.group(1) + (', com.atsuishio.portlib.item.LegacyBlockTooltip' if 'LegacyBlockTooltip' not in mm.group(1) else '') + mm.group(2), s, count=1, flags=re.M)
        s = ensure_import(s, 'net.minecraft.world.item.Item')
    if s != o:
        s = ensure_import(s, 'net.minecraft.network.chat.Component')
        s = ensure_import(s, 'net.minecraft.world.item.ItemStack')
        s = ensure_import(s, 'net.minecraft.world.item.TooltipFlag')
        open(path, 'w').write(s)
        print(('block ' if block_iface else 'item  ') + path)

for p in sys.argv[1:]:
    process(p)
