import os
import re
import sys


def replace_call(s, prefix, replacement):
    out = []
    i = 0
    while True:
        j = s.find(prefix, i)
        if j < 0:
            out.append(s[i:])
            break
        if j > 0 and (s[j - 1].isalnum() or s[j - 1] == '_'):
            out.append(s[i:j + len(prefix)])
            i = j + len(prefix)
            continue
        k = j + len(prefix)
        if k >= len(s) or s[k] != '(':
            out.append(s[i:k])
            i = k
            continue
        depth = 0
        m = k
        while m < len(s):
            if s[m] == '(':
                depth += 1
            elif s[m] == ')':
                depth -= 1
                if depth == 0:
                    break
            m += 1
        out.append(s[i:j])
        out.append(replacement)
        i = m + 1
    return ''.join(out)


MAP = [
    ('InteractionResultHolder.success', 'InteractionResult.SUCCESS'),
    ('InteractionResultHolder.sidedSuccess', 'InteractionResult.SUCCESS'),
    ('InteractionResultHolder.consume', 'InteractionResult.CONSUME'),
    ('InteractionResultHolder.pass', 'InteractionResult.PASS'),
    ('InteractionResultHolder.fail', 'InteractionResult.FAIL'),
    ('InteractionResult.sidedSuccess', 'InteractionResult.SUCCESS'),
    ('ItemInteractionResult.sidedSuccess', 'InteractionResult.SUCCESS'),
]


def migrate(s):
    o = s
    for prefix, rep in MAP:
        o = replace_call(o, prefix, rep)
    o = re.sub(r'\bInteractionResultHolder<ItemStack\??>', 'InteractionResult', o)
    o = re.sub(r'\b(InteractionResultHolder\.\w+)<ItemStack\??>\(', r'\1(', o)
    o = re.sub(r'\bItemInteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION\b', 'InteractionResult.TRY_WITH_EMPTY_HAND', o)
    o = re.sub(r'\bItemInteractionResult\.SKIP_DEFAULT_BLOCK_INTERACTION\b', 'InteractionResult.CONSUME', o)
    o = re.sub(r'\bItemInteractionResult\.(SUCCESS|CONSUME|FAIL)\b', r'InteractionResult.\1', o)
    o = re.sub(r'\bItemInteractionResult\b', 'InteractionResult', o)
    o = re.sub(r'\bInteractionResult\.CONSUME_PARTIAL\b', 'InteractionResult.CONSUME', o)
    o = re.sub(r'^import net\.minecraft\.world\.InteractionResultHolder;?\n', '', o, flags=re.M)
    o = re.sub(r'^import net\.minecraft\.world\.InteractionResult;?\n(?=(?:.*\n)*?import net\.minecraft\.world\.InteractionResult;?\n)', '', o, flags=re.M)
    if 'InteractionResult.' in o or ': InteractionResult' in o:
        kotlin = None
    return o


def main(roots):
    n = 0
    for root in roots:
        for d, _, fs in os.walk(root):
            for f in fs:
                if not (f.endswith('.kt') or f.endswith('.java')):
                    continue
                p = os.path.join(d, f)
                s = open(p, encoding='utf-8').read()
                o = migrate(s)
                if o != s:
                    kotlin = f.endswith('.kt')
                    imp = 'import net.minecraft.world.InteractionResult' + ('' if kotlin else ';')
                    if imp + '\n' not in o:
                        o = re.sub(r'^(package [^\n]*\n)', r'\1\n%s\n' % imp, o, count=1, flags=re.M)
                    open(p, 'w', encoding='utf-8').write(o)
                    n += 1
    print('changed', n)


if __name__ == '__main__':
    main(sys.argv[1:])
