import os
import re
import sys

KT_GETTERS = [
    ("getInt", "getIntOr", "0"), ("getFloat", "getFloatOr", "0f"), ("getDouble", "getDoubleOr", "0.0"),
    ("getLong", "getLongOr", "0L"), ("getShort", "getShortOr", "0.toShort()"), ("getByte", "getByteOr", "0.toByte()"),
    ("getBoolean", "getBooleanOr", "false"), ("getString", "getStringOr", "\"\""),
]
JAVA_GETTERS = [
    ("getInt", "getIntOr", "0"), ("getFloat", "getFloatOr", "0.0F"), ("getDouble", "getDoubleOr", "0.0D"),
    ("getLong", "getLongOr", "0L"), ("getShort", "getShortOr", "(short) 0"), ("getByte", "getByteOr", "(byte) 0"),
    ("getBoolean", "getBooleanOr", "false"), ("getString", "getStringOr", "\"\""),
]
ARG = r'((?:"[^"\n]*"|[A-Za-z_][\w.]*(?:\(\))?|\d+))'


def getters(s, kotlin):
    for old, new, default in (KT_GETTERS if kotlin else JAVA_GETTERS):
        s = re.sub(r'\.%s\(%s\)' % (old, ARG), lambda m: '.%s(%s, %s)' % (new, m.group(1), default), s)
    s = re.sub(r'\.getCompound\(%s\)' % ARG, r'.getCompoundOrEmpty(\1)', s)
    s = re.sub(r'\.getList\(%s,\s*(?:Tag\.TAG_\w+|\d+|net\.minecraft\.nbt\.Tag\.TAG_\w+)\)' % ARG, r'.getListOrEmpty(\1)', s)
    s = re.sub(r'\.contains\(%s,\s*(?:Tag\.TAG_\w+|\d+|net\.minecraft\.nbt\.Tag\.TAG_\w+)(?:\.toInt\(\))?\)' % ARG, r'.contains(\1)', s)
    return s


def kotlin_saves(s):
    def repl_read(m):
        indent, mods, name = m.group(1), m.group(2), m.group(3)
        return '%s%sfun readAdditionalSaveData(input: ValueInput) {\n%s    val %s = LegacyNbt.tagOf(input)\n' % (indent, mods, indent, name)
    s2 = re.sub(r'^(\s*)((?:public |protected |override |open |final )*)fun readAdditionalSaveData\((\w+): CompoundTag\) \{\n', repl_read, s, flags=re.M)
    names = re.findall(r'val (\w+) = LegacyNbt\.tagOf\(input\)', s2)
    for n in names:
        s2 = s2.replace('super.readAdditionalSaveData(%s)' % n, 'super.readAdditionalSaveData(input)')

    def repl_add(m):
        indent, mods, name = m.group(1), m.group(2), m.group(3)
        return '%s%sfun addAdditionalSaveData(output: ValueOutput) {\n%s    val %s = LegacyNbt.tagOf(output)\n' % (indent, mods, indent, name)
    s3 = re.sub(r'^(\s*)((?:public |protected |override |open |final )*)fun addAdditionalSaveData\((\w+): CompoundTag\) \{\n', repl_add, s2, flags=re.M)
    for n in re.findall(r'val (\w+) = LegacyNbt\.tagOf\(output\)', s3):
        s3 = s3.replace('super.addAdditionalSaveData(%s)' % n, 'super.addAdditionalSaveData(output)')

    def repl_load(m):
        indent, mods, tag, reg = m.group(1), m.group(2), m.group(3), m.group(4)
        return '%s%sfun loadAdditional(input: ValueInput) {\n%s    val %s = LegacyNbt.tagOf(input)\n%s    val %s = input.lookup()\n' % (indent, mods, indent, tag, indent, reg)
    s4 = re.sub(r'^(\s*)((?:public |protected |override |open |final )*)fun loadAdditional\((\w+): CompoundTag, (\w+): HolderLookup\.Provider\) \{\n', repl_load, s3, flags=re.M)
    s4 = re.sub(r'super\.loadAdditional\(\w+, \w+\)', 'super.loadAdditional(input)', s4)

    def repl_save(m):
        indent, mods, tag, reg = m.group(1), m.group(2), m.group(3), m.group(4)
        return '%s%sfun saveAdditional(output: ValueOutput) {\n%s    val %s = LegacyNbt.tagOf(output)\n%s    val %s = LegacyNbt.lookupOrEmpty(this.level?.registryAccess())\n' % (indent, mods, indent, tag, indent, reg)
    s5 = re.sub(r'^(\s*)((?:public |protected |override |open |final )*)fun saveAdditional\((\w+): CompoundTag, (\w+): HolderLookup\.Provider\) \{\n', repl_save, s4, flags=re.M)
    s5 = re.sub(r'super\.saveAdditional\(\w+, \w+\)', 'super.saveAdditional(output)', s5)
    if s5 != s:
        for imp in ['com.atsuishio.portlib.nbt.LegacyNbt', 'net.minecraft.world.level.storage.ValueInput', 'net.minecraft.world.level.storage.ValueOutput']:
            if 'import %s\n' % imp not in s5:
                s5 = re.sub(r'^(package [^\n]*\n)', r'\1\nimport %s\n' % imp, s5, count=1, flags=re.M)
    return s5


