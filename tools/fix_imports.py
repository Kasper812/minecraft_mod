import os
import re
import sys
from collections import defaultdict

MC = '/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad/mc262'
existing = set()
by_name = defaultdict(set)
nested = {}
for sub in ('common', 'clientOnly'):
    base = os.path.join(MC, sub)
    for d, _, fs in os.walk(base):
        for f in fs:
            if f.endswith('.java'):
                rel = os.path.relpath(os.path.join(d, f), base)[:-5].replace(os.sep, '.')
                existing.add(rel)
                by_name[rel.rsplit('.', 1)[1]].add(rel)
                src = open(os.path.join(d, f), encoding='utf-8', errors='ignore').read()
                for m in re.finditer(r'^\t(?:public |protected |private )?(?:static |final |abstract |sealed |non-sealed )*(?:class|interface|enum|record) (\w+)', src, re.M):
                    fq = rel + '.' + m.group(1)
                    existing.add(fq)

MANUAL = {
}


def fix(path):
    s = open(path, encoding='utf-8').read()
    kotlin = path.endswith('.kt')
    changed = False

    def repl(m):
        nonlocal changed
        fq = m.group(2)
        alias = m.group(3) or ''
        semi = m.group(4) or ''
        if fq.endswith('.*'):
            return m.group(0)
        if fq in existing or not (fq.startswith('net.minecraft.') or fq.startswith('com.mojang.')):
            return m.group(0)
        if fq in MANUAL:
            changed = True
            return '%s%s%s%s' % (m.group(1), MANUAL[fq], alias, semi)
        parts = fq.split('.')
        name = parts[-1]
        outer = None
        for i in range(len(parts) - 1, 0, -1):
            if parts[i][0].isupper() and i < len(parts) - 1:
                outer = i
        cands = by_name.get(name, set())
        if len(cands) == 1:
            changed = True
            return '%s%s%s%s' % (m.group(1), next(iter(cands)), alias, semi)
        if len(cands) > 1:
            def score(c):
                cp = c.split('.')
                n = 0
                for a, b in zip(cp, parts):
                    if a != b:
                        break
                    n += 1
                return n
            ranked = sorted(cands, key=score, reverse=True)
            if score(ranked[0]) > score(ranked[1]):
                changed = True
                return '%s%s%s%s' % (m.group(1), ranked[0], alias, semi)
        if len(parts) >= 2 and parts[-2][0].isupper():
            oc = by_name.get(parts[-2], set())
            if len(oc) == 1:
                changed = True
                return '%s%s.%s%s%s' % (m.group(1), next(iter(oc)), name, alias, semi)
        return m.group(0)

    s2 = re.sub(r'^(import )([\w.]+(?:\.\*)?)( as \w+)?(;)?\s*$', repl, s, flags=re.M)
    if s2 != s:
        open(path, 'w', encoding='utf-8').write(s2)
    return s2 != s


def main(roots):
    n = 0
    for root in roots:
        for d, _, fs in os.walk(root):
            for f in fs:
                if f.endswith('.kt') or f.endswith('.java'):
                    if fix(os.path.join(d, f)):
                        n += 1
    print('changed', n)
    missing = defaultdict(int)
    for root in roots:
        for d, _, fs in os.walk(root):
            for f in fs:
                if f.endswith('.kt') or f.endswith('.java'):
                    for m in re.finditer(r'^import ((?:net\.minecraft|com\.mojang)[\w.]+)', open(os.path.join(d, f), encoding='utf-8').read(), re.M):
                        if m.group(1) not in existing:
                            missing[m.group(1)] += 1
    for k, v in sorted(missing.items(), key=lambda x: -x[1]):
        print('MISSING', v, k)


if __name__ == '__main__':
    main(sys.argv[1:])
