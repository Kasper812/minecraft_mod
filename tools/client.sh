#!/bin/bash
cd /home/user/sbw-port
export JAVA_HOME=/opt/jdk25
L=/tmp/claude-0/-home-user-minecraft-mod/bc710cc3-b91f-504b-8b03-3b2f43d6ad63/scratchpad/${1:-cli}.log
JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS $EXTRA_JVM" timeout ${2:-420} xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew --offline :superbwarfare:runClient --args="--quickPlaySingleplayer test" > $L 2>&1
echo "exit=$?"
