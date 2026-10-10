#!/bin/bash
S=/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad
for p in $(pgrep java); do grep -qa devlaunchinjector /proc/$p/cmdline 2>/dev/null && kill $p; done
sleep 2
rm -f $S/cli.log
nohup /home/user/sbw-port/tools/client.sh cli ${1:-3000} > /dev/null 2>&1 &
sleep 10
until grep -q "Loaded 1 particle definitions\|Minecraft has crashed\|BUILD FAILED\|exit=" $S/cli.log 2>/dev/null; do sleep 3; done
if grep -q "Minecraft has crashed\|BUILD FAILED" $S/cli.log; then echo CRASHED; exit 1; fi
until grep -q "logged in with entity id\|Minecraft has crashed\|joined the game" $S/cli.log 2>/dev/null; do sleep 3; done
sleep 15
grep -q "Minecraft has crashed" $S/cli.log && echo CRASHED && exit 1
echo READY
