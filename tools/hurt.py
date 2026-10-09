import re, sys
for path in sys.argv[1:]:
    s = open(path).read()
    m = re.search(r'override fun hurt\(\s*(\w+): DamageSource,\s*(\w+): Float\s*\): Boolean \{', s)
    if not m:
        continue
    src, amt = m.group(1), m.group(2)
    start = m.end(); depth = 1; j = start
    while depth:
        c = s[j]
        if c == '{': depth += 1
        elif c == '}': depth -= 1
        j += 1
    body = s[start:j]
    body = re.sub(r'super\.hurt\(', 'super.hurtServer(serverLevel, ', body)
    s = s[:m.start()] + f'override fun hurtServer(serverLevel: ServerLevel, {src}: DamageSource, {amt}: Float): Boolean {{' + body + s[j:]
    if 'import net.minecraft.server.level.ServerLevel' not in s:
        s = re.sub(r'^(package .*\n)', r'\1\nimport net.minecraft.server.level.ServerLevel\n', s, count=1, flags=re.M)
    open(path, 'w').write(s)
    print('fixed', path)
