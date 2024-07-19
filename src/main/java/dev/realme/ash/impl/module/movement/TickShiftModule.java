package dev.realme.ash.impl.module.movement;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.network.UpdateWalkingEvent;
import dev.realme.ash.init.Managers;
import dev.realme.ash.util.player.MovementUtil;

public class TickShiftModule
extends ToggleModule {
    final Config<Integer> ticksConfig = new NumberConfig<>("MaxTicks", "Maximum charge ticks", 1, 20, 40);
    final Config<Integer> packetsConfig = new NumberConfig<>("Packets", "Packets to release from storage every tick", 1, 1, 5);
    final Config<Integer> chargeSpeedConfig = new NumberConfig<>("ChargeSpeed", "The speed to charge the stored packets", 1, 1, 5);
    private int packets;

    public TickShiftModule() {
        super("TickShift", "Exploits NCP to speed up ticks", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return String.valueOf(this.packets);
    }

    @EventListener
    public void onPlayerUpdate(UpdateWalkingEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (MovementUtil.isMoving() || !TickShiftModule.mc.player.isOnGround()) {
            this.packets -= this.packetsConfig.getValue();
            if (this.packets <= 0) {
                this.packets = 0;
                Managers.TICK.setClientTick(1.0f);
                return;
            }
            Managers.TICK.setClientTick((float) this.packetsConfig.getValue() + 1.0f);
        } else {
            this.packets += this.chargeSpeedConfig.getValue();
            if (this.packets > this.ticksConfig.getValue()) {
                this.packets = this.ticksConfig.getValue();
            }
        }
    }
}
