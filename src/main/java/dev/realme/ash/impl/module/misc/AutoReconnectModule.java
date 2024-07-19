package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;

public class AutoReconnectModule
extends ToggleModule {
    final Config<Integer> delayConfig = new NumberConfig<>("Delay", "The delay between reconnects to a server", 0, 5, 100);

    public AutoReconnectModule() {
        super("AutoReconnect", "Automatically reconnects to a server immediately after disconnecting", ModuleCategory.MISCELLANEOUS);
    }

    public int getDelay() {
        return this.delayConfig.getValue();
    }
}
