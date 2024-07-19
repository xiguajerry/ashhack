package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.PlayerTickEvent;

public class ParkourModule
extends ToggleModule {
    private boolean override;

    public ParkourModule() {
        super("Parkour", "Automatically jumps at the edge of blocks", ModuleCategory.MOVEMENT);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if (this.override) {
            this.override = false;
            ParkourModule.mc.options.jumpKey.setPressed(false);
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (ParkourModule.mc.player.isOnGround() && !ParkourModule.mc.player.isSneaking() && ParkourModule.mc.world.isSpaceEmpty(ParkourModule.mc.player.getBoundingBox().offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001))) {
            ParkourModule.mc.options.jumpKey.setPressed(true);
            this.override = true;
        } else if (this.override) {
            this.override = false;
            ParkourModule.mc.options.jumpKey.setPressed(false);
        }
    }
}
