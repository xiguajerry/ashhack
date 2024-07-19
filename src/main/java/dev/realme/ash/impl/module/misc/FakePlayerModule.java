package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.entity.player.PushEntityEvent;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.util.world.FakePlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class FakePlayerModule
extends ToggleModule {
    private FakePlayerEntity fakePlayer;

    public FakePlayerModule() {
        super("FakePlayer", "Spawns an indestructible client-side player", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable() {
        if (FakePlayerModule.mc.player != null && FakePlayerModule.mc.world != null) {
            this.fakePlayer = new FakePlayerEntity(FakePlayerModule.mc.player, "FakePlayer");
            this.fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable() {
        if (this.fakePlayer != null) {
            this.fakePlayer.despawnPlayer();
            this.fakePlayer = null;
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        this.fakePlayer = null;
        this.disable();
    }

    @EventListener
    public void onPushEntity(PushEntityEvent event) {
        if (event.getPushed().equals(FakePlayerModule.mc.player) && event.getPusher().equals(this.fakePlayer)) {
            event.setCanceled(true);
        }
    }
}
