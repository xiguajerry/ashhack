package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.LevitationEvent;

public class AntiLevitationModule
extends ToggleModule {
    public AntiLevitationModule() {
        super("AntiLevitation", "Prevents the player from being levitated", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onLevitation(LevitationEvent event) {
        event.cancel();
    }
}
