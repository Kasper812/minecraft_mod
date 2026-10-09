import re, sys
log = sys.argv[1]
lines = open(log).read().split('\n')
targets = set()
for i, l in enumerate(lines):
    if 'does not implement abstract' in l and i + 1 < len(lines) and 'hurtServer' in lines[i + 1]:
        m = re.match(r'e: file://(\S+?\.kt):(\d+):', l)
        if m: targets.add((m.group(1), int(m.group(2))))
for path, ln in targets:
    s = open(path).read().split('\n')
    i = ln - 1
    while '{' not in s[i]: i += 1
    s.insert(i + 1, '    override fun hurtServer(serverLevel: net.minecraft.server.level.ServerLevel, source: net.minecraft.world.damagesource.DamageSource, amount: Float): Boolean {\n        if (!this.isInvulnerableToBase(source)) this.markHurt()\n        return false\n    }\n')
    open(path, 'w').write('\n'.join(s)); print('added', path)
