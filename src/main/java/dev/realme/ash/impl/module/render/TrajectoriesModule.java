package dev.realme.ash.impl.module.render;

import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;

public class TrajectoriesModule
extends ToggleModule {
    public TrajectoriesModule() {
        super("Trajectories", "Renders the trajectory path of projectiles", ModuleCategory.RENDER);
    }
}
