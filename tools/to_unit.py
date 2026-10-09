import re, sys
name = sys.argv[1]
for path in sys.argv[2:]:
    s = open(path).read()
    out = []; i = 0; changed = False
    pat = re.compile(r'(override fun ' + name + r'\s*\((?:[^()]|\([^()]*\))*\))\s*:\s*Boolean\s*\{')
    while True:
        m = pat.search(s, i)
        if not m:
            out.append(s[i:]); break
        out.append(s[i:m.start()])
        out.append(m.group(1) + ' {')
        j = m.end(); depth = 1
        while depth:
            c = s[j]
            if c == '{': depth += 1
            elif c == '}': depth -= 1
            j += 1
        body = s[m.end():j]
        body = re.sub(r'return super\.' + name + r'\(', 'super.' + name + '(', body)
        body = re.sub(r'return (true|false)\b', 'return', body)
        body = re.sub(r'return@(\w+) (true|false)\b', r'return@\1', body)
        out.append(body); i = j; changed = True
    if changed:
        open(path, 'w').write(''.join(out)); print('fixed', path)
