package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;

public class AutoWalkModule
extends ToggleModule {
    Config<Boolean> lockConfig = new BooleanConfig("Lock", "Stops movement when sneaking or jumping", false);

    public AutoWalkModule() {
        super("AutoWalk", "Automatically moves forward", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        AutoWalkModule.mc.options.forwardKey.setPressed(false);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            AutoWalkModule.mc.options.forwardKey.setPressed(!AutoWalkModule.mc.options.sneakKey.isPressed() && (this.lockConfig.getValue() == false || !AutoWalkModule.mc.options.jumpKey.isPressed() && AutoWalkModule.mc.player.isOnGround()));
        }
    }
}
