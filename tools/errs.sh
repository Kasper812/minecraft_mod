#!/bin/bash
L=/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad/${LOG:-sbw}.log
grep '^e: ' $L | grep "$1" | sed -E 's#^e: file://[^ ]*/([A-Za-z0-9_]+\.kt):#\1:#' | cut -c1-${W:-260}
