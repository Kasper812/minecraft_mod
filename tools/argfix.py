import re, sys, os

def split_args(a):
    out = []; d = 0; cur = ''; q = False
    for ch in a:
        if ch == '"': q = not q
        if not q:
            if ch in '([{': d += 1
            if ch in ')]}': d -= 1
            if ch == ',' and d == 0:
                out.append(cur); cur = ''; continue
        cur += ch
    out.append(cur)
    return out

RULES = [
    ('.addParticle(', 8, lambda a: a[:2] + [' false'] + a[2:]),
    ('.sendParticles(', 11, lambda a: a[:3] + [' false'] + a[3:]),
]

def process(s):
    for name, arity, fn in RULES:
        res = ''; i = 0
        while True:
            k = s.find(name, i)
            if k < 0:
                res += s[i:]; break
            st = k + len(name); d = 1; j = st
            while d and j < len(s):
                if s[j] == '(': d += 1
                elif s[j] == ')': d -= 1
                j += 1
            args = split_args(s[st:j - 1])
            if len(args) == arity:
                args = fn(args)
                res += s[i:st] + ','.join(args) + ')'
            else:
                res += s[i:j]
            i = j
        s = res
    s = re.sub(r'([(,]\s*)ParticleTypes\.FLASH(\s*[,)])', r'\1net.minecraft.core.particles.ColorParticleOption.create(net.minecraft.core.particles.ParticleTypes.FLASH, -1)\2', s)
    return s

for root in sys.argv[1:]:
    for dp, dn, fn in os.walk(root):
        for f in fn:
            if not (f.endswith('.kt') or f.endswith('.java')): continue
            p = os.path.join(dp, f); s = open(p).read(); n = process(s)
            if n != s: open(p, 'w').write(n); print('fixed', p)
