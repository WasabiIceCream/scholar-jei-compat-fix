# Scholar JEI compat fix

Patch tool for Scholar 1.2.5 (Fabric, 26.1.2; `github.com/mortuusars/Scholar`, GPLv3). JEI 29.43
moved `UserInput` and `IUserInputHandler` from `mezz.jei.gui.input` to `mezz.jei.common.input`.
Scholar's `JeiKeyConflictContextMixin` (which keeps JEI's global hotkeys from firing while a
Scholar book is being written or signed) still used the old names, so on every world join the
mixin failed with "Invalid descriptor", JEI's `GlobalInputHandler` failed to load, and JEI logged
"Caught an error from mod plugin ... FabricGuiPlugin".

Upstream fixed exactly this in commit `eb775040` ("Compat with newest JEI", Scholar 1.2.6), but
only on its 1.21.1 branch; 1.2.5 is still the newest 26.1.2 release. `Remap.java` applies the same
change to the compiled class with ASM's `ClassRemapper` (the names change length, so a byte patch
won't do); every other jar entry is copied unchanged.

    ./patch.sh "../../fabric 26.1/mods/scholar-fabric-26.1.2-1.2.5.jar"         # drop (default), in place, keeps .bak
    ./patch.sh "../../fabric 26.1/mods/scholar-fabric-26.1.2-1.2.5.jar" remap   # JEI 29.43-29.46 only

**Since 2026-10-24 (JEI 29.47) the default mode drops the mixin instead.** JEI 29.47 rewrote its input handling
and removed `GlobalInputHandler` and `IUserInputHandler`, so no remap can make the mixin apply. Upstream deleted
the mixin on 2026-10-13 (commit `7a75d647`, Scholar 1.2.10, "Remove no longer needed JEI mixin and version
restriction"; released for 1.21.1, 26.2 and 26.3, not 26.1.2). `drop_jei_mixin.py` makes the same change: it removes
`jei.JeiKeyConflictContextMixin` from `scholar-common.mixins.json` and deletes the class. Always patch the stock
jar from Modrinth, never an already-remapped one.

Needs ASM 9.10.1 (`asm`, `asm-commons`, `asm-tree`) from PrismLauncher's library folder, or set
`ASM_DIR`. Scholar is a both-sides mod, so the patched jar goes in `fabric 26.1/mods/`. Drop this
once a 26.1.2 Scholar release includes the fix (`scripts/upstream_watch.json`).

Wholly original tool, MIT. The patched jar's source is upstream's public source plus commit
`eb775040`.
