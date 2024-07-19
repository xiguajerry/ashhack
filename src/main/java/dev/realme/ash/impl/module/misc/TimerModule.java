package dev.realme.ash.impl.module.misc;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.BooleanConfig;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.impl.event.render.TickCounterEvent;
import dev.realme.ash.init.Managers;
import java.text.DecimalFormat;

public class TimerModule
extends ToggleModule {
    final Config<Float> ticksConfig = new NumberConfig<>("Ticks", "The game tick speed", 0.1f, 2.0f, 50.0f);
    final Config<Boolean> tpsSyncConfig = new BooleanConfig("TPSSync", "Syncs game tick speed to server tick speed", false);
    private float prevTimer = -1.0f;
    private float timer = 1.0f;

    public TimerModule() {
        super("Timer", "Changes the client tick speed", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0#");
        return decimal.format(this.timer);
    }

    @Override
    public void toggle() {
        super.toggle();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE) {
            if (this.tpsSyncConfig.getValue()) {
                this.timer = Math.max(Managers.TICK.getTpsCurrent() / 20.0f, 0.1f);
                return;
            }
            this.timer = this.ticksConfig.getValue();
        }
    }

    @EventListener
    public void onTickCounter(TickCounterEvent event) {
        if (this.timer != 1.0f) {
            event.cancel();
            event.setTicks(this.timer);
        }
    }

    public float getTimer() {
        return this.timer;
    }

    public void setTimer(float timer) {
        this.prevTimer = this.timer;
        this.timer = timer;
    }

    public void resetTimer() {
        if (this.prevTimer > 0.0f) {
            this.timer = this.prevTimer;
            this.prevTimer = -1.0f;
        }
    }
}
