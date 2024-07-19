package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;

public class AutoTunnelModule
extends ToggleModule {
    public AutoTunnelModule() {
        super("AutoTunnel", "Automatically mines a tunnel", ModuleCategory.WORLD);
    }
}
