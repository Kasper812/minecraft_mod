import re,os,sys,glob
MC='/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad/mc262'
def find_class(fq):
    p=fq.replace('.','/')
    for root in ['common','clientOnly']:
        f=f'{MC}/{root}/{p}.java'
        if os.path.exists(f): return f
    outer=p.rsplit('/',1)[0]
    for root in ['common','clientOnly']:
        f=f'{MC}/{root}/{outer}.java'
        if os.path.exists(f): return f
    return None
for f in sorted(glob.glob(sys.argv[1]+'/**/*.java',recursive=True)):
    s=open(f).read()
    m=re.search(r'@Mixin\((?:value\s*=\s*)?\{?([^)}]*)\}?',s)
    if not m: continue
    targets=re.findall(r'([\w.]+)\.class',m.group(1))
    imports=dict((i.split('.')[-1],i) for i in re.findall(r'import ([\w.]+);',s))
    pkg=re.search(r'package ([\w.]+);',s).group(1)
    srcs=[]
    for t in targets:
        fq=imports.get(t.split('.')[0],t)
        if '.' in t and t.split('.')[0] in imports: fq=imports[t.split('.')[0]]+'.'+'.'.join(t.split('.')[1:])
        src=find_class(fq)
        srcs.append((t,src))
    methods=re.findall(r'method\s*=\s*(\{[^}]*\}|"[^"]*")',s)
    names=[]
    for mm in methods:
        names+=re.findall(r'"([^"]*)"',mm)
    ats=re.findall(r'target\s*=\s*"([^"]*)"',s)
    out=[]
    for t,src in srcs:
        if not src:
            out.append(f'  TARGET MISSING {t}'); continue
        code=open(src).read()
        for n in names:
            base=n.split('(')[0].split('*')[0]
            if base in ('<init>','<clinit>'): continue
            if not re.search(r'\b'+re.escape(base)+r'\s*\(',code):
                out.append(f'  method? {n}')
    for a in ats:
        mm=re.match(r'L([\w/$]+);(\w+)',a)
        if mm:
            cls=mm.group(1).replace('/','.').replace('$','.')
            src=find_class(cls)
            if not src: out.append(f'  at-class? {a}')
            elif not re.search(r'\b'+re.escape(mm.group(2))+r'\b',open(src).read()):
                out.append(f'  at-member? {a}')
    shadows=re.findall(r'@Shadow[^;{]*?\s(\w+)\s*(?:\(|;|=)',s)
    for sh in shadows:
        found=any(src and re.search(r'\b'+re.escape(sh)+r'\b',open(src).read()) for t,src in srcs)
        if not found: out.append(f'  shadow? {sh}')
    rel=f.split('/mixins/')[-1]
    print(('OK   ' if not out else 'FIX  ')+rel+' -> '+','.join(t for t,_ in srcs))
    for o in out: print(o)
