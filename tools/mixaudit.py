import os, re, subprocess, sys

MC = sys.argv[1]
ROOTS = sys.argv[2:]
JAVAP = "/opt/jdk25/bin/javap"
cache = {}


def javap(binname):
    if binname in cache:
        return cache[binname]
    path = os.path.join(MC, binname.replace(".", "/") + ".class")
    if not os.path.exists(path):
        cache[binname] = None
        return None
    out = subprocess.run([JAVAP, "-p", "-c", "-s", path], capture_output=True, text=True).stdout
    methods = {}
    fields = set()
    cur = None
    lines = out.splitlines()
    i = 0
    while i < len(lines):
        l = lines[i]
        if l.startswith("  ") and not l.startswith("    ") and (l.rstrip().endswith(";") or l.rstrip().endswith("{")):
            decl = l.strip()
            desc = None
            if i + 1 < len(lines) and lines[i + 1].strip().startswith("descriptor:"):
                desc = lines[i + 1].strip().split(":", 1)[1].strip()
            if decl.startswith("static {}"):
                cur = []
                methods.setdefault("<clinit>", []).append(("()V", cur))
            elif "(" in decl and desc:
                m = re.search(r"([\w$<>]+)\(", decl)
                name = m.group(1)
                if name == binname.split(".")[-1]:
                    name = "<init>"
                if decl.startswith("static {"):
                    name = "<clinit>"
                cur = []
                methods.setdefault(name, []).append((desc, cur))
            elif desc:
                name = re.findall(r"([\w$]+);$", decl)
                if name:
                    fields.add(name[0])
                cur = None
            elif decl.startswith("static {}") or decl == "static {};":
                cur = []
                methods.setdefault("<clinit>", []).append(("()V", cur))
        elif cur is not None and "//" in l:
            cur.append(l.split("//", 1)[1].strip())
        i += 1
    cache[binname] = (methods, fields)
    return cache[binname]


def resolve(cls, imports, pkg):
    head = cls.split(".")[0]
    rest = cls.split(".")[1:]
    if head in imports:
        base = imports[head]
        if not javap(base) and javap(base.rsplit(".",1)[0]) is not None:
            base = base.rsplit(".",1)[0] + "$" + base.rsplit(".",1)[1]
    elif "." in cls and cls[0].islower():
        parts = cls.split(".")
        for k in range(len(parts), 0, -1):
            cand = ".".join(parts[:k]) + ("$" + "$".join(parts[k:]) if parts[k:] else "")
            if javap(cand):
                return cand
        return cls
    else:
        base = pkg + "." + head
    return base + "".join("$" + r for r in rest)


ANN = r"@(Inject|WrapOperation|Redirect|ModifyArg|ModifyArgs|ModifyVariable|ModifyReturnValue|WrapWithCondition|ModifyExpressionValue|WrapMethod|ModifyConstant|ModifyReceiver)\b"

problems = 0
for root in ROOTS:
    for dp, dn, fn in os.walk(root):
        if "/build/" in dp + "/":
            continue
        for f in fn:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            src = open(p, encoding="utf-8").read()
            if "@Mixin" not in src:
                continue
            imports = {m.group(2): m.group(1) + m.group(2) for m in re.finditer(r"import\s+([\w.]+\.)(\w+);", src)}
            pkg = re.search(r"package\s+([\w.]+);", src).group(1)
            mm = re.search(r"@Mixin\s*\((.*?)\)\s*(?:@\w+(?:\([^)]*\))?\s*)*(public|abstract|class|interface|final)", src, re.S)
            targets = []
            for c in re.findall(r"([\w.]+)\.class", mm.group(1)):
                targets.append(resolve(c, imports, pkg))
            for t in re.findall(r'targets\s*=\s*\{?\s*"([^"]+)"', mm.group(1)):
                targets.append(t.replace("/", "."))
            infos = [(t, javap(t)) for t in targets]
            for t, info in infos:
                if info is None and (t.startswith("net.minecraft") or t.startswith("com.mojang")):
                    print(f"{p}: target class missing {t}")
                    problems += 1
            infos = [(t, i) for t, i in infos if i]
            if not infos:
                continue
            for am in re.finditer(ANN, src):
                start = am.start()
                end = src.find("{", start)
                semi = re.search(r"\)\s*(private|public|protected|static|void|\w+\s+\w+\()", src[start:])
                block = src[start:end]
                meth = re.search(r"method\s*=\s*(\{[^}]*\}|\"[^\"]*\")", block, re.S)
                if not meth:
                    continue
                sels = re.findall(r'"([^"]*)"', meth.group(1))
                require0 = re.search(r"require\s*=\s*0", block) is not None
                ats = re.findall(r'target\s*=\s*"([^"]+)"', block)
                for t, (methods, fields) in infos:
                    for sel in sels:
                        name = sel.split("(")[0]
                        desc = sel[len(name):] if "(" in sel else None
                        if name not in methods:
                            print(f"{p}:{src[:start].count(chr(10))+1}: {am.group(1)} method '{sel}' not in {t}{' (require=0)' if require0 else ''}")
                            problems += 1
                            continue
                        cands = [b for d, b in methods[name] if desc is None or d == desc]
                        if not cands:
                            print(f"{p}:{src[:start].count(chr(10))+1}: {am.group(1)} desc mismatch '{sel}' in {t}: have {[d for d, _ in methods[name]]}")
                            problems += 1
                            continue
                        for at in ats:
                            m2 = re.match(r"L([\w/$]+);([\w$<>]+)(.*)", at)
                            if not m2:
                                continue
                            owner, nm, rest = m2.groups()
                            if rest.startswith(":"):
                                key = f"Field {owner}.{nm}:{rest[1:]}"
                            else:
                                key = f"{owner}.{nm}:{rest}"
                            selfkey = None
                            if owner.replace("/", ".") == t:
                                selfkey = (f"Field {nm}:{rest[1:]}" if rest.startswith(":") else f"Method {nm}:{rest}")
                            found = any(any(key in ins or (selfkey and ins.endswith(selfkey)) or (selfkey and ("InterfaceMethod " + nm + ":" + rest) in ins) for ins in body) for body in cands)
                            if not found:
                                alt = any(any(f".{nm}:" in ins for ins in body) for body in cands)
                                print(f"{p}:{src[:start].count(chr(10))+1}: {am.group(1)} in '{sel}': target '{at}' not found{' (name present w/ other owner/desc)' if alt else ''}")
                                problems += 1
            for sm in re.finditer(r"@Shadow[^;{]*?\s([\w$]+)\s*(\(|;|=)", src):
                nm = sm.group(1)
                ok = any(nm in i[1] or nm in i[0] for _, i in infos)
                if not ok:
                    print(f"{p}: @Shadow {nm} not found in {[t for t,_ in infos]}")
                    problems += 1
            for sm in re.finditer(r"@(Accessor|Invoker)\s*(\(\s*(value\s*=\s*)?\"([^\"]+)\"\s*\))?[^;]*?\s(\w+)\s*\(", src):
                nm = sm.group(4)
                if not nm:
                    g = sm.group(5)
                    nm = re.sub(r"^(get|set|is|call|invoke)", "", g)
                    nm = nm[:1].lower() + nm[1:]
                ok = any(nm in i[1] or nm in i[0] for _, i in infos)
                if not ok:
                    print(f"{p}: @{sm.group(1)} {nm} not found")
                    problems += 1
print("problems:", problems)
