#!/bin/bash
# Usage: ./patch.sh <scholar-fabric-26.1.2-1.2.5.jar> [drop|remap]   (patches in place, keeps a .bak)
#   drop  (default): remove the JEI mixin, for JEI 29.47+ (upstream Scholar 1.2.10's change)
#   remap: rename the mixin's JEI classes for JEI 29.43-29.46 (upstream commit eb775040)
set -euo pipefail
JAR="$(realpath "${1:?usage: patch.sh <scholar jar> [drop|remap]}")"
MODE="${2:-drop}"
cd "$(dirname "$0")"
[ -f "$JAR.bak" ] || cp "$JAR" "$JAR.bak"
case "$MODE" in
  drop)
    python3 -I drop_jei_mixin.py "$JAR.bak" "$JAR.tmp"
    ;;
  remap)
    A=${ASM_DIR:-~/.local/share/PrismLauncher/libraries/org/ow2/asm}
    CP="$A/asm/9.10.1/asm-9.10.1.jar:$A/asm-commons/9.10.1/asm-commons-9.10.1.jar:$A/asm-tree/9.10.1/asm-tree-9.10.1.jar"
    mkdir -p out
    javac -nowarn -cp "$CP" -d out Remap.java
    java -cp "out:$CP" Remap "$JAR.bak" "$JAR.tmp"
    ;;
  *) echo "unknown mode: $MODE" >&2; exit 1 ;;
esac
mv "$JAR.tmp" "$JAR"
