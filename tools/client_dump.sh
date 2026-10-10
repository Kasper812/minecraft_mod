#!/bin/bash
S=/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad
P=$(pgrep -f "^/opt/jdk25/bin/java .*Main" | while read p; do grep -qa "devlaunchinjector" /proc/$p/cmdline && echo $p; done | head -1)
echo "pid=$P"
export DISPLAY=$(tr '\0' '\n' < /proc/$P/environ | grep ^DISPLAY= | cut -d= -f2)
export XAUTHORITY=$(tr '\0' '\n' < /proc/$P/environ | grep ^XAUTHORITY= | cut -d= -f2)
import -window root $S/${1:-shot}.png
/opt/jdk25/bin/jstack $P > $S/dump.txt 2>&1
