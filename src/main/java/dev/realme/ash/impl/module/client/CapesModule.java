package dev.realme.ash.impl.module.client;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;

public class CapesModule
extends ToggleModule {
    Config<Boolean> optifineConfig = new BooleanConfig("Optifine", "If to show optifine capes", true);

    public CapesModule() {
        super("Capes", "Shows player capes", ModuleCategory.CLIENT);
        this.enable();
    }

    public Config<Boolean> getOptifineConfig() {
        return this.optifineConfig;
    }
}
