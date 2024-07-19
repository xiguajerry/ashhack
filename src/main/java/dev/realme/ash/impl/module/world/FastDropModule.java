package dev.realme.ash.impl.module.world;

import dev.realme.ash.api.config.Config;
import dev.realme.ash.api.config.setting.NumberConfig;
import dev.realme.ash.api.event.EventStage;
import dev.realme.ash.api.event.listener.EventListener;
import dev.realme.ash.api.module.ModuleCategory;
import dev.realme.ash.api.module.ToggleModule;
import dev.realme.ash.impl.event.TickEvent;
import dev.realme.ash.init.Managers;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FastDropModule
extends ToggleModule {
    Config<Integer> delayConfig = new NumberConfig<Integer>("Delay", "The delay for dropping items", 0, 0, 4);
    private int dropTicks;

    public FastDropModule() {
        super("FastDrop", "Drops items from the hotbar faster", ModuleCategory.WORLD);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (FastDropModule.mc.options.dropKey.isPressed() && this.dropTicks > this.delayConfig.getValue()) {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            this.dropTicks = 0;
        }
        ++this.dropTicks;
    }
}
