#!/bin/bash
# Usage: ./patch.sh <scholar-fabric-26.1.2-1.2.5.jar>   (patches in place, keeps a .bak)
set -euo pipefail
JAR="$(realpath "${1:?usage: patch.sh <scholar jar>}")"
cd "$(dirname "$0")"
A=${ASM_DIR:-~/.local/share/PrismLauncher/libraries/org/ow2/asm}
CP="$A/asm/9.10.1/asm-9.10.1.jar:$A/asm-commons/9.10.1/asm-commons-9.10.1.jar:$A/asm-tree/9.10.1/asm-tree-9.10.1.jar"
mkdir -p out
javac -nowarn -cp "$CP" -d out Remap.java
[ -f "$JAR.bak" ] || cp "$JAR" "$JAR.bak"
java -cp "out:$CP" Remap "$JAR.bak" "$JAR.tmp"
mv "$JAR.tmp" "$JAR"
