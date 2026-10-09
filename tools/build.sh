#!/bin/bash
cd /home/user/sbw-port
export JAVA_HOME=/opt/jdk25
L=/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad/$1.log
shift
./gradlew ${ONLINE:+--refresh-dependencies} $([ -z "$ONLINE" ] && echo --offline) "$@" > $L 2>&1
echo "exit=$? errors=$(grep -cE '(error:|^e: )' $L)"
grep -E 'BUILD (SUCCESSFUL|FAILED)' $L
