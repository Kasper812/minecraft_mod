import re,sys
L=sys.argv[1]; filt=sys.argv[2] if len(sys.argv)>2 else ''
seen=set()
for line in open(L):
    m=re.match(r'e: file://(\S+?):(\d+):(\d+) (.*)',line)
    if not m: continue
    f,ln,col,msg=m.groups()
    if filt and not re.search(filt,f+' '+msg): continue
    key=(f,ln)
    if key in seen: continue
    seen.add(key)
    try: src=open(f).read().split('\n')[int(ln)-1].strip()
    except: src='?'
    short=f.split('/superbwarfare/')[-1]
    print(f'{short}:{ln}: {msg[:140]}\n    > {src[:200]}')
