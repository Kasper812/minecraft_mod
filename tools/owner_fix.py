import re, sys, collections
log = sys.argv[1]
root = sys.argv[2]
errs = collections.defaultdict(set)
for line in open(log):
    if not line.startswith('e: file://'): continue
    m = re.match(r'e: file://(\S+?\.kt):(\d+):(\d+) (.*)', line)
    if not m: continue
    path, ln, col, msg = m.group(1), int(m.group(2)), int(m.group(3)), m.group(4)
    if 'EntityReference<Entity>' in msg or "Check for instance is always 'false'" in msg:
        errs[path].add(ln)
for path, lines in errs.items():
    src = open(path).read().split('\n')
    changed = False
    for ln in sorted(lines):
        s = src[ln - 1]
        o = s
        s = re.sub(r'(?<![\w.])(this\.)?owner\s*=\s*(?!=)(.+)$', lambda m: f'{m.group(1) or ""}setOwner({m.group(2).rstrip()})', s)
        s = re.sub(r'(?<![\w])this\.owner(?![\w(])', 'this.getOwner()', s)
        s = re.sub(r'(?<![\w.])owner(?![\w(:])(?!\s*->)', 'getOwner()', s)
        if s != o:
            src[ln - 1] = s; changed = True
    if changed:
        open(path, 'w').write('\n'.join(src)); print('fixed', path, len(lines))
