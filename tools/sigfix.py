import re, sys
def ensure_import(s, imp):
    if f'import {imp}\n' in s: return s
    return re.sub(r'^(package .*\n)', r'\1\nimport ' + imp + '\n', s, count=1, flags=re.M)
for path in sys.argv[1:]:
    s = open(path).read(); o = s
    s, n1 = re.subn(r'override fun interact\(\s*(\w+): Player,\s*(\w+): InteractionHand\s*\): InteractionResult', r'override fun interact(\1: Player, \2: InteractionHand, location: Vec3): InteractionResult', s)
    s = re.sub(r'super\.interact\(\s*(\w+),\s*(\w+)\s*\)', r'super.interact(\1, \2, location)', s)
    s = s.replace('override fun canBeCollidedWith(): Boolean', 'override fun canBeCollidedWith(other: Entity?): Boolean')
    s = s.replace('super.canBeCollidedWith()', 'super.canBeCollidedWith(other)')
    s = s.replace('override fun updateInWaterStateAndDoFluidPushing(): Boolean', 'override fun updateFluidInteraction(): Boolean')
    s = s.replace('super.updateInWaterStateAndDoFluidPushing()', 'super.updateFluidInteraction()')
    s = re.sub(r'\bisControlledByLocalInstance\b', 'isLocalInstanceAuthoritative', s)
    if s != o:
        if 'location: Vec3' in s: s = ensure_import(s, 'net.minecraft.world.phys.Vec3')
        if 'other: Entity?' in s: s = ensure_import(s, 'net.minecraft.world.entity.Entity')
        open(path, 'w').write(s); print('fixed', path)