def java_saves(s):
    def rr(m):
        indent, mods, name = m.group(1), m.group(2), m.group(3)
        return '%s%svoid readAdditionalSaveData(ValueInput input) {\n%s    CompoundTag %s = LegacyNbt.tagOf(input);\n' % (indent, mods, indent, name)
    s2 = re.sub(r'^(\s*)((?:@\w+\s+)*(?:public |protected |private )?)void readAdditionalSaveData\((?:@\w+ )?CompoundTag (\w+)\) \{\n', rr, s, flags=re.M)
    for n in re.findall(r'CompoundTag (\w+) = LegacyNbt\.tagOf\(input\);', s2):
        s2 = s2.replace('super.readAdditionalSaveData(%s)' % n, 'super.readAdditionalSaveData(input)')

    def ra(m):
        indent, mods, name = m.group(1), m.group(2), m.group(3)
        return '%s%svoid addAdditionalSaveData(ValueOutput output) {\n%s    CompoundTag %s = LegacyNbt.tagOf(output);\n' % (indent, mods, indent, name)
    s3 = re.sub(r'^(\s*)((?:@\w+\s+)*(?:public |protected |private )?)void addAdditionalSaveData\((?:@\w+ )?CompoundTag (\w+)\) \{\n', ra, s2, flags=re.M)
    for n in re.findall(r'CompoundTag (\w+) = LegacyNbt\.tagOf\(output\);', s3):
        s3 = s3.replace('super.addAdditionalSaveData(%s)' % n, 'super.addAdditionalSaveData(output)')

    def rl(m):
        indent, mods, tag, reg = m.group(1), m.group(2), m.group(3), m.group(4)
        return '%s%svoid loadAdditional(ValueInput input) {\n%s    CompoundTag %s = LegacyNbt.tagOf(input);\n%s    HolderLookup.Provider %s = input.lookup();\n' % (indent, mods, indent, tag, indent, reg)
    s4 = re.sub(r'^(\s*)((?:@\w+\s+)*(?:public |protected )?)void loadAdditional\((?:@\w+ )?CompoundTag (\w+), (?:@\w+ )?HolderLookup\.Provider (\w+)\) \{\n', rl, s3, flags=re.M)
    s4 = re.sub(r'super\.loadAdditional\(\w+, \w+\)', 'super.loadAdditional(input)', s4)

    def rs(m):
        indent, mods, tag, reg = m.group(1), m.group(2), m.group(3), m.group(4)
        return '%s%svoid saveAdditional(ValueOutput output) {\n%s    CompoundTag %s = LegacyNbt.tagOf(output);\n%s    HolderLookup.Provider %s = LegacyNbt.lookupOrEmpty(this.level != null ? this.level.registryAccess() : null);\n' % (indent, mods, indent, tag, indent, reg)
    s5 = re.sub(r'^(\s*)((?:@\w+\s+)*(?:public |protected )?)void saveAdditional\((?:@\w+ )?CompoundTag (\w+), (?:@\w+ )?HolderLookup\.Provider (\w+)\) \{\n', rs, s4, flags=re.M)
    s5 = re.sub(r'super\.saveAdditional\(\w+, \w+\)', 'super.saveAdditional(output)', s5)
    s5 = re.sub(r'(\w+)\.putUUID\(', r'LegacyNbt.putUUID(\1, ', s5)
    s5 = re.sub(r'(\w+)\.getUUID\(', r'LegacyNbt.getUUID(\1, ', s5)
    s5 = re.sub(r'(\w+)\.hasUUID\(', r'LegacyNbt.hasUUID(\1, ', s5)
    if s5 != s:
        for imp in ['com.atsuishio.portlib.nbt.LegacyNbt', 'net.minecraft.world.level.storage.ValueInput', 'net.minecraft.world.level.storage.ValueOutput', 'net.minecraft.nbt.CompoundTag', 'net.minecraft.core.HolderLookup']:
            if 'import %s;' % imp not in s5:
                s5 = re.sub(r'^(package [^\n]*\n)', r'\1\nimport %s;\n' % imp, s5, count=1, flags=re.M)
    return s5


def main(roots, ext_pkg):
    n = 0
    for root in roots:
        for d, _, fs in os.walk(root):
            for f in fs:
                if not (f.endswith('.kt') or f.endswith('.java')):
                    continue
                p = os.path.join(d, f)
                s = open(p, encoding='utf-8').read()
                kotlin = f.endswith('.kt')
                o = getters(s, kotlin)
                o = kotlin_saves(o) if kotlin else java_saves(o)
                if kotlin and ext_pkg and ('import %s.*' % ext_pkg) not in o and not o.startswith('package %s\n' % ext_pkg):
                    o = re.sub(r'^(package [^\n]*\n)', r'\1\nimport %s.*\n' % ext_pkg, o, count=1, flags=re.M)
                if o != s:
                    open(p, 'w', encoding='utf-8').write(o)
                    n += 1
    print('changed', n)


if __name__ == '__main__':
    main(sys.argv[2:], sys.argv[1])
