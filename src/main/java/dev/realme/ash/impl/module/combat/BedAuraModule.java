package dev.realme.ash.impl.module.combat;

import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;

public class BedAuraModule
extends ToggleModule {
    public BedAuraModule() {
        super("BedAura", "Automatically places and explodes beds", ModuleCategory.COMBAT);
    }
}
