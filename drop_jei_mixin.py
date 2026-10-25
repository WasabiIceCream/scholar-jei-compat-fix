#!/usr/bin/env python3
"""Remove Scholar's JEI mixin from a Scholar jar, the same change as upstream commit 7a75d647 (Scholar 1.2.10,
2026-10-13, "Remove no longer needed JEI mixin and version restriction"; released for 1.21.1, 26.2 and 26.3 only).

JEI 29.47 rewrote its input handling and removed the classes JeiKeyConflictContextMixin targets
(GlobalInputHandler, IUserInputHandler), so the mixin can't apply there at all. Drops the
"jei.JeiKeyConflictContextMixin" entry from scholar-common.mixins.json and the mixin class itself; every other
entry is copied unchanged.

Usage: drop_jei_mixin.py <in.jar> <out.jar>
"""
import json
import sys
import zipfile

MIXIN = "jei.JeiKeyConflictContextMixin"
CONFIG = "scholar-common.mixins.json"
CLASS = "io/github/mortuusars/scholar/mixin/jei/JeiKeyConflictContextMixin.class"


def main(src, dst):
    with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
        names = zin.namelist()
        if CONFIG not in names:
            sys.exit(f"{CONFIG} not found in {src}")
        for info in zin.infolist():
            if info.filename == CLASS:
                continue
            data = zin.read(info.filename)
            if info.filename == CONFIG:
                config = json.loads(data)
                for key in ("mixins", "client", "server"):
                    if MIXIN in config.get(key, []):
                        config[key].remove(MIXIN)
                data = (json.dumps(config, indent=2) + "\n").encode()
            zout.writestr(info, data)
    print(f"removed {MIXIN} -> {dst}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(*sys.argv[1:])
