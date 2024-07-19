package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.ScreenOpenEvent;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.network.DisconnectEvent;
import dev.realme.ash.util.chat.ChatUtil;
import dev.realme.ash.util.math.timer.CacheTimer;
import dev.realme.ash.util.math.timer.Timer;
import net.minecraft.client.gui.screen.DeathScreen;

public class SpammerModule
extends ToggleModule {
    final Config<Float> delay = new NumberConfig<>("Delay", "", 0.0f, 500.0f, 5000.0f);
    private final Timer delayTimer = new CacheTimer();
    private boolean respawn;

    public SpammerModule() {
        super("Spammer", "Jing Minghui's dad", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (SpammerModule.nullCheck()) {
            return;
        }
        if (!this.delayTimer.passed(this.delay.getValue())) {
            return;
        }
        if (this.respawn) {
            return;
        }
        ChatUtil.serverSendCommand("kill");
        this.delayTimer.reset();
    }

    @EventListener
    public void onScreenOpen(ScreenOpenEvent event) {
        this.respawn = event.getScreen() instanceof DeathScreen;
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        this.disable();
    }
}
