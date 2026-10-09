import re, sys, collections
log = sys.argv[1]
errs = collections.defaultdict(set)
for line in open(log):
    m = re.match(r"e: file://(\S+?\.kt):(\d+):\d+ (.*)", line)
    if not m: continue
    if "nullable receiver of type 'Entity?'" in m.group(3) or "actual type is 'Vec3?', but 'Vec3'" in m.group(3) or "actual type is 'Entity?'" in m.group(3):
        errs[m.group(1)].add(int(m.group(2)))
for path, lines in errs.items():
    src = open(path).read().split('\n')
    changed = False
    for ln in lines:
        for k in range(ln - 1, max(ln - 60, 0), -1):
            if re.search(r'\bval owner\b', src[k]):
                for j in range(k + 1, min(ln + 3, len(src))):
                    n = src[j].replace('getOwner()', 'owner')
                    if n != src[j]: src[j] = n; changed = True
                break
    if changed:
        open(path, 'w').write('\n'.join(src)); print('reverted', path)
