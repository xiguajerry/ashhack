package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.JumpDelayEvent;

public class NoJumpDelayModule
extends ToggleModule {
    public NoJumpDelayModule() {
        super("NoJumpDelay", "Removes the vanilla jump delay", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onJumpDelay(JumpDelayEvent event) {
        event.cancel();
    }
}
