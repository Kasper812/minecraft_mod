import re,sys
L=sys.argv[1]; filt=sys.argv[2] if len(sys.argv)>2 else ''
lines=open(L).read().split('\n')
seen=set()
for i,line in enumerate(lines):
    m=re.match(r'\s*(/\S+\.java):(\d+): error: (.*)',line)
    if not m: continue
    f,ln,msg=m.groups()
    if filt and not re.search(filt,f+' '+msg): continue
    sym=''
    for j in range(i+1,min(i+6,len(lines))):
        if 'symbol:' in lines[j] or 'location:' in lines[j]:
            sym+=' | '+lines[j].strip()
        if re.match(r'\s*/\S+\.java:\d+: error',lines[j]): break
    key=(f,ln,msg,sym)
    if key in seen: continue
    seen.add(key)
    try: src=open(f).read().split('\n')[int(ln)-1].strip()
    except: src='?'
    short=f.split('/superbwarfare/')[-1]
    print(f'{short}:{ln}: {msg[:120]}{sym[:160]}\n    > {src[:180]}')
