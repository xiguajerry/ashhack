package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.ScreenOpenEvent;
import dev.realme.ash.impl.event.TickEvent;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawnModule
extends ToggleModule {
    private boolean respawn;

    public AutoRespawnModule() {
        super("AutoRespawn", "Respawns automatically after a death", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && this.respawn && AutoRespawnModule.mc.player.isDead()) {
            AutoRespawnModule.mc.player.requestRespawn();
            this.respawn = false;
        }
    }

    @EventListener
    public void onScreenOpen(ScreenOpenEvent event) {
        if (event.getScreen() instanceof DeathScreen) {
            this.respawn = true;
        }
    }
}
