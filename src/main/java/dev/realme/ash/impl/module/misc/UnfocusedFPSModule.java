package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.FramerateLimitEvent;

public class UnfocusedFPSModule
extends ToggleModule {
    Config<Integer> limitConfig = new NumberConfig<Integer>("Limit", "The FPS limit when game is in the background", 5, 30, 120);

    public UnfocusedFPSModule() {
        super("UnfocusedFPS", "Reduces FPS when game is in the background", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onFramerateLimit(FramerateLimitEvent event) {
        if (!mc.isWindowFocused()) {
            event.cancel();
            event.setFramerateLimit(this.limitConfig.getValue());
        }
    }
}
