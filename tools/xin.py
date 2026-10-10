import os, sys, time
from Xlib import X, XK, display
from Xlib.ext import xtest


def find_env():
    for p in os.listdir("/proc"):
        if not p.isdigit():
            continue
        try:
            cmd = open(f"/proc/{p}/cmdline", "rb").read()
        except Exception:
            continue
        if b"devlaunchinjector" in cmd and cmd.startswith(b"/opt/jdk25/bin/java"):
            env = dict(l.split(b"=", 1) for l in open(f"/proc/{p}/environ", "rb").read().split(b"\0") if b"=" in l)
            os.environ["DISPLAY"] = env[b"DISPLAY"].decode()
            if b"XAUTHORITY" in env:
                os.environ["XAUTHORITY"] = env[b"XAUTHORITY"].decode()
            return
    sys.exit("no client")


find_env()
d = display.Display()


def key(name, hold=0.08):
    kc = d.keysym_to_keycode(XK.string_to_keysym(name))
    xtest.fake_input(d, X.KeyPress, kc)
    d.sync()
    time.sleep(hold)
    xtest.fake_input(d, X.KeyRelease, kc)
    d.sync()


def down(name):
    xtest.fake_input(d, X.KeyPress, d.keysym_to_keycode(XK.string_to_keysym(name)))
    d.sync()


def up(name):
    xtest.fake_input(d, X.KeyRelease, d.keysym_to_keycode(XK.string_to_keysym(name)))
    d.sync()


def move(x, y):
    xtest.fake_input(d, X.MotionNotify, x=x, y=y)
    d.sync()


def click(x, y, b=1):
    move(x, y)
    time.sleep(0.05)
    xtest.fake_input(d, X.ButtonPress, b)
    d.sync()
    time.sleep(0.08)
    xtest.fake_input(d, X.ButtonRelease, b)
    d.sync()


def type_text(s):
    for ch in s:
        if ch == " ":
            key("space")
        elif ch == "/":
            key("slash")
        elif ch == "_":
            down("Shift_L"); key("minus"); up("Shift_L")
        elif ch == ":":
            down("Shift_L"); key("semicolon"); up("Shift_L")
        elif ch == ".":
            key("period")
        elif ch == "-":
            key("minus")
        elif ch == "@":
            down("Shift_L"); key("2"); up("Shift_L")
        elif ch == "[":
            key("bracketleft")
        elif ch == "]":
            key("bracketright")
        elif ch == "=":
            key("equal")
        elif ch == ",":
            key("comma")
        elif ch == "~":
            down("Shift_L"); key("grave"); up("Shift_L")
        elif ch.isupper():
            down("Shift_L"); key(ch.lower()); up("Shift_L")
        else:
            key(ch)
        time.sleep(0.02)


for a in sys.argv[1:]:
    op, _, arg = a.partition(":")
    if op == "key":
        key(arg)
    elif op == "down":
        down(arg)
    elif op == "up":
        up(arg)
    elif op == "click":
        x, y = arg.split(",")[:2]
        click(int(x), int(y), int(arg.split(",")[2]) if arg.count(",") == 2 else 1)
    elif op == "move":
        x, y = arg.split(",")
        move(int(x), int(y))
    elif op == "type":
        type_text(arg)
    elif op == "sleep":
        time.sleep(float(arg))
    elif op == "cmd":
        key("t")
        time.sleep(0.4)
        type_text(arg)
        time.sleep(0.2)
        key("Return")
        time.sleep(0.3)
